@file:Suppress("FunctionName", "LocalVariableName", "SpellCheckingInspection", "PackageDirectoryMismatch", "SimplifyBooleanWithConstants")

package com.zoffcc.applications.trifa2

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.zoffcc.applications.trifa.MainActivity.Companion.DEBUG_MESSAGE_SCROLLING
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

class ChatScrollManager(
    val listState: LazyListState,
    val scrollToBottomOffset: Int,
    private val expectedItemCount: (Int) -> Int,
    private val snapThreshold: Int
) {
    var stickToBottom by mutableStateOf(true)
        private set

    var isInitialLoad by mutableStateOf(true)
        private set

    var prevMsgSize by mutableStateOf(0)
        private set

    var suppressScrollDetection by mutableStateOf(false)
        private set

    // -------------------------------------------------------------------
    // NEW STATE:
    //
    // True while the user is dragging the vertical scrollbar thumb.
    //
    // Compose does not reliably set listState.isScrollInProgress while the
    // desktop scrollbar is dragged, so we need this extra flag to classify
    // those movements as real USER scrolling.
    // -------------------------------------------------------------------
    var isScrollbarDragging by mutableStateOf(false)
        private set

    // -------------------------------------------------------------------
    // NEW STATE:
    //
    // True while a layout shift compensation is in progress.
    // This prevents multiple concurrent compensations from being triggered
    // when a bubble changes size (e.g., file transfer finishes).
    // -------------------------------------------------------------------
    var layoutShiftCompensationActive by mutableStateOf(false)
        private set

    private val scrollDebugState = object {
        var programmaticScrollActive = false
        var lastLoggedIndex = listState.firstVisibleItemIndex
        var lastLoggedOffset = listState.firstVisibleItemScrollOffset
    }

    fun resetForRoomSwitch(newSize: Int) {
        isInitialLoad = true
        stickToBottom = true
        prevMsgSize = newSize

        // Also reset transient scrollbar drag state on room switch.
        isScrollbarDragging = false

        // NEW: reset layout shift compensation state on room switch.
        layoutShiftCompensationActive = false

        if (DEBUG_MESSAGE_SCROLLING) {
            println("[ROOM SWITCH DETECTED] Reset state. stickToBottom: true, isInitialLoad: true, prevMsgSize: $prevMsgSize")
        }
    }

    // -------------------------------------------------------------------
    // NEW FUNCTION:
    //
    // This must be called when the user clicks "Load Older Messages".
    //
    // Loading older history is a user action that wants to stay at the
    // top/current position. It must never trigger an automatic jump to
    // the bottom.
    //
    // Therefore:
    //   - disable stick-to-bottom
    //   - disable any pending initial snap
    // -------------------------------------------------------------------
    fun userLoadedOlderMessages() {
        // Read old values only for debug logging.
        val oldStickToBottom = stickToBottom
        val oldIsInitialLoad = isInitialLoad

        // Force detach from bottom so no safety valve / smooth follower can snap down.
        stickToBottom = false

        // Cancel the initial snap logic, otherwise a pending initial snap could still
        // scroll the list to the bottom after the older messages are loaded.
        isInitialLoad = false

        if (DEBUG_MESSAGE_SCROLLING) {
            println("[LOAD OLDER MESSAGES] User requested older history. " +
                    "old stickToBottom=$oldStickToBottom, old isInitialLoad=$oldIsInitialLoad, " +
                    "new stickToBottom=false, new isInitialLoad=false")
        }
    }

    // -------------------------------------------------------------------
    // NEW FUNCTION:
    //
    // Called by the UI when the desktop scrollbar thumb is dragged.
    // This lets the scroll manager treat scrollbar movement as USER input.
    // -------------------------------------------------------------------
    fun onScrollbarDragChanged(dragging: Boolean) {
        if (isScrollbarDragging == dragging) {
            return
        }

        isScrollbarDragging = dragging

        if (DEBUG_MESSAGE_SCROLLING) {
            println("[SCROLLBAR DRAG] Scrollbar drag state changed to: $dragging")
        }
    }

    suspend fun jumpToBottom(minimumDbCount: Int) {
        if (DEBUG_MESSAGE_SCROLLING) {
            println("[JUMP TO BOTTOM] The user explicitly wants to go to the bottom, so re-attach")
        }
        // The user explicitly wants to go to the bottom, so re-attach.
        stickToBottom = true

        safeProgrammaticScroll {
            smoothScrollToBottom(minimumDbCount, force = true)
        }
    }

    suspend fun safeProgrammaticScroll(block: suspend () -> Unit) {
        suppressScrollDetection = true
        scrollDebugState.programmaticScrollActive = true
        var interrupted = false

        try {
            block()
        } catch (e: Exception) {
            if (e::class.simpleName?.contains("CancellationException") == true) {
                interrupted = true
            }

            if (DEBUG_MESSAGE_SCROLLING) {
                println("[PROGRAMMATIC SCROLL] Exception: ${e.message}")
            }
        } finally {
            if (!interrupted) {
                snapshotFlow { listState.isScrollInProgress }.first { !it }
                delay(50.milliseconds)
            }

            suppressScrollDetection = false
            scrollDebugState.programmaticScrollActive = false
        }
    }

    suspend fun smoothScrollToBottom(minimumDbCount: Int, force: Boolean = false) {
        var iteration = 0

        while (iteration < 14 && (stickToBottom || force)) {
            val expectedTotal = expectedItemCount(minimumDbCount)

            // Wait a short time for LazyColumn to contain all known items.
            withTimeoutOrNull(120.milliseconds) {
                snapshotFlow { listState.layoutInfo.totalItemsCount }
                    .first { it >= expectedTotal }
            }

            val layoutInfo = listState.layoutInfo

            if (layoutInfo.totalItemsCount < expectedTotal) {
                if (DEBUG_MESSAGE_SCROLLING) {
                    println("[SMOOTH BOTTOM] Layout not ready. total=${layoutInfo.totalItemsCount}, expected=$expectedTotal")
                }

                delay(24.milliseconds)
                iteration++
                continue
            }

            val targetIndex = expectedTotal - 1
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull() ?: break

            if (lastVisible.index >= targetIndex) {
                // If the final item/spacer is visible but has not been measured yet,
                // wait one more iteration.
                if (lastVisible.size <= 0) {
                    delay(16.milliseconds)
                    iteration++
                    continue
                }

                val itemEnd = lastVisible.offset + lastVisible.size
                val delta = itemEnd - layoutInfo.viewportEndOffset

                if (delta > 2f) {
                    val safeDelta = delta.coerceIn(1, 800)
                    val duration = (safeDelta / 2f).toInt().coerceIn(140, 320)

                    if (DEBUG_MESSAGE_SCROLLING) {
                        println("[SMOOTH BOTTOM] Exact follow. delta=$safeDelta, duration=$duration")
                    }

                    listState.animateScrollBy(
                        value = safeDelta.toFloat(),
                        animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
                    )
                } else {
                    if (DEBUG_MESSAGE_SCROLLING) {
                        println("[SMOOTH BOTTOM] Reached bottom. targetIndex=$targetIndex")
                    }
                    break
                }
            } else {
                val hiddenItems = targetIndex - lastVisible.index

                // If this is a manual FAB jump and we are far away, do not slowly animate.
                // Snap directly to the bottom.
                if (force && hiddenItems > 5) {
                    if (DEBUG_MESSAGE_SCROLLING) {
                        println("[SMOOTH BOTTOM] Forced jump is far away. hiddenItems=$hiddenItems, snapping to targetIndex=$targetIndex")
                    }

                    try {
                        listState.scrollToItem(targetIndex.coerceAtLeast(0), scrollToBottomOffset)
                    } catch (_: IndexOutOfBoundsException) {
                        val retryIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
                        if (retryIndex >= 0) {
                            try {
                                listState.scrollToItem(retryIndex, scrollToBottomOffset)
                            } catch (_: IndexOutOfBoundsException) {
                            }
                        }
                    }

                    break
                }

                val averageHeight = layoutInfo.visibleItemsInfo.map { it.size }.average().toFloat()
                val itemHeight = if (averageHeight > 0f) averageHeight else 120f

                val distance = (itemHeight * hiddenItems).coerceIn(80f, 1600f)
                val duration = (distance / 3f).toInt().coerceIn(220, 650)

                if (DEBUG_MESSAGE_SCROLLING) {
                    println("[SMOOTH BOTTOM] Catch up. hiddenItems=$hiddenItems, distance=$distance, duration=$duration")
                }

                listState.animateScrollBy(
                    value = distance,
                    animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
                )
            }

            iteration++
            delay(16.milliseconds)
        }

        // -----------------------------------------------------------------
        // Final synchronization.
        //
        // This prevents the case where the loop thinks it is finished, but
        // the layout has just inserted one more item.
        // -----------------------------------------------------------------
        val expectedTotal = expectedItemCount(minimumDbCount)

        withTimeoutOrNull(200.milliseconds) {
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .first { it >= expectedTotal }
        }

        val finalLayoutInfo = listState.layoutInfo

        if (!stickToBottom && !force) return
        if (finalLayoutInfo.totalItemsCount < expectedTotal) return

        val targetIndex = expectedTotal - 1
        val lastVisible = finalLayoutInfo.visibleItemsInfo.lastOrNull()

        if (lastVisible == null) {
            // Very unusual, but do a safe correction.
            listState.scrollToItem(targetIndex.coerceAtLeast(0), scrollToBottomOffset)
            return
        }

        if (lastVisible.index >= targetIndex) {
            if (lastVisible.size <= 0) return

            val itemEnd = lastVisible.offset + lastVisible.size
            val delta = itemEnd - finalLayoutInfo.viewportEndOffset

            if (delta > 2f) {
                val safeDelta = delta.coerceIn(1, 350)
                val duration = (safeDelta / 2f).toInt().coerceIn(120, 260)

                if (DEBUG_MESSAGE_SCROLLING) {
                    println("[SMOOTH BOTTOM] Final micro correction. delta=$safeDelta, duration=$duration")
                }

                listState.animateScrollBy(
                    value = safeDelta.toFloat(),
                    animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
                )
            }
        } else {
            val hidden = targetIndex - lastVisible.index

            if (hidden <= 2) {
                val averageHeight = finalLayoutInfo.visibleItemsInfo.map { it.size }.average().toFloat()
                val itemHeight = if (averageHeight > 0f) averageHeight else 120f
                val distance = (itemHeight * hidden).coerceIn(40f, 450f)
                val duration = (distance / 2f).toInt().coerceIn(140, 320)

                if (DEBUG_MESSAGE_SCROLLING) {
                    println("[SMOOTH BOTTOM] Final catch up. hidden=$hidden, distance=$distance, duration=$duration")
                }

                listState.animateScrollBy(
                    value = distance,
                    animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
                )
            } else {
                // If we are still far away after all smooth attempts, force correctness.
                if (DEBUG_MESSAGE_SCROLLING) {
                    println("[SMOOTH BOTTOM] Final fallback snap. hidden=$hidden")
                }

                listState.scrollToItem(targetIndex.coerceAtLeast(0), scrollToBottomOffset)
            }
        }
    }

    fun startScrollMonitoring(scope: CoroutineScope) {
        if (DEBUG_MESSAGE_SCROLLING) {
            println("[INIT] Setting up scroll monitoring flows.")
        }

        // 1. Monitor exact scroll position changes
        scope.launch {
            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                .collect { (index, offset) ->
                    if (index != scrollDebugState.lastLoggedIndex || offset != scrollDebugState.lastLoggedOffset) {
                        val cause = when {
                            scrollDebugState.programmaticScrollActive -> "PROGRAMMATIC"
                            listState.isScrollInProgress -> "USER"

                            // NEW:
                            // The desktop scrollbar may move the list without setting
                            // isScrollInProgress, so classify that as USER_SCROLLBAR.
                            isScrollbarDragging -> "USER_SCROLLBAR"

                            else -> "LAYOUT_SHIFT"
                        }

                        if (DEBUG_MESSAGE_SCROLLING) {
                            println("[SCROLL POSITION CHANGED] Index: $index (was ${scrollDebugState.lastLoggedIndex}), Offset: $offset (was ${scrollDebugState.lastLoggedOffset}). Cause: $cause. isScrollInProgress: ${listState.isScrollInProgress}")
                        }

                        scrollDebugState.lastLoggedIndex = index
                        scrollDebugState.lastLoggedOffset = offset
                    }
                }
        }

        // 2. Monitor layout item count changes
        scope.launch {
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .collect { count ->
                    if (DEBUG_MESSAGE_SCROLLING) {
                        println("[LAYOUT INFO] Total items count in LazyColumn changed to: $count")
                    }
                }
        }

        // 3. Bottom monitor
        // Only detach stickToBottom when the USER manually scrolls away from the bottom.
        // Attach stickToBottom when the user manually scrolls to the bottom, or stops at the bottom.
        //
        // Manual scrolling means:
        //   - mouse wheel / touch scroll / fling -> listState.isScrollInProgress == true
        //   - desktop scrollbar thumb drag       -> isScrollbarDragging == true
        //
        // NEW: Also detect layout shifts (e.g., bubble size changes) while stickToBottom is active,
        // and automatically scroll down to keep the bottom visible.
        scope.launch {
            var prevFirstVisible = listState.firstVisibleItemIndex
            var prevFirstOffset = listState.firstVisibleItemScrollOffset

            snapshotFlow {
                val layoutInfo = listState.layoutInfo
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                val totalItems = layoutInfo.totalItemsCount
                val isScrolling = listState.isScrollInProgress
                val firstVisible = listState.firstVisibleItemIndex
                val firstOffset = listState.firstVisibleItemScrollOffset

                // NEW: include scrollbar drag state so this flow also reacts to scrollbar drags
                val scrollbarDragging = isScrollbarDragging

                // We define "atBottom" as:
                // - The very last item (spacer) is visible
                // - OR the second to last item (last message) is fully visible
                val atBottom = lastVisibleItem == null ||
                        lastVisibleItem.index == totalItems - 1 ||
                        (lastVisibleItem.index == totalItems - 2 &&
                                lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset + 5)

                listOf(isScrolling, firstVisible, firstOffset, atBottom, scrollbarDragging)
            }.collect { state ->
                // Do not process if a programmatic scroll is currently suppressing detection
                if (suppressScrollDetection) return@collect

                val isScrolling = state[0] as Boolean
                val firstVisible = state[1] as Int
                val firstOffset = state[2] as Int
                val atBottom = state[3] as Boolean
                val scrollbarDragging = state[4] as Boolean

                // NEW:
                // A real user scroll is either normal wheel/touch scrolling
                // or dragging the desktop scrollbar thumb.
                val userScrollActive = isScrolling || scrollbarDragging

                if (userScrollActive && !scrollDebugState.programmaticScrollActive) {
                    // User is actively and manually scrolling

                    // Determine if user is scrolling away from bottom (upwards)
                    val scrollingUp = firstVisible < prevFirstVisible ||
                            (firstVisible == prevFirstVisible && firstOffset < prevFirstOffset)

                    // Determine if user is scrolling towards bottom (downwards)
                    val scrollingDown = firstVisible > prevFirstVisible ||
                            (firstVisible == prevFirstVisible && firstOffset > prevFirstOffset)

                    if (scrollingUp && !atBottom) {
                        // User manually scrolled up and is no longer at the bottom -> detach
                        if (stickToBottom) {
                            stickToBottom = false

                            if (DEBUG_MESSAGE_SCROLLING) {
                                // NEW: log whether this was wheel/touch or scrollbar dragging
                                val scrollSource = if (isScrolling) "wheel/touch" else "scrollbar"
                                println("[BOTTOM MONITOR] User manually scrolled away from bottom via $scrollSource. Detaching.")
                            }
                        }
                    } else if (scrollingDown && atBottom) {
                        // User manually scrolled down and reached the bottom -> attach
                        if (!stickToBottom) {
                            stickToBottom = true

                            if (DEBUG_MESSAGE_SCROLLING) {
                                // NEW: log whether this was wheel/touch or scrollbar dragging
                                val scrollSource = if (isScrolling) "wheel/touch" else "scrollbar"
                                println("[BOTTOM MONITOR] User manually scrolled to bottom via $scrollSource. Attaching.")
                            }
                        }
                    }
                } else if (!userScrollActive && !scrollDebugState.programmaticScrollActive) {
                    // When manual scrolling stops, if we happen to be at the bottom, we attach
                    if (atBottom && !stickToBottom) {
                        stickToBottom = true

                        if (DEBUG_MESSAGE_SCROLLING) {
                            println("[BOTTOM MONITOR] Manual scrolling stopped at bottom. Attaching.")
                        }
                    }
                    // -------------------------------------------------------------------
                    // NEW: LAYOUT SHIFT COMPENSATION
                    //
                    // If stickToBottom is active, but we're not at the bottom anymore,
                    // and it's not a user scroll, it means a layout shift occurred
                    // (e.g., a bubble at the bottom grew in size, pushing the bottom out of view).
                    //
                    // We need to scroll down to keep the bottom visible.
                    // -------------------------------------------------------------------
                    else if (!atBottom && stickToBottom && !layoutShiftCompensationActive) {
                        if (DEBUG_MESSAGE_SCROLLING) {
                            println("[BOTTOM MONITOR] Layout shift detected while stickToBottom is active. Scrolling to bottom to compensate.")
                        }

                        layoutShiftCompensationActive = true
                        scope.launch {
                            try {
                                safeProgrammaticScroll {
                                    // Pass prevMsgSize so it calculates the correct expected total items.
                                    smoothScrollToBottom(prevMsgSize)
                                }
                            } finally {
                                layoutShiftCompensationActive = false
                            }
                        }
                    }
                }

                // Update previous state for the next frame
                prevFirstVisible = firstVisible
                prevFirstOffset = firstOffset
            }
        }
    }

    suspend fun handleSafetyValve(currentSize: Int) {
        val sizeDelta = abs(currentSize - prevMsgSize)

        if (DEBUG_MESSAGE_SCROLLING) {
            println("[MSG STORE] Size/Serial changed: $currentSize (prev: $prevMsgSize). Delta: $sizeDelta. Layout total items: ${listState.layoutInfo.totalItemsCount}")
        }

        if (stickToBottom && currentSize > 0 && sizeDelta >= snapThreshold) {
            val targetIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)

            if (DEBUG_MESSAGE_SCROLLING) {
                println("[ASYNC ENGINE INTERCEPTOR] Sudden jump detected! Enforcing hard scroll snap safety to index: $targetIndex")
            }

            safeProgrammaticScroll {
                try {
                    listState.scrollToItem(targetIndex, scrollToBottomOffset)

                    if (DEBUG_MESSAGE_SCROLLING) {
                        println("[ASYNC ENGINE INTERCEPTOR] Snap successful to index: $targetIndex")
                    }
                } catch (_: IndexOutOfBoundsException) {
                    if (DEBUG_MESSAGE_SCROLLING) {
                        println("[ASYNC ENGINE INTERCEPTOR] IndexOutOfBoundsException during snap! Retrying...")
                    }

                    val retryIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)

                    if (retryIndex >= 0) {
                        try {
                            listState.scrollToItem(retryIndex, scrollToBottomOffset)
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }

        prevMsgSize = currentSize
    }

    suspend fun startSmoothFollow(getState: () -> Pair<Any?, Int>) {
        var prevFollowSerial = getState().first
        var prevFollowSize = getState().second

        snapshotFlow { getState() }
            .collect { (currentSerial, currentSize) ->
                val logicalDelta = if (currentSerial != prevFollowSerial) {
                    if (currentSize != prevFollowSize) {
                        currentSize - prevFollowSize
                    } else {
                        // Size didn't change but the last message ID changed.
                        // This happens when a capped queue drops an old message to add a new one.
                        1
                    }
                } else {
                    0
                }

                if (!isInitialLoad && stickToBottom && logicalDelta > 0 && logicalDelta < snapThreshold) {
                    if (DEBUG_MESSAGE_SCROLLING) {
                        println("[SMOOTH FOLLOW] New messages detected. Following bottom smoothly. logicalDelta=$logicalDelta")
                    }

                    safeProgrammaticScroll {
                        smoothScrollToBottom(currentSize)
                    }
                }

                prevFollowSerial = currentSerial
                prevFollowSize = currentSize
            }
    }

    suspend fun handleInitialSnap(lastSerial: Any?, currentSize: Int) {
        if (DEBUG_MESSAGE_SCROLLING) {
            println("[LAST SERIAL EFFECT] Triggered. LastSerial: $lastSerial, isInitialLoad: $isInitialLoad, stickToBottom: $stickToBottom, DB size: $currentSize")
        }

        if (lastSerial != null) {
            if (isInitialLoad) {
                if (currentSize > 0) {
                    val targetLayoutIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)

                    if (DEBUG_MESSAGE_SCROLLING) {
                        println("[EXECUTION: INITIAL SNAP] Snapping to bottom index: $targetLayoutIndex. Total items in DB: $currentSize")
                    }

                    safeProgrammaticScroll {
                        try {
                            listState.scrollToItem(targetLayoutIndex, scrollToBottomOffset)
                        } catch (_: IndexOutOfBoundsException) {
                            if (DEBUG_MESSAGE_SCROLLING) {
                                println("[EXECUTION: INITIAL SNAP] IndexOutOfBoundsException. Retrying...")
                            }

                            val retryIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)

                            if (retryIndex >= 0) {
                                try {
                                    listState.scrollToItem(retryIndex, scrollToBottomOffset)
                                } catch (_: Exception) {
                                }
                            }
                        }
                    }
                }

                isInitialLoad = false
            } else if (stickToBottom) {
                if (DEBUG_MESSAGE_SCROLLING) {
                    println("[LAST SERIAL EFFECT] stickToBottom follow delegated to smooth follower.")
                }
            } else {
                if (DEBUG_MESSAGE_SCROLLING) {
                    println("[EXECUTION: SKIPPED] User scrolled away from bottom.")
                }
            }
        } else {
            if (DEBUG_MESSAGE_SCROLLING) {
                println("[LAST SERIAL EFFECT] lastSerial is null. Skipping execution.")
            }
        }
    }
}

@Composable
fun rememberChatScrollManager(
    listState: LazyListState,
    messagesSize: Int,
    lastSerial: Any?,
    selectedRoomId: String?,
    expectedItemCount: (Int) -> Int,
    snapToBottomNewItemCount: Int,
    scrollToBottomOffset: Int
): ChatScrollManager {

    val manager = remember {
        ChatScrollManager(
            listState = listState,
            scrollToBottomOffset = scrollToBottomOffset,
            expectedItemCount = expectedItemCount,
            snapThreshold = snapToBottomNewItemCount
        )
    }

    var prevGroupId by remember { mutableStateOf(selectedRoomId) }

    if (prevGroupId != selectedRoomId) {
        if (DEBUG_MESSAGE_SCROLLING) {
            println("[ROOM SWITCH DETECTED] Resetting Parameters. Old: $prevGroupId, New: $selectedRoomId")
        }

        prevGroupId = selectedRoomId
        manager.resetForRoomSwitch(messagesSize)
    }

    val currentMessagesSize by rememberUpdatedState(messagesSize)
    val currentLastSerial by rememberUpdatedState(lastSerial)

    LaunchedEffect(Unit) {
        manager.startScrollMonitoring(this)
    }

    LaunchedEffect(messagesSize, lastSerial) {
        manager.handleSafetyValve(messagesSize)
    }

    LaunchedEffect(selectedRoomId) {
        manager.startSmoothFollow { currentLastSerial to currentMessagesSize }
    }

    LaunchedEffect(lastSerial, selectedRoomId) {
        manager.handleInitialSnap(lastSerial, messagesSize)
    }

    return manager
}

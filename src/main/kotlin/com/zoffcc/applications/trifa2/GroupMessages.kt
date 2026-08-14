@file:Suppress("FunctionName", "LocalVariableName", "SpellCheckingInspection", "PackageDirectoryMismatch", "SimplifyBooleanWithConstants")

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zoffcc.applications.trifa.MainActivity.Companion.DEBUG_MESSAGE_SCROLLING
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

// ============================================================================
// REUSABLE SCROLL MANAGER (Can be moved to its own file: ChatScrollManager.kt)
// ============================================================================

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

    private val scrollDebugState = object {
        var programmaticScrollActive = false
        var lastLoggedIndex = listState.firstVisibleItemIndex
        var lastLoggedOffset = listState.firstVisibleItemScrollOffset
    }

    fun resetForRoomSwitch(newSize: Int) {
        isInitialLoad = true
        stickToBottom = true
        prevMsgSize = newSize
        if (DEBUG_MESSAGE_SCROLLING) {
            println("[ROOM SWITCH DETECTED] Reset state. stickToBottom: true, isInitialLoad: true, prevMsgSize: $prevMsgSize")
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
            if (DEBUG_MESSAGE_SCROLLING) println("[PROGRAMMATIC SCROLL] Exception: ${e.message}")
        } finally {
            if (!interrupted) {
                snapshotFlow { listState.isScrollInProgress }.first { !it }
                delay(50.milliseconds)
            }

            suppressScrollDetection = false
            scrollDebugState.programmaticScrollActive = false
        }
    }

    suspend fun smoothScrollToBottom(minimumDbCount: Int) {
        var iteration = 0

        while (iteration < 14 && stickToBottom) {
            val expectedTotal = expectedItemCount(minimumDbCount)

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

        val expectedTotal = expectedItemCount(minimumDbCount)

        withTimeoutOrNull(200.milliseconds) {
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .first { it >= expectedTotal }
        }

        val finalLayoutInfo = listState.layoutInfo

        if (!stickToBottom) return
        if (finalLayoutInfo.totalItemsCount < expectedTotal) return

        val targetIndex = expectedTotal - 1
        val lastVisible = finalLayoutInfo.visibleItemsInfo.lastOrNull()

        if (lastVisible == null) {
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
                if (DEBUG_MESSAGE_SCROLLING) {
                    println("[SMOOTH BOTTOM] Final fallback snap. hidden=$hidden")
                }
                listState.scrollToItem(targetIndex.coerceAtLeast(0), scrollToBottomOffset)
            }
        }
    }

    fun startScrollMonitoring(scope: CoroutineScope) {
        if (DEBUG_MESSAGE_SCROLLING) println("[INIT] Setting up scroll monitoring flows.")

        scope.launch {
            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                .collect { (index, offset) ->
                    if (index != scrollDebugState.lastLoggedIndex || offset != scrollDebugState.lastLoggedOffset) {
                        val cause = when {
                            scrollDebugState.programmaticScrollActive -> "PROGRAMMATIC"
                            listState.isScrollInProgress -> "USER"
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

        scope.launch {
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .collect { count ->
                    if (DEBUG_MESSAGE_SCROLLING) println("[LAYOUT INFO] Total items count in LazyColumn changed to: $count")
                }
        }

        scope.launch {
            snapshotFlow { listState.isScrollInProgress }.collect { isScrolling ->
                if (DEBUG_MESSAGE_SCROLLING) {
                    val cause = if (scrollDebugState.programmaticScrollActive) "PROGRAMMATIC" else "USER"
                    println("[BOTTOM MONITOR] Scroll state changed. isScrolling: $isScrolling. Cause: $cause")
                }

                val layoutInfo = listState.layoutInfo
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()
                val atBottom = lastVisible != null && lastVisible.index >= layoutInfo.totalItemsCount - 2

                if (!isScrolling && !suppressScrollDetection) {
                    if (DEBUG_MESSAGE_SCROLLING) {
                        println("[BOTTOM MONITOR] Scroll stopped. TotalItems: ${layoutInfo.totalItemsCount}, LastVisibleIndex: ${lastVisible?.index}, atBottom: $atBottom, CurrentStickToBottom: $stickToBottom")
                    }

                    if (atBottom) {
                        if (!stickToBottom && DEBUG_MESSAGE_SCROLLING) println("[BOTTOM MONITOR] Reached bottom, attaching.")
                        stickToBottom = true
                    } else {
                        if (stickToBottom && DEBUG_MESSAGE_SCROLLING) println("[BOTTOM MONITOR] User scrolled UP. Detaching from bottom.")
                        stickToBottom = false
                    }
                }
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
                    if (DEBUG_MESSAGE_SCROLLING) println("[ASYNC ENGINE INTERCEPTOR] Snap successful to index: $targetIndex")
                } catch (_: IndexOutOfBoundsException) {
                    if (DEBUG_MESSAGE_SCROLLING) println("[ASYNC ENGINE INTERCEPTOR] IndexOutOfBoundsException during snap! Retrying...")
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
                            if (DEBUG_MESSAGE_SCROLLING) println("[EXECUTION: INITIAL SNAP] IndexOutOfBoundsException. Retrying...")
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
                if (DEBUG_MESSAGE_SCROLLING) println("[EXECUTION: SKIPPED] User scrolled away from bottom.")
            }
        } else {
            if (DEBUG_MESSAGE_SCROLLING) println("[LAST SERIAL EFFECT] lastSerial is null. Skipping execution.")
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

    // Use rememberUpdatedState to ensure the lambda inside startSmoothFollow
    // always reads the absolute latest values without triggering a relaunch.
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


// ============================================================================
// UI COMPOSABLES
// ============================================================================

@Composable
internal fun GroupMessages(ui_scale: Float, selectedGroupId: String?,
                           onReplySelected: (UIGroupMessage) -> Unit,
                           onDeleteSelected: (UIGroupMessage) -> Unit,
                           onEmojiSelected: (UIGroupMessage, String) -> Unit
) {
    val listState = rememberLazyListState()
    val grpmsgs by groupmessagestore.stateFlow.collectAsState()

    if (DEBUG_MESSAGE_SCROLLING) {
        println("[STATE] GroupMessages recomposed. DB Messages: ${grpmsgs.groupmessages.size}, SelectedGroupId: $selectedGroupId")
    }

    val lastSerial = grpmsgs.groupmessages.lastOrNull()?.msgDatabaseId
    val messagesSize = grpmsgs.groupmessages.size

    // All complex scrolling logic is neatly handled by the manager here
    val scrollManager = rememberChatScrollManager(
        listState = listState,
        messagesSize = messagesSize,
        lastSerial = lastSerial,
        selectedRoomId = selectedGroupId,
        expectedItemCount = { dbCount ->
            val loadMoreCount = if (!groupstore.state.fullHistoryActive) 1 else 0
            dbCount + loadMoreCount + 2
        },
        snapToBottomNewItemCount = SNAP_TO_BOTTOM_NEW_ITEM_COUNT,
        scrollToBottomOffset = LAST_MSG_SCROLL_TO_SCROLL_OFFSET
    )

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(start = 4.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            state = listState
        ) {
            item(key = "FIRST_ITEM") { Spacer(Modifier.size(SPACE_BEFORE_FIRST_MESSAGE)) }

            if (!groupstore.state.fullHistoryActive) {
                item(key = "load_more_button_key") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = {
                            if (DEBUG_MESSAGE_SCROLLING) println("[USER ACTION] Load Older Messages button clicked.")
                            groupstore.fullHistoryActive(true)
                        }) {
                            Text("Load Older Messages")
                        }
                    }
                }
            }

            items(grpmsgs.groupmessages, key = { it.msgDatabaseId }) {
                GroupChatMessage(
                    isMyMessage = it.user == myUser,
                    groupmessage = it,
                    ui_scale = ui_scale,
                    onReplySelected = onReplySelected,
                    onDeleteSelected = onDeleteSelected,
                    onEmojiSelected = onEmojiSelected
                )
            }

            item(key = "LAST_ITEM") { Box(Modifier.height(SPACE_AFTER_LAST_MESSAGE)) }
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.fillMaxHeight().align(CenterEnd).width(10.dp)
        )
    }
}

@Composable
fun PeerPic(user: User, ui_scale: Float) {
    val imageSize = calc_avatar_size(AVATAR_SIZE * ui_scale)

    val painter = user.picture?.let {
        painterResource(it)
    } ?: object : Painter() {
        override val intrinsicSize: Size = Size(imageSize, imageSize)
        override fun DrawScope.onDraw() {
            drawRect(user.color, size = Size(imageSize * 4, imageSize * 4))
        }
    }

    Image(
        modifier = Modifier.size(imageSize.dp).clip(CircleShape),
        contentScale = ContentScale.Crop,
        painter = painter,
        contentDescription = "Peer picture",
        colorFilter = ColorFilter.tint(user.color, BlendMode.Modulate)
    )
}

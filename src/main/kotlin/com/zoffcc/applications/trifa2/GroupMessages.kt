@file:Suppress("FunctionName", "LocalVariableName", "SpellCheckingInspection", "PackageDirectoryMismatch")

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.ExperimentalResourceApi

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

    var stickToBottom by remember { mutableStateOf(true) }
    var suppressScrollDetection by remember { mutableStateOf(false) }
    var prevMsgSize by remember { mutableStateOf(grpmsgs.groupmessages.size) }
    var prevGroupId by remember { mutableStateOf(selectedGroupId) }
    var isInitialLoad by remember { mutableStateOf(true) }

    val scrollDebugState = remember {
        object {
            var programmaticScrollActive = false
            var lastLoggedIndex = listState.firstVisibleItemIndex
            var lastLoggedOffset = listState.firstVisibleItemScrollOffset
        }
    }

    // ---------------------------------------------------------------------
    // Expected number of items in the LazyColumn.
    //
    // LazyColumn contains:
    //   1x FIRST_ITEM spacer
    //   0/1x Load Older Messages button
    //   Nx group messages
    //   1x LAST_ITEM spacer
    // ---------------------------------------------------------------------
    fun expectedLazyItemCount(minimumDbCount: Int = 0): Int {
        val dbCount = maxOf(grpmsgs.groupmessages.size, minimumDbCount)
        val loadMoreCount = if (!groupstore.state.fullHistoryActive) 1 else 0
        return dbCount + loadMoreCount + 2
    }

    // ---------------------------------------------------------------------
    // HELPER: Protect programmatic scrolling from being interpreted as user
    // scrolling, and wait until the LazyColumn has really settled.
    // ---------------------------------------------------------------------
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
                delay(50)
            }

            suppressScrollDetection = false
            scrollDebugState.programmaticScrollActive = false
        }
    }

    // ---------------------------------------------------------------------
    // HELPER: Smooth chat-style follow-to-bottom.
    //
    // Important:
    // We must wait until LazyColumn layout has caught up with the DB size.
    // Otherwise we may scroll to the OLD bottom and stop too early.
    // ---------------------------------------------------------------------
    suspend fun smoothScrollToBottom(minimumDbCount: Int) {
        var iteration = 0

        while (iteration < 14 && stickToBottom) {
            val expectedTotal = expectedLazyItemCount(minimumDbCount)

            // Wait a short time for LazyColumn to contain all known items.
            withTimeoutOrNull(120) {
                snapshotFlow { listState.layoutInfo.totalItemsCount }
                    .first { it >= expectedTotal }
            }

            val layoutInfo = listState.layoutInfo

            if (layoutInfo.totalItemsCount < expectedTotal) {
                if (DEBUG_MESSAGE_SCROLLING) {
                    println("[SMOOTH BOTTOM] Layout not ready. total=${layoutInfo.totalItemsCount}, expected=$expectedTotal")
                }
                delay(24)
                iteration++
                continue
            }

            val targetIndex = expectedTotal - 1
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull() ?: break

            if (lastVisible.index >= targetIndex) {
                // If the final item/spacer is visible but has not been measured yet,
                // wait one more iteration.
                if (lastVisible.size <= 0) {
                    delay(16)
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
            delay(16)
        }

        // -----------------------------------------------------------------
        // Final synchronization.
        //
        // This prevents the case where the loop thinks it is finished, but
        // the layout has just inserted one more item.
        // -----------------------------------------------------------------
        val expectedTotal = expectedLazyItemCount(minimumDbCount)

        withTimeoutOrNull(200) {
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .first { it >= expectedTotal }
        }

        val finalLayoutInfo = listState.layoutInfo

        if (!stickToBottom) return
        if (finalLayoutInfo.totalItemsCount < expectedTotal) return

        val targetIndex = expectedTotal - 1
        val lastVisible = finalLayoutInfo.visibleItemsInfo.lastOrNull()

        if (lastVisible == null) {
            // Very unusual, but do a safe correction.
            listState.scrollToItem(targetIndex.coerceAtLeast(0), LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
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
                listState.scrollToItem(targetIndex.coerceAtLeast(0), LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
            }
        }
    }

    // ---------------------------------------------------------------------
    // SCROLL MONITORING & BOTTOM DETECTION
    // ---------------------------------------------------------------------
    LaunchedEffect(Unit) {
        if (DEBUG_MESSAGE_SCROLLING) println("[INIT] Setting up scroll monitoring flows.")

        // 1. Monitor exact scroll position changes
        launch {
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

        // 2. Monitor layout item count changes
        launch {
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .collect { count ->
                    if (DEBUG_MESSAGE_SCROLLING) println("[LAYOUT INFO] Total items count in LazyColumn changed to: $count")
                }
        }

        // 3. Bottom monitor
        launch {
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

    // ---------------------------------------------------------------------
    // SAFETY VALVE: Large asynchronous jumps still use a hard snap.
    // ---------------------------------------------------------------------
    LaunchedEffect(grpmsgs.groupmessages.size) {
        val currentSize = grpmsgs.groupmessages.size
        val sizeDelta = kotlin.math.abs(currentSize - prevMsgSize)

        if (DEBUG_MESSAGE_SCROLLING) {
            println("[MSG STORE] Size changed: $currentSize (prev: $prevMsgSize). Delta: $sizeDelta. Layout total items: ${listState.layoutInfo.totalItemsCount}")
        }

        if (stickToBottom && grpmsgs.groupmessages.isNotEmpty() && sizeDelta >= SNAP_TO_BOTTOM_NEW_ITEM_COUNT) {
            val targetIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)

            if (DEBUG_MESSAGE_SCROLLING) {
                println("[ASYNC ENGINE INTERCEPTOR] Sudden jump detected! Enforcing hard scroll snap safety to index: $targetIndex")
            }

            safeProgrammaticScroll {
                try {
                    listState.scrollToItem(targetIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
                    if (DEBUG_MESSAGE_SCROLLING) println("[ASYNC ENGINE INTERCEPTOR] Snap successful to index: $targetIndex")
                } catch (e: IndexOutOfBoundsException) {
                    if (DEBUG_MESSAGE_SCROLLING) println("[ASYNC ENGINE INTERCEPTOR] IndexOutOfBoundsException during snap! Retrying...")
                    val retryIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
                    if (retryIndex >= 0) {
                        try {
                            listState.scrollToItem(retryIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }

        prevMsgSize = currentSize
    }

    // ---------------------------------------------------------------------
    // SMOOTH FOLLOW PIPELINE
    // ---------------------------------------------------------------------
    LaunchedEffect(selectedGroupId) {
        var prevFollowSize = grpmsgs.groupmessages.size

        snapshotFlow { grpmsgs.groupmessages.size }
            .collect { currentSize ->
                val delta = currentSize - prevFollowSize

                if (!isInitialLoad && stickToBottom && delta > 0 && delta < SNAP_TO_BOTTOM_NEW_ITEM_COUNT) {
                    if (DEBUG_MESSAGE_SCROLLING) {
                        println("[SMOOTH FOLLOW] New messages detected. Following bottom smoothly. delta=$delta")
                    }

                    safeProgrammaticScroll {
                        smoothScrollToBottom(currentSize)
                    }
                }

                prevFollowSize = currentSize
            }
    }

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

        // ---------------------------------------------------------------------
        // Room switch detector
        // ---------------------------------------------------------------------
        if (prevGroupId != selectedGroupId) {
            if (DEBUG_MESSAGE_SCROLLING) {
                println("[ROOM SWITCH DETECTED] Resetting Parameters. Old: $prevGroupId, New: $selectedGroupId")
            }

            prevGroupId = selectedGroupId
            isInitialLoad = true
            stickToBottom = true
            prevMsgSize = grpmsgs.groupmessages.size

            if (DEBUG_MESSAGE_SCROLLING) {
                println("[ROOM SWITCH DETECTED] Reset state. stickToBottom: true, isInitialLoad: true, prevMsgSize: $prevMsgSize")
            }
        }

        // ---------------------------------------------------------------------
        // Initial snap pipeline
        // ---------------------------------------------------------------------
        val lastSerial = grpmsgs.groupmessages.lastOrNull()?.msgDatabaseId

        LaunchedEffect(lastSerial, selectedGroupId) {
            if (DEBUG_MESSAGE_SCROLLING) {
                println("[LAST SERIAL EFFECT] Triggered. LastSerial: $lastSerial, SelectedGroupId: $selectedGroupId, isInitialLoad: $isInitialLoad, stickToBottom: $stickToBottom, DB size: ${grpmsgs.groupmessages.size}")
            }

            if (lastSerial != null) {
                if (isInitialLoad) {
                    if (grpmsgs.groupmessages.isNotEmpty()) {
                        val targetLayoutIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)

                        if (DEBUG_MESSAGE_SCROLLING) {
                            println("[EXECUTION: INITIAL SNAP] Snapping to bottom index: $targetLayoutIndex. Total items in DB: ${grpmsgs.groupmessages.size}")
                        }

                        safeProgrammaticScroll {
                            try {
                                listState.scrollToItem(targetLayoutIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
                            } catch (e: IndexOutOfBoundsException) {
                                if (DEBUG_MESSAGE_SCROLLING) println("[EXECUTION: INITIAL SNAP] IndexOutOfBoundsException. Retrying...")
                                val retryIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
                                if (retryIndex >= 0) {
                                    try {
                                        listState.scrollToItem(retryIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        }
                    }

                    isInitialLoad = false
                } else if (stickToBottom) {
                    // Normal new-message following is handled by the dedicated
                    // smooth-follow LaunchedEffect(selectedGroupId) above.
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
}

@OptIn(ExperimentalResourceApi::class)
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

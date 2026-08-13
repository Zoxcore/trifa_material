@file:Suppress("FunctionName", "LocalVariableName", "SpellCheckingInspection", "PackageDirectoryMismatch")

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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

    // SIMPLIFIED BOTTOM MONITOR
    // We only evaluate stickToBottom when a user scroll STOPS.
    // This naturally ignores layout jitter and message insertions!
    LaunchedEffect(Unit) {
        snapshotFlow { listState.isScrollInProgress }.collect { isScrolling ->
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()
            val atBottom = lastVisible != null && lastVisible.index >= layoutInfo.totalItemsCount - 2

            if (DEBUG_MESSAGE_SCROLLING) {
                println("[BOTTOM MONITOR] Scroll stopped. TotalItems: ${layoutInfo.totalItemsCount}, LastVisibleIndex: ${lastVisible?.index}, atBottom: $atBottom, CurrentStickToBottom: $stickToBottom")
            }

            if (!isScrolling && !suppressScrollDetection) {
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

    // SAFETY VALVE: Detects asynchronous store recoveries
    LaunchedEffect(grpmsgs.groupmessages.size) {
        val currentSize = grpmsgs.groupmessages.size
        val sizeDelta = kotlin.math.abs(currentSize - prevMsgSize)

        if (DEBUG_MESSAGE_SCROLLING) {
            println("[MSG STORE] Size changed: $currentSize (prev: $prevMsgSize). Delta: $sizeDelta. Layout total items: ${listState.layoutInfo.totalItemsCount}")
        }

        if (stickToBottom && grpmsgs.groupmessages.isNotEmpty() && sizeDelta >= SNAP_TO_BOTTOM_NEW_ITEM_COUNT) {
            val targetIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
            if (DEBUG_MESSAGE_SCROLLING) println("[ASYNC ENGINE INTERCEPTOR] Sudden jump detected! Enforcing hard scroll snap safety to index: $targetIndex")

            suppressScrollDetection = true
            try {
                listState.scrollToItem(targetIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
                if (DEBUG_MESSAGE_SCROLLING) println("[ASYNC ENGINE INTERCEPTOR] Snap successful to index: $targetIndex")
            } catch (e: IndexOutOfBoundsException) {
                if (DEBUG_MESSAGE_SCROLLING) println("[ASYNC ENGINE INTERCEPTOR] IndexOutOfBoundsException during snap! Retrying...")
                val retryIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
                if (retryIndex >= 0) {
                    try { listState.scrollToItem(retryIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET) } catch (_: Exception) {}
                }
            }
            suppressScrollDetection = false
        }
        prevMsgSize = currentSize
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(start = 4.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            state = listState
        ) {
            item (key = "FIRST_ITEM") { Spacer(Modifier.size(SPACE_BEFORE_FIRST_MESSAGE)) }
            if (!groupstore.state.fullHistoryActive) {
                item(key = "load_more_button_key") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Button(onClick = { groupstore.fullHistoryActive(true) }) { Text("Load Older Messages") }
                    }
                }
            }
            items(grpmsgs.groupmessages, key = { it.msgDatabaseId }) {
                GroupChatMessage(
                    isMyMessage = it.user == myUser, groupmessage = it, ui_scale = ui_scale,
                    onReplySelected = onReplySelected, onDeleteSelected = onDeleteSelected, onEmojiSelected = onEmojiSelected
                )
            }
            item (key = "LAST_ITEM") { Box(Modifier.height(SPACE_AFTER_LAST_MESSAGE)) }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.fillMaxHeight().align(CenterEnd).width(10.dp)
        )

        // Room Switch Detector
        if (prevGroupId != selectedGroupId) {
            if (DEBUG_MESSAGE_SCROLLING) println("[ROOM SWITCH DETECTED] Resetting Parameters. Old: $prevGroupId, New: $selectedGroupId")
            prevGroupId = selectedGroupId
            isInitialLoad = true
            stickToBottom = true
            prevMsgSize = grpmsgs.groupmessages.size
            if (DEBUG_MESSAGE_SCROLLING) println("[ROOM SWITCH DETECTED] Reset state. stickToBottom: true, isInitialLoad: true, prevMsgSize: $prevMsgSize")
        }

        // Side-Effect Automation Pipeline
        val lastSerial = grpmsgs.groupmessages.lastOrNull()?.msgDatabaseId

        LaunchedEffect(lastSerial, selectedGroupId) {
            if (DEBUG_MESSAGE_SCROLLING) {
                println("[LAST SERIAL EFFECT] Triggered. LastSerial: $lastSerial, SelectedGroupId: $selectedGroupId, isInitialLoad: $isInitialLoad, stickToBottom: $stickToBottom, DB size: ${grpmsgs.groupmessages.size}")
            }
            if (lastSerial != null) {
                if (isInitialLoad) {
                    if (grpmsgs.groupmessages.isNotEmpty()) {
                        val targetLayoutIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
                        if (DEBUG_MESSAGE_SCROLLING) println("[EXECUTION: INITIAL SNAP] Snapping to bottom index: $targetLayoutIndex. Total items in DB: ${grpmsgs.groupmessages.size}")

                        suppressScrollDetection = true
                        try {
                            listState.scrollToItem(targetLayoutIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
                        } catch (e: IndexOutOfBoundsException) {
                            if (DEBUG_MESSAGE_SCROLLING) println("[EXECUTION: INITIAL SNAP] IndexOutOfBoundsException. Retrying...")
                            val retryIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
                            if (retryIndex >= 0) {
                                try { listState.scrollToItem(retryIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET) } catch (_: Exception) {}
                            }
                        } finally {
                            suppressScrollDetection = false
                        }
                    }
                    isInitialLoad = false
                } else if (stickToBottom) {
                    val layoutInfo = listState.layoutInfo
                    val viewportEnd = layoutInfo.viewportEndOffset
                    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()

                    if (lastVisible != null) {
                        val itemEnd = lastVisible.offset + lastVisible.size
                        val overflow = (itemEnd - viewportEnd).toFloat()
                        val scrollDistance = (overflow + 150f).coerceAtLeast(0f)

                        if (DEBUG_MESSAGE_SCROLLING) {
                            println("[EXECUTION: ANIMATE SCROLL CALC] ViewportEnd: $viewportEnd, LastVisibleOffset: ${lastVisible.offset}, LastVisibleSize: ${lastVisible.size}, ItemEnd: $itemEnd, Overflow: $overflow, ScrollDistance: $scrollDistance")
                        }

                        if (scrollDistance > 0f) {
                            if (DEBUG_MESSAGE_SCROLLING) println("[EXECUTION: ANIMATE SCROLL] Animating by $scrollDistance px with StiffnessVeryLow")

                            suppressScrollDetection = true
                            try {
                                listState.animateScrollBy(
                                    value = scrollDistance,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessVeryLow)
                                )
                            } catch (e: Exception) {
                                if (DEBUG_MESSAGE_SCROLLING) println("[EXECUTION: ANIMATE SCROLL] Exception during animation: ${e.message}")
                            } finally {
                                suppressScrollDetection = false
                            }
                        } else {
                            if (DEBUG_MESSAGE_SCROLLING) println("[EXECUTION: ANIMATE SCROLL] Scroll distance is 0. No animation needed.")
                        }
                    } else {
                        if (DEBUG_MESSAGE_SCROLLING) println("[EXECUTION: ANIMATE SCROLL] No visible items found.")
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

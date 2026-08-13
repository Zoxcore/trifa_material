@file:Suppress("FunctionName", "LocalVariableName", "SpellCheckingInspection", "PackageDirectoryMismatch")

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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    // --- ROBUST STICK-TO-BOTTOM STATE ---
    var stickToBottom by remember { mutableStateOf(true) }
    var prevScrollIndex by remember { mutableStateOf(0) }
    var prevScrollOffset by remember { mutableStateOf(0) }
    var suppressScrollDetection by remember { mutableStateOf(false) } // NEW: Flag to ignore layout jitter during auto-scroll

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        // If we are currently auto-scrolling or snapping, ignore layout changes to prevent false detach
        if (suppressScrollDetection) {
            prevScrollIndex = listState.firstVisibleItemIndex
            prevScrollOffset = listState.firstVisibleItemScrollOffset
            return@LaunchedEffect
        }

        val currentIndex = listState.firstVisibleItemIndex
        val currentOffset = listState.firstVisibleItemScrollOffset

        val layoutInfo = listState.layoutInfo
        val lastItemIndex = layoutInfo.totalItemsCount - 1
        val isSpacerVisible = layoutInfo.visibleItemsInfo.any { it.index == lastItemIndex }

        if (isSpacerVisible) {
            stickToBottom = true
        } else {
            val indexDecreased = currentIndex < prevScrollIndex
            // Increased threshold from 15f to 50f.
            // Layout jitter (text reflow, image loading) can easily cause 20-30px shifts.
            // A real user scroll will easily exceed 50px.
            val offsetDecreased = (currentIndex == prevScrollIndex) && (currentOffset < prevScrollOffset - 50f)

            if (indexDecreased || offsetDecreased) {
                stickToBottom = false
                if (DEBUG_MESSAGE_SCROLLING) println("[BOTTOM MONITOR] User scrolled UP. Detaching from bottom.")
            }
        }

        prevScrollIndex = currentIndex
        prevScrollOffset = currentOffset
    }

    var prevMessageStoreSize by remember { mutableStateOf(grpmsgs.groupmessages.size) }

    // SAFETY VALVE: Detects asynchronous store recoveries and auto-forces scroll correction
    LaunchedEffect(grpmsgs.groupmessages.size) {
        val currentSize = grpmsgs.groupmessages.size
        val sizeDelta = kotlin.math.abs(currentSize - prevMessageStoreSize)

        if (stickToBottom && grpmsgs.groupmessages.isNotEmpty() && sizeDelta >= SNAP_TO_BOTTOM_NEW_ITEM_COUNT) {
            // SAFE INDEX: Query the actual laid-out items count, not the data model size
            val targetIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)

            if (DEBUG_MESSAGE_SCROLLING) println("[ASYNC ENGINE INTERCEPTOR] Sudden jump detected! Enforcing hard scroll snap safety to index: $targetIndex")

            suppressScrollDetection = true
            try {
                listState.scrollToItem(targetIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
            } catch (e: IndexOutOfBoundsException) {
                // Race condition handled: list shrank while we were trying to scroll
                val retryIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
                if (retryIndex >= 0) {
                    try { listState.scrollToItem(retryIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET) } catch (_: Exception) {}
                }
            }
            suppressScrollDetection = false
        }
        prevMessageStoreSize = currentSize
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(start = 4.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            state = listState
        ) {
            item (key = "FIRST_ITEM") {
                Spacer(Modifier.size(SPACE_BEFORE_FIRST_MESSAGE))
            }
            if (!groupstore.state.fullHistoryActive) {
                item(key = "load_more_button_key") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { groupstore.fullHistoryActive(true) }) {
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
            item (key = "LAST_ITEM") {
                Box(Modifier.height(SPACE_AFTER_LAST_MESSAGE))
            }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.fillMaxHeight().align(CenterEnd).width(10.dp)
        )

        var prevselectedGroupId by remember { mutableStateOf(selectedGroupId) }
        var isInitialLoad by remember { mutableStateOf(true) }

        // 2. Room Switch Detector Logic Loop
        if (prevselectedGroupId != selectedGroupId) {
            if (DEBUG_MESSAGE_SCROLLING) println("[ROOM SWITCH DETECTED] Resetting Parameters")
            prevselectedGroupId = selectedGroupId
            isInitialLoad = true
            stickToBottom = true // Reset stick to bottom on room change
            prevMessageStoreSize = grpmsgs.groupmessages.size
        }

        // 3. Side-Effect Automation Pipeline
        val lastSerial = grpmsgs.groupmessages.lastOrNull()?.msgDatabaseId

        LaunchedEffect(lastSerial, selectedGroupId) {
            if (lastSerial != null) {
                if (isInitialLoad) {
                    if (grpmsgs.groupmessages.isNotEmpty()) {
                        val targetLayoutIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
                        if (DEBUG_MESSAGE_SCROLLING) println("[EXECUTION: INITIAL SNAP] Snapping to bottom index: $targetLayoutIndex")

                        suppressScrollDetection = true
                        try {
                            listState.scrollToItem(targetLayoutIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
                        } catch (e: IndexOutOfBoundsException) {
                            // Safety retry if the list shrank during the snap
                            val retryIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
                            if (retryIndex >= 0) {
                                try { listState.scrollToItem(retryIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET) } catch (_: Exception) {}
                            }
                        } finally {
                            // Wakes up monitor immediately if the user interrupts the initial snap
                            suppressScrollDetection = false
                        }
                    }
                    isInitialLoad = false
                } else {
                    if (stickToBottom) {
                        val layoutInfo = listState.layoutInfo
                        val viewportEnd = layoutInfo.viewportEndOffset
                        val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()

                        if (lastVisible != null) {
                            val itemEnd = lastVisible.offset + lastVisible.size
                            val overflow = (itemEnd - viewportEnd).toFloat()
                            val scrollDistance = (overflow + 150f).coerceAtLeast(0f)

                            if (scrollDistance > 0f) {
                                if (DEBUG_MESSAGE_SCROLLING) println("[EXECUTION: ANIMATE SCROLL] Animating by $scrollDistance px with StiffnessVeryLow")

                                suppressScrollDetection = true
                                try {
                                    listState.animateScrollBy(
                                        value = scrollDistance,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessVeryLow
                                        )
                                    )
                                } catch (e: Exception) {
                                    // Catch layout shift exceptions, but allow CancellationException to propagate
                                } finally {
                                    // CRITICAL: Instantly resets the flag if the user touches the screen
                                    // and cancels the animation mid-flight!
                                    suppressScrollDetection = false
                                }
                            }
                        }
                    } else {
                        if (DEBUG_MESSAGE_SCROLLING) println("[EXECUTION: SKIPPED] User scrolled away from bottom.")
                    }
                }
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

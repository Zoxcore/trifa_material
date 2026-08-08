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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.jetbrains.compose.resources.ExperimentalResourceApi


@Composable
internal fun GroupMessages(ui_scale: Float, selectedGroupId: String?) {
    val listState = rememberLazyListState()
    val grpmsgs by groupmessagestore.stateFlow.collectAsState()

    // Tracks if the very last item in the layout is fully visible
    val isAtBottomEnd = remember { derivedStateOf {
        val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
        val result = if (lastVisibleItem == null) false else {
            val viewportEnd = listState.layoutInfo.viewportEndOffset
            val itemEnd = lastVisibleItem.offset + lastVisibleItem.size
            // Checks if the last item (LAST_ITEM padding) is visible at or past the viewport edge
            lastVisibleItem.index == listState.layoutInfo.totalItemsCount - 1 && itemEnd <= viewportEnd
        }
        // NEW LOGGING FOR isAtBottomEnd DERIVED STATE EVALUATION
        //SCROLL_DEBUG//println("[isAtBottomEnd RE-EVALUATION] Result: $result | Last Visible Item Index: ${lastVisibleItem?.index ?: "NULL"} | Total Layout Count: ${listState.layoutInfo.totalItemsCount}")
        result
    }}

    // Capture bottom state snapshot right before composition updates data
    var wasAtBottomBeforeUpdate by remember { mutableStateOf(false) }
    SideEffect {
        if (wasAtBottomBeforeUpdate != isAtBottomEnd.value) {
            //SCROLL_DEBUG//println("[wasAtBottomBeforeUpdate FLIP] Mutating Context Status From: $wasAtBottomBeforeUpdate To: ${isAtBottomEnd.value}")
        } else {
            //SCROLL_DEBUG//println("[wasAtBottomBeforeUpdate CAPTURE] Stable State Maintained: $wasAtBottomBeforeUpdate")
        }
        wasAtBottomBeforeUpdate = isAtBottomEnd.value
    }

    // Track the previous size to calculate sudden jumps
    var prevMessageStoreSize by remember { mutableStateOf(grpmsgs.groupmessages.size) }


    // SAFETY VALVE: Detects asynchronous store recoveries and auto-forces scroll correction
    LaunchedEffect(grpmsgs.groupmessages.size) {
        val currentSize = grpmsgs.groupmessages.size
        val sizeDelta = kotlin.math.abs(currentSize - prevMessageStoreSize)

        // Only enforce hard snapping if the size jumped drastically (>= 100 items)
        if (wasAtBottomBeforeUpdate && grpmsgs.groupmessages.isNotEmpty() && sizeDelta >= 100) {
            val targetIndex = grpmsgs.groupmessages.size + 1
            //SCROLL_DEBUG//println("[ASYNC ENGINE INTERCEPTOR] Sudden jump detected! Size changed from $prevMessageStoreSize to $currentSize (Delta: $sizeDelta). Enforcing hard scroll snap safety to index: $targetIndex")
            listState.scrollToItem(targetIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
        } else if (wasAtBottomBeforeUpdate && grpmsgs.groupmessages.isNotEmpty()) {
            //SCROLL_DEBUG//println("[ASYNC ENGINE INTERCEPTOR] Normal update. Size changed from $prevMessageStoreSize to $currentSize (Delta: $sizeDelta). Bypassing snap to preserve animation.")
        }

        prevMessageStoreSize = currentSize
    }

    // Logs only when the bottom status changes
    LaunchedEffect(isAtBottomEnd.value) {
        //SCROLL_DEBUG//println("[BOTTOM MONITOR] Is At Bottom End: ${isAtBottomEnd.value}")
    }

    // 1. Continuous Scroll & Visibility Logger
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset, grpmsgs.groupmessages) {
        val visibleIds = listState.layoutInfo.visibleItemsInfo.mapNotNull { item ->
            when (item.key) {
                "FIRST_ITEM", "LAST_ITEM" -> null
                else -> {
                    val dataIndex = item.index - 1
                    grpmsgs.groupmessages.getOrNull(dataIndex)?.msgDatabaseId
                }
            }
        }
        //SCROLL_DEBUG//println("[SCROLL MONITOR] Index: ${listState.firstVisibleItemIndex} | Offset: ${listState.firstVisibleItemScrollOffset} | Total Items In Store: ${grpmsgs.groupmessages.size} | Visible Message IDs: $visibleIds")
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
            items(grpmsgs.groupmessages, key = { it.msgDatabaseId }) {
                GroupChatMessage(isMyMessage = it.user == myUser, it, ui_scale)
            }
            item (key = "LAST_ITEM") {
                Box(Modifier.height(SPACE_AFTER_LAST_MESSAGE))
            }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.fillMaxHeight().align(CenterEnd).width(10.dp)
        )

        var prevLastSerial by remember { mutableStateOf(-1L) }
        val lastSerial = grpmsgs.groupmessages.lastOrNull()?.msgDatabaseId
        var prevselectedGroupId by remember { mutableStateOf(selectedGroupId) }
        var isInitialLoad by remember { mutableStateOf(true) }

        // 2. Room Switch Detector Logic Loop
        if (prevselectedGroupId != selectedGroupId) {
            //SCROLL_DEBUG//println("[ROOM SWITCH DETECTED] Resetting Parameters -> Changing From: '$prevselectedGroupId' To: '$selectedGroupId' | Clearing prevLastSerial (Was: $prevLastSerial)")
            prevselectedGroupId = selectedGroupId
            prevLastSerial = -1L
            isInitialLoad = true
            // FIX: Re-initialize the layout size tracker during room change to prevent false delta math artifacts
            prevMessageStoreSize = grpmsgs.groupmessages.size
        }

        // 3. Side-Effect Automation Pipeline Logger
        LaunchedEffect(lastSerial, selectedGroupId) {
            //SCROLL_DEBUG//println("[LAUNCHED EFFECT TRIGGERED] Keys -> lastSerial: $lastSerial, selectedGroupId: $selectedGroupId | Current Context State -> isInitialLoad: $isInitialLoad, prevLastSerial: $prevLastSerial, wasAtBottomBeforeUpdate: $wasAtBottomBeforeUpdate")

            if (lastSerial != null) {
                // FIX: Point exactly to the LAST_ITEM layout item index (size + 1) to match layout structure
                val targetLayoutIndex = grpmsgs.groupmessages.size + 1

                if (isInitialLoad) {
                    //SCROLL_DEBUG//println("[EVALUATION: INITIAL LOAD] Messages Empty?: ${grpmsgs.groupmessages.isEmpty()} | Target Layout Index: $targetLayoutIndex")
                    if (grpmsgs.groupmessages.isNotEmpty()) {
                        //SCROLL_DEBUG//println("[EXECUTION: SNAP TO BOTTOM] Call -> listState.scrollToItem(index=$targetLayoutIndex, offset=$LAST_MSG_SCROLL_TO_SCROLL_OFFSET)")
                        listState.scrollToItem(targetLayoutIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
                    }
                    isInitialLoad = false
                    //SCROLL_DEBUG//println("[STATE CHANGE] isInitialLoad flag updated to: false")
                } else {
                    val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                    val lastVisibleSerial = when (lastVisibleItem?.key) {
                        "FIRST_ITEM", "LAST_ITEM" -> -1L
                        null -> -1L
                        else -> {
                            val dataIndex = lastVisibleItem.index - 1
                            grpmsgs.groupmessages.getOrNull(dataIndex)?.msgDatabaseId ?: -1L
                        }
                    }

                    //SCROLL_DEBUG//println("[EVALUATION: APPEND LOGIC] lastVisibleItem Key: ${lastVisibleItem?.key ?: "NULL"}, Index: ${lastVisibleItem?.index ?: "NULL"} | lastVisibleSerial: $lastVisibleSerial | Comparison: (lastVisibleSerial [$lastVisibleSerial] >= prevLastSerial [$prevLastSerial] OR lastVisibleSerial == -1L)")

                    val currentSize = grpmsgs.groupmessages.size
                    val sizeDelta = kotlin.math.abs(currentSize - prevMessageStoreSize)

                    // Intercept sudden list adjustments if user was firmly tracking the bottom state before the update
                    if (wasAtBottomBeforeUpdate && sizeDelta >= 100) {
                        //SCROLL_DEBUG//println("[RECOVERY: SNAP TO BOTTOM] Severe fluctuation threshold hit (Delta: $sizeDelta). Forcing scroll anchor recovery -> index: $targetLayoutIndex")
                        listState.scrollToItem(targetLayoutIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
                    } else if ((lastVisibleSerial >= prevLastSerial || lastVisibleSerial == -1L) &&
                        grpmsgs.groupmessages.lastIndex > 0 &&
                        lastVisibleItem != null &&
                        lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5) { // Proximity safety check

                        val layoutInfo = listState.layoutInfo
                        val visibleItems = layoutInfo.visibleItemsInfo
                        val lastVisible = visibleItems.lastOrNull()

                        if (lastVisible != null) {
                            val viewportEnd = layoutInfo.viewportEndOffset
                            val itemEnd = lastVisible.offset + lastVisible.size
                            val extraPaddingPx = 300f
                            val scrollDistance = (itemEnd - viewportEnd).toFloat() + extraPaddingPx

                            //SCROLL_DEBUG//println("[CALCULATION] Viewport End: $viewportEnd, Item End: $itemEnd, Base Delta: ${itemEnd - viewportEnd}, Final Extra Scroll Distance: $scrollDistance px")

                            if (scrollDistance > 0f) {
                                //SCROLL_DEBUG//println("[EXECUTION: ANIMATE SCROLL] Call -> listState.animateScrollBy(value=$scrollDistance px)")
                                listState.animateScrollBy(
                                    value = scrollDistance,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessVeryLow
                                    )
                                )
                            } else {
                                //SCROLL_DEBUG//println("[EXECUTION: SKIPPED] scrollDistance ($scrollDistance px) is not greater than 0f")
                            }
                        } else {
                            //SCROLL_DEBUG//println("[EXECUTION: ABORTED] lastVisible layout item reference is unexpectedly null")
                        }
                    } else {
                        //SCROLL_DEBUG//println("[EXECUTION: SKIPPED] Stick-to-bottom scroll conditions not met. Proximity Index: ${lastVisibleItem?.index ?: -1} vs Total: ${listState.layoutInfo.totalItemsCount}")
                    }
                }

                //SCROLL_DEBUG//println("[STATE CHANGE] Updating prevLastSerial From: $prevLastSerial To: $lastSerial")
                prevLastSerial = lastSerial
            } else {
                //SCROLL_DEBUG//println("[EXECUTION: ABORTED] lastSerial value is completely null (Empty message room stream)")
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

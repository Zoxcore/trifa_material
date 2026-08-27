@file:Suppress("FunctionName", "LocalVariableName", "SpellCheckingInspection", "PackageDirectoryMismatch", "SimplifyBooleanWithConstants")

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.zoffcc.applications.trifa2.rememberChatScrollManager
import kotlinx.coroutines.launch

// ============================================================================
// UI COMPOSABLES
// ============================================================================

@Composable
internal fun GroupMessages(
    ui_scale: Float,
    selectedGroupId: String?,
    onReplySelected: (UIGroupMessage) -> Unit,
    onDeleteSelected: (UIGroupMessage) -> Unit,
    onEmojiSelected: (UIGroupMessage, String) -> Unit
) {
    val listState = rememberLazyListState()
    val grpmsgs by groupmessagestore.stateFlow.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    if (DEBUG_MESSAGE_SCROLLING) {
        println("[STATE] GroupMessages recomposed. DB Messages: ${grpmsgs.groupmessages.size}, SelectedGroupId: $selectedGroupId")
    }

    val lastSerial = grpmsgs.groupmessages.lastOrNull()?.msgDatabaseId
    val messagesSize = grpmsgs.groupmessages.size
    val currentMessagesSize by remember {
        derivedStateOf { grpmsgs.groupmessages.size }
    }

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

    // --- FAB VISIBILITY FIX ---
    // Calculate exactly if the user is visually at the bottom directly from the LazyListState layout.
    // This guarantees the FAB shows 100% of the time when scrolled away from the bottom,
    // avoiding any delays or missed state updates from the complex scroll manager.
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()

            // If the list is empty or no items are visible, assume we are at the bottom
            if (lastVisible == null) return@derivedStateOf true

            // 1. Check if the absolute last item (the bottom spacer) is visible
            val isLastItemVisible = lastVisible.index >= layoutInfo.totalItemsCount - 1

            // 2. Check if the second to last item (the last message) is fully visible
            // We allow a 5px tolerance for viewport offsets
            val isSecondLastItemFullyVisible = lastVisible.index == layoutInfo.totalItemsCount - 2 &&
                    (lastVisible.offset + lastVisible.size <= layoutInfo.viewportEndOffset + 5)

            isLastItemVisible || isSecondLastItemFullyVisible
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 4.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            state = listState
        ) {
            item(key = "FIRST_ITEM") {
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
                        Button(
                            onClick = {
                                if (DEBUG_MESSAGE_SCROLLING) {
                                    println("[USER ACTION] Load Older Messages button clicked.")
                                }

                                groupstore.fullHistoryActive(true)
                            }
                        ) {
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

            item(key = "LAST_ITEM") {
                Box(Modifier.height(SPACE_AFTER_LAST_MESSAGE))
            }
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier
                .fillMaxHeight()
                .align(CenterEnd)
                .width(10.dp)
        )

        JumpToBottomFab(
            visible = !isAtBottom, // Show FAB 100% reliably when the list is scrolled away from the bottom
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 0.dp),
            onClick = {
                coroutineScope.launch {
                    scrollManager.jumpToBottom(currentMessagesSize)
                }
            }
        )
    }
}

@Composable
fun JumpToBottomFab(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        SmallFloatingActionButton(
            shape = CircleShape,
            modifier = Modifier
                // Added padding to increase the layout bounds of the composable.
                // This gives the shadow space to render so it doesn't get
                // chopped into a square by the AnimatedVisibility layer.
                .padding(16.dp)
                .size(40.dp),
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowDownward,
                contentDescription = "Jump to bottom",
                modifier = Modifier.size(20.dp)
            )
        }
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
        modifier = Modifier
            .size(imageSize.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
        painter = painter,
        contentDescription = "Peer picture",
        colorFilter = ColorFilter.tint(user.color, BlendMode.Modulate)
    )
}

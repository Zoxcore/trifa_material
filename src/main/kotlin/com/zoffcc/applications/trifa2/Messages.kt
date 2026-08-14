@file:Suppress("LocalVariableName", "FunctionName", "SpellCheckingInspection", "PackageDirectoryMismatch")

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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zoffcc.applications.trifa.MainActivity.Companion.DEBUG_MESSAGE_SCROLLING
import com.zoffcc.applications.trifa2.rememberChatScrollManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

// ============================================================================
// 1-ON-1 UI COMPOSABLE
// ============================================================================

@Composable
internal fun Messages(ui_scale: Float, selectedContactPubkey: String?,
                      onReplySelected: (UIMessage) -> Unit,
                      onDeleteSelected: (UIMessage) -> Unit,
                      onEmojiSelected: (UIMessage, String) -> Unit ) {
    val listState = rememberLazyListState()
    val msgs by messagestore.stateFlow.collectAsState()

    val lastSerial = msgs.messages.lastOrNull()?.msgDatabaseId
    val messagesSize = msgs.messages.size

    // All complex scrolling logic is neatly handled by the manager here
    val scrollManager = rememberChatScrollManager(
        listState = listState,
        messagesSize = messagesSize,
        lastSerial = lastSerial,
        selectedRoomId = selectedContactPubkey,
        expectedItemCount = { dbCount ->
            val loadMoreCount = if (!contactstore.state.fullHistoryActive) 1 else 0
            dbCount + loadMoreCount + 2
        },
        snapToBottomNewItemCount = SNAP_TO_BOTTOM_NEW_ITEM_COUNT,
        scrollToBottomOffset = LAST_MSG_SCROLL_TO_SCROLL_OFFSET
    )

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(start = 4.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            state = listState,
        ) {
            item(key = "FIRST_ITEM") {
                Spacer(Modifier.size(SPACE_BEFORE_FIRST_MESSAGE))
            }
            if (!contactstore.state.fullHistoryActive)
            {
                item(key = "load_more_button_key") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { contactstore.fullHistoryActive(true) }) {
                            Text("Load Older Messages")
                        }
                    }
                }
            }
            items(msgs.messages, key = { it.msgDatabaseId }) {
                ChatMessage(
                    isMyMessage = (it.user == myUser),
                    message = it,
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
            modifier = Modifier.fillMaxHeight().align(CenterEnd).width(10.dp)
        )
    }
}

fun calc_avatar_size(avatar_size: Float): Float
{
    if (avatar_size > MAX_AVATAR_SIZE)
    {
        return MAX_AVATAR_SIZE
    }
    return avatar_size
}

@Composable
fun UserPic(user: User, ui_scale: Float) {
    val imageSize = (calc_avatar_size(AVATAR_SIZE * ui_scale))
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
        contentDescription = "User picture"
    )
}

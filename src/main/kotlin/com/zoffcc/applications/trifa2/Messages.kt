@file:Suppress("LocalVariableName", "FunctionName", "SpellCheckingInspection", "PackageDirectoryMismatch")

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Composable
internal fun Messages(ui_scale: Float, selectedContactPubkey: String?) {
    val listState = rememberLazyListState()
    val msgs by messagestore.stateFlow.collectAsState()
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(start = 4.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            state = listState,
        ) {
            item (key = "FIRST_ITEM") {
                Spacer(Modifier.size(SPACE_BEFORE_FIRST_MESSAGE))
            }
            // Log.i(com.zoffcc.applications.trifa.TAG, "LazyColumn --> draw")
            items(msgs.messages, key = { it.msgDatabaseId }) {
                // Log.i(com.zoffcc.applications.trifa.TAG, "LazyColumn -> it.msgDatabaseId = " + it.msgDatabaseId)
                ChatMessage(isMyMessage = (it.user == myUser), it, ui_scale,
                    // modifier = Modifier.animateItemPlacement()
                )
            }
            item (key = "LAST_ITEM") {
                Box(Modifier.height(SPACE_AFTER_LAST_MESSAGE))
            }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.fillMaxHeight().align(CenterEnd).width(10.dp) // .background(Color.Red)
        )
        // This probably shouldn't cause a recomposition
        var prevLastSerial by remember { mutableStateOf(-1L) }
        var lastSerial = msgs.messages.lastOrNull()?.msgDatabaseId
        var prevselectedContactPubkey by remember { mutableStateOf(selectedContactPubkey) }
        if (prevselectedContactPubkey != selectedContactPubkey)
        {
            lastSerial = -1
            prevselectedContactPubkey = selectedContactPubkey
            prevLastSerial = -1L
        }
        LaunchedEffect(lastSerial, selectedContactPubkey) {
            if (lastSerial != null) {
                // If we're at the spot we last scrolled to
                val lastVisibleSerial = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                    ?.let { msgs.messages.getOrNull(it)?.msgDatabaseId }
                    ?: -1L
                if ((lastVisibleSerial >= prevLastSerial || lastVisibleSerial == -1L) && msgs.messages.lastIndex > 0) {
                    // scroll to the end if we were at the end
                    listState.scrollToItem(msgs.messages.lastIndex, LAST_MSG_SCROLL_TO_SCROLL_OFFSET)
                    // Log.i(com.zoffcc.applications.trifa.TAG, "messages -> scroll to the end")
                }
                // remember the last serial
                prevLastSerial = lastSerial
            }
        }
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

@OptIn(ExperimentalResourceApi::class)
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

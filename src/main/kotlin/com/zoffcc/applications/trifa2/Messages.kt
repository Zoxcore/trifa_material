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
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun Messages(messages: List<UIMessage>) {
    val listState = rememberLazyListState()
    if (messages.isNotEmpty()) {
        LaunchedEffect(messages.last()) {
            try
            {
                // listState.animateScrollToItem(messages.lastIndex, scrollOffset = 0)
                listState.scrollToItem(messages.lastIndex, scrollOffset = 2)
            }
            catch (e : Exception)
            {
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(start = 4.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
        ) {
            item { Spacer(Modifier.size(20.dp)) }
            items(messages, key = { it.id }) {
                ChatMessage(isMyMessage = (it.user == myUser), it)
            }
            item {
                Box(Modifier.height(70.dp))
            }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.fillMaxHeight().align(CenterEnd).width(10.dp) // .background(Color.Red)
        )
    }
}


@OptIn(ExperimentalResourceApi::class)
@Composable
fun UserPic(user: User) {
    val imageSize = 48f
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

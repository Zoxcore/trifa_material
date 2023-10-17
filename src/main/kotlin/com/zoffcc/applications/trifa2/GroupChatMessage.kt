import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.HelperFiletransfer
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.HelperGeneric.AsyncImage
import com.zoffcc.applications.trifa.HelperGeneric.loadImageBitmap
import com.zoffcc.applications.trifa.TRIFAGlobals
import java.io.File

@Composable
fun GroupTriangle(risingToTheRight: Boolean, background: Color) {
    Box(
        Modifier
            .padding(bottom = 10.dp, start = 0.dp)
            .clip(GroupTriangleEdgeShape(risingToTheRight))
            .background(background)
            .size(6.dp)
    )
}

@Composable
inline fun GroupChatMessage(isMyMessage: Boolean, groupmessage: UIGroupMessage) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {

        Row(verticalAlignment = Alignment.Bottom) {
            if (!isMyMessage) {
                Column {
                    PeerPic(groupmessage.user)
                }
                Spacer(Modifier.size(2.dp))
                Column {
                    GroupTriangle(true, ChatColorsConfig.OTHERS_MESSAGE)
                }
            }

            Column {
                Box(
                    Modifier.clip(
                        RoundedCornerShape(
                            10.dp,
                            10.dp,
                            if (!isMyMessage) 10.dp else 0.dp,
                            if (!isMyMessage) 0.dp else 10.dp
                        )
                    )
                        .background(color = if (!isMyMessage) ChatColorsConfig.OTHERS_MESSAGE else ChatColorsConfig.MY_MESSAGE)
                        .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp),
                ) {
                    Column {
                        if(!isMyMessage) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = groupmessage.user.name,
                                    style = MaterialTheme.typography.body1.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.sp,
                                        fontSize = 14.sp
                                    ),
                                    color = groupmessage.user.color
                                )
                            }
                        }
                        Spacer(Modifier.size(3.dp))
                        SelectionContainer()
                        {
                            Text(
                                text = groupmessage.text,
                                style = MaterialTheme.typography.body1.copy(
                                    fontSize = 18.sp,
                                    letterSpacing = 0.sp
                                )
                            )
                        }
                        if (groupmessage.trifaMsgType == TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value)
                        {
                            if (groupmessage.filename_fullpath != null)
                            {
                                if (HelperFiletransfer.check_filename_is_image(groupmessage.filename_fullpath))
                                {
                                    AsyncImage(load = {
                                        loadImageBitmap(File(groupmessage.filename_fullpath))
                                    }, painterFor = { remember { BitmapPainter(it) } },
                                        contentDescription = "Image",
                                        modifier = Modifier.size(IMAGE_PREVIEW_SIZE))
                                }
                                else
                                {
                                    Icon(
                                        modifier = Modifier.size(IMAGE_PREVIEW_SIZE),
                                        imageVector = Icons.Default.Attachment,
                                        contentDescription = "File",
                                        tint = MaterialTheme.colors.primary
                                    )
                                }
                            }
                            else
                            {
                                Icon(
                                    modifier = Modifier.size(IMAGE_PREVIEW_SIZE),
                                    imageVector = Icons.Default.BrokenImage,
                                    contentDescription = "failed",
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                        }
                        Spacer(Modifier.size(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = timeToString(groupmessage.timeMs),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.subtitle1.copy(fontSize = 10.sp),
                                color = ChatColorsConfig.TIME_TEXT
                            )
                        }
                    }
                }
                Box(Modifier.size(10.dp))
            }
            if(isMyMessage) {
                Column {
                    Triangle(false, ChatColorsConfig.MY_MESSAGE)
                }
            }
        }
    }
}



// Adapted from https://stackoverflow.com/questions/65965852/jetpack-compose-create-chat-bubble-with-arrow-and-border-elevation
class GroupTriangleEdgeShape(val risingToTheRight: Boolean) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val trianglePath = if(risingToTheRight) {
            Path().apply {
                moveTo(x = 0f, y = size.height)
                lineTo(x = size.width, y = 0f)
                lineTo(x = size.width, y = size.height)
            }
        } else {
            Path().apply {
                moveTo(x = 0f, y = 0f)
                lineTo(x = size.width, y = size.height)
                lineTo(x = 0f, y = size.height)
            }
        }

        return Outline.Generic(path = trianglePath)
    }
}
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.zoffcc.applications.trifa.HelperFiletransfer.check_filename_is_image
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.HelperOSFile
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE
import java.io.File
import kotlin.random.Random

@Composable
fun Triangle(risingToTheRight: Boolean, background: Color) {
    Box(
        Modifier
            .padding(bottom = 10.dp, start = 0.dp)
            .clip(TriangleEdgeShape(risingToTheRight))
            .background(background)
            .size(6.dp)
    )
}

fun randomColor() = Color(
    Random.nextInt(256),
    Random.nextInt(256),
    Random.nextInt(256),
    alpha = 255
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
inline fun ChatMessage(isMyMessage: Boolean, message: UIMessage) {
    val TAG = "trifa.ChatMessage"
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart,
    ) {

        Row(verticalAlignment = Alignment.Bottom) {
            if (!isMyMessage) {
                Column {
                    UserPic(message.user)
                }
                Spacer(Modifier.size(2.dp))
                Column {
                    Triangle(true, ChatColorsConfig.OTHERS_MESSAGE)
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
                                    text = message.user.name,
                                    style = MaterialTheme.typography.body1.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.sp,
                                        fontSize = 14.sp
                                    ),
                                    color = message.user.color
                                )
                            }
                        }
                        Spacer(Modifier.size(3.dp))
                        SelectionContainer()
                        {
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.body1.copy(
                                    fontSize = 18.sp,
                                    letterSpacing = 0.sp
                                )
                            )
                        }
                        if (message.trifaMsgType == TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value)
                        {
                            if ((message.filesize > 0.0f) && (message.currentfilepos < message.filesize))
                            {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    progress = (message.currentfilepos.toFloat() / message.filesize.toFloat())
                                )
                            }
                            else
                            {
                                if (message.filename_fullpath != null)
                                {
                                    if (check_filename_is_image(message.filename_fullpath))
                                    {
                                        HelperGeneric.AsyncImage(load = {
                                            HelperGeneric.loadImageBitmap(File(message.filename_fullpath))
                                        }, painterFor = { remember { BitmapPainter(it) } },
                                            contentDescription = "Image",
                                            modifier = Modifier.size(IMAGE_PREVIEW_SIZE).
                                            combinedClickable(
                                                onClick = { HelperOSFile.show_containing_dir_in_explorer(message.filename_fullpath) },
                                                onLongClick = {}))
                                    }
                                    else
                                    {
                                        Icon(
                                            modifier = Modifier.size(IMAGE_PREVIEW_SIZE).
                                            combinedClickable(
                                                onClick = { HelperOSFile.show_containing_dir_in_explorer(message.filename_fullpath) },
                                                onLongClick = {}),
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
                        }
                        Spacer(Modifier.size(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = timeToString(message.timeMs),
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
class TriangleEdgeShape(val risingToTheRight: Boolean) : Shape {
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanniktech.emoji.emojiInformation
import com.zoffcc.applications.trifa.HelperFiletransfer
import com.zoffcc.applications.trifa.HelperGeneric.AsyncImage
import com.zoffcc.applications.trifa.HelperGeneric.loadImageBitmap
import com.zoffcc.applications.trifa.HelperOSFile.show_containing_dir_in_explorer
import com.zoffcc.applications.trifa.HelperOSFile.show_file_in_explorer_or_open
import com.zoffcc.applications.trifa.TRIFAGlobals
import org.briarproject.briar.desktop.ui.Tooltip
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
inline fun GroupChatMessage(isMyMessage: Boolean, groupmessage: UIGroupMessage, ui_scale: Float) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {

        Row(verticalAlignment = Alignment.Bottom) {
            if (!isMyMessage) {
                Column {
                    PeerPic(groupmessage.user, ui_scale)
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
                    Column(Modifier.randomDebugBorder().padding(all = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        if(!isMyMessage) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                // println("NNN:" + groupmessage.user.name + "CCC:" +groupmessage.user.color.luminance())
                                Text(
                                    text = groupmessage.user.name,
                                    style = MaterialTheme.typography.body1.copy(
                                        shadow = if (groupmessage.user.color.luminance() > NGC_PEER_LUMINANCE_THRESHOLD_FOR_SHADOW) Shadow(Color.Black, offset = Offset.Zero, blurRadius = 2.4f) else Shadow(),
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = TextUnit.Unspecified,
                                        letterSpacing = 0.sp,
                                        fontSize = 14.sp
                                    ),
                                    color = groupmessage.user.color
                                )
                            }
                        }
                        SelectionContainer(modifier = Modifier.padding(all = 0.dp))
                        {
                            var msg_fontsize = MSG_TEXT_FONT_SIZE_MIXED
                            try
                            {
                                val emojiInformation = groupmessage.text.emojiInformation()
                                if (emojiInformation.isOnlyEmojis)
                                {
                                    msg_fontsize = MSG_TEXT_FONT_SIZE_EMOJI_ONLY
                                }
                            }
                            catch(_: Exception)
                            {
                            }
                            Text(
                                text = groupmessage.text,
                                modifier = Modifier.randomDebugBorder(),
                                style = MaterialTheme.typography.body1.copy(
                                    fontSize = ((msg_fontsize * ui_scale).toDouble()).sp,
                                    lineHeight = TextUnit.Unspecified,
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
                                        modifier = Modifier.size(IMAGE_PREVIEW_SIZE.dp * ui_scale).
                                        combinedClickable(
                                            onClick = { show_file_in_explorer_or_open(groupmessage.filename_fullpath) },
                                            onLongClick = { show_containing_dir_in_explorer(groupmessage.filename_fullpath) }))
                                }
                                else
                                {
                                    Icon(
                                        modifier = Modifier.size(IMAGE_PREVIEW_SIZE.dp * ui_scale).
                                        combinedClickable(
                                            onClick = { show_file_in_explorer_or_open(groupmessage.filename_fullpath) },
                                            onLongClick = { show_containing_dir_in_explorer(groupmessage.filename_fullpath) }),
                                        imageVector = Icons.Default.Attachment,
                                        contentDescription = "File",
                                        tint = MaterialTheme.colors.primary
                                    )
                                }
                            }
                            else
                            {
                                Icon(
                                    modifier = Modifier.size(IMAGE_PREVIEW_SIZE.dp * ui_scale),
                                    imageVector = Icons.Default.BrokenImage,
                                    contentDescription = "failed",
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.randomDebugBorder().padding(all = 0.dp).align(Alignment.End)
                        ) {
                            if (groupmessage.was_synced)
                            {
                                IconButton(
                                    modifier = Modifier.size(15.dp),
                                    icon = Icons.Filled.QuestionMark,
                                    iconTint = Color.Magenta,
                                    enabled = false,
                                    iconSize = 13.dp,
                                    contentDescription = "Message synced via History sync by other Peers" + "\n" +
                                            "Message contents cat not be fully verified",
                                    onClick = {}
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Tooltip("Message sent at: " + timeToString(groupmessage.timeMs)) {
                                Text(
                                    modifier = Modifier.padding(all = 0.dp),
                                    text = timeToString(groupmessage.timeMs),
                                    textAlign = TextAlign.End,
                                    style = MaterialTheme.typography.subtitle1.copy(fontSize = 10.sp, lineHeight = TextUnit.Unspecified),
                                    color = ChatColorsConfig.TIME_TEXT
                                )
                            }
                        }
                    }
                }
                Box(Modifier.size(10.dp))
            }
            if (isMyMessage) {
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
import ChatColorsConfig.NGC_FOUNDER_MESSAGE_COLOR
import ChatColorsConfig.NGC_MODERATOR_MESSAGE_COLOR
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanniktech.emoji.emojiInformation
import com.zoffcc.applications.trifa.HelperFiletransfer
import com.zoffcc.applications.trifa.HelperFiletransfer.byteCountToDisplaySize
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.HelperOSFile.open_webpage
import com.zoffcc.applications.trifa.HelperOSFile.show_containing_dir_in_explorer
import com.zoffcc.applications.trifa.HelperOSFile.show_file_in_explorer_or_open
import com.zoffcc.applications.trifa.TRIFAGlobals
import com.zoffcc.applications.trifa.ToxVars
import com.zoffcc.applications.trifa2.timeToString
import kotlinx.coroutines.DelicateCoroutinesApi
import org.briarproject.briar.desktop.ui.Tooltip
import java.io.File

@Composable
fun GroupTriangle(risingToTheRight: Boolean, peer_role: Int, padding_bottom: Dp = 10.dp) {
    var border_size = 5.dp
    var border_color = Color(NGC_FOUNDER_MESSAGE_COLOR)
    if (peer_role == ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_MODERATOR.value)
    {
        border_color = Color(NGC_MODERATOR_MESSAGE_COLOR)
        border_size = 3.dp
    }
    else if (peer_role == ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_FOUNDER.value)
    {
    }
    else
    {
        return
    }
    Box(
        Modifier
            .padding(bottom = padding_bottom, start = 0.dp)
            .clip(GroupTriangleEdgeShape(risingToTheRight))
            .background(border_color)
            .size(border_size)
    )
}

@OptIn(ExperimentalFoundationApi::class, DelicateCoroutinesApi::class)
@Composable
inline fun GroupChatMessage(isMyMessage: Boolean, groupmessage: UIGroupMessage, ui_scale: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (!isMyMessage) {
                Column {
                    PeerPic(groupmessage.user, ui_scale)
                }
                Spacer(Modifier.size(2.dp))
                Column {
                    GroupTriangle(true, groupmessage.peer_role, MESSAGE_BOX_BOTTOM_PADDING)
                }
            }
            Column {
                var image_save_ui_space = false
                if (groupmessage.trifaMsgType == TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value)
                {
                    if (groupmessage.filename_fullpath != null)
                    {
                        if (HelperFiletransfer.check_filename_is_image(groupmessage.filename_fullpath))
                        {
                            image_save_ui_space = true
                        }
                    }
                }

                val col_msg_other = if (groupmessage.is_private_msg == 0)
                    ChatColorsConfig.OTHERS_MESSAGE else ChatColorsConfig.OTHERS_PRIVATE_MESSAGE
                var start_padding = 10.dp
                var start_top = 5.dp
                var start_end = 10.dp
                var start_bottom = 5.dp
                Box(
                    FounderBorder(groupmessage).clip(
                        RoundedCornerShape(
                            10.dp,
                            10.dp,
                            if (!isMyMessage) 10.dp else 0.dp,
                            if (!isMyMessage) 0.dp else 10.dp
                        )
                    )
                        .background(color = if (!isMyMessage) col_msg_other else ChatColorsConfig.MY_MESSAGE)
                        .padding(start = start_padding, top = start_top, end = start_end, bottom = start_bottom),
                ) {
                    // -------- GroupMessage Content Box --------
                    // -------- GroupMessage Content Box --------
                    // -------- GroupMessage Content Box --------
                    Column(Modifier.randomDebugBorder().padding(all = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        if(!isMyMessage) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                if (groupmessage.is_private_msg == 1)
                                {
                                    Column() {
                                        Tooltip(text = "private Message") {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .border(1.dp, Color.Black, CircleShape)
                                                    .background(Color(NGC_PRIVATE_MSG_INDICATOR_COLOR),
                                                        CircleShape)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                    }
                                    Spacer(modifier = Modifier.width(5.dp))
                                }
                                // println("NNN:" + groupmessage.user.name + "CCC:" +groupmessage.user.color.luminance())
                                Text(
                                    text = groupmessage.user.name,
                                    modifier = if (groupmessage.user.color.luminance() > NGC_PEER_LUMINANCE_THRESHOLD_FOR_SHADOW)
                                        Modifier.background(Color(NGC_PEER_SHADOW_COLOR), RoundedCornerShape(30)).padding(3.dp)
                                    else
                                        Modifier,
                                    fontFamily = DefaultFont,
                                    style = MaterialTheme.typography.body1.copy(
                                        //shadow = if (groupmessage.user.color.luminance() > NGC_PEER_LUMINANCE_THRESHOLD_FOR_SHADOW)
                                        //    Shadow(Color.Black, offset = Offset.Zero, blurRadius = 2.1f)
                                        //else
                                        //    Shadow() ,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = TextUnit.Unspecified,
                                        letterSpacing = 0.sp,
                                        fontSize = ((MSG_TEXT_FONT_SIZE_MIXED * ui_scale / 1.285f).toDouble()).sp
                                    ),
                                    color = groupmessage.user.color
                                )
                            }
                        }
                        var show_link_click by remember { mutableStateOf(false) }
                        var link_str by remember { mutableStateOf("") }
                        if (!image_save_ui_space)
                        {
                            group_message_text_block(groupmessage, ui_scale) { show_link_click_, link_str_ ->
                                show_link_click = show_link_click_
                                link_str = link_str_
                            }
                            group_show_open_link_dialog(show_link_click, link_str) { show_link_click_, link_str_ ->
                                show_link_click = show_link_click_
                                link_str = link_str_
                            }
                        }

                        // ---------------- Filetransfer ----------------
                        // ---------------- Filetransfer ----------------
                        if (groupmessage.trifaMsgType == TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value)
                        {
                            if (!isMyMessage)
                            {
                                group_incoming_filetransfer(groupmessage, ui_scale)
                            }
                            else
                            {
                                group_outgoing_filetransfer(groupmessage, ui_scale)
                            }
                        }
                        // ---------------- Filetransfer ----------------
                        // ---------------- Filetransfer ----------------

                        Row(
                            horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start,
                            modifier = Modifier.randomDebugBorder().padding(all = 0.dp)
                                .align(if (isMyMessage) Alignment.End else Alignment.Start)
                        ) {
                            if (isMyMessage) {
                                message_checkmarks(groupmessage, isMyMessage)
                                message_timestamp_and_info(groupmessage)
                            } else {
                                message_timestamp_and_info(groupmessage)
                                message_checkmarks(groupmessage, isMyMessage)
                            }
                        }
                        // -------- GroupMessage Content Box --------
                        // -------- GroupMessage Content Box --------
                        // -------- GroupMessage Content Box --------
                    }
                }
                Box(Modifier.size(MESSAGE_BOX_BOTTOM_PADDING))
            }
            if (isMyMessage) {
                Column {
                    Triangle(false, ChatColorsConfig.MY_MESSAGE, MESSAGE_BOX_BOTTOM_PADDING)
                }
            }
        }
    }
}

fun FounderBorder(groupmessage: UIGroupMessage): Modifier =
    if (groupmessage.peer_role == ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_FOUNDER.value)
    {
        Modifier.border(width = 3.dp,
            color = Color(NGC_FOUNDER_MESSAGE_COLOR),
            shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 0.dp))
    }
    else if (groupmessage.peer_role == ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_MODERATOR.value)
    {
        Modifier.border(width = 2.dp,
            color = Color(NGC_MODERATOR_MESSAGE_COLOR),
            shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 0.dp))
    }
    else
    {
        Modifier
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun group_outgoing_filetransfer(groupmessage: UIGroupMessage, ui_scale: Float)
{
    if (groupmessage.filename_fullpath != null)
    {
        if (HelperFiletransfer.check_filename_is_image(groupmessage.filename_fullpath))
        {
            var file_name_without_path = ""
            try
            {
                file_name_without_path = File(groupmessage.filename_fullpath).name
            }
            catch(_: Exception)
            {
            }
            var file_size_in_bytes = "???"
            var file_size_human = file_size_in_bytes
            try
            {
                file_size_human = byteCountToDisplaySize(File(groupmessage.filename_fullpath).length())
                file_size_in_bytes = File(groupmessage.filename_fullpath).length().toString()
            }
            catch(_: Exception)
            {
            }
            Tooltip(text = "Filename: " + file_name_without_path + "\n"
                    + "Filesize: " + file_size_human + "\n"
                    + "Filesize: " + file_size_in_bytes + " Bytes",
                textcolor = Color.Black) {
                group_show_filetransfer_image(ui_scale = ui_scale, clickable = true,
                    fullpath = groupmessage.filename_fullpath, description = "Image")
            }
        }
        else
        {
            group_show_filetransfer_image(ui_scale = ui_scale, clickable = true,
                icon = Icons.Default.Attachment,
                tint = MaterialTheme.colors.primary,
                fullpath = groupmessage.filename_fullpath, description = "File")
        }
    }
    else
    {
        group_show_filetransfer_image(ui_scale = ui_scale, clickable = true,
            icon = Icons.Default.BrokenImage,
            tint = MaterialTheme.colors.primary,
            description = "failed")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun group_show_filetransfer_image(ui_scale: Float,
                                  clickable: Boolean = false,
                                  icon: ImageVector? = null,
                                  tint: Color = MaterialTheme.colors.primary,
                                  fullpath: String? = null,
                                  description: String = "failed")
{
    if (fullpath == null) {
        Icon(
            modifier = Modifier.size(IMAGE_PREVIEW_SIZE.dp * ui_scale),
            imageVector = Icons.Default.BrokenImage,
            contentDescription = description,
            tint = tint
        )
    } else if (icon != null) {
        Icon(
            modifier = Modifier.size(IMAGE_PREVIEW_SIZE.dp * ui_scale).combinedClickable(
                onClick = { show_file_in_explorer_or_open(fullpath) },
                onLongClick = { show_containing_dir_in_explorer(fullpath) }),
            imageVector = icon,
            contentDescription = description,
            tint = tint
        )
    } else
    {
        HelperGeneric.AsyncImage(load = {
            HelperGeneric.loadImageBitmap(File(fullpath))
        }, painterFor = { remember { BitmapPainter(it) } },
            contentDescription = description,
            modifier = Modifier.size(IMAGE_PREVIEW_SIZE.dp * ui_scale).combinedClickable(
                onClick = {
                    if (clickable)
                    {
                        show_file_in_explorer_or_open(fullpath)
                    }
                },
                onLongClick = {
                    if (clickable)
                    {
                        show_containing_dir_in_explorer(fullpath)
                    }
                }))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun group_incoming_filetransfer(groupmessage: UIGroupMessage, ui_scale: Float)
{
    if (groupmessage.filename_fullpath != null)
    {
        if (HelperFiletransfer.check_filename_is_image(groupmessage.filename_fullpath))
        {
            var file_name_without_path = ""
            try
            {
                file_name_without_path = File(groupmessage.filename_fullpath).name
            }
            catch(_: Exception)
            {
            }
            var file_size_in_bytes = "???"
            var file_size_human = file_size_in_bytes
            try
            {
                file_size_human = byteCountToDisplaySize(File(groupmessage.filename_fullpath).length())
                file_size_in_bytes = File(groupmessage.filename_fullpath).length().toString()
            }
            catch(_: Exception)
            {
            }
            Tooltip(text = "Filename: " + file_name_without_path + "\n"
                    + "Filesize: " + file_size_human + "\n"
                    + "Filesize: " + file_size_in_bytes + " Bytes",
                textcolor = Color.Black) {
                group_show_filetransfer_image(ui_scale = ui_scale, clickable = true,
                    fullpath = groupmessage.filename_fullpath, description = "Image")
            }
        }
        else
        {
            group_show_filetransfer_image(ui_scale = ui_scale, clickable = true,
                icon = Icons.Default.Attachment,
                tint = MaterialTheme.colors.primary,
                fullpath = groupmessage.filename_fullpath, description = "File")
        }
    }
    else
    {
        group_show_filetransfer_image(ui_scale = ui_scale, clickable = true,
            icon = Icons.Default.BrokenImage,
            tint = MaterialTheme.colors.primary,
            description = "failed")
    }
}

@Composable
fun group_show_open_link_dialog(show_link_click: Boolean, link_str: String, setLinkVars: (Boolean, String) -> Unit)
{
    var show_link_click1 = show_link_click
    var link_str1 = link_str
    if (show_link_click1)
    {
        AlertDialog(onDismissRequest = { link_str1 = ""; show_link_click1 = false; setLinkVars(show_link_click1, link_str1) },
            title = { Text("Open this URL ?") },
            confirmButton = {
                Button(onClick = { open_webpage(link_str1); link_str1 = ""; show_link_click1 = false; setLinkVars(show_link_click1, link_str1) }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { link_str1 = ""; show_link_click1 = false;setLinkVars(show_link_click1, link_str1) }) {
                    Text("No")
                }
            },
            text = { Text("This could be potentially dangerous!" + "\n\n" + link_str1) })
    }
}

@Composable
fun group_message_text_block(groupmessage: UIGroupMessage, ui_scale: Float, setLinkVars: (Boolean, String) -> Unit)
{
    var show_link_click1 = false
    var link_str1 = ""
    var text_is_only_emoji = false
    SelectionContainer(modifier = Modifier.padding(all = 0.dp))
    {
        var msg_fontsize = MSG_TEXT_FONT_SIZE_MIXED
        try
        {
            val emojiInformation = groupmessage.text.emojiInformation()
            if (emojiInformation.isOnlyEmojis)
            {
                msg_fontsize = MSG_TEXT_FONT_SIZE_EMOJI_ONLY
                text_is_only_emoji = true
            }
        }
        catch(_: Exception)
        {
        }
        var text_str = groupmessage.text
        UrlHighlightTextView(
            text = text_str,
            modifier = Modifier.randomDebugBorder(),
            style = MaterialTheme.typography.body1.copy(
                fontSize = ((msg_fontsize * ui_scale).toDouble()).sp,
                fontFamily = if (text_is_only_emoji) NotoEmojiFont else DefaultFont,
                lineHeight = TextUnit.Unspecified,
                letterSpacing = 0.sp
            )
        ) {
            show_link_click1 = true
            link_str1 = it
            setLinkVars(show_link_click1, link_str1)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun message_timestamp_and_info(groupmessage: UIGroupMessage)
{
    var message_size_in_bytes = 0
    try
    {
        message_size_in_bytes = groupmessage.text.toByteArray().size
    } catch (_: Exception)
    {
    }
    var file_info_lines = ""
    if (groupmessage.trifaMsgType == TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value)
    {
        if (groupmessage.filename_fullpath.isNullOrEmpty())
        {
            file_info_lines = "File fullpath: " + "???" + "\n"
        }
        else
        {
            file_info_lines = "File fullpath: " + groupmessage.filename_fullpath + "\n"
        }
        try
        {
            file_info_lines = file_info_lines + "File size in bytes: " + File(groupmessage.filename_fullpath!!).length() + "\n"
        }
        catch(_: Exception)
        {
            file_info_lines = file_info_lines + "File size: " + "???" + "\n"
        }
    }
    val is_prv_msg = if (groupmessage.is_private_msg == 1) "yes" else "no"
    Tooltip("Message sent at: " + timeToString(groupmessage.timeMs) + "\n" +
            "Message ID: " + groupmessage.message_id_tox + "\n" +
            "is private Message: " + is_prv_msg + "\n" +
            "Sender Peer Pubkey: " + groupmessage.toxpk + "\n" +
            "Message size in bytes: " + (if (message_size_in_bytes == 0) "unknown" else message_size_in_bytes) + "\n" +
            file_info_lines +
            "was synced: " + groupmessage.was_synced.toString()) {
        Text(
            modifier = Modifier.padding(all = 0.dp),
            text = timeToString(groupmessage.timeMs),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.subtitle1.copy(fontSize = 10.sp, lineHeight = TextUnit.Unspecified),
            color = ChatColorsConfig.TIME_TEXT
        )
    }
}

@Composable
fun message_checkmarks(groupmessage: UIGroupMessage, isMyMessage: Boolean)
{
    if (groupmessage.was_synced)
    {
        if (!isMyMessage) {
            Spacer(modifier = Modifier.width(10.dp))
        }
        IconButton(
            modifier = Modifier.size(15.dp),
            icon = Icons.Filled.Info,
            iconTint = Color.Magenta,
            enabled = false,
            iconSize = 13.dp,
            contentDescription = "Message synced via History sync by other Peers" + "\n" +
                    "Message contents can not be fully verified",
            onClick = {}
        )
        if (isMyMessage) {
            Spacer(modifier = Modifier.width(10.dp))
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
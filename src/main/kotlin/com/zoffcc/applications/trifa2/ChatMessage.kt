@file:Suppress("unused", "LocalVariableName", "PropertyName", "ClassName", "FunctionName", "SpellCheckingInspection")

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanniktech.emoji.emojiInformation
import com.zoffcc.applications.trifa.HelperFiletransfer.byteCountToDisplaySize
import com.zoffcc.applications.trifa.HelperFiletransfer.check_filename_is_image
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.HelperGeneric.cancel_ft_from_ui
import com.zoffcc.applications.trifa.HelperMessage.set_message_queueing_from_id
import com.zoffcc.applications.trifa.HelperOSFile.open_webpage
import com.zoffcc.applications.trifa.HelperOSFile.show_containing_dir_in_explorer
import com.zoffcc.applications.trifa.HelperOSFile.show_file_in_explorer_or_open
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.TRIFAGlobals
import com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE
import com.zoffcc.applications.trifa.ToxVars
import com.zoffcc.applications.trifa2.timeToString
import org.briarproject.briar.desktop.ui.Tooltip
import org.nibor.autolink.LinkExtractor
import org.nibor.autolink.LinkType
import java.io.File
import kotlin.random.Random


@Composable
fun Triangle(risingToTheRight: Boolean, peer_role: Int, background: Color, padding_bottom: Dp = 10.dp) {
    var border_size = 5.dp
    if (peer_role == ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_MODERATOR.value)
    {
        border_size = 3.dp
    }
    Box(
        Modifier
            .padding(bottom = padding_bottom, start = 0.dp)
            .clip(TriangleEdgeShape(risingToTheRight))
            .background(background)
            .size(border_size)
    )
}

fun randomColor() = Color(
    Random.nextInt(256),
    Random.nextInt(256),
    Random.nextInt(256),
    alpha = 255
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
inline fun ChatMessage(isMyMessage: Boolean,
                       message: UIMessage,
                       ui_scale: Float,
                       modifier: Modifier = Modifier,
                       crossinline onReplySelected: (UIMessage) -> Unit,
                       crossinline onDeleteSelected: (UIMessage) -> Unit,
                       crossinline onEmojiSelected: (UIMessage, String) -> Unit
) {
    val TAG = "trifa.ChatMessage"
    var isHovered by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    Box(
        modifier = modifier.fillMaxWidth()
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false },
        contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart,
    ) {

        Row(verticalAlignment = Alignment.Bottom) {
            if (!isMyMessage) {
                Column {
                    UserPic(message.user, ui_scale)
                }
                Spacer(Modifier.size(2.dp))
                Column {
                    Triangle(true, -1, ChatColorsConfig.OTHERS_MESSAGE, MESSAGE_BOX_BOTTOM_PADDING)
                }
            }
            Column {
                var image_save_ui_space = false
                if (message.trifaMsgType == TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value)
                {
                    if (is_filetransfer_finished_or_canceled(message, true))
                    {
                        if (check_filename_is_image(message.filename_fullpath))
                        {
                            image_save_ui_space = true
                        }
                    }
                }

                var start_padding = 10.dp
                var start_top = 5.dp
                var start_end = 10.dp
                var start_bottom = 5.dp
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
                        .padding(start = start_padding, top = start_top, end = start_end, bottom = start_bottom),
                ) {
                    // -------- Message Content Box --------
                    // -------- Message Content Box --------
                    // -------- Message Content Box --------
                    Column(Modifier.padding(all = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        if(!isMyMessage) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    fontFamily = DefaultFont,
                                    text = message.user.name,
                                    style = MaterialTheme.typography.body1.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = TextUnit.Unspecified,
                                        letterSpacing = 0.sp,
                                        fontSize = ((MSG_TEXT_FONT_SIZE_MIXED * ui_scale / 1.285f).toDouble()).sp
                                    ),
                                    color = message.user.color
                                )
                            }
                        }

                        var show_link_click by remember { mutableStateOf(false) }
                        var link_str by remember { mutableStateOf("") }
                        if (!image_save_ui_space)
                        {
                            message_text_block(message, ui_scale) { show_link_click_, link_str_ ->
                                show_link_click = show_link_click_
                                link_str = link_str_
                            }
                            show_open_link_dialog(show_link_click, link_str) { show_link_click_, link_str_ ->
                                show_link_click = show_link_click_
                                link_str = link_str_
                            }
                        }
                        // ---------------- Filetransfer ----------------
                        // ---------------- Filetransfer ----------------
                        if (message.trifaMsgType == TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value)
                        {
                            if (message.direction == TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value)
                            {
                                incoming_filetransfer(message, ui_scale)
                            }
                            else
                            {
                                outgoing_filetransfer(message, ui_scale)
                            }
                        }
                        // ---------------- Filetransfer ----------------
                        // ---------------- Filetransfer ----------------
                        Row(
                            horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start,
                            modifier = Modifier.randomDebugBorder().padding(all = 0.dp)
                                .align(if (isMyMessage) Alignment.End else Alignment.Start)
                        ) {
                            var msg_version_int: Int = 1
                            if (message.msg_version == 1) {
                                msg_version_int = 2
                            } else {
                                if (message.msg_idv3_hash.isNullOrEmpty()) {
                                    msg_version_int = 1
                                } else {
                                    msg_version_int = 3
                                }
                            }

                            if (isMyMessage) {
                                message_checkmarks(isMyMessage, message, msg_version_int)
                                message_timestamp_and_info(message, msg_version_int)
                            } else {
                                message_timestamp_and_info(message, msg_version_int)
                                message_checkmarks(isMyMessage, message, msg_version_int)
                            }
                        }
                    }
                    // -------- Message Content Box --------
                    // -------- Message Content Box --------
                    // -------- Message Content Box --------
                }
                Box(Modifier.size(MESSAGE_BOX_BOTTOM_PADDING))
            }
            if (isMyMessage) {
                Column {
                    Triangle(false, -1, ChatColorsConfig.MY_MESSAGE, MESSAGE_BOX_BOTTOM_PADDING)
                }
            }
        }

        // 1. Define a state variable to track if the button row is forced hidden by a right-click
        var isForceHidden by remember { mutableStateOf(false) }

        // Reset the hidden state when the hover ends so it can reappear next time
        LaunchedEffect(isHovered) {
            if (!isHovered) {
                isForceHidden = false
            }
        }

        // 2. Overlay the Action Bar on Hover
        AnimatedVisibility(
            // Visible if hovered AND NOT forced hidden by a right-click
            visible = isHovered && !isForceHidden,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier
                .align(if (isMyMessage) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = 16.dp)
        ) {
            // Shared container logic for the entire action group
            Surface(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp), // Rounded bar shape instead of a circle
                color = MaterialTheme.colors.surface,
                border = BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.08f)),
                modifier = Modifier
                    .pointerInput(Unit) {
                        // Right-click anywhere on the bar container hides it
                        detectTapGestures(
                            matcher = PointerMatcher.mouse(PointerButton.Secondary),
                            onTap = { isForceHidden = true }
                        )
                    }
            ) {
                // Arrange emoji list, reply, and delete horizontally
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {

                    // --- EMOJI REACTIONS SECTION ---
                    val emojis = listOf("👍", "👎", "❤️")
                    emojis.forEach { emoji ->
                        IconButton(
                            onClick = { onEmojiSelected(message, emoji) },
                            modifier = Modifier.size((28 * ui_scale).dp) // Reduced footprint size
                        ) {
                            Text(
                                text = emoji,
                                fontSize = (14 * ui_scale).sp // Balanced scaled font size
                            )
                        }
                    }

                    // Subtle vertical separator line between reactions and actions
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(1.dp)
                            .height((16 * ui_scale).dp)
                            .background(MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
                    )

                    // --- COPY ACTION BUTTON ---
                    TooltipArea(
                        tooltip = { ActionTooltip(text = "Copy", ui_scale = ui_scale) },
                        tooltipPlacement = TooltipPlacement.CursorPoint(
                            alignment = Alignment.BottomCenter,
                            offset = DpOffset(0.dp, 8.dp)
                        ),
                        delayMillis = 400
                    ) {
                        IconButton(
                            onClick = { clipboardManager.setText(AnnotatedString(message.text)) },
                            modifier = Modifier.size((28 * ui_scale).dp) // Reduced footprint size
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.size((14 * ui_scale).dp) // Reduced icon size
                            )
                        }
                    }

                    // --- REPLY ACTION BUTTON ---
                    TooltipArea(
                        tooltip = { ActionTooltip(text = "Reply", ui_scale = ui_scale) },
                        tooltipPlacement = TooltipPlacement.CursorPoint(
                            alignment = Alignment.BottomCenter,
                            offset = DpOffset(0.dp, 8.dp)
                        ),
                        delayMillis = 400
                    ) {
                        IconButton(
                            onClick = { onReplySelected(message) },
                            modifier = Modifier.size((28 * ui_scale).dp) // Reduced footprint size
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = "Reply",
                                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size((14 * ui_scale).dp) // Reduced icon size
                                    .graphicsLayer(
                                        scaleX = if (isMyMessage) 1f else -1f
                                    )
                            )
                        }
                    }

                    // --- DELETE ACTION BUTTON ---
                    TooltipArea(
                        tooltip = { ActionTooltip(text = "Delete", ui_scale = ui_scale) },
                        tooltipPlacement = TooltipPlacement.CursorPoint(
                            alignment = Alignment.BottomCenter,
                            offset = DpOffset(0.dp, 8.dp)
                        ),
                        delayMillis = 400
                    ) {
                        IconButton(
                            onClick = { onDeleteSelected(message) },
                            modifier = Modifier.size((28 * ui_scale).dp) // Reduced footprint size
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete, // Ensure Icons.Default.Delete is imported
                                contentDescription = "Delete",
                                tint = MaterialTheme.colors.error.copy(alpha = 0.8f), // Red color highlight
                                modifier = Modifier.size((14 * ui_scale).dp) // Reduced icon size
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun message_checkmarks(isMyMessage: Boolean, message: UIMessage, msg_version_int: Int)
{
    if (isMyMessage)
    {
        if (message.read)
        {
            if (msg_version_int == 2)
            {
                Box(
                    modifier = Modifier.height(MESSAGE_CHECKMARKS_CONTAINER_SIZE)
                        .background(Color.Transparent, CircleShape),
                ) {
                    Tooltip(text = "Message delivery (confirmed)", textcolor = Color.Black) {
                        Icon(Icons.Filled.Check, tint = DELIVERY_CHECKMARK_COLOR,
                            contentDescription = "Message delivered")
                        Icon(Icons.Filled.Check, tint = DELIVERY_CONFIRM_CHECKMARK_COLOR,
                            modifier = Modifier.padding(
                                start = MESSAGE_CHECKMARKS_CONTAINER_SIZE * 0.4f),
                            contentDescription = "Message delivery (confirmed)")
                    }
                }
            } else
            {
                IconButton(
                    modifier = Modifier.size(MESSAGE_CHECKMARKS_CONTAINER_SIZE)
                        .background(Color.Transparent, CircleShape),
                    icon = Icons.Filled.Check,
                    iconTint = DELIVERY_CHECKMARK_COLOR,
                    enabled = false,
                    iconSize = MESSAGE_CHECKMARKS_ICON_SIZE,
                    contentDescription = "Message delivered",
                    onClick = {}
                )
            }
        } else if (message.sent_push == 1)
        {
            IconButton(
                modifier = Modifier.size(MESSAGE_CHECKMARKS_CONTAINER_SIZE)
                    .background(Color.Transparent, CircleShape),
                icon = Icons.Filled.ArrowCircleUp,
                iconTint = MESSAGE_PUSH_CHECKMARK_COLOR,
                enabled = false,
                iconSize = MESSAGE_CHECKMARKS_ICON_SIZE,
                contentDescription = "Push Notification sent" + "\n"
                        + "The Push Notification does not contain any data," + "\n"
                        + "it is only a trigger to wake up the device of the friend",
                onClick = {}
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun outgoing_filetransfer(message: UIMessage, ui_scale: Float)
{
    if (message.filename_fullpath != null)
    {
        if (message.file_state == ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE.value)
        {
            // we have the option to start or cancel the outgoing FT here
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (check_filename_is_image(message.filename_fullpath))
                {
                    show_filetransfer_image(ui_scale = ui_scale, clickable = true, small = true,
                        fullpath = message.filename_fullpath, description = "Image")
                } else
                {
                    show_filetransfer_image(ui_scale = ui_scale, clickable = true, small = true,
                        icon = Icons.Default.Attachment,
                        tint = MaterialTheme.colors.primary,
                        fullpath = message.filename_fullpath, description = "File")
                }
                Spacer(Modifier.width(4.dp))
                IconButton(
                    icon = Icons.Filled.Check,
                    iconTint = Color.Green,
                    iconSize = 20.dp,
                    modifier = Modifier.size(24.dp),
                    contentDescription = "start",
                    onClick = {
                        set_message_queueing_from_id(message.msgDatabaseId, true)
                    }
                )
                IconButton(
                    icon = Icons.Filled.Cancel,
                    iconTint = Color.Red,
                    iconSize = 20.dp,
                    modifier = Modifier.size(24.dp),
                    contentDescription = "cancel",
                    onClick = {
                        cancel_ft_from_ui(message)
                    }
                )
            }
        } else if (message.file_state == ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value)
        {
            // filetransfer finished (either because of CANCEL or OK) ------------
            // filetransfer finished (either because of CANCEL or OK) ------------
            if (check_filename_is_image(message.filename_fullpath))
            {
                var file_name_without_path = ""
                try
                {
                    file_name_without_path = File(message.filename_fullpath).name
                }
                catch(_: Exception)
                {
                }
                var file_size_in_bytes = "???"
                var file_size_human = file_size_in_bytes
                try
                {
                    file_size_human = byteCountToDisplaySize(File(message.filename_fullpath).length())
                    file_size_in_bytes = File(message.filename_fullpath).length().toString()
                }
                catch(_: Exception)
                {
                }
                Tooltip(text = "Filename: " + file_name_without_path + "\n"
                        + "Filesize: " + file_size_human + "\n"
                        + "Filesize: " + file_size_in_bytes + " Bytes",
                    textcolor = Color.Black) {
                    show_filetransfer_image(ui_scale = ui_scale, clickable = true,
                        fullpath = message.filename_fullpath, description = "Image")
                }
            } else
            {
                show_filetransfer_image(ui_scale = ui_scale, clickable = true,
                    icon = Icons.Default.Attachment,
                    tint = MaterialTheme.colors.primary,
                    fullpath = message.filename_fullpath, description = "File")
            }
            // filetransfer finished (either because of CANCEL or OK) ------------
            // filetransfer finished (either because of CANCEL or OK) ------------
        } else // TOX_FILE_CONTROL_RESUME
        {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically)
            {
                var progress_: Float = 0.0f
                try
                {
                    progress_ = (message.currentfilepos.toFloat() / message.filesize.toFloat())
                }
                catch(_: Exception)
                {
                }
                if (progress_ < 0)
                {
                    progress_ = 0.0f
                }
                if (progress_.isNaN()) {
                    progress_ = 0.0f
                }
                LinearProgressIndicator(
                    progress = {
                        progress_
                    },
                    strokeCap = StrokeCap.Round,
                    drawStopIndicator = {},
                    modifier = Modifier.weight(1f).height(8.dp),
                )
                val currentTime = message.currentfileposTimeMs
                val deltaBytes: Float = message.currentfilepos.toFloat() - message.startfilepos.toFloat()
                val deltaTimeMs = currentTime - message.startfileposTimeMs
                var transferSpeedKbps: Float = 0f

                if (deltaTimeMs > 0) {
                    val deltaTimeSeconds = deltaTimeMs / 1000f
                    transferSpeedKbps = (deltaBytes / 1024f) / deltaTimeSeconds
                }
                /*
                Log.i("XXXXXXXXXXXX", " deltabyte="+deltaBytes+" "+currentTime +
                        " curpos="+message.currentfilepos +
                        " startpos="+message.startfilepos +
                        " "+message.startfileposTimeMs+" deltatime="+deltaTimeMs +" "+transferSpeedKbps)
                */
                Text(modifier = Modifier.padding(start = 8.dp),
                    fontSize = 12.sp,
                    text = "" + ((message.currentfilepos.toFloat() / message.filesize.toFloat()) * 100.0f).toLong() + "%" +
                            " " + formatSpeed(transferSpeedKbps)
                )
                IconButton(
                    icon = Icons.Filled.Cancel,
                    iconTint = Color.Red,
                    iconSize = 20.dp,
                    modifier = Modifier.size(24.dp),
                    contentDescription = "cancel",
                    onClick = {
                        cancel_ft_from_ui(message)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun incoming_filetransfer(message: UIMessage, ui_scale: Float)
{
    if (is_filetransfer_in_progress(message))
    {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically)
        {
            var progress_: Float = 0.0f
            try
            {
                progress_ = (message.currentfilepos.toFloat() / message.filesize.toFloat())
            }
            catch(_: Exception)
            {
            }
            if (progress_ < 0)
            {
                progress_ = 0.0f
            }
            if (progress_.isNaN()) {
                progress_ = 0.0f
            }
            LinearProgressIndicator(
                progress = {
                    progress_
                },
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {},
                modifier = Modifier.weight(1f).height(8.dp),
            )
            val currentTime = message.currentfileposTimeMs
            val deltaBytes: Float = message.currentfilepos.toFloat()
            val deltaTimeMs = currentTime - message.startfileposTimeMs
            var transferSpeedKbps: Float = 0f

            if (deltaTimeMs > 0) {
                val deltaTimeSeconds = deltaTimeMs / 1000f
                transferSpeedKbps = (deltaBytes / 1024f) / deltaTimeSeconds
            }

            Text(modifier = Modifier.padding(start = 8.dp),
                fontSize = 12.sp,
                text = "" + ((message.currentfilepos.toFloat() / message.filesize.toFloat()) * 100.0f).toLong() + "%" +
                        " " + formatSpeed(transferSpeedKbps)
            )
            IconButton(
                icon = Icons.Filled.Cancel,
                iconTint = Color.Red,
                iconSize = 20.dp,
                modifier = Modifier.size(24.dp),
                contentDescription = "cancel",
                onClick = {
                    cancel_ft_from_ui(message)
                }
            )
        }
    } else
    {
        if (message.filename_fullpath != null)
        {
            if (check_filename_is_image(message.filename_fullpath))
            {
                var file_name_without_path = ""
                try
                {
                    file_name_without_path = File(message.filename_fullpath).name
                }
                catch(_: Exception)
                {
                }
                var file_size_in_bytes = "???"
                var file_size_human = file_size_in_bytes
                try
                {
                    file_size_human = byteCountToDisplaySize(File(message.filename_fullpath).length())
                    file_size_in_bytes = File(message.filename_fullpath).length().toString()
                }
                catch(_: Exception)
                {
                }
                Tooltip(text = "Filename: " + file_name_without_path + "\n"
                        + "Filesize: " + file_size_human + "\n"
                        + "Filesize: " + file_size_in_bytes + " Bytes",
                    textcolor = Color.Black) {
                    show_filetransfer_image(ui_scale = ui_scale, clickable = true,
                        fullpath = message.filename_fullpath, description = "Image")
                }
            } else
            {
                show_filetransfer_image(ui_scale = ui_scale, clickable = true,
                    icon = Icons.Default.Attachment,
                    tint = MaterialTheme.colors.primary,
                    fullpath = message.filename_fullpath, description = "File")
            }
        } else
        {
            show_filetransfer_image(ui_scale = ui_scale, tint = MaterialTheme.colors.primary)
        }
    }
}

fun is_filetransfer_in_progress(message: UIMessage): Boolean
{
    if ((message.filesize > 0.0f) && (message.currentfilepos < message.filesize))
    {
        return true
    }
    else
    {
        return false
    }
}

fun is_filetransfer_finished_or_canceled(message: UIMessage, direction_outgoing: Boolean): Boolean
{
    if (direction_outgoing)
    {
        if (message.filename_fullpath != null)
        {
            if (message.file_state == ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value)
            {
                // filetransfer finished (either because of CANCEL or OK) ------------
                return true
            }
        }
    }
    return false
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun show_filetransfer_image(ui_scale: Float,
                                    clickable: Boolean = false,
                                    icon: ImageVector? = null,
                                    tint: Color = MaterialTheme.colors.primary,
                                    fullpath: String? = null,
                                    description: String = "failed",
                                    small: Boolean = false)
{
    val imgSize = if (small) 32.dp else (IMAGE_PREVIEW_SIZE.dp * ui_scale)
    if (fullpath == null) {
        Icon(
            modifier = Modifier.size(imgSize),
            imageVector = Icons.Default.BrokenImage,
            contentDescription = description,
            tint = tint
        )
    } else if (icon != null) {
        Icon(
            modifier = Modifier.size(imgSize).combinedClickable(
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
            modifier = Modifier.size(imgSize).combinedClickable(
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
fun message_timestamp_and_info(message: UIMessage, msg_version_int: Int)
{
    var message_size_in_bytes = 0
    try
    {
        message_size_in_bytes = message.text.toByteArray().size
    } catch (_: Exception)
    {
    }
    val msg_v2_hash_str = if (message.msg_id_hash.isNullOrEmpty()) "" else message.msg_id_hash
    val msg_v3_hash_str = if (message.msg_idv3_hash.isNullOrEmpty()) "" else message.msg_idv3_hash
    var file_info_lines = ""
    if (message.trifaMsgType == TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value)
    {
        if (message.filename_fullpath.isNullOrEmpty())
        {
            file_info_lines = "File fullpath: " + "???" + "\n"
        }
        else
        {
            file_info_lines = "File fullpath: " + message.filename_fullpath + "\n"
        }
        try
        {
            file_info_lines = file_info_lines + "File size in bytes: " + File(message.filename_fullpath!!).length() + "\n"
        }
        catch(_: Exception)
        {
            file_info_lines = file_info_lines + "File size: " + "???" + "\n"
        }
    }
    Tooltip("Message sent at: " + timeToString(message.sentTimeMs) + "\n" +
            "Message rcvd at: " + timeToString(message.recvTimeMs) + "\n" +
            "Message size in bytes: " + (if (message_size_in_bytes == 0) "unknown" else message_size_in_bytes) + "\n" +
            "Message version: " + msg_version_int + "\n" +
            "Message V2 Hash: " + msg_v2_hash_str + "\n" +
            "Message V3 Hash: " + msg_v3_hash_str + "\n" +
            file_info_lines +
            "The clocks on both sides are not synchronized for security reasons, " + "\n" +
            "therfore the timestamps may not be accurate") {
        Text(
            modifier = Modifier.padding(all = 0.dp),
            text = timeToString(message.timeMs),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.subtitle1.copy(fontSize = 10.sp, lineHeight = TextUnit.Unspecified),
            color = ChatColorsConfig.TIME_TEXT
        )
    }
}

@Composable
fun show_open_link_dialog(
    show_link_click: Boolean,
    link_str: String,
    setLinkVars: (Boolean, String) -> Unit
) {
    var show_link_click1 = show_link_click
    var link_str1 = link_str

    val clipboardManager = LocalClipboardManager.current

    if (show_link_click1) {
        val closeDialog: () -> Unit = {
            link_str1 = ""
            show_link_click1 = false
            setLinkVars(show_link_click1, link_str1)
        }

        AlertDialog(
            onDismissRequest = closeDialog,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,

            title = {
                Text(
                    text = "Open this URL ?",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )
            },

            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // Highly Visual Warning Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Warning",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "This could be potentially dangerous!",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    // Stylish Link Item Box
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    text = link_str1,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        open_webpage(link_str1)
                        closeDialog()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Yes", fontWeight = FontWeight.SemiBold)
                }
            },

            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = closeDialog,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("No", fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(link_str1))
                            closeDialog()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("copy URL", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        )
    }
}

@Composable
fun message_text_block(message: UIMessage, ui_scale: Float, setLinkVars: (Boolean, String) -> Unit)
{
    var show_link_click1 = false
    var link_str1 = ""
    var text_is_only_emoji = false
    SelectionContainer(modifier = Modifier.padding(all = 0.dp))
    {
        var msg_fontsize = MSG_TEXT_FONT_SIZE_MIXED
        try
        {
            val emojiInformation = message.text.emojiInformation()
            if (emojiInformation.isOnlyEmojis)
            {
                msg_fontsize = MSG_TEXT_FONT_SIZE_EMOJI_ONLY
                text_is_only_emoji = true
            }
        } catch (_: Exception)
        {
        }

        var message_text_string = message.text
        if (message.trifaMsgType == TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value)
        {
            text_is_only_emoji = false
            if (message.direction == TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value)
            {
                if (is_filetransfer_finished_or_canceled(message, true))
                {
                    if (check_filename_is_image(message.filename_fullpath))
                    {
                        message_text_string = ""
                    }
                }
            }
        }

        UrlHighlightTextView(
            text = message_text_string,
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


@Composable
fun UrlHighlightTextView(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    onClick: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colors
    val textStyle = SpanStyle(color = colorScheme.onBackground)
    val urlStyle = SpanStyle(color = Color(URL_TEXTVIEW_URL_COLOR))

    // Configure the high-performance link extractor
    val linkExtractor = remember {
        LinkExtractor.builder()
            .linkTypes(setOf(LinkType.URL, LinkType.WWW))
            .build()
    }

    // Build the annotated string efficiently
    val annotatedString = remember(text, textStyle, urlStyle) {
        buildAnnotatedString {
            val spans = linkExtractor.extractLinks(text)
            var lastIndex = 0

            for (span in spans) {
                // 1. Append preceding plain text block
                if (span.beginIndex > lastIndex) {
                    withStyle(style = textStyle) {
                        append(text.substring(lastIndex, span.beginIndex))
                    }
                }

                // 2. Append link block with a built-in modern Clickable Annotation
                val linkText = text.substring(span.beginIndex, span.endIndex)
                val linkAnnotation = LinkAnnotation.Url(
                    url = linkText,
                    styles = androidx.compose.ui.text.TextLinkStyles(style = urlStyle),
                    linkInteractionListener = {
                        onClick(linkText)
                    }
                )

                withLink(linkAnnotation) {
                    append(linkText)
                }

                lastIndex = span.endIndex
            }

            // 3. Append remaining trailing text block
            if (lastIndex < text.length) {
                withStyle(style = textStyle) {
                    append(text.substring(lastIndex, text.length))
                }
            }
        }
    }

    // BasicText seamlessly handles modern native LinkAnnotations out-of-the-box
    BasicText(
        text = annotatedString,
        modifier = modifier,
        style = style
    )
}

@Composable
fun UrlHighlightTextView__UNUSED_PREVIOUS_IMPL(text: String, modifier: Modifier = Modifier, style: TextStyle, onClick: (String) -> Unit) {

    val colorScheme = MaterialTheme.colors
    val textStyle = SpanStyle(color = colorScheme.onBackground)
    val urlStyle = SpanStyle(color = Color(URL_TEXTVIEW_URL_COLOR))

    // -----------------------------------------------------
    // works ok
    // val hashtags = Regex("((?=[^\\w!])[#@][\\u4e00-\\u9fa5\\w]+)")
    // -----------------------------------------------------
    // does not really work good
    // val urls = Regex("""(https://www\.|http://www\.|https://|http://)?[a-zA-Z]{2,}(\.[a-zA-Z]{2,})(\.[a-zA-Z]{2,})?/[a-zA-Z0-9]{2,}|((https://www\.|http://www\.|https://|http://)?[a-zA-Z]{2,}(\.[a-zA-Z]{2,})(\.[a-zA-Z]{2,})?)|(https://www\.|http://www\.|https://|http://)?[a-zA-Z0-9]{2,}\.[a-zA-Z0-9]{2,}\.[a-zA-Z0-9]{2,}(\.[a-zA-Z0-9]{2,})?""")
    // -----------------------------------------------------
    // works ok
    // val urls = Regex("^https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_\\+.~#?&\\/=]*)$")
    // -----------------------------------------------------
    //val urls = Regex("(^|[\\s.:;?\\-\\]<\\(])" +
    //        "((https?://|www\\.|pic\\.)[-\\w;/?:@&=+$\\|\\_.!~*\\|'()\\[\\]%#,☺]+[\\w/#](\\(\\))?)" +
    //        "(?=$|[\\s',\\|\\(\\).:;?\\-\\[\\]>\\)])")
    // -----------------------------------------------------
    // val urls = Regex("""\b(?:https?://)?(?:(?i:[a-z]+\.)+)[^\s,]+\b""")
    // -----------------------------------------------------
    // works best for now
    val urls = Regex("(((((http|ftp|https|gopher|telnet|file|localhost):\\/\\/)|(www\\.)|(xn--)){1}([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])?)|(([\\w_-]{2,200}(?:(?:\\.[\\w_-]+)*))((\\.[\\w_-]+\\/([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])?)|(\\.((org|com|net|edu|gov|mil|int|arpa|biz|info|unknown|one|ninja|network|host|coop|tech)|(jp|br|it|cn|mx|ar|nl|pl|ru|tr|tw|za|be|uk|eg|es|fi|pt|th|nz|cz|hu|gr|dk|il|sg|uy|lt|ua|ie|ir|ve|kz|ec|rs|sk|py|bg|hk|eu|ee|md|is|my|lv|gt|pk|ni|by|ae|kr|su|vn|cy|am|ke))))))(?!(((ttp|tp|ttps):\\/\\/)|(ww\\.)|(n--)))")
    // -----------------------------------------------------

    val annotatedStringList = remember {

        var lastIndex = 0
        val annotatedStringList = mutableStateListOf<AnnotatedString.Range<String>>()

        // Add a text range for urls
        for (match in urls.findAll(text)) {

            val start = match.range.first
            val end = match.range.last + 1
            val string = text.substring(start, end)

            if (start > lastIndex) {
                annotatedStringList.add(
                    AnnotatedString.Range(
                        text.substring(lastIndex, start),
                        lastIndex,
                        start,
                        "text"
                    )
                )
            }
            annotatedStringList.add(
                AnnotatedString.Range(string, start, end, "link")
            )
            lastIndex = end
        }

        // Add remaining text
        if (lastIndex < text.length) {
            annotatedStringList.add(
                AnnotatedString.Range(
                    text.substring(lastIndex, text.length),
                    lastIndex,
                    text.length,
                    "text"
                )
            )
        }
        annotatedStringList
    }

    // Build an annotated string
    val annotatedString = buildAnnotatedString {
        annotatedStringList.forEach {
            if (it.tag == "link") {
                pushStringAnnotation(tag = it.tag, annotation = it.item)
                withStyle(style = urlStyle) { append(it.item) }
                pop()
            } else {
                withStyle(style = textStyle) { append(it.item) }
            }
        }
    }

    ClickableText(
        text = annotatedString,
        style = style,
        modifier = modifier,
        onClick = { position ->
            try
            {
                val annotatedStringRange =
                    annotatedStringList.first { it.start < position && position < it.end }
                if (annotatedStringRange.tag == "link")
                {
                    onClick(annotatedStringRange.item)
                }
            }
            catch(_: Exception)
            {
            }
        }
    )
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

fun formatSpeed(speedKbps: Float): String {
    if (speedKbps < 0)
    {
        return "0"
    }
    else if (speedKbps < 1f)
    {
        // Display in bytes/sec
        val bytesPerSec = speedKbps * 1024
        return String.format("%.0f B/sec", bytesPerSec)
    } else if (speedKbps > 1000f) {
        // Display in MB/sec
        val bytesPerSec = speedKbps / 1024
        return String.format("%.1f MiB/sec", bytesPerSec)
    } else {
        // Display in KB/sec
        return String.format("%.1f kiB/sec", speedKbps)
    }
}

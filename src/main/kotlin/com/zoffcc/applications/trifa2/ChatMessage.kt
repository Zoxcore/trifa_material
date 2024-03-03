import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanniktech.emoji.emojiInformation
import com.zoffcc.applications.trifa.HelperFiletransfer.check_filename_is_image
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.HelperGeneric.cancel_ft_from_ui
import com.zoffcc.applications.trifa.HelperMessage.set_message_queueing_from_id
import com.zoffcc.applications.trifa.HelperOSFile.open_webpage
import com.zoffcc.applications.trifa.HelperOSFile.show_containing_dir_in_explorer
import com.zoffcc.applications.trifa.HelperOSFile.show_file_in_explorer_or_open
import com.zoffcc.applications.trifa.TRIFAGlobals
import com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE
import com.zoffcc.applications.trifa.ToxVars
import org.briarproject.briar.desktop.ui.Tooltip
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
inline fun ChatMessage(isMyMessage: Boolean, message: UIMessage, ui_scale: Float) {
    val TAG = "trifa.ChatMessage"
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart,
    ) {

        Row(verticalAlignment = Alignment.Bottom) {
            if (!isMyMessage) {
                Column {
                    UserPic(message.user, ui_scale)
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
                    // -------- Message Content Box --------
                    // -------- Message Content Box --------
                    // -------- Message Content Box --------
                    Column(Modifier.randomDebugBorder().padding(all = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        if(!isMyMessage) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = message.user.name,
                                    style = MaterialTheme.typography.body1.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = TextUnit.Unspecified,
                                        letterSpacing = 0.sp,
                                        fontSize = 14.sp
                                    ),
                                    color = message.user.color
                                )
                            }
                        }

                        var show_link_click by remember { mutableStateOf(false) }
                        var link_str by remember { mutableStateOf("") }

                        message_text_block(message, ui_scale) { show_link_click_, link_str_ ->
                            show_link_click = show_link_click_
                            link_str = link_str_
                        }
                        show_open_link_dialog(show_link_click, link_str) { show_link_click_, link_str_ ->
                            show_link_click = show_link_click_
                            link_str = link_str_
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
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.randomDebugBorder().padding(all = 0.dp).align(Alignment.End)
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

                            // ---------------- message checkmarks (push, delivery) ----------------
                            // ---------------- message checkmarks (push, delivery) ----------------
                            if (isMyMessage) {
                                if (message.read)
                                {
                                    if (msg_version_int == 2)
                                    {
                                        Box(
                                            modifier = Modifier.height(MESSAGE_CHECKMARKS_CONTAINER_SIZE)
                                                .align(Alignment.Bottom)
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
                                    }
                                    else
                                    {
                                        IconButton(
                                            modifier = Modifier.size(MESSAGE_CHECKMARKS_CONTAINER_SIZE)
                                                .align(Alignment.Bottom)
                                                .background(Color.Transparent, CircleShape),
                                            icon = Icons.Filled.Check,
                                            iconTint = DELIVERY_CHECKMARK_COLOR,
                                            enabled = false,
                                            iconSize = MESSAGE_CHECKMARKS_ICON_SIZE,
                                            contentDescription = "Message delivered",
                                            onClick = {}
                                        )
                                    }
                                }
                                else if (message.sent_push == 1)
                                {
                                    IconButton(
                                        modifier = Modifier.size(MESSAGE_CHECKMARKS_CONTAINER_SIZE)
                                            .align(Alignment.Bottom)
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
                            // ---------------- message checkmarks (push, delivery) ----------------
                            // ---------------- message checkmarks (push, delivery) ----------------

                            message_timestamp_and_info(message, msg_version_int)
                        }
                    }
                    // -------- Message Content Box --------
                    // -------- Message Content Box --------
                    // -------- Message Content Box --------
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun outgoing_filetransfer(message: UIMessage, ui_scale: Float)
{
    if (message.filename_fullpath != null)
    {
        if (message.file_state == ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE.value)
        {
            // we have the option to start or cancel the outgoing FT here
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.size(10.dp).align(Alignment.Start))
                Row(modifier = Modifier.align(Alignment.Start)) {
                    IconButton(
                        icon = Icons.Filled.Check,
                        iconTint = Color.Green,
                        iconSize = 30.dp,
                        contentDescription = "start",
                        onClick = {
                            set_message_queueing_from_id(message.msgDatabaseId, true)
                        }
                    )
                    IconButton(
                        icon = Icons.Filled.Cancel,
                        iconTint = Color.Red,
                        iconSize = 30.dp,
                        contentDescription = "cancel",
                        onClick = {
                            cancel_ft_from_ui(message)
                        }
                    )
                }
            }
        } else if (message.file_state == ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value)
        {
            if (check_filename_is_image(message.filename_fullpath))
            {
                HelperGeneric.AsyncImage(load = {
                    HelperGeneric.loadImageBitmap(File(message.filename_fullpath))
                }, painterFor = { remember { BitmapPainter(it) } },
                    contentDescription = "Image",
                    modifier = Modifier.size(IMAGE_PREVIEW_SIZE.dp * ui_scale).combinedClickable(
                        onClick = { show_file_in_explorer_or_open(message.filename_fullpath) },
                        onLongClick = { show_containing_dir_in_explorer(message.filename_fullpath) }))
            } else
            {
                Icon(
                    modifier = Modifier.size(IMAGE_PREVIEW_SIZE.dp * ui_scale).combinedClickable(
                        onClick = { show_file_in_explorer_or_open(message.filename_fullpath) },
                        onLongClick = { show_containing_dir_in_explorer(message.filename_fullpath) }),
                    imageVector = Icons.Default.Attachment,
                    contentDescription = "File",
                    tint = MaterialTheme.colors.primary
                )
            }
        } else // TOX_FILE_CONTROL_RESUME
        {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = (message.currentfilepos.toFloat() / message.filesize.toFloat())
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.size(10.dp).align(Alignment.Start))
                IconButton(
                    icon = Icons.Filled.Cancel,
                    iconTint = Color.Red,
                    iconSize = 30.dp,
                    modifier = Modifier.align(Alignment.Start),
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
    if ((message.filesize > 0.0f) && (message.currentfilepos < message.filesize))
    {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = (message.currentfilepos.toFloat() / message.filesize.toFloat())
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.size(10.dp).align(Alignment.Start))
            IconButton(
                icon = Icons.Filled.Cancel,
                iconTint = Color.Red,
                iconSize = 30.dp,
                modifier = Modifier.align(Alignment.Start),
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
                HelperGeneric.AsyncImage(load = {
                    HelperGeneric.loadImageBitmap(File(message.filename_fullpath))
                }, painterFor = { remember { BitmapPainter(it) } },
                    contentDescription = "Image",
                    modifier = Modifier.size(IMAGE_PREVIEW_SIZE.dp * ui_scale).combinedClickable(
                        onClick = { show_file_in_explorer_or_open(message.filename_fullpath) },
                        onLongClick = { show_containing_dir_in_explorer(message.filename_fullpath) }))
            } else
            {
                Icon(
                    modifier = Modifier.size(IMAGE_PREVIEW_SIZE.dp * ui_scale).combinedClickable(
                        onClick = { show_file_in_explorer_or_open(message.filename_fullpath) },
                        onLongClick = { show_containing_dir_in_explorer(message.filename_fullpath) }),
                    imageVector = Icons.Default.Attachment,
                    contentDescription = "File",
                    tint = MaterialTheme.colors.primary
                )
            }
        } else
        {
            Icon(
                modifier = Modifier.size(IMAGE_PREVIEW_SIZE.dp * ui_scale),
                imageVector = Icons.Default.BrokenImage,
                contentDescription = "failed",
                tint = MaterialTheme.colors.primary
            )
        }
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
    Tooltip("Message sent at: " + timeToString(message.sentTimeMs) + "\n" +
            "Message rcvd at: " + timeToString(message.recvTimeMs) + "\n" +
            "Message size in bytes: " + (if (message_size_in_bytes == 0) "unknown" else message_size_in_bytes) + "\n" +
            "Message version: " + msg_version_int + "\n" +
            "Message V2 Hash: " + msg_v2_hash_str + "\n" +
            "Message V3 Hash: " + msg_v3_hash_str + "\n" +
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
fun show_open_link_dialog(show_link_click: Boolean, link_str: String, setLinkVars: (Boolean, String) -> Unit)
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
fun message_text_block(message: UIMessage, ui_scale: Float, setLinkVars: (Boolean, String) -> Unit)
{
    var show_link_click1 = false
    var link_str1 = ""
    SelectionContainer(modifier = Modifier.padding(all = 0.dp))
    {
        var msg_fontsize = MSG_TEXT_FONT_SIZE_MIXED
        try
        {
            val emojiInformation = message.text.emojiInformation()
            if (emojiInformation.isOnlyEmojis)
            {
                msg_fontsize = MSG_TEXT_FONT_SIZE_EMOJI_ONLY
            }
        } catch (_: Exception)
        {
        }
        UrlHighlightTextView(
            text = message.text,
            modifier = Modifier.randomDebugBorder(),
            style = MaterialTheme.typography.body1.copy(
                fontSize = ((msg_fontsize * ui_scale).toDouble()).sp,
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

@Preview
@Composable
fun PreviewTest() {
    val string = "I am #hashtags or #hashtags# and @mentions in Jetpack Compose."
    UrlHighlightTextView(string, Modifier.padding(16.dp),
        style = MaterialTheme.typography.body1.copy(
            fontSize = 13.sp,
            lineHeight = TextUnit.Unspecified,
            letterSpacing = 0.sp
        )) {
        println(it)
    }
}

@Preview
@Composable
fun AlertDialogTest()
{
    AlertDialog(onDismissRequest = {},
        title = { Text("Hello title") },
        confirmButton = {
            Button(onClick = {
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = {
            }) {
                Text("Dismiss")
            }
        },
        text = { Text("Hello text") })
}

@Composable
fun UrlHighlightTextView(text: String, modifier: Modifier = Modifier, style: TextStyle, onClick: (String) -> Unit) {

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
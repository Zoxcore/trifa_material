import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.zoffcc.applications.trifa.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "trifa.SendGroupMessage"

@Composable
fun GroupSendMessage(focusRequester: FocusRequester, selectedGroupId: String?, sendGroupMessage: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }
    LaunchedEffect(selectedGroupId)
    {
        // Log.i(TAG, "selected group changed, reset input text")
        inputText = ""
    }
    var show_emoji_popup by remember { mutableStateOf(false) }
    TextField(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(1.dp)
            .focusRequester(focusRequester)
            .onPreviewKeyEvent {
                when {
                    (!it.isMetaPressed && !it.isAltPressed && !it.isCtrlPressed && !it.isShiftPressed && it.key == Key.Enter && it.type == KeyEventType.KeyDown) -> {
                        if (inputText.isNotEmpty())
                        {
                            sendGroupMessage(inputText)
                            inputText = ""
                        }
                        true
                    }
                    (!it.isMetaPressed && !it.isAltPressed && !it.isCtrlPressed && !it.isShiftPressed && it.key == Key.NumPadEnter && it.type == KeyEventType.KeyDown) -> {
                        if (inputText.isNotEmpty())
                        {
                            sendGroupMessage(inputText)
                            inputText = ""
                        }
                        true
                    }
                    (!it.isMetaPressed && !it.isAltPressed && !it.isCtrlPressed && !it.isShiftPressed && it.key == Key.Enter && it.type == KeyEventType.KeyUp) -> {
                        true
                    }
                    (!it.isMetaPressed && !it.isAltPressed && !it.isCtrlPressed && !it.isShiftPressed && it.key == Key.NumPadEnter && it.type == KeyEventType.KeyUp) -> {
                        true
                    }
                    else -> false
                }
            },
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false,
        ),
        value = inputText,
        placeholder = {
            Text(text = "Type Group Message...", fontSize = 14.sp)
        },
        onValueChange = {
            inputText = it
        },
        trailingIcon = {
            if (inputText.isNotEmpty()) {
                Row() {
                    Row(
                    modifier = Modifier
                        .clickable {
                            sendGroupMessage(inputText)
                            inputText = ""
                        }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                        ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colors.primary
                        )
                        Text("Send")
                    }
                    IconButton(
                        icon = Icons.Filled.InsertEmoticon,
                        iconTint = Color.DarkGray,
                        iconSize = 25.dp,
                        modifier = Modifier.width(40.dp).padding(end = 8.dp),
                        contentDescription = "",
                        onClick = {
                            if (show_emoji_popup == true)
                            {
                                show_emoji_popup = false
                            }
                            else
                            {
                                show_emoji_popup = true
                            }
                        }
                    )
                }
            }
            else
            {
                Row(modifier = Modifier.padding(end = 0.dp)) {
                    IconButton(
                        icon = Icons.Filled.InsertEmoticon,
                        iconTint = Color.DarkGray,
                        iconSize = 25.dp,
                        modifier = Modifier.width(40.dp),
                        contentDescription = "",
                        onClick = {
                            if (show_emoji_popup == true)
                            {
                                show_emoji_popup = false
                            }
                            else
                            {
                                show_emoji_popup = true
                            }
                        }
                    )
                }
            }
            if (show_emoji_popup)
            {
                val emoji_box_width_dp = 250.dp
                val emoji_box_height_dp = 230.dp
                val emoji_box_offset_x_px = 100.dp.DpAsPx.toInt()
                val emoji_box_offset_y_px = -(emoji_box_height_dp + 10.dp).DpAsPx.toInt()
                var cur_emoji_cat by remember { mutableStateOf(0) }
                Popup(alignment = Alignment.TopCenter,
                    properties = PopupProperties(focusable = false, dismissOnClickOutside = true),
                    onDismissRequest = {},
                    offset = IntOffset(emoji_box_offset_x_px, emoji_box_offset_y_px)) {
                    Box(
                        Modifier
                            .size(emoji_box_width_dp, emoji_box_height_dp)
                            .padding(top = 1.dp, bottom = 1.dp)
                            .background(MaterialTheme.colors.background)
                            .border(1.dp, color = Color.Black, RoundedCornerShape(10.dp))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(top = 4.dp, bottom = 4.dp, end = 1.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Row() {
                                for(k in 0..(emojis_cat_all_gropued.size - 1))
                                {
                                    IconButton(modifier = Modifier.width(30.dp).height(30.dp),
                                        onClick = { cur_emoji_cat = k }) {
                                        Text(text = emojis_cat_all_cat_emoji.get(k),
                                            color = Color.Black, fontSize = 20.sp, maxLines = 1)
                                    }
                                }
                            }
                            Spacer(Modifier.fillMaxWidth().height(2.dp).background(Color.LightGray))
                            val listState = rememberLazyListState()
                            Box(Modifier.fillMaxSize()) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(start = 1.dp, end = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                    state = listState,
                                ) {
                                    items(items = emojis_cat_all_gropued.get(cur_emoji_cat)) {
                                        Row(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                                            for (k in 0..(it.size - 1))
                                            {
                                                val emojistr = it[k]
                                                val placeholder = "?"
                                                var curtext by remember { mutableStateOf(placeholder) }
                                                val scope = rememberCoroutineScope()
                                                IconButton(modifier = Modifier.width(40.dp).height(40.dp),
                                                    onClick = { inputText = inputText + it[k] }) {
                                                    Text(text = curtext, color = Color.Black, fontSize = 30.sp, maxLines = 1)
                                                    scope.launch {
                                                        delay(62)
                                                        curtext = emojistr
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                VerticalScrollbar(
                                    adapter = rememberScrollbarAdapter(listState),
                                    modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd).width(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ).run {  }
}
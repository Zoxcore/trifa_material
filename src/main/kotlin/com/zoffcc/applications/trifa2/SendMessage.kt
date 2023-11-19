import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.HelperFiletransfer.get_incoming_filetransfer_local_filename
import com.zoffcc.applications.trifa.HelperMessage.getImageFromClipboard
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity.Companion.add_outgoing_file
import com.zoffcc.applications.trifa.TRIFAGlobals
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

private const val TAG = "trifa.SendMessage"

@Composable
fun SendMessage(focusRequester: FocusRequester, selectedContactPubkey: String?, sendMessage: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }
    LaunchedEffect(selectedContactPubkey)
    {
        // Log.i(TAG, "selected friend changed, reset input text")
        inputText = ""
    }
    TextField(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(1.dp)
            .focusRequester(focusRequester)
            .onPreviewKeyEvent {
                when {
                    (it.isCtrlPressed && it.key == Key.V && it.type == KeyEventType.KeyDown) -> {
                        Log.i(TAG, "PasteOccured...")
                        val img = getImageFromClipboard() as BufferedImage?
                        if (img != null)
                        {
                            Log.i(TAG, "PasteOccured...Image")
                            try
                            {
                                Log.i(TAG, "PasteOccured...Image:002")
                                Log.i(TAG, "PasteOccured...Image:003:" + selectedContactPubkey)
                                val friend_pubkey_str: String? = selectedContactPubkey
                                if (friend_pubkey_str != null)
                                {
                                    val wanted_full_filename_path: String = TRIFAGlobals.VFS_FILE_DIR + "/" + friend_pubkey_str
                                    File(wanted_full_filename_path).mkdirs()
                                    var filename_local_corrected = get_incoming_filetransfer_local_filename("clip.png",
                                        friend_pubkey_str)
                                    filename_local_corrected = "$wanted_full_filename_path/$filename_local_corrected"
                                    Log.i(TAG, "PasteOccured...Image:004:$filename_local_corrected")
                                    val f_send = File(filename_local_corrected)
                                    val res = ImageIO.write(img, "png", f_send)
                                    Log.i(TAG, "PasteOccured...Image:004:$filename_local_corrected res=$res")
                                    // send file
                                    add_outgoing_file(f_send.absoluteFile.parent, f_send.absoluteFile.name, friend_pubkey_str)
                                    return@onPreviewKeyEvent(true)
                                }
                            } catch (e2: Exception)
                            {
                                e2.printStackTrace()
                                Log.i(TAG, "PasteOccured...EE:" + e2.message)
                            }
                        }
                        false
                    }
                    (!it.isMetaPressed && !it.isAltPressed && !it.isCtrlPressed && !it.isShiftPressed && it.key == Key.Enter && it.type == KeyEventType.KeyDown) -> {
                        if (inputText.isNotEmpty())
                        {
                            sendMessage(inputText)
                            inputText = ""
                        }
                        true
                    }
                    (!it.isMetaPressed && !it.isAltPressed && !it.isCtrlPressed && !it.isShiftPressed && it.key == Key.NumPadEnter && it.type == KeyEventType.KeyDown) -> {
                        if (inputText.isNotEmpty())
                        {
                            sendMessage(inputText)
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
            }
        ,
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false,
        ),
        value = inputText,
        placeholder = {
            Text(text = "Type message...", fontSize = 14.sp)
        },
        onValueChange = {
            inputText = it
        },
        trailingIcon = {
            if (inputText.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .clickable {
                            sendMessage(inputText)
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
            }
        }
    ).run { }
}
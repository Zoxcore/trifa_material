import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.Log

private const val TAG = "trifa.SendMessage"

@Composable
fun SendMessage(sendMessage: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }
    val textFieldFocusRequester = remember { FocusRequester() }
    TextField(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(1.dp)
            .focusRequester(textFieldFocusRequester),
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
            // ?? haXX0r ??
            if (it == inputText + "\n")
            {
                // ?? haXX0r ??
                // Log.i(TAG, "enter key pressed")
                sendMessage(inputText)
                inputText = ""
            }
            else
            {
                inputText = it
            }
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
    ).run {  }
}
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_send_message
import com.zoffcc.applications.trifa.StateContacts
import com.zoffcc.applications.trifa.ToxVars.TOX_MESSAGE_TYPE
import com.zoffcc.applications.trifa.createContactStore
import com.zoffcc.applications.trifa.createSavepathStore
import com.zoffcc.applications.trifa.createToxDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

private const val TAG = "trifa.Chatapp"
val myUser = User("Me", picture = null, toxpk = null)
val store = CoroutineScope(SupervisorJob()).createStore()
val contactstore = CoroutineScope(SupervisorJob()).createContactStore()
val savepathstore = CoroutineScope(SupervisorJob()).createSavepathStore()
val toxdatastore = CoroutineScope(SupervisorJob()).createToxDataStore()

@Composable
fun ChatAppWithScaffold(displayTextField: Boolean = true, contactList: StateContacts)
{
    Theme {
        Scaffold(topBar = {
            TopAppBar(
                title = {
                    contactList.selectedContact?.let { Text(it.name) }
                },
                backgroundColor = MaterialTheme.colors.background,
            )
        }) {
            ChatApp(displayTextField = displayTextField, contactList.selectedContactPubkey)
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ChatApp(displayTextField: Boolean = true, selectedContactPubkey: String?)
{
    val state by store.stateFlow.collectAsState()
    Theme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(painterResource("background.jpg"), modifier = Modifier.fillMaxSize(), contentDescription = null, contentScale = ContentScale.Crop)
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        Messages(state.messages)
                    }
                    if (displayTextField)
                    {
                        SendMessage { text ->
                            Log.i(TAG, "selectedContactPubkey=" + selectedContactPubkey)
                            val friend_num = tox_friend_by_public_key(selectedContactPubkey)
                            tox_friend_send_message(friend_num, TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_NORMAL.value, text)
                            store.send(Action.SendMessage(UIMessage(myUser, timeMs = timestampMs(), text, toxpk = myUser.toxpk)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Theme(content: @Composable () -> Unit)
{
    MaterialTheme(
        colors = lightColors(
            surface = Color(ChatColors.SURFACE),
            background = Color(ChatColors.TOP_GRADIENT.last()),
        ),
    ) {
        ProvideTextStyle(LocalTextStyle.current.copy(letterSpacing = 0.sp)) {
            content()
        }
    }
}

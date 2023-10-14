import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.*
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_send_message
import com.zoffcc.applications.trifa.ToxVars.TOX_MESSAGE_TYPE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.briarproject.briar.desktop.contact.ContactItem
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

private const val TAG = "trifa.Chatapp"

val myUser = User("Me", picture = null, toxpk = null)
val store = CoroutineScope(SupervisorJob()).createStore()
val contactstore = CoroutineScope(SupervisorJob()).createContactStore()
val savepathstore = CoroutineScope(SupervisorJob()).createSavepathStore()
val toxdatastore = CoroutineScope(SupervisorJob()).createToxDataStore()

@Composable
fun ChatAppWithScaffold(displayTextField: Boolean = true, contactList: StateContacts) {
    Theme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        contactList.selectedContact?.let { Text(it.name) }
                    },
                    backgroundColor = MaterialTheme.colors.background,
                )
            }) {
            ChatApp(displayTextField = displayTextField)
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ChatApp(displayTextField: Boolean = true) {
    val state by store.stateFlow.collectAsState()
    Theme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painterResource("background.jpg"),
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(Modifier.weight(1f)) {
                        Messages(state.messages)
                    }
                    if (displayTextField) {
                        SendMessage { text ->
                            // !!!!! DEBUG DEBUG !!!!!
                            // !!!!! DEBUG DEBUG !!!!!
                            // !!!!! DEBUG DEBUG !!!!!
                            // send it to tox friend "0" (zero)
                            tox_friend_send_message(
                                0, TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_NORMAL.value, text
                            )
                            // !!!!! DEBUG DEBUG !!!!!
                            // !!!!! DEBUG DEBUG !!!!!
                            // !!!!! DEBUG DEBUG !!!!!
                            store.send(
                                Action.SendMessage(
                                    Message(myUser, timeMs = timestampMs(), text, toxpk = myUser.toxpk)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Theme(content: @Composable () -> Unit) {
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

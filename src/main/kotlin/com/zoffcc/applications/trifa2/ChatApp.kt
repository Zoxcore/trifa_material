import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_send_message
import com.zoffcc.applications.trifa.ToxVars.TOX_MESSAGE_TYPE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

private const val TAG = "trifa.Chatapp"

val myUser = User("Me", picture = null)
val friends = listOf(
    // User("Alex", picture = "stock1.jpg"),
    // User("Casey", picture = "stock2.jpg"),
    User("Sam", picture = "stock3.jpg")
    // User("Lora", picture = "stock4.jpg")
)
val friendMessages = listOf(
    "How's everybody doing today?",
    "I've been meaning to chat!",
    "When do we hang out next? 😋",
    "We really need to catch up!",
    "It's been too long!",
    "I can't\nbelieve\nit! 😱",
    "Did you see that ludicrous\ndisplay last night?",
    "We should meet up in person!",
    "How about a round of pinball?",
    "I'd love to:\n🍔 Eat something\n🎥 Watch a movie, maybe?\nWDYT?"
)
val store = CoroutineScope(SupervisorJob()).createStore()

@Composable
fun ChatAppWithScaffold(displayTextField: Boolean = true) {
    Theme {
        Scaffold(
            drawerContent = { Button(onClick = {}){} },
            topBar = {
                TopAppBar(
                    title = { Text("Tox Chat") },
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
                Image(painterResource("background.jpg"), null, contentScale = ContentScale.Crop)
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
                                    Message(myUser, timeMs = timestampMs(), text)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        while (!closing_application) {
            val thisFriend = friends.random()
            var thisMessage: String? = null
            try {
                thisMessage = MainActivity.incoming_messages_queue.poll()
            } catch (_: Exception) {
            }

            if (thisMessage != null) {
                store.send(
                    Action.SendMessage(
                        message = Message(
                            user = thisFriend,
                            timeMs = timestampMs(),
                            text = thisMessage
                        )
                    )
                )
            }
            delay(100)
        }
        Log.i(TAG, "endless loop ended");
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

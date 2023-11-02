import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.AVState
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.on_call_ended_actions
import com.zoffcc.applications.trifa.MainActivity.Companion.sent_message_to_db
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_send_message
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_send_message
import com.zoffcc.applications.trifa.MainActivity.Companion.toxav_call
import com.zoffcc.applications.trifa.MainActivity.Companion.toxav_call_control
import com.zoffcc.applications.trifa.StateContacts
import com.zoffcc.applications.trifa.StateGroups
import com.zoffcc.applications.trifa.TRIFAGlobals
import com.zoffcc.applications.trifa.ToxVars
import com.zoffcc.applications.trifa.ToxVars.TOX_MESSAGE_TYPE
import com.zoffcc.applications.trifa.createAVStateStore
import com.zoffcc.applications.trifa.createAVStateStoreCallState
import com.zoffcc.applications.trifa.createContactStore
import com.zoffcc.applications.trifa.createGroupPeerStore
import com.zoffcc.applications.trifa.createGroupStore
import com.zoffcc.applications.trifa.createSavepathStore
import com.zoffcc.applications.trifa.createToxDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.briarproject.briar.desktop.utils.ImagePicker.pickImageUsingDialog
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

private const val TAG = "trifa.Chatapp"
val myUser = User("Me", picture = null, toxpk = null)
val messagestore = CoroutineScope(SupervisorJob()).createMessageStore()
val groupmessagestore = CoroutineScope(SupervisorJob()).createGroupMessageStore()
val contactstore = CoroutineScope(SupervisorJob()).createContactStore()
val grouppeerstore = CoroutineScope(SupervisorJob()).createGroupPeerStore()
val groupstore = CoroutineScope(SupervisorJob()).createGroupStore()
val savepathstore = CoroutineScope(SupervisorJob()).createSavepathStore()
val toxdatastore = CoroutineScope(SupervisorJob()).createToxDataStore()
val avstatestore = CoroutineScope(SupervisorJob()).createAVStateStore()
val avstatestorecallstate = CoroutineScope(SupervisorJob()).createAVStateStoreCallState()

@Composable
fun ChatAppWithScaffold(focusRequester: FocusRequester, displayTextField: Boolean = true, contactList: StateContacts, ui_scale: Float)
{
    Theme {
        Scaffold(topBar = {
            TopAppBar(
                title = {
                    contactList.selectedContact?.let { Text(it.name) }
                },
                backgroundColor = MaterialTheme.colors.background,
                modifier = Modifier.height(40.dp),
                actions = {
                    IconButton(onClick = {/* Do Something*/ }) {
                        Icon(Icons.Filled.Call, null)
                    }
                    val curren_callstate by avstatestorecallstate.stateFlow.collectAsState()
                    IconButton(onClick = {
                        // video call button pressed
                        val friendpubkey = contactList.selectedContactPubkey
                        val friendnum = tox_friend_by_public_key(friendpubkey)

                        if (avstatestore.state.calling_state_get() == AVState.CALL_STATUS.CALL_STATUS_CALLING)
                        {
                            avstatestore.state.ffmpeg_devices_stop()
                            toxav_call_control(friendnum, ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value)
                            on_call_ended_actions()
                            println("toxav: ret 001")
                            return@IconButton
                        }

                        val call_res = toxav_call(friendnum, TRIFAGlobals.GLOBAL_AUDIO_BITRATE.toLong(), TRIFAGlobals.GLOBAL_VIDEO_BITRATE.toLong())
                        println("toxav call_res: $call_res")
                        if (call_res != 1)
                        {
                            on_call_ended_actions()
                            println("toxav: ret 002")
                            return@IconButton
                        }
                        avstatestore.state.calling_state_set(AVState.CALL_STATUS.CALL_STATUS_CALLING)
                        avstatestore.state.call_with_friend_pubkey_set(friendpubkey)
                        avstatestore.state.start_av_call()
                        println("toxav: set 003")
                    }) {
                        if (curren_callstate.call_state == AVState.CALL_STATUS.CALL_STATUS_CALLING)
                        {
                            Icon(imageVector = Icons.Filled.Videocam, contentDescription = "",
                                tint = Color.Red)
                        }
                        else
                        {
                            Icon(imageVector = Icons.Filled.Videocam, contentDescription = null)
                        }
                    }
                }
            )
        }) {
            ChatApp(focusRequester = focusRequester, displayTextField = displayTextField, contactList.selectedContactPubkey, ui_scale)
        }
    }
}

@Composable
fun GroupAppWithScaffold(focusRequester: FocusRequester, displayTextField: Boolean = true, groupList: StateGroups, ui_scale: Float)
{
    Theme {
        Scaffold(topBar = {
            TopAppBar(
                title = {
                    groupList.selectedGroup?.let { Text(it.name) }
                },
                backgroundColor = MaterialTheme.colors.background,
                modifier = Modifier.height(40.dp)
            )
        }) {
            GroupApp(focusRequester = focusRequester, displayTextField = displayTextField, groupList.selectedGroupId, ui_scale)
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ChatApp(focusRequester: FocusRequester, displayTextField: Boolean = true, selectedContactPubkey: String?, ui_scale: Float)
{
    val state by messagestore.stateFlow.collectAsState()
    Theme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(painterResource("background.jpg"), modifier = Modifier.fillMaxSize(), contentDescription = null, contentScale = ContentScale.Crop)
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        Messages(state.messages, ui_scale)
                    }
                    Row(modifier = Modifier.fillMaxWidth().height(MESAGE_INPUT_LINE_HEIGHT)) {
                        if (displayTextField)
                        {
                            Box(Modifier.weight(1f)) {
                                SendMessage(focusRequester) { text -> //
                                    // Log.i(TAG, "selectedContactPubkey=" + selectedContactPubkey)
                                    val friend_num = tox_friend_by_public_key(selectedContactPubkey)
                                    val timestamp = System.currentTimeMillis()
                                    val res = tox_friend_send_message(friend_num, TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_NORMAL.value, text)
                                    if (res >= 0)
                                    {
                                        val msg_id_db = sent_message_to_db(selectedContactPubkey, timestamp, text)
                                        messagestore.send(MessageAction.SendMessage(UIMessage(msgDatabaseId = msg_id_db, user = myUser, timeMs = timestamp, text = text, toxpk = myUser.toxpk, trifaMsgType = TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, filename_fullpath = null)))
                                    }
                                }
                            }
                        }
                        Box(Modifier.width(40.dp).height(MESAGE_INPUT_LINE_HEIGHT).
                        background(MaterialTheme.colors.background)) {
                            // val LocalWindowScope = staticCompositionLocalOf<FrameWindowScope?> { null }
                            // val windowScope = LocalWindowScope.current!!
                            IconButton(
                                icon = Icons.Filled.AttachFile,
                                iconTint = Color.DarkGray,
                                iconSize = 25.dp,
                                modifier = Modifier.width(40.dp).align(Alignment.Center),
                                contentDescription = "send File",
                                onClick = {
                                    pickImageUsingDialog(onCloseRequest = {
                                        Log.i(TAG, "pickImageUsingDialog:result=$it")
                                    })
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GroupApp(focusRequester: FocusRequester, displayTextField: Boolean = true, selectedGroupId: String?, ui_scale: Float)
{
    val state by groupmessagestore.stateFlow.collectAsState()
    Theme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(painterResource("background.jpg"), modifier = Modifier.fillMaxSize(), contentDescription = null, contentScale = ContentScale.Crop)
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        GroupMessages(state.groupmessages, ui_scale = ui_scale)
                    }
                    if (displayTextField)
                    {
                        GroupSendMessage (focusRequester) { text ->
                            val timestamp = System.currentTimeMillis()
                            val groupnum: Long = tox_group_by_groupid__wrapper(selectedGroupId!!)
                            val message_id: Long = tox_group_send_message(groupnum, ToxVars.TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_NORMAL.value, text)
                            if (message_id >= 0)
                            {
                                MainActivity.sent_groupmessage_to_db(groupid = selectedGroupId, message_timestamp =  timestamp, group_message = text, message_id = message_id )
                                groupmessagestore.send(GroupMessageAction.SendGroupMessage(UIGroupMessage(myUser, timeMs = timestamp, text, toxpk = myUser.toxpk, groupId = selectedGroupId!!.lowercase(),
                                    trifaMsgType = TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, filename_fullpath = null)))
                            }
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
            surface = Color(ChatColorsConfig.SURFACE),
            background = Color(ChatColorsConfig.TOP_GRADIENT.last()),
        ),
    ) {
        ProvideTextStyle(LocalTextStyle.current.copy(letterSpacing = 0.sp)) {
            content()
        }
    }
}

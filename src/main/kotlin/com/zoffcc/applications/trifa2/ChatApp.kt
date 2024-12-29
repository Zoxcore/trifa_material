@file:OptIn(ExperimentalComposeUiApi::class)
@file:Suppress("FunctionName", "SpellCheckingInspection", "LocalVariableName", "ConvertToStringTemplate")

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.AVState
import com.zoffcc.applications.trifa.HelperGeneric.send_message_onclick
import com.zoffcc.applications.trifa.HelperGroup
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper
import com.zoffcc.applications.trifa.HelperMessage.take_screen_shot_with_selection
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.add_ngc_outgoing_file
import com.zoffcc.applications.trifa.MainActivity.Companion.add_outgoing_file
import com.zoffcc.applications.trifa.MainActivity.Companion.on_call_ended_actions
import com.zoffcc.applications.trifa.MainActivity.Companion.set_toxav_video_sending_quality
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_self_get_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_send_message
import com.zoffcc.applications.trifa.MainActivity.Companion.toxav_call
import com.zoffcc.applications.trifa.MainActivity.Companion.toxav_call_control
import com.zoffcc.applications.trifa.StateContacts
import com.zoffcc.applications.trifa.StateGroups
import com.zoffcc.applications.trifa.TRIFAGlobals
import com.zoffcc.applications.trifa.ToxVars
import com.zoffcc.applications.trifa.createAVStateStore
import com.zoffcc.applications.trifa.createAVStateStoreCallState
import com.zoffcc.applications.trifa.createAVStateStoreVideoCaptureFpsState
import com.zoffcc.applications.trifa.createAVStateStoreVideoPlayFpsState
import com.zoffcc.applications.trifa.createContactStore
import com.zoffcc.applications.trifa.createFriendSettingsStore
import com.zoffcc.applications.trifa.createGlobalStore
import com.zoffcc.applications.trifa.createGroupPeerStore
import com.zoffcc.applications.trifa.createGroupSettingsStore
import com.zoffcc.applications.trifa.createGroupStore
import com.zoffcc.applications.trifa.createGroupstoreUnreadMessages
import com.zoffcc.applications.trifa.createSavepathStore
import com.zoffcc.applications.trifa.createToxDataStore
import com.zoffcc.applications.trifa.createUnreadMessages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.ui.Tooltip
import org.briarproject.briar.desktop.utils.FilePicker.pickFileUsingDialog
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.File
import java.net.URI
import java.nio.file.LinkOption
import kotlin.io.path.exists
import kotlin.io.path.toPath

private const val TAG = "trifa.Chatapp"
val myUser = User("Me", picture = null, toxpk = "AAA")
val messagestore = CoroutineScope(SupervisorJob()).createMessageStore()
val globalstore = CoroutineScope(SupervisorJob()).createGlobalStore()
val globalfrndstoreunreadmsgs = CoroutineScope(SupervisorJob()).createUnreadMessages()
val globalgrpstoreunreadmsgs = CoroutineScope(SupervisorJob()).createGroupstoreUnreadMessages()
val groupmessagestore = CoroutineScope(SupervisorJob()).createGroupMessageStore()
val contactstore = CoroutineScope(SupervisorJob()).createContactStore()
val grouppeerstore = CoroutineScope(SupervisorJob()).createGroupPeerStore()
val groupsettingsstore = CoroutineScope(SupervisorJob()).createGroupSettingsStore()
val friendsettingsstore = CoroutineScope(SupervisorJob()).createFriendSettingsStore()
val groupstore = CoroutineScope(SupervisorJob()).createGroupStore()
val savepathstore = CoroutineScope(SupervisorJob()).createSavepathStore()
val toxdatastore = CoroutineScope(SupervisorJob()).createToxDataStore()
val avstatestore = CoroutineScope(SupervisorJob()).createAVStateStore()
val avstatestorecallstate = CoroutineScope(SupervisorJob()).createAVStateStoreCallState()
val avstatestorevcapfpsstate = CoroutineScope(SupervisorJob()).createAVStateStoreVideoCaptureFpsState()
val avstatestorevplayfpsstate = CoroutineScope(SupervisorJob()).createAVStateStoreVideoPlayFpsState()

@OptIn(DelicateCoroutinesApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChatAppWithScaffold(focusRequester: FocusRequester, displayTextField: Boolean = true, contactList: StateContacts, ui_scale: Float)
{
    Theme {
        Column(modifier = Modifier.fillMaxWidth())
        {
            val focusRequester2 = remember { FocusRequester() }
            if (contactList.messageFilterActive)
            {
                var message_filter_str by remember { mutableStateOf(contactList.messageFilterString) }
                TextField(enabled = true, singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester2),
                    textStyle = TextStyle(fontSize = 16.sp),
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR)),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrect = false,
                    ),
                    placeholder = {
                        Text("type search text ...", fontSize = 16.sp)
                    },
                    value = message_filter_str,
                    onValueChange = {
                        message_filter_str = it
                        contactstore.messagefilterString(message_filter_str)
                    })
                LaunchedEffect(contactList.messageFilterActive) {
                    // HINT: focus on the search input field when search input field is opened
                    focusRequester2.requestFocus()
                    // Log.i(TAG, "FFFFFF2222222222222: focus on the search input field when search input field is opened")
                }
            }
            else
            {
                LaunchedEffect(contactList.messageFilterActive) {
                    // HINT: focus on the message input field, when search input field is closed
                    focusRequester.requestFocus()
                    // Log.i(TAG, "FFFFFF3333333333333: focus on the message input field, when search input field is closed")
                }
            }
            Scaffold(topBar = {
                TopAppBar(
                    title = {
                        contactList.selectedContact?.let { Text(it.name) }
                    },
                    backgroundColor = MaterialTheme.colors.background,
                    modifier = Modifier.height(40.dp),
                    actions = {
                        Tooltip(text = "Filter Messages") {
                            IconButton(onClick = {
                                contactstore.messagefilterString("")
                                contactstore.messagefilterActive(!contactstore.state.messageFilterActive) }) {
                                Icon(Icons.Filled.Search, null)
                            }
                        }
                        Tooltip(text = "Friend Settings and Info") {
                            IconButton(onClick = { friendsettingsstore.visible(true) }) {
                                Icon(Icons.Filled.Settings, null)
                            }
                        }
                        Tooltip(text = "use the Video Call Button") {
                            IconButton(onClick = {/* TODO: */ }) {
                                Icon(Icons.Filled.Call, null)
                            }
                        }
                        val current_callstate by avstatestorecallstate.stateFlow.collectAsState()
                        Tooltip(text = "start an Audio or Video Call") {
                            IconButton(onClick = {
                                // video call button pressed
                                val friendpubkey = contactList.selectedContactPubkey
                                if (avstatestore.state.calling_state_get() == AVState.CALL_STATUS.CALL_STATUS_INCOMING)
                                {
                                    println("toxav: we have an unanswered incoming call. ret 007")
                                    return@IconButton
                                }
                                val friendnum = tox_friend_by_public_key(friendpubkey)
                                if (avstatestore.state.calling_state_get() == AVState.CALL_STATUS.CALL_STATUS_CALLING)
                                {
                                    Log.i(com.zoffcc.applications.trifa.TAG, "ffmpeg_devices_stop:007")
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
                                GlobalScope.launch {
                                    delay(1000)
                                    set_toxav_video_sending_quality(MainActivity.PREF__video_bitrate_mode)
                                    println("toxav: set 003")
                                }
                            }) {
                                if (current_callstate.call_state == AVState.CALL_STATUS.CALL_STATUS_CALLING)
                                {
                                    Icon(imageVector = Icons.Filled.Videocam, contentDescription = "",
                                        tint = Color.Red)
                                } else
                                {
                                    Icon(imageVector = Icons.Filled.Videocam, contentDescription = null)
                                }
                            }
                        }
                    }
                )
            }) {
                ChatApp(focusRequester = focusRequester, displayTextField = displayTextField, contactList.selectedContactPubkey, ui_scale)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupAppWithScaffold(focusRequester: FocusRequester, displayTextField: Boolean = true, groupList: StateGroups, ui_scale: Float)
{
    Theme {
        Column(modifier = Modifier.fillMaxWidth())
        {
            val focusRequester2 = remember { FocusRequester() }
            if (groupList.groupmessageFilterActive)
            {
                var message_filter_str by remember { mutableStateOf(groupList.groupmessageFilterString) }
                TextField(enabled = true, singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester2),
                    textStyle = TextStyle(fontSize = 16.sp),
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR)),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrect = false,
                    ),
                    placeholder = {
                        Text("type search text ...", fontSize = 16.sp)
                    },
                    value = message_filter_str,
                    onValueChange = {
                        message_filter_str = it
                        groupstore.groupmessagefilterString(message_filter_str)
                    })
                LaunchedEffect(groupList.groupmessageFilterActive) {
                    // HINT: focus on the search input field when search input field is opened
                    focusRequester2.requestFocus()
                    // Log.i(TAG, "FFFFgg2222222222222: focus on the search input field when search input field is opened")
                }
            }
            else
            {
                LaunchedEffect(groupList.groupmessageFilterActive) {
                    // HINT: focus on the message input field, when search input field is closed
                    focusRequester.requestFocus()
                    // Log.i(TAG, "FFFFgg3333333333333: focus on the group message input field, when search input field is closed")
                }
            }
            Scaffold(topBar = {
                TopAppBar(
                    title = {
                        groupList.selectedGroup?.let { Text(it.name) }
                    },
                    actions = {
                        Tooltip(text = "Filter Messages") {
                            IconButton(onClick = {
                                groupstore.groupmessagefilterString("")
                                groupstore.groupmessagefilterActive(!groupstore.state.groupmessageFilterActive) }) {
                                Icon(Icons.Filled.Search, null)
                            }
                        }
                        Tooltip(text = "Group Settings and Info") {
                            IconButton(onClick = { groupsettingsstore.visible(true) }) {
                                Icon(Icons.Filled.Settings, null)
                            }
                        }
                    },
                    backgroundColor = MaterialTheme.colors.background,
                    modifier = Modifier.height(40.dp)
                )
            }) {
                GroupApp(focusRequester = focusRequester, displayTextField = displayTextField, groupList.selectedGroupId, ui_scale)
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChatApp(focusRequester: FocusRequester, displayTextField: Boolean = true, selectedContactPubkey: String?, ui_scale: Float)
{
    Theme {
        Surface {
            Box(modifier = Modifier.fillMaxSize().randomDebugBorder()) {
                Image(painterResource("background.jpg"), modifier = Modifier.fillMaxSize(), contentDescription = null, contentScale = ContentScale.Crop)
                Column(modifier = Modifier.fillMaxSize()) {
                    var isDragging by remember { mutableStateOf(false) }
                    val dragAndDropTarget = remember(selectedContactPubkey) {
                        object: DragAndDropTarget
                        {
                            override fun onExited(event: DragAndDropEvent)
                            {
                                // println("======> onExited:" + event)
                                isDragging = false
                            }

                            override fun onEntered(event: DragAndDropEvent)
                            {
                                isDragging = true
                                // println("======> onEntered:" + event)
                            }

                            override fun onChanged(event: DragAndDropEvent)
                            {
                                // println("======> onChanged:" + event)
                            }

                            override fun onStarted(event: DragAndDropEvent) {
                                // println("======> onStarted:" + event + " " + event.dragData())
                            }
                            override fun onEnded(event: DragAndDropEvent) {
                                isDragging = false
                                // println("======> onEnded:" + event)
                            }
                            override fun onDrop(event: DragAndDropEvent): Boolean {
                                isDragging = false
                                // println("======> onDrop:" + event + " " + event.dragData())
                                if (event.dragData() is DragData.FilesList)
                                {
                                    // println("======> onDrop:" + event)
                                    val newFiles = (event.dragData() as DragData.FilesList).readFiles().mapNotNull { it1: String ->
                                        URI(it1).toPath().takeIf { it.exists(LinkOption.NOFOLLOW_LINKS) }
                                    }
                                    newFiles.forEach {
                                        if (it.toAbsolutePath().toString().isNotEmpty()) {
                                            // Log.i(TAG," " + it.toAbsolutePath().parent.toString() + " "
                                            //        + it.toAbsolutePath().fileName.toString() + " " + selectedContactPubkey)
                                            add_outgoing_file(it.toAbsolutePath().parent.toString(),
                                                it.toAbsolutePath().fileName.toString(),
                                                selectedContactPubkey)
                                        }
                                    }
                                }
                                else if (event.dragData() is DragData.Image)
                                {
                                    // println("======> onDrop:iiiii " + event + " " + (event.dragData() as DragData.Image).toString())
                                }
                                else if (event.dragData() is DragData.Text)
                                {
                                    // println("======> onDrop:ttttt " + event + " " + (event.dragData() as DragData.Text).readText())
                                }
                                else
                                {
                                    // println("======> onDrop:uuuuu " + event + " " + event.dragData())
                                }
                                isDragging = false
                                return true
                            }
                        }
                    }


                    Box(Modifier.weight(1f)
                        .background(color = if (isDragging) Color.LightGray else Color.Transparent)
                        .dragAndDropTarget(
                            shouldStartDragAndDrop = { true },
                            target = dragAndDropTarget
                        )) {
                        if (isDragging)
                        {
                            val scope = rememberCoroutineScope()
                            Column(modifier = Modifier.fillMaxSize()
                                .padding(all = 10.dp)
                                .dashedBorder(color = if (isDragging) DragAndDropColors.active else Color.Transparent,
                                    strokeWidth = if (isDragging) 5.dp else 0.dp,
                                    cornerRadiusDp = if (isDragging) 25.dp else 0.dp)) {
                                Spacer(modifier = Modifier.weight(0.6f))
                                DragAndDropDescription(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    color = DragAndDropColors.active
                                )
                                Spacer(modifier = Modifier.weight(0.6f))
                            }
                        }
                        else
                        {
                            Messages(ui_scale, selectedContactPubkey)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (displayTextField)
                        {
                            Box(Modifier.weight(1f)) {
                                SendMessage(focusRequester, selectedContactPubkey) { text -> //
                                    Log.i(TAG, "selectedContactPubkey=" + selectedContactPubkey)
                                    if (selectedContactPubkey != null)
                                    {
                                        if (!send_message_onclick(text, selectedContactPubkey))
                                        {
                                            SnackBarToast("Sending Message failed")
                                        }
                                    }
                                }
                            }
                        }
                        Box(Modifier.width(80.dp).height(MESSAGE_INPUT_LINE_HEIGHT).
                        background(MaterialTheme.colors.background)) {
                            Row(modifier = Modifier.width(80.dp)){
                                IconButton(
                                    icon = Icons.Filled.AttachFile,
                                    iconTint = Color.DarkGray,
                                    iconSize = 25.dp,
                                    modifier = Modifier.width(40.dp),
                                    contentDescription = "send File",
                                    onClick = {
                                        pickFileUsingDialog(onCloseRequest = { dir, file ->
                                            Log.i(TAG, "pickFileUsingDialog:result=" + dir + "::" + File.separator + "::" + file )
                                            add_outgoing_file(dir, file, selectedContactPubkey)
                                        })
                                    }
                                )
                                IconButton(
                                    icon = Icons.Filled.Screenshot,
                                    iconTint = Color.DarkGray,
                                    iconSize = 25.dp,
                                    modifier = Modifier.width(40.dp),
                                    contentDescription = "take Screenshot",
                                    onClick = {
                                        take_screen_shot_with_selection(selectedContactPubkey)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun GroupApp(focusRequester: FocusRequester, displayTextField: Boolean = true, selectedGroupId: String?, ui_scale: Float)
{
    Theme {
        Surface {
            Box(modifier = Modifier.fillMaxSize().randomDebugBorder()) {
                Image(painterResource("background.jpg"), modifier = Modifier.fillMaxSize(),
                    contentDescription = null, contentScale = ContentScale.Crop)
                Column(modifier = Modifier.fillMaxSize()) {
                    var isDragging by remember { mutableStateOf(false) }

                    val dragAndDropTarget = remember(selectedGroupId) {
                        object: DragAndDropTarget
                        {
                            override fun onExited(event: DragAndDropEvent)
                            {
                                // println("======> onExited:" + event)
                                isDragging = false
                            }

                            override fun onEntered(event: DragAndDropEvent)
                            {
                                isDragging = true
                                // println("======> onEntered:" + event)
                            }

                            override fun onChanged(event: DragAndDropEvent)
                            {
                                // println("======> onChanged:" + event)
                            }

                            override fun onStarted(event: DragAndDropEvent) {
                                // println("======> onStarted:" + event + " " + event.dragData())
                            }
                            override fun onEnded(event: DragAndDropEvent) {
                                isDragging = false
                                // println("======> onEnded:" + event)
                            }
                            override fun onDrop(event: DragAndDropEvent): Boolean {
                                isDragging = false
                                if (event.dragData() is DragData.FilesList)
                                {
                                    val newFiles = (event.dragData() as DragData.FilesList).readFiles().mapNotNull { it1: String ->
                                        URI(it1).toPath().takeIf { it.exists(LinkOption.NOFOLLOW_LINKS) }
                                    }
                                    newFiles.forEach {
                                        // Log.i(TAG, "dropped file: " + it.toAbsolutePath() + " " + it.parent.normalize().name + " " + it.fileName.name)
                                        if (it.toAbsolutePath().toString().isNotEmpty()) {
                                            // Log.i(TAG," " + it.toAbsolutePath().parent.toString() + " "
                                            //      + it.toAbsolutePath().fileName.toString() + " " + selectedGroupId)
                                            add_ngc_outgoing_file(it.toAbsolutePath().parent.toString(),
                                                it.toAbsolutePath().fileName.toString(), selectedGroupId)
                                        }
                                    }
                                }
                                else if (event.dragData() is DragData.Image)
                                {
                                    // println("======> onDrop:iiiii " + event + " " + (event.dragData() as DragData.Image).toString())
                                }
                                else if (event.dragData() is DragData.Text)
                                {
                                    // println("======> onDrop:ttttt " + event + " " + (event.dragData() as DragData.Text).readText())
                                }
                                else
                                {
                                    // println("======> onDrop:uuuuu " + event + " " + event.dragData())
                                }
                                isDragging = false
                                return true
                            }
                        }
                    }


                    Box(Modifier.weight(1f)
                        .background(color = if (isDragging) Color.LightGray else Color.Transparent)
                        .dragAndDropTarget(
                            shouldStartDragAndDrop = { true },
                            target = dragAndDropTarget
                        )) {
                        if (isDragging)
                        {
                            Column(modifier = Modifier.fillMaxSize()
                                .padding(all = 10.dp)
                                .dashedBorder(color = if (isDragging) DragAndDropColors.active else Color.Transparent,
                                    strokeWidth = if (isDragging) 5.dp else 0.dp,
                                    cornerRadiusDp = if (isDragging) 25.dp else 0.dp)) {
                                Spacer(modifier = Modifier.weight(0.6f))
                                DragAndDropDescription(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    color = DragAndDropColors.active
                                )
                                Spacer(modifier = Modifier.weight(0.6f))
                            }
                        }
                        else
                        {
                            GroupMessages(ui_scale = ui_scale, selectedGroupId)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (displayTextField)
                        {
                            Box(Modifier.weight(1f)) {
                                GroupSendMessage (focusRequester, selectedGroupId) { text ->
                                    Log.i(TAG, "selectedGroupId=" + selectedGroupId)
                                    if (selectedGroupId != null)
                                    {
                                        val timestamp = System.currentTimeMillis()
                                        val groupnum: Long = tox_group_by_groupid__wrapper(selectedGroupId!!)
                                        val my_group_peerpk = tox_group_self_get_public_key(groupnum)
                                        val message_id: Long = tox_group_send_message(groupnum, ToxVars.TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_NORMAL.value, text)
                                        if (message_id >= 0)
                                        {
                                            var peer_role = -1
                                            try
                                            {
                                                val self_peer_role = MainActivity.tox_group_self_get_role(groupnum)
                                                if (self_peer_role >= 0)
                                                {
                                                    peer_role = self_peer_role
                                                }
                                            } catch (_: Exception)
                                            {
                                            }
                                            val message_id_hex = HelperGroup.fourbytes_of_long_to_hex(message_id)
                                            val db_msgid = MainActivity.sent_groupmessage_to_db(groupid = selectedGroupId, message_timestamp = timestamp, group_message = text, message_id = message_id, was_synced = false)
                                            groupmessagestore.send(GroupMessageAction.SendGroupMessage(
                                                UIGroupMessage(
                                                    was_synced = false,
                                                    is_private_msg = 0,
                                                    sentTimeMs = timestamp,
                                                    rcvdTimeMs = timestamp,
                                                    syncdTimeMs = timestamp,
                                                    peer_role = peer_role,
                                                    msg_id_hash = "",
                                                    message_id_tox = message_id_hex, msgDatabaseId = db_msgid,
                                                    user = myUser, timeMs = timestamp, text = text,
                                                    toxpk = my_group_peerpk,
                                                    groupId = selectedGroupId!!.lowercase(),
                                                    trifaMsgType = TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value,
                                                    filename_fullpath = null)))
                                        } else
                                        {
                                            SnackBarToast("Sending Group Message failed")
                                        }
                                    }
                                }
                            }
                        }
                        Box(Modifier.width(40.dp).height(MESSAGE_INPUT_LINE_HEIGHT).
                        background(MaterialTheme.colors.background)) {
                            Row(modifier = Modifier.width(40.dp)){
                                IconButton(
                                    icon = Icons.Filled.AttachFile,
                                    iconTint = Color.DarkGray,
                                    iconSize = 25.dp,
                                    modifier = Modifier.width(40.dp),
                                    contentDescription = "send File",
                                    onClick = {
                                        pickFileUsingDialog(onCloseRequest = { dir, file ->
                                            Log.i(TAG, "pickFileUsingDialog:result=" + dir + "::" + File.separator + "::" + file )
                                            add_ngc_outgoing_file(dir, file, selectedGroupId)
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
}

@Composable
fun Theme(content: @Composable () -> Unit)
{
    var Typography: Typography? = null
    try
    {
        Typography = Typography(
            defaultFontFamily = DefaultFont!!
        )
    }
    catch(_: Exception)
    {
        Typography = MaterialTheme.typography
    }

    // colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
    // TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR))
    MaterialTheme(
        typography = Typography!!,
        colors = lightColors(
            surface = Color(ChatColorsConfig.LIGHT__FGCOLOR),
            background = Color(ChatColorsConfig.LIGHT__BGCOLOR),
        ),
    ) {
        ProvideTextStyle(LocalTextStyle.current.copy(letterSpacing = 0.sp)) {
            content()
        }
    }
}

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
import com.zoffcc.applications.ffmpegav.AVActivity
import com.zoffcc.applications.ffmpegav.AVActivity.ffmpegav_close_video_in_device
import com.zoffcc.applications.ffmpegav.AVActivity.ffmpegav_get_video_in_devices
import com.zoffcc.applications.ffmpegav.AVActivity.ffmpegav_init
import com.zoffcc.applications.ffmpegav.AVActivity.ffmpegav_set_video_capture_callback
import com.zoffcc.applications.ffmpegav.AVActivity.ffmpegav_stop_video_in_capture
import com.zoffcc.applications.trifa.AVState
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.sent_message_to_db
import com.zoffcc.applications.trifa.MainActivity.Companion.set_JNI_video_buffer2
import com.zoffcc.applications.trifa.MainActivity.Companion.set_av_call_status
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_send_message
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_send_message
import com.zoffcc.applications.trifa.MainActivity.Companion.toxav_call
import com.zoffcc.applications.trifa.MainActivity.Companion.toxav_call_control
import com.zoffcc.applications.trifa.MainActivity.Companion.toxav_video_send_frame_age
import com.zoffcc.applications.trifa.StateContacts
import com.zoffcc.applications.trifa.StateGroups
import com.zoffcc.applications.trifa.TRIFAGlobals
import com.zoffcc.applications.trifa.ToxVars
import com.zoffcc.applications.trifa.ToxVars.TOX_MESSAGE_TYPE
import com.zoffcc.applications.trifa.VideoInFrame
import com.zoffcc.applications.trifa.VideoOutFrame
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
import java.nio.ByteBuffer

private const val TAG = "trifa.Chatapp"
val myUser = User("Me", picture = null, toxpk = null)
val messagestore = CoroutineScope(SupervisorJob()).createMessageStore()
val groupmessagestore = CoroutineScope(SupervisorJob()).createGroupMessageStore()
val contactstore = CoroutineScope(SupervisorJob()).createContactStore()
val grouppeerstore = CoroutineScope(SupervisorJob()).createGroupPeerStore()
val groupstore = CoroutineScope(SupervisorJob()).createGroupStore()
val savepathstore = CoroutineScope(SupervisorJob()).createSavepathStore()
val toxdatastore = CoroutineScope(SupervisorJob()).createToxDataStore()

@Composable
fun ChatAppWithScaffold(av_state: AVState, focusRequester: FocusRequester, displayTextField: Boolean = true, contactList: StateContacts, ui_scale: Float)
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
                    IconButton(onClick = {
                        // video call button pressed
                        val friendnum = tox_friend_by_public_key(contactList.selectedContactPubkey)
                        // set_av_call_status(1)

                        if (av_state.calling_state == AVState.CALL_STATUS.CALL_CALLING)
                        {
                            ffmpegav_stop_video_in_capture()
                            ffmpegav_close_video_in_device()
                            toxav_call_control(friendnum, ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value)
                            av_state.calling_state = AVState.CALL_STATUS.CALL_NONE
                            VideoOutFrame.clear_video_out_frame()
                            VideoInFrame.clear_video_in_frame()
                            return@IconButton
                        }

                        val call_res = toxav_call(friendnum,16, 2000)
                        println("toxav call_res: $call_res")
                        if (call_res == 0)
                        {
                            av_state.calling_state = AVState.CALL_STATUS.CALL_OUTGOING
                            av_state.calling_state = AVState.CALL_STATUS.CALL_CALLING
                        }
                        else
                        {
                            av_state.calling_state = AVState.CALL_STATUS.CALL_NONE
                            VideoOutFrame.clear_video_out_frame()
                            VideoInFrame.clear_video_in_frame()
                            return@IconButton
                        }
                        println("________________________")
                        if (!av_state.ffmpeg_init_done)
                        {
                            val res = ffmpegav_init()
                            println("ffmpeg init: $res")
                            av_state.ffmpeg_init_done = true
                        }
                        val video_in_device = av_state.video_in_device
                        val video_in_source = av_state.video_in_source
                        val capture_video_width = 1280
                        val capture_video_height = 720
                        var video_buffer_2 : ByteBuffer? = null

                        if ((video_in_device != null) && (video_in_device != ""))
                        {
                            if ((video_in_source != null) && (video_in_source != ""))
                            {
                                println("ffmpeg video in device: " + video_in_device + " " + video_in_source)
                                val res_vd = AVActivity.ffmpegav_open_video_in_device(
                                    video_in_device,
                                    video_in_source,
                                    capture_video_width,
                                    capture_video_height,
                                    20)
                                println("ffmpeg open video capture device: $res_vd")
                            }
                        }

                        val frame_width_px1 = capture_video_width
                        val frame_height_px1 = capture_video_height
                        val buffer_size_in_bytes1 = (frame_width_px1 * frame_height_px1 * 3) / 2
                        val video_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes1)
                        AVActivity.ffmpegav_set_JNI_video_buffer(video_buffer_1, frame_width_px1, frame_height_px1)
                        val frame_width_px2 = capture_video_width
                        val frame_height_px2 = capture_video_height
                        val y_size = frame_width_px2 * frame_height_px2
                        val u_size = (frame_width_px2 * frame_height_px2 / 4)
                        val v_size = (frame_width_px2 * frame_height_px2 / 4)
                        val video_buffer_2_y = ByteBuffer.allocateDirect(y_size)
                        val video_buffer_2_u = ByteBuffer.allocateDirect(u_size)
                        val video_buffer_2_v = ByteBuffer.allocateDirect(v_size)
                        AVActivity.ffmpegav_set_JNI_video_buffer2(video_buffer_2_y, video_buffer_2_u, video_buffer_2_v,
                            frame_width_px2, frame_height_px2)

                        ffmpegav_set_video_capture_callback(object : AVActivity.video_capture_callback
                        {
                            override fun onSuccess(width: Long, height: Long, pts: Long)
                            {
                                // com.zoffcc.applications.ffmpegav.Log.i(TAG, "ffmpeg open video capture onSuccess: $width $height $pts")
                                val frame_width_px: Int = width.toInt()
                                val frame_height_px: Int = height.toInt()
                                val buffer_size_in_bytes3 = (frame_width_px * frame_height_px * 1.5f).toInt()
                                if ((video_buffer_2 == null) || (frame_width_px != frame_width_px2) || (frame_height_px != frame_height_px2))
                                {
                                    video_buffer_2 = ByteBuffer.allocateDirect(buffer_size_in_bytes3)
                                    set_JNI_video_buffer2(video_buffer_2, frame_width_px, frame_height_px)
                                    VideoOutFrame.setup_video_out_resolution(frame_width_px, frame_height_px, buffer_size_in_bytes3)
                                }
                                video_buffer_2!!.rewind()
                                video_buffer_2_y.rewind()
                                video_buffer_2_u.rewind()
                                video_buffer_2_v.rewind()
                                video_buffer_2!!.put(video_buffer_2_y)
                                video_buffer_2!!.put(video_buffer_2_u)
                                video_buffer_2!!.put(video_buffer_2_v)
                                toxav_video_send_frame_age(friendnum = friendnum,
                                    frame_width_px = width.toInt(),
                                    frame_height_px = height.toInt(),
                                    age_ms = 0)

                                video_buffer_2!!.rewind()
                                VideoOutFrame.new_video_out_frame(video_buffer_2, frame_width_px, frame_height_px)
                            }
                            override fun onError()
                            {
                            }
                        })
                        AVActivity.ffmpegav_start_video_in_capture()
                    }) {
                        Icon(Icons.Filled.Videocam, null)
                    }
                }
            )
        }) {
            ChatApp(focusRequester = focusRequester, displayTextField = displayTextField, contactList.selectedContactPubkey, ui_scale)
        }
    }
}

@Composable
fun GroupAppWithScaffold(av_state: AVState, focusRequester: FocusRequester, displayTextField: Boolean = true, groupList: StateGroups, ui_scale: Float)
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

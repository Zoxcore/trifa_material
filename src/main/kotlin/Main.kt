import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.zoffcc.applications.ffmpegav.AVActivity
import com.zoffcc.applications.trifa.AudioBar
import com.zoffcc.applications.trifa.AudioBar.audio_in_bar
import com.zoffcc.applications.trifa.AudioBar.audio_out_bar
import com.zoffcc.applications.trifa.CustomSemaphore
import com.zoffcc.applications.trifa.HelperGeneric.PubkeyShort
import com.zoffcc.applications.trifa.JPictureBox
import com.zoffcc.applications.trifa.JPictureBoxOut
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity.Companion.main_init
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_name
import com.zoffcc.applications.trifa.PrefsSettings
import com.zoffcc.applications.trifa.RandomNameGenerator
import com.zoffcc.applications.trifa.SingleComponentAspectRatioKeeperLayout
import com.zoffcc.applications.trifa.TRIFAGlobals
import com.zoffcc.applications.trifa.TrifaToxService
import com.zoffcc.applications.trifa.TrifaToxService.Companion.clear_grouppeers
import com.zoffcc.applications.trifa.TrifaToxService.Companion.load_grouppeers
import com.zoffcc.applications.trifa.TrifaToxService.Companion.orma
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.briarproject.briar.desktop.SettingDetails
import org.briarproject.briar.desktop.contact.ContactList
import org.briarproject.briar.desktop.contact.GroupList
import org.briarproject.briar.desktop.contact.GroupPeerList
import org.briarproject.briar.desktop.navigation.BriarSidebar
import org.briarproject.briar.desktop.ui.AboutScreen
import org.briarproject.briar.desktop.ui.ExplainerChat
import org.briarproject.briar.desktop.ui.ExplainerGroup
import org.briarproject.briar.desktop.ui.HorizontalDivider
import org.briarproject.briar.desktop.ui.UiMode
import org.briarproject.briar.desktop.ui.UiPlaceholder
import org.briarproject.briar.desktop.ui.VerticalDivider
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import java.awt.Component
import java.awt.LayoutManager
import java.awt.Toolkit
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.prefs.Preferences
import javax.swing.JButton
import javax.swing.JPanel
import kotlin.collections.ArrayList

private const val TAG = "trifa.Main.kt"
var tox_running_state_wrapper = "start"
var start_button_text_wrapper = "stopped"
var online_button_text_wrapper = "offline"
var online_button_color_wrapper = Color.White.toArgb()
var closing_application = false
val global_prefs: Preferences = Preferences.userNodeForPackage(com.zoffcc.applications.trifa.PrefsSettings::class.java)
val UISCALE_ITEM_HEIGHT = 30.dp
val CONTACTITEM_HEIGHT = 50.dp
val GROUPITEM_HEIGHT = 50.dp
val GROUP_PEER_HEIGHT = 33.dp
val SETTINGS_HEADER_SIZE = 56.dp
val CONTACT_COLUMN_WIDTH = 230.dp
val GROUPS_COLUMN_WIDTH = 200.dp
val GROUP_PEER_COLUMN_WIDTH = 180.dp
val MESAGE_INPUT_LINE_HEIGHT = 55.dp
val MAIN_TOP_TAB_HEIGHT = 160.dp
val IMAGE_PREVIEW_SIZE = 70f
val AVATAR_SIZE = 40f
val MAX_AVATAR_SIZE = 70f
val SPACE_AFTER_LAST_MESSAGE = 2.dp
val SPACE_BEFORE_FIRST_MESSAGE = 10.dp
val CAPTURE_VIDEO_WIDTH = 640 // 1280
val CAPTURE_VIDEO_HEIGHT = 480 // 720
val CAPTURE_VIDEO_FPS = 20
val VIDEO_IN_BOX_WIDTH_SMALL = 80.dp
val VIDEO_IN_BOX_HEIGHT_SMALL = 80.dp
val VIDEO_IN_BOX_WIDTH_FRACTION_SMALL = 0.5f
val VIDEO_IN_BOX_WIDTH_FRACTION_BIG = 0.9f
val VIDEO_IN_BOX_WIDTH_BIG = 800.dp
val VIDEO_IN_BOX_HEIGHT_BIG = 800.dp
val VIDEO_OUT_BOX_WIDTH_SMALL = 50.dp
val VIDEO_OUT_BOX_HEIGHT_SMALL = 50.dp
val VIDEO_OUT_BOX_WIDTH_BIG = 500.dp
val VIDEO_OUT_BOX_HEIGHT_BIG = 500.dp
val SAVEDATA_PATH_WIDTH = 200.dp
val SAVEDATA_PATH_HEIGHT = 50.dp
val MYTOXID_WIDTH = 200.dp
val MYTOXID_HEIGHT = 50.dp
val ImageloaderDispatcher = Executors.newFixedThreadPool(5).asCoroutineDispatcher()
var global_semaphore_contactlist_ui = CustomSemaphore(1)
var global_semaphore_grouppeerlist_ui = CustomSemaphore(1)
var global_semaphore_grouplist_ui = CustomSemaphore(1)
var global_semaphore_messagelist_ui = CustomSemaphore(1)
var global_semaphore_groupmessagelist_ui = CustomSemaphore(1)

@OptIn(DelicateCoroutinesApi::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun App()
{
    var start_button_text by remember { mutableStateOf("start") }
    var tox_running_state: String by remember { mutableStateOf("stopped") }
    var ui_scale by remember { mutableStateOf(1.0f) }

    Log.i(TAG, "CCCC:" + PrefsSettings::class.java)
    ui_scale = 1.0f

    try
    {
        val tmp = global_prefs.get("main.ui_scale_factor", null)
        if (tmp != null)
        {
            ui_scale = tmp.toFloat()
        }
    } catch (_: Exception)
    {
    }

    MaterialTheme {
        Scaffold() {
            Row() {
                var uiMode by remember { mutableStateOf(UiMode.CONTACTS) }
                var main_top_tab_height by remember { mutableStateOf(MAIN_TOP_TAB_HEIGHT) }
                BriarSidebar(uiMode = uiMode, setUiMode = { uiMode = it })
                VerticalDivider()
                Column(Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth().height(main_top_tab_height)) {
                        Column() {
                            Row(Modifier.wrapContentHeight(), Arrangement.spacedBy(5.dp)) {
                                Button(modifier = Modifier.width(140.dp), onClick = { // start/stop tox button
                                    if (tox_running_state == "running")
                                    {
                                        tox_running_state = "stopping ..."
                                        start_button_text = tox_running_state
                                        tox_running_state_wrapper = tox_running_state
                                        start_button_text_wrapper = start_button_text
                                        Log.i(TAG, "----> tox_running_state = $tox_running_state_wrapper");
                                        Thread {
                                            Log.i(TAG, "waiting to stop ...");
                                            while (tox_running_state_wrapper != "stopped")
                                            {
                                                Thread.sleep(100)
                                                Log.i(TAG, "waiting ...");
                                            }
                                            Log.i(TAG, "is stopped now");
                                            tox_running_state = tox_running_state_wrapper
                                            start_button_text = "start"
                                        }.start()
                                        TrifaToxService.stop_me = true
                                    } else if (tox_running_state == "stopped")
                                    {
                                        TrifaToxService.stop_me = false
                                        tox_running_state = "starting ..."
                                        start_button_text = tox_running_state
                                        tox_running_state_wrapper = tox_running_state
                                        start_button_text_wrapper = start_button_text
                                        Log.i(TAG, "----> tox_running_state = $tox_running_state_wrapper");
                                        Thread {
                                            Log.i(TAG, "waiting to startup ...");
                                            while (tox_running_state_wrapper != "running")
                                            {
                                                Thread.sleep(100)
                                                Log.i(TAG, "waiting ...");
                                            }
                                            Log.i(TAG, "is started now");
                                            tox_running_state = tox_running_state_wrapper
                                            start_button_text = "stop"
                                        }.start()
                                        TrifaToxService.stop_me = false
                                        main_init()
                                    }
                                }) {
                                    Text(start_button_text)
                                }
                                var online_button_text by remember { mutableStateOf("offline") }
                                Button( // self connection state button
                                    onClick = {}, colors = ButtonDefaults.buttonColors(), enabled = false) {
                                    Box(modifier = Modifier.size(16.dp).border(1.dp, Color.Black, CircleShape).background(Color(online_button_color_wrapper), CircleShape))
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(getOnlineButtonText(online_button_text))
                                    Thread {
                                        while (true)
                                        {
                                            try
                                            {
                                                Thread.sleep(200)
                                                if (online_button_text != online_button_text_wrapper)
                                                {
                                                    online_button_text = online_button_text_wrapper
                                                }
                                            } catch (_: Exception)
                                            {
                                            }
                                        }
                                    }.start()
                                }
                            }
                            SaveDataPath()
                            ToxIDTextField()
                        }
                        var video_in_box_width by remember { mutableStateOf(VIDEO_IN_BOX_WIDTH_SMALL) }
                        var video_in_box_height by remember { mutableStateOf(VIDEO_IN_BOX_HEIGHT_SMALL) }
                        var video_in_box_small by remember { mutableStateOf(true)}
                        var video_in_box_width_fraction by remember { mutableStateOf(VIDEO_IN_BOX_WIDTH_FRACTION_SMALL)}
                        SwingPanel(
                            background = Color.Green,
                            modifier = Modifier.fillMaxWidth(video_in_box_width_fraction)
                                .fillMaxHeight(1.0f)
                                .combinedClickable(onClick = {
                                    if (video_in_box_small)
                                    {
                                        video_in_box_width = VIDEO_IN_BOX_WIDTH_BIG
                                        video_in_box_height = VIDEO_IN_BOX_HEIGHT_BIG
                                    }
                                    else
                                    {
                                        video_in_box_width = VIDEO_IN_BOX_WIDTH_SMALL
                                        video_in_box_height = VIDEO_IN_BOX_HEIGHT_SMALL
                                    }
                                    video_in_box_small != video_in_box_small
                                }),
                            factory = {
                                JPanel(SingleComponentAspectRatioKeeperLayout(),true).apply {
                                    add(JPictureBox.videoinbox)
                                }
                            }
                        )
                        Icon(modifier = Modifier.combinedClickable(onClick = {
                            if (video_in_box_small)
                            {
                                video_in_box_width = VIDEO_IN_BOX_WIDTH_BIG
                                video_in_box_height = VIDEO_IN_BOX_HEIGHT_BIG
                                main_top_tab_height = VIDEO_IN_BOX_HEIGHT_BIG
                                video_in_box_width_fraction = VIDEO_IN_BOX_WIDTH_FRACTION_BIG
                            }
                            else
                            {
                                video_in_box_width = VIDEO_IN_BOX_WIDTH_SMALL
                                video_in_box_height = VIDEO_IN_BOX_HEIGHT_SMALL
                                main_top_tab_height = MAIN_TOP_TAB_HEIGHT
                                video_in_box_width_fraction = VIDEO_IN_BOX_WIDTH_FRACTION_SMALL
                            }
                            video_in_box_small = video_in_box_small.not()
                            Log.i(TAG, "update3: " + video_in_box_small)
                        }), imageVector =  Icons.Default.Fullscreen, contentDescription =  "")
                        var video_out_box_width by remember { mutableStateOf(VIDEO_OUT_BOX_WIDTH_SMALL) }
                        var video_out_box_height by remember { mutableStateOf(VIDEO_OUT_BOX_HEIGHT_SMALL) }
                        var video_out_box_small by remember { mutableStateOf(true)}
                        SwingPanel(
                            background = Color.Green,
                            modifier = Modifier.size(video_out_box_width, video_out_box_height)
                                .combinedClickable(onClick = {
                                    if (video_out_box_small)
                                    {
                                        video_out_box_width = VIDEO_OUT_BOX_WIDTH_BIG
                                        video_out_box_height = VIDEO_OUT_BOX_HEIGHT_BIG
                                    }
                                    else
                                    {
                                        video_out_box_width = VIDEO_OUT_BOX_WIDTH_SMALL
                                        video_out_box_height = VIDEO_OUT_BOX_HEIGHT_SMALL
                                    }
                                    video_out_box_small = !video_out_box_small
                                    Log.i(TAG, "update1: " + video_out_box_small)
                                }),
                            factory = {
                                JPanel(SingleComponentAspectRatioKeeperLayout(),true).apply {
                                    add(JPictureBoxOut.videooutbox)
                                }
                            },
                            update = {Log.i(TAG, "update2: " + video_out_box_small) }
                        )
                        var expanded_a by remember { mutableStateOf(false) }
                        var expanded_v by remember { mutableStateOf(false) }
                        var expanded_as by remember { mutableStateOf(false) }
                        var expanded_vs by remember { mutableStateOf(false) }
                        val audio_in_devices by remember { mutableStateOf(ArrayList<String>()) }
                        val audio_in_sources by remember { mutableStateOf(ArrayList<String>()) }
                        var video_in_devices by remember { mutableStateOf(ArrayList<String>()) }
                        val video_in_sources by remember { mutableStateOf(ArrayList<String>()) }
                        Column {
                            Text(text = "audio in: " + avstatestore.state.audio_in_device_get() + " " + avstatestore.state.audio_in_source_get()
                                , fontSize = 13.sp, modifier = Modifier.fillMaxWidth(),
                                maxLines = 1)
                            Box {
                                IconButton(onClick = {
                                    avstatestore.state.ffmpeg_init_do()
                                    val audio_in_devices_get = AVActivity.ffmpegav_get_audio_in_devices()
                                    println("ffmpeg audio in devices: " + audio_in_devices_get.size)
                                    audio_in_devices.clear()
                                    audio_in_devices.addAll(audio_in_devices_get)
                                    expanded_a = true
                                },
                                    modifier = Modifier.size(16.dp)) {
                                    Icon(Icons.Filled.Refresh, null)
                                }
                                DropdownMenu(
                                    expanded = expanded_a,
                                    onDismissRequest = { expanded_a = false },
                                ) {
                                    if (audio_in_devices.size > 0)
                                    {
                                        audio_in_devices.forEach() {
                                            if (it != null)
                                            {
                                                DropdownMenuItem(onClick = { avstatestore.state.audio_in_source_set("");audio_in_sources.clear(); avstatestore.state.audio_in_device_set(it);expanded_a = false }) {
                                                    Text(""+it)
                                                }
                                            }
                                        }
                                    }
                                    DropdownMenuItem(
                                        onClick = {
                                            avstatestore.state.audio_in_source_set("")
                                            audio_in_sources.clear()
                                            avstatestore.state.audio_in_device_set("")
                                            expanded_a = false
                                        })
                                    {
                                        Text("-none-")
                                    }
                                }
                            }
                            Box {
                                IconButton(onClick = {
                                    if ((avstatestore.state.audio_in_device_get() != null) && (avstatestore.state.audio_in_device_get() != ""))
                                    {
                                        avstatestore.state.ffmpeg_init_do()
                                        var audio_in_sources_get: Array<String> = emptyArray()
                                        val tmp = AVActivity.ffmpegav_get_in_sources(avstatestore.state.audio_in_device_get(), 0)
                                        if (tmp == null)
                                        {
                                            audio_in_sources_get = emptyArray()
                                        }
                                        else
                                        {
                                            audio_in_sources_get = tmp
                                        }
                                        audio_in_sources.clear()
                                        if (audio_in_sources_get.isNotEmpty())
                                        {
                                            println("ffmpeg audio in sources: " + audio_in_sources_get.size)
                                            audio_in_sources.addAll(audio_in_sources_get)
                                            expanded_as = true
                                        }
                                    }
                                },
                                    modifier = Modifier.size(16.dp)) {
                                    Icon(Icons.Filled.Refresh, null)
                                }
                                DropdownMenu(
                                    expanded = expanded_as,
                                    onDismissRequest = { expanded_as = false },
                                ) {
                                    if (audio_in_sources.size > 0)
                                    {
                                        audio_in_sources.forEach() {
                                            if (it != null)
                                            {
                                                DropdownMenuItem(onClick = { avstatestore.state.audio_in_source_set(it);expanded_as = false }) {
                                                    Text(""+it)
                                                }
                                            }
                                        }
                                    }
                                    DropdownMenuItem(
                                        onClick = {
                                            avstatestore.state.audio_in_source_set("")
                                            expanded_as = false
                                        })
                                    {
                                        Text("-none-")
                                    }
                                }
                            }


                            Text("video in: " + avstatestore.state.video_in_device_get() + " " + avstatestore.state.video_in_source_get()
                                , fontSize = 13.sp, modifier = Modifier.fillMaxWidth(),
                                maxLines = 1)
                            Box {
                                IconButton(onClick = {
                                    avstatestore.state.ffmpeg_init_do()
                                    val video_in_devices_get = AVActivity.ffmpegav_get_video_in_devices()
                                    println("ffmpeg video in devices: " + video_in_devices_get.size)
                                    video_in_devices.clear()
                                    video_in_devices.addAll(video_in_devices_get)
                                    expanded_v = true
                                },
                                    modifier = Modifier.size(16.dp)) {
                                    Icon(Icons.Filled.Refresh, null)
                                }
                                DropdownMenu(
                                    expanded = expanded_v,
                                    onDismissRequest = { expanded_v = false },
                                ) {
                                    if (video_in_devices.size > 0)
                                    {
                                        video_in_devices.forEach() {
                                            if (it != null)
                                            {
                                                DropdownMenuItem(onClick = { avstatestore.state.video_in_source_set("");video_in_sources.clear(); avstatestore.state.video_in_device_set(it);expanded_v = false }) {
                                                    Text("" + it)
                                                }
                                            }
                                        }
                                    }
                                    DropdownMenuItem(
                                        onClick = {
                                            avstatestore.state.video_in_source_set("")
                                            video_in_sources.clear()
                                            avstatestore.state.video_in_device_set("")
                                            expanded_v = false
                                        })
                                    {
                                        Text("-none-")
                                    }
                                }
                            }
                            Box {
                                IconButton(onClick = {
                                    if ((avstatestore.state.video_in_device_get() != null) && (avstatestore.state.video_in_device_get() != ""))
                                    {
                                        avstatestore.state.ffmpeg_init_do()
                                        var video_in_sources_get: Array<String> = emptyArray()
                                        if (avstatestore.state.video_in_device_get() == "video4linux2,v4l2")
                                        {
                                            val tmp = AVActivity.ffmpegav_get_in_sources("v4l2", 1)
                                            if (tmp == null)
                                            {
                                                video_in_sources_get = emptyArray()
                                            }
                                            else
                                            {
                                                video_in_sources_get = tmp
                                            }
                                        }
                                        else
                                        {
                                            val tmp = AVActivity.ffmpegav_get_in_sources(avstatestore.state.video_in_device_get(), 1)
                                            if (tmp == null)
                                            {
                                                video_in_sources_get = emptyArray()
                                            }
                                            else
                                            {
                                                video_in_sources_get = tmp
                                            }
                                        }
                                        Log.i(TAG, "video_in_device=" + avstatestore.state.video_in_device_get())
                                        if (avstatestore.state.video_in_device_get() == "x11grab")
                                        {
                                            video_in_sources_get += listOf(":0.0", ":1.0", ":2.0", ":3.0", ":4.0", ":5.0")
                                        }
                                        video_in_sources.clear()
                                        if (video_in_sources_get.isNotEmpty())
                                        {
                                            println("ffmpeg video in sources: " + video_in_sources_get.size)
                                            video_in_sources.addAll(video_in_sources_get)
                                            expanded_vs = true
                                        }
                                    }
                                },
                                    modifier = Modifier.size(16.dp)) {
                                    Icon(Icons.Filled.Refresh, null)
                                }
                                DropdownMenu(
                                    expanded = expanded_vs,
                                    onDismissRequest = { expanded_vs = false },
                                ) {
                                    if (video_in_sources.size > 0)
                                    {
                                        video_in_sources.forEach() {
                                            if (it != null)
                                            {
                                                DropdownMenuItem(onClick = { avstatestore.state.video_in_source_set(it);expanded_vs = false }) {
                                                    Text(""+it)
                                                }
                                            }
                                        }
                                    }
                                    DropdownMenuItem(
                                        onClick = {
                                            avstatestore.state.video_in_source_set("")
                                            expanded_vs = false
                                        })
                                    {
                                        Text("-none-")
                                    }
                                }
                            }
                        }
                    }
                    val audio_bar_bgcolor = MaterialTheme.colors.background
                    SwingPanel(
                        modifier = Modifier.size(200.dp,5.dp),
                        factory = {
                            JPanel(SingleComponentAspectRatioKeeperLayout(), true).apply {
                                add(AudioBar.audio_out_bar)
                                AudioBar.set_bar_bgcolor(audio_bar_bgcolor.toArgb(), audio_out_bar)
                            }
                        },
                        update = { }
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    SwingPanel(
                        modifier = Modifier.size(200.dp,5.dp),
                        factory = {
                            JPanel(SingleComponentAspectRatioKeeperLayout(), true).apply {
                                add(AudioBar.audio_in_bar)
                                AudioBar.set_bar_bgcolor(audio_bar_bgcolor.toArgb(), audio_in_bar)
                            }
                        },
                        update = { }
                    )
                    UIScaleItem(
                        label = i18n("UI Scale"),
                        description = "${i18n("current_value:")}: "
                                + " " + ui_scale + ", " + i18n("drag Slider to change")) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(200.dp)) {
                            Icon(Icons.Default.FormatSize, null, Modifier.scale(0.7f))
                            Slider(value = ui_scale, onValueChange = {
                                ui_scale = it
                                global_prefs.putFloat("main.ui_scale_factor", ui_scale)
                                Log.i(TAG, "density: $ui_scale")
                            }, onValueChangeFinished = { }, valueRange = 0.6f..3f, steps = 6, // todo: without setting the width explicitly,
                                //  the slider takes up the whole remaining space
                                modifier = Modifier.width(150.dp))
                            Icon(Icons.Default.FormatSize, null)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    when (uiMode)
                    {
                        UiMode.CONTACTS ->
                        {
                            val focusRequester = remember { FocusRequester() }
                            val contacts by contactstore.stateFlow.collectAsState()
                            Row(modifier = Modifier.fillMaxWidth()) {
                                ContactList(contactList = contacts)
                                VerticalDivider()
                                if (contacts.selectedContactPubkey == null)
                                {
                                    ExplainerChat()
                                } else
                                {
                                    messagestore.send(MessageAction.Clear(0))
                                    //GlobalScope.launch {
                                        load_messages_for_friend(contacts.selectedContactPubkey)
                                    //}
                                    ChatAppWithScaffold(focusRequester = focusRequester, contactList = contacts, ui_scale = ui_scale)
                                    LaunchedEffect(contacts.selectedContactPubkey) {
                                        focusRequester.requestFocus()
                                    }
                                }
                            }
                        }
                        UiMode.GROUPS ->
                        {
                            val groupfocusRequester = remember { FocusRequester() }
                            val groups by groupstore.stateFlow.collectAsState()
                            val grouppeers by grouppeerstore.stateFlow.collectAsState()
                            Row(modifier = Modifier.fillMaxWidth()) {
                                GroupList(groupList = groups)
                                VerticalDivider()
                                clear_grouppeers()
                                if (groups.selectedGroupId != null)
                                {
                                    load_grouppeers(groups.selectedGroupId!!)
                                }
                                GroupPeerList(grouppeerList = grouppeers)
                                VerticalDivider()
                                if (groups.selectedGroupId == null)
                                {
                                    ExplainerGroup()
                                } else
                                {
                                    groupmessagestore.send(GroupMessageAction.ClearGroup(0))
                                    //GlobalScope.launch {
                                        load_groupmessages_for_friend(groups.selectedGroupId)
                                    //}
                                    GroupAppWithScaffold(focusRequester = groupfocusRequester, groupList = groups, ui_scale = ui_scale)
                                    LaunchedEffect(groups.selectedGroupId) {
                                        groupfocusRequester.requestFocus()
                                    }
                                }
                            }
                        }
                        UiMode.ABOUT -> AboutScreen()
                        UiMode.SETTINGS -> SettingDetails()
                        else -> UiPlaceholder()
                    }
                }
            }
        }
    }

}

fun load_messages_for_friend(selectedContactPubkey: String?)
{
    if (selectedContactPubkey != null)
    {
        try
        {
            val toxk = selectedContactPubkey.uppercase()
            val uimessages = ArrayList<UIMessage>()
            val messages = orma!!.selectFromMessage().tox_friendpubkeyEq(toxk).orderBySent_timestampAsc().toList()
            messages.forEach() { // 0 -> msg received, 1 -> msg sent
                when (it.direction)
                {
                    0 ->
                    {
                        val friendnum = tox_friend_by_public_key(it.tox_friendpubkey.uppercase())
                        val fname = tox_friend_get_name(friendnum)
                        val friend_user = User(fname!!, picture = "friend_avatar.png", toxpk = selectedContactPubkey, color = ColorProvider.getColor(false))
                        uimessages.add(UIMessage(msgDatabaseId = it.id, user = friend_user, timeMs = it.rcvd_timestamp, text = it.text, toxpk = it.tox_friendpubkey.uppercase(), trifaMsgType = it.TRIFA_MESSAGE_TYPE, filename_fullpath = it.filename_fullpath))
                    }
                    1 ->
                    {
                        uimessages.add(UIMessage(msgDatabaseId = it.id, user = myUser, timeMs = it.sent_timestamp, text = it.text, toxpk = myUser.toxpk, trifaMsgType = it.TRIFA_MESSAGE_TYPE, filename_fullpath = it.filename_fullpath))
                    }
                    else ->
                    {
                    }
                }
            }
            // Thread.sleep(4000)
            // Log.i(TAG, "LLLLLLLLLLLLLL")
            messagestore.send(MessageAction.ReceiveMessagesBulkWithClear(uimessages, toxk))
        } catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
}

fun load_groupmessages_for_friend(selectedGroupId: String?)
{
    if (selectedGroupId != null)
    {
        try
        {
            val messages = orma!!.selectFromGroupMessage().group_identifierEq(selectedGroupId).orderBySent_timestampAsc().toList()
            messages.forEach() { // 0 -> msg received, 1 -> msg sent
                when (it.direction)
                {
                    0 ->
                    {
                        val friend_user = User(it.tox_group_peername + " / " + PubkeyShort(it.tox_group_peer_pubkey), picture = "friend_avatar.png", toxpk = it.tox_group_peer_pubkey.uppercase(), color = ColorProvider.getColor(true, it.tox_group_peer_pubkey.uppercase()))
                        when (it.TRIFA_MESSAGE_TYPE)
                        {
                            TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value -> groupmessagestore.send(GroupMessageAction.ReceiveGroupMessage(groupmessage = UIGroupMessage(user = friend_user, timeMs = it.rcvd_timestamp, text = it.text, toxpk = it.tox_group_peer_pubkey.uppercase(), groupId = it.group_identifier.lowercase(), trifaMsgType = it.TRIFA_MESSAGE_TYPE, filename_fullpath = it.filename_fullpath)))
                            TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value -> groupmessagestore.send(GroupMessageAction.ReceiveGroupMessage(groupmessage = UIGroupMessage(user = friend_user, timeMs = it.rcvd_timestamp, text = it.text, toxpk = it.tox_group_peer_pubkey.uppercase(), groupId = it.group_identifier.lowercase(), trifaMsgType = it.TRIFA_MESSAGE_TYPE, filename_fullpath = it.filename_fullpath)))
                        }
                    }
                    1 ->
                    {
                        groupmessagestore.send(GroupMessageAction.SendGroupMessage(UIGroupMessage(myUser, timeMs = it.sent_timestamp, text = it.text, toxpk = myUser.toxpk, groupId = it.group_identifier.lowercase(), trifaMsgType = TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, filename_fullpath = null)))
                    }
                    else ->
                    {
                    }
                }
            }
        } catch (e: Exception)
        {
        }
    }
}

@Composable
private fun ToxIDTextField()
{
    val toxdata by toxdatastore.stateFlow.collectAsState()
    TextField(enabled = true, readOnly = true, singleLine = true,
        textStyle = TextStyle(fontSize = 13.sp),
        modifier = Modifier.width(MYTOXID_WIDTH).height(MYTOXID_HEIGHT).padding(0.dp),
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
        keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrect = false,
    ), value = toxdata.mytoxid, placeholder = {
        Text("my ToxID ...", fontSize = 13.sp)
    }, onValueChange = {})
}

@Composable
private fun SaveDataPath()
{
    val savepathdata by savepathstore.stateFlow.collectAsState()
    TextField(enabled = savepathdata.savePathEnabled, singleLine = true,
        textStyle = TextStyle(fontSize = 13.sp),
        modifier = Modifier.width(SAVEDATA_PATH_WIDTH).height(SAVEDATA_PATH_HEIGHT).padding(0.dp),
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
        keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrect = false,
    ), value = savepathdata.savePath, placeholder = {
        Text("save file path ...", fontSize = 13.sp)
    }, onValueChange = {
        savepathstore.updatePath(it)
    })
}

fun getOnlineButtonText(text_in: String): String
{
    return when (text_in)
    {
        "udp" -> "UDP"
        "tcp" -> "TCP"
        else -> "offline"
    }
}

fun set_tox_running_state(new_state: String)
{
    tox_running_state_wrapper = new_state
    start_button_text_wrapper = tox_running_state_wrapper
    Log.i(TAG, "----> tox_running_state = $tox_running_state_wrapper");
    if (tox_running_state_wrapper == "stopped")
    {
        online_button_color_wrapper = Color.White.toArgb()
        online_button_text_wrapper = "offline"
    }
}

fun set_tox_online_state(new_state: String)
{
    online_button_text_wrapper = new_state
    if (online_button_text_wrapper == "udp")
    {
        online_button_color_wrapper = Color.Green.toArgb()
    } else if (online_button_text_wrapper == "tcp")
    {
        online_button_color_wrapper = Color.Yellow.toArgb()
    } else
    {
        online_button_color_wrapper = Color.Red.toArgb()
    }
    Log.i(TAG, "----> tox_online_state = $online_button_text_wrapper");
}

fun main() = application(exitProcessOnExit = true) {
    try
    { // HINT: show proper name in MacOS Menubar
        // https://alvinalexander.com/java/java-application-name-mac-menu-bar-menubar-class-name/
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TRIfA - Material")
        System.setProperty("apple.awt.application.name", "TRIfA - Material")
        System.setProperty("apple.laf.useScreenMenuBar", "true")
    } catch (e: java.lang.Exception)
    {
        e.printStackTrace()
    }

    try
    { // set "StartupWMClass" for Java Swing applications
        //
        // https://stackoverflow.com/a/29218320
        //
        val xToolkit: Toolkit = Toolkit.getDefaultToolkit()
        var awtAppClassNameField: java.lang.reflect.Field? = null
        awtAppClassNameField = xToolkit.javaClass.getDeclaredField("awtAppClassName")
        awtAppClassNameField.isAccessible = true
        awtAppClassNameField[xToolkit] = "normal_trifa_material" // this needs to be exactly the same String as "StartupWMClass" in the "*.desktop" file
    } catch (e: Exception)
    { // e.printStackTrace()
    }

    MainAppStart()
}

@Composable
private fun MainAppStart()
{
    var showIntroScreen by remember { mutableStateOf(true) }
    var inputTextToxSelfName by remember { mutableStateOf(RandomNameGenerator.getFullName(Random())) }
    try
    {
        val tmp = global_prefs.getBoolean("main.show_intro_screen", true)
        if (tmp == false)
        {
            showIntroScreen = false
        }
    } catch (_: Exception)
    {
    }
    // showIntroScreen = true
    val appIcon = painterResource("icon-linux.png")
    if (showIntroScreen)
    { // ----------- intro screen -----------
        // ----------- intro screen -----------
        // ----------- intro screen -----------
        var isOpen by remember { mutableStateOf(true) }
        var isAskingToClose by remember { mutableStateOf(false) }

        if (isOpen)
        {
            Window(onCloseRequest = { isAskingToClose = true }, title = "TRIfA Material - Welcome", icon = appIcon) {
                Column(Modifier.fillMaxSize()) {
                    Button(onClick = {
                        global_prefs.putBoolean("main.show_intro_screen", false)
                        showIntroScreen = false
                        isOpen = false
                    }) {
                        Text("Start TRIfA")
                    }
                    Text(
                        text = "\n    Welcome to TRIfA Material \n\n    A Tox Client for Desktop",
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = 22.sp,
                        ),
                    )
                    Text(
                        modifier = Modifier.padding(top = 90.dp).align(Alignment.CenterHorizontally).width(400.dp),
                        text = "Choose a Name:",
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = 15.sp,
                        ),
                    )
                    TextField(enabled = true,
                        modifier = Modifier.padding(top = 3.dp).align(Alignment.CenterHorizontally).width(400.dp).border(width = 1.dp, color = Color.Gray),
                        readOnly = false,
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 18.sp),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None,autoCorrect = false),
                        value = inputTextToxSelfName,
                        placeholder = {   Text("") },
                        onValueChange = {inputTextToxSelfName = it})

                    if (isAskingToClose)
                    {
                        isOpen = false
                    }
                }
            }
        } // ----------- intro screen -----------
        // ----------- intro screen -----------
        // ----------- intro screen -----------
    } else
    { // ----------- main app screen -----------
        // ----------- main app screen -----------
        // ----------- main app screen -----------
        var isOpen by remember { mutableStateOf(true) }
        var isAskingToClose by remember { mutableStateOf(false) }
        var state = rememberWindowState()
        var x_ = Dp(0.0f)
        var y_ = Dp(0.0f)
        var w_ = Dp(0.0f)
        var h_ = Dp(0.0f)
        var error = 0
        try
        {
            x_ = global_prefs.get("main.window.position.x", "").toFloat().dp
            y_ = global_prefs.get("main.window.position.y", "").toFloat().dp
            w_ = global_prefs.get("main.window.size.width", "").toFloat().dp
            h_ = global_prefs.get("main.window.size.height", "").toFloat().dp
        } catch (_: Exception)
        {
            error = 1
        }

        if (error == 0)
        {
            val wpos = WindowPosition(x = x_, y = y_)
            val wsize = DpSize(w_, h_)
            state = rememberWindowState(position = wpos, size = wsize)
        }

        if (isOpen)
        {
            Window(onCloseRequest = { isAskingToClose = true }, title = "TRIfA",
                icon = appIcon, state = state,
                onKeyEvent = {
                    when (it.key) {
                        Key.F11 -> {
                            state.placement = WindowPlacement.Fullscreen
                            true
                        }
                        Key.Escape -> {
                            state.placement = WindowPlacement.Floating
                            true
                        }
                        else -> false
                    }
                }
            ) {
                if (isAskingToClose)
                {
                    Dialog(
                        onCloseRequest = { isAskingToClose = false },
                        title = i18n("Close TRIfA ?"),
                    ) {
                        Button(onClick = {
                            if (tox_running_state_wrapper == "running")
                            {
                                set_tox_running_state("stopping ...")
                                TrifaToxService.stop_me = true
                                runBlocking(Dispatchers.Default) {
                                    Log.i(TAG, "waiting to shutdown ...");
                                    while (tox_running_state_wrapper != "stopped")
                                    {
                                        delay(100)
                                        Log.i(TAG, "waiting ...");
                                    }
                                    Log.i(TAG, "closing application");
                                    closing_application = true
                                    isOpen = false
                                }
                            } else
                            {
                                Log.i(TAG, "closing application");
                                isOpen = false
                                closing_application = true
                            }
                        }) {
                            Text(i18n("Yes"))
                        }
                    }
                }

                LaunchedEffect(state) {
                    snapshotFlow { state.size }.onEach(::onWindowResize).launchIn(this)

                    snapshotFlow { state.position }.filter { it.isSpecified }.onEach(::onWindowRelocate).launchIn(this)
                }
                App()
            }
        } // ----------- main app screen -----------
        // ----------- main app screen -----------
        // ----------- main app screen -----------
    }
}

@Suppress("UNUSED_PARAMETER")
private fun onWindowResize(size: DpSize)
{ // println("onWindowResize $size")
    global_prefs.put("main.window.size.width", size.width.value.toString())
    global_prefs.put("main.window.size.height", size.height.value.toString())
}

@Suppress("UNUSED_PARAMETER")
private fun onWindowRelocate(position: WindowPosition)
{ // println("onWindowRelocate $position")
    global_prefs.put("main.window.position.x", position.x.value.toString())
    global_prefs.put("main.window.position.y", position.y.value.toString())
}

@Composable
fun UIScaleItem(
    label: String,
    description: String,
    setting: @Composable (RowScope.() -> Unit),
) = Row(Modifier.fillMaxWidth().height(UISCALE_ITEM_HEIGHT)
    .padding(horizontal = 16.dp).
    semantics(mergeDescendants = true) { // it would be nicer to derive the contentDescriptions from the descendants automatically
    // which is currently not supported in Compose for Desktop
    // see https://github.com/JetBrains/compose-jb/issues/2111
    contentDescription = description
}, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
    Text(label)
    setting()
}

fun unlock_data_dir_input()
{
    savepathstore.updateEnabled(true)
}

fun lock_data_dir_input()
{
    savepathstore.updateEnabled(false)
}

fun actionButton(
    text: String,
    action: () -> Unit
): JButton
{
    val button = JButton(text)
    button.alignmentX = Component.CENTER_ALIGNMENT
    button.addActionListener { action() }

    return button
}

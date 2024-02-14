@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Slider
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.NoiseAware
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RawOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
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
import ca.gosyer.appdirs.AppDirs
import com.google.gson.Gson
import com.vanniktech.emoji.Emoji
import com.zoffcc.applications.ffmpegav.AVActivity
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB
import com.zoffcc.applications.trifa.AVState
import com.zoffcc.applications.trifa.AudioBar
import com.zoffcc.applications.trifa.AudioBar.audio_in_bar
import com.zoffcc.applications.trifa.AudioBar.audio_out_bar
import com.zoffcc.applications.trifa.CustomSemaphore
import com.zoffcc.applications.trifa.GroupSettingDetails
import com.zoffcc.applications.trifa.HelperGeneric.PubkeyShort
import com.zoffcc.applications.trifa.HelperNotification.init_system_tray
import com.zoffcc.applications.trifa.HelperNotification.set_resouces_dir
import com.zoffcc.applications.trifa.JPictureBox
import com.zoffcc.applications.trifa.JPictureBoxOut
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__audio_input_filter
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__v4l2_capture_force_mjpeg
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__video_bitrate_mode
import com.zoffcc.applications.trifa.MainActivity.Companion.accept_incoming_av_call
import com.zoffcc.applications.trifa.MainActivity.Companion.decline_incoming_av_call
import com.zoffcc.applications.trifa.MainActivity.Companion.main_init
import com.zoffcc.applications.trifa.MainActivity.Companion.set_toxav_video_sending_quality
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_name
import com.zoffcc.applications.trifa.NodeListJS
import com.zoffcc.applications.trifa.PrefsSettings
import com.zoffcc.applications.trifa.RandomNameGenerator
import com.zoffcc.applications.trifa.SingleComponentAspectRatioKeeperLayout
import com.zoffcc.applications.trifa.TRIFAGlobals
import com.zoffcc.applications.trifa.ToxVars
import com.zoffcc.applications.trifa.TrifaToxService
import com.zoffcc.applications.trifa.TrifaToxService.Companion.clear_grouppeers
import com.zoffcc.applications.trifa.TrifaToxService.Companion.load_grouppeers
import com.zoffcc.applications.trifa.TrifaToxService.Companion.orma
import com.zoffcc.applications.trifa_material.trifa_material.BuildConfig
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.briarproject.briar.desktop.SettingDetails
import org.briarproject.briar.desktop.contact.ContactList
import org.briarproject.briar.desktop.contact.GroupList
import org.briarproject.briar.desktop.contact.GroupPeerList
import org.briarproject.briar.desktop.navigation.BriarSidebar
import org.briarproject.briar.desktop.ui.AboutScreen
import org.briarproject.briar.desktop.ui.AddFriend
import org.briarproject.briar.desktop.ui.AddGroup
import org.briarproject.briar.desktop.ui.ExplainerChat
import org.briarproject.briar.desktop.ui.ExplainerGroup
import org.briarproject.briar.desktop.ui.ExplainerToxNotRunning
import org.briarproject.briar.desktop.ui.HorizontalDivider
import org.briarproject.briar.desktop.ui.UiMode
import org.briarproject.briar.desktop.ui.UiPlaceholder
import org.briarproject.briar.desktop.ui.VerticalDivider
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import java.awt.Toolkit
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.concurrent.Executors
import java.util.prefs.Preferences
import javax.swing.JPanel
import javax.swing.UIManager

import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import com.vanniktech.emoji.search.SearchEmojiManager
import com.zoffcc.applications.ffmpegav.AVActivity.JAVA_AUDIO_IN_DEVICE_NAME
import com.zoffcc.applications.trifa.EmojiStrAndName
import com.zoffcc.applications.trifa.MainActivity.Companion.DEBUG_COMPOSE_UI_UPDATES
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__do_not_sync_av
import org.briarproject.briar.desktop.ui.Tooltip

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
val MESAGE_INPUT_LINE_HEIGHT = 58.dp
val MAIN_TOP_TAB_HEIGHT = 160.dp
const val IMAGE_PREVIEW_SIZE = 70f
const val AVATAR_SIZE = 40f
const val MAX_AVATAR_SIZE = 70f
val SPACE_AFTER_LAST_MESSAGE = 2.dp
val SPACE_BEFORE_FIRST_MESSAGE = 10.dp
const val CAPTURE_VIDEO_FPS = 15
const val CAPTURE_VIDEO_HIGH_FPS = 30
val VIDEO_IN_BOX_WIDTH_SMALL = 80.dp
val VIDEO_IN_BOX_HEIGHT_SMALL = 80.dp
const val VIDEO_IN_BOX_WIDTH_FRACTION_SMALL = 0.3f
const val VIDEO_IN_BOX_WIDTH_FRACTION_BIG = 0.9f
val VIDEO_IN_BOX_WIDTH_BIG = 800.dp
val VIDEO_IN_BOX_HEIGHT_BIG = 3000.dp
val VIDEO_OUT_BOX_WIDTH_SMALL = 130.dp
val VIDEO_OUT_BOX_HEIGHT_SMALL = 100.dp
val VIDEO_OUT_BOX_WIDTH_BIG = 500.dp
val VIDEO_OUT_BOX_HEIGHT_BIG = 500.dp
val SAVEDATA_PATH_WIDTH = 200.dp
val SAVEDATA_PATH_HEIGHT = 50.dp
val MYTOXID_WIDTH = 200.dp
val MYTOXID_HEIGHT = 50.dp
const val MSG_TEXT_FONT_SIZE_MIXED = 14.0f
const val MSG_TEXT_FONT_SIZE_EMOJI_ONLY = 55.0f
const val MAX_EMOJI_POP_SEARCH_LEN = 20
const val MAX_EMOJI_POP_RESULT = 15
const val MAX_ONE_ON_ONE_MESSAGES_TO_SHOW = 20000
const val MAX_GROUP_MESSAGES_TO_SHOW = 20000
const val SNACKBAR_TOAST_MS_DURATION: Long = 1000
var emojis_cat_all_gropued: ArrayList<ArrayList<ArrayList<EmojiStrAndName>>> = ArrayList()
var emojis_cat_all_cat_names: ArrayList<String> = ArrayList()
var emojis_cat_all_cat_emoji: ArrayList<String> = ArrayList()
const val emojis_per_row = 6
const val NGC_PEER_LUMINANCE_THRESHOLD_FOR_SHADOW = 0.935f // 0.733f // 0.935f
val ImageloaderDispatcher = Executors.newFixedThreadPool(5).asCoroutineDispatcher()
var global_semaphore_contactlist_ui = CustomSemaphore(1)
var global_semaphore_grouppeerlist_ui = CustomSemaphore(1)
var global_semaphore_grouplist_ui = CustomSemaphore(1)
var global_semaphore_messagelist_ui = CustomSemaphore(1)
var global_semaphore_groupmessagelist_ui = CustomSemaphore(1)
val APPDIRS = AppDirs("trifa_material", "zoxcore")
val RESOURCESDIR = File(System.getProperty("compose.application.resources.dir"))
const val GENERIC_TOR_USERAGENT = "Mozilla/5.0 (Windows NT 6.1; rv:60.0) Gecko/20100101 Firefox/60.0"
var scaffoldState: ScaffoldState = ScaffoldState(drawerState = DrawerState(initialValue = DrawerValue.Closed), snackbarHostState = SnackbarHostState())
@OptIn(DelicateCoroutinesApi::class)
var ScaffoldCoroutineScope: CoroutineScope = GlobalScope

@OptIn(DelicateCoroutinesApi::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun App()
{
    var start_button_text by remember { mutableStateOf("start") }
    var tox_running_state: String by remember { mutableStateOf("stopped") }

    println("User data dir: " + APPDIRS.getUserDataDir())
    println("User data dir (roaming): " + APPDIRS.getUserDataDir(roaming = true))
    savepathstore.updatePath(APPDIRS.getUserDataDir(roaming = true))
    println("User config dir: " + APPDIRS.getUserConfigDir())
    println("User config dir (roaming): " + APPDIRS.getUserConfigDir(roaming = true))
    println("User cache dir: " + APPDIRS.getUserCacheDir())
    println("User log dir: " + APPDIRS.getUserLogDir())
    println("Site data dir: " + APPDIRS.getSiteDataDir())
    println("Site data dir (multi path): " + APPDIRS.getSiteDataDir(multiPath = true))
    println("Site config dir: " + APPDIRS.getSiteConfigDir())
    println("Site config dir (multi path): " + APPDIRS.getSiteConfigDir(multiPath = true))
    println("Shared dir: " + APPDIRS.getSharedDir())
    try
    {
        println("Prefs dir (estimation for linux): " + "~/.java/.userPrefs/" + global_prefs.absolutePath())
    }
    catch(_: Exception)
    {
    }

    Log.i(TAG, "resources dir: " + RESOURCESDIR)
    Log.i(TAG, "resources dir canonical: " + RESOURCESDIR.canonicalPath + File.separator)

    Log.i(TAG, "CCCC:" + PrefsSettings::class.java)

    globalstore.loadUiScale()
    var ui_scale by remember { mutableStateOf(globalstore.getUiScale()) }
    MaterialTheme {
        scaffoldState = rememberScaffoldState()
        ScaffoldCoroutineScope = rememberCoroutineScope()
        Scaffold(modifier = Modifier.randomDebugBorder(), scaffoldState = scaffoldState) {
            Row(modifier = Modifier.randomDebugBorder()) {
                var uiMode by remember { mutableStateOf(UiMode.CONTACTS) }
                var main_top_tab_height by remember { mutableStateOf(MAIN_TOP_TAB_HEIGHT) }
                BriarSidebar(uiMode = uiMode, setUiMode = { uiMode = it })
                VerticalDivider()
                Column(Modifier.randomDebugBorder().fillMaxSize()) {
                    Row(modifier = Modifier.randomDebugBorder().fillMaxWidth().height(main_top_tab_height)) {
                        Column(modifier = Modifier.randomDebugBorder()) {
                            Row(Modifier.wrapContentHeight(), Arrangement.spacedBy(5.dp)) {
                                Button(modifier = Modifier.width(140.dp), onClick = { // start/stop tox button
                                    if (tox_running_state == "running")
                                    {
                                        if (avstatestore.state.call_with_friend_pubkey_get() != null)
                                        {
                                            val fnum = tox_friend_by_public_key(avstatestore.state.call_with_friend_pubkey_get())
                                            if (fnum != -1L)
                                            {
                                                MainActivity.shutdown_av_call(fnum)
                                            }
                                        }
                                        tox_running_state = "stopping ..."
                                        start_button_text = tox_running_state
                                        tox_running_state_wrapper = tox_running_state
                                        start_button_text_wrapper = start_button_text
                                        Log.i(TAG, "----> tox_running_state = $tox_running_state_wrapper")
                                        Thread {
                                            Log.i(TAG, "waiting to stop ...")
                                            while (tox_running_state_wrapper != "stopped")
                                            {
                                                Thread.sleep(100)
                                                Log.i(TAG, "waiting ...")
                                            }
                                            Log.i(TAG, "is stopped now")
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
                                        Log.i(TAG, "----> tox_running_state = $tox_running_state_wrapper")
                                        Thread {
                                            Log.i(TAG, "waiting to startup ...")
                                            while (tox_running_state_wrapper != "running")
                                            {
                                                Thread.sleep(100)
                                                Log.i(TAG, "waiting ...")
                                            }
                                            Log.i(TAG, "is started now")
                                            tox_running_state = tox_running_state_wrapper
                                            start_button_text = "stop"
                                        }.start()
                                        TrifaToxService.stop_me = false
                                        savepathstore.createPathDirectories()
                                        main_init()
                                    }
                                }) {
                                    Text(start_button_text)
                                }
                                var online_button_text by remember { mutableStateOf("offline") }
                                Button( // self connection state button
                                    modifier = Modifier.width(120.dp),
                                    onClick = {},
                                    border = BorderStroke(
                                        if (MainActivity.PREF__toxnoise_enabled_to_int_used_for_init == 1)
                                            3.dp
                                        else
                                            0.dp,
                                        if (MainActivity.PREF__toxnoise_enabled_to_int_used_for_init == 1)
                                            Color.Red
                                        else
                                            Color.Transparent
                                        ),
                                    colors = ButtonDefaults.buttonColors(),
                                    enabled = false) {
                                    Box(modifier = Modifier.size(16.dp).border(1.dp,
                                        Color.Black,
                                        CircleShape).background(Color(online_button_color_wrapper), CircleShape))
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
                            Row(verticalAlignment = Alignment.Bottom) {
                                Column {
                                    SaveDataPath()
                                    ToxIDTextField()
                                }
                                ToxIDQRCode()
                            }
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        val current_callstate2 by avstatestorecallstate.stateFlow.collectAsState()
                        when (current_callstate2.call_state)
                        {
                            AVState.CALL_STATUS.CALL_STATUS_INCOMING ->
                            {
                                Row() {
                                    Column(modifier = Modifier.fillMaxHeight(1.0f).width(400.dp)
                                        .padding(10.dp)
                                        .dashedBorder(color = Color.Red,
                                            strokeWidth = 5.dp,
                                            cornerRadiusDp = 25.dp)) {
                                        Row(modifier = Modifier.padding(15.dp)) {
                                            Spacer(Modifier.size(2.dp).weight(0.3f))
                                            var fname: String? = ""
                                            try
                                            {
                                                fname = tox_friend_get_name(tox_friend_by_public_key(avstatestore.state.call_with_friend_pubkey_get()))
                                            }
                                            catch (_: Exception)
                                            {
                                            }
                                            var text_value = "incoming Call"
                                            if ((fname != null) && (fname != ""))
                                            {
                                                text_value  = "incoming Call from: " + fname
                                            }
                                            Text(modifier = Modifier.align(Alignment.CenterVertically),
                                                fontSize = 20.sp,
                                                textAlign = TextAlign.Center,
                                                text = text_value)
                                            Spacer(Modifier.size(2.dp).weight(0.3f))
                                        }
                                        Row(modifier = Modifier.padding(15.dp)) {
                                            Spacer(Modifier.width(70.dp))
                                            IconButton(
                                                icon = Icons.Filled.Check,
                                                iconTint = Color.Green,
                                                iconSize = 30.dp,
                                                contentDescription = "start",
                                                onClick = {
                                                    val calling_friend_pk = avstatestore.state.call_with_friend_pubkey_get()
                                                    if (calling_friend_pk != null)
                                                    {
                                                        accept_incoming_av_call(calling_friend_pk)
                                                    }
                                                }
                                            )
                                            Spacer(Modifier.width(2.dp).weight(0.3f))
                                            IconButton(
                                                icon = Icons.Filled.Cancel,
                                                iconTint = Color.Red,
                                                iconSize = 30.dp,
                                                contentDescription = "cancel",
                                                onClick = {
                                                    if (avstatestore.state.call_with_friend_pubkey_get() != null)
                                                    {
                                                        decline_incoming_av_call()
                                                    }
                                                }
                                            )
                                            Spacer(Modifier.width(70.dp))
                                        }
                                        Row()
                                        {
                                            Spacer(Modifier.size(10.dp))
                                        }
                                    }
                                }
                            }
                            AVState.CALL_STATUS.CALL_STATUS_NONE,
                            AVState.CALL_STATUS.CALL_STATUS_CALLING,
                            AVState.CALL_STATUS.CALL_STATUS_ENDING ->
                            {
                                Row(modifier = Modifier.randomDebugBorder().padding(3.dp)) {
                                    var video_in_box_width by remember { mutableStateOf(VIDEO_IN_BOX_WIDTH_SMALL) }
                                    var video_in_box_height by remember { mutableStateOf(VIDEO_IN_BOX_HEIGHT_SMALL) }
                                    var video_in_box_small by remember { mutableStateOf(true) }
                                    var video_in_box_width_fraction by remember { mutableStateOf(VIDEO_IN_BOX_WIDTH_FRACTION_SMALL) }
                                    Column(modifier = Modifier.fillMaxHeight(1.0f)) {
                                        SwingPanel(
                                            background = Color.Green,
                                            modifier = Modifier.fillMaxWidth(video_in_box_width_fraction)
                                                .padding(5.dp)
                                                .weight(80.0f)
                                                .combinedClickable(onClick = {
                                                    if (video_in_box_small)
                                                    {
                                                        video_in_box_width = VIDEO_IN_BOX_WIDTH_BIG
                                                        video_in_box_height = VIDEO_IN_BOX_HEIGHT_BIG
                                                    } else
                                                    {
                                                        video_in_box_width = VIDEO_IN_BOX_WIDTH_SMALL
                                                        video_in_box_height = VIDEO_IN_BOX_HEIGHT_SMALL
                                                    }
                                                    video_in_box_small != video_in_box_small
                                                }),
                                            factory = {
                                                JPanel(SingleComponentAspectRatioKeeperLayout(), true).apply {
                                                    add(JPictureBox.videoinbox)
                                                }
                                            }
                                        )
                                        val current_vplayfps_state by avstatestorevplayfpsstate.stateFlow.collectAsState()
                                        /*
                                        Text(if (current_vplayfps_state.videoplayfps_state == 0) "" else (" fps: " + current_vplayfps_state.videoplayfps_state),
                                            fontSize = 13.sp,
                                            modifier = Modifier.height(20.dp),
                                            maxLines = 1)
                                        Text(if (current_vplayfps_state.videocap_dec_bitrate == 0) "" else (" BR: " + current_vplayfps_state.videocap_dec_bitrate),
                                            fontSize = 13.sp,
                                            maxLines = 1)
                                        */
                                        Text(" " + current_vplayfps_state.incomingResolution,
                                            fontSize = 13.sp,
                                            maxLines = 1)
                                        }
                                    Column {
                                        val aux_icons_start_padding = 9.dp
                                        val aux_icons_end_padding = 9.dp
                                        val aux_icons_top_padding = 3.dp
                                        val aux_icons_size = 18.dp
                                        Tooltip(text = "toggle large incoming video size") {
                                            Icon(modifier = Modifier.padding(start = aux_icons_start_padding, end = aux_icons_end_padding, top = aux_icons_top_padding).size(aux_icons_size).combinedClickable(
                                                onClick = {
                                                    if (video_in_box_small)
                                                    {
                                                        video_in_box_width = VIDEO_IN_BOX_WIDTH_BIG
                                                        video_in_box_height = VIDEO_IN_BOX_HEIGHT_BIG
                                                        main_top_tab_height = VIDEO_IN_BOX_HEIGHT_BIG
                                                        video_in_box_width_fraction = VIDEO_IN_BOX_WIDTH_FRACTION_BIG
                                                    } else
                                                    {
                                                        video_in_box_width = VIDEO_IN_BOX_WIDTH_SMALL
                                                        video_in_box_height = VIDEO_IN_BOX_HEIGHT_SMALL
                                                        main_top_tab_height = MAIN_TOP_TAB_HEIGHT
                                                        video_in_box_width_fraction = VIDEO_IN_BOX_WIDTH_FRACTION_SMALL
                                                    }
                                                    video_in_box_small = video_in_box_small.not()
                                                    Log.i(TAG, "update3: " + video_in_box_small)
                                                }), imageVector = Icons.Default.Fullscreen,
                                                contentDescription = "toggle large incoming video size"
                                            )
                                        }
                                        var audio_filter_current_value by remember { mutableStateOf(PREF__audio_input_filter) }
                                        Tooltip(text = "enable Noise Suppresion on audio capture") {
                                            Icon(modifier = Modifier.padding(start = aux_icons_start_padding, end = aux_icons_end_padding, top = aux_icons_top_padding).size(aux_icons_size).combinedClickable(
                                                onClick = {
                                                    if (PREF__audio_input_filter == 0)
                                                    {
                                                        PREF__audio_input_filter = 1
                                                    } else
                                                    {
                                                        PREF__audio_input_filter = 0
                                                    }
                                                    audio_filter_current_value = PREF__audio_input_filter
                                                    AVActivity.ffmpegav_apply_audio_filter(PREF__audio_input_filter)
                                                }),
                                                imageVector = Icons.Default.NoiseAware,
                                                contentDescription = "enable Noise Suppresion on audio capture",
                                                tint = if (audio_filter_current_value == 1) Color.Red else Color.DarkGray)
                                        }
                                        var video_force_mjpeg_value by remember { mutableStateOf(PREF__v4l2_capture_force_mjpeg) }
                                        Tooltip(text = "force MJPEG on video capture") {
                                            Icon(modifier = Modifier.padding(start = aux_icons_start_padding, end = aux_icons_end_padding, top = aux_icons_top_padding).size(aux_icons_size).combinedClickable(
                                                onClick = {
                                                    if (PREF__v4l2_capture_force_mjpeg == 0)
                                                    {
                                                        PREF__v4l2_capture_force_mjpeg = 1
                                                    } else
                                                    {
                                                        PREF__v4l2_capture_force_mjpeg = 0
                                                    }
                                                    video_force_mjpeg_value = PREF__v4l2_capture_force_mjpeg
                                                }),
                                                imageVector = Icons.Default.RawOff,
                                                contentDescription = "force MJPEG on video capture",
                                                tint = if (video_force_mjpeg_value == 1) Color.Red else Color.DarkGray)
                                        }

                                        var do_not_sync_av_value by remember { mutableStateOf(PREF__do_not_sync_av) }
                                        Tooltip(text = "force AV sync OFF") {
                                            Icon(modifier = Modifier.padding(start = aux_icons_start_padding, end = aux_icons_end_padding, top = aux_icons_top_padding).size(aux_icons_size).combinedClickable(
                                                onClick = {
                                                    if (PREF__do_not_sync_av == 0)
                                                    {
                                                        PREF__do_not_sync_av = 1
                                                    } else
                                                    {
                                                        PREF__do_not_sync_av = 0
                                                    }
                                                    do_not_sync_av_value = PREF__do_not_sync_av
                                                    MainActivity.tox_set_do_not_sync_av(PREF__do_not_sync_av)
                                                    println("tox_set_do_not_sync_av:2: " + PREF__do_not_sync_av)
                                                }),
                                                imageVector = Icons.Default.LinkOff,
                                                contentDescription = "force AV sync OFF",
                                                tint = if (do_not_sync_av_value == 1) Color.Red else Color.DarkGray)
                                        }


                                        var video_bitrate_mode_value by remember { mutableStateOf(PREF__video_bitrate_mode) }
                                        Tooltip(text = "toggle video capture quality") {
                                            Icon(modifier = Modifier.padding(start = aux_icons_start_padding, end = aux_icons_end_padding, top = aux_icons_top_padding).size(aux_icons_size).combinedClickable(
                                                onClick = {
                                                    if (PREF__video_bitrate_mode == 0)
                                                    {
                                                        PREF__video_bitrate_mode = 1
                                                    } else if (PREF__video_bitrate_mode == 1)
                                                    {
                                                        PREF__video_bitrate_mode = 2
                                                    } else // PREF__video_bitrate_mode == 2
                                                    {
                                                        PREF__video_bitrate_mode = 0
                                                    }
                                                    video_bitrate_mode_value = PREF__video_bitrate_mode

                                                    try
                                                    {
                                                        if (!savepathstore.isEnabled())
                                                        {
                                                            set_toxav_video_sending_quality(PREF__video_bitrate_mode)
                                                        }
                                                    } catch (_: java.lang.Exception)
                                                    {
                                                    }
                                                }),
                                                imageVector = Icons.Default.HighQuality,
                                                contentDescription = "toggle video capture quality",
                                                tint =
                                                if (video_bitrate_mode_value == 0) Color.DarkGray
                                                else if (video_bitrate_mode_value == 1) Color.Green
                                                else Color.Red)
                                        }


                                        val current_callstate3 by avstatestorecallstate.stateFlow.collectAsState()
                                        if (current_callstate3.call_state == AVState.CALL_STATUS.CALL_STATUS_CALLING)
                                        {
                                            Icon(modifier = Modifier.size(36.dp)
                                                .align(Alignment.CenterHorizontally)
                                                .combinedClickable(
                                                onClick = {
                                                    val friendnum = tox_friend_by_public_key(avstatestore.state.call_with_friend_pubkey_get())
                                                    Log.i(com.zoffcc.applications.trifa.TAG, "ffmpeg_devices_stop:002")
                                                    avstatestore.state.ffmpeg_devices_stop()
                                                    MainActivity.toxav_call_control(friendnum, ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value)
                                                    MainActivity.on_call_ended_actions()
                                                }),
                                                imageVector = Icons.Filled.Cancel,
                                                tint = Color.Red,
                                                contentDescription = "stop Call")
                                        }
                                    }
                                    Column {
                                        Spacer(modifier = Modifier.height(5.dp))
                                        var video_out_box_width by remember { mutableStateOf(VIDEO_OUT_BOX_WIDTH_SMALL) }
                                        var video_out_box_height by remember { mutableStateOf(VIDEO_OUT_BOX_HEIGHT_SMALL) }
                                        var video_out_box_small by remember { mutableStateOf(true) }
                                        SwingPanel(
                                            background = Color.Green,
                                            modifier = Modifier.size(video_out_box_width, video_out_box_height)
                                                .combinedClickable(onClick = {
                                                    if (video_out_box_small)
                                                    {
                                                        video_out_box_width = VIDEO_OUT_BOX_WIDTH_BIG
                                                        video_out_box_height = VIDEO_OUT_BOX_HEIGHT_BIG
                                                    } else
                                                    {
                                                        video_out_box_width = VIDEO_OUT_BOX_WIDTH_SMALL
                                                        video_out_box_height = VIDEO_OUT_BOX_HEIGHT_SMALL
                                                    }
                                                    video_out_box_small = !video_out_box_small
                                                    Log.i(TAG, "update1: " + video_out_box_small)
                                                }),
                                            factory = {
                                                JPanel(SingleComponentAspectRatioKeeperLayout(), true).apply {
                                                    add(JPictureBoxOut.videooutbox)
                                                }
                                            },
                                            update = { Log.i(TAG, "update2: " + video_out_box_small) }
                                        )
                                        val current_vicfps_state by avstatestorevcapfpsstate.stateFlow.collectAsState()

                                        /*
                                        Text(if (current_vicfps_state.videocapfps_state == 0) "" else ("fps: " + current_vicfps_state.videocapfps_state),
                                            fontSize = 13.sp,
                                            maxLines = 1)
                                        Text(if (current_vicfps_state.videocap_enc_bitrate == 0) "" else (" BR: " + current_vicfps_state.videocap_enc_bitrate),
                                            fontSize = 13.sp,
                                            maxLines = 1)
                                        */
                                        Text("" + current_vicfps_state.sourceResolution,
                                            fontSize = 13.sp,
                                            maxLines = 1)
                                        Text("" + current_vicfps_state.sourceFormat,
                                            fontSize = 13.sp,
                                            maxLines = 1)
                                    }
                                }
                            }
                        }

                        var expanded_a by remember { mutableStateOf(false) }
                        var expanded_v by remember { mutableStateOf(false) }
                        var expanded_as by remember { mutableStateOf(false) }
                        var expanded_vs by remember { mutableStateOf(false) }
                        val audio_in_devices by remember { mutableStateOf(ArrayList<String>()) }
                        val audio_in_sources by remember { mutableStateOf(ArrayList<AVActivity.ffmpegav_descrid>()) }
                        val video_in_devices by remember { mutableStateOf(ArrayList<String>()) }
                        val video_in_sources by remember { mutableStateOf(ArrayList<AVActivity.ffmpegav_descrid>()) }
                        Column(modifier = Modifier.padding(5.dp).randomDebugBorder()) {
                            Text(text = "audio capture: " + avstatestore.state.audio_in_device_get() + " " + avstatestore.state.audio_in_source_get()
                                , fontSize = 12.sp, modifier = Modifier.fillMaxWidth(),
                                maxLines = 1)
                            Box {
                                IconButton(onClick = {
                                    avstatestore.state.ffmpeg_init_do()
                                    val audio_in_devices_get = AVActivity.ffmpegav_get_audio_in_devices_wrapper()
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
                                        var audio_in_sources_get: Array<AVActivity.ffmpegav_descrid> = emptyArray()
                                        val tmp = AVActivity.ffmpegav_get_in_sources(avstatestore.state.audio_in_device_get(), 0)
                                        if (tmp == null)
                                        {
                                            if (avstatestore.state.audio_in_device_get() == JAVA_AUDIO_IN_DEVICE_NAME)
                                            {
                                                val tmp0 = AVActivity.ffmpegav_descrid()
                                                tmp0.id = "default"
                                                val tmp2 = ArrayList<AVActivity.ffmpegav_descrid>()
                                                tmp2.add(tmp0)
                                                audio_in_sources_get = tmp2.toTypedArray()
                                            }
                                            else
                                            {
                                                audio_in_sources_get = emptyArray()
                                            }
                                        }
                                        else if (avstatestore.state.audio_in_device_get() == JAVA_AUDIO_IN_DEVICE_NAME)
                                        {
                                            val tmp2 = ArrayList<AVActivity.ffmpegav_descrid>()
                                            tmp.iterator().forEach() {
                                                if ((it != null) && (it.id != null))
                                                {
                                                    tmp2.add(it)
                                                }
                                            }
                                            val tmp0 = AVActivity.ffmpegav_descrid()
                                            tmp0.id = "default"
                                            tmp2.add(tmp0)
                                            audio_in_sources_get = tmp2.toTypedArray()
                                        }
                                        else
                                        {
                                            val tmp2 = ArrayList<AVActivity.ffmpegav_descrid>()
                                            tmp.iterator().forEach() {
                                                if ((it != null) && (it.id != null))
                                                {
                                                    tmp2.add(it)
                                                }
                                            }
                                            audio_in_sources_get = tmp2.toTypedArray()
                                        }
                                        //if (avstatestore.state.audio_in_device_get() == "dshow")
                                        //{
                                        //    audio_in_sources_get += listOf("audio=" + MainActivity.DB_PREF__windows_audio_in_source + "")
                                        //}
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
                                                DropdownMenuItem(onClick = { avstatestore.state.audio_in_source_set(it.id);expanded_as = false }) {
                                                    Text(if (it.description.isEmpty()) it.id else it.description + " ("+it.id+")")
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

                            Text("video capture: " + avstatestore.state.video_in_device_get() + " " + avstatestore.state.video_in_source_get()
                                , fontSize = 12.sp, modifier = Modifier.fillMaxWidth(),
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
                                        var video_in_sources_get: Array<AVActivity.ffmpegav_descrid> = emptyArray()
                                        if (avstatestore.state.video_in_device_get() == "video4linux2,v4l2")
                                        {
                                            val tmp = AVActivity.ffmpegav_get_in_sources("v4l2", 1)
                                            if (tmp == null)
                                            {
                                                video_in_sources_get = emptyArray()
                                            }
                                            else
                                            {
                                                val tmp2 = ArrayList<AVActivity.ffmpegav_descrid>()
                                                tmp.iterator().forEach() {
                                                    if ((it != null) && (it.id != null))
                                                    {
                                                        tmp2.add(it)
                                                    }
                                                }
                                                video_in_sources_get = tmp2.toTypedArray()
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
                                                val tmp2 = ArrayList<AVActivity.ffmpegav_descrid>()
                                                tmp.iterator().forEach() {
                                                    if ((it != null) && (it.id != null))
                                                    {
                                                        tmp2.add(it)
                                                    }
                                                }
                                                video_in_sources_get = tmp2.toTypedArray()
                                            }
                                        }
                                        Log.i(TAG, "video_in_device=" + avstatestore.state.video_in_device_get())
                                        if (avstatestore.state.video_in_device_get() == "x11grab")
                                        {
                                            val tmp0 = AVActivity.ffmpegav_descrid()
                                            tmp0.id = ":0.0"
                                            video_in_sources_get += listOf(tmp0)
                                            val tmp1 = AVActivity.ffmpegav_descrid()
                                            tmp1.id = ":1.0"
                                            video_in_sources_get += listOf(tmp1)
                                            val tmp2 = AVActivity.ffmpegav_descrid()
                                            tmp2.id = ":2.0"
                                            video_in_sources_get += listOf(tmp2)
                                            val tmp3 = AVActivity.ffmpegav_descrid()
                                            tmp3.id = ":3.0"
                                            video_in_sources_get += listOf(tmp3)
                                            val tmp4 = AVActivity.ffmpegav_descrid()
                                            tmp4.id = ":4.0"
                                            video_in_sources_get += listOf(tmp4)
                                            val tmp5 = AVActivity.ffmpegav_descrid()
                                            tmp5.id = ":5.0"
                                            video_in_sources_get += listOf(tmp5)
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
                                                DropdownMenuItem(onClick = { avstatestore.state.video_in_source_set(it.id);expanded_vs = false }) {
                                                    Text(if (it.description.isEmpty()) it.id else it.description + " ("+it.id+")")
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

                            var resolution_expanded by remember { mutableStateOf(false) }
                            Text("video resolution: " + avstatestore.state.video_in_resolution_get()
                                , fontSize = 13.sp, modifier = Modifier.fillMaxWidth(),
                                maxLines = 1)
                            IconButton(onClick = {
                                resolution_expanded = true
                            },
                                modifier = Modifier.size(16.dp)) {
                                Icon(Icons.Filled.Refresh, null)
                            }
                            val items = listOf("480x270", "640x360", "640x480", "480x640", "960x540", "1280x720", "720x1280", "1920x1080", "1080x1920")
                            DropdownMenu(
                                expanded = resolution_expanded,
                                onDismissRequest = { resolution_expanded = false },
                            ){
                                items.forEachIndexed { index, s ->
                                    DropdownMenuItem(onClick = {
                                        avstatestore.state.video_in_resolution_set(s)
                                        resolution_expanded = false
                                    }) {
                                        Text(text = s)
                                    }
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.randomDebugBorder().padding(4.dp)) {
                        val audio_bar_bgcolor = MaterialTheme.colors.background
                        SwingPanel(
                            modifier = Modifier.size(200.dp, 5.dp),
                            factory = {
                                JPanel(SingleComponentAspectRatioKeeperLayout(), true).apply {
                                    add(audio_out_bar)
                                    AudioBar.set_bar_bgcolor(audio_bar_bgcolor.toArgb(), audio_out_bar)
                                }
                            },
                            update = { }
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        SwingPanel(
                            modifier = Modifier.size(200.dp, 5.dp),
                            factory = {
                                JPanel(SingleComponentAspectRatioKeeperLayout(), true).apply {
                                    add(audio_in_bar)
                                    AudioBar.set_bar_bgcolor(audio_bar_bgcolor.toArgb(), audio_in_bar)
                                }
                            },
                            update = { }
                        )
                    }
                    UIScaleItem(
                        label = i18n("ui.ui_scale"),
                        description = "${i18n("ui.current_value")}: "
                                + " " + ui_scale + ", " + i18n("ui.drag_slider_to_change")) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(200.dp)) {
                            Icon(Icons.Default.FormatSize, null, Modifier.scale(0.7f))
                            Slider(value = ui_scale, onValueChange = {
                                ui_scale = it
                                globalstore.updateUiScale(it)
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
                            groupsettingsstore.visible(false)
                            contactstore.visible(true)
                            groupstore.visible(false)
                            val focusRequester = remember { FocusRequester() }
                            Row(modifier = Modifier.fillMaxWidth().randomDebugBorder()) {
                                val contacts by contactstore.stateFlow.collectAsState()
                                ContactList(contactList = contacts)
                                VerticalDivider()
                                if (contacts.selectedContactPubkey == null)
                                {
                                    ExplainerChat()
                                } else
                                {
                                    Log.i(TAG, "CONTACTS -> draw")
                                    load_messages_for_friend(contacts.selectedContactPubkey)
                                    GlobalScope.launch { globalstore.try_clear_unread_message_count() }
                                    GlobalScope.launch {
                                        globalfrndstoreunreadmsgs.try_clear_unread_per_friend_message_count(contacts.selectedContactPubkey)
                                    }
                                    ChatAppWithScaffold(focusRequester = focusRequester, contactList = contacts, ui_scale = ui_scale)
                                    //LaunchedEffect(contacts.selectedContactPubkey) {
                                    //    focusRequester.requestFocus()
                                    //}
                                }
                            }
                        }
                        UiMode.GROUPS ->
                        {
                            contactstore.visible(false)
                            groupstore.visible(true)
                            val groupfocusRequester = remember { FocusRequester() }
                            val groups by groupstore.stateFlow.collectAsState()
                            val grouppeers by grouppeerstore.stateFlow.collectAsState()
                            Row(modifier = Modifier.fillMaxWidth().randomDebugBorder()) {
                                GroupList(groupList = groups)
                                VerticalDivider()
                                val groupsettings by groupsettingsstore.stateFlow.collectAsState()
                                if ((groupsettings.visible) && (groups.selectedGroupId != null)) // show group settings
                                {
                                    GroupSettingDetails(groups.selectedGroupId)
                                }
                                else // -- show group messages and peer
                                {
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
                                        Log.i(TAG, "GROUPS -> draw")
                                        load_groupmessages_for_friend(groups.selectedGroupId)
                                        GlobalScope.launch {
                                            globalstore.try_clear_unread_group_message_count()
                                        }
                                        GlobalScope.launch {
                                            globalgrpstoreunreadmsgs.try_clear_unread_per_group_message_count(groups.selectedGroupId)
                                        }
                                        GroupAppWithScaffold(focusRequester = groupfocusRequester, groupList = groups, ui_scale = ui_scale)
                                        //LaunchedEffect(groups.selectedGroupId) {
                                        //    groupfocusRequester.requestFocus()
                                        //}
                                    }
                                }
                            }
                        }
                        UiMode.ADDFRIEND -> {
                            groupsettingsstore.visible(false)
                            contactstore.visible(false)
                            groupstore.visible(false)
                            if (tox_running_state == "running") AddFriend()
                            else ExplainerToxNotRunning()
                        }
                        UiMode.ADDGROUP -> {
                            groupsettingsstore.visible(false)
                            contactstore.visible(false)
                            groupstore.visible(false)
                            if (tox_running_state == "running") AddGroup()
                            else ExplainerToxNotRunning()
                        }
                        UiMode.SETTINGS -> {
                            groupsettingsstore.visible(false)
                            contactstore.visible(false)
                            groupstore.visible(false)
                            SettingDetails()
                        }
                        UiMode.ABOUT -> {
                            groupsettingsstore.visible(false)
                            contactstore.visible(false)
                            groupstore.visible(false)
                            AboutScreen()
                        }
                        else -> {
                            groupsettingsstore.visible(false)
                            contactstore.visible(false)
                            groupstore.visible(false)
                            UiPlaceholder()
                        }
                    }
                }
            }
        }
    }

}

@OptIn(DelicateCoroutinesApi::class)
fun SnackBarToast(message: String, duration_ms: Long = SNACKBAR_TOAST_MS_DURATION)
{
    GlobalScope.launch {
        val job = ScaffoldCoroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
        delay(duration_ms)
        job.cancel()
    }
}

fun load_messages_for_friend(selectedContactPubkey: String?)
{
    if (selectedContactPubkey != null)
    {
        Log.i(TAG, "load_messages_for_friend")
        try
        {
            val toxpk = selectedContactPubkey.uppercase()
            try {
                orma!!.updateMessage().tox_friendpubkeyEq(toxpk).is_new(false).execute()
            } catch(_: Exception) {
            }
            val uimessages = ArrayList<UIMessage>()
            val messages = orma!!.selectFromMessage().
                tox_friendpubkeyEq(toxpk).orderBySent_timestampAsc().toList()
            messages.forEach() {
                when (it.direction)
                {
                    TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value ->
                    {
                        val friendnum = tox_friend_by_public_key(it.tox_friendpubkey.uppercase())
                        val fname = tox_friend_get_name(friendnum)
                        val friend_user = User(fname!!, picture = "friend_avatar.png", toxpk = selectedContactPubkey, color = ColorProvider.getColor(false))
                        uimessages.add(UIMessage(direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value,
                            user = friend_user, timeMs = it.rcvd_timestamp,
                            text = it.text, toxpk = it.tox_friendpubkey.uppercase(),
                            trifaMsgType = it.TRIFA_MESSAGE_TYPE, msgDatabaseId = it.id,
                            filename_fullpath = it.filename_fullpath, file_state = it.state))
                    }
                    TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value ->
                    {
                        uimessages.add(UIMessage(direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value,
                            user = myUser, timeMs = it.sent_timestamp,
                            text = it.text, toxpk = it.tox_friendpubkey.uppercase(),
                            trifaMsgType = it.TRIFA_MESSAGE_TYPE, msgDatabaseId = it.id,
                            filename_fullpath = it.filename_fullpath, file_state = it.state))
                    }
                    else ->
                    {
                    }
                }
            }
            // Thread.sleep(4000)
            messagestore.send(MessageAction.ReceiveMessagesBulkWithClear(uimessages, toxpk))
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
        Log.i(TAG, "load_groupmessages_for_friend")
        try
        {
            val groupid = selectedGroupId.lowercase()
            try {
                orma!!.updateGroupMessage().group_identifierEq(selectedGroupId)
                    .is_new(false).execute()
            } catch(_: Exception) {
            }
            val uigroupmessages = ArrayList<UIGroupMessage>()
            val messages = orma!!.selectFromGroupMessage().group_identifierEq(selectedGroupId).orderBySent_timestampAsc().toList()
            messages.forEach() { // 0 -> msg received, 1 -> msg sent
                when (it.direction)
                {
                    TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value ->
                    {
                        val friend_user = User(it.tox_group_peername + " / " + PubkeyShort(it.tox_group_peer_pubkey), picture = "friend_avatar.png", toxpk = it.tox_group_peer_pubkey.uppercase(), color = ColorProvider.getColor(true, it.tox_group_peer_pubkey.uppercase()))
                        when (it.TRIFA_MESSAGE_TYPE)
                        {
                            TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value ->
                                uigroupmessages.add(UIGroupMessage(was_synced = it.was_synced, msg_id_hash = it.msg_id_hash, message_id_tox = it.message_id_tox, msgDatabaseId = it.id, user = friend_user, timeMs = it.sent_timestamp, text = it.text, toxpk = it.tox_group_peer_pubkey.uppercase(), groupId = it.group_identifier.lowercase(), trifaMsgType = it.TRIFA_MESSAGE_TYPE, filename_fullpath = it.filename_fullpath))
                            TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value ->
                                uigroupmessages.add(UIGroupMessage(was_synced = it.was_synced, msg_id_hash = it.msg_id_hash, message_id_tox = it.message_id_tox, msgDatabaseId = it.id, user = friend_user, timeMs = it.sent_timestamp, text = it.text, toxpk = it.tox_group_peer_pubkey.uppercase(), groupId = it.group_identifier.lowercase(), trifaMsgType = it.TRIFA_MESSAGE_TYPE, filename_fullpath = it.filename_fullpath))

                        }
                    }
                    TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value ->
                    {
                        when (it.TRIFA_MESSAGE_TYPE)
                        {
                            TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value ->
                                uigroupmessages.add(UIGroupMessage(was_synced = it.was_synced, msg_id_hash = it.msg_id_hash, message_id_tox = it.message_id_tox, msgDatabaseId = it.id, user = myUser, timeMs = it.sent_timestamp, text = it.text, toxpk = it.tox_group_peer_pubkey.uppercase(), groupId = it.group_identifier.lowercase(), trifaMsgType = it.TRIFA_MESSAGE_TYPE, filename_fullpath = it.filename_fullpath))
                            TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value ->
                                uigroupmessages.add(UIGroupMessage(was_synced = it.was_synced, msg_id_hash = it.msg_id_hash, message_id_tox = it.message_id_tox, msgDatabaseId = it.id, user = myUser, timeMs = it.sent_timestamp, text = it.text, toxpk = it.tox_group_peer_pubkey.uppercase(), groupId = it.group_identifier.lowercase(), trifaMsgType = it.TRIFA_MESSAGE_TYPE, filename_fullpath = it.filename_fullpath))
                        }
                    }
                    else ->
                    {
                    }
                }
            }
            // Thread.sleep(4000)
            groupmessagestore.send(GroupMessageAction.ReceiveMessagesBulkWithClear(uigroupmessages, groupid))
        } catch (_: Exception)
        {
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ToxIDQRCode()
{
    val toxdata by toxdatastore.stateFlow.collectAsState()
    val qrcodePainter = rememberQrCodePainter(toxdata.mytoxid, toxdata.mytoxid) {
        colors {
            dark = QrBrush.solid(Color.Black)
            light = QrBrush.solid(Color.White)
            frame = QrBrush.solid(Color.Blue)
        }
    }

    val QRCODE_BOX_WIDTH_SMALL = MYTOXID_HEIGHT
    val QRCODE_BOX_HEIGHT_SMALL = MYTOXID_HEIGHT
    val QRCODE_BOX_WIDTH_LARGE = 150.dp
    val QRCODE_BOX_HEIGHT_LARGE = 150.dp
    var qrcode_box_width by remember { mutableStateOf(QRCODE_BOX_WIDTH_SMALL) }
    var qrcode_box_height by remember { mutableStateOf(QRCODE_BOX_HEIGHT_SMALL) }
    var qrcode_box_small by remember { mutableStateOf(true)}
    val image_qrcode_icon = rememberVectorPainter(Icons.Filled.QrCode2)
    Image(if (qrcode_box_small) image_qrcode_icon else qrcodePainter,
        modifier = Modifier.width(qrcode_box_width).
        height(qrcode_box_height).
        padding(start = 5.dp).
        combinedClickable(onClick = {
                if (qrcode_box_small)
                {
                    qrcode_box_width = QRCODE_BOX_WIDTH_LARGE
                    qrcode_box_height = QRCODE_BOX_HEIGHT_LARGE
                    qrcode_box_small = false
                }
                else
                {
                    qrcode_box_width = QRCODE_BOX_WIDTH_SMALL
                    qrcode_box_height = QRCODE_BOX_HEIGHT_SMALL
                    qrcode_box_small = true
                }
            })
        ,
        contentDescription = "my ToxID QR-Code",
        contentScale = ContentScale.Fit)
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
    Log.i(TAG, "----> tox_running_state = $tox_running_state_wrapper")
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
        if (MainActivity.PREF__orbot_enabled_to_int_used_for_init == 1)
        {
            online_button_color_wrapper = Color.Magenta.toArgb()
        }
        else
        {
            online_button_color_wrapper = Color.Yellow.toArgb()
        }
    } else
    {
        online_button_color_wrapper = Color.Red.toArgb()
    }
    Log.i(TAG, "----> tox_online_state = $online_button_text_wrapper")
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

    try {
        set_resouces_dir(RESOURCESDIR.canonicalPath)
    } catch(_: Exception) {}

    try
    {
        EmojiManager.install(IosEmojiProvider())
        // ------
        var emojis_cat_: List<Emoji>
        var grouped_entries: Int
        var remain: Int
        // ------
        // --- loop ---
        for(j1 in 0..(IosEmojiProvider().categories.size - 1))
        {
            Log.i(TAG, "adding emoji category: " + j1 + " : " + IosEmojiProvider().categories[j1].categoryNames.values.elementAt(0))
            val emojis_cat_gropued: ArrayList<ArrayList<EmojiStrAndName>> = ArrayList()
            emojis_cat_ = IosEmojiProvider().categories[j1].emojis
            grouped_entries = emojis_cat_.size / emojis_per_row
            remain = emojis_cat_.size - (grouped_entries * emojis_per_row)
            for (i in 0..(grouped_entries - 1))
            {
                val pos = i * emojis_per_row
                val e: ArrayList<EmojiStrAndName> = ArrayList()
                for (j in 0..(emojis_per_row - 1))
                {
                    // Log.i(TAG, "emoji name(s): " + emojis_cat_[pos + j].shortcodes)
                    // Log.i(TAG, "emoji: " + emojis_cat_[pos + j].unicode)
                    val em = EmojiStrAndName()
                    em.char = emojis_cat_[pos + j].unicode
                    em.name = ""
                    try
                    {
                        em.name = emojis_cat_[pos + j].shortcodes[0]
                    }
                    catch(_: java.lang.Exception)
                    {
                    }
                    e.add(em)
                }
                emojis_cat_gropued.add(e)
            }
            if (remain > 0)
            {
                val pos = grouped_entries * emojis_per_row
                val e: ArrayList<EmojiStrAndName> = ArrayList()
                for (j in 0..(remain - 1))
                {
                    val em = EmojiStrAndName()
                    em.char = emojis_cat_[pos + j].unicode
                    em.name = ""
                    try
                    {
                        em.name = emojis_cat_[pos + j].shortcodes.get(0)
                    }
                    catch(_: java.lang.Exception)
                    {
                    }
                    e.add(em)
                }
                emojis_cat_gropued.add(e)
            }
            emojis_cat_all_gropued.add(emojis_cat_gropued)
            val cat_name = IosEmojiProvider().categories[j1].categoryNames.values.elementAt(0)
            emojis_cat_all_cat_names.add(cat_name)
            var cat_emoji: String
            try
            {
                var search_str = "slightly_smiling_face"
                when (cat_name.lowercase())
                {
                    "faces" -> search_str = "slightly_smiling_face"
                    "nature" -> search_str = "panda"
                    "food" -> search_str = "cup"
                    "activities" -> search_str = "soccer"
                    "places" -> search_str = "car"
                    "objects" -> search_str = "bulb"
                    "symbols" -> search_str = "abc"
                    "flags" -> search_str = "flag-at"
                    else ->
                    {
                    }
                }
                cat_emoji = SearchEmojiManager().search(query = search_str).first().emoji.unicode
                emojis_cat_all_cat_emoji.add(cat_emoji)
            }
            catch(e3: Exception)
            {
                cat_emoji = SearchEmojiManager().search(query = "smile").first().emoji.unicode
                emojis_cat_all_cat_emoji.add(cat_emoji)
            }
            Log.i(TAG, "emoji cat: " + cat_name + " emoji: " + cat_emoji)
        }
        // --- loop ---
    }
    catch (e: Exception)
    {
        e.printStackTrace()
    }

    // ------- set UI look and feel to "system" for java AWT ----------
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    // ------- set UI look and feel to "system" for java AWT ----------

    init_system_tray(RESOURCESDIR.canonicalPath + File.separator + "icon-linux.png")

    MainAppStart()
}

fun update_bootstrap_nodes_from_internet()
{
    val NODES_URL = "https://nodes.tox.chat/json"
    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder()
        .uri(URI(NODES_URL))
        .GET()
        .header("User-Agent", GENERIC_TOR_USERAGENT)
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    val fromJson: NodeListJS = Gson().fromJson(response.body(), NodeListJS::class.java)
    Log.i(TAG, "getLastRefresh=" + fromJson.lastRefresh)
    Log.i(TAG, "getLastScan=" + fromJson.lastScan)
    Log.i(TAG, "getNodes=" + fromJson.nodes.size)
    val bootstrap_nodes_list_from_internet = fromJson.nodes
    var BootstrapNodeEntryDB_ids_full: List<BootstrapNodeEntryDB?>? = orma?.selectFromBootstrapNodeEntryDB()?.orderByIdAsc()?.toList()
    val BootstrapNodeEntryDB_ids: MutableList<Long> = ArrayList()
    if (BootstrapNodeEntryDB_ids_full != null)
    {
        for (bn1 in BootstrapNodeEntryDB_ids_full)
        {
            if (bn1 != null)
            {
                BootstrapNodeEntryDB_ids.add(bn1.id)
            }
        }
    }
    // HINT: set null to GC it soon
    // HINT: set null to GC it soon
    BootstrapNodeEntryDB_ids_full = null
    var num_udp = 0
    var num_tcp = 0
    for (nl_entry in bootstrap_nodes_list_from_internet)
    {
        try
        {
            if (nl_entry.statusUdp)
            {
                try
                {
                    val bn2 = BootstrapNodeEntryDB()
                    bn2.ip = nl_entry.ipv4
                    bn2.port = nl_entry.port?.toLong()!!
                    bn2.key_hex = nl_entry.publicKey.uppercase()
                    bn2.udp_node = true
                    bn2.num = num_udp.toLong()
                    if (bn2.ip != null && !bn2.ip.equals("none", ignoreCase = true) && bn2.port > 0 && bn2.key_hex != null)
                    {
                        orma?.insertIntoBootstrapNodeEntryDB(bn2)
                        Log.i(TAG, "add UDP node:$bn2")
                        num_udp++
                    }
                    val bn2_ip6 = BootstrapNodeEntryDB()
                    bn2_ip6.ip = nl_entry.ipv6
                    bn2_ip6.port = nl_entry.port?.toLong()!!
                    bn2_ip6.key_hex = nl_entry.publicKey.uppercase()
                    bn2_ip6.udp_node = true
                    bn2_ip6.num = num_udp.toLong()
                    if (!bn2_ip6.ip.equals("-", ignoreCase = true) && bn2_ip6.port > 0 && bn2_ip6.key_hex != null)
                    {
                        orma?.insertIntoBootstrapNodeEntryDB(bn2_ip6)
                        Log.i(TAG, "add UDP ipv6 node:$bn2_ip6")
                        num_udp++
                    }
                } catch (e: java.lang.Exception)
                {
                    Log.i(TAG, "add UDP node:EE4:" + e.message)
                    e.printStackTrace()
                }
            }
            if (nl_entry.statusTcp)
            {
                val k = 0
                try
                {
                    val bn2 = BootstrapNodeEntryDB()
                    bn2.ip = nl_entry.ipv4
                    val tcp_ports_count = nl_entry.tcpPorts.size
                    bn2.port = nl_entry.tcpPorts[k]?.toLong()!!
                    bn2.key_hex = nl_entry.publicKey.uppercase()
                    bn2.udp_node = false
                    if (bn2.ip != null && !bn2.ip.equals("none", ignoreCase = true) && bn2.port > 0 && bn2.key_hex != null)
                    {
                        for (p in 0 until tcp_ports_count)
                        {
                            bn2.num = num_tcp.toLong()
                            bn2.port = nl_entry.tcpPorts[p]?.toLong()!!
                            orma?.insertIntoBootstrapNodeEntryDB(bn2)
                            Log.i(TAG, "add tcp node:$bn2")
                            num_tcp++
                        }
                    }
                    val bn2_ip6 = BootstrapNodeEntryDB()
                    bn2_ip6.ip = nl_entry.ipv6
                    val tcp_ports_count_ip6 = nl_entry.tcpPorts.size
                    bn2_ip6.key_hex = nl_entry.publicKey.uppercase()
                    bn2_ip6.udp_node = false
                    bn2_ip6.num = num_tcp.toLong()
                    if (!bn2_ip6.ip.equals("-", ignoreCase = true) && tcp_ports_count_ip6 > 0 && bn2_ip6.key_hex != null)
                    {
                        for (p in 0 until tcp_ports_count_ip6)
                        {
                            val bn2_ip6_ = BootstrapNodeEntryDB()
                            bn2_ip6_.ip = nl_entry.ipv6
                            bn2_ip6_.port = nl_entry.tcpPorts[p]?.toLong()!!
                            bn2_ip6_.key_hex = nl_entry.publicKey.uppercase()
                            bn2_ip6_.udp_node = false
                            bn2_ip6_.num = num_tcp.toLong()
                            orma?.insertIntoBootstrapNodeEntryDB(bn2_ip6_)
                            Log.i(TAG, "add tcp ipv6 node:$bn2_ip6_")
                            num_tcp++
                        }
                    }
                } catch (e: java.lang.Exception)
                {
                    Log.i(TAG, "add tcp node:EE5:" + e.message)
                    e.printStackTrace()
                }
            }
        } catch (e: java.lang.Exception)
        {
            Log.i(TAG, "onConnected:EE3:" + e.message)
            e.printStackTrace()
        }
    }

    try
    {
        if (num_tcp > 1 && num_udp > 1)
        {
            // HINT: we added at least 2 UDP and 2 TCP nodes
            // delete previous nodes from DB
            for (bn_old__id in BootstrapNodeEntryDB_ids)
            {
                orma?.deleteFromBootstrapNodeEntryDB()?.idEq(bn_old__id)?.execute()
            }
        }
    } catch (e: java.lang.Exception)
    {
        Log.i(TAG, "onConnected:EE6:" + e.message)
        e.printStackTrace()
    }
}

object AboutIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFFA500))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MainAppStart()
{
    var showIntroScreen by remember { mutableStateOf(true) }
    var firstRun by remember { mutableStateOf(true) }
    var inputTextToxSelfName by remember { mutableStateOf(RandomNameGenerator.getFullName(Random())) }
    try
    {
        val tmp = global_prefs.getBoolean("main.show_intro_screen", true)
        if (tmp == false)
        {
            showIntroScreen = false
            firstRun = false
        }
    } catch (_: Exception)
    {
    }
    println("globalstore.updateFirstRun:" + firstRun)
    if (firstRun)
    {
        globalstore.updateFirstRun(firstRun)
    }
    // ************* DEBUG ONLY *************
    // ************* DEBUG ONLY *************
    // ************* DEBUG ONLY *************
    // showIntroScreen = true
    // ************* DEBUG ONLY *************
    // ************* DEBUG ONLY *************
    // ************* DEBUG ONLY *************
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
        globalstore.updateStartupSelfname(inputTextToxSelfName)
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
            Window(onCloseRequest = { isAskingToClose = true }, title = "TRIfA - " + BuildConfig.APP_VERSION,
                icon = appIcon, state = state,
                focusable = true,
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
                        title = i18n("ui.close_trifa"),
                    ) {
                        Button(onClick = {
                            if (tox_running_state_wrapper == "running")
                            {
                                set_tox_running_state("stopping ...")
                                TrifaToxService.stop_me = true
                                runBlocking(Dispatchers.Default) {
                                    Log.i(TAG, "waiting to shutdown ...")
                                    while (tox_running_state_wrapper != "stopped")
                                    {
                                        delay(100)
                                        Log.i(TAG, "waiting ...")
                                    }
                                    Log.i(TAG, "closing application")
                                    closing_application = true
                                    isOpen = false
                                }
                            } else
                            {
                                Log.i(TAG, "closing application")
                                isOpen = false
                                closing_application = true
                            }
                        }) {
                            Text(i18n("ui.yes"))
                        }
                    }
                }
                val windowInfo = LocalWindowInfo.current
                LaunchedEffect(windowInfo) {
                    snapshotFlow { windowInfo.isWindowFocused }.collect { onWindowFocused ->
                        onWindowFocused(onWindowFocused)
                    }
                }
                LaunchedEffect(state) {
                    snapshotFlow { state.isMinimized }.onEach(::onWindowMinimised).launchIn(this)
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
private fun onWindowFocused(focused: Boolean)
{
    println("onWindowFocused $focused")
    globalstore.updateFocused(focused)
}

@Suppress("UNUSED_PARAMETER")
private fun onWindowMinimised(minimised: Boolean)
{
    println("onWindowMinimised $minimised")
    globalstore.updateMinimized(minimised)
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
) = Row(Modifier.randomDebugBorder().fillMaxWidth().height(UISCALE_ITEM_HEIGHT)
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

object DragAndDropColors {
    val default = Color.Gray
    val active = Color(29, 117, 223, 255)
    val fileItemBg = Color(233, 30, 99, 255)
    val fileItemFg = Color.White
}

@Composable
fun DragAndDropDescription(modifier: Modifier, color: Color) {
    val modifier2 = modifier.padding(vertical = 2.dp)
    Text(
        "Drag & drop files here",
        fontSize = 20.sp,
        modifier = modifier2,
        color = color
    )
}

fun Modifier.randomDebugBorder(): Modifier =
    if (DEBUG_COMPOSE_UI_UPDATES)
    {
        Modifier.padding(3.dp).border(width = 4.dp,
            color = Color(
                Random().nextInt(0, 255),
                Random().nextInt(0, 255),
                Random().nextInt(0, 255)),
            shape = RectangleShape)
    }
    else
    {
        Modifier
    }

fun Modifier.dashedBorder(strokeWidth: Dp, color: Color, cornerRadiusDp: Dp) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }
        val cornerRadiusPx = density.run { cornerRadiusDp.toPx() }

        then(
            Modifier.drawWithCache {
                onDrawBehind {
                    val stroke = Stroke(
                        width = strokeWidthPx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    drawRoundRect(
                        color = color,
                        style = stroke,
                        cornerRadius = CornerRadius(cornerRadiusPx)
                    )
                }
            }
        )
    }
)


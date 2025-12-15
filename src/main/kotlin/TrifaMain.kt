@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@file:Suppress("LocalVariableName", "FunctionName", "ConvertToStringTemplate", "SpellCheckingInspection", "UnusedReceiverParameter", "LiftReturnOrAssignment", "CascadeIf", "SENSELESS_COMPARISON", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_ANONYMOUS_PARAMETER", "REDUNDANT_ELSE_IN_WHEN", "ReplaceSizeCheckWithIsNotEmpty", "ReplaceRangeToWithRangeUntil", "ReplaceGetOrSet", "SimplifyBooleanWithConstants")

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.NoiseAware
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RawOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SafetyCheck
import androidx.compose.material.icons.filled.VideoLabel
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ca.gosyer.appdirs.AppDirs
import com.google.gson.Gson
import com.kdroid.composetray.utils.SingleInstanceManager
import com.vanniktech.emoji.Emoji
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import com.vanniktech.emoji.search.SearchEmojiManager
import com.zoffcc.applications.ffmpegav.AVActivity
import com.zoffcc.applications.ffmpegav.AVActivity.JAVA_AUDIO_IN_DEVICE_NAME
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB
import com.zoffcc.applications.sorm.GroupMessage
import com.zoffcc.applications.sorm.Message
import com.zoffcc.applications.trifa.AVState
import com.zoffcc.applications.trifa.AudioBar
import com.zoffcc.applications.trifa.AudioBar.audio_in_bar
import com.zoffcc.applications.trifa.AudioBar.audio_out_bar
import com.zoffcc.applications.trifa.CustomSemaphore
import com.zoffcc.applications.trifa.EmojiStrAndName
import com.zoffcc.applications.trifa.FriendSettingDetails
import com.zoffcc.applications.trifa.GroupSettingDetails
import com.zoffcc.applications.trifa.HelperGeneric.PubkeyShort
import com.zoffcc.applications.trifa.HelperGeneric.get_java_os_name
import com.zoffcc.applications.trifa.HelperGeneric.get_java_os_version
import com.zoffcc.applications.trifa.HelperGeneric.get_trifa_build_str
import com.zoffcc.applications.trifa.HelperGeneric.ngc_video_frame_last_incoming_ts
import com.zoffcc.applications.trifa.HelperGroup
import com.zoffcc.applications.trifa.HelperGroup.group_get_last_know_peername
import com.zoffcc.applications.trifa.HelperGroup.update_group_peername_in_all_missing_messages
import com.zoffcc.applications.trifa.HelperNotification.init_system_tray
import com.zoffcc.applications.trifa.HelperNotification.set_resouces_dir
import com.zoffcc.applications.trifa.JPictureBox
import com.zoffcc.applications.trifa.JPictureBoxOut
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.DEBUG_COMPOSE_UI_UPDATES
import com.zoffcc.applications.trifa.MainActivity.Companion.DEBUG_SET_FAKE_WEBCAM
import com.zoffcc.applications.trifa.MainActivity.Companion.ORBOT_PROXY_HOST
import com.zoffcc.applications.trifa.MainActivity.Companion.ORBOT_PROXY_PORT
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__audio_input_filter
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__do_not_sync_av
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__orbot_enabled_to_int
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__v4l2_capture_force_mjpeg
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__video_bitrate_mode
import com.zoffcc.applications.trifa.MainActivity.Companion.accept_incoming_av_call
import com.zoffcc.applications.trifa.MainActivity.Companion.decline_incoming_av_call
import com.zoffcc.applications.trifa.MainActivity.Companion.main_init
import com.zoffcc.applications.trifa.MainActivity.Companion.set_toxav_video_sending_quality
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_get_name
import com.zoffcc.applications.trifa.MainActivity.Companion.toxav_option_set
import com.zoffcc.applications.trifa.NodeListJS
import com.zoffcc.applications.trifa.OperatingSystem
import com.zoffcc.applications.trifa.PrefsSettings
import com.zoffcc.applications.trifa.RandomNameGenerator
import com.zoffcc.applications.trifa.SQLITE_TYPE
import com.zoffcc.applications.trifa.SingleComponentAspectRatioKeeperLayout
import com.zoffcc.applications.trifa.SqliteEscapeLikeString
import com.zoffcc.applications.trifa.TAG
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
import okhttp3.OkHttpClient
import okhttp3.Request
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
import org.briarproject.briar.desktop.ui.ExplainerInfoIsRelay
import org.briarproject.briar.desktop.ui.ExplainerToxNotRunning
import org.briarproject.briar.desktop.ui.HorizontalDivider
import org.briarproject.briar.desktop.ui.Tooltip
import org.briarproject.briar.desktop.ui.UiMode
import org.briarproject.briar.desktop.ui.UiPlaceholder
import org.briarproject.briar.desktop.ui.VerticalDivider
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import java.awt.Toolkit
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.prefs.Preferences
import javax.swing.JPanel

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
const val CONTACT_COLUMN_CONTACTNAME_LEN_THRESHOLD = 13
const val PUSHURL_SHOW_LEN_THRESHOLD = 60
val GROUPS_COLUMN_WIDTH = 190.dp
val GROUPS_COLLAPSED_COLUMN_WIDTH = 50.dp
const val GROUPS_COLUMN_GROUPNAME_LEN_THRESHOLD = 13
val GROUP_PEER_COLUMN_WIDTH = 165.dp
val GROUP_COLLAPSED_PEER_COLUMN_WIDTH = 45.dp
const val GROUP_PEER_COLUMN_PEERNAME_LEN_THRESHOLD = 12
val MESSAGE_INPUT_LINE_HEIGHT = 58.dp
val MAIN_TOP_TAB_HEIGHT = 160.dp
const val IMAGE_PREVIEW_SIZE = 70f
const val AVATAR_SIZE = 40f
const val MAX_AVATAR_SIZE = 70f
val SPACE_AFTER_LAST_MESSAGE = 2.dp
val SPACE_BEFORE_FIRST_MESSAGE = 10.dp
const val LAST_MSG_SCROLL_TO_SCROLL_OFFSET = 10000
const val VIDEO_PLACEHOLDER_ALPHA = 0.2f
val AV_SELECTOR_ICON_SIZE = 10.dp
val VIDEO_IN_BOX_WIDTH_SMALL = 80.dp
val VIDEO_IN_BOX_HEIGHT_SMALL = 80.dp
const val VIDEO_IN_BOX_WIDTH_FRACTION_SMALL = 0.3f
const val VIDEO_IN_BOX_WIDTH_FRACTION_BIG = 0.9f
val VIDEO_STATS_TEXT_HEIGHT = 20.dp
val VIDEO_IN_BOX_WIDTH_BIG = 800.dp
val VIDEO_IN_BOX_HEIGHT_BIG = 3000.dp
val VIDEO_OUT_BOX_WIDTH_SMALL = 130.dp
val VIDEO_OUT_BOX_HEIGHT_SMALL = 100.dp
val VIDEO_OUT_BOX_WIDTH_BIG = 500.dp
val VIDEO_OUT_BOX_HEIGHT_BIG = 500.dp
const val DOUBLE_BUFFER_VIDEOIN = true
const val DOUBLE_BUFFER_VIDEOOUT = true
val SAVEDATA_PATH_WIDTH = 200.dp
val SAVEDATA_PATH_HEIGHT = 50.dp
val MYTOXID_WIDTH = 200.dp
val MYTOXID_HEIGHT = 50.dp
val MAIN_STATUS_BAR_HEIGHT = 18.dp
val MESSAGE_BOX_BOTTOM_PADDING = 4.dp
const val MSG_TEXT_FONT_SIZE_MIXED = 14.0f
const val MSG_TEXT_FONT_SIZE_EMOJI_ONLY = 55.0f
const val MAX_EMOJI_POP_SEARCH_LEN = 20
const val MAX_EMOJI_POP_RESULT = 15
const val MAX_ONE_ON_ONE_MESSAGES_TO_SHOW = 20000
const val MAX_GROUP_MESSAGES_TO_SHOW = 20000
const val SNACKBAR_TOAST_MS_DURATION: Long = 1200
const val BG_COLOR_RELAY_CONTACT_ITEM = 0x448ABEB9
const val BG_COLOR_OWN_RELAY_CONTACT_ITEM = 0x44FFFFB9
const val URL_TEXTVIEW_URL_COLOR = 0xFF223DDC
const val NGC_PRIVATE_MSG_INDICATOR_COLOR = 0xFFFFA255
val VIDEO_BOX_BG_COLOR = Color(0x00E7E7E7) // this is now fully transparent. but just in case the color vaule of the grey BG is saved here
val MESSAGE_PUSH_CHECKMARK_COLOR = Color(0xFF2684A7)
val DELIVERY_CHECKMARK_COLOR = Color(0xFF2684A7)
val DELIVERY_CONFIRM_CHECKMARK_COLOR = Color(0xFF2684A7)
val MESSAGE_CHECKMARKS_ICON_SIZE = 12.dp
val MESSAGE_CHECKMARKS_CONTAINER_SIZE = 12.dp
var emojis_cat_all_gropued: ArrayList<ArrayList<ArrayList<EmojiStrAndName>>> = ArrayList()
var emojis_cat_all_cat_names: ArrayList<String> = ArrayList()
var emojis_cat_all_cat_emoji: ArrayList<String> = ArrayList()
const val emojis_per_row = 6
const val NGC_PEER_LUMINANCE_THRESHOLD_FOR_SHADOW = 0.733 // 0.85f // 0.935f // 0.733f // 0.935f
const val NGC_PEER_SHADOW_COLOR = 0xFF444444
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
var NotoEmojiFont: FontFamily? = null
var DefaultFont: FontFamily? = null
const val DISPLAY_SINGLE_INSTANCE_INFO = 1000L

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
    scaffoldState = rememberScaffoldState()
    ScaffoldCoroutineScope = rememberCoroutineScope()
    Theme {
        Scaffold(modifier = Modifier.randomDebugBorder(), scaffoldState = scaffoldState) {
            Column() {
                Row(modifier = Modifier.randomDebugBorder().weight(0.999f)) {
                    var uiMode by remember { mutableStateOf(UiMode.CONTACTS) }
                    val main_top_tab_height by remember { mutableStateOf(MAIN_TOP_TAB_HEIGHT) }
                    BriarSidebar(uiMode = uiMode, setUiMode = { uiMode = it })
                    VerticalDivider()
                    Column(Modifier.randomDebugBorder()) {
                        Row(modifier = Modifier.randomDebugBorder().fillMaxWidth().height(main_top_tab_height)) {
                            Column(modifier = Modifier.randomDebugBorder()) {
                                Row(Modifier.wrapContentHeight(), Arrangement.spacedBy(5.dp)) {

                                    if (globalstore.getApp_startup())
                                    {
                                        Log.i(TAG, "XXXXXXXXX:00:" + globalstore.getApp_startup())
                                        globalstore.setApp_startup(false)
                                        Log.i(TAG, "XXXXXXXXX:11:" + globalstore.getApp_startup())

                                        if (tox_running_state == "stopped")
                                        {
                                            if (global_prefs.getBoolean("main.auto_connect_tox_with_default_profile", false))
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
                                                    start_button_text = i18n("ui.start_button.stop")
                                                }.start()
                                                TrifaToxService.stop_me = false
                                                savepathstore.createPathDirectories()
                                                main_init()
                                            }
                                        }
                                    }


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
                                                start_button_text = i18n("ui.start_button.stop")
                                            }.start()
                                            TrifaToxService.stop_me = false
                                            savepathstore.createPathDirectories()
                                            main_init()

                                            // ************* DEBUG ONLY *************
                                            // ************* DEBUG ONLY *************
                                            // ************* DEBUG ONLY *************
                                            if (DEBUG_SET_FAKE_WEBCAM)
                                            {
                                                // HINT: set video in source to "v4l2" / "/dev/video10"
                                                Log.i(TAG, "****** DEBUG_SET_FAKE_WEBCAM ******")
                                                Log.i(TAG, "****** DEBUG_SET_FAKE_WEBCAM ******")
                                                Log.i(TAG, "****** DEBUG_SET_FAKE_WEBCAM ******")
                                                Log.i(TAG, "****** DEBUG_SET_FAKE_WEBCAM ******")
                                                avstatestore.state.video_in_device_set("video4linux2,v4l2")
                                                avstatestore.state.video_in_source_set("/dev/video10")
                                            }
                                            // ************* DEBUG ONLY *************
                                            // ************* DEBUG ONLY *************
                                            // ************* DEBUG ONLY *************
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
                                                } catch (_: Exception)
                                                {
                                                }
                                                var text_value = "incoming Call"
                                                if ((fname != null) && (fname != ""))
                                                {
                                                    text_value = "incoming Call from: " + fname
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
                                        val video_in_box_small by remember { mutableStateOf(true) }
                                        val video_in_box_width_fraction by remember { mutableStateOf(VIDEO_IN_BOX_WIDTH_FRACTION_SMALL) }
                                        var h265_encoder by remember { mutableStateOf(false) }
                                        Column(modifier = Modifier.fillMaxHeight(1.0f)) {
                                            if ((current_callstate2.call_state == AVState.CALL_STATUS.CALL_STATUS_NONE) &&
                                                ((ngc_video_frame_last_incoming_ts + 2000) < System.currentTimeMillis()))
                                            {
                                                val painter = painterResource("Tv-test-pattern-146649_640.png")
                                                Tooltip(text = "incoming Video") {
                                                    Image(
                                                        modifier = Modifier.fillMaxWidth(video_in_box_width_fraction)
                                                            .padding(5.dp)
                                                            .weight(80.0f)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .border(1.dp, color = Color.Gray, RoundedCornerShape(10.dp)),
                                                        alpha = VIDEO_PLACEHOLDER_ALPHA,
                                                        contentScale = ContentScale.Crop,
                                                        painter = painter,
                                                        contentDescription = "incoming Video"
                                                    )
                                                }
                                            } else
                                            {
                                                if (avstatestorecallstate.state.video_in_popout)
                                                {
                                                    IconButton(
                                                        modifier = Modifier.fillMaxWidth(video_in_box_width_fraction)
                                                            .padding(5.dp)
                                                            .weight(80.0f)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .border(1.dp, color = Color.Gray, RoundedCornerShape(10.dp)),
                                                        icon = Icons.Filled.Check,
                                                        iconTint = DELIVERY_CHECKMARK_COLOR,
                                                        enabled = false,
                                                        iconSize = 20.dp,
                                                        contentDescription = "Video in popout Window",
                                                        onClick = {}
                                                    )
                                                } else
                                                {
                                                    SwingPanel(
                                                        background = VIDEO_BOX_BG_COLOR,
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
                                                            JPanel(SingleComponentAspectRatioKeeperLayout(), DOUBLE_BUFFER_VIDEOIN).apply {
                                                                add(JPictureBox.videoinbox)
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                            val current_vplayfps_state by avstatestorevplayfpsstate.stateFlow.collectAsState()
                                            /*
                                    // !! IMPORTANT !!
                                    // because for some reason the whole UI and all chat messages will repaint when this
                                    // element changes. i do not know why :-(
                                    Text(if (current_vplayfps_state.videoplayfps_state == 0) "" else (" fps: " + current_vplayfps_state.videoplayfps_state),
                                        fontSize = 11.sp,
                                        modifier = Modifier.height(VIDEO_STATS_TEXT_HEIGHT),
                                        maxLines = 1)
                                    // !! IMPORTANT !!
                                    // because for some reason the whole UI and all chat messages will repaint when this
                                    // element changes. i do not know why :-(
                                    Text(if (current_vplayfps_state.videocap_dec_bitrate == 0) "" else (" BR: " + current_vplayfps_state.videocap_dec_bitrate),
                                        fontSize = 11.sp,
                                        modifier = Modifier.height(VIDEO_STATS_TEXT_HEIGHT),
                                        maxLines = 1)
                                    */
                                            // !! IMPORTANT !!
                                            // because for some reason the whole UI and all chat messages will repaint when this
                                            // element changes. i do not know why :-(
                                            Text(" " + current_vplayfps_state.incomingResolution,
                                                fontSize = 11.sp,
                                                modifier = Modifier.height(VIDEO_STATS_TEXT_HEIGHT),
                                                maxLines = 1)
                                        }
                                        Row() {
                                            val aux_icons_start_padding = 5.dp
                                            val aux_icons_end_padding = 5.dp
                                            val aux_icons_top_padding = 3.dp
                                            val aux_icons_size = 18.dp
                                            Column {
                                                Tooltip(text = "toggle large incoming video size") {
                                                    Icon(modifier = Modifier.padding(start = aux_icons_start_padding, end = aux_icons_end_padding, top = aux_icons_top_padding).size(aux_icons_size).combinedClickable(
                                                        onClick = {
                                                            avstatestorecallstate.video_in_popout_update(!avstatestorecallstate.state.video_in_popout)
                                                            /*
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
                                                    */
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
                                                                Log.i(TAG, "ffmpeg_devices_stop:002")
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
                                                Tooltip(text = "toggle H265 video encoder") {
                                                    Icon(modifier = Modifier.padding(start = aux_icons_start_padding, end = aux_icons_end_padding, top = aux_icons_top_padding).size(aux_icons_size).combinedClickable(
                                                        onClick = {
                                                            h265_encoder = h265_encoder.not()
                                                            if (h265_encoder)
                                                            {
                                                                toxav_option_set(
                                                                    tox_friend_by_public_key(avstatestore.state.call_with_friend_pubkey_get()),
                                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_CODEC_USED.value.toLong(),
                                                                    ToxVars.TOXAV_ENCODER_CODEC_USED_VALUE.TOXAV_ENCODER_CODEC_USED_H265.value.toLong())
                                                            } else
                                                            {
                                                                toxav_option_set(
                                                                    tox_friend_by_public_key(avstatestore.state.call_with_friend_pubkey_get()),
                                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_CODEC_USED.value.toLong(),
                                                                    ToxVars.TOXAV_ENCODER_CODEC_USED_VALUE.TOXAV_ENCODER_CODEC_USED_VP8.value.toLong())
                                                            }
                                                        }), imageVector = Icons.Default.VideoLabel,
                                                        tint = if (h265_encoder) Color.Red else Color.DarkGray,
                                                        contentDescription = "toggle H265 video encoder"
                                                    )
                                                }
                                            }
                                        }
                                        Column {
                                            Spacer(modifier = Modifier.height(5.dp))
                                            var video_out_box_width by remember { mutableStateOf(VIDEO_OUT_BOX_WIDTH_SMALL) }
                                            var video_out_box_height by remember { mutableStateOf(VIDEO_OUT_BOX_HEIGHT_SMALL) }
                                            var video_out_box_small by remember { mutableStateOf(true) }
                                            if (current_callstate2.call_state == AVState.CALL_STATUS.CALL_STATUS_NONE)
                                            {
                                                val painter = painterResource("Tv-test-pattern-146649_640.png")
                                                Tooltip(text = "own Video") {
                                                    Image(
                                                        modifier = Modifier.size(video_out_box_width, video_out_box_height)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .border(1.dp, color = Color.Gray, RoundedCornerShape(10.dp)),
                                                        alpha = VIDEO_PLACEHOLDER_ALPHA,
                                                        contentScale = ContentScale.Crop,
                                                        painter = painter,
                                                        contentDescription = "own Video"
                                                    )
                                                }
                                            } else
                                            {
                                                SwingPanel(
                                                    background = VIDEO_BOX_BG_COLOR,
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
                                                        JPanel(SingleComponentAspectRatioKeeperLayout(), DOUBLE_BUFFER_VIDEOOUT).apply {
                                                            add(JPictureBoxOut.videooutbox)
                                                        }
                                                    },
                                                    update = { Log.i(TAG, "update2: " + video_out_box_small) }
                                                )
                                            }
                                            val current_vicfps_state by avstatestorevcapfpsstate.stateFlow.collectAsState()
                                            /*
                                    // !! IMPORTANT !!
                                    // because for some reason the whole UI and all chat messages will repaint when this
                                    // element changes. i do not know why :-(
                                    Text(if (current_vicfps_state.videocapfps_state == 0) "" else ("fps: " + current_vicfps_state.videocapfps_state),
                                        fontSize = 11.sp,
                                        modifier = Modifier.height(VIDEO_STATS_TEXT_HEIGHT),
                                        maxLines = 1)
                                    // !! IMPORTANT !!
                                    // because for some reason the whole UI and all chat messages will repaint when this
                                    // element changes. i do not know why :-(
                                    Text(if (current_vicfps_state.videocap_enc_bitrate == 0) "" else (" BR: " + current_vicfps_state.videocap_enc_bitrate),
                                        fontSize = 11.sp,
                                        modifier = Modifier.height(VIDEO_STATS_TEXT_HEIGHT),
                                        maxLines = 1)
                                    */
                                            // !! IMPORTANT !!
                                            // because for some reason the whole UI and all chat messages will repaint when this
                                            // element changes. i do not know why :-(
                                            Text("" + current_vicfps_state.sourceResolution,
                                                fontSize = 11.sp,
                                                modifier = Modifier.height(VIDEO_STATS_TEXT_HEIGHT),
                                                maxLines = 1)
                                            // !! IMPORTANT !!
                                            // because for some reason the whole UI and all chat messages will repaint when this
                                            // element changes. i do not know why :-(
                                            Text("" + current_vicfps_state.sourceFormat,
                                                fontSize = 11.sp,
                                                modifier = Modifier.height(VIDEO_STATS_TEXT_HEIGHT),
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
                            Column(modifier = Modifier.randomDebugBorder()) {
                                Text(text = "audio capture: " + avstatestore.state.audio_in_device_get() + " " + avstatestore.state.audio_in_source_get(), fontSize = 12.sp, modifier = Modifier.fillMaxWidth(),
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
                                        modifier = Modifier.size(AV_SELECTOR_ICON_SIZE)) {
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
                                                        Text("" + it)
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
                                                } else
                                                {
                                                    audio_in_sources_get = emptyArray()
                                                }
                                            } else if (avstatestore.state.audio_in_device_get() == JAVA_AUDIO_IN_DEVICE_NAME)
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
                                            } else
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
                                            // if (avstatestore.state.audio_in_device_get() == "dshow")
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
                                        modifier = Modifier.size(AV_SELECTOR_ICON_SIZE)) {
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
                                                        Text(if (it.description.isEmpty()) it.id else it.description + " (" + it.id + ")")
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

                                Text("video capture: " + avstatestore.state.video_in_device_get() + " " + avstatestore.state.video_in_source_get(), fontSize = 12.sp, modifier = Modifier.fillMaxWidth(),
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
                                        modifier = Modifier.size(AV_SELECTOR_ICON_SIZE)) {
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
                                                } else
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
                                            } else
                                            {
                                                val tmp = AVActivity.ffmpegav_get_in_sources(avstatestore.state.video_in_device_get(), 1)
                                                if (tmp == null)
                                                {
                                                    video_in_sources_get = emptyArray()
                                                } else
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
                                        modifier = Modifier.size(AV_SELECTOR_ICON_SIZE)) {
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
                                                        Text(if (it.description.isEmpty()) it.id else it.description + " (" + it.id + ")")
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
                                Text("video resolution: " + avstatestore.state.video_in_resolution_get(), fontSize = 13.sp, modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1)
                                IconButton(onClick = {
                                    resolution_expanded = true
                                },
                                    modifier = Modifier.size(AV_SELECTOR_ICON_SIZE)) {
                                    Icon(Icons.Filled.Refresh, null)
                                }
                                val items = listOf("480x270", "640x360", "640x480", "480x640", "960x540", "720x720", "1024x768", "1280x720", "720x1280", "1080x1080", "1920x1080", "1080x1920")
                                DropdownMenu(
                                    expanded = resolution_expanded,
                                    onDismissRequest = { resolution_expanded = false },
                                ) {
                                    items.forEachIndexed { index, s ->
                                        DropdownMenuItem(onClick = {
                                            if (avstatestore.state.calling_state_get() != AVState.CALL_STATUS.CALL_STATUS_CALLING)
                                            {
                                                // HINT: only allow to change resolution when no call is active
                                                avstatestore.state.video_in_resolution_set(s)
                                            }
                                            else
                                            {
                                                SnackBarToast("Resolution change is only allowed when no call is active")
                                            }
                                            resolution_expanded = false
                                        }) {
                                            Text(text = s)
                                        }
                                    }
                                }
                                var capture_fps_expanded by remember { mutableStateOf(false) }
                                val fps_int = avstatestore.state.video_capture_fps_get()
                                var fps_info_str = "" + fps_int
                                if (fps_int == -1)
                                {
                                    fps_info_str = "Default"
                                }
                                Text("capture fps: " + fps_info_str, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1)
                                IconButton(onClick = {
                                    capture_fps_expanded = true
                                },
                                    modifier = Modifier.size(AV_SELECTOR_ICON_SIZE)) {
                                    Icon(Icons.Filled.Refresh, null)
                                }
                                val items_capture_fps = listOf(-1, 5, 10, 15, 20, 24, 25, 30, 60)
                                DropdownMenu(
                                    expanded = capture_fps_expanded,
                                    onDismissRequest = { capture_fps_expanded = false },
                                ) {
                                    items_capture_fps.forEachIndexed { index, s ->
                                        DropdownMenuItem(onClick = {
                                            try
                                            {
                                                avstatestore.state.video_capture_fps_set(s)
                                            } catch (_: Exception)
                                            {
                                            }
                                            capture_fps_expanded = false
                                        }) {
                                            if (s == -1)
                                            {
                                                Text(text = "Default")
                                            }
                                            else
                                            {
                                                Text(text = "" + s)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Column(modifier = Modifier.randomDebugBorder().padding(4.dp)) {
                            val current_callstate3 by avstatestorecallstate.stateFlow.collectAsState()
                            val audio_bar_bgcolor = MaterialTheme.colors.background
                            if ((current_callstate3.call_state == AVState.CALL_STATUS.CALL_STATUS_NONE) &&
                                ((ngc_video_frame_last_incoming_ts + 2000) < System.currentTimeMillis()))
                            {
                                Box(modifier = Modifier.size(200.dp, 5.dp))
                            } else
                            {
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
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            if ((current_callstate3.call_state == AVState.CALL_STATUS.CALL_STATUS_NONE) &&
                                ((ngc_video_frame_last_incoming_ts + 2000) < System.currentTimeMillis()))
                            {
                                Box(modifier = Modifier.size(200.dp, 5.dp))
                            } else
                            {
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
                        }
                        UIScaleItem(
                            label = i18n("ui.ui_textscale"),
                            description = "${i18n("ui.current_value")}: "
                                    + " " + ui_scale + ", " + i18n("ui.drag_slider_to_change")) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.width(200.dp)) {
                                Icon(Icons.Default.FormatSize, null, Modifier.scale(0.7f))
                                Slider(value = ui_scale,
                                    onValueChange = {
                                        ui_scale = it
                                        globalstore.updateUiScale(it)
                                        Log.i(TAG, "updateUiScale:density: $ui_scale")
                                    },
                                    onValueChangeFinished = { },
                                    valueRange = 0.6f..3f, steps = 6, // todo: without setting the width explicitly,
                                    //  the slider takes up the whole remaining space
                                    modifier = Modifier.width(150.dp))
                                Icon(imageVector = Icons.Default.FormatSize, contentDescription = null)
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
                                Row(modifier = Modifier.randomDebugBorder()) {
                                    val contacts by contactstore.stateFlow.collectAsState()
                                    val ContactListScope = rememberCoroutineScope()
                                    ContactList(contactList = contacts)
                                    VerticalDivider()
                                    val friendsettings by friendsettingsstore.stateFlow.collectAsState()
                                    if ((friendsettings.visible) && (contacts.selectedContactPubkey != null)) // show friend settings
                                    {
                                        FriendSettingDetails(contacts.selectedContactPubkey)
                                    }
                                    else // -- show friend messages
                                    {
                                        if (contacts.selectedContactPubkey == null)
                                        {
                                            ExplainerChat()
                                        }
                                        else
                                        {
                                            if ((contacts.selectedContact != null) && (contacts.selectedContact!!.is_relay))
                                            {
                                                ExplainerInfoIsRelay(contacts.selectedContact)
                                            }
                                            else
                                            {
                                                // Log.i(TAG, "CONTACTS -> draw")
                                                load_messages_for_friend(contacts.selectedContactPubkey)
                                                ContactListScope.launch {
                                                    globalstore.try_clear_unread_message_count()
                                                    globalfrndstoreunreadmsgs.try_clear_unread_per_friend_message_count(contacts.selectedContactPubkey)
                                                }
                                                ChatAppWithScaffold(focusRequester = focusRequester, contactList = contacts, ui_scale = ui_scale)
                                                LaunchedEffect(contacts.selectedContactPubkey) {
                                                    // HINT: focus on the message input field
                                                    focusRequester.requestFocus()
                                                    // Log.i(TAG, "FFFFFF1111111111111: focus on the message input field")
                                                    contactstore.messageresetFilter()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            UiMode.GROUPS ->
                            {
                                friendsettingsstore.visible(false)
                                contactstore.visible(false)
                                groupstore.visible(true)
                                val groupfocusRequester = remember { FocusRequester() }
                                val groups by groupstore.stateFlow.collectAsState()
                                val grouppeers by grouppeerstore.stateFlow.collectAsState()
                                val globalstore__ by globalstore.stateFlow.collectAsState()
                                Row(modifier = Modifier.randomDebugBorder()) {
                                    Box(modifier = Modifier.animateContentSize()) {
                                        GroupList(groupList = groups, peercollapsed = globalstore__.peerListCollapse)
                                    }
                                    VerticalDivider()
                                    val groupsettings by groupsettingsstore.stateFlow.collectAsState()
                                    if (groups.selectedGroupId == null)
                                    {
                                        ExplainerGroup()
                                    }
                                    else
                                    {
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
                                            val GroupPeerListScope = rememberCoroutineScope()
                                            Box(modifier = Modifier.animateContentSize()) {
                                                GroupPeerList(grouppeerList = grouppeers, peercollapsed = globalstore__.peerListCollapse)
                                            }
                                            VerticalDivider()
                                            // Log.i(TAG, "GROUPS -> draw")
                                            load_groupmessages(groups.selectedGroupId)
                                            GroupPeerListScope.launch {
                                                globalstore.try_clear_unread_group_message_count()
                                                globalgrpstoreunreadmsgs.try_clear_unread_per_group_message_count(groups.selectedGroupId)
                                            }
                                            GroupAppWithScaffold(focusRequester = groupfocusRequester, groupList = groups, ui_scale = ui_scale)
                                            LaunchedEffect(groups.selectedGroupId) {
                                                // HINT: focus on the group message input field
                                                groupfocusRequester.requestFocus()
                                                // Log.i(TAG, "FFFFgg1111111111111: focus on the group message input field")
                                                groupstore.groupmessageresetFilter()
                                            }
                                        }
                                    }
                                }
                            }
                            UiMode.ADDFRIEND ->
                            {
                                groupsettingsstore.visible(false)
                                friendsettingsstore.visible(false)
                                contactstore.visible(false)
                                groupstore.visible(false)
                                if (tox_running_state == "running") AddFriend()
                                else ExplainerToxNotRunning()
                            }
                            UiMode.ADDGROUP ->
                            {
                                groupsettingsstore.visible(false)
                                friendsettingsstore.visible(false)
                                contactstore.visible(false)
                                groupstore.visible(false)
                                if (tox_running_state == "running") AddGroup()
                                else ExplainerToxNotRunning()
                            }
                            UiMode.SETTINGS ->
                            {
                                groupsettingsstore.visible(false)
                                friendsettingsstore.visible(false)
                                contactstore.visible(false)
                                groupstore.visible(false)
                                SettingDetails()
                            }
                            UiMode.ABOUT ->
                            {
                                groupsettingsstore.visible(false)
                                friendsettingsstore.visible(false)
                                contactstore.visible(false)
                                groupstore.visible(false)
                                AboutScreen()
                            }
                            else ->
                            {
                                groupsettingsstore.visible(false)
                                friendsettingsstore.visible(false)
                                contactstore.visible(false)
                                groupstore.visible(false)
                                UiPlaceholder()
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(0.dp))
                Row(
                    modifier = Modifier.randomDebugBorder().height(MAIN_STATUS_BAR_HEIGHT).fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom)
                {
                    Spacer(modifier = Modifier.width(5.dp))
                    val globalstore2 by globalstore.stateFlow.collectAsState()
                    Tooltip(
                        modifier = Modifier.randomDebugBorder().width(18.dp),
                        text = if (globalstore2.native_sqlite_type == SQLITE_TYPE.SQLCIPHER) "using sqlcipher encrypted database"
                        else if (globalstore2.native_sqlite_type == SQLITE_TYPE.UNLOADED) "sqlite lib not yet loaded"
                        else "regular sqlite database",
                        textcolor = Color.Black) {
                        IconButton(
                            modifier = Modifier.padding(vertical = 0.dp),
                            icon = if (globalstore2.native_sqlite_type == SQLITE_TYPE.SQLCIPHER) Icons.Filled.SafetyCheck
                                else if (globalstore2.native_sqlite_type == SQLITE_TYPE.UNLOADED) Icons.Filled.Downloading
                                else Icons.Filled.Info,
                            iconSize = 16.dp,
                            enabled = false,
                            iconTint = if (globalstore2.native_sqlite_type == SQLITE_TYPE.SQLCIPHER) Color(0xff116B1B)
                                else if (globalstore2.native_sqlite_type == SQLITE_TYPE.UNLOADED) Color.DarkGray.copy(alpha = 0.7f)
                                else Color.DarkGray.copy(alpha = 0.7f),
                            contentDescription = "",
                            onClick = {},
                        )
                    }
                    Tooltip(
                        modifier = Modifier.randomDebugBorder().width(18.dp),
                        text = if (globalstore2.native_ffmpegav_lib_loaded) "ffmpeg AV JNI lib loaded"
                    else "ffmpeg AV JNI lib not loaded",
                        textcolor = Color.Black) {
                        IconButton(
                            modifier = Modifier.padding(vertical = 0.dp),
                            icon = if (globalstore2.native_ffmpegav_lib_loaded) Icons.Filled.Info else Icons.Filled.Error,
                            iconSize = 16.dp,
                            enabled = false,
                            iconTint = if (globalstore2.native_ffmpegav_lib_loaded) Color.DarkGray.copy(alpha = 0.7f) else Color(0xff9c7924),
                            contentDescription = "",
                            onClick = {},
                        )
                    }
                    // HINT: on Windows we do NOT have a JNI lib for notifications. java can handle it just fine.
                    if (OperatingSystem.getCurrent() != OperatingSystem.WINDOWS)
                    {
                        Tooltip(
                            modifier = Modifier.randomDebugBorder().width(18.dp),
                            text = if (globalstore2.native_notification_lib_loaded) "Notification JNI lib loaded"
                        else "Notification JNI lib not loaded",
                            textcolor = Color.Black) {
                            IconButton(
                                modifier = Modifier.padding(vertical = 0.dp),
                                icon = if (globalstore2.native_notification_lib_loaded) Icons.Filled.Info else Icons.Filled.Error,
                                iconSize = 16.dp,
                                enabled = false,
                                iconTint = if (globalstore2.native_notification_lib_loaded) Color.DarkGray.copy(alpha = 0.7f) else Color(0xff9c7924),
                                contentDescription = "",
                                onClick = {},
                            )
                        }
                    }

                    if (avstatestorecallstate.state.display_av_stats)
                    {
                        val current_vicfps_state by avstatestorevcapfpsstate.stateFlow.collectAsState()
                        Text(if (current_vicfps_state.videocapfps_state == 0) "" else (" cap fps: " + current_vicfps_state.videocapfps_state),
                            fontSize = 11.sp,
                            maxLines = 1)
                        Text(if (current_vicfps_state.videocap_enc_bitrate == 0) "" else (" / BR: " + current_vicfps_state.videocap_enc_bitrate),
                            fontSize = 11.sp,
                            maxLines = 1)

                        val current_vplayfps_state by avstatestorevplayfpsstate.stateFlow.collectAsState()
                        Text(if (current_vplayfps_state.videoplayfps_state == 0) "" else ("  |  play fps: " + current_vplayfps_state.videoplayfps_state),
                            fontSize = 11.sp,
                            maxLines = 1)
                        Text(if (current_vplayfps_state.videocap_dec_bitrate == 0) "" else (" / BR: " + current_vplayfps_state.videocap_dec_bitrate),
                            fontSize = 11.sp,
                            maxLines = 1)
                        Text(if (current_vplayfps_state.network_rtt == 0) "" else ("  |  net RTT: " + current_vplayfps_state.network_rtt + " ms"),
                            fontSize = 11.sp,
                            maxLines = 1)
                        Text(if (current_vplayfps_state.play_delay == 0) "" else ("  |  play delay: " + current_vplayfps_state.play_delay + " ms"),
                            fontSize = 11.sp,
                            maxLines = 1)
                    }

                    var os_name_ver = ""
                    try
                    {
                        os_name_ver = "" + get_java_os_name() + " " + get_java_os_version()
                    }
                    catch (_: Exception)
                    {
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(os_name_ver,
                        fontSize = 11.sp,
                        maxLines = 1)
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
        // Log.i(TAG, "load_messages_for_friend")
        try
        {
            val toxpk = selectedContactPubkey.uppercase()
            try {
                orma!!.updateMessage().tox_friendpubkeyEq(toxpk).is_new(false).execute()
            } catch(_: Exception) {
            }
            val uimessages = ArrayList<UIMessage>()
            var messages: MutableList<Message>? = null
            val filter_active = contactstore.state.messageFilterActive
            val filter_value_raw = contactstore.state.messageFilterString
            val filter_value = SqliteEscapeLikeString(filter_value_raw)
            if ((filter_active) &&
                (!filter_value_raw.isNullOrEmpty()) &&
                (filter_value_raw.isNotBlank()) &&
                (filter_value_raw.length > 0))
            {
                messages = orma!!.
                selectFromMessage().
                tox_friendpubkeyEq(toxpk).
                textLike("%" + filter_value + "%").
                orderBySent_timestampAsc().toList()
            }
            else
            {
                messages = orma!!.
                selectFromMessage().
                tox_friendpubkeyEq(toxpk).
                orderBySent_timestampAsc().toList()
            }
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
                            read = it.read,
                            sent_push = it.sent_push,
                            msg_id_hash = it.msg_id_hash,
                            msg_idv3_hash = it.msg_idv3_hash,
                            msg_version = it.msg_version,
                            recvTimeMs = it.rcvd_timestamp,
                            sentTimeMs = it.sent_timestamp,
                            text = it.text, toxpk = it.tox_friendpubkey.uppercase(),
                            trifaMsgType = it.TRIFA_MESSAGE_TYPE, msgDatabaseId = it.id,
                            filename_fullpath = it.filename_fullpath, file_state = it.state))
                    }
                    TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value ->
                    {
                        uimessages.add(UIMessage(direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value,
                            user = myUser, timeMs = it.sent_timestamp,
                            recvTimeMs = it.rcvd_timestamp,
                            sentTimeMs = it.sent_timestamp,
                            read = it.read,
                            sent_push = it.sent_push,
                            msg_id_hash = it.msg_id_hash,
                            msg_idv3_hash = it.msg_idv3_hash,
                            msg_version = it.msg_version,
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

fun load_groupmessages(selectedGroupId: String?)
{
    if (selectedGroupId != null)
    {
        // Log.i(TAG, "load_groupmessages")
        try
        {
            val groupid = selectedGroupId.lowercase()
            try {
                orma!!.updateGroupMessage().group_identifierEq(selectedGroupId)
                    .is_new(false).execute()
            } catch(_: Exception) {
            }
            val uigroupmessages = ArrayList<UIGroupMessage>()
            var messages: MutableList<GroupMessage>? = null
            val filter_active = groupstore.state.groupmessageFilterActive
            val filter_value_raw = groupstore.state.groupmessageFilterString
            val filter_value = SqliteEscapeLikeString(filter_value_raw)
            if ((filter_active) &&
                (!filter_value_raw.isNullOrEmpty()) &&
                (filter_value_raw.isNotBlank()) &&
                (filter_value_raw.length > 0))
            {
                messages = orma!!
                    .selectFromGroupMessage()
                    .group_identifierEq(selectedGroupId)
                    .textLike("%" + filter_value + "%")
                    .orderBySent_timestampAsc()
                    .toList()
            }
            else
            {
                messages = orma!!
                    .selectFromGroupMessage()
                    .group_identifierEq(selectedGroupId)
                    .orderBySent_timestampAsc()
                    .toList()
            }
            messages.forEach() { // 0 -> msg received, 1 -> msg sent
                when (it.direction)
                {
                    TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value ->
                    {
                        if (it.tox_group_peername.isNullOrEmpty())
                        {
                            // we do not have a peername for this message, try to fix now
                            try
                            {
                                val group_num = HelperGroup.tox_group_by_groupid__wrapper(selectedGroupId)
                                val peernum = MainActivity.tox_group_peer_by_public_key(group_num, it.tox_group_peer_pubkey)
                                val peername_try = tox_group_peer_get_name(group_num, peernum)
                                // HINT: write found peername back to message in DB
                                if (!peername_try.isNullOrEmpty()) {
                                    it.tox_group_peername = peername_try
                                    Log.i(TAG, "load_groupmessages:fill in name=" + it.tox_group_peername)
                                    orma!!.updateGroupMessage().group_identifierEq(selectedGroupId)
                                        .tox_group_peer_pubkeyEq(it.tox_group_peer_pubkey)
                                        .idEq(it.id)
                                        .tox_group_peername(it.tox_group_peername)
                                        .execute()
                                }
                            }
                            catch (_: Exception)
                            {
                            }
                        }

                        if (it.tox_group_peername.isNullOrEmpty())
                        {
                            // we still do not have a peername for this message, try to get it from previous messages in this group
                            val last_know_peername = group_get_last_know_peername(it.group_identifier, it.tox_group_peer_pubkey)
                            if (!last_know_peername.isNullOrEmpty())
                            {
                                it.tox_group_peername = last_know_peername
                                // Log.i(TAG, "load_groupmessages:msg id of missing name:" + it.id)
                                update_group_peername_in_all_missing_messages(it.group_identifier, it.tox_group_peer_pubkey, it.tox_group_peername)
                            }
                        }

                        val friend_user = User(it.tox_group_peername + " / " + PubkeyShort(it.tox_group_peer_pubkey), picture = "friend_avatar.png", toxpk = it.tox_group_peer_pubkey.uppercase(), color = ColorProvider.getColor(true, it.tox_group_peer_pubkey.uppercase()))
                        when (it.TRIFA_MESSAGE_TYPE)
                        {
                            TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value ->
                                uigroupmessages.add(UIGroupMessage(was_synced = it.was_synced,
                                    is_private_msg = it.private_message,
                                    sentTimeMs = it.sent_timestamp,
                                    rcvdTimeMs = it.rcvd_timestamp,
                                    syncdTimeMs = it.rcvd_timestamp,
                                    peer_role = it.tox_group_peer_role,
                                    msg_id_hash = it.msg_id_hash, message_id_tox = it.message_id_tox, msgDatabaseId = it.id, user = friend_user, timeMs = it.sent_timestamp, text = it.text, toxpk = it.tox_group_peer_pubkey.uppercase(), groupId = it.group_identifier.lowercase(), trifaMsgType = it.TRIFA_MESSAGE_TYPE, filename_fullpath = it.filename_fullpath))
                            TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value ->
                                uigroupmessages.add(UIGroupMessage(was_synced = it.was_synced,
                                    is_private_msg = it.private_message,
                                    sentTimeMs = it.sent_timestamp,
                                    rcvdTimeMs = it.rcvd_timestamp,
                                    syncdTimeMs = it.rcvd_timestamp,
                                    peer_role = it.tox_group_peer_role,
                                    msg_id_hash = it.msg_id_hash, message_id_tox = it.message_id_tox, msgDatabaseId = it.id, user = friend_user, timeMs = it.sent_timestamp, text = it.text, toxpk = it.tox_group_peer_pubkey.uppercase(), groupId = it.group_identifier.lowercase(), trifaMsgType = it.TRIFA_MESSAGE_TYPE, filename_fullpath = it.filename_fullpath))

                        }
                    }
                    TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value ->
                    {
                        when (it.TRIFA_MESSAGE_TYPE)
                        {
                            TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value ->
                                uigroupmessages.add(UIGroupMessage(was_synced = it.was_synced,
                                    is_private_msg = it.private_message,
                                    sentTimeMs = it.sent_timestamp,
                                    rcvdTimeMs = it.rcvd_timestamp,
                                    syncdTimeMs = it.rcvd_timestamp,
                                    peer_role = it.tox_group_peer_role,
                                    msg_id_hash = it.msg_id_hash, message_id_tox = it.message_id_tox, msgDatabaseId = it.id, user = myUser, timeMs = it.sent_timestamp, text = it.text, toxpk = it.tox_group_peer_pubkey.uppercase(), groupId = it.group_identifier.lowercase(), trifaMsgType = it.TRIFA_MESSAGE_TYPE, filename_fullpath = it.filename_fullpath))
                            TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value ->
                                uigroupmessages.add(UIGroupMessage(was_synced = it.was_synced,
                                    is_private_msg = it.private_message,
                                    sentTimeMs = it.sent_timestamp,
                                    rcvdTimeMs = it.rcvd_timestamp,
                                    syncdTimeMs = it.rcvd_timestamp,
                                    peer_role = it.tox_group_peer_role,
                                    msg_id_hash = it.msg_id_hash, message_id_tox = it.message_id_tox, msgDatabaseId = it.id, user = myUser, timeMs = it.sent_timestamp, text = it.text, toxpk = it.tox_group_peer_pubkey.uppercase(), groupId = it.group_identifier.lowercase(), trifaMsgType = it.TRIFA_MESSAGE_TYPE, filename_fullpath = it.filename_fullpath))
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
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR)),
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
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR)),
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

@OptIn(DelicateCoroutinesApi::class)
fun main(args: Array<String>) = application(exitProcessOnExit = true) {

    try
    {
        println("args START ============")
        println("args all:" + args.size)
        args.iterator().forEach {
            println("args:" + it)
        }
        println("args DONE  ============")
    }
    catch(e: Exception)
    {
        e.printStackTrace()
    }

    // -- check for single instance --
    // thanks to: https://github.com/kdroidFilter/ComposeNativeTray/blob/master/src/commonMain/kotlin/com/kdroid/composetray/utils/SingleInstanceManager.kt
    //
    var jump_single_instance by remember { mutableStateOf(false) }
    var isSingleInstance = SingleInstanceManager.isSingleInstance(
        onRestoreRequest = {
            // indicate that our main windows needs to be shown (if minimized now)
            globalstore.updateMinimized(false)
        }
    )

    var isOpenSingleInstance by remember { mutableStateOf(true) }
    if (!isSingleInstance) {
        var isAskingToCloseSingleInstance by remember { mutableStateOf(false) }
        if (isOpenSingleInstance)
        {
            Window(onCloseRequest = { isAskingToCloseSingleInstance = true }, title = "Info") {
                GlobalScope.launch {
                    delay(DISPLAY_SINGLE_INSTANCE_INFO)
                    isOpenSingleInstance = false
                }
                @OptIn(ExperimentalComposeUiApi::class)
                Column {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "TRIfA is already running",
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = 22.sp,
                        ),
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Button(onClick = {
                        jump_single_instance = true
                        isOpenSingleInstance = false
                    }) {
                        Text("Start TRIfA anyway (in case of Error)")
                    }
                    if (isAskingToCloseSingleInstance)
                    {
                        isOpenSingleInstance = false
                    }
                }
            }
        }
        else
        {
            if (!jump_single_instance)
            {
                exitApplication()
                return@application
            }
        }
    }

    if ((jump_single_instance) || (isSingleInstance))
    {
        // -- check for single instance --
        try
        {
            // HINT: show proper name in MacOS Menubar
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
            awtAppClassNameField[xToolkit] = "TrifaMainKt" // this needs to be exactly the same String as "StartupWMClass" in the "*.desktop" file
        } catch (e: Exception)
        { // e.printStackTrace()
        }

        try
        {
            set_resouces_dir(RESOURCESDIR.canonicalPath)
        } catch (_: Exception)
        {
        }

        try
        {
            EmojiManager.install(IosEmojiProvider())
            // ------
            var emojis_cat_: List<Emoji>
            var grouped_entries: Int
            var remain: Int
            // ------
            // --- loop ---
            for (j1 in 0..(IosEmojiProvider().categories.size - 1))
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
                        } catch (_: java.lang.Exception)
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
                        } catch (_: java.lang.Exception)
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
                } catch (e3: Exception)
                {
                    cat_emoji = SearchEmojiManager().search(query = "smile").first().emoji.unicode
                    emojis_cat_all_cat_emoji.add(cat_emoji)
                }
                Log.i(TAG, "emoji cat: " + cat_name + " emoji: " + cat_emoji)
            }
            // --- loop ---
        } catch (e: Exception)
        {
            e.printStackTrace()
        }
        // ------- set UI look and feel to "system" for java AWT ----------
        // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        // ------- set UI look and feel to "system" for java AWT ----------
        init_system_tray(RESOURCESDIR.canonicalPath + File.separator + "icon-linux.png")

        MainAppStart()
    }
}

fun update_bootstrap_nodes_from_internet()
{
    val NODES_URL = "https://nodes.tox.chat/json"
    var client: OkHttpClient

    if (PREF__orbot_enabled_to_int == 1)
    {
        val proxyAddr = InetSocketAddress(ORBOT_PROXY_HOST, ORBOT_PROXY_PORT.toInt())
        val proxy = Proxy(Proxy.Type.SOCKS, proxyAddr)
        client = OkHttpClient().newBuilder()
            .proxy(proxy)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(6, TimeUnit.SECONDS)
            .connectTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    } else
    {
        client = OkHttpClient().newBuilder()
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(6, TimeUnit.SECONDS)
            .connectTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }
    val request = Request.Builder()
        .url(NODES_URL)
        .get()
        .header("User-Agent", GENERIC_TOR_USERAGENT)
        .build()
    val response = client.newCall(request).execute()
    val fromJson: NodeListJS = Gson().fromJson(response.body?.string(), NodeListJS::class.java)
    Log.i(TAG, "getLastRefresh=" + fromJson.lastRefresh)
    Log.i(TAG, "getLastScan=" + fromJson.lastScan)
    Log.i(TAG, "getNodes=" + fromJson.nodes.size)
    val bootstrap_nodes_list_from_internet = fromJson.nodes
    val BootstrapNodeEntryDB_ids_full: List<BootstrapNodeEntryDB?>? = orma?.selectFromBootstrapNodeEntryDB()?.orderByIdAsc()?.toList()
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
                        Log.i(TAG, "add UDP node:" + bn2.toString().trimEnd())
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
                        Log.i(TAG, "add UDP ipv6 node:" + bn2_ip6.toString().trimEnd())
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
                            Log.i(TAG, "add tcp node:" + bn2.toString().trimEnd())
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
                            Log.i(TAG, "add tcp ipv6 node:" + bn2_ip6_.toString().trimEnd())
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

    try
    {
        response.body.close()
    }
    catch(_: Exception)
    {
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
    globalstore.setDefaultDensity(LocalDensity.current.density)

    var use_custom_font_with_color_emoji = true
    try
    {
        val tmp = global_prefs.getBoolean("main.use_custom_font_with_color_emoji", true)
        if (tmp == false)
        {
            use_custom_font_with_color_emoji = false
        }
    } catch (_: Exception)
    {
    }

    try
    {
        // HINT: for some reason the fonts do not load on macOS
        if ((OperatingSystem.getCurrent() != OperatingSystem.MACOS) && (OperatingSystem.getCurrent() != OperatingSystem.MACARM))
        {
            NotoEmojiFont = FontFamily(
                Font(resource = "fonts/NotoColorEmoji.ttf", FontWeight.Normal),
            )
        }
    }
    catch(_: Exception)
    {
    }

    try
    {
        // HINT: for some reason the fonts do not load on macOS
        if ((OperatingSystem.getCurrent() != OperatingSystem.MACOS) && (OperatingSystem.getCurrent() != OperatingSystem.MACARM))
        {
            if (OperatingSystem.getCurrent() ==  OperatingSystem.LINUX)
            {
                if (use_custom_font_with_color_emoji)
                {
                    // HINT: use a patched font that noto color emoji with regular chars like numbers and spaces removed
                    // this will show emojis in texts but use the default font as fallback for anything that is not an emoji
                    DefaultFont = FontFamily(
                        Font(resource = "fonts/Noto-COLRv1_normal_chars_removed.ttf", FontWeight.Normal, FontStyle.Normal),
                        Font(resource = "fonts/Noto-COLRv1_normal_chars_removed.ttf", FontWeight.SemiBold, FontStyle.Normal),
                        Font(resource = "fonts/Noto-COLRv1_normal_chars_removed.ttf", FontWeight.Bold, FontStyle.Normal),
                        // Font(resource = "fonts/NotoSans-Regular.ttf", FontWeight.Normal, FontStyle.Normal),
                        // Font(resource = "fonts/NotoSans-SemiBold.ttf", FontWeight.SemiBold, FontStyle.Normal),
                        // Font(resource = "fonts/NotoSans-SemiBold.ttf", FontWeight.Bold, FontStyle.Normal),
                    )
                    /*
                    val default_font_file_with_path = RESOURCESDIR.toString() + "/" + "NotoSans-Regular.ttf"
                    Log.i(TAG, "font=" + default_font_file_with_path)
                    val f:  java.awt.Font =  java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
                        File(default_font_file_with_path))
                    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    ge.registerFont(f)
                    */
                }
                else
                {
                    DefaultFont = FontFamily(
                        // Font(resource = "fonts/Ubuntu-R.ttf", FontWeight.Normal, FontStyle.Normal),
                        // Font(resource = "fonts/Ubuntu-B.ttf", FontWeight.Bold, FontStyle.Normal),
                        Font(resource = "fonts/NotoSans-Regular.ttf", FontWeight.Normal, FontStyle.Normal),
                        Font(resource = "fonts/NotoSans-SemiBold.ttf", FontWeight.SemiBold, FontStyle.Normal),
                        Font(resource = "fonts/NotoSans-SemiBold.ttf", FontWeight.Bold, FontStyle.Normal),
                    )
                }
            }
        }
    }
    catch(e: Exception)
    {
        e.printStackTrace()
    }


    var display_status_info_for_av_calls = false
    try
    {
        val tmp = global_prefs.getBoolean("main.display_status_info_for_av_calls", false)
        if (tmp == true)
        {
            display_status_info_for_av_calls = true
        }
    } catch (_: Exception)
    {
    }
    avstatestorecallstate.display_av_stats(display_status_info_for_av_calls)

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

    globalstore.loadUiDensity()
    val appIcon = painterResource("icon-linux.png")

    if (showIntroScreen)
    {
        // ----------- intro screen -----------
        // ----------- intro screen -----------
        // ----------- intro screen -----------
        var isOpen by remember { mutableStateOf(true) }
        var isAskingToClose by remember { mutableStateOf(false) }

        if (isOpen)
        {
            Window(onCloseRequest = { isAskingToClose = true }, title = "TRIfA Material - Welcome", icon = appIcon) {
                @OptIn(ExperimentalComposeUiApi::class)
                window.exceptionHandler = WindowExceptionHandler { e -> println("Exception in Compose: $e") }
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
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR)),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, autoCorrect = false),
                        value = inputTextToxSelfName,
                        placeholder = { Text("") },
                        onValueChange = { inputTextToxSelfName = it })

                    if (isAskingToClose)
                    {
                        isOpen = false
                    }
                }
            }
        }
        // ----------- intro screen -----------
        // ----------- intro screen -----------
        // ----------- intro screen -----------
    } else
    {
        // ----------- main app screen -----------
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
            println("init:onWindowReload " + x_ + " " + y_ + " " + w_ + " " + h_)
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
            var win_title_addon = "Unknown Version"
            try
            {
                win_title_addon = BuildConfig.APP_VERSION + " (Build: " + get_trifa_build_str() + ")"
            } catch (_: java.lang.Exception)
            {
            }
            Window(onCloseRequest = { isAskingToClose = true },
                title = "TRIfA - " + win_title_addon,
                icon = appIcon, state = state,
                focusable = true,
                onKeyEvent = {
                    if (!it.isMetaPressed && !it.isAltPressed && !it.isCtrlPressed && !it.isShiftPressed && it.key == Key.F11 && it.type == KeyEventType.KeyDown)
                    {
                        if (state.placement == WindowPlacement.Fullscreen)
                        {
                            state.placement = WindowPlacement.Floating
                        }
                        else
                        {
                            state.placement = WindowPlacement.Fullscreen
                        }
                        true
                    }
                    else if (!it.isMetaPressed && !it.isAltPressed && !it.isCtrlPressed && !it.isShiftPressed && it.key == Key.Escape && it.type == KeyEventType.KeyDown)
                    {
                        if (state.placement == WindowPlacement.Fullscreen)
                        {
                            state.placement = WindowPlacement.Floating
                            true
                        }
                        else
                        {
                            false
                        }
                    } else
                    {
                        false
                    }
                }
            ) {
                @OptIn(ExperimentalComposeUiApi::class)
                window.exceptionHandler = WindowExceptionHandler { e -> println("Exception in Compose: $e") }
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
                // var ui_density by remember { mutableStateOf(globalstore.getUiDensity()) }
                // val manual_recompose = remember { mutableStateOf(globalstore.state.ui_density) }
                CompositionLocalProvider(
                    LocalDensity provides Density(globalstore.state.ui_density)
                )
                {
                    val globalstore__ by globalstore.stateFlow.collectAsState()
                    if (!globalstore__.mainwindow_minimized)
                    {
                        // un-minimize main window when someone tried to open another instance of this app
                        state.isMinimized = false
                        globalstore.updateMinimized(false)
                    }
                    App()
                }
            }

            // ------------ incoming video popup window ------------
            // ------------ incoming video popup window ------------
            val current_callstate by avstatestorecallstate.stateFlow.collectAsState()
            if (current_callstate.video_in_popout)
            {
                val video_in_popout_window_state = rememberWindowState()
                Window(onCloseRequest = { avstatestorecallstate.video_in_popout_update(false) },
                    focusable = true,
                    state = video_in_popout_window_state,
                    onKeyEvent = {
                        if (!it.isMetaPressed && !it.isAltPressed && !it.isCtrlPressed && !it.isShiftPressed && it.key == Key.F11 && it.type == KeyEventType.KeyDown)
                        {
                            if (video_in_popout_window_state.placement == WindowPlacement.Fullscreen)
                            {
                                video_in_popout_window_state.placement = WindowPlacement.Floating
                            }
                            else
                            {
                                video_in_popout_window_state.placement = WindowPlacement.Fullscreen
                            }
                            true
                        }
                        else if (!it.isMetaPressed && !it.isAltPressed && !it.isCtrlPressed && !it.isShiftPressed && it.key == Key.Escape && it.type == KeyEventType.KeyDown)
                        {
                            if (video_in_popout_window_state.placement == WindowPlacement.Fullscreen)
                            {
                                video_in_popout_window_state.placement = WindowPlacement.Floating
                                true
                            }
                            else
                            {
                                false
                            }
                        } else
                        {
                            false
                        }
                    },
                    title = "incoming Video",
                    icon = appIcon) {
                    Column(Modifier.randomDebugBorder().fillMaxSize()) {
                        SwingPanel(
                            background = VIDEO_BOX_BG_COLOR,
                            modifier = Modifier.randomDebugBorder().fillMaxSize()
                                .combinedClickable(onClick = {
                                }),
                            factory = {
                                JPanel(SingleComponentAspectRatioKeeperLayout(), DOUBLE_BUFFER_VIDEOIN).apply {
                                    add(JPictureBox.videoinbox)
                                }
                            }
                        )
                    }
                }
            }
            // ------------ incoming video popup window ------------
            // ------------ incoming video popup window ------------
        }
        // ----------- main app screen -----------
        // ----------- main app screen -----------
        // ----------- main app screen -----------
    }
}

private fun onWindowFocused(focused: Boolean)
{
    // println("onWindowFocused $focused")
    globalstore.updateFocused(focused)
}

private fun onWindowMinimised(minimised: Boolean)
{
    // println("onWindowMinimised $minimised")
    globalstore.updateMinimized(minimised)
}

private fun onWindowResize(size: DpSize)
{
    // println("size: onWindowResize $size " + size.width.value.toString() + " " + size.height.value.toString())
    global_prefs.put("main.window.size.width", size.width.value.toString())
    global_prefs.put("main.window.size.height", size.height.value.toString())
}

private fun onWindowRelocate(position: WindowPosition)
{
    // println("pos : onWindowRelocate $position " + position.x.value.toString() + " " + position.y.value.toString())
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

@Suppress("unused")
fun Modifier.randomDebugBorder2(): Modifier =
    Modifier.padding(3.dp).border(width = 4.dp,
        color = Color(
            Random().nextInt(0, 255),
            Random().nextInt(0, 255),
            Random().nextInt(0, 255)),
        shape = RectangleShape)

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


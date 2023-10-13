import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity.Companion.main_init
import com.zoffcc.applications.trifa.PrefsSettings
import com.zoffcc.applications.trifa.StateContacts
import com.zoffcc.applications.trifa.TrifaToxService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.briarproject.briar.desktop.contact.ContactList
import org.briarproject.briar.desktop.navigation.BriarSidebar
import org.briarproject.briar.desktop.ui.VerticalDivider
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import java.util.prefs.Preferences

private const val TAG = "trifa.Main.kt"

var tox_running_state_wrapper = "start"
var start_button_text_wrapper = "stopped"

var online_button_text_wrapper = "offline"
var online_button_color_wrapper = Color.White.toArgb()

var closing_application = false
private val prefs: Preferences =
    Preferences.userNodeForPackage(com.zoffcc.applications.trifa.PrefsSettings::class.java)

val TOP_HEADER_SIZE = 56.dp
val CONTACT_COLUMN_WIDTH = 230.dp

@Composable
@Preview
fun App() {
    var start_button_text by remember { mutableStateOf("start") }
    var tox_running_state: String by remember { mutableStateOf("stopped") }

    Log.i(TAG, "CCCC:" + PrefsSettings::class.java)

    var uiscale_default = LocalDensity.current.density

    try {
        val tmp = prefs.get("main.ui_scale_factor", null)
        if (tmp != null) {
            uiscale_default = tmp.toFloat()
        }
    } catch (_: Exception) {
    }

    MaterialTheme {
        Scaffold() {
            Row {
                BriarSidebar()
                VerticalDivider()
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.wrapContentHeight(), Arrangement.spacedBy(5.dp)) {
                        Button(modifier = Modifier.width(140.dp), onClick = { // start/stop tox button
                            if (tox_running_state == "running") {
                                tox_running_state = "stopping ..."
                                start_button_text = tox_running_state
                                tox_running_state_wrapper = tox_running_state
                                start_button_text_wrapper = start_button_text
                                Log.i(TAG, "----> tox_running_state = $tox_running_state_wrapper");
                                Thread {
                                    Log.i(TAG, "waiting to stop ...");
                                    while (tox_running_state_wrapper != "stopped") {
                                        Thread.sleep(100)
                                        Log.i(TAG, "waiting ...");
                                    }
                                    Log.i(TAG, "is stopped now");
                                    tox_running_state = tox_running_state_wrapper
                                    start_button_text = "start"
                                }.start()
                                TrifaToxService.stop_me = true
                            } else if (tox_running_state == "stopped") {
                                TrifaToxService.stop_me = false
                                tox_running_state = "starting ..."
                                start_button_text = tox_running_state
                                tox_running_state_wrapper = tox_running_state
                                start_button_text_wrapper = start_button_text
                                Log.i(TAG, "----> tox_running_state = $tox_running_state_wrapper");
                                Thread {
                                    Log.i(TAG, "waiting to startup ...");
                                    while (tox_running_state_wrapper != "running") {
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
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(),
                            enabled = false
                        ) {
                            Box(
                                modifier = Modifier.size(16.dp).border(1.dp, Color.Black, CircleShape)
                                    .background(
                                        Color(online_button_color_wrapper),
                                        CircleShape
                                    )
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(getOnlineButtonText(online_button_text))
                            Thread {
                                while (true) {
                                    Thread.sleep(200)
                                    if (online_button_text != online_button_text_wrapper) {
                                        online_button_text = online_button_text_wrapper
                                    }
                                }
                            }.start()
                        }
                    }
                    SaveDataPath()
                    ToxIDTextField()
                    // UIScaleSlider(uiscale_default)
                    val contacts by contactstore.stateFlow.collectAsState()
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ContactList(
                            contactList = contacts,
                        )
                        VerticalDivider()
                        ChatAppWithScaffold()
                    }
                }
            }
        }
    }

}

@Composable
private fun UIScaleSlider(uiscale_default: Float) {
    var ui_scale by remember { mutableStateOf(uiscale_default) }
    DetailItem(
        label = i18n("UI Scale"),
        description = "${i18n("current_value:")}: " + " " +
                ui_scale + ", " +
                i18n("drag Slider to change")
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(200.dp)
        ) {
            Icon(Icons.Default.FormatSize, null, Modifier.scale(0.7f))
            Slider(
                value = ui_scale ?: LocalDensity.current.density,
                onValueChange = {
                    ui_scale = it
                    prefs.putFloat("main.ui_scale_factor", ui_scale)
                    Log.i(TAG, "density: $ui_scale")
                },
                onValueChangeFinished = { },
                valueRange = 1f..3f,
                steps = 3,
                // todo: without setting the width explicitly,
                //  the slider takes up the whole remaining space
                modifier = Modifier.width(150.dp)
            )
            Icon(Icons.Default.FormatSize, null)
        }
    }
}

@Composable
private fun ToxIDTextField() {
    val toxdata by toxdatastore.stateFlow.collectAsState()
    TextField(
        enabled = true,
        readOnly = true,
        singleLine = true,
        textStyle = TextStyle(fontSize = 18.sp),
        modifier = Modifier.width(500.dp),
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false,
        ),
        value = toxdata.mytoxid,
        placeholder = {
            Text("my ToxID ...")
        },
        onValueChange = {
        }
    )
}

@Composable
private fun SaveDataPath() {
    val savepathdata by savepathstore.stateFlow.collectAsState()
    TextField(
        enabled = savepathdata.savePathEnabled,
        singleLine = true,
        textStyle = TextStyle(fontSize = 18.sp),
        modifier = Modifier.width(500.dp),
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false,
        ),
        value = savepathdata.savePath,
        placeholder = {
            Text("save file path ...")
        },
        onValueChange = {
            savepathstore.updatePath(it)
        }
    )
}

fun getOnlineButtonText(text_in: String): String {
    return when (text_in) {
        "udp" -> "UDP"
        "tcp" -> "TCP"
        else -> "offline"
    }
}

fun set_tox_running_state(new_state: String) {
    tox_running_state_wrapper = new_state
    start_button_text_wrapper = tox_running_state_wrapper
    Log.i(TAG, "----> tox_running_state = $tox_running_state_wrapper");
    if (tox_running_state_wrapper == "stopped") {
        online_button_color_wrapper = Color.White.toArgb()
        online_button_text_wrapper = "offline"
    }
}

fun set_tox_online_state(new_state: String) {
    online_button_text_wrapper = new_state
    if (online_button_text_wrapper == "udp") {
        online_button_color_wrapper = Color.Green.toArgb()
    } else if (online_button_text_wrapper == "tcp") {
        online_button_color_wrapper = Color.Yellow.toArgb()
    } else {
        online_button_color_wrapper = Color.Red.toArgb()
    }
    Log.i(TAG, "----> tox_online_state = $online_button_text_wrapper");
}

fun main() = application(exitProcessOnExit = true) {
    MainAppStart()
}

@Composable
private fun MainAppStart() {
    var showIntroScreen by remember { mutableStateOf(true) }
    try {
        val tmp = prefs.getBoolean("main.show_intro_screen", true)
        if (tmp == false) {
            showIntroScreen = false
        }
    } catch (_: Exception) {
    }
    // showIntroScreen = true

    val appIcon = painterResource("icon-linux.png")
    if (showIntroScreen) {
        // ----------- intro screen -----------
        // ----------- intro screen -----------
        // ----------- intro screen -----------
        var isOpen by remember { mutableStateOf(true) }
        var isAskingToClose by remember { mutableStateOf(false) }

        if (isOpen) {
            Window(
                onCloseRequest = { isAskingToClose = true },
                title = "TRIfA Material - Welcome",
                icon = appIcon
            )
            {
                Column(Modifier.fillMaxSize()) {
                    Button(onClick = {
                        prefs.putBoolean("main.show_intro_screen", false)
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

                    if (isAskingToClose) {
                        isOpen = false
                    }
                }
            }
        }
        // ----------- intro screen -----------
        // ----------- intro screen -----------
        // ----------- intro screen -----------
    } else {
        // ----------- main app screen -----------
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
        try {
            x_ = prefs.get("main.window.position.x", "").toFloat().dp
            y_ = prefs.get("main.window.position.y", "").toFloat().dp
            w_ = prefs.get("main.window.size.width", "").toFloat().dp
            h_ = prefs.get("main.window.size.height", "").toFloat().dp
        } catch (_: Exception) {
            error = 1
        }

        if (error == 0) {
            val wpos = WindowPosition(x = x_, y = y_)
            val wsize = DpSize(w_, h_)
            state = rememberWindowState(position = wpos, size = wsize)
        }

        if (isOpen) {
            Window(
                onCloseRequest = { isAskingToClose = true },
                title = "TRIfA",
                icon = appIcon,
                state = state
            ) {
                if (isAskingToClose) {
                    Dialog(
                        onCloseRequest = { isAskingToClose = false },
                        title = i18n("Close TRIfA ?"),
                    ) {
                        Button(
                            onClick = {
                                if (tox_running_state_wrapper == "running") {
                                    set_tox_running_state("stopping ...")
                                    TrifaToxService.stop_me = true
                                    runBlocking(Dispatchers.Default) {
                                        Log.i(TAG, "waiting to shutdown ...");
                                        while (tox_running_state_wrapper != "stopped") {
                                            delay(100)
                                            Log.i(TAG, "waiting ...");
                                        }
                                        Log.i(TAG, "closing application");
                                        closing_application = true
                                        isOpen = false
                                    }
                                } else {
                                    Log.i(TAG, "closing application");
                                    isOpen = false
                                    closing_application = true
                                }
                            }
                        ) {
                            Text(i18n("Yes"))
                        }
                    }
                }

                LaunchedEffect(state) {
                    snapshotFlow { state.size }
                        .onEach(::onWindowResize)
                        .launchIn(this)

                    snapshotFlow { state.position }
                        .filter { it.isSpecified }
                        .onEach(::onWindowRelocate)
                        .launchIn(this)
                }
                App()
            }
        }

        // ----------- main app screen -----------
        // ----------- main app screen -----------
        // ----------- main app screen -----------
    }
}

@Suppress("UNUSED_PARAMETER")
private fun onWindowResize(size: DpSize) {
    // println("onWindowResize $size")
    prefs.put("main.window.size.width", size.width.value.toString())
    prefs.put("main.window.size.height", size.height.value.toString())
}

@Suppress("UNUSED_PARAMETER")
private fun onWindowRelocate(position: WindowPosition) {
    // println("onWindowRelocate $position")
    prefs.put("main.window.position.x", position.x.value.toString())
    prefs.put("main.window.position.y", position.y.value.toString())
}

@Composable
fun DetailItem(
    label: String,
    description: String,
    setting: @Composable (RowScope.() -> Unit),
) = Row(
    Modifier
        .fillMaxWidth().height(TOP_HEADER_SIZE).padding(horizontal = 16.dp)
        .semantics(mergeDescendants = true) {
            // it would be nicer to derive the contentDescriptions from the descendants automatically
            // which is currently not supported in Compose for Desktop
            // see https://github.com/JetBrains/compose-jb/issues/2111
            contentDescription = description
        },
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    Text(label)
    setting()
}

fun unlock_data_dir_input() {
    savepathstore.updateEnabled(true)
}

fun lock_data_dir_input() {
    savepathstore.updateEnabled(false)
}

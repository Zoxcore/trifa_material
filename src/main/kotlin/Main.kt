import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.russhwolf.settings.Settings
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity.Companion.main_init
import com.zoffcc.applications.trifa.TrifaToxService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

private const val TAG = "trifa.Main.kt"

var tox_running_state_wrapper = "start"
var start_button_text_wrapper = "stopped"

var online_button_text_wrapper = "offline"
var online_button_color_wrapper = Color.White.toArgb()

var closing_application = false

val settings: Settings = Settings()
val HEADER_SIZE = 56.dp

@Composable
@Preview
fun App() {
    var start_button_text by remember { mutableStateOf("start") }
    var tox_running_state: String by remember { mutableStateOf("stopped") }

    var online_button_text by remember { mutableStateOf("offline") }
    var online_button_color by remember { mutableStateOf(Color.White.toArgb()) }

    var uiscale_default = LocalDensity.current.density

    try {
        val tmp = settings.getFloatOrNull("main.ui_scale_factor")
        if (tmp != null) {
            uiscale_default = tmp
        }
    } catch (_: Exception) {
    }

    var ui_scale by remember { mutableStateOf(uiscale_default) }

    MaterialTheme {
        Scaffold() {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.wrapContentHeight(), Arrangement.spacedBy(5.dp)) {
                    Button(onClick = {
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
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(),
                        enabled = true
                    ) {
                        Icon(
                            Icons.Filled.Add, contentDescription = online_button_text,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(online_button_text)

                        Thread {
                            while (true) {
                                Thread.sleep(100)
                                if (online_button_text != online_button_text_wrapper) {
                                    online_button_text = online_button_text_wrapper
                                    // online_button_color = online_button_color_wrapper
                                }
                            }
                        }.start()
                    }
                }

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
                                settings.putFloat("main.ui_scale_factor", ui_scale)
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



                ChatAppWithScaffold()
            }
        }
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
    var showIntroScreen by remember { mutableStateOf(true) }
    try {
        val tmp = settings.getBooleanOrNull("main.show_intro_screen")
        if (tmp == false) {
            showIntroScreen = false
        }
    } catch (_: Exception) {
    }

    if (showIntroScreen) {
        // ----------- intro screen -----------
        // ----------- intro screen -----------
        // ----------- intro screen -----------
        var isOpen by remember { mutableStateOf(true) }
        var isAskingToClose by remember { mutableStateOf(false) }

        if (isOpen) {
            Window(
                onCloseRequest = { isAskingToClose = true },
                title = "TRIfA Material - Welcome"
            )
            {
                Text("Welcome to TRIfA Material")
                Button(onClick = {
                    settings.putBoolean("main.show_intro_screen", false)
                    showIntroScreen = false
                    isOpen = false
                }) {
                }
                if (isAskingToClose) {
                    isOpen = false
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
            x_ = settings.getString("main.window.position.x", "").toFloat().dp
            y_ = settings.getString("main.window.position.y", "").toFloat().dp
            w_ = settings.getString("main.window.size.width", "").toFloat().dp
            h_ = settings.getString("main.window.size.height", "").toFloat().dp
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
    settings.putString("main.window.size.width", size.width.value.toString())
    settings.putString("main.window.size.height", size.height.value.toString())
}

@Suppress("UNUSED_PARAMETER")
private fun onWindowRelocate(position: WindowPosition) {
    // println("onWindowRelocate $position")
    settings.putString("main.window.position.x", position.x.value.toString())
    settings.putString("main.window.position.y", position.y.value.toString())
}

@Composable
fun DetailItem(
    label: String,
    description: String,
    setting: @Composable (RowScope.() -> Unit),
) = Row(
    Modifier
        .fillMaxWidth().height(HEADER_SIZE).padding(horizontal = 16.dp)
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
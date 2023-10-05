package com.zoffcc.applications.trifa

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.zoffcc.applications.trifa.MainActivity.Companion.main_init
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

private const val TAG = "trifa.Main.kt"

var tox_running_state_wrapper = "start"
var start_button_text_wrapper = "stopped"

var online_button_text_wrapper = "offline"
var online_button_color_wrapper = Color.White.toArgb()

@Composable
@Preview
fun App() {
    var start_button_text by remember { mutableStateOf("start") }
    var tox_running_state: String by remember { mutableStateOf("stopped") }

    var online_button_text by remember { mutableStateOf("offline") }
    var online_button_color by remember { mutableStateOf(Color.White.toArgb()) }

    MaterialTheme {
        Row(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
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

            Button(onClick = {},
                colors = ButtonDefaults.buttonColors(),
                enabled = true) {
                Icon(
                    Icons.Filled.Add, contentDescription = online_button_text,
                    modifier = Modifier.size(ButtonDefaults.IconSize))
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
    var isOpen by remember { mutableStateOf(true) }
    var isAskingToClose by remember { mutableStateOf(false) }
    val state = rememberWindowState()

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
                                    isOpen = false
                                }
                            } else {
                                Log.i(TAG, "closing application");
                                isOpen = false
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
}

@Suppress("UNUSED_PARAMETER")
private fun onWindowResize(size: DpSize) {
    // println("onWindowResize $size")
}

@Suppress("UNUSED_PARAMETER")
private fun onWindowRelocate(position: WindowPosition) {
    // println("onWindowRelocate $position")
}
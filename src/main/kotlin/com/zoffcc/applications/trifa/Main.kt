package com.zoffcc.applications.trifa

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import java.time.Duration

private const val TAG = "trifa.Main.kt"

var tox_running_state_wrapper = "start"
var start_button_text_wrapper = "stopped"

@Composable
@Preview
fun App() {
    var start_button_text by remember { mutableStateOf("start") }
    var tox_running_state: String by remember { mutableStateOf("stopped") }

    MaterialTheme {
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
                MainActivity.main_init()
            }
        }) {
            Text(start_button_text)
        }
    }
}

fun set_tox_running_state(new_state: String) {
    tox_running_state_wrapper = new_state
    start_button_text_wrapper = tox_running_state_wrapper
    Log.i(TAG, "----> tox_running_state = $tox_running_state_wrapper");
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
                    title = "Close TRIfA ?",
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
                        Text("Yes")
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

private fun onWindowResize(size: DpSize) {
    println("onWindowResize $size")
}

private fun onWindowRelocate(position: WindowPosition) {
    println("onWindowRelocate $position")
}
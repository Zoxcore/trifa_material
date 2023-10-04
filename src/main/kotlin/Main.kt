package com.zoffcc.applications.trifa

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File

private const val TAG = "trifa.Main.kt"

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    MainActivity.main_init()

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
    }
}

fun main() = application(exitProcessOnExit = true) {
    Window(onCloseRequest = ::exitApplication, title = "TRIfA") {
        App()
    }
}

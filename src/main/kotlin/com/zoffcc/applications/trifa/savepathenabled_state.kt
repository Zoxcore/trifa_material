package com.zoffcc.applications.trifa

import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__database_files_dir
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__tox_savefile_dir
import com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR
import com.zoffcc.applications.trifa.TRIFAGlobals.VFS_TMP_FILE_DIR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.io.File

data class globalstore_state(
    val mainwindow_minimized: Boolean = false,
    val mainwindow_focused: Boolean = true
)

interface GlobalStore {
    fun updateMinimized(value: Boolean)
    fun updateFocused(value: Boolean)
    fun isMinimized(): Boolean
    fun isFocused(): Boolean
    val stateFlow: StateFlow<globalstore_state>
    val state get() = stateFlow.value
}

fun CoroutineScope.createGlobalStore(): GlobalStore {
    val mutableStateFlow = MutableStateFlow(globalstore_state())
    return object : GlobalStore
    {
        override val stateFlow: StateFlow<globalstore_state> = mutableStateFlow

        override fun updateMinimized(value: Boolean)
        {
            mutableStateFlow.value = state.copy(mainwindow_minimized = value)
        }

        override fun updateFocused(value: Boolean)
        {
            mutableStateFlow.value = state.copy(mainwindow_focused = value)
        }

        override fun isMinimized(): Boolean
        {
            return state.mainwindow_minimized
        }

        override fun isFocused(): Boolean
        {
            return state.mainwindow_focused
        }
    }
}

data class savepathenabled_state(
    val savePathEnabled: Boolean = true,
    val savePath: String = File(MainActivity.PREF__tox_savefile_dir).canonicalPath + File.separator
)

interface SavepathStore {
    fun updatePath(p: String)
    fun createPathDirectories()
    fun updateEnabled(e: Boolean)
    fun isEnabled(): Boolean
    val stateFlow: StateFlow<savepathenabled_state>
    val state get() = stateFlow.value
}

fun CoroutineScope.createSavepathStore(): SavepathStore {
    val mutableStateFlow = MutableStateFlow(savepathenabled_state())
    val channelPath: Channel<String> = Channel(Channel.UNLIMITED)
    val channelEnabled: Channel<Boolean> = Channel(Channel.UNLIMITED)

    return object : SavepathStore {
        override val stateFlow: StateFlow<savepathenabled_state> = mutableStateFlow

        override fun updatePath(p: String) {
            launch {
                channelPath.send(p)
            }
        }
        override fun createPathDirectories()
        {
            try
            {
                val dir_file = File(PREF__tox_savefile_dir)
                dir_file.mkdirs()
            }
            catch(e: Exception)
            {
                Log.i(TAG, "error creating savefile directory and parents: " + PREF__tox_savefile_dir)
            }
        }

        override fun updateEnabled(e: Boolean) {
            launch {
                channelEnabled.send(e)
            }
        }
        override fun isEnabled(): Boolean
        {
            return state.savePathEnabled
        }

        init {
            launch {
                channelEnabled.consumeAsFlow().collect { item ->
                    mutableStateFlow.value =
                        state.copy(
                            savePathEnabled = item,
                            savePath = state.savePath
                        )
                }
            }
            launch {
                channelPath.consumeAsFlow().collect { item ->
                    try
                    {
                        val dir_file = File(item)
                        PREF__tox_savefile_dir = item
                        PREF__database_files_dir = item
                        VFS_TMP_FILE_DIR = PREF__tox_savefile_dir + File.separator + "/tempdir/files/"
                        VFS_FILE_DIR = PREF__tox_savefile_dir + File.separator + "/datadir/files/"
                        mutableStateFlow.value =
                            state.copy(
                                savePathEnabled = state.savePathEnabled,
                                savePath = item
                            )
                    }
                    catch(e: Exception)
                    {
                        Log.i(TAG, "error setting savefile dir: " + item)
                    }
                }
            }

        }
    }
}

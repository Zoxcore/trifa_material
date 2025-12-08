@file:Suppress("FunctionName", "PropertyName", "ClassName")

package com.zoffcc.applications.trifa

import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__database_files_dir
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__tox_savefile_dir
import com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR
import com.zoffcc.applications.trifa.TRIFAGlobals.VFS_TMP_FILE_DIR
import global_prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.io.File

enum class SQLITE_TYPE(val type: Int) {
    UNLOADED(0),
    SQLITE(1),
    SQLCIPHER(2),
}

data class globalstore_state(
    val mainwindow_minimized: Boolean = false,
    val mainwindow_focused: Boolean = true,
    val contacts_unread_message_count: Int = 0,
    val contacts_unread_group_message_count: Int = 0,
    val firstRun: Boolean = false,
    val peerListCollapse: Boolean = false,
    val startupSelfname: String = "",
    val ui_scale: Float = 1.0f,
    val ui_density: Float = 1.0f,
    val default_density: Float = 1.0f,
    val toxRunning: Boolean = false,
    val ormaRunning: Boolean = false,
    val native_ffmpegav_lib_loaded: Boolean = false,
    val native_notification_lib_loaded: Boolean = false,
    val native_sqlite_type: SQLITE_TYPE = SQLITE_TYPE.UNLOADED,
    val app_startup: Boolean = true
)

private val globalstore_state_lock = Any()

interface GlobalStore {
    fun updateMinimized(value: Boolean)
    fun updateFocused(value: Boolean)
    fun updateFirstRun(value: Boolean)
    fun updateStartupSelfname(value: String)
    fun updateUiScale(value: Float)
    fun updatePeerListCollapse(value: Boolean)
    fun updateUiDensity(value: Float)
    fun setDefaultDensity(value: Float)
    fun isMinimized(): Boolean
    fun isFocused(): Boolean
    fun isFirstRun(): Boolean
    fun getStartupSelfname(): String
    fun loadUiScale()
    fun getUiScale(): Float
    fun isPeerListCollapse(): Boolean
    fun loadUiDensity()
    fun getUiDensity(): Float
    fun setToxRunning(value: Boolean)
    fun getToxRunning(): Boolean
    fun setNative_ffmpegav_lib_loaded(value: Boolean)
    fun getNative_ffmpegav_lib_loaded(): Boolean
    fun setApp_startup(value: Boolean)
    fun getApp_startup(): Boolean
    fun setNative_notification_lib_loaded(value: Boolean)
    fun getNative_notification_lib_loaded(): Boolean
    fun setNative_sqlite_type(value: SQLITE_TYPE)
    fun getNative_sqlite_type(): SQLITE_TYPE
    fun setOrmaRunning(value: Boolean)
    fun getOrmaRunning(): Boolean
    fun increase_unread_message_count()
    fun get_unread_message_count(): Int
    fun try_clear_unread_message_count()
    fun hard_clear_unread_message_count()
    fun increase_unread_group_message_count()
    fun get_unread_group_message_count(): Int
    fun try_clear_unread_group_message_count()
    fun hard_clear_unread_group_message_count()
    val stateFlow: StateFlow<globalstore_state>
    val state get() = stateFlow.value
}

@OptIn(DelicateCoroutinesApi::class)
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

        override fun updateFirstRun(value: Boolean)
        {
            mutableStateFlow.value = state.copy(firstRun = value)
        }

        override fun updatePeerListCollapse(value: Boolean)
        {
            mutableStateFlow.value = state.copy(peerListCollapse = value)
        }

        override fun updateStartupSelfname(value: String)
        {
            mutableStateFlow.value = state.copy(startupSelfname = value)
        }

        override fun updateUiScale(value: Float)
        {
            GlobalScope.launch {
                try
                {
                    global_prefs.putFloat("main.ui_scale_factor", value)
                }
                catch(_: Exception)
                {
                }
            }
            mutableStateFlow.value = state.copy(ui_scale = value)
        }

        override fun setDefaultDensity(value: Float)
        {
            mutableStateFlow.value = state.copy(default_density = value)
        }

        override fun updateUiDensity(value: Float)
        {
            GlobalScope.launch {
                try
                {
                    global_prefs.putFloat("main.ui_density_factor", value)
                }
                catch(_: Exception)
                {
                }
            }
            mutableStateFlow.value = state.copy(ui_density = value)
        }

        override fun isMinimized(): Boolean
        {
            return state.mainwindow_minimized
        }

        override fun isFocused(): Boolean
        {
            return state.mainwindow_focused
        }

        override fun isFirstRun(): Boolean
        {
            return state.firstRun
        }

        override fun isPeerListCollapse(): Boolean
        {
            return state.peerListCollapse
        }

        override fun getStartupSelfname(): String
        {
            return state.startupSelfname
        }

        override fun getToxRunning(): Boolean
        {
            return state.toxRunning
        }

        override fun setToxRunning(value: Boolean)
        {
            mutableStateFlow.value = state.copy(toxRunning = value)
        }

        override fun getNative_ffmpegav_lib_loaded(): Boolean
        {
            return state.native_ffmpegav_lib_loaded
        }

        override fun setNative_ffmpegav_lib_loaded(value: Boolean)
        {
            mutableStateFlow.value = state.copy(native_ffmpegav_lib_loaded = value)
        }

        override fun getNative_notification_lib_loaded(): Boolean
        {
            return state.native_notification_lib_loaded
        }

        override fun getNative_sqlite_type(): SQLITE_TYPE
        {
            return state.native_sqlite_type
        }

        override fun setApp_startup(value: Boolean)
        {
            mutableStateFlow.value = state.copy(app_startup = value)
            Log.i(TAG, "setApp_startup: " + value)
        }

        override fun getApp_startup(): Boolean
        {
            return state.app_startup
        }

        override fun setNative_notification_lib_loaded(value: Boolean)
        {
            mutableStateFlow.value = state.copy(native_notification_lib_loaded = value)
        }

        override fun setNative_sqlite_type(value: SQLITE_TYPE)
        {
            mutableStateFlow.value = state.copy(native_sqlite_type = value)
        }

        override fun getOrmaRunning(): Boolean
        {
            return state.ormaRunning
        }

        override fun setOrmaRunning(value: Boolean)
        {
            mutableStateFlow.value = state.copy(ormaRunning = value)
        }

        override fun loadUiScale()
        {
            var value = 1.0f
            try
            {
                val tmp = global_prefs.get("main.ui_scale_factor", null)
                if (tmp != null)
                {
                    value = tmp.toFloat()
                    Log.i(TAG, "loadUiScale:density: $value")
                }
            } catch (_: Exception)
            {
            }
            mutableStateFlow.value = state.copy(ui_scale = value)
        }

        override fun loadUiDensity()
        {
            var value = 1.0f
            try
            {
                value = state.default_density
                Log.i(TAG, "current default density = " + value)
            }
            catch(_: Exception)
            {
            }
            try
            {
                val tmp = global_prefs.get("main.ui_density_factor", null)
                if (tmp != null)
                {
                    value = tmp.toFloat()
                    Log.i(TAG, "loadUiDensity:density: $value")
                }
            } catch (_: Exception)
            {
            }
            Log.i(TAG, "loading density = " + value)
            mutableStateFlow.value = state.copy(ui_density = value)
        }

        override fun getUiScale(): Float
        {
            return state.ui_scale
        }

        override fun getUiDensity(): Float
        {
            return state.ui_density
        }

        override fun increase_unread_message_count()
        {
            mutableStateFlow.value = state.copy(contacts_unread_message_count = (state.contacts_unread_message_count + 1))
        }

        override fun get_unread_message_count(): Int
        {
            return state.contacts_unread_message_count
        }

        override fun try_clear_unread_message_count()
        {
            synchronized(globalstore_state_lock) {
                var unread_count = 0
                try
                {
                    if (state.ormaRunning)
                    {
                        unread_count = TrifaToxService.orma!!.selectFromMessage()
                            .directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value)
                            .is_newEq(true).count()
                        if (unread_count > 0)
                        {
                            // Log.i(TAG, "try_clear_unread_message_count:unread_count=" + unread_count)
                        }
                    }
                } catch (e: Exception)
                {
                    e.printStackTrace()
                }
                mutableStateFlow.value = state.copy(contacts_unread_message_count = unread_count)
            }
        }

        override fun hard_clear_unread_message_count()
        {
            mutableStateFlow.value = state.copy(contacts_unread_message_count = 0)
        }

        override fun increase_unread_group_message_count()
        {
            mutableStateFlow.value = state.copy(contacts_unread_group_message_count = (state.contacts_unread_group_message_count + 1))
        }

        override fun get_unread_group_message_count(): Int
        {
            return state.contacts_unread_group_message_count
        }

        override fun try_clear_unread_group_message_count()
        {
            var unread_count = 0
            try
            {
                if (state.ormaRunning)
                {
                    unread_count = TrifaToxService.orma!!.selectFromGroupMessage()
                        .directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value)
                        .is_newEq(true).count()
                    if (unread_count != 0)
                    {
                        // Log.i(TAG, "try_clear_unread_group_message_count:unread_count=" + unread_count)
                    }
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            mutableStateFlow.value = state.copy(contacts_unread_group_message_count = unread_count)
        }

        override fun hard_clear_unread_group_message_count()
        {
            mutableStateFlow.value = state.copy(contacts_unread_group_message_count = 0)
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

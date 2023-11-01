package com.zoffcc.applications.trifa

import global_prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AVState(val a: Int)
{
    enum class CALL_STATUS {
        CALL_NONE,
        CALL_OUTGOING,
        CALL_INCOMING,
        CALL_CALLING,
        CALL_ENDING
    }
    enum class CALLVIDEO {
        CALLVIDEO_NONE,
        CALLVIDEO_INCOMING,
        CALLVIDEO_CALLING,
        CALLVIDEO_ENDING
    }
    enum class CALLAUDIO {
        CALLAUDIO_NONE,
        CALLAUDIO_INCOMING,
        CALLAUDIO_CALLING,
        CALLAUDIO_ENDING
    }
    var ffmpeg_init_done: Boolean = false
    private var audio_in_device = ""
    private var audio_in_source = ""
    private var video_in_device = ""
    private var video_in_source = ""
    var calling_state = CALL_STATUS.CALL_NONE
    var calling_vstate = CALLVIDEO.CALLVIDEO_NONE
    var calling_astate = CALLAUDIO.CALLAUDIO_NONE
    var call_with_friend_pubkey: String? = null

    init
    {
        load_device_information()
    }

    fun audio_in_device_get(): String
    {
        return if (audio_in_device == null) "" else audio_in_device
    }

    fun audio_in_source_get(): String
    {
        return if (audio_in_source == null) "" else audio_in_source
    }

    fun video_in_device_get(): String
    {
        return if (video_in_device == null) "" else video_in_device
    }

    fun video_in_source_get(): String
    {
        return if (video_in_source == null) "" else video_in_source
    }

    fun audio_in_device_set(value: String?)
    {
        if (value == null)
        {
            audio_in_device = ""
        }
        else
        {
            audio_in_device = value
        }
        save_device_information()
    }

    fun audio_in_source_set(value: String?)
    {
        if (value == null)
        {
            audio_in_source = ""
        }
        else
        {
            audio_in_source = value
        }
        save_device_information()
    }

    fun video_in_device_set(value: String?)
    {
        if (value == null)
        {
            video_in_device = ""
        }
        else
        {
            video_in_device = value
        }
        save_device_information()
    }

    fun video_in_source_set(value: String?)
    {
        if (value == null)
        {
            video_in_source = ""
        }
        else
        {
            video_in_source = value
        }
        save_device_information()
    }

    fun load_device_information()
    {
        try
        {
            val tmp = global_prefs.get("main.av.audio_in_device", "")
            if (tmp != null)
            {
                audio_in_device = tmp
            }
        } catch (_: Exception)
        {
        }

        try
        {
            val tmp = global_prefs.get("main.av.audio_in_source", "")
            if (tmp != null)
            {
                audio_in_source = tmp
            }
        } catch (_: Exception)
        {
        }

        try
        {
            val tmp = global_prefs.get("main.av.video_in_device", "")
            if (tmp != null)
            {
                video_in_device = tmp
            }
        } catch (_: Exception)
        {
        }

        try
        {
            val tmp = global_prefs.get("main.av.video_in_source", "")
            if (tmp != null)
            {
                video_in_source = tmp
            }
        } catch (_: Exception)
        {
        }
    }

    fun save_device_information()
    {
        try
        {
            global_prefs.put("main.av.audio_in_device", audio_in_device)
        } catch (_: Exception)
        {
        }
        try
        {
            global_prefs.put("main.av.audio_in_source", audio_in_source)
        } catch (_: Exception)
        {
        }
        try
        {
            global_prefs.put("main.av.video_in_device", video_in_device)
        } catch (_: Exception)
        {
        }
        try
        {
            global_prefs.put("main.av.video_in_source", video_in_source)
        } catch (_: Exception)
        {
        }
    }
}

interface AVStateStore
{
    val stateFlow: StateFlow<AVState>
    val state get() = stateFlow.value
}

fun CoroutineScope.createAVStateStore(): AVStateStore
{
    val mutableStateFlow = MutableStateFlow(AVState(1))

    return object : AVStateStore
    {
        override val stateFlow: StateFlow<AVState> = mutableStateFlow
    }
}
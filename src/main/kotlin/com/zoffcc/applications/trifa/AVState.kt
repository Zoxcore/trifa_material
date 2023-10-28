package com.zoffcc.applications.trifa

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
    var audio_in_device = ""
    var audio_in_source = ""
    var video_in_device = ""
    var video_in_source = ""
    var calling_state = CALL_STATUS.CALL_NONE
    var calling_vstate = CALLVIDEO.CALLVIDEO_NONE
    var calling_astate = CALLAUDIO.CALLAUDIO_NONE

    init
    {
    }
}
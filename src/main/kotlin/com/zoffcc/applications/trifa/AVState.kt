package com.zoffcc.applications.trifa

import CAPTURE_VIDEO_FPS
import CAPTURE_VIDEO_HEIGHT
import CAPTURE_VIDEO_WIDTH
import avstatestorecallstate
import com.zoffcc.applications.ffmpegav.AVActivity
import com.zoffcc.applications.ffmpegav.AVActivity.ffmpegav_init
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__audio_play_volume_percent
import com.zoffcc.applications.trifa.MainActivity.Companion.set_audio_play_volume_percent
import global_prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.concurrent.Semaphore

data class AVStateCallState(val call_state: AVState.CALL_STATUS = AVState.CALL_STATUS.CALL_STATUS_NONE)

data class AVState(val a: Int)
{
    enum class CALL_DEVICES_STATE {
        CALL_DEVICES_STATE_CLOSED,
        CALL_DEVICES_STATE_ACTIVE,
    }

    enum class CALL_STATUS {
        CALL_STATUS_NONE,
        CALL_STATUS_OUTGOING,
        CALL_STATUS_INCOMING,
        CALL_STATUS_CALLING,
        CALL_STATUS_ENDING
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
    private var ffmpeg_init_done: Boolean = false
    private var audio_in_device = ""
    private var audio_in_source = ""
    private var video_in_device = ""
    private var video_in_source = ""
    var calling_state = CALL_STATUS.CALL_STATUS_NONE
    private var devices_state = CALL_DEVICES_STATE.CALL_DEVICES_STATE_CLOSED
    private var call_with_friend_pubkey: String? = null
    private var semaphore_avstate = CustomSemaphore(1)

    init
    {
        devices_state = CALL_DEVICES_STATE.CALL_DEVICES_STATE_CLOSED
        load_device_information()
        ffmpeg_init_done_set(false)
    }

    fun ffmpeg_devices_start()
    {
        val devices_state_copy: CALL_DEVICES_STATE
        semaphore_avstate.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
        devices_state_copy = devices_state
        semaphore_avstate.release()
        if (devices_state_copy == CALL_DEVICES_STATE.CALL_DEVICES_STATE_CLOSED)
        {
            if ((video_in_device != null) && (video_in_device != ""))
            {
                if ((video_in_source != null) && (video_in_source != ""))
                {
                    println("ffmpeg video in device: " + video_in_device + " " + video_in_source)
                    val res_vd = AVActivity.ffmpegav_open_video_in_device(video_in_device, video_in_source,
                        CAPTURE_VIDEO_WIDTH, CAPTURE_VIDEO_HEIGHT, CAPTURE_VIDEO_FPS)
                    println("ffmpeg open video capture device: $res_vd")
                }
            }
            if ((audio_in_device != null) && (audio_in_device != ""))
            {
                if ((audio_in_source != null) && (audio_in_source != ""))
                {
                    println("ffmpeg audio in device: " + audio_in_device + " " + audio_in_source)
                    val res_ad = AVActivity.ffmpegav_open_audio_in_device(audio_in_device, audio_in_source)
                    println("ffmpeg open audio capture device: $res_ad")
                }
            }
            AVActivity.ffmpegav_start_video_in_capture()
            AVActivity.ffmpegav_start_audio_in_capture()
        }
        semaphore_avstate.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
        devices_state = CALL_DEVICES_STATE.CALL_DEVICES_STATE_ACTIVE
        semaphore_avstate.release()
    }

    fun ffmpeg_devices_stop()
    {
        val devices_state_copy: CALL_DEVICES_STATE
        semaphore_avstate.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
        devices_state_copy = devices_state
        semaphore_avstate.release()
        if (devices_state_copy == CALL_DEVICES_STATE.CALL_DEVICES_STATE_ACTIVE)
        {
            AVActivity.ffmpegav_stop_audio_in_capture()
            AVActivity.ffmpegav_close_audio_in_device()
            AVActivity.ffmpegav_stop_video_in_capture()
            AVActivity.ffmpegav_close_video_in_device()
            AudioBar.set_cur_value(0, AudioBar.audio_in_bar)
            AudioBar.set_cur_value(0, AudioBar.audio_out_bar)
            VideoOutFrame.clear_video_out_frame()
            VideoInFrame.clear_video_in_frame()
        }
        semaphore_avstate.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
        devices_state = CALL_DEVICES_STATE.CALL_DEVICES_STATE_CLOSED
        semaphore_avstate.release()
    }

    fun ffmpeg_init_do()
    {
        if (!ffmpeg_init_done_get())
        {
            val res = ffmpegav_init()
            println("==================ffmpeg init: $res")
            ffmpeg_init_done_set(true)
        }
    }

    fun call_with_friend_pubkey_get(): String?
    {
        return call_with_friend_pubkey
    }

    fun call_with_friend_pubkey_set(value: String?)
    {
        call_with_friend_pubkey = value
    }

    fun calling_state_get(): CALL_STATUS
    {
        return calling_state
    }

    fun calling_state_set(value: CALL_STATUS)
    {
        calling_state = value
        avstatestorecallstate.update(status = value)
    }

    fun ffmpeg_init_done_get(): Boolean
    {
        return ffmpeg_init_done
    }

    fun ffmpeg_init_done_set(value: Boolean)
    {
        ffmpeg_init_done = value
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

    fun restart_devices()
    {
        val devices_state_copy: CALL_DEVICES_STATE
        semaphore_avstate.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
        devices_state_copy = devices_state
        semaphore_avstate.release()
        if (devices_state_copy == CALL_DEVICES_STATE.CALL_DEVICES_STATE_ACTIVE)
        {
            ffmpeg_devices_stop()
            ffmpeg_devices_start()
        }
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
        restart_devices()
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
        restart_devices()
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
        restart_devices()
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
        restart_devices()
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

    fun start_av_call()
    {
        val friendpubkey = call_with_friend_pubkey

        if (friendpubkey == null)
        {
            println("start_outgoing_video: friend pubkey is null!! ERROR !!");
            return
        }

        MainActivity.set_av_call_status(1)
        set_audio_play_volume_percent(PREF__audio_play_volume_percent)

        println("___________start_outgoing_video_____________")
        ffmpeg_init_do()

        val devices_state_copy: CALL_DEVICES_STATE
        semaphore_avstate.acquire()
        devices_state_copy = devices_state
        semaphore_avstate.release()
        if (devices_state_copy == CALL_DEVICES_STATE.CALL_DEVICES_STATE_CLOSED)
        {
            val video_in_device = video_in_device_get()
            val video_in_source = video_in_source_get()
            var video_buffer_2: ByteBuffer? = null

            if ((video_in_device != null) && (video_in_device != ""))
            {
                if ((video_in_source != null) && (video_in_source != ""))
                {
                    println("ffmpeg video in device: " + video_in_device + " " + video_in_source)
                    val res_vd = AVActivity.ffmpegav_open_video_in_device(video_in_device, video_in_source,
                        CAPTURE_VIDEO_WIDTH, CAPTURE_VIDEO_HEIGHT, CAPTURE_VIDEO_FPS)
                    println("ffmpeg open video capture device: $res_vd")
                }
            }
            val frame_width_px1 = CAPTURE_VIDEO_WIDTH
            val frame_height_px1 = CAPTURE_VIDEO_HEIGHT
            val buffer_size_in_bytes1 = (frame_width_px1 * frame_height_px1 * 3) / 2
            val video_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes1)
            AVActivity.ffmpegav_set_JNI_video_buffer(video_buffer_1, frame_width_px1, frame_height_px1)
            val frame_width_px2 = CAPTURE_VIDEO_WIDTH
            val frame_height_px2 = CAPTURE_VIDEO_HEIGHT
            val y_size = frame_width_px2 * frame_height_px2
            val u_size = (frame_width_px2 * frame_height_px2 / 4)
            val v_size = (frame_width_px2 * frame_height_px2 / 4)
            val video_buffer_2_y = ByteBuffer.allocateDirect(y_size)
            val video_buffer_2_u = ByteBuffer.allocateDirect(u_size)
            val video_buffer_2_v = ByteBuffer.allocateDirect(v_size)
            AVActivity.ffmpegav_set_JNI_video_buffer2(video_buffer_2_y, video_buffer_2_u, video_buffer_2_v, frame_width_px2, frame_height_px2)
            val audio_in_device = audio_in_device_get()
            val audio_in_source = audio_in_source_get()
            var audio_buffer_2: ByteBuffer? = null

            if ((audio_in_device != null) && (audio_in_device != ""))
            {
                if ((audio_in_source != null) && (audio_in_source != ""))
                {
                    println("ffmpeg audio in device: " + audio_in_device + " " + audio_in_source)
                    val res_ad = AVActivity.ffmpegav_open_audio_in_device(audio_in_device, audio_in_source)
                    println("ffmpeg open audio capture device: $res_ad")
                }
            }
            val buffer_size_in_bytes2 = 50000 // TODO: don't hardcode this
            val audio_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes2)
            AVActivity.ffmpegav_set_JNI_audio_buffer2(audio_buffer_1)

            AVActivity.ffmpegav_set_audio_capture_callback(object : AVActivity.audio_capture_callback
            {
                override fun onSuccess(read_bytes: Long, out_samples: Int, out_channels: Int, out_sample_rate: Int, pts: Long)
                { // Log.i(TAG, "ffmpeg open audio capture onSuccess: $read_bytes $out_samples $out_channels $out_sample_rate $pts")
                    if ((audio_buffer_2 == null))
                    {
                        audio_buffer_2 = ByteBuffer.allocateDirect(buffer_size_in_bytes2)
                        MainActivity.set_JNI_audio_buffer(audio_buffer_2)
                    }/* DEBUG ONLY ----------------------------
                    try
                    {
                        audio_buffer_1!!.rewind()
                        val audio_buffer_2_ = ffmpegav_ByteBufferCompat(audio_buffer_1)
                        Log.i(TAG, "audiobytes1:" + AVActivity.bytesToHex(audio_buffer_2_.array(), 0, 100))
                    } catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                    DEBUG ONLY ---------------------------- */

                    audio_buffer_2!!.rewind()
                    audio_buffer_1!!.rewind()
                    audio_buffer_2!!.put(audio_buffer_1) // can we cache that? what if a friend gets deleted while in a call? and the friend number changes?
                    val friendnum = MainActivity.tox_friend_by_public_key(friendpubkey)
                    val tox_audio_res = MainActivity.toxav_audio_send_frame(friend_number = friendnum, sample_count = out_samples.toLong(), channels = out_channels, sampling_rate = out_sample_rate.toLong()) // Log.i(TAG, "tox_audio_res=" + tox_audio_res)
                    val sample_count_: Int = out_samples
                    val t_audio_bar_set: Thread = object : Thread()
                    {
                        override fun run()
                        {
                            var global_audio_in_vu: Float = MainActivity.AUDIO_VU_MIN_VALUE
                            if (sample_count_ > 0)
                            {
                                audio_buffer_1.rewind()
                                val data_compat = ByteBufferCompat(audio_buffer_1)
                                val vu_value: Float = AudioBar.audio_vu(data_compat.array(), sample_count_)
                                global_audio_in_vu = if (vu_value > MainActivity.AUDIO_VU_MIN_VALUE)
                                {
                                    vu_value
                                } else
                                {
                                    0f
                                }
                            }
                            AudioBar.set_cur_value(global_audio_in_vu.toInt(), AudioBar.audio_in_bar)
                        }
                    }
                    t_audio_bar_set.start()/* DEBUG ONLY ----------------------------
                    try
                    {
                        audio_buffer_2!!.rewind()
                        val audio_buffer_2_ = ffmpegav_ByteBufferCompat(audio_buffer_2)
                        // Log.i(TAG, "audiobytes2:" + AVActivity.bytesToHex(audio_buffer_2_.array(), 0, 100))
                    } catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                    DEBUG ONLY ---------------------------- */
                }

                override fun onError()
                {
                }
            })

            AVActivity.ffmpegav_set_video_capture_callback(object : AVActivity.video_capture_callback
            {
                override fun onSuccess(width: Long, height: Long, pts: Long)
                { // Log.i(TAG, "ffmpeg open video capture onSuccess: $width $height $pts")
                    val frame_width_px: Int = width.toInt()
                    val frame_height_px: Int = height.toInt()
                    val buffer_size_in_bytes3 = (frame_width_px * frame_height_px * 1.5f).toInt()
                    if ((video_buffer_2 == null) || (frame_width_px != frame_width_px2) || (frame_height_px != frame_height_px2))
                    {
                        video_buffer_2 = ByteBuffer.allocateDirect(buffer_size_in_bytes3)
                        MainActivity.set_JNI_video_buffer2(video_buffer_2, frame_width_px, frame_height_px)
                        VideoOutFrame.setup_video_out_resolution(frame_width_px, frame_height_px, buffer_size_in_bytes3)
                    }
                    video_buffer_2!!.rewind()
                    video_buffer_2_y.rewind()
                    video_buffer_2_u.rewind()
                    video_buffer_2_v.rewind()
                    video_buffer_2!!.put(video_buffer_2_y)
                    video_buffer_2!!.put(video_buffer_2_u)
                    video_buffer_2!!.put(video_buffer_2_v) // can we cache that? what if a friend gets deleted while in a call? and the friend number changes?
                    val friendnum = MainActivity.tox_friend_by_public_key(friendpubkey)
                    MainActivity.toxav_video_send_frame_age(friendnum = friendnum, frame_width_px = width.toInt(), frame_height_px = height.toInt(), age_ms = 0)

                    video_buffer_2!!.rewind()
                    VideoOutFrame.new_video_out_frame(video_buffer_2, frame_width_px, frame_height_px)
                }

                override fun onError()
                {
                }
            })

            AVActivity.ffmpegav_start_video_in_capture()
            AVActivity.ffmpegav_start_audio_in_capture()
        }
        semaphore_avstate.acquire()
        devices_state = CALL_DEVICES_STATE.CALL_DEVICES_STATE_ACTIVE
        semaphore_avstate.release()
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

interface AVStateStoreCallState
{
    val stateFlow: StateFlow<AVStateCallState>
    val state get() = stateFlow.value
    fun update(status: AVState.CALL_STATUS)
}

fun CoroutineScope.createAVStateStoreCallState(): AVStateStoreCallState
{
    val mutableStateFlow = MutableStateFlow(AVStateCallState())

    return object : AVStateStoreCallState
    {
        override val stateFlow: StateFlow<AVStateCallState> = mutableStateFlow
        override fun update(status: AVState.CALL_STATUS)
        {
            launch {
                mutableStateFlow.value = state.copy(call_state = status)
            }
        }
    }
}
@file:Suppress("FunctionName", "SpellCheckingInspection", "ConvertToStringTemplate", "LiftReturnOrAssignment", "MemberVisibilityCanBePrivate", "LocalVariableName", "UnnecessaryVariable", "ReplaceCallWithBinaryOperator", "PropertyName", "PrivatePropertyName", "ClassName", "JoinDeclarationAndAssignment")

package com.zoffcc.applications.trifa

import RESOURCESDIR
import avstatestore
import avstatestorecallstate
import avstatestorevcapfpsstate
import com.zoffcc.applications.ffmpegav.AVActivity
import com.zoffcc.applications.ffmpegav.AVActivity.ffmpegav_apply_audio_filter
import com.zoffcc.applications.ffmpegav.AVActivity.ffmpegav_init
import com.zoffcc.applications.trifa.MainActivity.Companion.AUDIO_PCM_DEBUG_FILES
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__audio_input_filter
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__audio_play_volume_percent
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__do_not_sync_av
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__v4l2_capture_force_mjpeg
import com.zoffcc.applications.trifa.MainActivity.Companion.set_audio_play_volume_percent
import global_prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer

data class AVStateCallState(val call_state: AVState.CALL_STATUS = AVState.CALL_STATUS.CALL_STATUS_NONE, val video_in_popout: Boolean = false, val display_av_stats: Boolean = false)
data class AVStateVideoCaptureFpsState(val videocapfps_state: Int = 0, val videocap_enc_bitrate: Int = 0, val sourceResolution: String = "", val sourceFormat: String = "")
data class AVStateVideoPlayFpsState(val videoplayfps_state: Int = 0, val videocap_dec_bitrate: Int = 0, val incomingResolution: String = "", val network_rtt: Int = 0, val play_delay: Int = 0)

data class AVState(val a: Int)
{
    enum class CALL_DEVICES_STATE {
        CALL_DEVICES_STATE_CLOSED,
        CALL_DEVICES_STATE_ACTIVE,
    }

    enum class CALL_STATUS {
        CALL_STATUS_NONE,
        CALL_STATUS_INCOMING,
        CALL_STATUS_CALLING,
        CALL_STATUS_ENDING
    }

    /*
    enum class NGC_CALL_STATUS {
        CALL_STATUS_NONE,
        CALL_STATUS_INCOMING,
        CALL_STATUS_CALLING,
        CALL_STATUS_ENDING
    }
     */

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
    private var video_capture_fps = -1
    private var video_in_resolution_width = 640
    private var video_in_resolution_height = 480
    private var video_in_source_resolution_width = 0
    private var video_in_source_resolution_height = 0
    private var current_video_in_fps = 0
    var calling_state = CALL_STATUS.CALL_STATUS_NONE
    private var devices_state = CALL_DEVICES_STATE.CALL_DEVICES_STATE_CLOSED
    private var call_with_friend_pubkey: String? = null
    private var semaphore_avstate = CustomSemaphore(1)
    private val ffmpeg_devices_lock = Any()

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

        Log.i(TAG, "ffmpeg_devices_start:clear_video_out_frame")
        VideoOutFrame.clear_video_out_frame()
        Log.i(TAG, "ffmpeg_devices_start:clear_video_in_frame")
        VideoInFrame.clear_video_in_frame()

        if (devices_state_copy == CALL_DEVICES_STATE.CALL_DEVICES_STATE_CLOSED)
        {
            if ((video_in_device != null) && (video_in_device != ""))
            {
                if ((video_in_source != null) && (video_in_source != ""))
                {
                    //var capture_fps_used = CAPTURE_VIDEO_FPS
                    //if ((video_in_resolution_width == 1920) && (video_in_resolution_height == 1080))
                    //{
                    //    capture_fps_used = CAPTURE_VIDEO_HIGH_FPS
                    //}
                    println("ffmpeg video in device:2: " + video_in_resolution_width + " x " + video_in_resolution_height)
                    println("capture_fps_used:2: " + avstatestore.state.video_capture_fps_get())
                    println("ffmpeg video in device: " + video_in_device + " " + video_in_source)
                    val res_vd = AVActivity.ffmpegav_open_video_in_device(video_in_device, video_in_source,
                        video_in_resolution_width, video_in_resolution_height, avstatestore.state.video_capture_fps_get(),
                        PREF__v4l2_capture_force_mjpeg)
                    println("ffmpeg open video capture device: $res_vd")
                }
            }
            if ((audio_in_device != null) && (audio_in_device != ""))
            {
                if ((audio_in_source != null) && (audio_in_source != ""))
                {
                    println("ffmpeg audio in device: " + audio_in_device + " " + audio_in_source)
                    val res_ad = AVActivity.ffmpegav_open_audio_in_device_wrapper(audio_in_device, audio_in_source)
                    println("ffmpeg open audio capture device: $res_ad")
                }
            }
            AVActivity.ffmpegav_start_video_in_capture()
            AVActivity.ffmpegav_start_audio_in_capture_wrapper()
        }
        semaphore_avstate.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
        devices_state = CALL_DEVICES_STATE.CALL_DEVICES_STATE_ACTIVE
        semaphore_avstate.release()
    }

    fun ffmpeg_devices_stop()
    {
        synchronized(ffmpeg_devices_lock) {
            val devices_state_copy: CALL_DEVICES_STATE
            semaphore_avstate.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            devices_state_copy = devices_state
            semaphore_avstate.release()
            if (devices_state_copy == CALL_DEVICES_STATE.CALL_DEVICES_STATE_ACTIVE)
            {
                Log.i(TAG, "ffmpeg_devices_stop:ffmpegav_stop_audio_in_capture")
                AVActivity.ffmpegav_stop_audio_in_capture_wrapper()
                //Thread.sleep(20)
                Log.i(TAG, "ffmpeg_devices_stop:ffmpegav_close_audio_in_device")
                AVActivity.ffmpegav_close_audio_in_device_wrapper()
                //Thread.sleep(20)
                Log.i(TAG, "ffmpeg_devices_stop:ffmpegav_stop_video_in_capture")
                AVActivity.ffmpegav_stop_video_in_capture()
                //Thread.sleep(20)
                Log.i(TAG, "ffmpeg_devices_stop:ffmpegav_close_video_in_device")
                AVActivity.ffmpegav_close_video_in_device()
                //Thread.sleep(20)
                Log.i(TAG, "ffmpeg_devices_stop:set_cur_value1")
                AudioBar.set_cur_value(0, AudioBar.audio_in_bar)
                Log.i(TAG, "ffmpeg_devices_stop:set_cur_value2")
                AudioBar.set_cur_value(0, AudioBar.audio_out_bar)
                Log.i(TAG, "ffmpeg_devices_stop:clear_video_out_frame")
                VideoOutFrame.clear_video_out_frame()
                Log.i(TAG, "ffmpeg_devices_stop:clear_video_in_frame")
                VideoInFrame.clear_video_in_frame()
                Log.i(TAG, "ffmpeg_devices_stop:DONE")
            }
            else
            {
                Log.i(TAG, "ffmpeg_devices_stop:already stopped")
            }
            semaphore_avstate.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            devices_state = CALL_DEVICES_STATE.CALL_DEVICES_STATE_CLOSED
            semaphore_avstate.release()
        }
    }

    fun ffmpeg_init_do()
    {
        if (!ffmpeg_init_done_get())
        {
            try
            {
                val res = ffmpegav_init(RESOURCESDIR.canonicalPath + File.separator)
                println("==================ffmpeg init:1: $res")
            }
            catch(e: Exception)
            {
                Log.i(TAG, "ERROR: something happend while trying to get RESOURCESDIR.canonicalPath")
                val res = ffmpegav_init(null)
                println("==================ffmpeg init:2: $res")
            }
            ffmpeg_init_done_set(true)
        }
    }

    fun video_capture_fps_get(): Int
    {
        return (video_capture_fps)
    }

    fun video_capture_fps_set(value: Int)
    {
        if ((value == -1) || ((value > 0) && (value < 120)))
        {
            video_capture_fps = value
            save_device_information()
            restart_devices()
        }
    }

    fun video_in_resolution_get(): String
    {
        return (video_in_resolution_width.toString() + "x" + video_in_resolution_height.toString())
    }

    fun video_in_resolution_set(value: String?)
    {
        if ((value != null) && (value.length > 2))
        {
            try
            {
                video_in_resolution_width = value.split("x", limit = 2)[0].toInt()
                video_in_resolution_height = value.split("x", limit = 2)[1].toInt()
            } catch (_: Exception) {
            }
        }
    }

    fun video_in_source_resolution_get(): String
    {
        return (video_in_source_resolution_width.toString() + "x" + video_in_source_resolution_height.toString())
    }

    fun video_in_source_resolution_set(value: String?)
    {
        if ((value != null) && (value.length > 2))
        {
            try
            {
                video_in_source_resolution_width = value.split("x", limit = 2)[0].toInt()
                video_in_source_resolution_height = value.split("x", limit = 2)[1].toInt()
            } catch (_: Exception) {
            }
        }
    }

    fun current_video_in_fps_get(): Int
    {
        return current_video_in_fps
    }

    fun current_video_in_fps_set(value: Int)
    {
        current_video_in_fps = value
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
        if (value != CALL_STATUS.CALL_STATUS_CALLING)
        {
            avstatestorecallstate.video_in_popout_update(video_in_popout = false)
        }
        avstatestorecallstate.update(status = value)
        Log.i(TAG, "calling_state_set:" + value)
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
            Log.i(TAG, "ffmpeg_devices_stop:001")
            ffmpeg_devices_stop()
            Log.i(TAG, "ffmpeg_devices_start:001:########################")
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

        try
        {
            val tmp = global_prefs.get("main.av.video_capture_fps", "-1")
            if (tmp != null)
            {
                video_capture_fps = tmp.toInt()
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
        try
        {
            global_prefs.put("main.av.video_capture_fps", video_capture_fps.toString())
        } catch (_: Exception)
        {
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun start_av_call()
    {
        val friendpubkey = call_with_friend_pubkey

        if (friendpubkey == null)
        {
            println("start_outgoing_video: friend pubkey is null!! ERROR !!")
            return
        }

        MainActivity.set_av_call_status(1)
        MainActivity.tox_set_do_not_sync_av(PREF__do_not_sync_av)
        println("tox_set_do_not_sync_av:1: " +  PREF__do_not_sync_av)
        ffmpegav_apply_audio_filter(PREF__audio_input_filter)
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

            val video_in_resolution_width_pin = video_in_resolution_width
            val video_in_resolution_height_pin = video_in_resolution_height

            if ((video_in_device != null) && (video_in_device != ""))
            {
                if ((video_in_source != null) && (video_in_source != ""))
                {
                    //var capture_fps_used = CAPTURE_VIDEO_FPS
                    //if ((video_in_resolution_width == 1920) && (video_in_resolution_height == 1080))
                    //{
                    //    capture_fps_used = CAPTURE_VIDEO_HIGH_FPS
                    //}
                    println("ffmpeg video in device:1: " + video_in_resolution_width + " x " + video_in_resolution_height)
                    println("capture_fps_used:1: " + avstatestore.state.video_capture_fps_get())
                    println("ffmpeg video in device: " + video_in_device + " " + video_in_source)
                    val res_vd = AVActivity.ffmpegav_open_video_in_device(video_in_device, video_in_source,
                        video_in_resolution_width_pin, video_in_resolution_height_pin, avstatestore.state.video_capture_fps_get(),
                        PREF__v4l2_capture_force_mjpeg)
                    println("ffmpeg open video capture device: $res_vd")
                }
            }
            val frame_width_px1 = video_in_resolution_width_pin
            val frame_height_px1 = video_in_resolution_height_pin
            val buffer_size_in_bytes1 = (frame_width_px1 * frame_height_px1 * 3) / 2
            val video_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes1)
            AVActivity.ffmpegav_set_JNI_video_buffer(video_buffer_1, frame_width_px1, frame_height_px1)
            var frame_width_px2 = video_in_resolution_width_pin
            var frame_height_px2 = video_in_resolution_height_pin
            val y_size = frame_width_px2 * frame_height_px2
            val u_size = (frame_width_px2 * frame_height_px2 / 4)
            val v_size = (frame_width_px2 * frame_height_px2 / 4)
            AVActivity.ffmpegav_video_buffer_2_y = ByteBuffer.allocateDirect(y_size)
            AVActivity.ffmpegav_video_buffer_2_u = ByteBuffer.allocateDirect(u_size)
            AVActivity.ffmpegav_video_buffer_2_v = ByteBuffer.allocateDirect(v_size)
            AVActivity.ffmpegav_set_JNI_video_buffer2(AVActivity.ffmpegav_video_buffer_2_y, AVActivity.ffmpegav_video_buffer_2_u, AVActivity.ffmpegav_video_buffer_2_v, frame_width_px2, frame_height_px2)
            val audio_in_device = audio_in_device_get()
            val audio_in_source = audio_in_source_get()
            var audio_buffer_2: ByteBuffer? = null

            if ((audio_in_device != null) && (audio_in_device != ""))
            {
                if ((audio_in_source != null) && (audio_in_source != ""))
                {
                    println("ffmpeg audio in device: " + audio_in_device + " " + audio_in_source)
                    val res_ad = AVActivity.ffmpegav_open_audio_in_device_wrapper(audio_in_device, audio_in_source)
                    println("ffmpeg open audio capture device: $res_ad")
                }
            }
            val buffer_size_in_bytes2 = 50000 // TODO: don't hardcode this
            var audio_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes2)
            AVActivity.ffmpegav_set_JNI_audio_buffer2_wrapper(audio_buffer_1)

            AVActivity.ffmpegav_set_audio_capture_callback(object : AVActivity.audio_capture_callback
            {
                override fun onSuccess(read_bytes: Long, out_samples: Int, out_channels: Int, out_sample_rate: Int, pts: Long)
                {
                    // Log.i(TAG, "ffmpeg open audio capture onSuccess: $read_bytes $out_samples $out_channels $out_sample_rate $pts")
                    // val t1 = System.currentTimeMillis()
                    if ((audio_buffer_2 == null))
                    {
                        audio_buffer_2 = ByteBuffer.allocateDirect(buffer_size_in_bytes2)
                        MainActivity.set_JNI_audio_buffer(audio_buffer_2)
                    }
                    /* DEBUG ONLY ----------------------------
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
                    audio_buffer_2!!.put(audio_buffer_1)
                    // can we cache that? what if a friend gets deleted while in a call? and the friend number changes?
                    val friendnum = MainActivity.tox_friend_by_public_key(friendpubkey)
                    // HINT: fix me --------
                    GlobalScope.launch {
                        val toxav_audio_send_frame_res = MainActivity.toxav_audio_send_frame(friend_number = friendnum, sample_count = out_samples.toLong(), channels = out_channels, sampling_rate = out_sample_rate.toLong())
                        if (toxav_audio_send_frame_res != 0)
                        {
                            Log.i(TAG, "toxav_audio_send_frame:result=" + toxav_audio_send_frame_res)
                        }
                        if (AUDIO_PCM_DEBUG_FILES)
                        {
                            val f = File("/tmp/toxaudio_send.txt")
                            try
                            {
                                audio_buffer_2!!.rewind()
                                val want_bytes = out_samples * 2
                                val audio_in_byte_buffer = ByteArray(want_bytes)
                                audio_buffer_2!![audio_in_byte_buffer, 0, want_bytes]
                                f.appendBytes(audio_in_byte_buffer)
                            } catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                    }
                    // HINT: fix me --------
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
                    // val t2 = System.currentTimeMillis()
                    // Log.i(TAG, "AAAAAAAAAA:" + (t2 - t1))
                }

                override fun onBufferTooSmall(audio_buffer_size: Int)
                {
                    audio_buffer_1 = ByteBuffer.allocateDirect(audio_buffer_size)
                    AVActivity.ffmpegav_set_JNI_audio_buffer2_wrapper(audio_buffer_1)
                }

                override fun onError()
                {
                }
            })

            AVActivity.ffmpegav_set_video_capture_callback(object : AVActivity.video_capture_callback
            {
                // WARNING: you need to make this thread safe !!!!!
                override fun onSuccess(width: Long, height: Long, source_width: Long, source_height: Long, pts: Long, fps: Int, source_format: Int)
                {
                    // Log.i(TAG, "ffmpeg open video capture onSuccess: $width $height $pts FPS: $fps Source Format: "
                    //         + AVActivity.ffmpegav_video_source_format_name.value_str(source_format))
                    if (current_video_in_fps_get() != fps) {
                        current_video_in_fps_set(fps)
                        avstatestorevcapfpsstate.update(fps)
                    }
                    if (!("" + source_width + "x" + source_height).equals(avstatestorevcapfpsstate.state.sourceResolution))
                    {
                        video_in_source_resolution_set("" + source_width + "x" + source_height)
                        avstatestorevcapfpsstate.updateSourceResolution("" + source_width + "x" + source_height)
                    }

                    if (!AVActivity.ffmpegav_video_source_format_name.value_str(source_format).equals(avstatestorevcapfpsstate.state.sourceFormat))
                    {
                        avstatestorevcapfpsstate.updateSourceFormat(AVActivity.ffmpegav_video_source_format_name.value_str(source_format))
                    }

                    val frame_width_px: Int = width.toInt()
                    val frame_height_px: Int = height.toInt()
                    if ((video_buffer_2 == null) || (frame_width_px != frame_width_px2) || (frame_height_px != frame_height_px2))
                    {
                        Log.i(TAG, "ffmpeg open video capture: sizes changed: " +
                                video_buffer_2 + " " +
                                frame_width_px + " " +
                                frame_width_px2 + " " +
                                frame_height_px + " " +
                                frame_height_px2 + " "
                        )
                        val buffer_size_in_bytes3 = (frame_width_px * frame_height_px * 1.5f).toInt()
                        video_buffer_2 = ByteBuffer.allocateDirect(buffer_size_in_bytes3)
                        MainActivity.set_JNI_video_buffer2(video_buffer_2, frame_width_px, frame_height_px)
                        VideoOutFrame.setup_video_out_resolution(frame_width_px, frame_height_px, buffer_size_in_bytes3)
                        frame_width_px2 = frame_width_px
                        frame_height_px2 = frame_height_px
                        Log.i(TAG, "ffmpeg open video capture: sizes changed: finished")
                    }
                    video_buffer_2!!.rewind()
                    AVActivity.ffmpegav_video_buffer_2_y.rewind()
                    AVActivity.ffmpegav_video_buffer_2_u.rewind()
                    AVActivity.ffmpegav_video_buffer_2_v.rewind()
                    try
                    {
                        video_buffer_2!!.put(AVActivity.ffmpegav_video_buffer_2_y)
                        video_buffer_2!!.put(AVActivity.ffmpegav_video_buffer_2_u)
                        video_buffer_2!!.put(AVActivity.ffmpegav_video_buffer_2_v)
                    }
                    catch(e: Exception)
                    {
                        // HINT: AVActivity.ffmpegav_video_buffer_2_[y/u/v] buffers are too large for video_buffer_2
                        // if we want to change resolution during a call we need to change some stuff
                        return
                    }
                    // can we cache that? what if a friend gets deleted while in a call? and the friend number changes?
                    val friendnum = MainActivity.tox_friend_by_public_key(friendpubkey)
                    MainActivity.toxav_video_send_frame_age(friendnum = friendnum, frame_width_px = width.toInt(), frame_height_px = height.toInt(), age_ms = pts.toInt())

                    video_buffer_2!!.rewind()
                    VideoOutFrame.new_video_out_frame(video_buffer_2, frame_width_px, frame_height_px)
                }

                // WARNING: you need to make this thread safe !!!!!
                override fun onBufferTooSmall(y_buffer_size: Int, u_buffer_size: Int, v_buffer_size: Int)
                {
                    Log.i(TAG, "ffmpeg open video capture onBufferTooSmall: sizes needed: " + y_buffer_size + " " + u_buffer_size + " " + v_buffer_size)
                    AVActivity.ffmpegav_video_buffer_2_y = ByteBuffer.allocateDirect(y_buffer_size)
                    AVActivity.ffmpegav_video_buffer_2_u = ByteBuffer.allocateDirect(u_buffer_size)
                    AVActivity.ffmpegav_video_buffer_2_v = ByteBuffer.allocateDirect(v_buffer_size)
                    AVActivity.ffmpegav_set_JNI_video_buffer2(AVActivity.ffmpegav_video_buffer_2_y, AVActivity.ffmpegav_video_buffer_2_u, AVActivity.ffmpegav_video_buffer_2_v, frame_width_px2, frame_height_px2)
                    Log.i(TAG, "ffmpeg open video capture onBufferTooSmall: finished")
                }

                // WARNING: you need to make this thread safe !!!!!
                override fun onError()
                {
                }
            })

            AVActivity.ffmpegav_start_video_in_capture()
            AVActivity.ffmpegav_start_audio_in_capture_wrapper()
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

interface AVStateStoreVideoCaptureFpsState
{
    val stateFlow: StateFlow<AVStateVideoCaptureFpsState>
    val state get() = stateFlow.value
    fun update(fps: Int)
    fun updateEncoderVBitrate(bitrate: Int)
    fun updateSourceResolution(sourceResolution: String)
    fun updateSourceFormat(sourceFormat: String)
}

fun CoroutineScope.createAVStateStoreVideoCaptureFpsState(): AVStateStoreVideoCaptureFpsState
{
    val mutableStateFlow = MutableStateFlow(AVStateVideoCaptureFpsState())

    return object : AVStateStoreVideoCaptureFpsState
    {
        override val stateFlow: StateFlow<AVStateVideoCaptureFpsState> = mutableStateFlow
        override fun update(fps: Int)
        {
            launch {
                if (avstatestorecallstate.state.display_av_stats)
                {
                    // !! IMPORTANT !! we only change this value when it deviates more than x
                    // because for some reason the whole UI and all chat messages will repaint when the
                    // element where this is displayed changes. i do not know why :-(
                    if (Math.abs(state.videocapfps_state - fps) > 2)
                    {
                        mutableStateFlow.value = state.copy(videocapfps_state = fps)
                    }
                }
            }
        }

        override fun updateEncoderVBitrate(bitrate: Int)
        {
            //launch {
            if (avstatestorecallstate.state.display_av_stats)
            {
                mutableStateFlow.value = state.copy(videocap_enc_bitrate = bitrate)
            }
            //}
        }

        override fun updateSourceResolution(sourceResolution: String)
        {
            //launch {
                mutableStateFlow.value = state.copy(sourceResolution = sourceResolution)
            //}
        }
        override fun updateSourceFormat(sourceFormat: String)
        {
            //launch {
                mutableStateFlow.value = state.copy(sourceFormat = sourceFormat)
            //}
        }
    }
}

interface AVStateStoreVideoPlayFpsState
{
    val stateFlow: StateFlow<AVStateVideoPlayFpsState>
    val state get() = stateFlow.value
    fun update(fps: Int)
    fun updateNetworkRTT(value: Int)
    fun updatePlayDelay(value: Int)
    fun updateDecoderVBitrate(bitrate: Int)
    fun updateIncomingResolution(incomingResolution: String)
}

fun CoroutineScope.createAVStateStoreVideoPlayFpsState(): AVStateStoreVideoPlayFpsState
{
    val mutableStateFlow = MutableStateFlow(AVStateVideoPlayFpsState())

    return object : AVStateStoreVideoPlayFpsState
    {
        override val stateFlow: StateFlow<AVStateVideoPlayFpsState> = mutableStateFlow
        override fun update(fps: Int)
        {
            launch {
                if (avstatestorecallstate.state.display_av_stats)
                {
                    // !! IMPORTANT !! we only change this value when it deviates more than x
                    // incoming fps fluctuates more than capture fps. and therefore a higher delta value (x)
                    // because for some reason the whole UI and all chat messages will repaint when the
                    // element where this is displayed changes. i do not know why :-(
                    if (Math.abs(state.videoplayfps_state - fps) > 4)
                    {
                        mutableStateFlow.value = state.copy(videoplayfps_state = fps)
                    }
                }
            }
        }
        override fun updateNetworkRTT(value: Int)
        {
            if (avstatestorecallstate.state.display_av_stats)
            {
                mutableStateFlow.value = state.copy(network_rtt = value)
            }
        }
        override fun updatePlayDelay(value: Int)
        {
            if (avstatestorecallstate.state.display_av_stats)
            {
                mutableStateFlow.value = state.copy(play_delay = value)
            }
        }

        override fun updateDecoderVBitrate(bitrate: Int)
        {
            //launch {
            if (avstatestorecallstate.state.display_av_stats)
            {
                mutableStateFlow.value = state.copy(videocap_dec_bitrate = bitrate)
            }
            //}
        }

        override fun updateIncomingResolution(incomingResolution: String)
        {
            //launch {
                mutableStateFlow.value = state.copy(incomingResolution = incomingResolution)
            //}
        }
    }
}

interface AVStateStoreCallState
{
    val stateFlow: StateFlow<AVStateCallState>
    val state get() = stateFlow.value
    fun update(status: AVState.CALL_STATUS)
    fun video_in_popout_update(video_in_popout: Boolean)
    fun display_av_stats(value: Boolean)
}

fun CoroutineScope.createAVStateStoreCallState(): AVStateStoreCallState
{
    val mutableStateFlow = MutableStateFlow(AVStateCallState())

    return object : AVStateStoreCallState
    {
        override val stateFlow: StateFlow<AVStateCallState> = mutableStateFlow
        override fun update(status: AVState.CALL_STATUS)
        {
            //launch {
                mutableStateFlow.value = state.copy(call_state = status)
            //}
        }

        override fun video_in_popout_update(value: Boolean)
        {
            if (value == true)
            {
                if (state.call_state == AVState.CALL_STATUS.CALL_STATUS_CALLING)
                {
                    mutableStateFlow.value = state.copy(video_in_popout = value)
                    Log.i(TAG, "video_in_popout = " + value)
                }
            }
            else
            {
                mutableStateFlow.value = state.copy(video_in_popout = value)
                Log.i(TAG, "video_in_popout = " + value)
            }
        }
        override fun display_av_stats(value: Boolean)
        {
            mutableStateFlow.value = state.copy(display_av_stats = value)
        }
    }
}

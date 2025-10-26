package com.zoffcc.applications.ffmpegav;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.util.Random;

public class AVActivity {

    private static final String TAG = "ffmpegav.AVActivity";
    static final String Version = "0.99.31";
    public static final String JAVA_AUDIO_IN_DEVICE_NAME = "Java Audio in (-fallback-)";

    private static boolean java_audio_in_device_used = false;
    public static native String ffmpegav_version();
    public static native String ffmpegav_GITHASH();
    public static native String ffmpegav_libavutil_version();
    public static native int ffmpegav_init(String resources_dir);
    public static native ffmpegav_descrid[] ffmpegav_get_in_sources(String devicename, int is_video);

    public static native String[] ffmpegav_get_video_in_devices();
    public static native int ffmpegav_open_video_in_device(String deviceformat, String inputname, int wanted_width, int wanted_height, int fps, int force_mjpeg);
    public static native int ffmpegav_start_video_in_capture();
    public static native int ffmpegav_stop_video_in_capture();
    public static native int ffmpegav_close_video_in_device();

    private static native String[] ffmpegav_get_audio_in_devices();
    private static native int ffmpegav_open_audio_in_device(String deviceformat, String inputname);
    private static native int ffmpegav_start_audio_in_capture();
    private static native int ffmpegav_stop_audio_in_capture();
    private static native int ffmpegav_close_audio_in_device();
    public static native void ffmpegav_apply_audio_filter(int apply_filter);

    public static java.nio.ByteBuffer ffmpegav_video_buffer_2_y = null;
    public static java.nio.ByteBuffer ffmpegav_video_buffer_2_u = null;
    public static java.nio.ByteBuffer ffmpegav_video_buffer_2_v = null;

    private static TargetDataLine targetDataLine = null;
    private static AudioFormat audioformat = null;
    private static Thread t_audio_rec = null;
    private static java.nio.ByteBuffer java_api_audio_capture_buffer = null;
    private static boolean ffmpegav_java_audio_capture_running = false;
    private final static int AUDIO_FRAMEDURATION_MS = 60; // fixed ms interval for outgoing audio
    private final static int AUDIO_REC_SAMPLE_RATE = 48000;
    private final static int AUDIO_REC_CHANNELS = 1;
    private final static int AUDIO_REC_SAMPLE_SIZE_BIT = 16;

    private static boolean mult_thr_test_finish = false;

    public static class ffmpegav_descrid
    {
        public String description;
        public String id;
        public ffmpegav_descrid(){
            description    = new String("");
            id             = new String("");
        }
        public String toString() {
            return "[" + description + "," + id +"]";
        }
    }

    public static enum ffmpegav_video_source_format_name
    {
        // HINT: for more values see "codec_id.h" of ffmpeg source code
        AV_CODEC_ID_NONE(0),
        AV_CODEC_ID_MPEG1VIDEO(1),
        AV_CODEC_ID_MPEG2VIDEO(2),
        AV_CODEC_ID_H261(3),
        AV_CODEC_ID_H263(4),
        AV_CODEC_ID_RV10(5),
        AV_CODEC_ID_RV20(6),
        AV_CODEC_ID_MJPEG(7),
        AV_CODEC_ID_RAWVIDEO(13),
        AV_CODEC_ID_H264(27);

        public int value;

        private ffmpegav_video_source_format_name(int value)
        {
            this.value = value;
        }

        public static String value_str(int value)
        {
            if (value == AV_CODEC_ID_NONE.value)
            {
                return "CODEC: NONE";
            }
            else if (value == AV_CODEC_ID_MJPEG.value)
            {
                return "MJPEG";
            }
            else if (value == AV_CODEC_ID_RAWVIDEO.value)
            {
                return "RAWVIDEO";
            }
            else if (value == AV_CODEC_ID_H264.value)
            {
                return "H264";
            }
            return "UNKNOWN";
        }
    }

    /*
     * add the java fallback audio input device to the end of the list
     */
    public static String[] ffmpegav_get_audio_in_devices_wrapper()
    {
        // this will always return a string array with 64 entries
        // some or all of the strings in the array may be NULL
        String[] audio_devices_list = ffmpegav_get_audio_in_devices();
        if ((audio_devices_list == null) || (audio_devices_list.length == 0))
        {
            // HINT: should never get here. but just to be safe
            return audio_devices_list;
        }
        String[] audio_devices_list_with_j_fallback = new String[audio_devices_list.length + 1];
        int i = 0;
        for (i=0;i<audio_devices_list.length;i++)
        {
            audio_devices_list_with_j_fallback[i] = audio_devices_list[i];
        }
        audio_devices_list_with_j_fallback[i] = JAVA_AUDIO_IN_DEVICE_NAME;
        return audio_devices_list_with_j_fallback;
    }

    public static int ffmpegav_open_audio_in_device_wrapper(String deviceformat, String inputname)
    {
        if (deviceformat.equals(JAVA_AUDIO_IN_DEVICE_NAME)) {
            Log.i(TAG, "ffmpegav_open_audio_in_device_wrapper ... JAVA" + deviceformat + " " + inputname);
            java_audio_in_device_used = true;
            ffmpegav_open_java_audio_in_device();
            return 0;
        } else {
            Log.i(TAG, "ffmpegav_open_audio_in_device_wrapper ... regular" + deviceformat + " " + inputname);
            java_audio_in_device_used = false;
            return ffmpegav_open_audio_in_device(deviceformat, inputname);
        }
    }

    public static int ffmpegav_start_audio_in_capture_wrapper()
    {
        if (java_audio_in_device_used) {
            Log.i(TAG, "ffmpegav_start_audio_in_capture_wrapper ... JAVA");
            ffmpegav_start_java_audio_in_capture();
            return 0;
        } else {
            Log.i(TAG, "ffmpegav_start_audio_in_capture_wrapper ... regular");
            return ffmpegav_start_audio_in_capture();
        }
    }

    public static int ffmpegav_stop_audio_in_capture_wrapper()
    {
        if (java_audio_in_device_used) {
            Log.i(TAG, "ffmpegav_stop_audio_in_capture_wrapper ... JAVA");
            ffmpegav_stop_java_audio_in_capture();
            return 0;
        } else {
            Log.i(TAG, "ffmpegav_stop_audio_in_capture_wrapper ... regular");
            return ffmpegav_stop_audio_in_capture();
        }
    }

    /*
     * check if the java fallback audio input device is in use
     */
    public static int ffmpegav_close_audio_in_device_wrapper()
    {
        if (java_audio_in_device_used) {
            Log.i(TAG, "ffmpegav_close_audio_in_device_wrapper ... JAVA");
            ffmpegav_close_java_audio_in_device();
            java_audio_in_device_used = false;
            return 0;
        } else {
            Log.i(TAG, "ffmpegav_close_audio_in_device_wrapper ... regular");
            java_audio_in_device_used = false;
            return ffmpegav_close_audio_in_device();
        }
    }

    final static int audio_buffer_size_in_bytes2 = 20000;
    final static java.nio.ByteBuffer audio_buffer_2 = java.nio.ByteBuffer.allocateDirect(audio_buffer_size_in_bytes2);

    public static interface video_capture_callback {
        void onSuccess(long width, long height, long source_width, long source_height, long pts, int fps, int source_format);
        void onBufferTooSmall(int y_buffer_size, int u_buffer_size, int v_buffer_size);
        void onError();
    }
    static video_capture_callback video_capture_callback_function = null;

    public static interface audio_capture_callback {
        void onSuccess(long read_bytes, int out_samples, int out_channels, int out_sample_rate, long pts);
        void onBufferTooSmall(int audio_buffer_size);
        void onError();
    }
    static audio_capture_callback audio_capture_callback_function = null;

    // buffer is for playing video
    public static native long ffmpegav_set_JNI_video_buffer(java.nio.ByteBuffer buffer, int frame_width_px, int frame_height_px);
    // buffer2 is for capturing video
    public static native void ffmpegav_set_JNI_video_buffer2(java.nio.ByteBuffer send_vbuf_y, java.nio.ByteBuffer send_vbuf_u, java.nio.ByteBuffer send_vbuf_v, int frame_width_px, int frame_height_px);
    // audio_buffer is for playing audio
    public static native void ffmpegav_set_JNI_audio_buffer(java.nio.ByteBuffer audio_buffer);
    // audio_buffer2 is for capturing audio
    private static native void ffmpegav_set_JNI_audio_buffer2(java.nio.ByteBuffer audio_buffer2);

    public static void ffmpegav_set_JNI_audio_buffer2_wrapper(java.nio.ByteBuffer audio_buffer2)
    {
        // HINT: save the buffer object for java audio
        java_api_audio_capture_buffer = audio_buffer2;
        ffmpegav_set_JNI_audio_buffer2(audio_buffer2);
    }

    public static void ffmpegav_set_video_capture_callback(video_capture_callback callback)
    {
        video_capture_callback_function = callback;
    }

    public static void ffmpegav_callback_video_capture_frame_pts_cb_method(long width, long height, long source_width, long source_height, long pts, int fps, int source_format)
    {
        // Log.i(TAG, "capture video frame w: " + width + " h: " + height + " pts: " + pts);
        if (video_capture_callback_function != null) {
            video_capture_callback_function.onSuccess(width, height, source_width, source_height, pts, fps, source_format);
        }
    }

    public static void ffmpegav_callback_video_capture_frame_too_small_cb_method(int y_buffer_size, int u_buffer_size, int v_buffer_size)
    {
        if (video_capture_callback_function != null) {
            video_capture_callback_function.onBufferTooSmall(y_buffer_size, u_buffer_size, v_buffer_size);
        }
    }

    public static void ffmpegav_set_audio_capture_callback(audio_capture_callback callback)
    {
        audio_capture_callback_function = callback;
    }

    public static void ffmpegav_callback_audio_capture_frame_pts_cb_method(long read_bytes, int out_samples, int out_channels, int out_sample_rate, long pts)
    {
        // Log.i(TAG, "capture audio frame bytes: " + read_bytes + " samples: " + out_samples + " channels: " + out_channels + " sample_rate: " + out_sample_rate);
        if (audio_capture_callback_function != null) {
            audio_capture_callback_function.onSuccess(read_bytes, out_samples, out_channels, out_sample_rate, pts);
        }
    }

    public static void ffmpegav_callback_audio_capture_frame_too_small_cb_method(int audio_buffer_size)
    {
        if (audio_capture_callback_function != null) {
            audio_capture_callback_function.onBufferTooSmall(audio_buffer_size);
        }
    }

    private static void ffmpegav_open_java_audio_in_device()
    {
        audioformat = new AudioFormat(AUDIO_REC_SAMPLE_RATE,
                AUDIO_REC_SAMPLE_SIZE_BIT, AUDIO_REC_CHANNELS,
                true,false);
        // final Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        final DataLine.Info targetDLInfo = new DataLine.Info(TargetDataLine.class, audioformat);
        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(targetDLInfo);
        } catch (Exception e) {
            Log.i(TAG, "no working audio input found");
            targetDataLine = null;
        }
    }

    private static void ffmpegav_close_java_audio_in_device()
    {
        targetDataLine = null;
    }

    private static void ffmpegav_start_java_audio_in_capture()
    {
        ffmpegav_start_java_target_line();
        try {
            if (t_audio_rec != null)
            {
                    t_audio_rec.join(1000);
            }
        } catch (Exception ignored) {
        } finally {
            t_audio_rec = null;
        }

        ffmpegav_java_audio_capture_running = true;
        t_audio_rec = new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "capturing java audio ... starting");
                this.setName("t_a_rec");

                final int sample_count2 = ((AUDIO_REC_SAMPLE_RATE * AUDIO_FRAMEDURATION_MS) / 1000);
                final int want_numBytesRead = sample_count2 * AUDIO_REC_CHANNELS * 2;
                final byte[] data = new byte[want_numBytesRead];
                int sample_count = 0;
                int numBytesRead = 0;

                while (ffmpegav_java_audio_capture_running)
                {
                    try
                    {
                        if (targetDataLine != null) {
                            if (targetDataLine.isOpen()) {
                                // HINT: this may block. but it's ok it will not block any Tox or UI threads
                                numBytesRead = targetDataLine.read(data, 0, data.length);
                                sample_count = ((numBytesRead / 2) / AUDIO_REC_CHANNELS);

                                java_api_audio_capture_buffer.rewind();
                                if (java_api_audio_capture_buffer.capacity() < data.length) {
                                    ffmpegav_callback_audio_capture_frame_too_small_cb_method(data.length);
                                } else {
                                    java_api_audio_capture_buffer.put(data, 0, data.length);
                                }

                                ffmpegav_callback_audio_capture_frame_pts_cb_method(
                                        numBytesRead, sample_count,
                                        AUDIO_REC_CHANNELS, AUDIO_REC_SAMPLE_RATE, 0);
                                /*
                                Log.i(TAG, "sample_count=" + sample_count + " sample_count2=" + sample_count2 +
                                           " frameduration_ms=" + AUDIO_FRAMEDURATION_MS + " want_numBytesRead=" +
                                           want_numBytesRead);

                                Log.i(TAG, "t_audio_rec:read:" + numBytesRead + " isRunning=" +
                                           targetDataLine.isRunning());
                                 */
                            } else {
                                Thread.sleep(50);
                            }
                        } else {
                            Thread.sleep(50);
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "capturing java audio ... finished");
            }
        };
        t_audio_rec.start();
    }

    private static void ffmpegav_stop_java_audio_in_capture()
    {
        ffmpegav_java_audio_capture_running = false;
        try {
            if (t_audio_rec != null)
            {
                t_audio_rec.join(1000);
            }
        } catch (Exception ignored) {
        } finally {
            t_audio_rec = null;
        }
        ffmpegav_close_java_target_line();
    }

    private static synchronized void ffmpegav_close_java_target_line() {
        try {
            if (targetDataLine != null) {
                try {
                    targetDataLine.stop();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

                try {
                    targetDataLine.flush();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

                try {
                    targetDataLine.close();
                    Log.i(TAG, "select audio in:" + "close old line");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        catch(Exception e)
        {
            Log.i(TAG, "error closing java target line");
        }
    }

    private static synchronized void ffmpegav_start_java_target_line()
    {
        try {
            if (targetDataLine.isRunning())
            {
                Log.i(TAG, "isRunning:TRUE");
            }
            else
            {
                Log.i(TAG, "isRunning:**false**");
            }

            targetDataLine.open(audioformat);
            targetDataLine.start();
            Log.i(TAG, "getBufferSize=" + targetDataLine.getBufferSize());
        } catch(Exception e)
        {
            Log.i(TAG, "error starting java target line");
        }
    }

    private static synchronized void ffmpegav_change_java_audio_device(Mixer.Info i)
    {
        Log.i(TAG, "AA::IN::change_device:001:" + i.getDescription());
        Log.i(TAG, "select audio in:" + i.getDescription());

        try
        {
            Mixer currentMixer = AudioSystem.getMixer(i);
            Log.i(TAG, "select audio in:" + "sel:" + i.getDescription());

            if (targetDataLine != null)
            {
                try
                {
                    targetDataLine.stop();
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }

                try
                {
                    targetDataLine.flush();
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }

                try
                {
                    targetDataLine.close();
                    Log.i(TAG, "select audio in:" + "close old line");
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }

                targetDataLine = null;
            }

            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioformat);
            try
            {
                if (currentMixer.isLineSupported(dataLineInfo))
                {
                    Log.i(TAG, "linesupported:TRUE");
                }
                else
                {
                    Log.i(TAG, "linesupported:**false**");
                }

                if (dataLineInfo.isFormatSupported(audioformat))
                {
                    Log.i(TAG, "linesupported:TRUE");
                }
                else
                {
                    Log.i(TAG, "linesupported:**false**");
                }

                // targetDataLine = (TargetDataLine) currentMixer.getLine(dataLineInfo);
                targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);

                if (targetDataLine.isRunning())
                {
                    Log.i(TAG, "isRunning:TRUE");
                }
                else
                {
                    Log.i(TAG, "isRunning:**false**");
                }

                targetDataLine.open(audioformat);
                targetDataLine.start();
                Log.i(TAG, "getBufferSize=" + targetDataLine.getBufferSize());
            }
            catch (SecurityException se1)
            {
                se1.printStackTrace();
                Log.i(TAG, "select audio in:EE3:" + se1.getMessage());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                Log.i(TAG, "select audio in:EE2:" + e1.getMessage());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Utility class to allow OS determination
     * <p>
     * Created on Mar 11, 2010
     *
     * @author Eugene Ryzhikov
     */
    public enum OperatingSystem
    {

        WINDOWS("windows"), MACOS("mac"), MACARM("silicone"), RASPI("aarm64"), LINUX("linux"), UNIX("nix"), SOLARIS("solaris"),

        UNKNOWN("unknown")
                {
                    @Override
                    protected boolean isReal()
                    {
                        return false;
                    }
                };

        private String tag;

        OperatingSystem(String tag)
        {
            this.tag = tag;
        }

        public boolean isCurrent()
        {
            return isReal() && getName().toLowerCase().indexOf(tag) >= 0;
        }

        public static final String getName()
        {
            return System.getProperty("os.name");
        }

        public static final String getVersion()
        {
            return System.getProperty("os.version");
        }

        public static final String getArchitecture()
        {
            return System.getProperty("os.arch");
        }

        @Override
        public final String toString()
        {
            return String.format("%s v%s (%s)", getName(), getVersion(), getArchitecture());
        }

        protected boolean isReal()
        {
            return true;
        }

        /**
         * Returns current operating system
         *
         * @return current operating system or UNKNOWN if not found
         */
        public static final OperatingSystem getCurrent()
        {
            for (OperatingSystem os : OperatingSystem.values())
            {
                if (os.isCurrent())
                {
                    if (os == OperatingSystem.MACOS)
                    {
                        if (getArchitecture().equalsIgnoreCase("aarch64"))
                        {
                            return OperatingSystem.MACARM;
                        }
                    }
                    else if (os == OperatingSystem.LINUX)
                    {
                        if (getArchitecture().equalsIgnoreCase("aarch64"))
                        {
                            return OperatingSystem.RASPI;
                        }
                    }
                    return os;
                }
            }
            return UNKNOWN;
        }
    }

    public static int ffmpegav_loadjni(String jnilib_path) {
        String linux_lib_filename = null;
        if (OperatingSystem.getCurrent() == OperatingSystem.LINUX)
        {
            linux_lib_filename = jnilib_path + "/libffmpeg_av_jni.so";
        } else if (OperatingSystem.getCurrent() == OperatingSystem.RASPI)
        {
            linux_lib_filename = jnilib_path + "/libffmpeg_av_jni_raspi.so";
        } else if (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS)
        {
            linux_lib_filename = jnilib_path + "/ffmpeg_av_jni.dll";
        } else if (OperatingSystem.getCurrent() == OperatingSystem.MACOS)
        {
            linux_lib_filename = jnilib_path + "/libffmpeg_av_jni.jnilib";
        } else if (OperatingSystem.getCurrent() == OperatingSystem.MACARM)
        {
            linux_lib_filename = jnilib_path + "/libffmpeg_av_jni_arm64.jnilib";
        } else
        {
            Log.i(TAG,"OS:Unknown operating system: " + OperatingSystem.getCurrent());
            return -1;
        }

        if (linux_lib_filename == null) {
            Log.i(TAG,"OS:Unknown operating system (null): " + OperatingSystem.getCurrent());
            return -1;
        }

        try
        {
            System.load(linux_lib_filename);
            Log.i(TAG, "successfully loaded native library path: " + linux_lib_filename);
            return 0;
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            Log.i(TAG, "loadLibrary ffmpeg_av_jni failed! path: " + linux_lib_filename);
            e.printStackTrace();
            return -1;
        }
    }

    public static void main(String[] args) {
        int loadjni_res = -1;
        try {
            loadjni_res = ffmpegav_loadjni(new java.io.File(".").getAbsolutePath());
        } catch (Exception e) {
        }
        Log.i(TAG, "libavutil version: " + ffmpegav_libavutil_version());
        Log.i(TAG, "ffmpegav version: " + ffmpegav_version());
        Log.i(TAG, "ffmpegav commit: " + ffmpegav_GITHASH());
        // final int res = ffmpegav_init("./"); // exmaple with path must include the seperator at the end!
        final int res = ffmpegav_init(null); // exmaple with "null" -> filter data will be loaded from current directory
        Log.i(TAG, "ffmpeg init: " + res);

        final String[] video_in_devices = ffmpegav_get_video_in_devices();
        Log.i(TAG, "ffmpeg video in devices: " + video_in_devices.length);

        String vdevice = "";
        String vsource = "";

        String adevice = "";
        String asource = "";

        for (int i=0;i<video_in_devices.length;i++)
        {
            if (video_in_devices[i] != null)
            {
                final ffmpegav_descrid[] video_in_sources = ffmpegav_get_in_sources(video_in_devices[i], 1);
                if (video_in_sources != null)
                {
                    for (int j=0;j<video_in_sources.length;j++)
                    {
                        if (video_in_sources[j] != null)
                        {
                            Log.i(TAG, "ffmpeg video in source #"+i+": " + video_in_sources[j].id);
                        }
                    }
                }
            }
        }

        final boolean TEST_DEV_VIDEO_0 = false;

        if (TEST_DEV_VIDEO_0)
        {
            vdevice = "video4linux2";
            vsource = "/dev/video0";
            final int res_vd = ffmpegav_open_video_in_device(vdevice,
                    vsource, 640, 480, 20, 1);
            Log.i(TAG, "ffmpeg open video capture device: " + res_vd);
        }
        else
        {
            for (int i=0;i<video_in_devices.length;i++)
            {
                if (video_in_devices[i] != null)
                {
                    Log.i(TAG, "ffmpeg video in device #"+i+": " + video_in_devices[i]);
                    if (i == 1)
                    {
                        vdevice = video_in_devices[i];
                        vsource = ":0";
                        final int res_vd = ffmpegav_open_video_in_device(vdevice,
                                vsource, 640, 480, 30, 0);
                        Log.i(TAG, "ffmpeg open video capture device: " + res_vd);
                    }
                }
            }
        }

        final String[] audio_in_devices = ffmpegav_get_audio_in_devices_wrapper();
        Log.i(TAG, "ffmpeg audio in devices: " + audio_in_devices.length);
        for (int i=0;i<audio_in_devices.length;i++)
        {
            if (audio_in_devices[i] != null)
            {
                final ffmpegav_descrid[] audio_in_sources = ffmpegav_get_in_sources(audio_in_devices[i], 0);
                if (audio_in_sources != null)
                {
                    for (int j=0;j<audio_in_sources.length;j++)
                    {
                        if (audio_in_sources[j] != null)
                        {
                            Log.i(TAG, "ffmpeg audio in source id=#"+i+": " + audio_in_sources[j].id);
                            Log.i(TAG, "ffmpeg audio in source descr=#"+i+": " + audio_in_sources[j].description);
                        }
                    }
                }
            }
        }

        for (int i=0;i<audio_in_devices.length;i++)
        {
            if (audio_in_devices[i] != null)
            {
                Log.i(TAG, "ffmpeg audio in device #"+i+": " + audio_in_devices[i]);
                if (i == 1)
                {
                    adevice = audio_in_devices[i];
                    asource = "default";
                    final int res_ad = ffmpegav_open_audio_in_device_wrapper(audio_in_devices[i],
                            "default");
                    Log.i(TAG, "ffmpeg open audio capture device: " + res_ad);
                }
            }
        }

        final int frame_width_px1 = 640;
        final int frame_height_px1 = 480;
        final int buffer_size_in_bytes1 = ((frame_width_px1 * frame_height_px1) * 3) / 2;
        final java.nio.ByteBuffer video_buffer_1_y = java.nio.ByteBuffer.allocateDirect(buffer_size_in_bytes1);
        ffmpegav_set_JNI_video_buffer(video_buffer_1_y, frame_width_px1, frame_height_px1);

        final int frame_width_px2 = 640;
        final int frame_height_px2 = 480;
        final int buffer_size_in_bytes2 = 10; // ((frame_width_px2 * frame_height_px2) * 3) / 2;
        ffmpegav_video_buffer_2_y = java.nio.ByteBuffer.allocateDirect(buffer_size_in_bytes2);
        ffmpegav_video_buffer_2_u = java.nio.ByteBuffer.allocateDirect(buffer_size_in_bytes2);
        ffmpegav_video_buffer_2_v = java.nio.ByteBuffer.allocateDirect(buffer_size_in_bytes2);
        ffmpegav_set_JNI_video_buffer2(ffmpegav_video_buffer_2_y, ffmpegav_video_buffer_2_u, ffmpegav_video_buffer_2_v, frame_width_px2, frame_height_px2);


        ffmpegav_set_JNI_audio_buffer2(audio_buffer_2);

        ffmpegav_set_video_capture_callback(new video_capture_callback() {
            @Override
            public void onSuccess(long width, long height, long source_width, long source_height, long pts, int fps, int source_format) {
                Log.i(TAG, "ffmpeg open video capture onSuccess:" + width + " " + height + " " +
                source_width + " " + source_height + " " + pts + " fps: " + fps +
                " source_format: " + ffmpegav_video_source_format_name.value_str(source_format));
            }
            @Override
            public void onError() {
            }
            @Override
            public void onBufferTooSmall(int y_buffer_size, int u_buffer_size, int v_buffer_size) {
                Log.i(TAG, "Video buffer too small, needed sizes: " + y_buffer_size
                    + " " + u_buffer_size + " "+ v_buffer_size);
                ffmpegav_video_buffer_2_y = java.nio.ByteBuffer.allocateDirect(y_buffer_size);
                ffmpegav_video_buffer_2_u = java.nio.ByteBuffer.allocateDirect(u_buffer_size);
                ffmpegav_video_buffer_2_v = java.nio.ByteBuffer.allocateDirect(v_buffer_size);
                ffmpegav_set_JNI_video_buffer2(ffmpegav_video_buffer_2_y, ffmpegav_video_buffer_2_u, ffmpegav_video_buffer_2_v, frame_width_px2, frame_height_px2);
            }
        });

        ffmpegav_set_audio_capture_callback(new audio_capture_callback() {
            @Override
            public void onSuccess(long read_bytes, int out_samples, int out_channels, int out_sample_rate, long pts) {
                Log.i(TAG, "ffmpeg open audio capture onSuccess:" + read_bytes + " " + out_samples + " " + out_channels + " " + out_sample_rate + " " + pts);
                try
                {
                    audio_buffer_2.rewind();
                    final ffmpegav_ByteBufferCompat audio_buffer_2_ = new ffmpegav_ByteBufferCompat(audio_buffer_2);
                    Log.i(TAG, "audiobytes:" + bytesToHex(audio_buffer_2_.array(), 0, 100));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError() {
            }
            @Override
            public void onBufferTooSmall(int audio_buffer_size) {
                Log.i(TAG, "Audio buffer too small, needed size=" + audio_buffer_size);
            }
        });

        ffmpegav_start_video_in_capture();
        ffmpegav_apply_audio_filter(1);
        ffmpegav_start_audio_in_capture_wrapper();
        try
        {
            Thread.sleep(1000);
        }
        catch(Exception e)
        {
        }
        //
        //
        ffmpegav_stop_audio_in_capture_wrapper();
        ffmpegav_stop_video_in_capture();
        int res_aclose = ffmpegav_close_audio_in_device_wrapper();
        Log.i(TAG, "ffmpeg open close audio capture device: " + res_aclose);
        int res_vclose = ffmpegav_close_video_in_device();
        Log.i(TAG, "ffmpeg open close video capture device: " + res_vclose);
        //
        //
        // test if calling stop and close again does something bad
        //
        Log.i(TAG, "ffmpeg ========= stop and close again =========");
        Log.i(TAG, "ffmpeg ========= stop and close again =========");
        Log.i(TAG, "ffmpeg ========= stop and close again =========");
        Log.i(TAG, "ffmpeg ========= stop and close again =========");
        ffmpegav_stop_audio_in_capture_wrapper();
        ffmpegav_stop_video_in_capture();
        res_aclose = ffmpegav_close_audio_in_device_wrapper();
        Log.i(TAG, "ffmpeg open close audio capture device: " + res_aclose);
        res_vclose = ffmpegav_close_video_in_device();
        Log.i(TAG, "ffmpeg open close video capture device: " + res_vclose);
        Log.i(TAG, "ffmpeg ========= stop and close again =========");
        Log.i(TAG, "ffmpeg ========= stop and close again =========");
        Log.i(TAG, "ffmpeg ========= stop and close again =========");
        //
        //
        try
        {
            Thread.sleep(100);
        }
        catch(Exception e)
        {
        }

        // -----------------------
        // -----------------------
        final int res_vd2 = ffmpegav_open_video_in_device(vdevice,
            vsource, 640, 480, 15, 0);
        Log.i(TAG, "ffmpeg open video capture device: " + res_vd2);

        final int res_ad2 = ffmpegav_open_audio_in_device_wrapper(adevice,
            asource);
        Log.i(TAG, "ffmpeg open audio capture device: " + res_ad2);
        ffmpegav_start_video_in_capture();
        ffmpegav_start_audio_in_capture_wrapper();
        try
        {
            Thread.sleep(1000);
        }
        catch(Exception e)
        {
        }
        ffmpegav_stop_audio_in_capture_wrapper();
        ffmpegav_stop_video_in_capture();
        ffmpegav_close_audio_in_device_wrapper();
        ffmpegav_close_video_in_device();
        // -----------------------
        // -----------------------


        // -----------------------
        // -----------------------
        Log.i(TAG, "ffmpeg ========= test with empty parameters =========");
        Log.i(TAG, "ffmpeg ========= test with empty parameters =========");
        Log.i(TAG, "ffmpeg ========= test with empty parameters =========");
        final int res_vd3 = ffmpegav_open_video_in_device("",
                "", 640, 480, 30, 0);
        Log.i(TAG, "ffmpeg open video capture device: " + res_vd3);

        final int res_ad3 = ffmpegav_open_audio_in_device_wrapper("",
                "");
        Log.i(TAG, "ffmpeg open audio capture device: " + res_ad3);
        ffmpegav_start_video_in_capture();
        ffmpegav_start_audio_in_capture_wrapper();
        try
        {
            Thread.sleep(1000);
        }
        catch(Exception e)
        {
        }
        ffmpegav_stop_audio_in_capture_wrapper();
        ffmpegav_stop_video_in_capture();
        ffmpegav_close_audio_in_device_wrapper();
        ffmpegav_close_video_in_device();
        // -----------------------
        // -----------------------
        Log.i(TAG, "ffmpeg ========= all OK =========");
        Log.i(TAG, "ffmpeg ========= all OK =========");
        Log.i(TAG, "ffmpeg ========= all OK =========");


        Log.i(TAG, "ffmpeg ========= multi thread test START =========");
        final String vdevice_ = vdevice;
        final String vsource_ = vsource;
        final String adevice_ = adevice;
        final String asource_ = asource;
        mult_thr_test_finish = false;

        Thread t1 = new Thread() {
            @Override
            public void run() {
                Random r = new Random();
                while (!mult_thr_test_finish) {
                    try
                    {
                        Log.i(TAG, "T1: ffmpeg open video capture device:start");
                        final int res_vd2 = ffmpegav_open_video_in_device(vdevice_,
                            vsource_, 640, 480, 15, 0);
                        Log.i(TAG, "T1: ffmpeg open video capture device:done");
                        int low = 10;
                        int high = 80;
                        int result = r.nextInt(high-low) + low;
                        Thread.sleep(result);
                        Log.i(TAG, "T1: ffmpeg close video capture device:start");
                        ffmpegav_close_video_in_device();
                        Log.i(TAG, "T1: ffmpeg close video capture device:done");
                    }
                    catch(Exception e) {}
                }
            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                Random r = new Random();
                while (!mult_thr_test_finish) {
                    try
                    {
                        Log.i(TAG, "T2: ffmpeg open audio capture device:start");
                        final int res_ad2 = ffmpegav_open_audio_in_device_wrapper(adevice_, asource_);
                        Log.i(TAG, "T2: ffmpeg open audio capture device:done");
                        int low = 10;
                        int high = 80;
                        int result = r.nextInt(high-low) + low;
                        Thread.sleep(result);
                        Log.i(TAG, "T2: ffmpeg close audio capture device:start");
                        ffmpegav_close_audio_in_device_wrapper();
                        Log.i(TAG, "T2: ffmpeg close audio capture device:done");
                    }
                    catch(Exception e) {}
                }
            }
        };
        Thread t3 = new Thread() {
            @Override
            public void run() {
                Random r = new Random();
                while (!mult_thr_test_finish) {
                    try
                    {
                        Log.i(TAG, "T3: ffmpeg open video capture device:start");
                        final int res_vd2 = ffmpegav_open_video_in_device(vdevice_,
                            vsource_, 640, 480, 15, 0);
                        Log.i(TAG, "T3: ffmpeg open video capture device:done");
                        int low = 10;
                        int high = 80;
                        int result = r.nextInt(high-low) + low;
                        Thread.sleep(result);
                        Log.i(TAG, "T3: ffmpeg close video capture device:start");
                        ffmpegav_close_video_in_device();
                        Log.i(TAG, "T3: ffmpeg close video capture device:done");
                    }
                    catch(Exception e) {}
                }
            }
        };
        Thread t4 = new Thread() {
            @Override
            public void run() {
                Random r = new Random();
                while (!mult_thr_test_finish) {
                    try
                    {
                        Log.i(TAG, "T4: ffmpeg open audio capture device:start");
                        final int res_ad2 = ffmpegav_open_audio_in_device_wrapper(adevice_, asource_);
                        Log.i(TAG, "T4: ffmpeg open audio capture device:done");
                        int low = 10;
                        int high = 80;
                        int result = r.nextInt(high-low) + low;
                        Thread.sleep(result);
                        Log.i(TAG, "T4: ffmpeg close audio capture device:start");
                        ffmpegav_close_audio_in_device_wrapper();
                        Log.i(TAG, "T4: ffmpeg close audio capture device:done");
                    }
                    catch(Exception e) {}
                }
            }
        };
        Thread t5 = new Thread() {
            @Override
            public void run() {
                Random r = new Random();
                while (!mult_thr_test_finish) {
                    try
                    {
                        Log.i(TAG, "T5: ffmpeg start video capture: start");
                        ffmpegav_start_video_in_capture();
                        Log.i(TAG, "T5: ffmpeg start video capture: done");
                        int low = 10;
                        int high = 80;
                        int result = r.nextInt(high-low) + low;
                        Thread.sleep(result);
                        Log.i(TAG, "T5: ffmpeg stop video capture: start");
                        ffmpegav_stop_video_in_capture();
                        Log.i(TAG, "T5: ffmpeg stop video capture: done");
                    }
                    catch(Exception e) {}
                }
            }
        };
        Thread t6 = new Thread() {
            @Override
            public void run() {
                Random r = new Random();
                while (!mult_thr_test_finish) {
                    try
                    {
                        Log.i(TAG, "T6: ffmpeg start audio capture: start");
                        ffmpegav_start_audio_in_capture_wrapper();
                        Log.i(TAG, "T6: ffmpeg start audio capture: done");
                        int low = 10;
                        int high = 80;
                        int result = r.nextInt(high-low) + low;
                        Thread.sleep(result);
                        Log.i(TAG, "T6: ffmpeg stop audio capture: start");
                        ffmpegav_stop_audio_in_capture_wrapper();
                        Log.i(TAG, "T6: ffmpeg stop audio capture: done");
                    }
                    catch(Exception e) {}
                }
            }
        };
        Thread t7 = new Thread() {
            @Override
            public void run() {
                Random r = new Random();
                while (!mult_thr_test_finish) {
                    try
                    {
                        Log.i(TAG, "T7: ffmpeg start video capture: start");
                        ffmpegav_start_video_in_capture();
                        Log.i(TAG, "T7: ffmpeg start video capture: done");
                        int low = 10;
                        int high = 80;
                        int result = r.nextInt(high-low) + low;
                        Thread.sleep(result);
                        Log.i(TAG, "T7: ffmpeg stop video capture: start");
                        ffmpegav_stop_video_in_capture();
                        Log.i(TAG, "T7: ffmpeg stop video capture: done");
                    }
                    catch(Exception e) {}
                }
            }
        };
        Thread t8 = new Thread() {
            @Override
            public void run() {
                Random r = new Random();
                while (!mult_thr_test_finish) {
                    try
                    {
                        Log.i(TAG, "T8: ffmpeg start audio capture: start");
                        ffmpegav_start_audio_in_capture_wrapper();
                        Log.i(TAG, "T8: ffmpeg start audio capture: done");
                        int low = 10;
                        int high = 80;
                        int result = r.nextInt(high-low) + low;
                        Thread.sleep(result);
                        Log.i(TAG, "T8: ffmpeg stop audio capture: start");
                        ffmpegav_stop_audio_in_capture_wrapper();
                        Log.i(TAG, "T8: ffmpeg stop audio capture: done");
                    }
                    catch(Exception e) {}
                }
            }
        };
        Log.i(TAG, "ffmpeg ========= multi thread test RUN ===========");
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
        t8.start();
        try
        {
            Thread.sleep(10000);
        }
        catch(Exception e)
        {
        }
        Log.i(TAG, "ffmpeg ========= multi thread test STOP ==========");
        mult_thr_test_finish = true;
        ffmpegav_stop_video_in_capture();
        ffmpegav_stop_audio_in_capture();
        try
        {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
            t5.join();
            t6.join();
            t7.join();
            t8.join();
        }
        catch(Exception e)
        {
        }
        ffmpegav_stop_audio_in_capture_wrapper();
        ffmpegav_stop_video_in_capture();
        ffmpegav_close_audio_in_device_wrapper();
        ffmpegav_close_video_in_device();
        Log.i(TAG, "ffmpeg ========= multi thread test OK ============");
    }

    public static String bytesToHex(byte[] bytes, int start, int len)
    {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[(len) * 2];
        // System.out.println("blen=" + (len));

        for (int j = start; j < (start + len); j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[(j - start) * 2] = hexArray[v >>> 4];
            hexChars[(j - start) * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static class ffmpegav_ByteBufferCompat
    {
        byte[] b = null;
        ByteBuffer f = null;

        public ffmpegav_ByteBufferCompat(ByteBuffer bf)
        {
            this.f = bf;
            bf.rewind();
            this.b = new byte[bf.remaining()];
            bf.slice().get(this.b);
        }

        public byte[] array()
        {
            return b;
        }

        public int limit()
        {
            return this.f.limit();
        }

        public int arrayOffset()
        {
            return 0;
        }
    }
}


package com.zoffcc.applications.ffmpegav;

import java.nio.ByteBuffer;

public class AVActivity {

    private static final String TAG = "ffmpegav.AVActivity";
    static final String Version = "0.99.7";

    public static native String ffmpegav_version();
    public static native String ffmpegav_libavutil_version();
    public static native int ffmpegav_init(String resources_dir);
    public static native void ffmpegav_apply_audio_filter(int apply_filter);
    public static native String[] ffmpegav_get_video_in_devices();
    public static native String[] ffmpegav_get_audio_in_devices();
    public static native String[] ffmpegav_get_in_sources(String devicename, int is_video);
    public static native int ffmpegav_open_video_in_device(String deviceformat, String inputname, int wanted_width, int wanted_height, int fps);
    public static native int ffmpegav_open_audio_in_device(String deviceformat, String inputname);
    public static native int ffmpegav_start_video_in_capture();
    public static native int ffmpegav_start_audio_in_capture();
    public static native int ffmpegav_stop_video_in_capture();
    public static native int ffmpegav_stop_audio_in_capture();
    public static native int ffmpegav_close_audio_in_device();
    public static native int ffmpegav_close_video_in_device();

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

    final static int audio_buffer_size_in_bytes2 = 20000;
    final static java.nio.ByteBuffer audio_buffer_2 = java.nio.ByteBuffer.allocateDirect(audio_buffer_size_in_bytes2);

    public static interface video_capture_callback {
        void onSuccess(long width, long height, long source_width, long source_height, long pts, int fps, int source_format);
        void onError();
    }
    static video_capture_callback video_capture_callback_function = null;

    public static interface audio_capture_callback {
        void onSuccess(long read_bytes, int out_samples, int out_channels, int out_sample_rate, long pts);
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
    public static native void ffmpegav_set_JNI_audio_buffer2(java.nio.ByteBuffer audio_buffer2);

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

    /**
     * Utility class to allow OS determination
     * <p>
     * Created on Mar 11, 2010
     *
     * @author Eugene Ryzhikov
     */
    public enum OperatingSystem
    {

        WINDOWS("windows"), MACOS("mac"), LINUX("linux"), UNIX("nix"), SOLARIS("solaris"),

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
        } else if (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS)
        {
            linux_lib_filename = jnilib_path + "/ffmpeg_av_jni.dll";
        } else if (OperatingSystem.getCurrent() == OperatingSystem.MACOS)
        {
            linux_lib_filename = jnilib_path + "/libffmpeg_av_jni.jnilib";
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
                final String[] video_in_sources = ffmpegav_get_in_sources(video_in_devices[i], 1);
                if (video_in_sources != null)
                {
                    for (int j=0;j<video_in_sources.length;j++)
                    {
                        if (video_in_sources[j] != null)
                        {
                            Log.i(TAG, "ffmpeg video in source #"+i+": " + video_in_sources[j]);
                        }
                    }
                }
            }
        }
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
                            vsource, 640, 480, 30);
                    Log.i(TAG, "ffmpeg open video capture device: " + res_vd);
                }
            }
        }


        final String[] audio_in_devices = ffmpegav_get_audio_in_devices();
        Log.i(TAG, "ffmpeg audio in devices: " + audio_in_devices.length);
        for (int i=0;i<audio_in_devices.length;i++)
        {
            if (audio_in_devices[i] != null)
            {
                final String[] audio_in_sources = ffmpegav_get_in_sources(audio_in_devices[i], 0);
                if (audio_in_sources != null)
                {
                    for (int j=0;j<audio_in_sources.length;j++)
                    {
                        if (audio_in_sources[j] != null)
                        {
                            Log.i(TAG, "ffmpeg audio in source #"+i+": " + audio_in_sources[j]);
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
                    final int res_ad = ffmpegav_open_audio_in_device(audio_in_devices[i],
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
        final int buffer_size_in_bytes2 = ((frame_width_px2 * frame_height_px2) * 3) / 2;
        final java.nio.ByteBuffer video_buffer_2_y = java.nio.ByteBuffer.allocateDirect(buffer_size_in_bytes2);
        final java.nio.ByteBuffer video_buffer_2_u = java.nio.ByteBuffer.allocateDirect(buffer_size_in_bytes2);
        final java.nio.ByteBuffer video_buffer_2_v = java.nio.ByteBuffer.allocateDirect(buffer_size_in_bytes2);
        ffmpegav_set_JNI_video_buffer2(video_buffer_2_y, video_buffer_2_u, video_buffer_2_v, frame_width_px2, frame_height_px2);


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
        });

        ffmpegav_start_video_in_capture();
        ffmpegav_apply_audio_filter(1);
        ffmpegav_start_audio_in_capture();
        try
        {
            Thread.sleep(10000);
        }
        catch(Exception e)
        {
        }
        ffmpegav_stop_audio_in_capture();
        ffmpegav_stop_video_in_capture();


        final int res_aclose = ffmpegav_close_audio_in_device();
        Log.i(TAG, "ffmpeg open close audio capture device: " + res_aclose);

        final int res_vclose = ffmpegav_close_video_in_device();
        Log.i(TAG, "ffmpeg open close video capture device: " + res_vclose);

        try
        {
            Thread.sleep(100);
        }
        catch(Exception e)
        {
        }

        // -----------------------
        // -----------------------
        /*
        final int res_vd2 = ffmpegav_open_video_in_device(vdevice,
            vsource, 640, 480, 15);
        Log.i(TAG, "ffmpeg open video capture device: " + res_vd2);

        final int res_ad2 = ffmpegav_open_audio_in_device(adevice,
            asource);
        Log.i(TAG, "ffmpeg open audio capture device: " + res_ad2);
        ffmpegav_start_video_in_capture();
        ffmpegav_start_audio_in_capture();
        try
        {
            Thread.sleep(1000);
        }
        catch(Exception e)
        {
        }
        ffmpegav_stop_audio_in_capture();
        ffmpegav_stop_video_in_capture();
        ffmpegav_close_audio_in_device();
        ffmpegav_close_video_in_device();
        */
        // -----------------------
        // -----------------------


        // -----------------------
        // -----------------------
        final int res_vd3 = ffmpegav_open_video_in_device("",
                "", 640, 480, 30);
        Log.i(TAG, "ffmpeg open video capture device: " + res_vd3);

        final int res_ad3 = ffmpegav_open_audio_in_device("",
                "");
        Log.i(TAG, "ffmpeg open audio capture device: " + res_ad3);
        ffmpegav_start_video_in_capture();
        ffmpegav_start_audio_in_capture();
        try
        {
            Thread.sleep(1000);
        }
        catch(Exception e)
        {
        }
        ffmpegav_stop_audio_in_capture();
        ffmpegav_stop_video_in_capture();
        ffmpegav_close_audio_in_device();
        ffmpegav_close_video_in_device();
        // -----------------------
        // -----------------------

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


package com.zoffcc.applications.ffmpegav;

public class AVActivity {

    private static final String TAG = "ffmpegav.AVActivity";
    static final String Version = "0.99.0";

    public static native String ffmpegav_libavutil_version();
    public static native int ffmpegav_init();
    public static native String[] ffmpegav_get_video_in_devices();
    public static native int ffmpegav_open_video_in_device(String deviceformat, int wanted_width, int wanted_height, String x11_display_num, int fps);
    public static native int ffmpegav_start_video_in_capture();
    public static native int ffmpegav_stop_video_in_capture();
    public static native int ffmpegav_close_video_in_device();

    public static interface video_capture_callback {
        void onSuccess(long width, long height, long pts);
        void onError();
    }
    static video_capture_callback video_capture_callback_function = null;

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

    public static void ffmpegav_callback_video_capture_frame_pts_cb_method(long width, long height, long pts)
    {
        Log.i(TAG, "capture video frame w: " + width + " h: " + height + " pts: " + pts);
        video_capture_callback_function.onSuccess(width, height, pts);
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
        final int res = ffmpegav_init();
        Log.i(TAG, "ffmpeg init: " + res);
        final String[] video_in_devices = ffmpegav_get_video_in_devices();
        Log.i(TAG, "ffmpeg video in devices: " + video_in_devices.length);
        for (int i=0;i<video_in_devices.length;i++)
        {
            if (video_in_devices[i] != null)
            {
                Log.i(TAG, "ffmpeg video in device #"+i+": " + video_in_devices[i]);
                if (i == 1)
                {
                    final int res_vd = ffmpegav_open_video_in_device(video_in_devices[i], 640, 480, ":0.0", 15);
                    Log.i(TAG, "ffmpeg open video capture device: " + res_vd);
                }
            }
        }

        ffmpegav_set_video_capture_callback(new video_capture_callback() {
            @Override
            public void onSuccess(long width, long height, long pts) {
                Log.i(TAG, "ffmpeg open video capture onSuccess:" + width + " " + height + " " + pts);
            }
            @Override
            public void onError() {
            }
        });

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

        ffmpegav_start_video_in_capture();
        try
        {
            Thread.sleep(1 * 1000);
        }
        catch(Exception e)
        {
        }
        ffmpegav_stop_video_in_capture();

        final int res_vclose = ffmpegav_close_video_in_device();
        Log.i(TAG, "ffmpeg open close capture device: " + res_vclose);
    }
}


package com.zoffcc.applications.jninotifications;

import java.io.File;

public class NTFYActivity {
    private static final String TAG = "NTFYActivity";
    static final String Version = "0.99.9";

    public static native String jninotifications_version();
    public static native int jninotifications_notify(String application, String title,
                                                     String message, String icon_filename_fullpath);

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

    public static int jninotifications_loadjni(String jnilib_path) {
        String linux_lib_filename = null;
        if (OperatingSystem.getCurrent() == OperatingSystem.LINUX)
        {
            linux_lib_filename = jnilib_path + "/libjni_notifications.so";
        } else if (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS)
        {
            linux_lib_filename = jnilib_path + "/jni_notificationsi.dll";
        } else if (OperatingSystem.getCurrent() == OperatingSystem.MACOS)
        {
            linux_lib_filename = jnilib_path + "/libjni_notifications.jnilib";
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
            Log.i(TAG, "loadLibrary libjni_notifications failed! path: " + linux_lib_filename);
            e.printStackTrace();
            return -1;
        }
    }

    public static void main(String[] args) {
        int loadjni_res = -1;
        try {
            loadjni_res = jninotifications_loadjni(new java.io.File(".").getAbsolutePath());
        } catch (Exception e) {
        }

        File icon_file = new File("./icon-linux.png");
        String icon_path = icon_file.getAbsolutePath();

        Log.i(TAG, "jninotifications version: " + jninotifications_version());
        jninotifications_notify("test application",
                "title", "message",
                icon_path);
    }
}
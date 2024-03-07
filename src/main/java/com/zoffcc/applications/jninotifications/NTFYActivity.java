package com.zoffcc.applications.jninotifications;

import java.io.File;

public class NTFYActivity {
    private static final String TAG = "NTFYActivity";
    static final String Version = "0.99.2";

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

    public static int jninotifications_loadjni(String jnilib_path) {
        String linux_lib_filename = null;
        if (OperatingSystem.getCurrent() == OperatingSystem.LINUX)
        {
            linux_lib_filename = jnilib_path + "/libjni_notifications.so";
        } else if (OperatingSystem.getCurrent() == OperatingSystem.RASPI)
        {
            linux_lib_filename = jnilib_path + "/libjni_notifications_raspi.so";
        } else if (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS)
        {
            linux_lib_filename = jnilib_path + "/jni_notificationsi.dll";
        } else if (OperatingSystem.getCurrent() == OperatingSystem.MACOS)
        {
            linux_lib_filename = jnilib_path + "/libjni_notifications.jnilib";
        } else if (OperatingSystem.getCurrent() == OperatingSystem.MACARM)
        {
            linux_lib_filename = jnilib_path + "/libjni_notifications_arm64.jnilib";
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

        final int s = 100;

        jninotifications_notify("test application",
            "title", "message",
            icon_path);
        try{Thread.sleep(s);}catch(Exception e){}

        jninotifications_notify(null,
            null, null,
            null);
        try{Thread.sleep(s);}catch(Exception e){}

        jninotifications_notify(null,
            "1b", "1c",
            "1d");
        try{Thread.sleep(s);}catch(Exception e){}

        jninotifications_notify("2a",
            null, "2c",
            "2d");
        try{Thread.sleep(s);}catch(Exception e){}

        jninotifications_notify("3a",
            "3b", null,
            "3d");
        try{Thread.sleep(s);}catch(Exception e){}

        jninotifications_notify("4a",
            "4b", "4c",
            null);
        try{Thread.sleep(s);}catch(Exception e){}

        jninotifications_notify("हिन्दी",
            "हिन्दी", "हिन्दी",
            icon_path);
        try{Thread.sleep(s);}catch(Exception e){}

        jninotifications_notify("iconpathhindi",
            "हिन्दी", "हिन्दी",
            "हिन्दी");


        // HINT: it should only show 4 notifications (and also not crash)
    }
}
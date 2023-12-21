/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2021 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.File;

import static com.zoffcc.applications.jninotifications.NTFYActivity.jninotifications_notify;
import static com.zoffcc.applications.trifa.MainActivity.getDB_PREF__notifications_active;

public class HelperNotification
{
    private static final String TAG = "trifa.HelperNotification";

    static TrayIcon trayIcon = null;
    static long last_message_timestamp = -1L;
    static final String notify_send_full_path = "/usr/bin/notify-send";
    static final String notify_send_full_path_aternative = "/bin/notify-send";
    static String resources_dir = null;

    public static void set_resouces_dir(String dir)
    {
        resources_dir = dir;
    }

    public static void displayNotification(final String message)
    {
        displayNotification_with_force_option(message, false);
    }

    public static void displayNotification_with_force_option(final String message, final boolean force)
    {
        if ((!getDB_PREF__notifications_active()) && (!force))
        {
            return;
        }

        try
        {
            if ((last_message_timestamp + (2 * 1000)) > System.currentTimeMillis())
            {
                // HINT: flood protection
                // only allow this function to display message notification every 2 seconds
                return;
            }

            last_message_timestamp = System.currentTimeMillis();

            final String application = "TRIfA";
            final String title = "TRIfA";
            final String os = System.getProperty("os.name");
            if (os.contains("Linux"))
            {
                int res_jni_notify = -1;
                if (MainActivity.getNative_notification_lib_loaded_error() == 0)
                {
                    String icon_path = null;
                    if (resources_dir != null)
                    {
                        icon_path = resources_dir + File.separator + "icon-linux.png";
                    }
                    res_jni_notify = jninotifications_notify(application,
                            title, message,
                            icon_path);
                    Log.i(TAG, "using native JNI for Notification");
                }

                if (res_jni_notify != 0) {
                    final File f1 = new File(notify_send_full_path);
                    final File f2 = new File(notify_send_full_path_aternative);
                    if (f1.exists() && f1.isFile()) {
                        Log.i(TAG, "using notify-send for Notification");
                        ProcessBuilder builder = new ProcessBuilder(notify_send_full_path, "-a", "TRIfA", "" + filter_out_specials_2(title), "" + filter_out_specials_2(message));
                        builder.inheritIO().start();
                    } else if (f2.exists() && f2.isFile()) {
                        Log.i(TAG, "using notify-send alternative path for Notification");
                        ProcessBuilder builder = new ProcessBuilder(notify_send_full_path, "-a", "TRIfA", "" + filter_out_specials_2(title), "" + filter_out_specials_2(message));
                        builder.inheritIO().start();
                    } else {
                        Log.i(TAG, "using zenity for Notification");
                        ProcessBuilder builder = new ProcessBuilder("zenity", "--notification", "--title=" + filter_out_specials_2(title), "--text=" + filter_out_specials_2(message));
                        builder.inheritIO().start();
                    }
                }
            }
            else if (os.contains("Mac"))
            {
                int res_jni_notify = -1;
                if (MainActivity.getNative_notification_lib_loaded_error() == 0)
                {
                    String icon_path = null;
                    if (resources_dir != null)
                    {
                        icon_path = resources_dir + File.separator + "icon-linux.png";
                    }
                    res_jni_notify = jninotifications_notify(application,
                            title, message,
                            icon_path);
                    Log.i(TAG, "using macOS objC native JNI for Notification");
                }

                if (res_jni_notify != 0) {
                    Log.i(TAG, "using osascript for Notification");
                    ProcessBuilder builder = new ProcessBuilder("osascript", "-e", "display notification \"" + filter_out_specials_2(message) + "\"" + " with title \"" + filter_out_specials_2(title) + "\"");
                    builder.inheritIO().start();
                }
            }
            else if (SystemTray.isSupported())
            {
                Log.i(TAG, "using SystemTray for Notification");
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String filter_out_specials_2(String in)
    {
        try
        {
            final String out = in.replaceAll("[^ a-zA-Z0-9:]", "_");
            try
            {
                if (out.length() > 50) {
                    return out.substring(0, 50);
                }
                return out;
            }
            catch (Exception e)
            {
                return out;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return in;
        }
    }

    public static void init_system_tray(final String icon_file_full_path)
    {
        try
        {
            if (SystemTray.isSupported())
            {
                Log.i(TAG, "SystemTray supported on this platform");
                if (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS)
                {
                    SystemTray tray = SystemTray.getSystemTray();
                    Image image = Toolkit.getDefaultToolkit().getImage(icon_file_full_path);
                    trayIcon = new TrayIcon(image, "TRIfA");
                    trayIcon.setImageAutoSize(true);
                    tray.add(trayIcon);
                    Log.i(TAG, "SystemTray added");
                }
                else
                {
                    Log.i(TAG, "SystemTray will not be used on this platform");
                }
            }
            else
            {
                Log.i(TAG, "SystemTray NOT supported on this platform");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

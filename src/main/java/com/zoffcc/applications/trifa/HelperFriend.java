package com.zoffcc.applications.trifa;

import com.zoffcc.applications.sorm.TRIFADatabaseGlobalsNew;
import org.sqlite.SQLiteException;

import java.nio.ByteBuffer;

public class HelperFriend {

    private static final String TAG = "trifa.Hlp.Friend";

    static void send_friend_msg_receipt_v2_wrapper(final long friend_number, final int msg_type, final ByteBuffer msg_id_buffer, long t_sec_receipt) {
        // (msg_type == 1) msgV2 direct message
        // (msg_type == 2) msgV2 relay message
        // (msg_type == 3) msgV2 group confirm msg received message
        // (msg_type == 4) msgV2 confirm unknown received message
        if (msg_type == 1) {
            // send message receipt v2
            MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);
        } else if (msg_type == 2) {
            MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt,
                    msg_id_buffer);
        } else if (msg_type == 3) {
            // send message receipt v2
            MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);
        } else if (msg_type == 4) {
            // send message receipt v2 for unknown message
            MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);
        }
    }

    static void delete_friend_all_filetransfers(final String friendpubkey)
    {
        try
        {
            Log.i(TAG, "delete_ft:ALL for friend=" + friendpubkey);
            TrifaToxService.Companion.getOrma().deleteFromFiletransfer().
                    tox_public_key_stringEq(friendpubkey).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void delete_friend_all_messages(final String friendpubkey)
    {
        TrifaToxService.Companion.getOrma().
                deleteFromMessage().tox_friendpubkeyEq(friendpubkey).execute();
    }

    static void delete_friend(final String friend_pubkey)
    {
        TrifaToxService.Companion.getOrma().deleteFromFriendList().
                tox_public_key_stringEq(friend_pubkey).
                execute();
    }

    public static String get_g_opts(String key)
    {
        try
        {
            if (TrifaToxService.Companion.getOrma().selectFromTRIFADatabaseGlobalsNew().keyEq(key).count() == 1)
            {
                TRIFADatabaseGlobalsNew g_opts = TrifaToxService.Companion.getOrma().selectFromTRIFADatabaseGlobalsNew().keyEq(key).get(0);
                // Log.i(TAG, "get_g_opts:(SELECT):key=" + key);
                return g_opts.value;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_g_opts:EE1:" + e.getMessage());
            return null;
        }
    }

    public static void set_g_opts(String key, String value)
    {
        try
        {
            TRIFADatabaseGlobalsNew g_opts = new TRIFADatabaseGlobalsNew();
            g_opts.key = key;
            g_opts.value = value;

            try
            {
                TrifaToxService.Companion.getOrma().insertIntoTRIFADatabaseGlobalsNew(g_opts);
                Log.i(TAG, "set_g_opts:(INSERT):key=" + key + " value=" + "xxxxxxxxxxxxx");
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                try
                {
                    TrifaToxService.Companion.getOrma().updateTRIFADatabaseGlobalsNew().keyEq(key).value(value).execute();
                    Log.i(TAG, "set_g_opts:(UPDATE):key=" + key + " value=" + "xxxxxxxxxxxxxxx");
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                    Log.i(TAG, "set_g_opts:EE1:" + e2.getMessage());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_g_opts:EE2:" + e.getMessage());
        }
    }

    public static void del_g_opts(String key)
    {
        try
        {
            TrifaToxService.Companion.getOrma().deleteFromTRIFADatabaseGlobalsNew().keyEq(key).execute();
            Log.i(TAG, "del_g_opts:(DELETE):key=" + key);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "del_g_opts:EE2:" + e.getMessage());
        }
    }
}

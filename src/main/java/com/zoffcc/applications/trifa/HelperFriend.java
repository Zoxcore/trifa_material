package com.zoffcc.applications.trifa;

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
}

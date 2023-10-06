package com.zoffcc.applications.trifa;

import java.nio.ByteBuffer;

import static com.zoffcc.applications.trifa.HelperGeneric.hexstring_to_bytebuffer;
import static com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK;

public class HelperMessage {
    static void send_msgv3_high_level_ack(final long friend_number, String msgV3hash_hex_string)
    {
        if (msgV3hash_hex_string.length() < TOX_HASH_LENGTH)
        {
            return;
        }
        ByteBuffer hash_bytes = HelperGeneric.hexstring_to_bytebuffer(msgV3hash_hex_string);

        long t_sec = (System.currentTimeMillis() / 1000);
        long res = MainActivity.tox_messagev3_friend_send_message(friend_number,
                TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK.value,
                "_", hash_bytes, t_sec);
    }
}

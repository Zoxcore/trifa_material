package com.zoffcc.applications.trifa;

import java.nio.ByteBuffer;

import static com.zoffcc.applications.trifa.MainActivity.tox_group_by_chat_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_chat_id;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GROUP_ID_LENGTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.UINT32_MAX_JAVA;

public class HelperGroup {

    public static byte[] hex_to_bytes(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static long tox_group_by_groupid__wrapper(String group_id_string)
    {
        ByteBuffer group_id_buffer = ByteBuffer.allocateDirect(GROUP_ID_LENGTH);
        byte[] data = hex_to_bytes(group_id_string.toUpperCase());
        group_id_buffer.put(data);
        group_id_buffer.rewind();

        long res = tox_group_by_chat_id(group_id_buffer);
        if (res == UINT32_MAX_JAVA)
        {
            return -1;
        }
        else if (res < 0)
        {
            return -1;
        }
        else
        {
            return res;
        }
    }

    public static String tox_group_by_groupnum__wrapper(long groupnum)
    {
        try
        {
            ByteBuffer groupid_buf = ByteBuffer.allocateDirect(GROUP_ID_LENGTH * 2);
            if (tox_group_get_chat_id(groupnum, groupid_buf) == 0)
            {
                byte[] groupid_buffer = new byte[GROUP_ID_LENGTH];
                groupid_buf.get(groupid_buffer, 0, GROUP_ID_LENGTH);
                return bytes_to_hex(groupid_buffer);
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static String bytes_to_hex(byte[] in)
    {
        try
        {
            final StringBuilder builder = new StringBuilder();

            for (byte b : in)
            {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "*ERROR*";
    }

    public static String fourbytes_of_long_to_hex(final long in)
    {
        return String.format("%08x", in);
    }
}

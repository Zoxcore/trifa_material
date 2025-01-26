package com.zoffcc.applications.trifa;

import com.zoffcc.applications.sorm.GroupMessage;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.zoffcc.applications.trifa.HelperFiletransfer.get_incoming_filetransfer_local_filename;
import static com.zoffcc.applications.trifa.HelperFiletransfer.save_group_incoming_file;
import static com.zoffcc.applications.trifa.HelperGeneric.getHexArray;
import static com.zoffcc.applications.trifa.MainActivity.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.ToxVars.*;
import static com.zoffcc.applications.trifa.ToxVars.GC_MAX_SAVED_PEERS;

public class HelperGroup {

    private static final String TAG = "trifa.Hlp.Group";

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
                return bytes_to_hex(groupid_buffer).toLowerCase();
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

    public static String bytebuffer_to_hexstring(ByteBuffer in, boolean upper_case)
    {
        try
        {
            in.rewind();
            StringBuilder sb = new StringBuilder("");
            while (in.hasRemaining())
            {
                if (upper_case)
                {
                    sb.append(String.format("%02X", in.get()));
                }
                else
                {
                    sb.append(String.format("%02x", in.get()));
                }
            }
            in.rewind();
            return sb.toString();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static class incoming_group_file_meta_data
    {
        long rowid;
        String message_text;
        String path_name;
        String file_name;
        String msg_id_hash;
    }

    static incoming_group_file_meta_data handle_incoming_group_file(String msg_id_hash_string,
                        String sender_peer_name,
                        String sender_peer_pubkey, boolean was_synced,
                        long timestamp, long group_number, long sender_peer_num, byte[] data,
                        long length, long header)
    {
        incoming_group_file_meta_data ret = new incoming_group_file_meta_data();
        ret.message_text = null;
        ret.path_name = null;
        ret.file_name = null;
        ret.msg_id_hash = "";
        try
        {
            long res = tox_group_self_get_peer_id(group_number);
            if (res == sender_peer_num)
            {
                // HINT: do not add our own messages, they are already in the DB!
                Log.i(TAG, "group_custom_packet_cb:gn=" + group_number + " peerid=" + sender_peer_num + " ignoring own file");
                return null;
            }

            String group_id = "-1";
            try
            {
                group_id = tox_group_by_groupnum__wrapper(group_number);
            }
            catch (Exception ignored)
            {
            }

            if (group_id.compareTo("-1") == 0)
            {
                return null;
            }

            String filename = "image.jpg";
            if (!was_synced) {
                ByteBuffer filename_bytes = ByteBuffer.allocateDirect(TOX_MAX_FILENAME_LENGTH);
                filename_bytes.put(data, 8 + 32 + 4, 255);
                filename_bytes.rewind();
                try {
                    byte[] filename_byte_buf = new byte[255];
                    Arrays.fill(filename_byte_buf, (byte) 0x0);
                    filename_bytes.rewind();
                    filename_bytes.get(filename_byte_buf);

                    int start_index = 0;
                    int end_index = 254;
                    for (int j = 0; j < 255; j++) {
                        if (filename_byte_buf[j] == 0) {
                            start_index = j + 1;
                        } else {
                            break;
                        }
                    }

                    for (int j = 254; j >= 0; j--) {
                        if (filename_byte_buf[j] == 0) {
                            end_index = j;
                        } else {
                            break;
                        }
                    }

                    Log.i(TAG, "group_custom_packet_cb:start_index=" + start_index + " end_index=" + end_index);
                    byte[] filename_byte_buf_stripped = Arrays.copyOfRange(filename_byte_buf, start_index, end_index);
                    filename = new String(filename_byte_buf_stripped, StandardCharsets.UTF_8);
                    Log.i(TAG, "group_custom_packet_cb:filename str=" + filename);

                    filename_bytes.rewind();
                    ByteBufferCompat filename_bytes_compat = new ByteBufferCompat(filename_bytes);
                    Log.i(TAG, "group_custom_packet_cb:filename:" + filename_bytes_compat.arrayOffset() + " " + filename_bytes_compat.limit() + " " + filename_bytes_compat.array().length);
                    Log.i(TAG, "group_custom_packet_cb:filename hex=" + HelperGeneric.bytesToHex(filename_bytes_compat.array(), filename_bytes_compat.arrayOffset(), filename_bytes_compat.limit()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            long file_size = length - header;
            if (was_synced) {
                file_size = length;
            }
            if (file_size < 1)
            {
                Log.i(TAG, "group_custom_packet_cb: file size less than 1 byte");
                return null;
            }

            Log.i(TAG, "group_custom_packet_cb: filename=" + filename + " group_id.toLowerCase()=" + group_id.toLowerCase());
            String filename_corrected = get_incoming_filetransfer_local_filename(filename, group_id.toLowerCase());

            String tox_peerpk = sender_peer_pubkey.toUpperCase();
            String peername = sender_peer_name;

            GroupMessage m = new GroupMessage();
            m.is_new = false;
            m.tox_group_peer_pubkey = tox_peerpk;
            m.direction = TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value;
            m.TOX_MESSAGE_TYPE = 0;
            m.read = true;
            m.tox_group_peername = peername;
            m.private_message = 0;
            m.group_identifier = group_id.toLowerCase();
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_FILE.value;
            m.rcvd_timestamp = timestamp;
            m.sent_timestamp = timestamp;
            m.text = filename_corrected + "\n" + file_size + " bytes";
            m.message_id_tox = "";
            m.was_synced = was_synced;
            m.path_name = VFS_FILE_DIR + "/" + m.group_identifier + "/";
            m.file_name = filename_corrected;
            m.filename_fullpath = m.path_name + m.file_name;
            m.msg_id_hash = msg_id_hash_string;
            m.filesize = file_size;

            ret.msg_id_hash = m.msg_id_hash;

            File f1 = new File(m.path_name + "/" + m.file_name);
            File f2 = new File(f1.getParent());
            f2.mkdirs();

            if (was_synced) {
                save_group_incoming_file(m.path_name, m.file_name, data, 0, file_size);
            } else {
                save_group_incoming_file(m.path_name, m.file_name, data, header, file_size);
            }
            long row_id = -1;
            try
            {
                row_id = TrifaToxService.Companion.getOrma().insertIntoGroupMessage(m);
            } catch (Exception e)
            {
            }

            ret.message_text =  m.text;
            ret.path_name = m.path_name;
            ret.file_name = m.file_name;
            ret.rowid = row_id;

            return ret; // return metadata
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    static void delete_group_all_messages(final String group_identifier)
    {
        try
        {
            TrifaToxService.Companion.getOrma().deleteFromGroupMessage().group_identifierEq(group_identifier.toLowerCase()).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "group_conference_all_messages:EE:" + e.getMessage());
        }
    }

    static void delete_group(final String group_identifier)
    {
        try
        {
            TrifaToxService.Companion.getOrma().deleteFromGroupDB().group_identifierEq(group_identifier.toLowerCase()).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "delete_group:EE:" + e.getMessage());
        }
    }

    static int send_group_image(final GroupMessage g)
    {
        // @formatter:off
        /*
           40000 max bytes length for custom lossless NGC packets.
           37000 max bytes length for file and header, to leave some space for offline message syncing.

        | what      | Length in bytes| Contents                                           |
        |------     |--------        |------------------                                  |
        | magic     |       6        |  0x667788113435                                    |
        | version   |       1        |  0x01                                              |
        | pkt id    |       1        |  0x11                                              |
        | msg id    |      32        | *uint8_t  to uniquely identify the message         |
        | create ts |       4        |  uint32_t unixtimestamp in UTC of local wall clock |
        | filename  |     255        |  len TOX_MAX_FILENAME_LENGTH                       |
        |           |                |      data first, then pad with NULL bytes          |
        | data      |[1, 36701]      |  bytes of file data, zero length files not allowed!|


        header size: 299 bytes
        data   size: 1 - 36701 bytes
         */
        // @formatter:on

        final long header = 6 + 1 + 1 + 32 + 4 + 255;
        long data_length = header + g.filesize;

        if ((data_length > TOX_MAX_NGC_FILE_AND_HEADER_SIZE) || (data_length < (header + 1)))
        {
            Log.i(TAG, "send_group_image: data length has wrong size: " + data_length);
            return -1;
        }

        ByteBuffer data_buf = ByteBuffer.allocateDirect((int)data_length);

        data_buf.rewind();
        //
        data_buf.put((byte)0x66);
        data_buf.put((byte)0x77);
        data_buf.put((byte)0x88);
        data_buf.put((byte)0x11);
        data_buf.put((byte)0x34);
        data_buf.put((byte)0x35);
        //
        data_buf.put((byte)0x01);
        //
        data_buf.put((byte)0x11);
        //
        try
        {
            data_buf.put(hex_to_bytes(g.msg_id_hash), 0, 32);
        }
        catch(Exception e)
        {
            for(int jj=0;jj<32;jj++)
            {
                data_buf.put((byte)0x0);
            }
        }
        //
        // TODO: write actual timestamp into buffer
        data_buf.put((byte)0x0);
        data_buf.put((byte)0x0);
        data_buf.put((byte)0x0);
        data_buf.put((byte)0x0);
        //
        byte[] fn = "image.jpg".getBytes(StandardCharsets.UTF_8);
        try
        {
            if (g.file_name.getBytes(StandardCharsets.UTF_8).length <= 255)
            {
                fn = g.file_name.getBytes(StandardCharsets.UTF_8);
            }
        }
        catch(Exception ignored)
        {
        }
        data_buf.put(fn);
        for (int k=0;k<(255 - fn.length);k++)
        {
            // fill with null bytes up to 255 for the filename
            data_buf.put((byte) 0x0);
        }
        // -- now fill the data from file --
        java.io.File img_file = new java.io.File(g.filename_fullpath);

        long length_sum = 0;
        java.io.FileInputStream is = null;
        try
        {
            is = new java.io.FileInputStream(img_file);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = is.read(buffer)) > 0)
            {
                data_buf.put(buffer, 0, length);
                length_sum = length_sum + length;
                // Log.i(TAG,"put " + length + " bytes into buffer");
            }
        }
        catch(Exception e)
        {
        }
        finally
        {
            try
            {
                is.close();
            }
            catch(Exception e2)
            {
            }
        }
        Log.i(TAG,"put " + length_sum + " bytes TOTAL into buffer, and should match " + g.filesize);
        // -- now fill the data from file --

        byte[] data = new byte[(int)data_length];
        data_buf.rewind();
        data_buf.get(data);
        int res = tox_group_send_custom_packet(tox_group_by_groupid__wrapper(g.group_identifier),
                1,
                data,
                (int)data_length);
        return res;
    }

    /*
    this is a bit costly, asking for pubkeys of all group peers
    */
    static long get_group_peernum_from_peer_pubkey(final String group_identifier, final String peer_pubkey)
    {
        try
        {
            long group_num = tox_group_by_groupid__wrapper(group_identifier);
            long num_peers = MainActivity.tox_group_peer_count(group_num);

            if (num_peers > 0)
            {
                long[] peers = tox_group_get_peerlist(group_num);
                if (peers != null)
                {
                    long i = 0;
                    for (i = 0; i < num_peers; i++)
                    {
                        try
                        {
                            String pubkey_try = tox_group_peer_get_public_key(group_num, peers[(int) i]);
                            if (pubkey_try != null)
                            {
                                if (pubkey_try.equals(peer_pubkey))
                                {
                                    // we found the peer number
                                    return peers[(int) i];
                                }
                            }
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }
            }
            return -2;
        }
        catch (Exception e)
        {
            return -2;
        }
    }

    public static String bytesToHexJava(byte[] bytes, int start, int len)
    {
        char[] hexChars = new char[(len) * 2];
        // System.out.println("blen=" + (len));

        for (int j = start; j < (start + len); j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[(j - start) * 2] = getHexArray()[v >>> 4];
            hexChars[(j - start) * 2 + 1] = getHexArray()[v & 0x0F];
        }

        return new String(hexChars);
    }

    private static void send_ngch_syncfile(final String group_identifier, final String peer_pubkey, final GroupMessage m)
    {
        try
        {
            Random rand = new Random();
            int rndi = rand.nextInt(301);
            int n = 300 + rndi;
            // Log.i(TAG, "send_ngch_syncfile: sleep for " + n + " ms");
            Thread.sleep(n);
            //
            final int header_length = 6 + 1 + 1 + 32 + 32 + 4 + 25 + 255;
            final java.io.File f1 = new java.io.File(m.path_name + "/" + m.file_name);

            long data_length_ = header_length + f1.length();
            long f_length = f1.length();

            // Log.i(TAG, "send_ngch_syncfile: file=" + m.path_name + "__/__" + m.file_name + " " + m.filename_fullpath);
            // Log.i(TAG, "send_ngch_syncfile: data_length=" + data_length_ + " header_length=" +
            //         header_length + " filesize=" + f_length);

            if (data_length_ < (header_length + 1) || (data_length_ > 40000))
            {
                // Log.i(TAG, "send_ngch_syncfile: some error in calculating data length [1]");
                return;
            }

            final int data_length = (int)data_length_;
            ByteBuffer data_buf = ByteBuffer.allocateDirect(data_length);

            data_buf.rewind();
            //
            data_buf.put((byte) 0x66);
            data_buf.put((byte) 0x77);
            data_buf.put((byte) 0x88);
            data_buf.put((byte) 0x11);
            data_buf.put((byte) 0x34);
            data_buf.put((byte) 0x35);
            //
            data_buf.put((byte) 0x1);
            //
            data_buf.put((byte) 0x3);
            // should be 32 bytes
            try
            {
                // Log.i(TAG, "send_ngch_syncfile:send msg_id_hash=" + m.msg_id_hash);
                data_buf.put(hex_to_bytes(m.msg_id_hash), 0,32);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                for(int jj=0;jj<32;jj++)
                {
                    data_buf.put((byte) 0x0);
                }
            }
            // should be 32 bytes
            try
            {
                data_buf.put(hex_to_bytes(m.tox_group_peer_pubkey), 0, 32);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                for(int jj=0;jj<32;jj++)
                {
                    data_buf.put((byte) 0x0);
                }
            }
            //
            // unix timestamp
            long timestamp_tmp = (m.sent_timestamp / 1000);
            // Log.i(TAG,"send_ngch_syncfile:outgoing_timestamp=" + timestamp_tmp);
            ByteBuffer temp_buffer = ByteBuffer.allocate(8);
            temp_buffer.putLong(timestamp_tmp).order(ByteOrder.BIG_ENDIAN);
            temp_buffer.position(4);
            data_buf.put(temp_buffer);
            //Log.i(TAG,"send_ngch_syncmsg:send_ts_bytes:" +
            //          HelperGeneric.bytesToHex(temp_buffer.array(), temp_buffer.arrayOffset(), temp_buffer.limit()));
            //
            byte[] fn = "peer".getBytes(StandardCharsets.UTF_8);
            try
            {
                final long groupnum = tox_group_by_groupid__wrapper(m.group_identifier);
                final long peernum = tox_group_peer_by_public_key(groupnum, m.tox_group_peer_pubkey);
                final String peer_name = tox_group_peer_get_name(groupnum, peernum);
                if (peer_name.getBytes(StandardCharsets.UTF_8).length > TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES)
                {
                    fn = Arrays.copyOfRange(peer_name.getBytes(StandardCharsets.UTF_8),0,TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES);
                }
                else
                {
                    fn = peer_name.getBytes(StandardCharsets.UTF_8);
                }
            }
            catch(Exception e)
            {
            }
            data_buf.put(fn);
            for (int k=0;k<(TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES - fn.length);k++)
            {
                // fill with null bytes up to TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES for the peername
                data_buf.put((byte) 0x0);
            }
            //
            //
            //
            byte[] filename_bytes = "image.jpg".getBytes(StandardCharsets.UTF_8);
            try
            {
                if (m.file_name.getBytes(StandardCharsets.UTF_8).length > TOX_MAX_FILENAME_LENGTH)
                {
                    filename_bytes = Arrays.copyOfRange(m.file_name.getBytes(StandardCharsets.UTF_8),0,TOX_MAX_FILENAME_LENGTH);
                }
                else
                {
                    filename_bytes = m.file_name.getBytes(StandardCharsets.UTF_8);
                }
            }
            catch(Exception e)
            {
            }
            data_buf.put(filename_bytes);
            for (int k=0;k<(TOX_MAX_FILENAME_LENGTH - filename_bytes.length);k++)
            {
                // fill with null bytes up to TOX_MAX_FILENAME_LENGTH for the peername
                data_buf.put((byte) 0x0);
            }
            //
            //
            // -- now fill the file data --

            java.io.FileInputStream inputStream = new java.io.FileInputStream(f1);
            byte[] file_raw_data = new byte[(int)f1.length()];
            inputStream.read(file_raw_data);
            inputStream.close();

            data_buf.put(file_raw_data);
            //
            //
            //
            byte[] data = new byte[data_length];
            data_buf.rewind();
            data_buf.get(data);
            int result = tox_group_send_custom_private_packet(tox_group_by_groupid__wrapper(group_identifier),
                    get_group_peernum_from_peer_pubkey(group_identifier,
                            peer_pubkey), 1, data,
                    data_length);
            // Log.i(TAG, "send_ngch_syncfile: sending request:result=" + result);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "send_ngch_syncfile:EE:" + e.getMessage());
        }
    }

    private static void send_ngch_syncmsg(final String group_identifier, final String peer_pubkey, final GroupMessage m)
    {
        try
        {
            Random rand = new Random();
            int rndi = rand.nextInt(301);
            int n = 300 + rndi;
            // Log.i(TAG, "send_ngch_syncmsg: sleep for " + n + " ms");
            Thread.sleep(n);
            //
            final int header_length = 6 + 1 + 1 + 4 + 32 + 4 + 25;
            final int data_length = header_length + m.text.getBytes(StandardCharsets.UTF_8).length;

            if (data_length < (header_length + 1) || (data_length > 40000))
            {
                // Log.i(TAG, "send_ngch_syncmsg: some error in calculating data length [2]");
                return;
            }

            ByteBuffer data_buf = ByteBuffer.allocateDirect(data_length);

            data_buf.rewind();
            //
            data_buf.put((byte) 0x66);
            data_buf.put((byte) 0x77);
            data_buf.put((byte) 0x88);
            data_buf.put((byte) 0x11);
            data_buf.put((byte) 0x34);
            data_buf.put((byte) 0x35);
            //
            data_buf.put((byte) 0x1);
            //
            data_buf.put((byte) 0x2);
            // should be 4 bytes
            try
            {
                data_buf.put(hex_to_bytes(m.message_id_tox), 0,4);
            }
            catch (Exception e)
            {
                data_buf.put((byte) 0x0);
                data_buf.put((byte) 0x0);
                data_buf.put((byte) 0x0);
                data_buf.put((byte) 0x0);
            }
            // should be 32 bytes
            try
            {
                data_buf.put(hex_to_bytes(m.tox_group_peer_pubkey), 0, 32);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                for(int jj=0;jj<32;jj++)
                {
                    data_buf.put((byte) 0x0);
                }
            }
            //
            // unix timestamp
            long timestamp_tmp = (m.sent_timestamp / 1000);
            // Log.i(TAG,"send_ngch_syncmsg:outgoing_timestamp=" + timestamp_tmp);
            ByteBuffer temp_buffer = ByteBuffer.allocate(8);
            temp_buffer.putLong(timestamp_tmp).order(ByteOrder.BIG_ENDIAN);
            temp_buffer.position(4);
            data_buf.put(temp_buffer);
            //Log.i(TAG,"send_ngch_syncmsg:send_ts_bytes:" +
            //         HelperGeneric.bytesToHex(temp_buffer.array(), temp_buffer.arrayOffset(), temp_buffer.limit()));
            /*
            data_buf.put((byte)((timestamp_tmp >> 32) & 0xFF));
            data_buf.put((byte)((timestamp_tmp >> 16) & 0xFF));
            data_buf.put((byte)((timestamp_tmp >> 8) & 0xFF));
            data_buf.put((byte)(timestamp_tmp & 0xFF));
            */
            //
            byte[] fn = "peer".getBytes(StandardCharsets.UTF_8);
            try
            {
                final long groupnum = tox_group_by_groupid__wrapper(m.group_identifier);
                final long peernum = tox_group_peer_by_public_key(groupnum, m.tox_group_peer_pubkey);
                final String peer_name = tox_group_peer_get_name(groupnum, peernum);
                if (peer_name.getBytes(StandardCharsets.UTF_8).length > TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES)
                {
                    fn = Arrays.copyOfRange(peer_name.getBytes(StandardCharsets.UTF_8),0,TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES);
                }
                else
                {
                    fn = peer_name.getBytes(StandardCharsets.UTF_8);
                }
            }
            catch(Exception e)
            {
            }
            data_buf.put(fn);
            for (int k=0;k<(TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES - fn.length);k++)
            {
                // fill with null bytes up to TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES for the peername
                data_buf.put((byte) 0x0);
            }
            // -- now fill the message text --
            fn = m.text.getBytes(StandardCharsets.UTF_8);
            data_buf.put(fn);
            //
            //
            //
            byte[] data = new byte[data_length];
            data_buf.rewind();
            data_buf.get(data);
            //Log.i(TAG,"send_ngch_syncmsg:send_ts_bytes_to_network:" +
            //          HelperGeneric.bytesToHex(data, 6 + 1 + 1 + 4 + 32 , 4));
            int result = tox_group_send_custom_private_packet(tox_group_by_groupid__wrapper(group_identifier),
                    get_group_peernum_from_peer_pubkey(group_identifier,
                            peer_pubkey), 1, data,
                    data_length);
            // Log.i(TAG, "send_ngch_syncmsg: sending request:result=" + result);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "send_ngch_syncmsg:EE:" + e.getMessage());
        }
    }

    static void sync_group_message_history(final long group_number, final long peer_id)
    {
        final String peer_pubkey = tox_group_peer_get_public_key(group_number, peer_id);
        final String group_identifier = tox_group_by_groupnum__wrapper(group_number);

        try
        {
            long res = tox_group_self_get_peer_id(tox_group_by_groupid__wrapper(group_identifier));
            if (res == get_group_peernum_from_peer_pubkey(group_identifier, peer_pubkey))
            {
                // HINT: ignore self
                Log.i(TAG, "sync_group_message_history:dont send to self");
                return;
            }
        }
        catch(Exception ignored)
        {
        }

        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    // HINT: calculate x minutes into the past from now
                    final long sync_from_ts = System.currentTimeMillis() - (TOX_NGC_HISTORY_SYNC_MAX_SECONDS_BACK * 1000);

                    if (sync_from_ts < 1)
                    {
                        // fail safe
                        return;
                    }

                    // Log.i(TAG, "sync_group_message_history:sync_from_ts:" + sync_from_ts);

                    Iterator<GroupMessage> i1 =  TrifaToxService.Companion.getOrma().selectFromGroupMessage()
                            .group_identifierEq(group_identifier)
                            // .TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value)
                            .private_messageEq(0)
                            .tox_group_peer_pubkeyNotEq("-1")
                            .sent_timestampGt(sync_from_ts)
                            .orderByRcvd_timestampAsc()
                            .toList().iterator();

                    // Log.i(TAG, "sync_group_message_history:i1:" + i1);

                    while (i1.hasNext())
                    {
                        try
                        {
                            GroupMessage gm = i1.next();
                            if (!gm.tox_group_peer_pubkey.equalsIgnoreCase("-1"))
                            {
                                //Log.i(TAG, "sync_group_message_history:sync:sent_ts="
                                //           + gm.sent_timestamp + " syncts=" + sync_from_ts + " "
                                //           + gm.tox_group_peer_pubkey + " " +
                                //           gm.message_id_tox + " " + gm.msg_id_hash);
                                if (gm.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                                {
                                    send_ngch_syncfile(group_identifier, peer_pubkey, gm);
                                }
                                else
                                {
                                    send_ngch_syncmsg(group_identifier, peer_pubkey, gm);
                                }
                            }
                            else
                            {
                                // Log.i(TAG, "sync_group_message_history:sync:ignoring system message");
                            }
                        }
                        catch (Exception e2)
                        {
                            e2.printStackTrace();
                        }
                    }

                    // Log.i(TAG, "sync_group_message_history:END");
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    static void handle_incoming_sync_group_file(final long group_number, final long syncer_peer_id, final byte[] data, final long length)
    {
        try
        {
            long res = tox_group_self_get_peer_id(group_number);
            if (res == syncer_peer_id)
            {
                // HINT: do not add our own messages, they are already in the DB!
                // Log.i(TAG, "handle_incoming_sync_group_file:gn=" + group_number + " peerid=" + syncer_peer_id + " ignoring self");
                return;
            }

            final String group_identifier = tox_group_by_groupnum__wrapper(group_number);
            final String syncer_pubkey = tox_group_peer_get_public_key(group_number, syncer_peer_id);

            ByteBuffer send_pubkey_bytes = ByteBuffer.allocateDirect(TOX_GROUP_PEER_PUBLIC_KEY_SIZE);
            send_pubkey_bytes.put(data, 8 + 32, 32);
            ByteBufferCompat send_pubkey_bytes_compat = new ByteBufferCompat(send_pubkey_bytes);
            final String original_sender_peerpubkey = bytesToHexJava(send_pubkey_bytes_compat.array(),send_pubkey_bytes_compat.arrayOffset(),send_pubkey_bytes_compat.limit()).toUpperCase();
            Log.i(TAG, "handle_incoming_sync_group_file:peerpubkey hex=" + original_sender_peerpubkey);
            try
            {
                Log.i(TAG, "handle_incoming_sync_group_file:---===> syncer_name=" + tox_group_peer_get_name(group_number, syncer_peer_id));
            }
            catch(Exception e)
            {

            }
            Log.i(TAG, "handle_incoming_sync_group_file:syncer_pubkey=" + syncer_pubkey);

            if (tox_group_self_get_public_key(group_number).toUpperCase().equalsIgnoreCase(original_sender_peerpubkey))
            {
                // HINT: do not add our own files, they are already in the DB!
                // Log.i(TAG, "handle_incoming_sync_group_file:gn=" + group_number + " ignoring myself as original sender");
                return;
            }
            //
            //
            // HINT: putting 4 bytes unsigned int in big endian format into a java "long" is more complex than i thought
            ByteBuffer timestamp_byte_buffer = ByteBuffer.allocateDirect(8);
            timestamp_byte_buffer.put((byte)0x0);
            timestamp_byte_buffer.put((byte)0x0);
            timestamp_byte_buffer.put((byte)0x0);
            timestamp_byte_buffer.put((byte)0x0);
            timestamp_byte_buffer.put(data, 8+32+32, 4);
            timestamp_byte_buffer.order(ByteOrder.BIG_ENDIAN);
            timestamp_byte_buffer.rewind();
            long timestamp = timestamp_byte_buffer.getLong();
            timestamp_byte_buffer.rewind();
            Log.i(TAG, "handle_incoming_sync_group_file:timestamp=" + timestamp);

            if (timestamp > ((System.currentTimeMillis() / 1000) + (60 * 5)))
            {
                long delta_t = timestamp - (System.currentTimeMillis() / 1000);
                Log.i(TAG, "handle_incoming_sync_group_file:delta t=" + delta_t + " do NOT sync files from the future");
                return;
            }
            else if (timestamp < ((System.currentTimeMillis() / 1000) - (60 * 200)))
            {
                long delta_t = (System.currentTimeMillis() / 1000) - timestamp;
                Log.i(TAG, "handle_incoming_sync_group_file:delta t=" + (-delta_t) + " do NOT sync files that are too old");
                return;
            }

            //
            //
            //
            ByteBuffer hash_msg_id_bytes = ByteBuffer.allocateDirect(32);
            hash_msg_id_bytes.put(data, 8, 32);
            ByteBufferCompat hash_msg_id_bytes_compat = new ByteBufferCompat(hash_msg_id_bytes);
            final String message_id_hash = bytesToHexJava(hash_msg_id_bytes_compat.array(),hash_msg_id_bytes_compat.arrayOffset(),hash_msg_id_bytes_compat.limit()).toUpperCase();
            Log.i(TAG, "handle_incoming_sync_group_file:message_id_hash hex=" + message_id_hash);
            //
            //
            ByteBuffer name_buffer = ByteBuffer.allocateDirect(TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES);
            name_buffer.put(data, 8 + 32 + 32 + 4, TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES);
            name_buffer.rewind();
            String peer_name = "peer";
            try
            {
                byte[] name_byte_buf = new byte[TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES];
                Arrays.fill(name_byte_buf, (byte)0x0);
                name_buffer.rewind();
                name_buffer.get(name_byte_buf);

                int start_index = 0;
                int end_index = TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES - 1;
                for(int j=0;j<TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES;j++)
                {
                    if (name_byte_buf[j] == 0)
                    {
                        start_index = j+1;
                    }
                    else
                    {
                        break;
                    }
                }

                for(int j=(TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES-1);j>=0;j--)
                {
                    if (name_byte_buf[j] == 0)
                    {
                        end_index = j;
                    }
                    else
                    {
                        break;
                    }
                }

                byte[] peername_byte_buf_stripped = Arrays.copyOfRange(name_byte_buf, start_index,end_index);
                peer_name = new String(peername_byte_buf_stripped, StandardCharsets.UTF_8);
                Log.i(TAG,"handle_incoming_sync_group_file:peer_name str=" + peer_name);
                //
                //
                //
                // TODO: fixme, get real filename
                final String filename = "image.jpg";
                //
                //
                //
                final int header = 6+1+1+32+32+4+25+255;
                long filedata_size = length - header;
                if ((filedata_size < 1) || (filedata_size > 37000))
                {
                    Log.i(TAG, "handle_incoming_sync_group_file: file size less than 1 byte or larger than 37000 bytes");
                    return;
                }

                byte[] file_byte_buf = Arrays.copyOfRange(data, header, (int)length);

                long sender_peer_num = HelperGroup.get_group_peernum_from_peer_pubkey(group_identifier,
                        original_sender_peerpubkey);

                try
                {
                    if (group_identifier != null)
                    {
                        GroupMessage gm = TrifaToxService.Companion.getOrma().selectFromGroupMessage().
                                group_identifierEq(group_identifier.toLowerCase()).
                                tox_group_peer_pubkeyEq(original_sender_peerpubkey.toUpperCase()).
                                msg_id_hashEq(message_id_hash).get(0);

                        if (gm != null)
                        {
                            Log.i(TAG, "handle_incoming_sync_group_file:potential double file, message_id_hash:" + message_id_hash);
                            return;
                        }
                    }
                }
                catch(Exception e)
                {
                }

                Log.i(TAG, "handle_incoming_sync_group_file:group_file_add_from_sync, message_id_hash:" + message_id_hash);
                group_file_add_from_sync(group_identifier, syncer_pubkey, sender_peer_num, original_sender_peerpubkey,
                        file_byte_buf, filename, peer_name,
                        (timestamp * 1000), message_id_hash,
                        TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_NGC_PEERS.value);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                Log.i(TAG,"handle_incoming_sync_group_file:EE002:" + e.getMessage());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "handle_incoming_sync_group_file:EE001:" + e.getMessage());
        }
    }

    private static void group_file_add_from_sync(final String group_identifier, final String syncer_pubkey,
                                                 final long sender_peer_num,
                                                 final String original_sender_peerpubkey,
                                                 final byte[] file_byte_buf, final String filename,
                                                 final String peer_name,
                                                 final long timestamp, final String message_id_hash,
                                                 final int aTRIFA_SYNC_TYPE)
    {
        String group_id = group_identifier;
        try
        {
            long header_ngc_histsync_and_files = 6 + 1 + 1 + 32 + 32 + 4 + 25 + 255;
            long group_number = tox_group_by_groupid__wrapper(group_id);
            incoming_group_file_meta_data incoming_group_file_meta_data =
                    handle_incoming_group_file(
                            message_id_hash,
                            peer_name,
                            original_sender_peerpubkey,
                            true, timestamp, group_number,
                            sender_peer_num, file_byte_buf, file_byte_buf.length,
                            0);

            if (incoming_group_file_meta_data == null)
            {
                Log.i(TAG, "group_file_add_from_sync:EE02");
                return;
            }

            Companion.add_ngc_incoming_file_to_ui(incoming_group_file_meta_data,
                    timestamp, peer_name, original_sender_peerpubkey,
                    incoming_group_file_meta_data.msg_id_hash,
                    group_id,
                    true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void handle_incoming_sync_group_message(final long group_number, final long syncer_peer_id, final byte[] data, final long length)
    {
        try
        {
            // Log.i(TAG, "handle_incoming_sync_group_message: ________________________________________________________________");

            long res = tox_group_self_get_peer_id(group_number);
            if (res == syncer_peer_id)
            {
                // HINT: do not add our own messages, they are already in the DB!
                // Log.i(TAG, "handle_incoming_sync_group_message:gn=" + group_number + " peerid=" + syncer_peer_id + " ignoring self");
                return;
            }

            final String group_identifier = tox_group_by_groupnum__wrapper(group_number);
            final String syncer_pubkey = tox_group_peer_get_public_key(group_number, syncer_peer_id);

            ByteBuffer hash_bytes = ByteBuffer.allocateDirect(TOX_GROUP_PEER_PUBLIC_KEY_SIZE);
            hash_bytes.put(data, 6 + 1 + 1 + 4, 32);
            ByteBufferCompat hash_bytes_compat = new ByteBufferCompat(hash_bytes);
            final String original_sender_peerpubkey = bytesToHexJava
                    (hash_bytes_compat.array(),hash_bytes_compat.arrayOffset(),hash_bytes_compat.limit()).toUpperCase();
            // Log.i(TAG, "handle_incoming_sync_group_message:peerpubkey hex=" + original_sender_peerpubkey);

            if (tox_group_self_get_public_key(group_number).toUpperCase().equalsIgnoreCase(original_sender_peerpubkey))
            {
                // HINT: do not add our own messages, they are already in the DB!
                // Log.i(TAG, "handle_incoming_sync_group_message:gn=" + group_number + " peerid=" + peer_id + " ignoring myself as original sender");
                return;
            }

            try
            {
                // Log.i(TAG, "handle_incoming_sync_group_message:---===> syncer_name=" + tox_group_peer_get_name(group_number, syncer_peer_id));
            }
            catch(Exception e)
            {

            }
            // Log.i(TAG, "handle_incoming_sync_group_message:syncer_pubkey=" + syncer_pubkey);

            //
            //
            // HINT: putting 4 bytes unsigned int in big endian format into a java "long" is more complex than i thought
            ByteBuffer timestamp_byte_buffer = ByteBuffer.allocateDirect(8);
            timestamp_byte_buffer.put((byte)0x0);
            timestamp_byte_buffer.put((byte)0x0);
            timestamp_byte_buffer.put((byte)0x0);
            timestamp_byte_buffer.put((byte)0x0);
            timestamp_byte_buffer.put(data, 6 + 1 + 1 + 4 + 32, 4);
            timestamp_byte_buffer.order(java.nio.ByteOrder.BIG_ENDIAN);
            timestamp_byte_buffer.rewind();
            long timestamp = timestamp_byte_buffer.getLong();
            // Log.i(TAG,"handle_incoming_sync_group_message:got_ts_bytes:" +
            //           HelperGeneric.bytesToHex(data, 6 + 1 + 1 + 4 + 32, 4));
            timestamp_byte_buffer.rewind();
            //Log.i(TAG,"handle_incoming_sync_group_message:got_ts_bytes:bytebuffer:" +
            //          HelperGeneric.bytesToHex(timestamp_byte_buffer.array(),
            //                                   timestamp_byte_buffer.arrayOffset(),
            //                                  timestamp_byte_buffer.limit()));

            // Log.i(TAG, "handle_incoming_sync_group_message:timestamp=" + timestamp + " " + long_date_time_format(timestamp * 1000));

            if (timestamp > ((System.currentTimeMillis() / 1000) + (60 * 5)))
            {
                long delta_t = timestamp - (System.currentTimeMillis() / 1000);
                // Log.i(TAG, "handle_incoming_sync_group_message:delta t=" + delta_t + " do NOT sync messages from the future");
                return;
            }
            else if (timestamp < ((System.currentTimeMillis() / 1000) - (60 * 200)))
            {
                long delta_t = (System.currentTimeMillis() / 1000) - timestamp;
                // Log.i(TAG, "handle_incoming_sync_group_message:delta t=" + (-delta_t) + " do NOT sync messages that are too old");
                return;
            }

            //
            //
            //
            ByteBuffer hash_msg_id_bytes = ByteBuffer.allocateDirect(4);
            hash_msg_id_bytes.put(data, 6 + 1 + 1, 4);
            ByteBufferCompat hash_msg_id_bytes_compat = new ByteBufferCompat(hash_msg_id_bytes);
            final String message_id_tox = bytesToHexJava(hash_msg_id_bytes_compat.array(),hash_msg_id_bytes_compat.arrayOffset(),hash_msg_id_bytes_compat.limit()).toLowerCase();
            // Log.i(TAG, "handle_incoming_sync_group_message:message_id_tox hex=" + message_id_tox);
            //
            //
            ByteBuffer name_buffer = ByteBuffer.allocateDirect(TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES);
            name_buffer.put(data, 6 + 1 + 1 + 4 + 32 + 4, TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES);
            name_buffer.rewind();

            ByteBufferCompat name_buffer_compat = new ByteBufferCompat(name_buffer);
            final String name_str_with_padding_hex = bytesToHexJava(name_buffer_compat.array(),name_buffer_compat.arrayOffset(),name_buffer_compat.limit());
            // Log.i(TAG, "handle_incoming_sync_group_message:sender_name hex=" + name_str_with_padding_hex);

            String peer_name = "peer";
            try
            {
                byte[] name_byte_buf = new byte[TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES];
                Arrays.fill(name_byte_buf, (byte)0x0);
                name_buffer.rewind();
                name_buffer.get(name_byte_buf);

                int start_index = 0;
                int end_index = TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES - 1;
                for(int j=0;j<TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES;j++)
                {
                    if (name_byte_buf[j] == 0)
                    {
                        start_index = j+1;
                    }
                    else
                    {
                        break;
                    }
                }
                // Log.i(TAG, "handle_incoming_sync_group_message:start_index=" + start_index);

                for(int j=(TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES-1);j>=0;j--)
                {
                    if (name_byte_buf[j] == 0)
                    {
                        end_index = j;
                    }
                    else
                    {
                        break;
                    }
                }
                // Log.i(TAG, "handle_incoming_sync_group_message:end_index=" + end_index);

                if (end_index <= start_index)
                {
                    // Log.i(TAG, "handle_incoming_sync_group_message:error on null byte detection in name");
                }
                else
                {
                    byte[] peername_byte_buf_stripped = Arrays.copyOfRange(name_byte_buf, start_index,end_index);
                    peer_name = new String(peername_byte_buf_stripped, StandardCharsets.UTF_8);
                }
                //
                // Log.i(TAG,"handle_incoming_sync_group_message:peer_name str=" + peer_name);
                //
                final int header = 6 + 1 + 1 + 4 + 32 + 4 + 25; // 73 bytes
                long text_size = length - header;
                if ((text_size < 1) || (text_size > 37000))
                {
                    // Log.i(TAG, "handle_incoming_sync_group_message: text size less than 1 byte or larger than 37000 bytes");
                    return;
                }

                byte[] text_byte_buf = Arrays.copyOfRange(data, header, (int)length);
                String message_str = new String(text_byte_buf, StandardCharsets.UTF_8);
                // Log.i(TAG,"handle_incoming_sync_group_message:message str=" + message_str);

                long sender_peer_num = HelperGroup.get_group_peernum_from_peer_pubkey(group_identifier,
                        original_sender_peerpubkey);

                GroupMessage gm = get_last_group_message_in_this_group_within_n_seconds_from_sender_pubkey(
                        group_identifier, original_sender_peerpubkey, (timestamp * 1000),
                        message_id_tox, MESSAGE_GROUP_HISTORY_SYNC_DOUBLE_INTERVAL_SECS, message_str);

                if (gm != null)
                {
                    // Log.i(TAG,"handle_incoming_sync_group_message:potential double message:" + message_str);
                    return;
                }

                long peernum = tox_group_peer_by_public_key(group_number, original_sender_peerpubkey);
                final String peer_name_saved = tox_group_peer_get_name(group_number, peernum);
                if ((peer_name_saved != null) && (peer_name_saved.length() > 0))
                {
                    // HINT: use saved name instead of name from sync message
                    // Log.i(TAG,"handle_incoming_sync_group_message:use saved name instead of name from sync message:" + peer_name_saved);
                    peer_name = peer_name_saved;
                }

                group_message_add_from_sync(group_identifier, syncer_pubkey, sender_peer_num, original_sender_peerpubkey,
                        TRIFA_MSG_TYPE_TEXT.value, message_str, message_str.length(),
                        (timestamp * 1000), message_id_tox,
                        TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_NGC_PEERS.value,
                        peer_name);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                // Log.i(TAG,"handle_incoming_sync_group_message:EE002:" + e.getMessage());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            // Log.i(TAG, "handle_incoming_sync_group_message:EE001:" + e.getMessage());
        }
    }

    static void send_ngch_request(final String group_identifier, final String peer_pubkey)
    {
        try
        {
            long res = tox_group_self_get_peer_id(tox_group_by_groupid__wrapper(group_identifier));
            if (res == get_group_peernum_from_peer_pubkey(group_identifier, peer_pubkey))
            {
                // HINT: ignore own packets
                Log.i(TAG, "send_ngch_request:dont send to self");
                return;
            }
        }
        catch(Exception e)
        {
        }

        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    // HINT: sleep "5 + random(0 .. 6)" seconds
                    java.util.Random rand = new java.util.Random();
                    int rndi = rand.nextInt(7);
                    int n = 5 + rndi;
                    // Log.i(TAG,"send_ngch_request: sleep for " + n + " seconds");
                    Thread.sleep(1000 * n);
                    //
                    final int data_length = 6 + 1 + 1;
                    ByteBuffer data_buf = ByteBuffer.allocateDirect(data_length);

                    data_buf.rewind();
                    //
                    data_buf.put((byte) 0x66);
                    data_buf.put((byte) 0x77);
                    data_buf.put((byte) 0x88);
                    data_buf.put((byte) 0x11);
                    data_buf.put((byte) 0x34);
                    data_buf.put((byte) 0x35);
                    //
                    data_buf.put((byte) 0x1);
                    //
                    data_buf.put((byte) 0x1);

                    byte[] data = new byte[data_length];
                    data_buf.rewind();
                    data_buf.get(data);
                    int result = tox_group_send_custom_private_packet(
                            tox_group_by_groupid__wrapper(group_identifier),
                            get_group_peernum_from_peer_pubkey(group_identifier, peer_pubkey),
                            1,
                            data,
                            data_length);
                    // Log.i(TAG,"send_ngch_request: sending request:result=" + result);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    @Nullable
    static GroupMessage get_last_group_message_in_this_group_within_n_seconds_from_sender_pubkey(
            String group_identifier, String sender_pubkey, long sent_timestamp, String message_id_tox,
            long time_delta_ms, final String message_text)
    {
        try
        {
            if ((message_id_tox == null) || (message_id_tox.length() < 8))
            {
                return null;
            }

            GroupMessage gm = TrifaToxService.Companion.getOrma().selectFromGroupMessage().
                    group_identifierEq(group_identifier.toLowerCase()).
                    tox_group_peer_pubkeyEq(sender_pubkey.toUpperCase()).
                    message_id_toxEq(message_id_tox.toLowerCase()).
                    sent_timestampGt(sent_timestamp - (time_delta_ms * 1000)).
                    textEq(message_text).
                    limit(1).
                    get(0);

            return gm;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static void update_group_peername_in_all_missing_messages(final String group_identifier,
                                                                     final String peer_pubkey, final String peer_name)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateGroupMessage()
                    .group_identifierEq(group_identifier)
                    .tox_group_peer_pubkeyEq(peer_pubkey)
                    .tox_group_peernameEq("")
                    .tox_group_peername(peer_name)
                    .execute();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        try
        {
            TrifaToxService.Companion.getOrma().updateGroupMessage()
                    .group_identifierEq(group_identifier)
                    .tox_group_peer_pubkeyEq(peer_pubkey)
                    .tox_group_peernameIsNull()
                    .tox_group_peername(peer_name)
                    .execute();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public static String group_get_last_know_peername(final String group_identifier, final String peer_pubkey)
    {
        try
        {
            final String last_know_peername = TrifaToxService.Companion.getOrma()
                    .selectFromGroupMessage()
                    .group_identifierEq(group_identifier)
                    .tox_group_peer_pubkeyEq(peer_pubkey)
                    .tox_group_peernameNotEq("")
                    .orderBySent_timestampDesc()
                    .limit(1)
                    .get(0)
                    .tox_group_peername;
            // Log.i(TAG, "last_know_peername=" + last_know_peername);
            return last_know_peername;
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
        return null;
    }

    static void group_message_add_from_sync(final String group_identifier, final String syncer_pubkey,
                                            long peer_number2, String peer_pubkey, int a_TOX_MESSAGE_TYPE,
                                            String message, long length, long sent_timestamp_in_ms,
                                            String message_id, int sync_type, final String peer_name)
    {
        // HINT: if peername is null or empty, lets try to get the last know peername from this groups messages
        if ((peer_name == null) || (peer_name.length() < 1))
        {
             final String last_know_peername = group_get_last_know_peername(group_identifier, peer_pubkey);
        }

        // Log.i(TAG,
        //       "group_message_add_from_sync:cf_num=" + group_identifier + " pnum=" + peer_number2 + " msg=" + message);

        // Log.i(TAG, "group_message_add_from_sync:peername=" + peer_name);

        long group_num_ = tox_group_by_groupid__wrapper(group_identifier);
        int res = -1;
        if (peer_number2 == -1)
        {
            res = -1;
        }
        else
        {
            final long my_peer_num = tox_group_self_get_peer_id(group_num_);
            if (my_peer_num == peer_number2)
            {
                res = 1;
            }
            else
            {
                res = 0;
            }
        }

        if (res == 1)
        {
            // HINT: do not add our own messages, they are already in the DB!
            // Log.i(TAG, "conference_message_add_from_sync:own peer");
            return;
        }

        GroupMessage m = new GroupMessage();
        m.is_new = false;
        m.tox_group_peer_pubkey = peer_pubkey;
        m.direction = TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value;
        m.TOX_MESSAGE_TYPE = 0;
        m.read = false;
        m.tox_group_peername = peer_name;
        m.group_identifier = group_identifier;
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
        m.sent_timestamp = sent_timestamp_in_ms;
        m.rcvd_timestamp = System.currentTimeMillis();
        m.text = message;
        m.message_id_tox = message_id;
        m.was_synced = true;
        m.private_message = 0;

        m.tox_group_peer_role = -1;
        try
        {
            int peer_role = tox_group_peer_get_role(group_num_, peer_number2);
            if (peer_role >= 0)
            {
                m.tox_group_peer_role = peer_role;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (m.tox_group_peername == null)
        {
            try
            {
                String peer_name_try = tox_group_peer_get_name(group_num_, peer_number2);
                if ((peer_name_try != null) && (peer_name_try.length() > 0))
                {
                    m.tox_group_peername = peer_name_try;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        Companion.incoming_synced_group_text_msg(m);
    }

    public static byte[] YUV420rotate90(byte[] data, byte[] output, int imageWidth, int imageHeight)
    {
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++)
        {
            for (int y = imageHeight - 1; y >= 0; y--)
            {
                output[i++] = data[y * imageWidth + x];
            }
        }

        // Rotate the U and V color components
        int size = imageWidth * imageHeight;
        i = size;
        int j = size;
        int uv = size / 4;
        for (int x = 0; x < (imageWidth / 2); x++)
        {
            for (int y = (imageHeight / 2) - 1; y >= 0; y--)
            {
                try
                {
                    output[i] = data[j + (y * (imageWidth / 2) + x)];
                    output[i + uv] = data[j + uv + (y * (imageWidth / 2) + x)];
                }
                catch (Exception e)
                {
                }
                i++;
            }
        }
        return output;
    }

    public static byte[] YUV420rotateMinus90(byte[] data, byte[] output, int imageWidth, int imageHeight)
    {
        return rotateYUV420Degree180(YUV420rotate90(data, output, imageWidth, imageHeight), imageWidth, imageHeight);
    }

    private static byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight)
    {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;
        int count = 0;
        // y
        for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
            yuv[count] = data[i];
            count++;
        }
        //
        final int y_size = imageWidth * imageHeight;
        final int u_size = (imageWidth * imageHeight) / 4;
        // u
        for (i = u_size - 1; i >= 0; i--) {
            yuv[count] = data[y_size + i];
            count++;
        }
        // v
        for (i = u_size - 1; i >= 0; i--) {
            yuv[count] = data[y_size + u_size + i];
            count++;
        }
        return yuv;
    }

    static class group_list_peer
    {
        String peer_name;
        long peer_num;
        String peer_pubkey;
        int peer_connection_status;
        boolean self;
    }

    public static void dump_saved_offline_peers_to_log(final String group_identifier)
    {
        final long conference_num = tox_group_by_groupid__wrapper(group_identifier);

        long offline_num_peers = tox_group_offline_peer_count(conference_num);

        if (offline_num_peers > 0)
        {
            List<group_list_peer> group_peers_offline = new ArrayList<>();
            long i = 0;
            for (i = 0; i < GC_MAX_SAVED_PEERS; i++)
            {
                try
                {
                    String peer_pubkey_temp = tox_group_savedpeer_get_public_key(conference_num, i);
                    if (peer_pubkey_temp.compareToIgnoreCase("-1") == 0)
                    {
                        continue;
                    }
                    String peer_name = "zzzzzoffline " + i;
                    group_list_peer glp3 = new group_list_peer();
                    glp3.peer_pubkey = peer_pubkey_temp;
                    glp3.peer_num = i;
                    glp3.peer_name = peer_name;
                    glp3.peer_connection_status = ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE.value;
                    group_peers_offline.add(glp3);
                }
                catch (Exception ignored)
                {
                }
            }

            try
            {
                Collections.sort(group_peers_offline, new Comparator<group_list_peer>()
                {
                    @Override
                    public int compare(group_list_peer p1, group_list_peer p2)
                    {
                        String name1 = p1.peer_pubkey;
                        String name2 = p2.peer_pubkey;
                        return name1.compareToIgnoreCase(name2);
                    }
                });
            }
            catch (Exception ignored)
            {
            }

            StringBuilder logstr = new StringBuilder();
            for (group_list_peer peerloffline : group_peers_offline)
            {
                logstr.append(peerloffline.peer_pubkey).append(":").append(peerloffline.peer_num).append("\n");
            }
            Log.i(TAG, "\n\nNGC_GROUP_OFFLINE_PEERLIST:\n" + logstr + "\n\n");
        }
    }
}

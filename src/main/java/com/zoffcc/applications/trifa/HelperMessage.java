package com.zoffcc.applications.trifa;

import com.zoffcc.applications.sorm.Filetransfer;
import com.zoffcc.applications.sorm.FriendList;
import com.zoffcc.applications.sorm.Message;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static com.zoffcc.applications.trifa.HelperFiletransfer.get_incoming_filetransfer_local_filename;
import static com.zoffcc.applications.trifa.HelperFriend.*;
import static com.zoffcc.applications.trifa.HelperFriend.send_friend_msg_receipt_v2_wrapper;
import static com.zoffcc.applications.trifa.HelperGroup.bytebuffer_to_hexstring;
import static com.zoffcc.applications.trifa.HelperGroup.bytesToHexJava;
import static com.zoffcc.applications.trifa.MainActivity.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.*;
import static com.zoffcc.applications.trifa.ToxVars.*;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK;

public class HelperMessage {

    private static final String TAG = "trifa.Hlp.Message";

    static void send_msgv3_high_level_ack(final long friend_number, String msgV3hash_hex_string)
    {
        if (msgV3hash_hex_string.length() < TOX_HASH_LENGTH)
        {
            return;
        }
        ByteBuffer hash_bytes = HelperGeneric.hexstring_to_bytebuffer(msgV3hash_hex_string);

        if (hash_bytes == null) {
            return;
        } else {
            long t_sec = (System.currentTimeMillis() / 1000);
            long res = MainActivity.tox_messagev3_friend_send_message(friend_number,
                    TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK.value,
                    "_", hash_bytes, t_sec);
        }
    }

    static void process_msgv3_high_level_ack(final long friend_number, String msgV3hash_hex_string, long message_timestamp)
    {
        Message m = null;
        try
        {
            m = TrifaToxService.Companion.getOrma().selectFromMessage().
                    msg_idv3_hashEq(msgV3hash_hex_string).
                    tox_friendpubkeyEq(tox_friend_get_public_key(friend_number)).
                    directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).
                    readEq(false).
                    orderByIdDesc().
                    get(0);
        }
        catch (Exception e)
        {
            return;
        }

        if (m != null)
        {
            try
            {
                if (message_timestamp > 0)
                {
                    m.rcvd_timestamp = message_timestamp * 1000;
                }
                else
                {
                    m.rcvd_timestamp = System.currentTimeMillis();
                }
                m.read = true;
                update_message_in_db_read_rcvd_timestamp_rawmsgbytes(m);
                // update message in UI
                modify_text_message(m);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    static void update_message_in_db_read_rcvd_timestamp_rawmsgbytes(final Message m)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().
                    idEq(m.id).
                    read(m.read).
                    raw_msgv2_bytes(m.raw_msgv2_bytes).
                    rcvd_timestamp(m.rcvd_timestamp).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static long get_message_id_from_filetransfer_id_and_friendnum(long filetransfer_id, long friend_number)
    {
        try
        {
            List<Message> m = TrifaToxService.Companion.getOrma().selectFromMessage().
                    filetransfer_idEq(filetransfer_id).
                    tox_friendpubkeyEq(tox_friend_get_public_key(friend_number)).
                    orderByIdDesc().toList();

            if (m.size() == 0)
            {
                return -1;
            }

            return m.get(0).id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:EE:" + e.getMessage());
            return -1;
        }
    }

    static void update_message_in_db_filename_fullpath_from_id(long msg_id, String filename_fullpath)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().idEq(msg_id).filename_fullpath(filename_fullpath).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_filename_fullpath_friendnum_and_filenum(long friend_number, long file_number, String filename_fullpath)
    {
        try
        {
            long ft_id = TrifaToxService.Companion.getOrma().selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                    file_numberEq(file_number).orderByIdDesc().get(0).id;

            update_message_in_db_filename_fullpath_from_id(TrifaToxService.Companion.getOrma().selectFromMessage().
                    filetransfer_idEq(ft_id).
                    tox_friendpubkeyEq(tox_friend_get_public_key(friend_number)).
                    get(0).id, filename_fullpath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_message_state_from_id(long mid, int state)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().idEq(mid).state(state).execute();
            Log.i(TAG, "set_message_state_from_id:message_id=" + mid + " state=" + state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_state_from_friendnum_and_filenum(long friend_number, long file_number, int state)
    {
        try
        {
            long ft_id = TrifaToxService.Companion.getOrma().selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                    file_numberEq(file_number).orderByIdDesc().get(0).id;
            // Log.i(TAG,
            //       "set_message_state_from_friendnum_and_filenum:ft_id=" + ft_id + " friend_number=" + friend_number +
            //       " file_number=" + file_number);
            set_message_state_from_id(TrifaToxService.Companion.getOrma().selectFromMessage().
                    filetransfer_idEq(ft_id).
                    tox_friendpubkeyEq(tox_friend_get_public_key(friend_number)).
                    get(0).id, state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_friendnum_and_filenum:EE:" + e.getMessage());
        }
    }

    public static void set_message_filedb_from_id(long mid, long filedb_id)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().idEq(mid).filedb_id(filedb_id).execute();
            // Log.i(TAG, "set_message_filedb_from_id:message_id=" + message_id + " filedb_id=" + filedb_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_filedb_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_filedb_from_friendnum_and_filenum(long friend_number, long file_number, long filedb_id)
    {
        try
        {
            long ft_id = TrifaToxService.Companion.getOrma().selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                    file_numberEq(file_number).
                    orderByIdDesc().
                    get(0).id;
            // Log.i(TAG,
            //       "set_message_filedb_from_friendnum_and_filenum:ft_id=" + ft_id + " friend_number=" + friend_number +
            //       " file_number=" + file_number);
            set_message_filedb_from_id(TrifaToxService.Companion.getOrma().selectFromMessage().
                    filetransfer_idEq(ft_id).
                    tox_friendpubkeyEq(tox_friend_get_public_key(friend_number)).
                    orderByIdDesc().
                    get(0).id, filedb_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_filedb_from_friendnum_and_filenum:EE:" + e.getMessage());
        }
    }

    public static void update_single_message_from_messge_id(final long mid, final long file_size, final boolean force)
    {
        if (mid != -1)
        {
            try
            {
                Message m = TrifaToxService.Companion.getOrma().selectFromMessage().
                        idEq(mid).orderByIdDesc().get(0);

                if (m.id != -1)
                {
                    // if (force)
                    {
                        modify_message_with_finished_ft(m, file_size);
                    }
                }
            }
            catch (Exception e2)
            {
            }
        }
    }

    public static void update_single_message_from_ftid(final Filetransfer ft, final boolean force)
    {
        try
        {
            Message m = TrifaToxService.Companion.getOrma().selectFromMessage().
                    filetransfer_idEq(ft.id).
                    orderByIdDesc().get(0);

            if (m.id != -1)
            {
                // if (force)
                {
                    modify_message_with_ft(m, ft);
                }
            }
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }

    public static void set_message_queueing_from_id(long mid, boolean ft_outgoing_queued)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().idEq(mid).ft_outgoing_queued(ft_outgoing_queued).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_start_queueing_from_id:EE:" + e.getMessage());
        }
        try
        {
            Message msg = TrifaToxService.Companion.getOrma().selectFromMessage().idEq(mid).get(0);
            if (msg != null)
            {
                final Filetransfer ft = new Filetransfer();
                ft.filesize(0);
                if (ft_outgoing_queued == true) {
                    msg.state = ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME.value;
                }
                Log.i(TAG, "modify_message_with_ft: state="+msg.state);
                set_message_state_from_id(msg.id, msg.state);
                modify_message_with_ft(msg, ft);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_filetransfer_kind(final Message m)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().
                    idEq(m.id).
                    filetransfer_kind(m.filetransfer_kind).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_message_start_sending_from_id(long message_id)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().
                    idEq(message_id).ft_outgoing_started(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_start_sending_from_id:EE:" + e.getMessage());
        }
    }

    /**
     * Get an image off the system clipboard.
     *
     * @return Returns an Image if successful; otherwise returns null.
     */
    public static Image getImageFromClipboard()
    {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor))
        {
            try
            {
                // Log.i(TAG, "getImageFromClipboard:"+transferable.getTransferDataFlavors());
                // Log.i(TAG, "getImageFromClipboard:I="+(Image) transferable.getTransferData(DataFlavor.imageFlavor));
                return (Image) transferable.getTransferData(DataFlavor.imageFlavor);
            }
            catch (UnsupportedFlavorException e)
            {
                // handle this as desired
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // handle this as desired
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void take_screen_shot_with_selection(final String selected_friend_pubkey)
    {
        try
        {
            Log.i(TAG, "CaptureOccured...SelectionRectangle start");
            new SelectionRectangle();
            final Thread t = new Thread(() -> {
                try
                {
                    while (SelectionRectangle.showing)
                    {
                        Thread.sleep(20);
                    }

                    Thread.sleep(200);
                    Log.i(TAG, "CaptureOccured...SelectionRectangle done");

                    try
                    {
                        if (!SelectionRectangle.cancel)
                        {
                            Log.i(TAG, "CaptureOccured...Screenshot capture");
                            BufferedImage img = (BufferedImage) Screenshot.capture(SelectionRectangle.capture_x,
                                    SelectionRectangle.capture_y,
                                    SelectionRectangle.capture_width,
                                    SelectionRectangle.capture_height).getImage();

                            Log.i(TAG, "CaptureOccured...Screenshot capture DONE");

                            if (img != null)
                            {
                                Log.i(TAG, "CaptureOccured...Image");
                                try
                                {
                                    Log.i(TAG, "CaptureOccured...Image:003:" + selected_friend_pubkey);

                                    final String friend_pubkey_str = selected_friend_pubkey;

                                    String wanted_full_filename_path =
                                            TRIFAGlobals.VFS_FILE_DIR + "/" + friend_pubkey_str;
                                    new File(wanted_full_filename_path).mkdirs();

                                    String filename_local_corrected = get_incoming_filetransfer_local_filename(
                                            "clip.png", friend_pubkey_str);

                                    filename_local_corrected =
                                            wanted_full_filename_path + "/" + filename_local_corrected;

                                    Log.i(TAG, "CaptureOccured...Image:004:" + filename_local_corrected);
                                    final File f_send = new File(filename_local_corrected);
                                    boolean res = ImageIO.write(img, "png", f_send);
                                    Log.i(TAG,
                                            "CaptureOccured...Image:004:" + filename_local_corrected + " res=" +
                                                    res);

                                    // send file
                                    MainActivity.Companion.add_outgoing_file(f_send.getAbsoluteFile().getParent(),
                                            f_send.getAbsoluteFile().getName(), friend_pubkey_str);
                                }
                                catch (Exception e2)
                                {
                                    e2.printStackTrace();
                                    Log.i(TAG, "CaptureOccured...EE2:" + e2.getMessage());
                                }
                            }
                        }
                        else
                        {
                            Log.i(TAG, "CaptureOccured...SelectionRectangle CANCEL");
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
                catch (Exception e2)
                {
                }
            });
            t.start();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            Log.i(TAG, "CaptureOccured...EE1:" + e.getMessage());
        }
    }

    /*
     * returns: send_message_result class
     *
     *    send_message_result: NULL (if friend does not exist)
     *
     *    long msg_num: -98 (msgv3 error), -1 -2 -3 -4 -5 -6 (msgv3 error), -99 (msgv3 error),
     *                  "0..(UINT32_MAX-1)" (message sent OK)
     *
     *    long msg_num: -1 -2 -3 -4 -5 -6 -99 (msgv2 error),
     *                  MESSAGE_V2_MSG_SENT_OK (msgv2 sent OK), "0..(UINT32_MAX-1)" (message "old" sent OK)
     *
     *    boolean msg_v2: true if msgv2 was used, false otherwise
     *
     */
    public static MainActivity.Companion.send_message_result tox_friend_send_message_wrapper(final String friend_pubkey, int a_TOX_MESSAGE_TYPE, String message, long timestamp_unixtime_seconds)
    {
        Log.i(TAG, "tox_friend_send_message_wrapper:" + friend_pubkey);
        FriendList f = main_get_friend(friend_pubkey);
        if (f == null)
        {
            return null;
        }

        long friendnum_to_use = tox_friend_by_public_key(friend_pubkey);
        boolean need_call_push_url = false;

        final long fconnstatus = tox_friend_get_connection_status(friendnum_to_use);

        boolean msgv1 = true;
        if ((f.capabilities & TOX_CAPABILITY_MSGV2) != 0)
        {
            msgv1 = false;
        }
        Log.i(TAG, "tox_friend_send_message_wrapper:msgv1=" + msgv1);

        Log.i(TAG, "tox_friend_send_message_wrapper:f conn=" + fconnstatus);
        if (fconnstatus == TOX_CONNECTION_NONE.value)
        {
            String relay_pubkey = HelperRelay.get_relay_for_friend(friend_pubkey);
            if (relay_pubkey != null)
            {
                // friend has a relay
                friendnum_to_use = tox_friend_by_public_key(relay_pubkey);
                msgv1 = false;
                // Log.d(TAG, "tox_friend_send_message_wrapper:friendnum_to_use=" + friendnum_to_use);
            }
            else // if friend is NOT online and does not have a relay, try if he has a push url
            {
                need_call_push_url = true;
            }
        }

        if (msgv1)
        {
            // old msgV1 message (but always send msgV3 format)
            ByteBuffer hash_bytes = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            MainActivity.tox_messagev3_get_new_message_id(hash_bytes);
            MainActivity.Companion.send_message_result result = new MainActivity.Companion.send_message_result();

            // long t_sec = (System.currentTimeMillis() / 1000);
            long res = MainActivity.tox_messagev3_friend_send_message(friendnum_to_use, a_TOX_MESSAGE_TYPE, message,
                    hash_bytes, timestamp_unixtime_seconds);
            Log.i(TAG, "tox_friend_send_message_wrapper:msg=" + message + " " + timestamp_unixtime_seconds);

            result.msg_num = res;
            result.msg_v2 = false;
            result.msg_hash_hex = "";
            result.msg_hash_v3_hex = bytebuffer_to_hexstring(hash_bytes, true);
            Log.i(TAG, "tox_friend_send_message_wrapper:msg_hash_v3_hex=" + result.msg_hash_v3_hex);
            result.raw_message_buf_hex = "";

            if (need_call_push_url)
            {
                friend_call_push_url(friend_pubkey, System.currentTimeMillis());
            }

            return result;
        }
        else // use msgV2
        {
            MainActivity.Companion.send_message_result result = new MainActivity.Companion.send_message_result();
            ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) TOX_MAX_FILETRANSFER_SIZE_MSGV2);
            ByteBuffer raw_message_length_buf = ByteBuffer.allocateDirect((int) 2); // 2 bytes for length
            ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            // use msg V2 API Call
            // long t_sec = (System.currentTimeMillis() / 1000);
            long res = MainActivity.tox_util_friend_send_message_v2(friendnum_to_use, a_TOX_MESSAGE_TYPE,
                    timestamp_unixtime_seconds, message,
                    message.length(), raw_message_buf,
                    raw_message_length_buf, msg_id_buffer);

            ByteBufferCompat raw_message_length_buf_compat = new ByteBufferCompat(raw_message_length_buf);
            final int len_low_byte = raw_message_length_buf_compat.
                    array()[raw_message_length_buf_compat.arrayOffset()] & 0xFF;
            final int len_high_byte = (raw_message_length_buf_compat.
                    array()[raw_message_length_buf_compat.arrayOffset() + 1] & 0xFF) * 256;
            final int raw_message_length_int = len_low_byte + len_high_byte;

            result.error_num = res;

            if (res == -9999)
            {
                // msg V2 OK
                result.msg_num = MESSAGE_V2_MSG_SENT_OK;
                result.msg_v2 = true;

                ByteBufferCompat msg_id_buffer_compat = new ByteBufferCompat(msg_id_buffer);
                result.msg_hash_hex = bytesToHexJava(msg_id_buffer_compat.array(), msg_id_buffer_compat.arrayOffset(),
                        msg_id_buffer_compat.limit());
                ByteBufferCompat raw_message_buf_compat = new ByteBufferCompat(raw_message_buf);
                result.raw_message_buf_hex = bytesToHexJava(raw_message_buf_compat.array(),
                        raw_message_buf_compat.arrayOffset(), raw_message_length_int);
                if (need_call_push_url)
                {
                    friend_call_push_url(friend_pubkey, System.currentTimeMillis());
                }
                return result;
            }
            else
            {
                if (res == -9991)
                {
                    result.msg_num = -1;
                }
                else
                {
                    result.msg_num = res;
                }

                result.msg_v2 = true;
                result.msg_hash_hex = "";
                result.raw_message_buf_hex = "";
                if (need_call_push_url)
                {
                    friend_call_push_url(friend_pubkey, System.currentTimeMillis());
                }
                return result;
            }
        }
    }

    static boolean get_message_in_db_sent_push_is_read(final String friend_pubkey, final long sent_timestamp)
    {
        boolean ret = false;
        try
        {
            Message m = TrifaToxService.Companion.getOrma().selectFromMessage().
                    tox_friendpubkeyEq(friend_pubkey).
                    sent_timestampBetween(sent_timestamp - PUSH_URL_TRIGGER_GET_MESSAGE_FOR_delta_ms_prev,
                    sent_timestamp + PUSH_URL_TRIGGER_GET_MESSAGE_FOR_delta_ms_after).
                    directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).
                    orderBySent_timestampAsc().
                    limit(1).get(0);

            return m.read;
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        return ret;
    }

    static void update_message_in_db_sent_push_set(final String friend_pubkey, final long sent_timestamp)
    {
        try
        {
            Message m = TrifaToxService.Companion.getOrma().selectFromMessage().
                    tox_friendpubkeyEq(friend_pubkey).
                    sent_timestampBetween(sent_timestamp - PUSH_URL_TRIGGER_GET_MESSAGE_FOR_delta_ms_prev,
                    sent_timestamp + PUSH_URL_TRIGGER_GET_MESSAGE_FOR_delta_ms_after).
                    directionEq(TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).
                    sent_pushEq(0).
                    orderBySent_timestampAsc().
                    limit(1).get(0);

            // Log.i(TAG, "update_message_in_db_sent_push_set:ts=" + sent_timestamp + " m=" + m);

            if (m.sent_push != 1) {
                TrifaToxService.Companion.getOrma().updateMessage().tox_friendpubkeyEq(friend_pubkey).idEq(m.id).sent_push(1).execute();
                m.sent_push = 1;
                // update message in UI
                modify_text_message(m);
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    static void update_message_in_db_resend_count(final Message m)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().
                    idEq(m.id).
                    resend_count(m.resend_count).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_messageid(final Message m)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().
                    idEq(m.id).
                    message_id(m.message_id).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized static void update_message_in_db_no_read_recvedts(final Message m)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().
                    idEq(m.id).
                    text(m.text).
                    sent_timestamp(m.sent_timestamp).
                    msg_version(m.msg_version).
                    filename_fullpath(m.filename_fullpath).
                    raw_msgv2_bytes(m.raw_msgv2_bytes).
                    msg_id_hash(m.msg_id_hash).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void sync_messagev2_answer(ByteBuffer raw_message_buf_wrapped, long friend_number, ByteBuffer msg_id_buffer, String real_sender_as_hex_string, String msg_id_as_hex_string_wrapped)
    {
        // we got an "msg receipt" from the relay
        // Log.i(TAG, "friend_sync_message_v2_cb:TOX_FILE_KIND_MESSAGEV2_ANSWER");
        final String message_id_hash_as_hex_string = msg_id_as_hex_string_wrapped;

        try
        {
            // Log.i(TAG, "friend_sync_message_v2_cb:message_id_hash_as_hex_string=" + message_id_hash_as_hex_string +
            //            " friendpubkey=" + real_sender_as_hex_string);

            final List<Message> mlist = TrifaToxService.Companion.getOrma().selectFromMessage().
                    msg_id_hashEq(message_id_hash_as_hex_string).
                    tox_friendpubkeyEq(real_sender_as_hex_string).
                    directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).
                    readEq(false).
                    toList();

            if (mlist.size() > 0)
            {
                final Message m = mlist.get(0);
                try
                {
                    long msg_wrapped_sec = MainActivity.tox_messagev2_get_ts_sec(raw_message_buf_wrapped);
                    long msg_wrapped_ms = MainActivity.tox_messagev2_get_ts_ms(raw_message_buf_wrapped);
                    m.raw_msgv2_bytes = "";
                    m.rcvd_timestamp = (msg_wrapped_sec * 1000) + msg_wrapped_ms;
                    m.read = true;
                    update_message_in_db_read_rcvd_timestamp_rawmsgbytes(m);
                    m.resend_count = 2;
                    update_message_in_db_resend_count(m);
                    // update message in UI
                    modify_text_message(m);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                send_friend_msg_receipt_v2_wrapper(friend_number, 4, msg_id_buffer,
                        (System.currentTimeMillis() / 1000));
            }
        }
        catch (Exception e)
        {
        }

        send_friend_msg_receipt_v2_wrapper(friend_number, 4, msg_id_buffer, (System.currentTimeMillis() / 1000));
    }
}

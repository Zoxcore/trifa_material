package com.zoffcc.applications.trifa;

import com.zoffcc.applications.sorm.Filetransfer;
import com.zoffcc.applications.sorm.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import java.util.Random;

import static com.zoffcc.applications.sorm.OrmaDatabase.sqldb;
import static com.zoffcc.applications.trifa.MainActivity.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.*;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_FTV2;

public class HelperFiletransfer {
    private static final String TAG = "trifa.Hlp.Filetransfer";

    public static String filter_out_specials_from_filepath(String path)
    {
        try
        {
            // TODO: be less strict here, but really actually test it then!
            return path.replaceAll("[^a-zA-Z0-9_.]", "_");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return path;
        }
    }

    public static String filter_out_specials(String in)
    {
        try
        {
            return in.replaceAll("[^a-zA-Z0-9]", "");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return in;
        }
    }

    public static String bytesToString(byte[] bytes)
    {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] StringToBytes2(String in)
    {
        try
        {
            return in.getBytes(StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            // TODO: fix me!!!!!
            // TODO: fix me!!!!!
            // TODO: fix me!!!!!
            e.printStackTrace();
            return null;
            // TODO: fix me!!!!!
            // TODO: fix me!!!!!
            // TODO: fix me!!!!!
        }
    }

    public static byte[] sha256(byte[] input)
    {
        try
        {
            return MessageDigest.getInstance("SHA-256").digest(input);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    static void move_tmp_file_to_real_file(String src_path_name, String src_file_name, String dst_path_name, String dst_file_name)
    {
        // Log.i(TAG, "move_tmp_file_to_real_file:" + src_path_name + "/" + src_file_name + " -> " + dst_path_name + "/" +
        //           dst_file_name);
        try
        {
            File f1 = new File(src_path_name + "/" + src_file_name);
            File f2 = new File(dst_path_name + "/" + dst_file_name);
            File dst_dir = new File(dst_path_name + "/");
            dst_dir.mkdirs();
            f1.renameTo(f2);

            // Log.i(TAG, "move_tmp_file_to_real_file:OK");
        }
        catch (Exception e)
        {
            // Log.i(TAG, "move_tmp_file_to_real_file:EE:" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String get_incoming_filetransfer_local_filename(String incoming_filename, String friend_pubkey_str)
    {
        String result = filter_out_specials_from_filepath(incoming_filename);
        String wanted_full_filename_path = VFS_FILE_DIR + "/" + friend_pubkey_str;

        // Log.i(TAG, "check_auto_accept_incoming_filetransfer:start=" + incoming_filename + " " + result + " " +
        //           wanted_full_filename_path);

        File f1 = new File(wanted_full_filename_path + "/" + result);

        if (f1.exists())
        {
            Random random = new Random();
            long new_random_log = (long) random.nextInt() + (1L << 31);

            // Log.i(TAG, "check_auto_accept_incoming_filetransfer:new_random_log=" + new_random_log);

            String random_filename_addon = filter_out_specials(
                    bytesToString(sha256(
                            StringToBytes2("" + new_random_log))));
            // Log.i(TAG, "check_auto_accept_incoming_filetransfer:random_filename_addon=" + random_filename_addon);

            String extension = "";

            try
            {
                extension = result.substring(result.lastIndexOf("."));

                if (extension.equalsIgnoreCase("."))
                {
                    extension = "";
                }
            }
            catch (Exception e)
            {
                extension = "";
            }

            result = result + "_" + random_filename_addon + extension;

            // Log.i(TAG, "check_auto_accept_incoming_filetransfer:result=" + result);
        }

        return result;
    }

    static void save_group_incoming_file(String file_path_name, String file_name, byte[] data, long buffer_offset, long write_bytes)
    {
        try
        {
            if (buffer_offset >= 2100000L)
            {
                return;
            }
            if (write_bytes >= 2100000L)
            {
                return;
            }

            File outfile = new File(file_path_name + "/" + file_name);
            FileOutputStream outputStream = new FileOutputStream(outfile);
            outputStream.write(data, (int)buffer_offset, (int)write_bytes);
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "save_group_incoming_file:EE");
        }
    }

    public static void set_filetransfer_for_message_from_filetransfer_id(long filetransfer_id, long ft_id)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().filetransfer_idEq(filetransfer_id).filetransfer_id(ft_id).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void delete_filetransfers_from_friendnum_and_filenum(long friend_number, long file_number)
    {
        try
        {
            long del_ft_id = TrifaToxService.Companion.getOrma().selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                    file_numberEq(file_number).
                    orderByIdDesc().toList().
                    get(0).id;
            // Log.i(TAG, "delete_ft:id=" + del_ft_id);
            delete_filetransfers_from_id(del_ft_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void delete_filetransfers_from_id(long filetransfer_id)
    {
        try
        {
            // Log.i(TAG, "delete_ft:id=" + filetransfer_id);
            TrifaToxService.Companion.getOrma().deleteFromFiletransfer().idEq(filetransfer_id).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_filetransfer_for_message_from_friendnum_and_filenum(long friend_number, long file_number, long ft_id)
    {
        try
        {
            set_filetransfer_for_message_from_filetransfer_id(TrifaToxService.Companion.getOrma().selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                    file_numberEq(file_number).
                    orderByIdDesc().toList().
                    get(0).id, ft_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String get_filetransfer_filename_from_id(long filetransfer_id)
    {
        try
        {
            if (TrifaToxService.Companion.getOrma().selectFromFiletransfer().idEq(filetransfer_id).count() == 1)
            {
                return TrifaToxService.Companion.getOrma().selectFromFiletransfer().idEq(filetransfer_id).toList().get(0).file_name;
            }
            else
            {
                return "";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static long get_filetransfer_filesize_from_id(long filetransfer_id)
    {
        try
        {
            if (TrifaToxService.Companion.getOrma().selectFromFiletransfer().idEq(filetransfer_id).count() == 1)
            {
                return TrifaToxService.Companion.getOrma().selectFromFiletransfer().idEq(filetransfer_id).toList().get(0).filesize;
            }
            else
            {
                return -1;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public static long get_filetransfer_state_from_id(long filetransfer_id)
    {
        try
        {
            if (TrifaToxService.Companion.getOrma().selectFromFiletransfer().idEq(filetransfer_id).count() == 1)
            {
                return TrifaToxService.Companion.getOrma().selectFromFiletransfer().idEq(filetransfer_id).toList().get(0).state;
            }
            else
            {
                return TOX_FILE_CONTROL_CANCEL.value;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return TOX_FILE_CONTROL_CANCEL.value;
        }
    }

    public static void set_filetransfer_accepted_from_id(long filetransfer_id)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateFiletransfer().idEq(filetransfer_id).ft_accepted(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_filetransfer_state_from_id(long filetransfer_id, int state)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateFiletransfer().idEq(filetransfer_id).state(state).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_message_accepted_from_id(long message_id)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().idEq(message_id).ft_accepted(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_message_state_from_id(long message_id, int state)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().idEq(message_id).state(state).execute();
            // Log.i(TAG, "set_message_state_from_id:message_id=" + message_id + " state=" + state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_id:EE:" + e.getMessage());
        }
    }

    public static long get_filetransfer_filenum_from_id(long filetransfer_id)
    {
        try
        {
            if (TrifaToxService.Companion.getOrma().selectFromFiletransfer().idEq(filetransfer_id).count() == 1)
            {
                return TrifaToxService.Companion.getOrma().selectFromFiletransfer().idEq(filetransfer_id).toList().get(0).file_number;
            }
            else
            {
                return -1;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    static long insert_into_filetransfer_db(final Filetransfer f)
    {
        long row_id = -1;
        try
        {
            row_id = TrifaToxService.Companion.getOrma().insertIntoFiletransfer(f);
            // Log.i(TAG, "insert_into_filetransfer_db:row_id=" + row_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // Log.i(TAG, "insert_into_filetransfer_db:EE:" + e.getMessage());
            return -1;
        }

        try
        {
            long ft_id = -1;
            try
            {
                Statement statement = sqldb.createStatement();
                ResultSet rs = statement.executeQuery("SELECT id FROM Filetransfer where rowid='" + row_id + "'");
                if (rs.next())
                {
                    ft_id = rs.getLong("id");
                }

                try
                {
                    statement.close();
                }
                catch (Exception ignored)
                {
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return ft_id;
        }
        catch (Exception e)
        {
            // Log.i(TAG, "insert_into_filetransfer_db:EE:" + e.getMessage());
            // e.printStackTrace();
            return -1;
        }
    }

    public static boolean check_filename_is_image(String filename_full_path)
    {
        try {
            String mimeType = URLConnection.guessContentTypeFromName(filename_full_path);
            if (mimeType.startsWith("image")) {
                return true;
            }
        }
        catch(Exception e)
        {
        }
        return false;
    }

    public static boolean check_auto_accept_incoming_filetransfer(Message message)
    {
        try
        {
            String mimeType = URLConnection.guessContentTypeFromName(
                    get_filetransfer_filename_from_id(message.filetransfer_id).toLowerCase());
            // Log.i(TAG, "check_auto_accept_incoming_filetransfer:mime-type=" + mimeType);

            if (mimeType != null)
            {
                if (PREF__auto_accept_image)
                {
                    if (get_filetransfer_filesize_from_id(message.filetransfer_id) <=
                            6 * 1014 * 1024) // if file size is smaller than 6 MByte accept FT
                    {
                        if (mimeType.startsWith("image"))
                        {
                            if (get_filetransfer_state_from_id(message.filetransfer_id) == TOX_FILE_CONTROL_PAUSE.value)
                            {
                                // accept FT
                                set_filetransfer_accepted_from_id(message.filetransfer_id);
                                set_filetransfer_state_from_id(message.filetransfer_id, TOX_FILE_CONTROL_RESUME.value);
                                set_message_accepted_from_id(message.id);
                                set_message_state_from_id(message.id, TOX_FILE_CONTROL_RESUME.value);
                                tox_file_control(tox_friend_by_public_key(message.tox_friendpubkey),
                                        get_filetransfer_filenum_from_id(message.filetransfer_id),
                                        TOX_FILE_CONTROL_RESUME.value);

                                // update message view
                                //***//update_single_message_from_messge_id(message.id, true);
                                // Log.i(TAG, "check_auto_accept_incoming_filetransfer:image:accepted");
                                return true;
                            }
                        }
                    }
                }

                if (PREF__auto_accept_video)
                {
                    if (get_filetransfer_filesize_from_id(message.filetransfer_id) <=
                            20 * 1014 * 1024) // if file size is smaller than 20 MByte accept FT
                    {
                        if (mimeType.startsWith("video"))
                        {
                            if (get_filetransfer_state_from_id(message.filetransfer_id) == TOX_FILE_CONTROL_PAUSE.value)
                            {
                                // accept FT
                                set_filetransfer_accepted_from_id(message.filetransfer_id);
                                set_filetransfer_state_from_id(message.filetransfer_id, TOX_FILE_CONTROL_RESUME.value);
                                set_message_accepted_from_id(message.id);
                                set_message_state_from_id(message.id, TOX_FILE_CONTROL_RESUME.value);
                                tox_file_control(tox_friend_by_public_key(message.tox_friendpubkey),
                                        get_filetransfer_filenum_from_id(message.filetransfer_id),
                                        TOX_FILE_CONTROL_RESUME.value);

                                // update message view
                                //***//update_single_message_from_messge_id(message.id, true);
                                // Log.i(TAG, "check_auto_accept_incoming_filetransfer:video:accepted");
                                return true;
                            }
                        }
                    }
                }
            }

            if (PREF__auto_accept_all_upto)
            {
                // haXX0r ------------------------
                // haXX0r ------------------------
                // ** accept any size FT for now !! **
                // haXX0r ------------------------
                // haXX0r ------------------------
                //if (get_filetransfer_filesize_from_id(message.filetransfer_id) <=
                //        200 * 1014 * 1024) // if file size is smaller than 200 MByte accept FT
                {
                    if (get_filetransfer_state_from_id(message.filetransfer_id) == TOX_FILE_CONTROL_PAUSE.value)
                    {
                        // accept FT
                        set_filetransfer_accepted_from_id(message.filetransfer_id);
                        set_filetransfer_state_from_id(message.filetransfer_id, TOX_FILE_CONTROL_RESUME.value);
                        set_message_accepted_from_id(message.id);
                        set_message_state_from_id(message.id, TOX_FILE_CONTROL_RESUME.value);
                        tox_file_control(tox_friend_by_public_key(message.tox_friendpubkey),
                                get_filetransfer_filenum_from_id(message.filetransfer_id),
                                TOX_FILE_CONTROL_RESUME.value);

                        // update message view
                        //***//update_single_message_from_messge_id(message.id, true);
                        // Log.i(TAG, "check_auto_accept_incoming_filetransfer:video:accepted");
                        return true;
                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    static void update_filetransfer_db_full(final Filetransfer f)
    {
        TrifaToxService.Companion.getOrma().updateFiletransfer().
                idEq(f.id).
                tox_public_key_string(f.tox_public_key_string).
                direction(f.direction).
                file_number(f.file_number).
                kind(f.kind).
                state(f.state).
                path_name(f.path_name).
                message_id(f.message_id).
                file_name(f.file_name).
                fos_open(f.fos_open).
                filesize(f.filesize).
                current_position(f.current_position).
                tox_file_id_hex(f.tox_file_id_hex).
                execute();
    }

    static void update_filetransfer_db_current_position(final Filetransfer f)
    {
        TrifaToxService.Companion.getOrma().updateFiletransfer().
                tox_public_key_stringEq(f.tox_public_key_string).
                file_numberEq(f.file_number).
                stateNotEq(TOX_FILE_CONTROL_CANCEL.value).
                current_position(f.current_position).
                execute();
    }

    static void cancel_filetransfer(long friend_number, long file_number)
    {
        // Log.i(TAG, "FTFTFT:cancel_filetransfer");
        Filetransfer f = null;

        try
        {
            f = TrifaToxService.Companion.getOrma().selectFromFiletransfer().
                    file_numberEq(file_number).
                    tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                    orderByIdDesc().
                    toList().get(0);

            if (f.direction == TRIFA_FT_DIRECTION_INCOMING.value)
            {
                if ((f.kind == TOX_FILE_KIND_DATA.value) || (f.kind == TOX_FILE_KIND_FTV2.value))
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                    // delete tmp file
                    delete_filetransfer_tmpfile(friend_number, file_number);
                    // set state for FT in message
                    HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    // remove link to any message
                    set_filetransfer_for_message_from_friendnum_and_filenum(friend_number, file_number, -1);
                    // delete FT in DB
                    // Log.i(TAG, "FTFTFT:002");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);

                    // update UI
                    // TODO: updates all messages, this is bad
                    // update_all_messages_global(false);
                    try
                    {
                        if (f.id != -1)
                        {
                            //**//HelperMessage.update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
                else // avatar FT
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                    set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_CANCEL.value);
                    HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    // delete tmp file
                    delete_filetransfer_tmpfile(friend_number, file_number);
                    // delete FT in DB
                    // Log.i(TAG, "FTFTFT:003");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                }
            }
            else // outgoing FT
            {
                if ((f.kind == TOX_FILE_KIND_DATA.value) || (f.kind == TOX_FILE_KIND_FTV2.value))
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                    // set state for FT in message
                    HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    // remove link to any message
                    set_filetransfer_for_message_from_friendnum_and_filenum(friend_number, file_number, -1);
                    // delete FT in DB
                    // Log.i(TAG, "FTFTFT:OGFT:002");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);

                    // update UI
                    try
                    {
                        if (f.id != -1)
                        {
                            //**//HelperMessage.update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
                else // avatar FT
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                    set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_CANCEL.value);

                    if (msg_id > -1)
                    {
                        HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    }

                    // delete tmp file
                    delete_filetransfer_tmpfile(friend_number, file_number);
                    // delete FT in DB
                    // Log.i(TAG, "FTFTFT:OGFT:003");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static long get_filetransfer_id_from_friendnum_and_filenum(long friend_number, long file_number)
    {
        try
        {
            long ft_id = TrifaToxService.Companion.getOrma().selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                    file_numberEq(file_number).
                    orderByIdDesc().
                    toList().
                    get(0).id;
            return ft_id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:EE:" + e.getMessage());
            return -1;
        }
    }

    public static void delete_filetransfer_tmpfile(long filetransfer_id)
    {
        try
        {
            Filetransfer ft = TrifaToxService.Companion.getOrma().selectFromFiletransfer().idEq(filetransfer_id).toList().get(0);
            Log.i(TAG, "delete_filetransfer_tmpfile:path=" + VFS_TMP_FILE_DIR + "/" + ft.tox_public_key_string + "/" + ft.file_name);
            File f1 = new File(VFS_TMP_FILE_DIR + "/" + ft.tox_public_key_string + "/" + ft.file_name);
            f1.delete();
            Log.i(TAG, "delete_filetransfer_tmpfile:OK");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void delete_filetransfer_tmpfile(long friend_number, long file_number)
    {
        try
        {
            delete_filetransfer_tmpfile(TrifaToxService.Companion.getOrma().selectFromFiletransfer().tox_public_key_stringEq(
                    tox_friend_get_public_key(friend_number)).file_numberEq(
                    file_number).toList().get(0).id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

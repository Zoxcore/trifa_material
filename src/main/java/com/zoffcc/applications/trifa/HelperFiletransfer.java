package com.zoffcc.applications.trifa;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;

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
}

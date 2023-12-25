package com.zoffcc.applications.trifa;

import com.zoffcc.applications.sorm.FriendList;
import com.zoffcc.applications.sorm.TRIFADatabaseGlobalsNew;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;

import static com.zoffcc.applications.trifa.HelperMessage.get_message_in_db_sent_push_is_read;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_sent_push_set;
import static com.zoffcc.applications.trifa.HelperRelay.get_pushurl_for_friend;
import static com.zoffcc.applications.trifa.HelperRelay.is_valid_pushurl_for_friend_with_whitelist;
import static com.zoffcc.applications.trifa.MainActivity.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.*;
import static java.time.temporal.ChronoUnit.SECONDS;

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
            MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);
        } else if (msg_type == 3) {
            // send message receipt v2
            MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);
        } else if (msg_type == 4) {
            // send message receipt v2 for unknown message
            MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);
        }
    }

    static void update_friend_msgv3_capability(long friend_number, int new_value)
    {
        try
        {
            if ((new_value == 0) || (new_value == 1))
            {
                FriendList f = TrifaToxService.Companion.getOrma().selectFromFriendList().
                        tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                        get(0);
                if (f != null)
                {
                    if (f.msgv3_capability != new_value)
                    {
                        Log.i(TAG,
                                "update_friend_msgv3_capability f=" +
                                        get_friend_name_from_num(friend_number) + " new=" +
                                        new_value + " old=" + f.msgv3_capability);
                        TrifaToxService.Companion.getOrma().updateFriendList().
                                tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                                msgv3_capability(new_value).
                                execute();
                    }
                }
            }
        }
        catch (Exception e)
        {
        }
    }

    static String get_friend_name_from_num(long friendnum)
    {
        String result = "Unknown";

        try
        {
            if (TrifaToxService.Companion.getOrma() != null)
            {
                try
                {
                    String result_alias = TrifaToxService.Companion.getOrma().selectFromFriendList().
                            tox_public_key_stringEq(tox_friend_get_public_key(friendnum)).
                            toList().get(0).alias_name;

                    if (result_alias != null)
                    {
                        if (result_alias.length() > 0)
                        {
                            result = result_alias;
                            return result;
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                result = TrifaToxService.Companion.getOrma().selectFromFriendList().
                        tox_public_key_stringEq(tox_friend_get_public_key(friendnum)).
                        toList().get(0).name;
            }
        }
        catch (Exception e)
        {
            result = "Unknown";
            e.printStackTrace();
        }

        return result;
    }

    static String get_friend_name_from_pubkey(String friend_pubkey)
    {
        String ret = "Unknown";
        String friend_alias_name = "";
        String friend_name = "";

        try
        {
            friend_alias_name = TrifaToxService.Companion.getOrma().selectFromFriendList().
                    tox_public_key_stringEq(friend_pubkey).
                    toList().get(0).alias_name;
        }
        catch (Exception e)
        {
            friend_alias_name = "";
            e.printStackTrace();
        }

        if ((friend_alias_name == null) || (friend_alias_name.equals("")))
        {
            try
            {
                friend_name = TrifaToxService.Companion.getOrma().selectFromFriendList().
                        tox_public_key_stringEq(friend_pubkey).
                        toList().get(0).name;
            }
            catch (Exception e)
            {
                friend_name = "";
                e.printStackTrace();
            }

            if ((friend_name != null) && (!friend_name.equals("")))
            {
                ret = friend_name;
            }
        }
        else
        {
            ret = friend_alias_name;
        }

        return ret;
    }

    static FriendList main_get_friend(long friendnum) {
        FriendList f = null;

        try {
            String pubkey_temp = tox_friend_get_public_key(friendnum);
            // Log.i(TAG, "main_get_friend:pubkey=" + pubkey_temp + " fnum=" + friendnum);
            List<FriendList> fl = TrifaToxService.Companion.getOrma().selectFromFriendList().tox_public_key_stringEq(tox_friend_get_public_key(friendnum)).toList();

            // Log.i(TAG, "main_get_friend:fl=" + fl + " size=" + fl.size());

            if (fl.size() > 0) {
                f = fl.get(0);
                // Log.i(TAG, "main_get_friend:f=" + f);
            } else {
                f = null;
            }
        } catch (Exception e) {
            f = null;
        }

        return f;
    }

    static FriendList main_get_friend(String friend_pubkey) {
        FriendList f = null;

        try {
            List<FriendList> fl = TrifaToxService.Companion.getOrma().selectFromFriendList().tox_public_key_stringEq(friend_pubkey).toList();

            if (fl.size() > 0) {
                f = fl.get(0);
            } else {
                f = null;
            }
        } catch (Exception e) {
            f = null;
        }

        return f;
    }

    static void delete_friend_all_filetransfers(final String friendpubkey) {
        try {
            Log.i(TAG, "delete_ft:ALL for friend=" + friendpubkey);
            TrifaToxService.Companion.getOrma().deleteFromFiletransfer().tox_public_key_stringEq(friendpubkey).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void delete_friend_all_messages(final String friendpubkey) {
        TrifaToxService.Companion.getOrma().deleteFromMessage().tox_friendpubkeyEq(friendpubkey).execute();
    }

    static void delete_friend(final String friend_pubkey) {
        TrifaToxService.Companion.getOrma().deleteFromFriendList().tox_public_key_stringEq(friend_pubkey).execute();
    }

    public static String get_g_opts(String key) {
        try {
            if (TrifaToxService.Companion.getOrma().selectFromTRIFADatabaseGlobalsNew().keyEq(key).count() == 1) {
                TRIFADatabaseGlobalsNew g_opts = TrifaToxService.Companion.getOrma().selectFromTRIFADatabaseGlobalsNew().keyEq(key).get(0);
                // Log.i(TAG, "get_g_opts:(SELECT):key=" + key);
                return g_opts.value;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "get_g_opts:EE1:" + e.getMessage());
            return null;
        }
    }

    public static void set_g_opts(String key, String value) {
        try {
            TRIFADatabaseGlobalsNew g_opts = new TRIFADatabaseGlobalsNew();
            g_opts.key = key;
            g_opts.value = value;

            try {
                TrifaToxService.Companion.getOrma().insertIntoTRIFADatabaseGlobalsNew(g_opts);
                Log.i(TAG, "set_g_opts:(INSERT):key=" + key + " value=" + "xxxxxxxxxxxxx");
            } catch (Exception e) {
                // e.printStackTrace();
                try {
                    TrifaToxService.Companion.getOrma().updateTRIFADatabaseGlobalsNew().keyEq(key).value(value).execute();
                    Log.i(TAG, "set_g_opts:(UPDATE):key=" + key + " value=" + "xxxxxxxxxxxxxxx");
                } catch (Exception e2) {
                    e2.printStackTrace();
                    Log.i(TAG, "set_g_opts:EE1:" + e2.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "set_g_opts:EE2:" + e.getMessage());
        }
    }

    public static void del_g_opts(String key) {
        try {
            TrifaToxService.Companion.getOrma().deleteFromTRIFADatabaseGlobalsNew().keyEq(key).execute();
            Log.i(TAG, "del_g_opts:(DELETE):key=" + key);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "del_g_opts:EE2:" + e.getMessage());
        }
    }

    static void friend_call_push_url(final String friend_pubkey, final long message_timestamp_circa) {
        try {
            final String pushurl_for_friend = get_pushurl_for_friend(friend_pubkey);

            if (pushurl_for_friend != null) {
                if (pushurl_for_friend.length() > "https://".length()) {
                    if (is_valid_pushurl_for_friend_with_whitelist(pushurl_for_friend)) {

                        Thread t = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    friend_do_actual_weburl_call(friend_pubkey, pushurl_for_friend, message_timestamp_circa, true);
                                    // HINT: trigger push again after PUSH_URL_TRIGGER_AGAIN_SECONDS seconds to
                                    //       make sure iphones actually get online and receive the message
                                    boolean res = false;
                                    for (int j = 0; j < PUSH_URL_TRIGGER_AGAIN_MAX_COUNT; j++) {
                                        Thread.sleep(PUSH_URL_TRIGGER_AGAIN_SECONDS * 1000);
                                        res = friend_do_actual_weburl_call(friend_pubkey, pushurl_for_friend, message_timestamp_circa, false);
                                        if (res) {
                                            Log.i(TAG, "friend_call_push_url:BREAK");
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.i(TAG, "friend_call_push_url:EE2:" + e.getMessage());
                                }
                            }
                        };
                        t.start();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    /*
     * return true if we should stop triggering push notifications
     *        false otherwise
     */
    static boolean friend_do_actual_weburl_call(final String friend_pubkey, final String pushurl_for_friend, final long message_timestamp_circa, final boolean update_message_flag) {
        try {
            if (!update_message_flag) {
                if (get_message_in_db_sent_push_is_read(friend_pubkey, message_timestamp_circa)) {
                    // message is "read" (received) so stop triggering push notifications
                    return true;
                }
            }

            try {
                HttpClient client = null;

                client = HttpClient.newBuilder().connectTimeout(Duration.of(8, SECONDS)).build();

                //                                   cacheControl(new CacheControl.Builder().noCache().build()).

                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(pushurl_for_friend)).header("User-Agent", GENERIC_TOR_USERAGENT).timeout(Duration.of(5, SECONDS)).POST(HttpRequest.BodyPublishers.ofString("ping=1")).build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    Log.i(TAG, "friend_call_push_url:url=" + pushurl_for_friend + " RES=" + response.statusCode());

                    if ((response.statusCode() < 300) && (response.statusCode() > 199)) {
                        if (update_message_flag) {
                            update_message_in_db_sent_push_set(friend_pubkey, message_timestamp_circa);
                        }
                    }
                } catch (Exception ignored) {
                }
            } catch (Exception e) {
                Log.i(TAG, "friend_call_push_url:001:EE:" + e.getMessage());
            }

        } catch (Exception ignored) {
        }

        return false;
    }

    public static void add_friend_real(final String friend_tox_id)
    {
        // nospam=8 chars, checksum=4 chars
        String friend_public_key = friend_tox_id.substring(0, friend_tox_id.length() - 12);
        // Log.i(TAG, "add_friend_real:add friend PK:" + friend_public_key);
        FriendList f = new FriendList();
        f.tox_public_key_string = friend_public_key.toUpperCase();

        try
        {
            // set name as the last 5 char of TOXID (until we get a name sent from friend)
            f.name = friend_public_key.substring(friend_public_key.length() - 5,
                    friend_public_key.length());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            f.name = "Unknown";
        }

        f.TOX_USER_STATUS = 0;
        f.TOX_CONNECTION = 0;
        f.TOX_CONNECTION_on_off = 0;
        f.avatar_filename = null;
        f.avatar_pathname = null;

        try
        {
            insert_into_friendlist_db(f);
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    static void add_friend_to_system(final String friend_public_key, final boolean as_friends_relay, final String owner_public_key)
    {
        final FriendList f = new FriendList();
        f.tox_public_key_string = friend_public_key;
        f.TOX_USER_STATUS = 0;
        f.TOX_CONNECTION = 0;
        f.TOX_CONNECTION_on_off = 0;
        // set name as the last 5 char of the publickey (until we get a proper name)
        f.name = friend_public_key.substring(friend_public_key.length() - 5, friend_public_key.length());
        f.avatar_pathname = null;
        f.avatar_filename = null;
        f.capabilities = 0;

        try
        {
            f.added_timestamp = System.currentTimeMillis();
            insert_into_friendlist_db(f);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "friend_request:insert:EE2:" + e.getMessage());
            return;
        }

        if (as_friends_relay)
        {
            // add relay for friend to DB
            // TODO // HelperRelay.add_or_update_friend_relay(friend_public_key, owner_public_key);
        }

        if (MainActivity.getDB_PREF__U_keep_nospam() == false)
        {
            // ---- set new random nospam value after each added friend ----
            // ---- set new random nospam value after each added friend ----
            // ---- set new random nospam value after each added friend ----
            HelperGeneric.set_new_random_nospam_value();
            final String my_tox_id_local = get_my_toxid();
            global_my_toxid = my_tox_id_local;
        }
    }

    synchronized static void insert_into_friendlist_db(final FriendList f)
    {
        try
        {
            if (TrifaToxService.Companion.getOrma().selectFromFriendList().
                    tox_public_key_stringEq(f.tox_public_key_string).count() == 0)
            {
                f.added_timestamp = System.currentTimeMillis();
                TrifaToxService.Companion.getOrma().insertIntoFriendList(f);
                Log.i(TAG, "friend added to DB: " + f.tox_public_key_string.substring(0, 5));
            }
            else
            {
                // friend already in DB
                Log.i(TAG, "friend already in DB: " + f.tox_public_key_string.substring(0, 5));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "friend add to DB:EE:" + e.getMessage());
        }

        //            }
        //        };
        //        t.start();
    }

    static void update_friend_in_db_capabilities(FriendList f)
    {
        TrifaToxService.Companion.getOrma().updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                capabilities(f.capabilities).
                execute();
    }

    static void update_friend_in_db_msgv3_capability(FriendList f)
    {
        TrifaToxService.Companion.getOrma().updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                msgv3_capability(f.msgv3_capability).
                execute();
    }

    static void add_pushurl_for_friend(final String friend_push_url, final String friend_pubkey)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateFriendList().tox_public_key_stringEq(friend_pubkey).push_url(friend_push_url).execute();
        }
        catch (Exception e)
        {
            Log.i(TAG, "add_pushurl_for_friend:EE:" + e.getMessage());
        }
    }

    static void remove_pushurl_for_friend(final String friend_pubkey)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateFriendList().tox_public_key_stringEq(friend_pubkey).push_url(null).execute();
        }
        catch (Exception e)
        {
            Log.i(TAG, "remove_pushurl_for_friend:EE:" + e.getMessage());
        }
    }
}

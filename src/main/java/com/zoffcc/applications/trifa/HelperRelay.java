/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2020 Zoff <zoff@zoff.cc>
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

import com.zoffcc.applications.sorm.FriendList;
import com.zoffcc.applications.sorm.OrmaDatabase;
import com.zoffcc.applications.sorm.RelayListDB;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static com.zoffcc.applications.sorm.OrmaDatabase.s;
import static com.zoffcc.applications.trifa.HelperFriend.add_friend_to_system;
import static com.zoffcc.applications.trifa.HelperGeneric.*;
import static com.zoffcc.applications.trifa.HelperGroup.hex_to_bytes;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONTROL_PROXY_MESSAGE_TYPE.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_NTFY_PUSH_URL_PREFIX;
import static com.zoffcc.applications.trifa.ToxVars.*;

public class HelperRelay
{
    private static final String TAG = "trifa.Hlp.Relay";

    public static String get_friend_of_relay(String relay_pubkey)
    {
        try
        {
            return TrifaToxService.Companion.getOrma().selectFromFriendList().
                    is_relayNotEq(true).
                    tox_public_key_stringEq(
                    TrifaToxService.Companion.getOrma().selectFromRelayListDB().
                            tox_public_key_stringEq(relay_pubkey).get(0).tox_public_key_string_of_owner.toUpperCase()
                    ).get(0).tox_public_key_string.toUpperCase();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            // Log.i(TAG, "get_friend_of_relay:EE1:" + e.getMessage());
            return null;
        }
    }

    public static String get_relay_for_friend(String friend_pubkey)
    {
        try
        {
            String ret = null;
            ret = TrifaToxService.Companion.getOrma().selectFromRelayListDB().
                    own_relayEq(false).tox_public_key_string_of_ownerEq(friend_pubkey).get(0).tox_public_key_string;
            return ret;
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            // Log.i(TAG, "get_relay_for_friend:EE1:" + e.getMessage());
            return null;
        }
    }

    public static String get_pushurl_for_friend(String friend_pubkey)
    {
        String ret = null;

        try
        {
            ret = TrifaToxService.Companion.getOrma().selectFromFriendList().tox_public_key_stringEq(friend_pubkey).get(0)
                    .push_url;
        }
        catch (Exception e)
        {
        }

        return ret;
    }

    static boolean is_valid_pushurl_for_friend_with_whitelist(String push_url)
    {
        // whitelist google FCM gateway
        if (push_url.length() > NOTIFICATION_FCM_PUSH_URL_PREFIX.length())
        {
            if (push_url.startsWith(NOTIFICATION_FCM_PUSH_URL_PREFIX))
            {
                return true;
            }
        }

        // whitelist OLD google FCM gateway
        if (push_url.length() > NOTIFICATION_FCM_PUSH_URL_PREFIX_OLD.length())
        {
            if (push_url.startsWith(NOTIFICATION_FCM_PUSH_URL_PREFIX_OLD))
            {
                return true;
            }
        }

        // whitelist unified push demo server
        if (push_url.length() > NOTIFICATION_UP_PUSH_URL_PREFIX.length())
        {
            if (push_url.startsWith(NOTIFICATION_UP_PUSH_URL_PREFIX))
            {
                return true;
            }
        }

        // whitelist ntfy.sh server
        if (push_url.length() > NOTIFICATION_NTFY_PUSH_URL_PREFIX.length())
        {
            if (push_url.startsWith(NOTIFICATION_NTFY_PUSH_URL_PREFIX))
            {
                return true;
            }
        }

        // anything else is not allowed at this time!
        return false;
    }

    public static boolean is_any_relay(String friend_pubkey)
    {
        try
        {
            int count = TrifaToxService.Companion.getOrma().selectFromFriendList().
                    tox_public_key_stringEq(friend_pubkey.toUpperCase()).
                    is_relayEq(true).count();
            if (count > 0)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static boolean have_own_relay()
    {
        boolean ret = false;
        int num = TrifaToxService.Companion.getOrma().selectFromRelayListDB()
                .own_relayEq(true).count();
        if (num == 1)
        {
            ret = true;
        }
        return ret;
    }

    public static String get_own_relay_pubkey()
    {
        String ret = null;

        try
        {
            ret = TrifaToxService.Companion.getOrma().selectFromRelayListDB()
                    .own_relayEq(true).get(0).tox_public_key_string;
        }
        catch (Exception e)
        {
        }

        return ret;
    }

    public static boolean is_own_relay(String friend_pubkey)
    {
        boolean ret = false;

        try
        {
            String own_relay_pubkey = get_own_relay_pubkey();

            if (own_relay_pubkey != null)
            {
                if (friend_pubkey.equals(own_relay_pubkey) == true)
                {
                    ret = true;
                }
            }
        }
        catch (Exception e)
        {
        }

        return ret;
    }

    public static void delete_relay(String relay_pubkey, boolean delete_from_friends)
    {
        try
        {
            if (relay_pubkey != null)
            {
                try
                {
                    TrifaToxService.Companion.getOrma().updateFriendList()
                            .tox_public_key_stringEq(relay_pubkey)
                            .is_relay(false).execute();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }

                try
                {
                    TrifaToxService.Companion.getOrma().deleteFromRelayListDB()
                            .tox_public_key_stringEq(relay_pubkey).execute();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }

            if (delete_from_friends)
            {
                delete_friend_wrapper(relay_pubkey, "Relay removed");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static boolean remove_own_relay_in_db()
    {
        boolean ret = false;
        try
        {
            final List<RelayListDB> rl = TrifaToxService.Companion.getOrma()
                    .selectFromRelayListDB().own_relayEq(true).toList();

            if ((rl != null) && (rl.size() > 0))
            {
                for (RelayListDB r : rl) {
                    try {
                        delete_friend_wrapper(r.tox_public_key_string.toUpperCase(), null);
                    } catch (Exception e2) {
                    }
                }
                TrifaToxService.Companion.getOrma()
                        .deleteFromRelayListDB().own_relayEq(true).execute();
                JavaSnackBarToast("Own Relay deleted");
                ret = true;
            }
        }
        catch (Exception e1)
        {
            Log.i(TAG, "remove_own_relay_in_db:EE3:" + e1.getMessage());
        }
        return ret;
    }

    public static boolean set_friend_as_own_relay_in_db(String friend_public_key)
    {
        boolean ret = false;

        if (friend_public_key == null) {
            return false;
        }

        final String own_relay_pubkey_current = get_own_relay_pubkey();
        // Log.i(TAG, "friend_as_relay_own_in_db: own_relay_pubkey_current=" + own_relay_pubkey_current + " friend_public_key=" + friend_public_key);
        if ((own_relay_pubkey_current != null) &&
                (own_relay_pubkey_current.toUpperCase().equals(friend_public_key.toUpperCase()))) {
            JavaSnackBarToast("this is already your own Relay");
            Log.i(TAG, "friend_as_relay_own_in_db: this is already your relay");
            return true;
        }

        if (own_relay_pubkey_current != null) {
            remove_own_relay_in_db();
            Log.i(TAG, "friend_as_relay_own_in_db: fully removing old relay");
        }

        try
        {
            final List<FriendList> fl = TrifaToxService.Companion.getOrma().selectFromFriendList()
                    .tox_public_key_stringEq(friend_public_key).toList();

            if (fl.size() == 1)
            {
                // add relay to DB table
                RelayListDB new_relay = new RelayListDB();
                new_relay.own_relay = true;
                new_relay.TOX_CONNECTION = fl.get(0).TOX_CONNECTION;
                new_relay.TOX_CONNECTION_on_off = fl.get(0).TOX_CONNECTION_on_off;
                new_relay.last_online_timestamp = fl.get(0).last_online_timestamp;
                new_relay.tox_public_key_string = friend_public_key;
                new_relay.tox_public_key_string_of_owner = "-- OWN RELAY --";
                //
                TrifaToxService.Companion.getOrma().insertIntoRelayListDB(new_relay);
                // Log.i(TAG, "friend_as_relay_own_in_db:+ADD own relay+");
                // friend exists -> update
                TrifaToxService.Companion.getOrma().updateFriendList()
                        .tox_public_key_stringEq(friend_public_key).is_relay(true).execute();
                // Log.i(TAG, "friend_as_relay_own_in_db:+UPDATE friend+");
                ret = true;

                JavaSnackBarToast("add own Relay");
            }
        }
        catch (Exception e1)
        {
            Log.i(TAG, "friend_as_relay_own_in_db:EE3:" + e1.getMessage());
        }

        return ret;
    }

    static void delete_friend_current_relay(String friend_pubkey, boolean delete_from_friends)
    {
        try
        {
            String friend_current_relay_pubkey = get_relay_for_friend(friend_pubkey).toUpperCase();

            if (friend_current_relay_pubkey != null)
            {
                try
                {
                    TrifaToxService.Companion.getOrma().updateFriendList()
                            .tox_public_key_stringEq(friend_current_relay_pubkey)
                            .is_relay(false).execute();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }

                try
                {
                    TrifaToxService.Companion.getOrma().deleteFromRelayListDB()
                            .tox_public_key_string_of_ownerEq(friend_pubkey).execute();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }

            if (delete_from_friends)
            {
                delete_friend_wrapper(friend_current_relay_pubkey, "Relay removed");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void add_or_update_own_relay(String relay_public_toxid_string)
    {
        try {
            // Log.i(TAG, "add_or_update_own_relay:001:toxid=" + relay_public_toxid_string);
            if ((relay_public_toxid_string == null)
                    || (relay_public_toxid_string.length() != (TOX_ADDRESS_SIZE * 2))) {
                JavaSnackBarToast("Relay ToxID is empty or wrong length");
                Log.i(TAG, "add_or_update_own_relay:relay toxid null or wrong length");
                return;
            }

            final String relay_public_key_string =
                    relay_public_toxid_string.toUpperCase().substring(0, (TOX_PUBLIC_KEY_SIZE * 2));
            // Log.i(TAG, "add_or_update_own_relay:002:toxpk=" + relay_public_key_string);
            final long friendnum = tox_friend_add(relay_public_toxid_string.toUpperCase(), "please add me");
            boolean add_no_request_needed = true;
            if (friendnum > -1) {
                if (friendnum != UINT32_MAX_JAVA) {
                    add_no_request_needed = false;
                }
            }
            if (add_no_request_needed) {
                tox_friend_add_norequest(relay_public_key_string.toUpperCase());
            }
            update_savedata_file_wrapper();
            add_friend_to_system(relay_public_key_string.toUpperCase(), false, null);

            try {
                set_friend_as_own_relay_in_db(relay_public_key_string);
            } catch (Exception e) {
            }
        } catch (Exception e3) {
        }
    }

    static void add_or_update_friend_relay(String relay_public_key_string, String friend_pubkey)
    {
        Log.i(TAG, "add_or_update_friend_relay:001");
        if (relay_public_key_string == null)
        {
            Log.i(TAG, "add_or_update_friend_relay:ret01");
            return;
        }

        if (friend_pubkey == null)
        {
            Log.i(TAG, "add_or_update_friend_relay:ret02");
            return;
        }

        try
        {
            if (!is_any_relay(friend_pubkey))
            {
                String friend_old_relay_pubkey = get_relay_for_friend(friend_pubkey);

                if (friend_old_relay_pubkey != null)
                {
                    // delete old relay
                    delete_friend_current_relay(friend_pubkey, false);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (!is_any_relay(friend_pubkey))
            {
                FriendList fl = HelperFriend.main_get_friend(
                        tox_friend_by_public_key(friend_pubkey));

                if (fl != null)
                {
                    // add relay to DB table
                    RelayListDB new_relay = new RelayListDB();
                    new_relay.own_relay = false;
                    new_relay.TOX_CONNECTION = fl.TOX_CONNECTION;
                    new_relay.TOX_CONNECTION_on_off = fl.TOX_CONNECTION_on_off;
                    new_relay.last_online_timestamp = fl.last_online_timestamp;
                    new_relay.tox_public_key_string = relay_public_key_string.toUpperCase();
                    new_relay.tox_public_key_string_of_owner = friend_pubkey;

                    //
                    try
                    {
                        TrifaToxService.Companion.getOrma().insertIntoRelayListDB(new_relay);
                        Log.i(TAG, "add_or_update_friend_relay:+ADD friend relay+ owner pubkey=" + friend_pubkey);
                    }
                    catch (Exception e2)
                    {
                        // e2.printStackTrace();
                    }

                    // friend exists -> update
                    try
                    {
                        TrifaToxService.Companion.getOrma().updateFriendList().
                                tox_public_key_stringEq(relay_public_key_string).
                                is_relay(true).
                                execute();
                    }
                    catch (Exception e2)
                    {
                        // e2.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void send_relay_pubkey_to_friend(String relay_public_key_string, String friend_pubkey)
    {
        int i = 0;
        long friend_num = tox_friend_by_public_key(friend_pubkey);
        byte[] data = hex_to_bytes("FF" + relay_public_key_string);
        data[0] = (byte) CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND.value;
        // Log.d(TAG, "send_relay_pubkey_to_friend:data=" + data);
        int result = tox_friend_send_lossless_packet(friend_num, data, TOX_PUBLIC_KEY_SIZE + 1);
        // Log.d(TAG, "send_relay_pubkey_to_friend:res=" + result);
    }

    static void send_friend_pubkey_to_relay(String relay_public_key_string, String friend_pubkey)
    {
        int i = 0;
        long friend_num = tox_friend_by_public_key(relay_public_key_string);
        byte[] data = hex_to_bytes("FF" + friend_pubkey);
        data[0] = (byte) CONTROL_PROXY_MESSAGE_TYPE_FRIEND_PUBKEY_FOR_PROXY.value;
        // Log.d(TAG, "send_friend_pubkey_to_relay:data=" + data);
        int result = tox_friend_send_lossless_packet(friend_num, data, TOX_PUBLIC_KEY_SIZE + 1);
        // Log.d(TAG, "send_friend_pubkey_to_relay:res=" + result);
    }

    static void invite_to_all_groups_own_relay(String relay_public_key_string)
    {
        try
        {
            long num_groups = tox_group_get_number_groups();
            long[] group_numbers = tox_group_get_grouplist();
            ByteBuffer groupid_buf3 = ByteBuffer.allocateDirect(GROUP_ID_LENGTH * 2);
            int conf_ = 0;
            while (conf_ < num_groups)
            {
                groupid_buf3.clear();
                if (tox_group_get_chat_id(group_numbers[conf_], groupid_buf3) == 0)
                {
                    byte[] groupid_buffer = new byte[GROUP_ID_LENGTH];
                    groupid_buf3.get(groupid_buffer, 0, GROUP_ID_LENGTH);
                    String group_identifier = HelperGeneric.bytesToHex(groupid_buffer, 0, GROUP_ID_LENGTH).toLowerCase();
                    // Log.i(TAG, "invite_to_all_groups_own_relay: group_identifier=" + group_identifier);
                    final long group_num = tox_group_by_groupid__wrapper(group_identifier);
                    int res = tox_group_invite_friend(group_num,
                            tox_friend_by_public_key(relay_public_key_string));
                    update_savedata_file_wrapper();
                    byte[] data = hex_to_bytes("FF" + group_identifier);
                    data[0] = (byte) CONTROL_PROXY_MESSAGE_TYPE_GROUP_ID_FOR_PROXY.value;
                    tox_friend_send_lossless_packet(tox_friend_by_public_key(relay_public_key_string),
                            data, TOX_GROUP_CHAT_ID_SIZE + 1);
                }
                conf_++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void send_all_friend_pubkeys_to_relay(String relay_public_key_string)
    {
        List<FriendList> fl = TrifaToxService.Companion.getOrma().selectFromFriendList()
                .is_relayNotEq(true).toList();

        if (fl != null)
        {
            if (fl.size() > 0)
            {
                int i = 0;
                long friend_num = tox_friend_by_public_key(relay_public_key_string);

                for (i = 0; i < fl.size(); i++)
                {
                    FriendList n = fl.get(i);
                    byte[] data = hex_to_bytes("FF" + n.tox_public_key_string);
                    data[0] = (byte) CONTROL_PROXY_MESSAGE_TYPE_FRIEND_PUBKEY_FOR_PROXY.value;
                    tox_friend_send_lossless_packet(friend_num, data, TOX_PUBLIC_KEY_SIZE + 1);
                }
            }
        }
    }

    static void send_pushtoken_to_relay()
    {
        // HINT: no push token on desktop
    }
}

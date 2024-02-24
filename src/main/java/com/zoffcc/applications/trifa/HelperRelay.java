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

import java.sql.ResultSet;
import java.sql.Statement;

import static com.zoffcc.applications.sorm.OrmaDatabase.s;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key;
import static com.zoffcc.applications.trifa.TRIFAGlobals.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NOTIFICATION_NTFY_PUSH_URL_PREFIX;

public class HelperRelay
{
    private static final String TAG = "trifa.Hlp.Relay";

    static String get_relay_for_friend(String friend_pubkey)
    {
        try
        {
            String ret = null;
            Statement statement = OrmaDatabase.getSqldb().createStatement();

            ResultSet rs = statement.executeQuery(
                    "select tox_public_key_string from RelayListDB where own_relay='0' and tox_public_key_string_of_owner='" +
                            s(friend_pubkey) + "'");
            if (rs.next())
            {
                ret = rs.getString("tox_public_key_string");
            }

            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
            }

            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_relay_for_friend:EE1:" + e.getMessage());
            return null;
        }
    }

    static String get_pushurl_for_friend(String friend_pubkey)
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

    static boolean is_any_relay(String friend_pubkey)
    {
        try
        {
            Statement statement = OrmaDatabase.getSqldb().createStatement();
            ResultSet rs = statement.executeQuery(
                    "select count(*) as count from FriendList where tox_public_key_string='" +
                            s(friend_pubkey.toUpperCase()) +
                            "' and is_relay='1'");
            if (rs.next())
            {
                int count = rs.getInt("count");
                if (count > 0)
                {
                    try
                    {
                        statement.close();
                    }
                    catch (Exception ignored)
                    {
                    }
                    return true;
                }
            }
            else
            {
                try
                {
                    statement.close();
                }
                catch (Exception ignored)
                {
                }
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }

        return false;
    }

    static void delete_friend_current_relay(String friend_pubkey)
    {
        try
        {
            //**// TODO:
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
                    delete_friend_current_relay(friend_pubkey);
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
}

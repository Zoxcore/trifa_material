/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
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

public class ChatColors
{
    private static final String TAG = "trifa.ChatCols";

    static int[] PeerAvatarColors = {
            //
            // https://www.w3schools.com/colors/colors_picker.asp
            //
            // ** too dark ** // Color.parseColor("#0000FF"), // Blue
            Color.parseColor("#6666ff"), // * lighter blue *
            //
            // ** // Color.parseColor("#FF00FF"), // Fuchsia
            //
            Color.parseColor("#00FFFF"), // Aqua
            //
            Color.parseColor("#008000"), // Green
            //
            Color.parseColor("#dce775"), // Lime
            //
            // ** too dark ** // Color.parseColor("#800000"), // Maroon
            Color.parseColor("#f06292"), // * lighter red *
            //
            // ** too dark ** // Color.parseColor("#000080"), // Navy
            Color.parseColor("#42a5f5"), // * lighter blue *
            //
            Color.parseColor("#808000"), // Olive
            //
            Color.parseColor("#800080"), // Purple
            //
            // ** too dark ** // Color.parseColor("#FF0000"), // Red
            Color.parseColor("#ff4d4d"), // * lighter red *
            //
            Color.parseColor("#008080"), // Teal
            //
            // ** too bright ** // Color.parseColor("#FFFF00")  // Yellow
            Color.parseColor("#cccc00"), // * darker yellow *
            //
    };

    static int get_size()
    {
        return PeerAvatarColors.length;
    }

    static int get_shade(int color, String pubkey)
    {
        // Log.i(TAG, "get_shade:pubkey=" + pubkey + " pubkey.substring(0, 1)=" + pubkey.substring(0, 1));
        // Log.i(TAG, "get_shade:pubkey=" + pubkey + " pubkey.substring(1, 2)=" + pubkey.substring(1, 2));

        float factor =
                (Integer.parseInt(pubkey.substring(0, 1), 16) + (Integer.parseInt(pubkey.substring(1, 2), 16) * 16)) /
                        255.0f;

        final float range = 0.5f;
        final float min_value = 1.0f - (range * 0.6f);
        factor = (factor * range) + min_value;

        return manipulateColor(color, factor);
    }

    public static int manipulateColor(int color, float factor)
    {
        // Log.i(TAG, "manipulateColor:color=" + color + " factor=" + factor);

        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }

    static int hash_to_bucket(String hash_value, int number_of_buckets)
    {
        try
        {
            int ret = 0;
            int value = (Integer.parseInt(hash_value.substring(hash_value.length() - 1, hash_value.length() - 0), 16) +
                    (Integer.parseInt(hash_value.substring(hash_value.length() - 2, hash_value.length() - 1), 16) *
                            16) +
                    (Integer.parseInt(hash_value.substring(hash_value.length() - 3, hash_value.length() - 2), 16) *
                            (16 * 2)) +
                    (Integer.parseInt(hash_value.substring(hash_value.length() - 4, hash_value.length() - 3), 16) *
                            (16 * 3)));

            // Log.i(TAG, "hash_to_bucket:value=" + value);

            ret = (value % number_of_buckets);

            // BigInteger bigInt = new BigInteger(1, hash_value.getBytes());
            // int ret = (int) (bigInt.longValue() % (long) number_of_buckets);
            // // Log.i(TAG, "hash_to_bucket:" + "ret=" + ret + " hash_as_int=" + bigInt + " hash=" + hash_value);
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "hash_to_bucket:EE:" + e.getMessage());
            return 0;
        }
    }

    public static int get_ngc_peer_color(String peer_pubkey)
    {
        return ChatColors.get_shade(
                ChatColors.PeerAvatarColors[hash_to_bucket(peer_pubkey, ChatColors.get_size())],
                peer_pubkey);
    }
}

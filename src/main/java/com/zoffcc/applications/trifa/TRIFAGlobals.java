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

import java.awt.Color;

import static com.zoffcc.applications.trifa.ToxVars.MAX_FILE_DATA_SIZE;


public class TRIFAGlobals
{
    static String global_my_toxid = "";
    static String global_my_name = "";
    static String global_my_status_message = "";
    static boolean bootstrapping = false;
    static long global_self_last_went_online_timestamp = -1;
    static long global_self_last_went_offline_timestamp = 0;
    // bootstrap again after 30 seconds offline
    final static int TOX_BOOTSTRAP_AGAIN_AFTER_OFFLINE_MILLIS = 30 * 1000;

    public static final String MY_PACKAGE_NAME = "com.zoffcc.applications.trifa";

    // ----------
    // https://toxme.io/u/echobot
    //  echobot@toxme.io
    final static String ECHOBOT_TOXID = "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6";
    final static String ECHOBOT_INIT_NAME = "Echobot";
    final static String ECHOBOT_INIT_STATUSMSG = "A tiny bot to test Tox audio and video.";
    final static String TOXIRC_TOKTOK_CONFID = "836eaf5f6af15a9608feb231e48112f074b7625c054446163a4d8311a5abbb19";
    final static String TOXIRC_PUBKEY = "A922A51E1C91205B9F7992E2273107D47C72E8AE909C61C28A77A4A2A115431B";
    // ----------
    // https://toxme.io/u/groupbot
    //  groupbot@toxme.io
    final static String GROUPBOT_TOXID = "56A1ADE4B65B86BCD51CC73E2CD4E542179F47959FE3E0E21B4B0ACDADE51855D34D34D37CB5";

    // TRIfA public info group
    final public static String TOX_TRIFA_PUBLIC_GROUPID = "154b3973bd0e66304fd6179a8a54759073649e09e6e368f0334fc6ed666ab762";

    final static boolean ADD_BOTS_ON_STARTUP = true;

    final static int USE_MAX_NUMBER_OF_BOOTSTRAP_NODES = 40;
    final static int USE_MAX_NUMBER_OF_BOOTSTRAP_TCP_RELAYS = 40;

    final static int HIGHER_GLOBAL_VIDEO_BITRATE = 3500;
    public final static int NORMAL_GLOBAL_VIDEO_BITRATE = 1500;
    public final static int SUPERHIGH_GLOBAL_VIDEO_BITRATE = 8000;
    public final static int MEDIUM_GLOBAL_VIDEO_BITRATE = 700;
    public final static int LOWER_GLOBAL_VIDEO_BITRATE = 350;
    public final static int ABSOLUTE_MINIMUM_GLOBAL_VIDEO_BITRATE = 95;

    public final static int NORMAL_GLOBAL_INCOMING_AV_BUFFER_MS = 0;
    public final static int HIGH_GLOBAL_INCOMING_AV_BUFFER_MS = 60;

    final static int HIGHER_GLOBAL_AUDIO_BITRATE = 128;
    final static int NORMAL_GLOBAL_AUDIO_BITRATE = 16; // 64;
    final static int LOWER_GLOBAL_AUDIO_BITRATE = 8;

    final static int HIGHER_NGC_VIDEO_BITRATE = 400;
    final static int LOWER_NGC_VIDEO_BITRATE = 90;

    final static int NGC_AUDIO_BITRATE = 8000;

    final static int HIGHER_NGC_VIDEO_QUANTIZER = 38; // higher here means "higer video quality" which is a lower q value!
    final static int LOWER_NGC_VIDEO_QUANTIZER = 51;

    public static int GLOBAL_VIDEO_BITRATE = NORMAL_GLOBAL_VIDEO_BITRATE; // this works nice: 2500;
    public static int GLOBAL_AUDIO_BITRATE = NORMAL_GLOBAL_AUDIO_BITRATE; // allowed values: (xx>=6) && (xx<=510)

    static final String GENERIC_TOR_USERAGENT = "Mozilla/5.0 (Windows NT 6.1; rv:60.0) Gecko/20100101 Firefox/60.0";
    static final int PUSH_URL_TRIGGER_AGAIN_MAX_COUNT = 8;
    static final int PUSH_URL_TRIGGER_AGAIN_SECONDS = 21;
    static final int PUSH_URL_TRIGGER_GET_MESSAGE_FOR_delta_ms_prev = 100;
    static final int PUSH_URL_TRIGGER_GET_MESSAGE_FOR_delta_ms_after = 1000;

    static final int VIDEO_CODEC_VP8 = 0;
    static final int VIDEO_CODEC_H264 = 1;

    static final int MESSAGE_PAGING_NUM_MSGS_PER_PAGE = 200;
    static final int MESSAGE_PAGING_LAST_PAGE_MARGIN = 40;
    static final String MESSAGE_PAGING_SHOW_OLDER_HASH = "00000000000000001";
    static final String MESSAGE_PAGING_SHOW_NEWER_HASH = "00000000000000002";

    static int VIDEO_FRAME_RATE_OUTGOING = 0;
    static long last_video_frame_sent = -1;
    static int count_video_frame_sent = 0;
    static int VIDEO_FRAME_RATE_INCOMING = 0;
    static long last_video_frame_received = -1;
    static int count_video_frame_received = 0;

    static final int TOX_ITERATE_MS_MIN_NORMAL = 12;
    static final int TOX_ITERATE_MS_MIN_FILETRANSFER = 8;

    static long ONE_HOUR_IN_MS = 3600 * 1000;

    final static int GLOBAL_MIN_VIDEO_BITRATE = 64;
    final static int GLOBAL_MIN_AUDIO_BITRATE = 8; // allowed values: (xx>=6) && (xx<=510)

    static final int CAMPREVIEW_NUM_BUFFERS = 3;

    static final String TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY = "-1";
    static final Color TRIFA_SYSTEM_MESSAGE_PEER_CHATCOLOR = new Color(0xffc35838, true); // red-ish
    public static final int CONFERENCE_ID_LENGTH = 32;
    public static final int GROUP_ID_LENGTH = 32;

    public static final int MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION = 4;

    public static final long UINT32_MAX_JAVA = 4294967295L; // 0xffffffff == UINT32_MAX

    static long global_last_activity_for_battery_savings_ts = -1;
    static long LAST_ONLINE_TIMSTAMP_ONLINE_NOW = Long.MAX_VALUE - 1;
    static long LAST_ONLINE_TIMSTAMP_ONLINE_OFFLINE = -1;

    public static final int TOX_NGC_HISTORY_SYNC_MAX_PEERNAME_BYTES = 25;
    public static final int TOX_NGC_HISTORY_SYNC_MAX_SECONDS_BACK = 130 * 60; // 130 minutes

    static int MESSAGE_GROUP_HISTORY_SYNC_DOUBLE_INTERVAL_SECS = 60 * 60 * 24 * 1; // 1 day

    static long global_last_activity_outgoung_ft_ts = -1;

    final static String NOTIFICATION_FCM_PUSH_URL_PREFIX = "https://tox.zoff.xyz/toxfcm/fcm.php?id=";
    final static String NOTIFICATION_FCM_PUSH_URL_PREFIX_OLD = "https://toxcon2020.zoff.cc/toxfcm/fcm.php?id=";
    final static String NOTIFICATION_UP_PUSH_URL_PREFIX = "https://gotify1.unifiedpush.org/UP?token=";
    final static String NOTIFICATION_NTFY_PUSH_URL_PREFIX = "https://ntfy.sh/";

    public static final Color CHAT_MSG_BG_SELF_COLOR = new Color(0xff33b5e5, true);
    public static final Color CHAT_MSG_BG_OTHER_COLOR = new Color(0xffffbb33, true);
    public static final Color SEE_THRU = new Color(0x00111111, true);

    static int MESSAGE_SYNC_DOUBLE_INTERVAL_SECS = 20;
    static long MESSAGE_V2_MSG_SENT_OK = (Long.MAX_VALUE - 1);
    static int global_self_connection_status = ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE.value;

    static String VFS_TMP_FILE_DIR = "./tempdir/files/";
    // final static String VFS_TMP_AVATAR_DIR = "/avatar_tempdir/files/"; // TODO: avatar should get their own directory!
    public static String VFS_FILE_DIR = "./datadir/files/";
    final static String VFS_OWN_AVATAR_DIR = "./datadir/myavatar/";
    static String VFS_PREFIX = ""; // only set for normal (unencrypted) storage

    public final static String TRIFA_GITHUB_REPO_URL = "https://github.com/Zoxcore/trifa_material";
    public final static String TRIFA_GITHUB_NEW_ISSUE_URL = TRIFA_GITHUB_REPO_URL + "/issues/new";

    final static String IMAGE_THUMBNAIL_PLACEHOLDER = "image_thumb.png";
    final static long IMAGE_FILESIZE_MAX_BYTES_FOR_THUMB = 5000000;
    final static long IMAGE_FILE_MAX_RENDER_MS = 60;

    final static long AVATAR_INCOMING_MAX_BYTE_SIZE = 1 * 1024 * 1024; // limit incoming avatars at 1MByte size
    final static long AVATAR_SELF_MAX_BYTE_SIZE = 1 * 1024 * 1024; // limit incoming avatars at 1MByte size
    final static String FRIEND_AVATAR_FILENAME = "_____xyz____avatar.png";

    // ---- lookup cache ----
    // static Map<String, java.io.FileInputStream> cache_ft_fis = new HashMap<String, java.io.FileInputStream>();
    // static Map<String, java.io.FileOutputStream> cache_ft_fos = new HashMap<String, java.io.FileOutputStream>();
    // static Map<String, java.io.FileOutputStream> cache_ft_fos_normal = new HashMap<String, java.io.FileOutputStream>();
    // ---- lookup cache ----

    static final long UPDATE_MESSAGE_PROGRESS_AFTER_BYTES =
            120L * MAX_FILE_DATA_SIZE; // x kBytes // update FT and progress bars every XX bytes
    static final long UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES = 8L * MAX_FILE_DATA_SIZE; // x kBytes
    static final long UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES = 250000L; // x kBytes, less than this in bytes is a small file

    // static boolean global_incoming_ft_active = false;

    static final int FRIEND_NAME_DISPLAY_MENU_MAXLEN = 50;
    static final int CONFERENCE_NAME_DISPLAY_MENU_MAXLEN = 50;
    static final int FT_IMAGE_THUMBNAIL_WIDTH = 200;
    static final int FT_IMAGE_THUMBNAIL_HEIGHT = 90;

    public static final Color COLOR_SELECTED_TOX_FRIEND = new Color(0x33cc00ff, true);

    public static enum TRIFA_FT_DIRECTION
    {
        TRIFA_FT_DIRECTION_INCOMING(0), TRIFA_FT_DIRECTION_OUTGOING(1);

        public int value;

        private TRIFA_FT_DIRECTION(int value)
        {
            this.value = value;
        }


    }

    public static enum TRIFA_MSG_DIRECTION
    {
        TRIFA_MSG_DIRECTION_RECVD(0), TRIFA_MSG_DIRECTION_SENT(1) ;

        public int value;

        private TRIFA_MSG_DIRECTION(int value)
        {
            this.value = value;
        }

    }

    public static enum TRIFA_MSG_TYPE
    {
        TRIFA_MSG_TYPE_TEXT(0), TRIFA_MSG_FILE(1);

        public int value;

        private TRIFA_MSG_TYPE(int value)
        {
            this.value = value;
        }

    }

    public static enum TRIFA_SYNC_TYPE
    {
        TRIFA_SYNC_TYPE_NONE(0), TRIFA_SYNC_TYPE_TOXPROXY(1), TRIFA_SYNC_TYPE_NGC_PEERS(2);

        public int value;

        private TRIFA_SYNC_TYPE(int value)
        {
            this.value = value;
        }
    }

    public static enum CONTROL_PROXY_MESSAGE_TYPE
    {
        CONTROL_PROXY_MESSAGE_TYPE_FRIEND_PUBKEY_FOR_PROXY(175), CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND(
            176), CONTROL_PROXY_MESSAGE_TYPE_ALL_MESSAGES_SENT(177), CONTROL_PROXY_MESSAGE_TYPE_PROXY_KILLSWITCH(
            178), CONTROL_PROXY_MESSAGE_TYPE_NOTIFICATION_TOKEN(179), CONTROL_PROXY_MESSAGE_TYPE_PUSH_URL_FOR_FRIEND(
            181), CONTROL_PROXY_MESSAGE_TYPE_GROUP_ID_FOR_PROXY(182);

        public int value;

        private CONTROL_PROXY_MESSAGE_TYPE(int value)
        {
            this.value = value;
        }
    }
}

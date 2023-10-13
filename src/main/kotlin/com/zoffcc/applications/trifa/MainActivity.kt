package com.zoffcc.applications.trifa

import Action
import Message
import User
import com.zoffcc.applications.trifa.HelperFriend.send_friend_msg_receipt_v2_wrapper
import com.zoffcc.applications.trifa.HelperGeneric.bytesToHex
import com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper
import com.zoffcc.applications.trifa.TRIFAGlobals.*
import com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH
import contactstore
import lock_data_dir_input
import org.briarproject.briar.desktop.contact.ContactItem
import set_tox_online_state
import store
import timestampMs
import toxdatastore
import java.io.File
import java.io.PrintWriter
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore


@Suppress("UNUSED_PARAMETER")
class MainActivity {
    val nativeLibAPI: String?
        external get

    companion object {
        private const val TAG = "trifa.MainActivity"
        const val Version = "0.99.0"

        // --------- global config ---------
        // --------- global config ---------
        const val CTOXCORE_NATIVE_LOGGING = true // set "false" for release builds

        // --------- global config ---------
        // --------- global config ---------
        var native_lib_loaded = false
        var tox_service_fg: TrifaToxService? = null
        var tox_savefile_directory = "."
        var PREF__udp_enabled = 1
        var PREF__orbot_enabled_to_int = 0
        var PREF__local_discovery_enabled = 1
        var PREF__ipv6_enabled = 1
        var PREF__force_udp_only = 0

        var incoming_messages_queue: BlockingQueue<String> = LinkedBlockingQueue()

        //
        var PREF__ngc_video_bitrate: Int = LOWER_NGC_VIDEO_BITRATE // ~600 kbits/s -> ~60 kbytes/s
        var PREF__ngc_video_max_quantizer: Int =
            LOWER_NGC_VIDEO_QUANTIZER // 47 -> default, 51 -> lowest quality, 30 -> very high quality and lots of bandwidth!
        var PREF__ngc_audio_bitrate: Int = NGC_AUDIO_BITRATE
        var PREF__ngc_audio_samplerate = 48000
        var PREF__ngc_audio_channels = 1

        var PREF__tox_savefile_dir = "."
        @JvmField
        var PREF__database_files_dir = "."

        //
        // !!!!!! DEBUG !!!!!! change to real password later !!!!!!
        // !!!!!! DEBUG !!!!!! change to real password later !!!!!!
        //
        var password_hash = """passXY!7$9%"""

        //
        // !!!!!! DEBUG !!!!!! change to real password later !!!!!!
        // !!!!!! DEBUG !!!!!! change to real password later !!!!!!
        //
        var semaphore_tox_savedata: Semaphore? = Semaphore(1)

        fun main_init() {
            println("Version:" + Version)
            try {
                println("java.vm.name:" + System.getProperty("java.vm.name"))
                println("java.home:" + System.getProperty("java.home"))
                println("java.vendor:" + System.getProperty("java.vendor"))
                println("java.version:" + System.getProperty("java.version"))
                println("java.specification.vendor:" + System.getProperty("java.specification.vendor"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val locale = Locale.getDefault()
            Log.i(TAG, locale.displayCountry)
            Log.i(TAG, locale.displayLanguage)
            Log.i(TAG, locale.displayName)
            Log.i(TAG, locale.isO3Country)
            Log.i(TAG, locale.isO3Language)
            Log.i(TAG, locale.language)
            Log.i(TAG, locale.country)
            try {
                Thread.currentThread().name = "t_main"
            } catch (e: Exception) {
            }
            Log.i(TAG, "java.library.path:" + System.getProperty("java.library.path"))
            Log.i(
                TAG,
                "loaded:c-toxcore:v" + tox_version_major() + "." + tox_version_minor() + "." + tox_version_patch()
            )
            Log.i(TAG, "loaded:jni-c-toxcore:v" + jnictoxcore_version())

            Log.i(TAG, "tox_service_fg:" + tox_service_fg)
            Log.i(TAG, "native_lib_loaded:" + native_lib_loaded)
            Log.i(TAG, "MainActivity:" + this)

            tox_service_fg = TrifaToxService()

            lock_data_dir_input()

            if (!TrifaToxService.TOX_SERVICE_STARTED) {
                var ORBOT_PROXY_HOST = ""
                var ORBOT_PROXY_PORT: Long = 0
                tox_savefile_directory = PREF__tox_savefile_dir + File.separator
                Log.i(TAG, "init:PREF__udp_enabled=$PREF__udp_enabled")
                init(
                    tox_savefile_directory,
                    PREF__udp_enabled,
                    PREF__local_discovery_enabled,
                    PREF__orbot_enabled_to_int,
                    ORBOT_PROXY_HOST,
                    ORBOT_PROXY_PORT,
                    password_hash,
                    PREF__ipv6_enabled,
                    PREF__force_udp_only,
                    PREF__ngc_video_bitrate,
                    PREF__ngc_video_max_quantizer,
                    PREF__ngc_audio_bitrate,
                    PREF__ngc_audio_samplerate,
                    PREF__ngc_audio_channels
                )
                tox_service_fg!!.tox_thread_start_fg()
            }

            val my_tox_id_temp = get_my_toxid()
            Log.i(TAG, "MyToxID:$my_tox_id_temp")
            try {
                Thread.currentThread().name = "t_main"
            } catch (_: Exception) {
            }

            try {
            toxdatastore.updateToxID(my_tox_id_temp)
            } catch (_: Exception) {
            }

            try {
                PrintWriter(PREF__tox_savefile_dir + File.separator + "toxid.txt", "UTF-8")
                    .use { out -> out.write(my_tox_id_temp) }
                Log.i(
                    TAG, "writing toxid to: "
                            + File(PREF__tox_savefile_dir).canonicalPath +
                            File.separator + "toxid.txt"
                )
            } catch (_: Exception) {
            }
        }

        init {
            if (!native_lib_loaded) {
                val resourcesDir = File(System.getProperty("compose.application.resources.dir"))
                System.out.println("XXXXX1:" + resourcesDir)
                System.out.println("XXXXX1.1:OS:" + OperatingSystem.getCurrent())
                System.out.println("XXXXX1.2:OS:" + OperatingSystem.getName())
                System.out.println("XXXXX1.3:OS:" + OperatingSystem.getArchitecture())
                var libFile = File(resourcesDir, "libjni-c-toxcore.so")
                if (OperatingSystem.getCurrent() == OperatingSystem.LINUX) {
                    libFile = File(resourcesDir, "libjni-c-toxcore.so")
                } else if (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS) {
                    libFile = File(resourcesDir, "jni-c-toxcore.dll")
                } else if (OperatingSystem.getCurrent() == OperatingSystem.MACOS) {
                    libFile = File(resourcesDir, "libjni-c-toxcore.jnilib")
                } else {
                    System.out.println("XXXXX1.1:OS:Unknown operating system:EXIT")
                    System.exit(3)
                }
                System.out.println("XXXXX2:" + libFile + " " + libFile.canonicalPath)

                try {
                    System.load(libFile.canonicalPath)
                    native_lib_loaded = true
                    Log.i(TAG, "successfully loaded native library")
                } catch (e: UnsatisfiedLinkError) {
                    native_lib_loaded = false
                    Log.i(TAG, "loadLibrary jni-c-toxcore failed!")
                    e.printStackTrace()
                    System.exit(4)
                }
            }
        }

        // -------- native methods --------
        // -------- native methods --------
        // -------- native methods --------
        @JvmStatic
        external fun init(
            data_dir: String?,
            udp_enabled: Int,
            local_discovery_enabled: Int,
            orbot_enabled: Int,
            orbot_host: String?,
            orbot_port: Long,
            tox_encrypt_passphrase_hash: String?,
            enable_ipv6: Int,
            force_udp_only_mode: Int,
            ngc_video_bitrate: Int,
            max_quantizer: Int,
            ngc_audio_bitrate: Int,
            ngc_audio_sampling_rate: Int,
            ngc_audio_channel_count: Int
        )

        val nativeLibGITHASH: String?
            external get
        val nativeLibTOXGITHASH: String?
            external get

        @JvmStatic
        external fun update_savedata_file(tox_encrypt_passphrase_hash: String?)

        @JvmStatic
        external fun get_my_toxid(): String

        @JvmStatic
        external fun add_tcp_relay_single(ip: String?, key_hex: String?, port: Long): Int

        @JvmStatic
        external fun bootstrap_single(ip: String?, key_hex: String?, port: Long): Int

        @JvmStatic
        external fun tox_self_get_connection_status(): Int

        @JvmStatic
        external fun init_tox_callbacks()

        @JvmStatic
        external fun tox_iteration_interval(): Long

        @JvmStatic
        external fun tox_iterate(): Long

        // ----------- TRIfA internal -----------
        @JvmStatic
        external fun jni_iterate_group_audio(delta_new: Int, want_ms_output: Int): Int

        @JvmStatic
        external fun jni_iterate_videocall_audio(
            delta_new: Int,
            want_ms_output: Int,
            channels: Int,
            sample_rate: Int,
            send_emtpy_buffer: Int
        ): Int

        @JvmStatic
        external fun crgb2yuv(
            rgba_buf: ByteBuffer?,
            yuv_buf: ByteBuffer?,
            w_yuv: Int,
            h_yuv: Int,
            w_rgba: Int,
            h_rgba: Int
        )

        // ------------------------------

        @JvmStatic
        external fun tox_set_do_not_sync_av(do_not_sync_av: Int)

        @JvmStatic
        external fun tox_set_onion_active(active: Int)

        // ----------- TRIfA internal -----------
        @JvmStatic
        external fun tox_kill(): Long

        @JvmStatic
        external fun exit()

        @JvmStatic
        external fun tox_friend_send_message(friendnum: Long, a_TOX_MESSAGE_TYPE: Int, message: String?): Long

        @JvmStatic
        external fun tox_version_major(): Long

        @JvmStatic
        external fun tox_version_minor(): Long

        @JvmStatic
        external fun tox_version_patch(): Long

        @JvmStatic
        external fun jnictoxcore_version(): String

        @JvmStatic
        external fun libavutil_version(): String?

        @JvmStatic
        external fun libopus_version(): String?

        @JvmStatic
        external fun libsodium_version(): String?

        @JvmStatic
        external fun tox_max_filename_length(): Long

        @JvmStatic
        external fun tox_file_id_length(): Long

        @JvmStatic
        external fun tox_max_message_length(): Long

        @JvmStatic
        external fun tox_friend_add(toxid_str: String?, message: String?): Long

        @JvmStatic
        external fun tox_friend_add_norequest(public_key_str: String?): Long

        @JvmStatic
        external fun tox_self_get_friend_list_size(): Long

        @JvmStatic
        external fun tox_self_set_nospam(nospam: Long) // this actually needs an "uint32_t" which is an unsigned 32bit integer value

        @JvmStatic
        external fun tox_self_get_nospam(): Long // this actually returns an "uint32_t" which is an unsigned 32bit integer value

        @JvmStatic
        external fun tox_friend_by_public_key(friend_public_key_string: String?): Long

        @JvmStatic
        external fun tox_friend_get_public_key(friend_number: Long): String?

        @JvmStatic
        external fun tox_friend_get_name(friend_number: Long): String?

        @JvmStatic
        external fun tox_friend_get_capabilities(friend_number: Long): Long

        @JvmStatic
        external fun tox_self_get_friend_list(): LongArray?

        @JvmStatic
        external fun tox_self_set_name(name: String?): Int

        @JvmStatic
        external fun tox_self_set_status_message(status_message: String?): Int

        @JvmStatic
        external fun tox_self_set_status(a_TOX_USER_STATUS: Int)

        @JvmStatic
        external fun tox_self_set_typing(friend_number: Long, typing: Int): Int

        @JvmStatic
        external fun tox_friend_get_connection_status(friend_number: Long): Int

        @JvmStatic
        external fun tox_friend_delete(friend_number: Long): Int

        @JvmStatic
        external fun tox_self_get_name(): String?

        @JvmStatic
        external fun tox_self_get_name_size(): Long

        @JvmStatic
        external fun tox_self_get_status_message_size(): Long

        @JvmStatic
        external fun tox_self_get_status_message(): String?

        @JvmStatic
        external fun tox_friend_send_lossless_packet(friend_number: Long, data: ByteArray?, data_length: Int): Int

        @JvmStatic
        external fun tox_file_control(friend_number: Long, file_number: Long, a_TOX_FILE_CONTROL: Int): Int

        @JvmStatic
        external fun tox_hash(hash_buffer: ByteBuffer?, data_buffer: ByteBuffer?, data_length: Long): Int

        @JvmStatic
        external fun tox_file_seek(friend_number: Long, file_number: Long, position: Long): Int

        @JvmStatic
        external fun tox_file_get_file_id(friend_number: Long, file_number: Long, file_id_buffer: ByteBuffer?): Int

        @JvmStatic
        external fun tox_file_send(
            friend_number: Long,
            kind: Long,
            file_size: Long,
            file_id_buffer: ByteBuffer?,
            file_name: String?,
            filename_length: Long
        ): Long

        @JvmStatic
        external fun tox_file_send_chunk(
            friend_number: Long,
            file_number: Long,
            position: Long,
            data_buffer: ByteBuffer?,
            data_length: Long
        ): Int

        // --------------- Message V2 -------------
        // --------------- Message V2 -------------
        // --------------- Message V2 -------------
        @JvmStatic
        external fun tox_messagev2_size(text_length: Long, type: Long, alter_type: Long): Long

        @JvmStatic
        external fun tox_messagev2_wrap(
            text_length: Long,
            type: Long,
            alter_type: Long,
            message_text_buffer: ByteBuffer?,
            ts_sec: Long,
            ts_ms: Long,
            raw_message_buffer: ByteBuffer?,
            msgid_buffer: ByteBuffer?
        ): Int

        @JvmStatic
        external fun tox_messagev2_get_message_id(raw_message_buffer: ByteBuffer?, msgid_buffer: ByteBuffer?): Int

        @JvmStatic
        external fun tox_messagev2_get_ts_sec(raw_message_buffer: ByteBuffer?): Long

        @JvmStatic
        external fun tox_messagev2_get_ts_ms(raw_message_buffer: ByteBuffer?): Long

        @JvmStatic
        external fun tox_messagev2_get_message_text(
            raw_message_buffer: ByteBuffer?,
            raw_message_len: Long,
            is_alter_msg: Int,
            alter_type: Long,
            message_text_buffer: ByteBuffer?
        ): Long

        @JvmStatic
        external fun tox_messagev2_get_sync_message_pubkey(raw_message_buffer: ByteBuffer?): String?

        @JvmStatic
        external fun tox_messagev2_get_sync_message_type(raw_message_buffer: ByteBuffer?): Long

        @JvmStatic
        external fun tox_util_friend_send_msg_receipt_v2(
            friend_number: Long,
            ts_sec: Long,
            msgid_buffer: ByteBuffer?
        ): Int

        @JvmStatic
        external fun tox_util_friend_send_message_v2(
            friend_number: Long,
            type: Int,
            ts_sec: Long,
            message: String?,
            length: Long,
            raw_message_back_buffer: ByteBuffer?,
            raw_message_back_buffer_length: ByteBuffer?,
            msgid_back_buffer: ByteBuffer?
        ): Long

        @JvmStatic
        external fun tox_util_friend_resend_message_v2(
            friend_number: Long,
            raw_message_buffer: ByteBuffer?,
            raw_msg_len: Long
        ): Int

        // --------------- Message V2 -------------
        // --------------- Message V2 -------------
        // --------------- Message V2 -------------
        // --------------- Message V3 -------------
        // --------------- Message V3 -------------
        // --------------- Message V3 -------------
        @JvmStatic
        external fun tox_messagev3_get_new_message_id(hash_buffer: ByteBuffer?): Int

        @JvmStatic
        external fun tox_messagev3_friend_send_message(
            friendnum: Long,
            a_TOX_MESSAGE_TYPE: Int,
            message: String?,
            mag_hash: ByteBuffer?,
            timestamp: Long
        ): Long

        // --------------- Message V3 -------------
        // --------------- Message V3 -------------
        // --------------- Message V3 -------------
        // --------------- Conference -------------
        // --------------- Conference -------------
        // --------------- Conference -------------
        @JvmStatic
        external fun tox_conference_join(friend_number: Long, cookie_buffer: ByteBuffer?, cookie_length: Long): Long

        @JvmStatic
        external fun tox_conference_peer_count(conference_number: Long): Long

        @JvmStatic
        external fun tox_conference_peer_get_name_size(conference_number: Long, peer_number: Long): Long

        @JvmStatic
        external fun tox_conference_peer_get_name(conference_number: Long, peer_number: Long): String?

        @JvmStatic
        external fun tox_conference_peer_get_public_key(conference_number: Long, peer_number: Long): String?

        @JvmStatic
        external fun tox_conference_offline_peer_count(conference_number: Long): Long

        @JvmStatic
        external fun tox_conference_offline_peer_get_name_size(conference_number: Long, offline_peer_number: Long): Long

        @JvmStatic
        external fun tox_conference_offline_peer_get_name(conference_number: Long, offline_peer_number: Long): String?

        @JvmStatic
        external fun tox_conference_offline_peer_get_public_key(
            conference_number: Long,
            offline_peer_number: Long
        ): String?

        @JvmStatic
        external fun tox_conference_offline_peer_get_last_active(
            conference_number: Long,
            offline_peer_number: Long
        ): Long

        @JvmStatic
        external fun tox_conference_peer_number_is_ours(conference_number: Long, peer_number: Long): Int

        @JvmStatic
        external fun tox_conference_get_title_size(conference_number: Long): Long

        @JvmStatic
        external fun tox_conference_get_title(conference_number: Long): String?

        @JvmStatic
        external fun tox_conference_get_type(conference_number: Long): Int

        @JvmStatic
        external fun tox_conference_send_message(
            conference_number: Long,
            a_TOX_MESSAGE_TYPE: Int,
            message: String?
        ): Int

        @JvmStatic
        external fun tox_conference_delete(conference_number: Long): Int

        @JvmStatic
        external fun tox_conference_get_chatlist_size(): Long

        @JvmStatic
        external fun tox_conference_get_chatlist(): LongArray?

        @JvmStatic
        external fun tox_conference_get_id(conference_number: Long, cookie_buffer: ByteBuffer?): Int

        @JvmStatic
        external fun tox_conference_new(): Int

        @JvmStatic
        external fun tox_conference_invite(friend_number: Long, conference_number: Long): Int

        @JvmStatic
        external fun tox_conference_set_title(conference_number: Long, title: String?): Int
        // --------------- Conference -------------
        // --------------- Conference -------------
        // --------------- Conference -------------
        // --------------- new Groups -------------
        // --------------- new Groups -------------
        // --------------- new Groups -------------
        /**
         * Creates a new group chat.
         *
         *
         * This function creates a new group chat object and adds it to the chats array.
         *
         *
         * The caller of this function has Founder role privileges.
         *
         *
         * The client should initiate its peer list with self info after calling this function, as
         * the peer_join callback will not be triggered.
         *
         * @param a_TOX_GROUP_PRIVACY_STATE The privacy state of the group. If this is set to TOX_GROUP_PRIVACY_STATE_PUBLIC,
         * the group will attempt to announce itself to the DHT and anyone with the Chat ID may join.
         * Otherwise a friend invite will be required to join the group.
         * @param group_name                The name of the group. The name must be non-NULL.
         * @param my_peer_name              The name of the peer creating the group.
         * @return group_number on success, UINT32_MAX on failure.
         */
        @JvmStatic
        external fun tox_group_new(a_TOX_GROUP_PRIVACY_STATE: Int, group_name: String?, my_peer_name: String?): Long

        /**
         * Joins a group chat with specified Chat ID.
         *
         *
         * This function creates a new group chat object, adds it to the chats array, and sends
         * a DHT announcement to find peers in the group associated with chat_id. Once a peer has been
         * found a join attempt will be initiated.
         *
         * @param chat_id_buffer The Chat ID of the group you wish to join. This must be TOX_GROUP_CHAT_ID_SIZE bytes.
         * @param password       The password required to join the group. Set to NULL if no password is required.
         * @param my_peer_name   The name of the peer joining the group.
         * @return group_number on success, UINT32_MAX on failure.
         */
        @JvmStatic
        external fun tox_group_join(
            chat_id_buffer: ByteBuffer?,
            chat_id_length: Long,
            my_peer_name: String?,
            password: String?
        ): Long

        @JvmStatic
        external fun tox_group_leave(group_number: Long, part_message: String?): Int

        @JvmStatic
        external fun tox_group_self_get_peer_id(group_number: Long): Long

        @JvmStatic
        external fun tox_group_self_set_name(group_number: Long, my_peer_name: String?): Int

        @JvmStatic
        external fun tox_group_self_get_public_key(group_number: Long): String?

        @JvmStatic
        external fun tox_group_self_get_role(group_number: Long): Int

        @JvmStatic
        external fun tox_group_peer_get_role(group_number: Long, peer_id: Long): Int

        @JvmStatic
        external fun tox_group_get_chat_id(group_number: Long, chat_id_buffer: ByteBuffer?): Int

        @JvmStatic
        external fun tox_group_get_number_groups(): Long

        @JvmStatic
        external fun tox_group_get_grouplist(): LongArray?

        @JvmStatic
        external fun tox_group_peer_count(group_number: Long): Long

        @JvmStatic
        external fun tox_group_get_peer_limit(group_number: Long): Int

        @JvmStatic
        external fun tox_group_founder_set_peer_limit(group_number: Long, max_peers: Int): Int

        @JvmStatic
        external fun tox_group_offline_peer_count(group_number: Long): Long

        @JvmStatic
        external fun tox_group_get_peerlist(group_number: Long): LongArray?

        @JvmStatic
        external fun tox_group_by_chat_id(chat_id_buffer: ByteBuffer?): Long

        @JvmStatic
        external fun tox_group_get_privacy_state(group_number: Long): Int

        @JvmStatic
        external fun tox_group_mod_kick_peer(group_number: Long, peer_id: Long): Int

        @JvmStatic
        external fun tox_group_mod_set_role(group_number: Long, peer_id: Long, a_Tox_Group_Role: Int): Int

        @JvmStatic
        external fun tox_group_peer_get_public_key(group_number: Long, peer_id: Long): String?

        @JvmStatic
        external fun tox_group_peer_by_public_key(group_number: Long, peer_public_key_string: String?): Long

        @JvmStatic
        external fun tox_group_peer_get_name(group_number: Long, peer_id: Long): String?

        @JvmStatic
        external fun tox_group_get_name(group_number: Long): String?

        @JvmStatic
        external fun tox_group_get_topic(group_number: Long): String?

        @JvmStatic
        external fun tox_group_peer_get_connection_status(group_number: Long, peer_id: Long): Int

        @JvmStatic
        external fun tox_group_invite_friend(group_number: Long, friend_number: Long): Int

        @JvmStatic
        external fun tox_group_is_connected(group_number: Long): Int

        @JvmStatic
        external fun tox_group_reconnect(group_number: Long): Int

        @JvmStatic
        external fun tox_group_send_custom_packet(
            group_number: Long,
            lossless: Int,
            data: ByteArray?,
            data_length: Int
        ): Int

        @JvmStatic
        external fun tox_group_send_custom_private_packet(
            group_number: Long,
            peer_id: Long,
            lossless: Int,
            data: ByteArray?,
            data_length: Int
        ): Int

        /**
         * Send a text chat message to the group.
         *
         *
         * This function creates a group message packet and pushes it into the send
         * queue.
         *
         *
         * The message length may not exceed TOX_MAX_MESSAGE_LENGTH. Larger messages
         * must be split by the client and sent as separate messages. Other clients can
         * then reassemble the fragments. Messages may not be empty.
         *
         * @param group_number       The group number of the group the message is intended for.
         * @param a_TOX_MESSAGE_TYPE Message type (normal, action, ...).
         * @param message            A non-NULL pointer to the first element of a byte array
         * containing the message text.
         * @return message_id on success. return < 0 on error.
         */
        @JvmStatic
        external fun tox_group_send_message(group_number: Long, a_TOX_MESSAGE_TYPE: Int, message: String?): Long

        /**
         * Send a text chat message to the specified peer in the specified group.
         *
         *
         * This function creates a group private message packet and pushes it into the send
         * queue.
         *
         *
         * The message length may not exceed TOX_MAX_MESSAGE_LENGTH. Larger messages
         * must be split by the client and sent as separate messages. Other clients can
         * then reassemble the fragments. Messages may not be empty.
         *
         * @param group_number The group number of the group the message is intended for.
         * @param peer_id      The ID of the peer the message is intended for.
         * @param message      A non-NULL pointer to the first element of a byte array
         * containing the message text.
         * @return true on success.
         */
        @JvmStatic
        external fun tox_group_send_private_message(
            group_number: Long,
            peer_id: Long,
            a_TOX_MESSAGE_TYPE: Int,
            message: String?
        ): Int

        /**
         * Send a text chat message to the specified peer in the specified group.
         *
         *
         * This function creates a group private message packet and pushes it into the send
         * queue.
         *
         *
         * The message length may not exceed TOX_MAX_MESSAGE_LENGTH. Larger messages
         * must be split by the client and sent as separate messages. Other clients can
         * then reassemble the fragments. Messages may not be empty.
         *
         * @param group_number           The group number of the group the message is intended for.
         * @param peer_public_key_string A memory region of at least TOX_PUBLIC_KEY_SIZE bytes of the peer the
         * message is intended for. If this parameter is NULL, this function will return false.
         * @param message                A non-NULL pointer to the first element of a byte array
         * containing the message text.
         * @return 0 on success. return < 0 on error.
         */
        @JvmStatic
        external fun tox_group_send_private_message_by_peerpubkey(
            group_number: Long,
            peer_public_key_string: String?,
            a_TOX_MESSAGE_TYPE: Int,
            message: String?
        ): Int

        /**
         * Accept an invite to a group chat that the client previously received from a friend. The invite
         * is only valid while the inviter is present in the group.
         *
         * @param invite_data_buffer The invite data received from the `group_invite` event.
         * @param my_peer_name       The name of the peer joining the group.
         * @param password           The password required to join the group. Set to NULL if no password is required.
         * @return the group_number on success, UINT32_MAX on failure.
         */
        @JvmStatic
        external fun tox_group_invite_accept(
            friend_number: Long,
            invite_data_buffer: ByteBuffer?,
            invite_data_length: Long,
            my_peer_name: String?,
            password: String?
        ): Long

        // --------------- new Groups -------------
        // --------------- new Groups -------------
        // --------------- new Groups -------------
        // --------------- AV -------------
        // --------------- AV -------------
        // --------------- AV -------------
        @JvmStatic
        external fun toxav_answer(friendnum: Long, audio_bit_rate: Long, video_bit_rate: Long): Int

        @JvmStatic
        external fun toxav_iteration_interval(): Long

        @JvmStatic
        external fun toxav_call(friendnum: Long, audio_bit_rate: Long, video_bit_rate: Long): Int

        @JvmStatic
        external fun toxav_bit_rate_set(friendnum: Long, audio_bit_rate: Long, video_bit_rate: Long): Int

        @JvmStatic
        external fun toxav_call_control(friendnum: Long, a_TOXAV_CALL_CONTROL: Int): Int

        @JvmStatic
        external fun toxav_video_send_frame_uv_reversed(friendnum: Long, frame_width_px: Int, frame_height_px: Int): Int

        @JvmStatic
        external fun toxav_video_send_frame(friendnum: Long, frame_width_px: Int, frame_height_px: Int): Int

        @JvmStatic
        external fun toxav_video_send_frame_age(
            friendnum: Long,
            frame_width_px: Int,
            frame_height_px: Int,
            age_ms: Int
        ): Int

        @JvmStatic
        external fun toxav_video_send_frame_h264(
            friendnum: Long,
            frame_width_px: Int,
            frame_height_px: Int,
            data_len: Long
        ): Int

        @JvmStatic
        external fun toxav_video_send_frame_h264_age(
            friendnum: Long,
            frame_width_px: Int,
            frame_height_px: Int,
            data_len: Long,
            age_ms: Int
        ): Int

        @JvmStatic
        external fun toxav_option_set(friendnum: Long, a_TOXAV_OPTIONS_OPTION: Long, value: Long): Int

        @JvmStatic
        external fun set_av_call_status(status: Int)

        @JvmStatic
        external fun set_audio_play_volume_percent(volume_percent: Int)

        // buffer is for incoming video (call)
        @JvmStatic
        external fun set_JNI_video_buffer(buffer: ByteBuffer?, frame_width_px: Int, frame_height_px: Int): Long

        // buffer2 is for sending video (call)
        @JvmStatic
        external fun set_JNI_video_buffer2(buffer2: ByteBuffer?, frame_width_px: Int, frame_height_px: Int)

        // audio_buffer is for sending audio (group and call)
        @JvmStatic
        external fun set_JNI_audio_buffer(audio_buffer: ByteBuffer?)

        // audio_buffer2 is for incoming audio (group and call)
        @JvmStatic
        external fun set_JNI_audio_buffer2(audio_buffer2: ByteBuffer?)

        /**
         * Send an audio frame to a friend.
         *
         *
         * The expected format of the PCM data is: [s1c1][s1c2][...][s2c1][s2c2][...]...
         * Meaning: sample 1 for channel 1, sample 1 for channel 2, ...
         * For mono audio, this has no meaning, every sample is subsequent. For stereo,
         * this means the expected format is LRLRLR... with samples for left and right
         * alternating.
         *
         * @param friend_number The friend number of the friend to which to send an
         * audio frame.
         * @param sample_count  Number of samples in this frame. Valid numbers here are
         * ((sample rate) * (audio length) / 1000), where audio length can be
         * 2.5, 5, 10, 20, 40 or 60 millseconds.
         * @param channels      Number of audio channels. Supported values are 1 and 2.
         * @param sampling_rate Audio sampling rate used in this frame. Valid sampling
         * rates are 8000, 12000, 16000, 24000, or 48000.
         */
        @JvmStatic
        external fun toxav_audio_send_frame(
            friend_number: Long,
            sample_count: Long,
            channels: Int,
            sampling_rate: Long
        ): Int

        // --------------- AV -------------
        // --------------- AV -------------
        // --------------- AV -------------
        // -------- native methods --------
        // -------- native methods --------
        // -------- native methods --------
        // -------- called by AV native methods --------
        // -------- called by AV native methods --------
        // -------- called by AV native methods --------
        @JvmStatic
        fun android_toxav_callback_call_cb_method(friend_number: Long, audio_enabled: Int, video_enabled: Int) {
        }

        @JvmStatic
        fun android_toxav_callback_video_receive_frame_cb_method(
            friend_number: Long,
            frame_width_px: Long,
            frame_height_px: Long,
            ystride: Long,
            ustride: Long,
            vstride: Long
        ) {
        }

        @JvmStatic
        fun android_toxav_callback_call_state_cb_method(friend_number: Long, a_TOXAV_FRIEND_CALL_STATE: Int) {
        }

        @JvmStatic
        fun android_toxav_callback_bit_rate_status_cb_method(
            friend_number: Long,
            audio_bit_rate: Long,
            video_bit_rate: Long
        ) {
        }

        @JvmStatic
        fun android_toxav_callback_audio_receive_frame_cb_method(
            friend_number: Long,
            sample_count: Long,
            channels: Int,
            sampling_rate: Long
        ) {
        }

        @JvmStatic
        fun android_toxav_callback_audio_receive_frame_pts_cb_method(
            friend_number: Long,
            sample_count: Long,
            channels: Int,
            sampling_rate: Long,
            pts: Long
        ) {
        }

        @JvmStatic
        fun android_toxav_callback_video_receive_frame_pts_cb_method(
            friend_number: Long,
            frame_width_px: Long,
            frame_height_px: Long,
            ystride: Long,
            ustride: Long,
            vstride: Long,
            pts: Long
        ) {
        }

        @JvmStatic
        fun android_toxav_callback_video_receive_frame_h264_cb_method(friend_number: Long, buf_size: Long) {
        }

        @JvmStatic
        fun android_toxav_callback_group_audio_receive_frame_cb_method(
            conference_number: Long,
            peer_number: Long,
            sample_count: Long,
            channels: Int,
            sampling_rate: Long
        ) {
        }

        @JvmStatic
        fun android_toxav_callback_call_comm_cb_method(
            friend_number: Long,
            a_TOXAV_CALL_COMM_INFO: Long,
            comm_number: Long
        ) {
        }

        // -------- called by AV native methods --------
        // -------- called by AV native methods --------
        // -------- called by AV native methods --------
        // -------- called by native methods --------
        // -------- called by native methods --------
        // -------- called by native methods --------
        @JvmStatic
        fun android_tox_callback_self_connection_status_cb_method(a_TOX_CONNECTION: Int) {
            Log.i(TAG, "android_tox_callback_self_connection_status_cb_method: " + a_TOX_CONNECTION)
            update_savedata_file_wrapper()
            if (a_TOX_CONNECTION == ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP.value) {
                set_tox_online_state("tcp")
            } else if (a_TOX_CONNECTION == ToxVars.TOX_CONNECTION.TOX_CONNECTION_UDP.value) {
                set_tox_online_state("udp")
            } else {
                set_tox_online_state("offline")
            }
        }

        @JvmStatic
        fun android_tox_callback_friend_name_cb_method(friend_number: Long, friend_name: String?, length: Long) {
            try {
                contactstore.update(
                    item = ContactItem(
                        name = friend_name!!,
                        isConnected = tox_friend_get_connection_status(friend_number),
                        pubkey = tox_friend_get_public_key(friend_number)!!
                    )
                )
            } catch (_: Exception) {
            }
        }

        @JvmStatic
        fun android_tox_callback_friend_status_message_cb_method(
            friend_number: Long,
            status_message: String?,
            length: Long
        ) {
            try {
                var fname = tox_friend_get_name(friend_number)
                if (fname == null) {
                    fname = "Friend"
                }
                contactstore.update(
                    item = ContactItem(
                        name = fname,
                        isConnected = tox_friend_get_connection_status(friend_number),
                        pubkey = tox_friend_get_public_key(friend_number)!!
                    )
                )
            } catch (_: Exception) {
            }
        }

        @JvmStatic
        fun android_tox_callback_friend_lossless_packet_cb_method(
            friend_number: Long,
            data: ByteArray?,
            length: Long
        ) {
        }

        @JvmStatic
        fun android_tox_callback_friend_status_cb_method(friend_number: Long, a_TOX_USER_STATUS: Int) {
        }

        @JvmStatic
        fun android_tox_callback_friend_connection_status_cb_method(friend_number: Long, a_TOX_CONNECTION: Int) {
            Log.i(
                TAG,
                "android_tox_callback_friend_connection_status_cb_method: fn=" + friend_number + " " + a_TOX_CONNECTION
            )
            update_savedata_file_wrapper()
            try {
                var fname = tox_friend_get_name(friend_number)
                if (fname == null) {
                    fname = "Friend"
                }
                contactstore.update(
                    item = ContactItem(
                        name = fname,
                        isConnected = tox_friend_get_connection_status(friend_number),
                        pubkey = tox_friend_get_public_key(friend_number)!!
                    )
                )
            } catch (_: Exception) {
            }
        }

        @JvmStatic
        fun android_tox_callback_friend_typing_cb_method(friend_number: Long, typing: Int) {
        }

        @JvmStatic
        fun android_tox_callback_friend_read_receipt_cb_method(friend_number: Long, message_id: Long) {
        }

        @JvmStatic
        fun android_tox_callback_friend_request_cb_method(
            friend_public_key: String?,
            friend_request_message: String?,
            length: Long
        ) {
            Log.i(TAG, "android_tox_callback_friend_request_cb_method: friend_public_key=" + friend_public_key)
            tox_friend_add_norequest(friend_public_key)
            update_savedata_file_wrapper()
        }

        @JvmStatic
        fun android_tox_callback_friend_message_cb_method(
            friend_number: Long,
            message_type: Int,
            friend_message: String?,
            length: Long,
            msgV3hash_bin: ByteArray?,
            message_timestamp: Long
        ) {
            Log.i(
                TAG,
                "android_tox_callback_friend_message_cb_method: fn=" + friend_number + " friend_message=" + friend_message
            )

            var msgV3hash_hex_string: String? = null
            if (msgV3hash_bin != null) {
                msgV3hash_hex_string = bytesToHex(msgV3hash_bin, 0, msgV3hash_bin.size)
            }

            if (message_type == ToxVars.TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK.value) {
                return
            }

            if (msgV3hash_hex_string != null) {
                HelperMessage.send_msgv3_high_level_ack(friend_number, msgV3hash_hex_string);
                try {
                    // ("msgv3:"+friend_message)
                    val toxpk = tox_friend_get_public_key(friend_number)
                    val friend_user = User("Friend " + friend_number, picture = "friend_avatar.png", toxpk = toxpk)
                    store.send(
                        Action.SendMessage(
                            message = Message(
                                user = friend_user,
                                timeMs = timestampMs(),
                                text = friend_message!!,
                                toxpk = toxpk
                            )
                        )
                    )
                } catch (_: Exception) {
                }
            } else {
                try {
                    // ("msgv1:"+friend_message)
                    val toxpk = tox_friend_get_public_key(friend_number)
                    val friend_user = User("Friend " + friend_number, picture = "friend_avatar.png", toxpk = toxpk)
                    store.send(
                        Action.SendMessage(
                            message = Message(
                                user = friend_user,
                                timeMs = timestampMs(),
                                text = friend_message!!,
                                toxpk = toxpk
                            )
                        )
                    )
                } catch (_: Exception) {
                }
            }
        }

        @JvmStatic
        fun android_tox_callback_friend_message_v2_cb_method(
            friend_number: Long,
            friend_message: String?,
            length: Long,
            ts_sec: Long,
            ts_ms: Long,
            raw_message: ByteArray?,
            raw_message_length: Long
        ) {
            Log.i(
                TAG,
                "android_tox_callback_friend_message_v2_cb_method: fn=" + friend_number + " friend_message=" + friend_message
            )

            val msg_type = 1
            val raw_message_buf = ByteBuffer.allocateDirect(raw_message_length.toInt())
            raw_message_buf.put(raw_message, 0, raw_message_length.toInt())
            val msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH)
            tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer)
            val ts_sec = tox_messagev2_get_ts_sec(raw_message_buf)
            val ts_ms = tox_messagev2_get_ts_ms(raw_message_buf)

            val msg_id_buffer_compat = ByteBufferCompat(msg_id_buffer)
            val msg_id_as_hex_string: String? = msg_id_buffer_compat.array()?.let {
                bytesToHex(
                    it, msg_id_buffer_compat.arrayOffset(),
                    msg_id_buffer_compat.limit()
                )
            }
            Log.i(TAG, "TOX_FILE_KIND_MESSAGEV2_SEND:MSGv2HASH:2=" + msg_id_as_hex_string);

            try {
                val toxpk = tox_friend_get_public_key(friend_number)
                val friend_user = User("Friend " + friend_number, picture = "friend_avatar.png", toxpk = toxpk)
                store.send(
                    Action.SendMessage(
                        message = Message(
                            user = friend_user,
                            timeMs = timestampMs(),
                            text = friend_message!!,
                            toxpk = toxpk
                        )
                    )
                )
                // incoming_messages_queue.offer(friend_message) // ("msgv2:"+friend_message)
            } catch (_: Exception) {
            }

            val pin_timestamp = System.currentTimeMillis()
            send_friend_msg_receipt_v2_wrapper(friend_number, msg_type, msg_id_buffer, (pin_timestamp / 1000));
        }

        @JvmStatic
        fun android_tox_callback_friend_sync_message_v2_cb_method(
            friend_number: Long,
            ts_sec: Long,
            ts_ms: Long,
            raw_message: ByteArray?,
            raw_message_length: Long,
            raw_data: ByteArray?,
            raw_data_length: Long
        ) {
        }

        @JvmStatic
        fun sync_messagev2_send(
            friend_number: Long,
            raw_data: ByteArray?,
            raw_data_length: Long,
            raw_message_buf_wrapped: ByteBuffer?,
            msg_id_buffer: ByteBuffer?,
            real_sender_as_hex_string: String?
        ) {
        }

        @JvmStatic
        fun android_tox_callback_friend_read_receipt_message_v2_cb_method(
            friend_number: Long,
            ts_sec: Long,
            msg_id: ByteArray?
        ) {
            val msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH)
            msg_id_buffer.put(msg_id, 0, TOX_HASH_LENGTH)
            val msg_id_buffer_compat = ByteBufferCompat(msg_id_buffer)

            val message_id_hash_as_hex_string = bytesToHex(
                msg_id_buffer_compat.array()!!,
                msg_id_buffer_compat.arrayOffset(),
                msg_id_buffer_compat.limit()
            )
            Log.i(TAG, "receipt_message_v2_cb:MSGv2HASH:2=" + message_id_hash_as_hex_string);
        }

        @JvmStatic
        fun android_tox_callback_file_recv_control_cb_method(
            friend_number: Long,
            file_number: Long,
            a_TOX_FILE_CONTROL: Int
        ) {
        }

        @JvmStatic
        fun android_tox_callback_file_chunk_request_cb_method(
            friend_number: Long,
            file_number: Long,
            position: Long,
            length: Long
        ) {
        }

        @JvmStatic
        fun android_tox_callback_file_recv_cb_method(
            friend_number: Long,
            file_number: Long,
            a_TOX_FILE_KIND: Int,
            file_size: Long,
            filename: String?,
            filename_length: Long
        ) {
        }

        @JvmStatic
        fun android_tox_callback_file_recv_chunk_cb_method(
            friend_number: Long,
            file_number: Long,
            position: Long,
            data: ByteArray?,
            length: Long
        ) {
        }

        @JvmStatic
        fun android_tox_log_cb_method(
            a_TOX_LOG_LEVEL: Int,
            file: String?,
            line: Long,
            function: String?,
            message: String?
        ) {
            if (CTOXCORE_NATIVE_LOGGING) {
                Log.i(
                    TAG,
                    "C-TOXCORE:" + ToxVars.TOX_LOG_LEVEL.value_str(a_TOX_LOG_LEVEL) + ":file=" + file + ":linenum=" +
                            line + ":func=" + function + ":msg=" + message
                );
            }
        }

        // -------- called by native methods --------
        // -------- called by native methods --------
        // -------- called by native methods --------
        // -------- called by native Conference methods --------
        // -------- called by native Conference methods --------
        // -------- called by native Conference methods --------
        @JvmStatic
        fun android_tox_callback_conference_invite_cb_method(
            friend_number: Long,
            a_TOX_CONFERENCE_TYPE: Int,
            cookie_buffer: ByteArray?,
            cookie_length: Long
        ) {
        }

        @JvmStatic
        fun android_tox_callback_conference_connected_cb_method(conference_number: Long) {
        }

        @JvmStatic
        fun android_tox_callback_conference_message_cb_method(
            conference_number: Long,
            peer_number: Long,
            a_TOX_MESSAGE_TYPE: Int,
            message_orig: String?,
            length: Long
        ) {
        }

        @JvmStatic
        fun android_tox_callback_conference_title_cb_method(
            conference_number: Long,
            peer_number: Long,
            title: String?,
            title_length: Long
        ) {
        }

        @JvmStatic
        fun android_tox_callback_conference_peer_name_cb_method(
            conference_number: Long,
            peer_number: Long,
            name: String?,
            name_length: Long
        ) {
        }

        @JvmStatic
        fun android_tox_callback_conference_peer_list_changed_cb_method(conference_number: Long) {
        }

        @JvmStatic
        fun android_tox_callback_conference_namelist_change_cb_method(
            conference_number: Long,
            peer_number: Long,
            a_TOX_CONFERENCE_STATE_CHANGE: Int
        ) {
        }

        // -------- called by native Conference methods --------
        // -------- called by native Conference methods --------
        // -------- called by native Conference methods --------
        // -------- called by native new Group methods --------
        // -------- called by native new Group methods --------
        // -------- called by native new Group methods --------
        @JvmStatic
        fun android_tox_callback_group_message_cb_method(
            group_number: Long,
            peer_id: Long,
            a_TOX_MESSAGE_TYPE: Int,
            message_orig: String?,
            length: Long,
            message_id: Long
        ) {
        }

        @JvmStatic
        fun android_tox_callback_group_private_message_cb_method(
            group_number: Long,
            peer_id: Long,
            a_TOX_MESSAGE_TYPE: Int,
            message_orig: String?,
            length: Long
        ) {
        }

        @JvmStatic
        fun android_tox_callback_group_privacy_state_cb_method(group_number: Long, a_TOX_GROUP_PRIVACY_STATE: Int) {
        }

        @JvmStatic
        fun android_tox_callback_group_invite_cb_method(
            friend_number: Long,
            invite_data: ByteArray?,
            invite_data_length: Long,
            group_name: String?
        ) {
        }

        @JvmStatic
        fun android_tox_callback_group_peer_join_cb_method(group_number: Long, peer_id: Long) {
        }

        @JvmStatic
        fun android_tox_callback_group_peer_exit_cb_method(
            group_number: Long,
            peer_id: Long,
            a_Tox_Group_Exit_Type: Int
        ) {
        }

        @JvmStatic
        fun android_tox_callback_group_peer_name_cb_method(group_number: Long, peer_id: Long) {
        }

        @JvmStatic
        fun android_tox_callback_group_join_fail_cb_method(group_number: Long, a_Tox_Group_Join_Fail: Int) {
        }

        @JvmStatic
        fun android_tox_callback_group_self_join_cb_method(group_number: Long) {
        }

        @JvmStatic
        fun android_tox_callback_group_moderation_cb_method(
            group_number: Long,
            source_peer_id: Long,
            target_peer_id: Long,
            a_Tox_Group_Mod_Event: Int
        ) {
        }

        @JvmStatic
        fun android_tox_callback_group_connection_status_cb_method(
            group_number: Long,
            a_TOX_GROUP_CONNECTION_STATUS: Int
        ) {
        }

        @JvmStatic
        fun android_tox_callback_group_topic_cb_method(
            group_number: Long,
            peer_id: Long,
            topic: String?,
            topic_length: Long
        ) {
        }

        @JvmStatic
        fun android_tox_callback_group_custom_packet_cb_method(
            group_number: Long,
            peer_id: Long,
            data: ByteArray?,
            length: Long
        ) {
        }

        @JvmStatic
        fun android_tox_callback_group_custom_private_packet_cb_method(
            group_number: Long,
            peer_id: Long,
            data: ByteArray?,
            length: Long
        ) {
        }

        @JvmStatic
        fun bootstrap_single_wrapper(ip: String, port: Int, key_hex: String): Int {
            return bootstrap_single(ip, key_hex, port.toLong())
        }

        @JvmStatic
        fun add_tcp_relay_single_wrapper(ip: String, port: Int, key_hex: String): Int {
            return add_tcp_relay_single(ip, key_hex, port.toLong())
        }
    }
}

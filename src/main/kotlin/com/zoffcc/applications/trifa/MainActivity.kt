package com.zoffcc.applications.trifa

import ColorProvider
import GroupMessageAction
import MessageAction
import UIGroupMessage
import UIMessage
import User
import com.zoffcc.applications.sorm.FileDB
import com.zoffcc.applications.sorm.Filetransfer
import com.zoffcc.applications.sorm.GroupMessage
import com.zoffcc.applications.sorm.Message
import com.zoffcc.applications.trifa.HelperFiletransfer.check_auto_accept_incoming_filetransfer
import com.zoffcc.applications.trifa.HelperFiletransfer.get_incoming_filetransfer_local_filename
import com.zoffcc.applications.trifa.HelperFiletransfer.move_tmp_file_to_real_file
import com.zoffcc.applications.trifa.HelperFiletransfer.set_message_accepted_from_id
import com.zoffcc.applications.trifa.HelperFiletransfer.update_filetransfer_db_full
import com.zoffcc.applications.trifa.HelperFriend.send_friend_msg_receipt_v2_wrapper
import com.zoffcc.applications.trifa.HelperGeneric.PubkeyShort
import com.zoffcc.applications.trifa.HelperGeneric.bytesToHex
import com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper
import com.zoffcc.applications.trifa.HelperGroup.fourbytes_of_long_to_hex
import com.zoffcc.applications.trifa.HelperGroup.handle_incoming_group_file
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupnum__wrapper
import com.zoffcc.applications.trifa.HelperMessage.update_single_message_from_ftid
import com.zoffcc.applications.trifa.HelperMessage.update_single_message_from_messge_id
import com.zoffcc.applications.trifa.TRIFAGlobals.AVATAR_INCOMING_MAX_BYTE_SIZE
import com.zoffcc.applications.trifa.TRIFAGlobals.GROUP_ID_LENGTH
import com.zoffcc.applications.trifa.TRIFAGlobals.LOWER_NGC_VIDEO_BITRATE
import com.zoffcc.applications.trifa.TRIFAGlobals.LOWER_NGC_VIDEO_QUANTIZER
import com.zoffcc.applications.trifa.TRIFAGlobals.NGC_AUDIO_BITRATE
import com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE
import com.zoffcc.applications.trifa.TRIFAGlobals.UINT32_MAX_JAVA
import com.zoffcc.applications.trifa.TRIFAGlobals.UPDATE_MESSAGE_PROGRESS_AFTER_BYTES
import com.zoffcc.applications.trifa.TRIFAGlobals.UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES
import com.zoffcc.applications.trifa.TRIFAGlobals.UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES
import com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR
import com.zoffcc.applications.trifa.TRIFAGlobals.VFS_TMP_FILE_DIR
import com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_outgoung_ft_ts
import com.zoffcc.applications.trifa.ToxVars.TOX_FILE_ID_LENGTH
import com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH
import com.zoffcc.applications.trifa.ToxVars.TOX_MAX_NGC_FILE_AND_HEADER_SIZE
import com.zoffcc.applications.trifa.TrifaToxService.Companion.orma
import contactstore
import groupmessagestore
import groupstore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lock_data_dir_input
import messagestore
import org.briarproject.briar.desktop.contact.ContactItem
import org.briarproject.briar.desktop.contact.GroupItem
import set_tox_online_state
import timestampMs
import toxdatastore
import java.io.File
import java.io.PrintWriter
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore

@Suppress("UNUSED_PARAMETER")
class MainActivity
{
    val nativeLibAPI: String?
        external get

    companion object
    {
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
        var PREF__ngc_video_max_quantizer: Int = LOWER_NGC_VIDEO_QUANTIZER // 47 -> default, 51 -> lowest quality, 30 -> very high quality and lots of bandwidth!
        var PREF__ngc_audio_bitrate: Int = NGC_AUDIO_BITRATE
        var PREF__ngc_audio_samplerate = 48000
        var PREF__ngc_audio_channels = 1
        @JvmField var PREF__tox_savefile_dir = "."
        @JvmField public var PREF__auto_accept_image = true
        @JvmField var PREF__auto_accept_video = true
        @JvmField var PREF__auto_accept_all_upto = true

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
        fun main_init()
        {
            println("Version:" + Version)
            try
            {
                println("java.vm.name:" + System.getProperty("java.vm.name"))
                println("java.home:" + System.getProperty("java.home"))
                println("java.vendor:" + System.getProperty("java.vendor"))
                println("java.version:" + System.getProperty("java.version"))
                println("java.specification.vendor:" + System.getProperty("java.specification.vendor"))
            } catch (e: Exception)
            {
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
            try
            {
                Thread.currentThread().name = "t_main"
            } catch (e: Exception)
            {
            }
            Log.i(TAG, "java.library.path:" + System.getProperty("java.library.path"))
            Log.i(TAG, "loaded:c-toxcore:v" + tox_version_major() + "." + tox_version_minor() + "." + tox_version_patch())
            Log.i(TAG, "loaded:jni-c-toxcore:v" + jnictoxcore_version())

            Log.i(TAG, "tox_service_fg:" + tox_service_fg)
            Log.i(TAG, "native_lib_loaded:" + native_lib_loaded)
            Log.i(TAG, "MainActivity:" + this)

            tox_service_fg = TrifaToxService()

            lock_data_dir_input()

            if (!TrifaToxService.TOX_SERVICE_STARTED)
            {
                var ORBOT_PROXY_HOST = ""
                var ORBOT_PROXY_PORT: Long = 0
                tox_savefile_directory = PREF__tox_savefile_dir + File.separator
                Log.i(TAG, "init:PREF__udp_enabled=$PREF__udp_enabled")
                init(tox_savefile_directory, PREF__udp_enabled, PREF__local_discovery_enabled, PREF__orbot_enabled_to_int, ORBOT_PROXY_HOST, ORBOT_PROXY_PORT, password_hash, PREF__ipv6_enabled, PREF__force_udp_only, PREF__ngc_video_bitrate, PREF__ngc_video_max_quantizer, PREF__ngc_audio_bitrate, PREF__ngc_audio_samplerate, PREF__ngc_audio_channels)
                tox_service_fg!!.tox_thread_start_fg()
            }
            val my_tox_id_temp = get_my_toxid()
            Log.i(TAG, "MyToxID:$my_tox_id_temp")
            try
            {
                Thread.currentThread().name = "t_main"
            } catch (_: Exception)
            {
            }

            try
            {
                toxdatastore.updateToxID(my_tox_id_temp)
            } catch (_: Exception)
            {
            }

            try
            {
                PrintWriter(PREF__tox_savefile_dir + File.separator + "toxid.txt", "UTF-8").use { out -> out.write(my_tox_id_temp) }
                Log.i(TAG, "writing toxid to: " + File(PREF__tox_savefile_dir).canonicalPath + File.separator + "toxid.txt")
            } catch (_: Exception)
            {
            }
        }

        init
        {
            if (!native_lib_loaded)
            {
                val resourcesDir = File(System.getProperty("compose.application.resources.dir"))
                System.out.println("XXXXX1:" + resourcesDir)
                System.out.println("XXXXX1.1:OS:" + OperatingSystem.getCurrent())
                System.out.println("XXXXX1.2:OS:" + OperatingSystem.getName())
                System.out.println("XXXXX1.3:OS:" + OperatingSystem.getArchitecture())
                var libFile = File(resourcesDir, "libjni-c-toxcore.so")
                if (OperatingSystem.getCurrent() == OperatingSystem.LINUX)
                {
                    libFile = File(resourcesDir, "libjni-c-toxcore.so")
                } else if (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS)
                {
                    libFile = File(resourcesDir, "jni-c-toxcore.dll")
                } else if (OperatingSystem.getCurrent() == OperatingSystem.MACOS)
                {
                    libFile = File(resourcesDir, "libjni-c-toxcore.jnilib")
                } else
                {
                    System.out.println("XXXXX1.1:OS:Unknown operating system:EXIT")
                    System.exit(3)
                }
                System.out.println("XXXXX2:" + libFile + " " + libFile.canonicalPath)

                try
                {
                    System.load(libFile.canonicalPath)
                    native_lib_loaded = true
                    Log.i(TAG, "successfully loaded native library")
                } catch (e: UnsatisfiedLinkError)
                {
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
        external fun init(data_dir: String?, udp_enabled: Int, local_discovery_enabled: Int, orbot_enabled: Int, orbot_host: String?, orbot_port: Long, tox_encrypt_passphrase_hash: String?, enable_ipv6: Int, force_udp_only_mode: Int, ngc_video_bitrate: Int, max_quantizer: Int, ngc_audio_bitrate: Int, ngc_audio_sampling_rate: Int, ngc_audio_channel_count: Int)
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
        external fun jni_iterate_videocall_audio(delta_new: Int, want_ms_output: Int, channels: Int, sample_rate: Int, send_emtpy_buffer: Int): Int

        @JvmStatic
        external fun crgb2yuv(rgba_buf: ByteBuffer?, yuv_buf: ByteBuffer?, w_yuv: Int, h_yuv: Int, w_rgba: Int, h_rgba: Int)

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
        external fun tox_file_send(friend_number: Long, kind: Long, file_size: Long, file_id_buffer: ByteBuffer?, file_name: String?, filename_length: Long): Long

        @JvmStatic
        external fun tox_file_send_chunk(friend_number: Long, file_number: Long, position: Long, data_buffer: ByteBuffer?, data_length: Long): Int

        // --------------- Message V2 -------------
        // --------------- Message V2 -------------
        // --------------- Message V2 -------------
        @JvmStatic
        external fun tox_messagev2_size(text_length: Long, type: Long, alter_type: Long): Long

        @JvmStatic
        external fun tox_messagev2_wrap(text_length: Long, type: Long, alter_type: Long, message_text_buffer: ByteBuffer?, ts_sec: Long, ts_ms: Long, raw_message_buffer: ByteBuffer?, msgid_buffer: ByteBuffer?): Int

        @JvmStatic
        external fun tox_messagev2_get_message_id(raw_message_buffer: ByteBuffer?, msgid_buffer: ByteBuffer?): Int

        @JvmStatic
        external fun tox_messagev2_get_ts_sec(raw_message_buffer: ByteBuffer?): Long

        @JvmStatic
        external fun tox_messagev2_get_ts_ms(raw_message_buffer: ByteBuffer?): Long

        @JvmStatic
        external fun tox_messagev2_get_message_text(raw_message_buffer: ByteBuffer?, raw_message_len: Long, is_alter_msg: Int, alter_type: Long, message_text_buffer: ByteBuffer?): Long

        @JvmStatic
        external fun tox_messagev2_get_sync_message_pubkey(raw_message_buffer: ByteBuffer?): String?

        @JvmStatic
        external fun tox_messagev2_get_sync_message_type(raw_message_buffer: ByteBuffer?): Long

        @JvmStatic
        external fun tox_util_friend_send_msg_receipt_v2(friend_number: Long, ts_sec: Long, msgid_buffer: ByteBuffer?): Int

        @JvmStatic
        external fun tox_util_friend_send_message_v2(friend_number: Long, type: Int, ts_sec: Long, message: String?, length: Long, raw_message_back_buffer: ByteBuffer?, raw_message_back_buffer_length: ByteBuffer?, msgid_back_buffer: ByteBuffer?): Long

        @JvmStatic
        external fun tox_util_friend_resend_message_v2(friend_number: Long, raw_message_buffer: ByteBuffer?, raw_msg_len: Long): Int

        // --------------- Message V2 -------------
        // --------------- Message V2 -------------
        // --------------- Message V2 -------------
        // --------------- Message V3 -------------
        // --------------- Message V3 -------------
        // --------------- Message V3 -------------
        @JvmStatic
        external fun tox_messagev3_get_new_message_id(hash_buffer: ByteBuffer?): Int

        @JvmStatic
        external fun tox_messagev3_friend_send_message(friendnum: Long, a_TOX_MESSAGE_TYPE: Int, message: String?, mag_hash: ByteBuffer?, timestamp: Long): Long

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
        external fun tox_conference_offline_peer_get_public_key(conference_number: Long, offline_peer_number: Long): String?

        @JvmStatic
        external fun tox_conference_offline_peer_get_last_active(conference_number: Long, offline_peer_number: Long): Long

        @JvmStatic
        external fun tox_conference_peer_number_is_ours(conference_number: Long, peer_number: Long): Int

        @JvmStatic
        external fun tox_conference_get_title_size(conference_number: Long): Long

        @JvmStatic
        external fun tox_conference_get_title(conference_number: Long): String?

        @JvmStatic
        external fun tox_conference_get_type(conference_number: Long): Int

        @JvmStatic
        external fun tox_conference_send_message(conference_number: Long, a_TOX_MESSAGE_TYPE: Int, message: String?): Int

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
        external fun tox_conference_set_title(conference_number: Long, title: String?): Int // --------------- Conference ------------- // --------------- Conference ------------- // --------------- Conference ------------- // --------------- new Groups ------------- // --------------- new Groups ------------- // --------------- new Groups -------------

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
        external fun tox_group_join(chat_id_buffer: ByteBuffer?, chat_id_length: Long, my_peer_name: String?, password: String?): Long

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
        external fun tox_group_send_custom_packet(group_number: Long, lossless: Int, data: ByteArray?, data_length: Int): Int

        @JvmStatic
        external fun tox_group_send_custom_private_packet(group_number: Long, peer_id: Long, lossless: Int, data: ByteArray?, data_length: Int): Int

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
        external fun tox_group_send_private_message(group_number: Long, peer_id: Long, a_TOX_MESSAGE_TYPE: Int, message: String?): Int

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
        external fun tox_group_send_private_message_by_peerpubkey(group_number: Long, peer_public_key_string: String?, a_TOX_MESSAGE_TYPE: Int, message: String?): Int

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
        external fun tox_group_invite_accept(friend_number: Long, invite_data_buffer: ByteBuffer?, invite_data_length: Long, my_peer_name: String?, password: String?): Long

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
        external fun toxav_video_send_frame_age(friendnum: Long, frame_width_px: Int, frame_height_px: Int, age_ms: Int): Int

        @JvmStatic
        external fun toxav_video_send_frame_h264(friendnum: Long, frame_width_px: Int, frame_height_px: Int, data_len: Long): Int

        @JvmStatic
        external fun toxav_video_send_frame_h264_age(friendnum: Long, frame_width_px: Int, frame_height_px: Int, data_len: Long, age_ms: Int): Int

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
        external fun toxav_audio_send_frame(friend_number: Long, sample_count: Long, channels: Int, sampling_rate: Long): Int

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
        fun android_toxav_callback_call_cb_method(friend_number: Long, audio_enabled: Int, video_enabled: Int)
        {
        }

        @JvmStatic
        fun android_toxav_callback_video_receive_frame_cb_method(friend_number: Long, frame_width_px: Long, frame_height_px: Long, ystride: Long, ustride: Long, vstride: Long)
        {
        }

        @JvmStatic
        fun android_toxav_callback_call_state_cb_method(friend_number: Long, a_TOXAV_FRIEND_CALL_STATE: Int)
        {
        }

        @JvmStatic
        fun android_toxav_callback_bit_rate_status_cb_method(friend_number: Long, audio_bit_rate: Long, video_bit_rate: Long)
        {
        }

        @JvmStatic
        fun android_toxav_callback_audio_receive_frame_cb_method(friend_number: Long, sample_count: Long, channels: Int, sampling_rate: Long)
        {
        }

        @JvmStatic
        fun android_toxav_callback_audio_receive_frame_pts_cb_method(friend_number: Long, sample_count: Long, channels: Int, sampling_rate: Long, pts: Long)
        {
        }

        @JvmStatic
        fun android_toxav_callback_video_receive_frame_pts_cb_method(friend_number: Long, frame_width_px: Long, frame_height_px: Long, ystride: Long, ustride: Long, vstride: Long, pts: Long)
        {
        }

        @JvmStatic
        fun android_toxav_callback_video_receive_frame_h264_cb_method(friend_number: Long, buf_size: Long)
        {
        }

        @JvmStatic
        fun android_toxav_callback_group_audio_receive_frame_cb_method(conference_number: Long, peer_number: Long, sample_count: Long, channels: Int, sampling_rate: Long)
        {
        }

        @JvmStatic
        fun android_toxav_callback_call_comm_cb_method(friend_number: Long, a_TOXAV_CALL_COMM_INFO: Long, comm_number: Long)
        {
        }

        // -------- called by AV native methods --------
        // -------- called by AV native methods --------
        // -------- called by AV native methods --------
        // -------- called by native methods --------
        // -------- called by native methods --------
        // -------- called by native methods --------
        @JvmStatic
        fun android_tox_callback_self_connection_status_cb_method(a_TOX_CONNECTION: Int)
        {
            Log.i(TAG, "android_tox_callback_self_connection_status_cb_method: " + a_TOX_CONNECTION)
            update_savedata_file_wrapper()
            if (a_TOX_CONNECTION == ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP.value)
            {
                set_tox_online_state("tcp")
            } else if (a_TOX_CONNECTION == ToxVars.TOX_CONNECTION.TOX_CONNECTION_UDP.value)
            {
                set_tox_online_state("udp")
            } else
            {
                set_tox_online_state("offline")
            }
        }

        @JvmStatic
        fun android_tox_callback_friend_name_cb_method(friend_number: Long, friend_name: String?, length: Long)
        {
            try
            {
                contactstore.update(item = ContactItem(name = friend_name!!, isConnected = tox_friend_get_connection_status(friend_number), pubkey = tox_friend_get_public_key(friend_number)!!))
            } catch (_: Exception)
            {
            }
        }

        @JvmStatic
        fun android_tox_callback_friend_status_message_cb_method(friend_number: Long, status_message: String?, length: Long)
        {
            try
            {
                var fname = tox_friend_get_name(friend_number)
                if (fname == null)
                {
                    fname = "Friend"
                }
                contactstore.update(item = ContactItem(name = fname, isConnected = tox_friend_get_connection_status(friend_number), pubkey = tox_friend_get_public_key(friend_number)!!))
            } catch (_: Exception)
            {
            }
        }

        @JvmStatic
        fun android_tox_callback_friend_lossless_packet_cb_method(friend_number: Long, data: ByteArray?, length: Long)
        {
        }

        @JvmStatic
        fun android_tox_callback_friend_status_cb_method(friend_number: Long, a_TOX_USER_STATUS: Int)
        {
        }

        @JvmStatic
        fun android_tox_callback_friend_connection_status_cb_method(friend_number: Long, a_TOX_CONNECTION: Int)
        {
            Log.i(TAG, "android_tox_callback_friend_connection_status_cb_method: fn=" + friend_number + " " + a_TOX_CONNECTION)
            update_savedata_file_wrapper()
            try
            {
                var fname = tox_friend_get_name(friend_number)
                if (fname == null)
                {
                    fname = "Friend"
                }
                contactstore.update(item = ContactItem(name = fname, isConnected = tox_friend_get_connection_status(friend_number), pubkey = tox_friend_get_public_key(friend_number)!!))
            } catch (_: Exception)
            {
            }
        }

        @JvmStatic
        fun android_tox_callback_friend_typing_cb_method(friend_number: Long, typing: Int)
        {
        }

        @JvmStatic
        fun android_tox_callback_friend_read_receipt_cb_method(friend_number: Long, message_id: Long)
        {
        }

        @JvmStatic
        fun android_tox_callback_friend_request_cb_method(friend_public_key: String?, friend_request_message: String?, length: Long)
        {
            Log.i(TAG, "android_tox_callback_friend_request_cb_method: friend_public_key=" + friend_public_key)
            val new_friendnumber = tox_friend_add_norequest(friend_public_key)
            try
            {
                contactstore.add(item = ContactItem(name = "new Friend #" + new_friendnumber, isConnected = 0, pubkey = friend_public_key!!))
            } catch (_: Exception)
            {
            }
            update_savedata_file_wrapper()
        }

        @JvmStatic
        fun android_tox_callback_friend_message_cb_method(friend_number: Long, message_type: Int, friend_message: String?, length: Long, msgV3hash_bin: ByteArray?, message_timestamp: Long)
        {
            // Log.i(TAG, "android_tox_callback_friend_message_cb_method: fn=" + friend_number + " friend_message=" + friend_message)
            var msgV3hash_hex_string: String? = null
            if (msgV3hash_bin != null)
            {
                msgV3hash_hex_string = bytesToHex(msgV3hash_bin, 0, msgV3hash_bin.size)
            }

            if (message_type == ToxVars.TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK.value)
            {
                return
            }

            GlobalScope.launch(Dispatchers.IO) {
                if (msgV3hash_hex_string != null)
                {
                    HelperMessage.send_msgv3_high_level_ack(friend_number, msgV3hash_hex_string);
                    try
                    { // ("msgv3:"+friend_message)
                        val toxpk = tox_friend_get_public_key(friend_number)!!.uppercase()
                        var timestamp_wrap: Long = message_timestamp * 1000
                        if (timestamp_wrap == 0L)
                        {
                            timestamp_wrap = System.currentTimeMillis()
                        }
                        val msg_id_db = received_message_to_db(toxpk, timestamp_wrap, friend_message)
                        val friendnum = tox_friend_by_public_key(toxpk)
                        val fname = tox_friend_get_name(friendnum)
                        val friend_user = User(fname!!, picture = "friend_avatar.png", toxpk = toxpk)
                        messagestore.send(MessageAction.ReceiveMessage(message = UIMessage(msgDatabaseId = msg_id_db, user = friend_user, timeMs = timestamp_wrap, text = friend_message!!, toxpk = toxpk, trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, filename_fullpath = null)))
                    } catch (_: Exception)
                    {
                    }
                } else
                {
                    try
                    { // ("msgv1:"+friend_message)
                        val toxpk = tox_friend_get_public_key(friend_number)
                        var timestamp_wrap: Long = message_timestamp * 1000
                        if (timestamp_wrap == 0L)
                        {
                            timestamp_wrap = System.currentTimeMillis()
                        }
                        val msg_id_db = received_message_to_db(toxpk, timestamp_wrap, friend_message)
                        val friendnum = tox_friend_by_public_key(toxpk)
                        val fname = tox_friend_get_name(friendnum)
                        val friend_user = User(fname!!, picture = "friend_avatar.png", toxpk = toxpk)
                        messagestore.send(MessageAction.ReceiveMessage(message = UIMessage(msgDatabaseId = msg_id_db, user = friend_user, timeMs = timestamp_wrap, text = friend_message!!, toxpk = toxpk, trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, filename_fullpath = null)))
                    } catch (_: Exception)
                    {
                    }
                }
            }
        }

        @JvmStatic
        fun android_tox_callback_friend_message_v2_cb_method(friend_number: Long, friend_message: String?, length: Long, ts_sec: Long, ts_ms: Long, raw_message: ByteArray?, raw_message_length: Long)
        {
            Log.i(TAG, "android_tox_callback_friend_message_v2_cb_method: fn=" + friend_number + " friend_message=" + friend_message)
            val msg_type = 1
            val raw_message_buf = ByteBuffer.allocateDirect(raw_message_length.toInt())
            raw_message_buf.put(raw_message, 0, raw_message_length.toInt())
            val msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH)
            tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer) // val ts_sec = tox_messagev2_get_ts_sec(raw_message_buf)
            // val ts_ms = tox_messagev2_get_ts_ms(raw_message_buf)
            val msg_id_buffer_compat = ByteBufferCompat(msg_id_buffer)
            val msg_id_as_hex_string: String? = msg_id_buffer_compat.array()?.let {
                bytesToHex(it, msg_id_buffer_compat.arrayOffset(), msg_id_buffer_compat.limit())
            }
            Log.i(TAG, "TOX_FILE_KIND_MESSAGEV2_SEND:MSGv2HASH:2=" + msg_id_as_hex_string);

            try
            {
                val toxpk = tox_friend_get_public_key(friend_number)
                val message_timestamp = ts_sec * 1000
                val msg_id_db = received_message_to_db(toxpk, message_timestamp, friend_message)
                val friendnum = tox_friend_by_public_key(toxpk)
                val fname = tox_friend_get_name(friendnum)
                val friend_user = User(fname!!, picture = "friend_avatar.png", toxpk = toxpk)
                messagestore.send(MessageAction.ReceiveMessage(message = UIMessage(msgDatabaseId = msg_id_db, user = friend_user, timeMs = message_timestamp, text = friend_message!!, toxpk = toxpk, trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, filename_fullpath = null)))
            } catch (_: Exception)
            {
            }
            val pin_timestamp = System.currentTimeMillis()
            send_friend_msg_receipt_v2_wrapper(friend_number, msg_type, msg_id_buffer, (pin_timestamp / 1000));
        }

        @JvmStatic
        fun android_tox_callback_friend_sync_message_v2_cb_method(friend_number: Long, ts_sec: Long, ts_ms: Long, raw_message: ByteArray?, raw_message_length: Long, raw_data: ByteArray?, raw_data_length: Long)
        {
        }

        @JvmStatic
        fun sync_messagev2_send(friend_number: Long, raw_data: ByteArray?, raw_data_length: Long, raw_message_buf_wrapped: ByteBuffer?, msg_id_buffer: ByteBuffer?, real_sender_as_hex_string: String?)
        {
        }

        @JvmStatic
        fun android_tox_callback_friend_read_receipt_message_v2_cb_method(friend_number: Long, ts_sec: Long, msg_id: ByteArray?)
        {
            val msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH)
            msg_id_buffer.put(msg_id, 0, TOX_HASH_LENGTH)
            val msg_id_buffer_compat = ByteBufferCompat(msg_id_buffer)
            val message_id_hash_as_hex_string = bytesToHex(msg_id_buffer_compat.array()!!, msg_id_buffer_compat.arrayOffset(), msg_id_buffer_compat.limit())
            // Log.i(TAG, "receipt_message_v2_cb:MSGv2HASH:2=" + message_id_hash_as_hex_string);
        }

        @JvmStatic
        fun android_tox_callback_file_recv_control_cb_method(friend_number: Long, file_number: Long, a_TOX_FILE_CONTROL: Int)
        {
            if (a_TOX_FILE_CONTROL == ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value)
            {
                Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_CANCEL")
                HelperFiletransfer.cancel_filetransfer(friend_number, file_number)
            } else if (a_TOX_FILE_CONTROL == ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME.value)
            {
                Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME")
                try
                {
                    val ft_id: Long = HelperFiletransfer.get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number)
                    val ft_check = orma!!.selectFromFiletransfer().idEq(ft_id).toList()[0]
                    if (ft_check.kind == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                    {
                        HelperFiletransfer.set_filetransfer_state_from_id(ft_id, ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME.value)
                        HelperFiletransfer.set_filetransfer_accepted_from_id(ft_id)
                    } else
                    {
                        val msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number)
                        HelperFiletransfer.set_filetransfer_state_from_id(ft_id, ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME.value)
                        HelperMessage.set_message_state_from_id(msg_id, ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME.value)
                        HelperFiletransfer.set_filetransfer_accepted_from_id(ft_id)
                        set_message_accepted_from_id(msg_id)
                        try
                        {
                            if (ft_id != -1L)
                            {
                                //**// HelperMessage.update_single_message_from_messge_id(msg_id, true)
                            }
                        } catch (e: java.lang.Exception)
                        {
                        }
                    }
                } catch (e: java.lang.Exception)
                {
                    e.printStackTrace()
                }
            } else if (a_TOX_FILE_CONTROL == ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE.value)
            {
                Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_PAUSE")
                try
                {
                    val ft_id: Long = HelperFiletransfer.get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number)
                    val msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number)
                    HelperFiletransfer.set_filetransfer_state_from_id(ft_id, ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE.value)
                    HelperMessage.set_message_state_from_id(msg_id, ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE.value)
                    try
                    {
                        if (ft_id != -1L)
                        {
                            //**// HelperMessage.update_single_message_from_messge_id(msg_id, true)
                        }
                    } catch (e: java.lang.Exception)
                    {
                        e.printStackTrace()
                    }
                } catch (e2: java.lang.Exception)
                {
                    e2.printStackTrace()
                }
            }
        }

        @JvmStatic
        fun android_tox_callback_file_chunk_request_cb_method(friend_number: Long, file_number: Long, position: Long, length: Long)
        {
            global_last_activity_outgoung_ft_ts = System.currentTimeMillis();
        }

        @JvmStatic
        fun android_tox_callback_file_recv_cb_method(friend_number: Long, file_number: Long, a_TOX_FILE_KIND: Int, file_size: Long, filename: String?, filename_length: Long)
        {
            if (a_TOX_FILE_KIND == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
            { // Log.i(TAG, "file_recv:TOX_FILE_KIND_AVATAR");
                if (file_size > AVATAR_INCOMING_MAX_BYTE_SIZE)
                {
                    Log.i(TAG, "file_recv:avatar_too_large")
                    try
                    {
                        tox_file_control(friend_number, file_number, ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value)
                    } catch (e: java.lang.Exception)
                    {
                        e.printStackTrace()
                    }
                    return
                } else if (file_size == 0L)
                {
                    Log.i(TAG, "file_recv:avatar_size_zero") // friend wants to unset avatar
                    return
                }
                tox_file_control(friend_number, file_number, ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value)
            } else  // DATA file ft
            {
                val friend_pk = tox_friend_get_public_key(friend_number)
                val filename_corrected = get_incoming_filetransfer_local_filename(filename, friend_pk) // @formatter:off
                // @formatter:on
                Log.i(TAG, "file_recv:incoming regular file:file_number=$file_number")
                val f = Filetransfer()
                f.tox_public_key_string = friend_pk
                f.direction = TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING.value
                f.file_number = file_number
                f.kind = a_TOX_FILE_KIND
                f.state = ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE.value
                f.path_name = VFS_TMP_FILE_DIR + "/" + f.tox_public_key_string + "/"
                f.file_name = filename_corrected
                f.filesize = file_size
                f.ft_accepted = false
                f.ft_outgoing_started = false // dummy for incoming FTs, but still set it here
                f.current_position = 0
                f.message_id = -1
                val ft_id: Long = HelperFiletransfer.insert_into_filetransfer_db(f)
                Log.i(TAG, "file_recv:ft_id=$ft_id") // @formatter:off
                Log.i(TAG, "DEBUG_FT:IN:file_recv:file_number=" +
                            file_number.toString() +
                            " fn=" + friend_number.toString() +
                            " pk=" + friend_pk +
                            " fname=" + filename +
                            " fname2=" + filename_corrected +
                            " ft_id=" + ft_id)
                // @formatter:on
                f.id = ft_id // add FT message to UI
                val m = Message()
                m.tox_friendpubkey = friend_pk
                m.direction = 0 // msg received
                m.TOX_MESSAGE_TYPE = 0
                m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value
                m.filetransfer_id = ft_id
                m.filedb_id = -1
                m.state = ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE.value
                m.ft_accepted = false
                m.ft_outgoing_started = false // dummy for incoming FTs, but still set it here
                m.ft_outgoing_queued = false
                m.rcvd_timestamp = System.currentTimeMillis()
                m.sent_timestamp = m.rcvd_timestamp
                m.text = filename_corrected + "\n" + file_size + " bytes";
                if (a_TOX_FILE_KIND == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_FTV2.value)
                {
                    m.filetransfer_kind = ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_FTV2.value
                } else if (a_TOX_FILE_KIND == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA.value)
                {
                    m.filetransfer_kind = ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA.value
                }
                m.is_new = false
                var new_msg_id: Long = -1
                try
                {
                    new_msg_id = orma!!.insertIntoMessage(m)
                } catch (e: Exception)
                {
                }
                m.id = new_msg_id
                Log.i(TAG, "new_msg_id=$new_msg_id") // @formatter:off
                // @formatter:on
                f.message_id = new_msg_id
                update_filetransfer_db_full(f)

                try
                {
                    val t: Thread = object : Thread()
                    {
                        override fun run()
                        {
                            try
                            {
                                sleep((1 * 50).toLong())
                            } catch (e2: java.lang.Exception)
                            {
                                e2.printStackTrace()
                            }
                            check_auto_accept_incoming_filetransfer(m)
                            Log.i(TAG, "file_recv:check_auto_accept_incoming_filetransfer")
                        }
                    }
                    t.start()
                } catch (e: java.lang.Exception)
                {
                    e.printStackTrace()
                }
                val friendnum = tox_friend_by_public_key(friend_pk)
                val fname = tox_friend_get_name(friendnum)
                val friend_user = User(fname!!, picture = "friend_avatar.png", toxpk = friend_pk)
                messagestore.send(MessageAction.ReceiveMessage(message = UIMessage(msgDatabaseId = new_msg_id, user = friend_user, timeMs = timestampMs(), text = m.text, toxpk = friend_pk, trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value, filename_fullpath = null)))
            }
            Log.i(TAG, "file_recv:incoming regular file:999")
        }

        @JvmStatic
        fun android_tox_callback_file_recv_chunk_cb_method(friend_number: Long, file_number: Long, position: Long, data: ByteArray?, length: Long)
        {
            global_last_activity_outgoung_ft_ts = System.currentTimeMillis();
            val friend_pk = tox_friend_get_public_key(friend_number)
            var f: Filetransfer? = null

            try
            {
                f = orma!!.selectFromFiletransfer().directionEq(TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING.value)
                    .file_numberEq(file_number).stateNotEq(ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value).
                tox_public_key_stringEq(friend_pk).orderByIdDesc().toList()[0]
                if (f == null)
                {
                    return
                }
                if (position == 0L)
                {
                    val f1 = File(f.path_name + "/" + f.file_name)
                    val f2 = File(f1.parent)
                    f2.mkdirs()
                }
            } catch (e: java.lang.Exception)
            {
                return
            }

            if (length == 0L) // FT finished
            {
                try
                {
                    move_tmp_file_to_real_file(f.path_name, f.file_name, VFS_FILE_DIR + "/" + f.tox_public_key_string + "/", f.file_name)
                    var filedb_id: Long = -1
                    if (f.kind != ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                    {
                        val file_ = FileDB()
                        file_.kind = f.kind
                        file_.direction = f.direction
                        file_.tox_public_key_string = f.tox_public_key_string
                        file_.path_name = VFS_FILE_DIR + "/" + f.tox_public_key_string + "/"
                        file_.file_name = f.file_name
                        file_.filesize = f.filesize
                        val row_id = orma!!.insertIntoFileDB(file_)
                        filedb_id = orma!!.selectFromFileDB().tox_public_key_stringEq(f.tox_public_key_string).file_nameEq(f.file_name).orderByIdDesc().toList()[0].id // Log.i(TAG, "file_recv_chunk:FileDB:filedb_id=" + filedb_id);
                    }
                    if (f.kind == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                    {
                    } else
                    {
                        val msg_id: Long = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(f.id, friend_number) // Log.i(TAG, "file_recv_chunk:file_READY:001a:msg_id=" + msg_id);
                        HelperMessage.update_message_in_db_filename_fullpath_friendnum_and_filenum(friend_number, file_number, (VFS_FILE_DIR + "/" + f.tox_public_key_string + "/" + f.file_name))
                        HelperMessage.set_message_state_from_friendnum_and_filenum(friend_number, file_number, ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value)
                        HelperMessage.set_message_filedb_from_friendnum_and_filenum(friend_number, file_number, filedb_id)
                        HelperFiletransfer.set_filetransfer_for_message_from_friendnum_and_filenum(friend_number, file_number, -1)
                        try
                        {
                            if (f.id != -1L)
                            {
                                update_single_message_from_messge_id(msg_id, f.filesize, true)
                                Log.i(TAG, "update FT ----==========>>> file DONE " + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/" + f.file_name)
                            }
                        } catch (e: java.lang.Exception)
                        {
                        }
                    } // remove FT from DB
                    HelperFiletransfer.delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number)
                } catch (e2: java.lang.Exception)
                {
                }
            } else  // normal chunck recevied ---------- (NOT start, and NOT end)
            {
                try
                {
                    GlobalScope.launch(Dispatchers.IO) {
                        try
                        {
                            val fos = RandomAccessFile(f.path_name + "/" + f.file_name, "rw")
                            if (f.kind == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_FTV2.value)
                            {
                                fos.seek(position)
                                fos.write(Arrays.copyOfRange(data, TOX_FILE_ID_LENGTH, data!!.size))
                                fos.close()
                            } else
                            {
                                fos.seek(position)
                                fos.write(data)
                                fos.close()
                            }
                        } catch (ex: java.lang.Exception)
                        {
                        }
                    }
                    if (f.filesize < UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES)
                    {
                        if ((f.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES) < position)
                        {
                            GlobalScope.launch(Dispatchers.IO) {
                                try
                                {
                                    f.current_position = position
                                    HelperFiletransfer.update_filetransfer_db_current_position(f)
                                    if (f.kind != ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                                    {
                                        if (f.id != -1L)
                                        {
                                            update_single_message_from_ftid(f, true)
                                            // Log.i(TAG, "update FT ----==========>>> file pos=" + position + " " + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/" + f.file_name)
                                        }
                                    }
                                } catch (e: java.lang.Exception)
                                {
                                }
                            }
                        }
                    } else
                    {
                        if ((f.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES) < position)
                        {
                            GlobalScope.launch(Dispatchers.IO) {
                                try
                                {
                                    f.current_position = position
                                    HelperFiletransfer.update_filetransfer_db_current_position(f)
                                    if (f.kind != ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                                    {
                                        if (f.id != -1L)
                                        { //**// HelperMessage.update_single_message_from_ftid(f.id, true)
                                            // Log.i(TAG, "update FT ----==========>>> file pos=" + position + " " + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/" + f.file_name)
                                            update_single_message_from_ftid(f, true)

                                        }
                                    }
                                } catch (e: java.lang.Exception)
                                {
                                }
                            }
                        }
                    }
                } catch (e: java.lang.Exception)
                {
                }
            }
        }

        @JvmStatic
        fun android_tox_log_cb_method(a_TOX_LOG_LEVEL: Int, file: String?, line: Long, function: String?, message: String?)
        {
            if (CTOXCORE_NATIVE_LOGGING)
            {
                Log.i(TAG, "C-TOXCORE:" + ToxVars.TOX_LOG_LEVEL.value_str(a_TOX_LOG_LEVEL) + ":file=" + file + ":linenum=" + line + ":func=" + function + ":msg=" + message);
            }
        }

        // -------- called by native methods --------
        // -------- called by native methods --------
        // -------- called by native methods --------
        // -------- called by native Conference methods --------
        // -------- called by native Conference methods --------
        // -------- called by native Conference methods --------
        @JvmStatic
        fun android_tox_callback_conference_invite_cb_method(friend_number: Long, a_TOX_CONFERENCE_TYPE: Int, cookie_buffer: ByteArray?, cookie_length: Long)
        {
        }

        @JvmStatic
        fun android_tox_callback_conference_connected_cb_method(conference_number: Long)
        {
        }

        @JvmStatic
        fun android_tox_callback_conference_message_cb_method(conference_number: Long, peer_number: Long, a_TOX_MESSAGE_TYPE: Int, message_orig: String?, length: Long)
        {
        }

        @JvmStatic
        fun android_tox_callback_conference_title_cb_method(conference_number: Long, peer_number: Long, title: String?, title_length: Long)
        {
        }

        @JvmStatic
        fun android_tox_callback_conference_peer_name_cb_method(conference_number: Long, peer_number: Long, name: String?, name_length: Long)
        {
        }

        @JvmStatic
        fun android_tox_callback_conference_peer_list_changed_cb_method(conference_number: Long)
        {
        }

        @JvmStatic
        fun android_tox_callback_conference_namelist_change_cb_method(conference_number: Long, peer_number: Long, a_TOX_CONFERENCE_STATE_CHANGE: Int)
        {
        }

        // -------- called by native Conference methods --------
        // -------- called by native Conference methods --------
        // -------- called by native Conference methods --------
        // -------- called by native new Group methods --------
        // -------- called by native new Group methods --------
        // -------- called by native new Group methods --------
        @OptIn(DelicateCoroutinesApi::class)
        @JvmStatic
        fun android_tox_callback_group_message_cb_method(group_number: Long, peer_id: Long, a_TOX_MESSAGE_TYPE: Int, message_orig: String?, length: Long, message_id: Long)
        {
            val res = tox_group_self_get_peer_id(group_number)
            if (res == peer_id)
            { // do not process our own sent group messages (again)
                return
            }

            val group_id = tox_group_by_groupnum__wrapper(group_number).lowercase()
            val tox_peerpk = tox_group_peer_get_public_key(group_number, peer_id)!!.uppercase()
            val message_timestamp = System.currentTimeMillis()
            received_groupmessage_to_db(tox_peerpk = tox_peerpk!!, groupid = group_id, message_timestamp = message_timestamp, group_message = message_orig, message_id = message_id)
            val peernum = tox_group_peer_by_public_key(group_number, tox_peerpk)
            val fname = tox_group_peer_get_name(group_number, peernum)
            val peer_user = User(fname + " / " + PubkeyShort(tox_peerpk), picture = "friend_avatar.png", toxpk = tox_peerpk.uppercase(), color = ColorProvider.getColor(true, tox_peerpk.uppercase()))
            groupmessagestore.send(GroupMessageAction.ReceiveGroupMessage(UIGroupMessage(peer_user, timeMs = message_timestamp, message_orig!!, toxpk = tox_peerpk, groupId = group_id!!.lowercase(), trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, filename_fullpath = null)))
        }

        @JvmStatic
        fun android_tox_callback_group_private_message_cb_method(group_number: Long, peer_id: Long, a_TOX_MESSAGE_TYPE: Int, message_orig: String?, length: Long)
        {
        }

        @JvmStatic
        fun android_tox_callback_group_privacy_state_cb_method(group_number: Long, a_TOX_GROUP_PRIVACY_STATE: Int)
        {
            update_savedata_file_wrapper()
        }

        @JvmStatic
        fun android_tox_callback_group_invite_cb_method(friend_number: Long, invite_data: ByteArray?, invite_data_length: Long, group_name: String?)
        {
            val invite_data_buf_wrapped = ByteBuffer.allocateDirect(invite_data_length.toInt())
            invite_data_buf_wrapped.put(invite_data, 0, invite_data_length.toInt())
            invite_data_buf_wrapped.rewind()
            val new_group_num = tox_group_invite_accept(friend_number, invite_data_buf_wrapped, invite_data_length, RandomNameGenerator.getFullName(Random()), null)
            update_savedata_file_wrapper()

            if (new_group_num >= 0 && new_group_num < UINT32_MAX_JAVA)
            {
                try
                {
                    val group_identifier: String = bytesToHex(Arrays.copyOfRange(invite_data, 0, GROUP_ID_LENGTH), 0, GROUP_ID_LENGTH).lowercase()
                    val new_privacy_state = tox_group_get_privacy_state(new_group_num)
                    groupstore.add(item = GroupItem(name = group_name!!, isConnected = 0, groupId = group_identifier, privacyState = new_privacy_state))
                } catch (_: Exception)
                {
                }
            }
        }

        @JvmStatic
        fun android_tox_callback_group_peer_join_cb_method(group_number: Long, peer_id: Long)
        {
            update_savedata_file_wrapper()
        }

        @JvmStatic
        fun android_tox_callback_group_peer_exit_cb_method(group_number: Long, peer_id: Long, a_Tox_Group_Exit_Type: Int)
        {
            update_savedata_file_wrapper()
        }

        @JvmStatic
        fun android_tox_callback_group_peer_name_cb_method(group_number: Long, peer_id: Long)
        {
            update_savedata_file_wrapper()
        }

        @JvmStatic
        fun android_tox_callback_group_join_fail_cb_method(group_number: Long, a_Tox_Group_Join_Fail: Int)
        {
            update_savedata_file_wrapper()
        }

        @JvmStatic
        fun android_tox_callback_group_self_join_cb_method(group_number: Long)
        {
            update_savedata_file_wrapper()
        }

        @JvmStatic
        fun android_tox_callback_group_moderation_cb_method(group_number: Long, source_peer_id: Long, target_peer_id: Long, a_Tox_Group_Mod_Event: Int)
        {
            // ** this happens non stop, so don't save here ** // update_savedata_file_wrapper()
        }

        @JvmStatic
        fun android_tox_callback_group_connection_status_cb_method(group_number: Long, a_TOX_GROUP_CONNECTION_STATUS: Int)
        {
            try
            { // groupstore.update(item = GroupItem(name = group_name!!, isConnected = 0, groupId = group_identifier, privacyState = new_privacy_state))
            } catch (_: Exception)
            {
            }
            update_savedata_file_wrapper()
        }

        @JvmStatic
        fun android_tox_callback_group_topic_cb_method(group_number: Long, peer_id: Long, topic: String?, topic_length: Long)
        {
            update_savedata_file_wrapper()
        }

        @JvmStatic
        fun android_tox_callback_group_custom_packet_cb_method(group_number: Long, peer_id: Long, data: ByteArray?, length: Long)
        { // check for correct signature of packets
            val header_ngc_audio_v1 = (6 + 1 + 1 + 1 + 1).toLong()
            val header_ngc_video_v1 = (6 + 1 + 1 + 1 + 1 + 1).toLong()
            val header_ngc_video_v2 = (6 + 1 + 1 + 1 + 1 + 1 + 2 + 1).toLong()
            val header_ngc_histsync_and_files = (6 + 1 + 1 + 32 + 4 + 255).toLong()
            if (length <= TOX_MAX_NGC_FILE_AND_HEADER_SIZE && length >= header_ngc_histsync_and_files + 1)
            { // @formatter:off
                /*
                | what      | Length in bytes| Contents                                           |
                |------     |--------        |------------------                                  |
                | magic     |       6        |  0x667788113435                                    |
                | version   |       1        |  0x01                                              |
                | pkt id    |       1        |  0x11
                 */
                // @formatter:on
                if (data!![0] == 0x66.toByte() && data[1] == 0x77.toByte() && data[2] == 0x88.toByte() && data[3] == 0x11.toByte() && data[4] == 0x34.toByte() && data[5] == 0x35.toByte())
                {
                    if (data[6] == 0x1.toByte() && data[7] == 0x11.toByte())
                    {
                        val group_id = tox_group_by_groupnum__wrapper(group_number)
                        val tox_peerpk = tox_group_peer_get_public_key(group_number, peer_id)!!.uppercase(Locale.getDefault());
                        val peernum = tox_group_peer_by_public_key(group_number, tox_peerpk)
                        val fname = tox_group_peer_get_name(group_number, peernum)
                        val msg_timestamp = System.currentTimeMillis()
                        val incoming_group_file_meta_data = handle_incoming_group_file(group_number, peer_id, data, length, header_ngc_histsync_and_files)

                        if (incoming_group_file_meta_data != null)
                        {
                            val peer_user = User(fname + " / " + PubkeyShort(tox_peerpk), picture = "friend_avatar.png", toxpk = tox_peerpk.uppercase(), color = ColorProvider.getColor(true, tox_peerpk.uppercase()))
                            groupmessagestore.send(GroupMessageAction.ReceiveGroupMessage(UIGroupMessage(peer_user, timeMs = msg_timestamp, incoming_group_file_meta_data.message_text, toxpk = tox_peerpk, groupId = group_id!!.lowercase(), trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value, filename_fullpath = incoming_group_file_meta_data.path_name + incoming_group_file_meta_data.file_name)))
                        }
                    } else
                    { // Log.i(TAG, "group_custom_packet_cb:wrong signature 2");
                    }
                } else
                { // Log.i(TAG, "group_custom_packet_cb:wrong signature 1");
                }
            }

            if (length <= TOX_MAX_NGC_FILE_AND_HEADER_SIZE && length >= header_ngc_video_v1 + 1)
            { // @formatter:off
                /*
                | what      | Length in bytes| Contents                                           |
                |------     |--------        |------------------                                  |
                | magic     |       6        |  0x667788113435                                    |
                | version   |       1        |  0x01                                              |
                | pkt id    |       1        |  0x21
                 */
                // @formatter:on
                if (data!![0] == 0x66.toByte() && data[1] == 0x77.toByte() && data[2] == 0x88.toByte() && data[3] == 0x11.toByte() && data[4] == 0x34.toByte() && data[5] == 0x35.toByte())
                { // byte 640 and 480. LOL
                    if (data[6] == 0x01.toByte() && data[7] == 0x21.toByte() && data[8] == 480.toByte() && data[9] == 640.toByte() && data[10] == 1.toByte())
                    { // disable ngc video version 1 -----------
                    } else if (data[6] == 0x02.toByte() && data[7] == 0x21.toByte() && data[8] == 480.toByte() && data[9] == 640.toByte() && data[10] == 1.toByte() && length >= header_ngc_video_v2 + 1)
                    { // Log.i(TAG, "group_custom_packet_cb:video_v2");
                        //*****//show_ngc_incoming_video_frame_v2(group_number, peer_id, data, length)
                    } else
                    { // Log.i(TAG, "group_custom_packet_cb:wrong signature 2");
                    }
                } else
                { // Log.i(TAG, "group_custom_packet_cb:wrong signature 1");
                }
            }

            if (length <= TOX_MAX_NGC_FILE_AND_HEADER_SIZE && length >= header_ngc_audio_v1 + 1)
            { // @formatter:off
                /*
                | what          | Length in bytes| Contents                                           |
                |------         |--------        |------------------                                  |
                | magic         |       6        |  0x667788113435                                    |
                | version       |       1        |  0x01                                              |
                | pkt id        |       1        |  0x31                                              |
                | audio channels|       1        |  uint8_t always 1 (for MONO)                       |
                | sampling freq |       1        |  uint8_t always 48 (for 48kHz)                     |
                | data          |[1, 1363]       |  *uint8_t  bytes, zero not allowed!                |
                 */
                // @formatter:on
                if (data!![0] == 0x66.toByte() && data[1] == 0x77.toByte() && data[2] == 0x88.toByte() && data[3] == 0x11.toByte() && data[4] == 0x34.toByte() && data[5] == 0x35.toByte())
                {
                    if (data[6] == 0x01.toByte() && data[7] == 0x31.toByte() && data[8] == 1.toByte() && data[9] == 48.toByte())
                    { //*****//play_ngc_incoming_audio_frame(group_number, peer_id, data, length)
                    } else
                    {
                    }
                } else
                {
                }
            }

        }

        @JvmStatic
        fun android_tox_callback_group_custom_private_packet_cb_method(group_number: Long, peer_id: Long, data: ByteArray?, length: Long)
        {
        }

        @JvmStatic
        fun bootstrap_single_wrapper(ip: String, port: Int, key_hex: String): Int
        {
            return bootstrap_single(ip, key_hex, port.toLong())
        }

        @JvmStatic
        fun add_tcp_relay_single_wrapper(ip: String, port: Int, key_hex: String): Int
        {
            return add_tcp_relay_single(ip, key_hex, port.toLong())
        }

        fun received_message_to_db(toxpk: String?, message_timestamp: Long, friend_message: String?): Long
        {
            val m = com.zoffcc.applications.sorm.Message()
            m.is_new = false
            m.tox_friendpubkey = toxpk
            m.direction = 0 // msg received
            m.TOX_MESSAGE_TYPE = 0
            m.read = true
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value
            if (message_timestamp > 0)
            {
                m.rcvd_timestamp = message_timestamp
            } else
            {
                m.rcvd_timestamp = System.currentTimeMillis()
            }
            m.rcvd_timestamp_ms = 0
            m.sent_timestamp = message_timestamp
            m.sent_timestamp_ms = 0
            m.text = friend_message
            m.msg_version = 0
            m.msg_idv3_hash = ""
            m.sent_push = 0
            var row_id: Long = -1
            try
            {
                row_id = orma!!.insertIntoMessage(m)
            } catch (e: Exception)
            {
            }

            return row_id
        }

        fun sent_message_to_db(toxpk: String?, message_timestamp: Long, friend_message: String?): Long
        {
            val m = com.zoffcc.applications.sorm.Message()
            m.is_new = false
            m.tox_friendpubkey = toxpk
            m.direction = 1 // msg sent
            m.TOX_MESSAGE_TYPE = 0
            m.read = false
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value
            if (message_timestamp > 0)
            {
                m.sent_timestamp = message_timestamp
            } else
            {
                m.sent_timestamp = System.currentTimeMillis()
            }
            m.sent_timestamp_ms = 0
            m.rcvd_timestamp = 0
            m.rcvd_timestamp_ms = 0
            m.text = friend_message
            m.msg_version = 0
            m.msg_idv3_hash = ""
            m.sent_push = 0
            var row_id: Long = -1
            try
            {
                row_id = orma!!.insertIntoMessage(m)
            } catch (e: Exception)
            {
            }

            return row_id
        }

        fun received_groupmessage_to_db(tox_peerpk: String, groupid: String, message_timestamp: Long, group_message: String?, message_id: Long): Long
        {
            val message_id_hex = fourbytes_of_long_to_hex(message_id)
            val groupnum = tox_group_by_groupid__wrapper(groupid)
            val peernum = tox_group_peer_by_public_key(groupnum, tox_peerpk)
            val peername = tox_group_peer_get_name(groupnum, peernum)
            val m = GroupMessage()
            m.is_new = false
            m.tox_group_peer_pubkey = tox_peerpk
            m.direction = 0 // msg received
            m.TOX_MESSAGE_TYPE = 0
            m.read = false
            m.tox_group_peername = peername
            m.private_message = 0
            m.group_identifier = groupid.lowercase()
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value
            m.rcvd_timestamp = System.currentTimeMillis()
            m.sent_timestamp = System.currentTimeMillis()
            m.text = group_message
            m.message_id_tox = message_id_hex
            m.was_synced = false
            var row_id: Long = -1
            try
            {
                row_id = orma!!.insertIntoGroupMessage(m)
            } catch (e: Exception)
            {
            }

            return row_id
        }

        fun sent_groupmessage_to_db(groupid: String, message_timestamp: Long, group_message: String?, message_id: Long): Long
        {
            val message_id_hex = fourbytes_of_long_to_hex(message_id)
            val groupnum = tox_group_by_groupid__wrapper(groupid)
            val peernum = tox_group_self_get_peer_id(groupnum)
            val peername = tox_group_peer_get_name(groupnum, peernum)
            val m = com.zoffcc.applications.sorm.GroupMessage()
            m.is_new = false
            m.tox_group_peer_pubkey = tox_group_self_get_public_key(tox_group_by_groupid__wrapper(groupid))!!.uppercase()
            m.direction = 1 // msg sent
            m.TOX_MESSAGE_TYPE = 0
            m.read = true
            m.tox_group_peername = peername
            m.private_message = 0;
            m.group_identifier = groupid;
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value
            m.sent_timestamp = System.currentTimeMillis();
            m.rcvd_timestamp = System.currentTimeMillis(); // since we do not have anything better assume "now"
            m.text = group_message;
            m.was_synced = false;
            m.message_id_tox = message_id_hex
            var row_id: Long = -1
            try
            {
                row_id = orma!!.insertIntoGroupMessage(m)
            } catch (e: Exception)
            {
            }

            return row_id
        }

        @JvmStatic fun modify_message_with_ft(message: Message, filetransfer: Filetransfer?)
        {
            messagestore.send(MessageAction.UpdateMessage(message, filetransfer))
        }

        @JvmStatic fun modify_message_with_finished_ft(message: Message, file_size: Long)
        {
            Log.i(TAG, "modify_message:filename_fullpath=" + message.filename_fullpath)
            messagestore.send(MessageAction.UpdateMessage(message, Filetransfer().filesize(file_size)))
        }
    }
}

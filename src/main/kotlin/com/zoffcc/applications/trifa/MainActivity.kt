@file:Suppress("UNUSED_PARAMETER", "LocalVariableName", "PropertyName", "ClassName", "FunctionName", "unused", "UNUSED_VARIABLE", "SpellCheckingInspection", "UnnecessaryVariable", "ConvertToStringTemplate", "UNUSED_VALUE", "ReplaceCallWithBinaryOperator", "CascadeIf", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "ControlFlowWithEmptyBody", "MemberVisibilityCanBePrivate", "ConstPropertyName", "ConstPropertyName", "ObjectPropertyName", "ReplaceJavaStaticMethodWithKotlinAnalog", "KotlinConstantConditions", "FoldInitializerAndIfToElvis", "SENSELESS_COMPARISON")
package com.zoffcc.applications.trifa

import ColorProvider
import GroupMessageAction
import MessageAction
import SnackBarToast
import UIGroupMessage
import UIMessage
import User
import avstatestore
import avstatestorecallstate
import avstatestorevcapfpsstate
import avstatestorevplayfpsstate
import com.zoffcc.applications.ffmpegav.AVActivity.ffmpegav_loadjni
import com.zoffcc.applications.jninotifications.NTFYActivity
import com.zoffcc.applications.jninotifications.NTFYActivity.jninotifications_loadjni
import com.zoffcc.applications.sorm.FileDB
import com.zoffcc.applications.sorm.Filetransfer
import com.zoffcc.applications.sorm.FriendList
import com.zoffcc.applications.sorm.GroupDB
import com.zoffcc.applications.sorm.GroupMessage
import com.zoffcc.applications.sorm.Message
import com.zoffcc.applications.trifa.AudioBar.audio_in_bar
import com.zoffcc.applications.trifa.AudioBar.audio_out_bar
import com.zoffcc.applications.trifa.HelperFiletransfer.check_auto_accept_incoming_filetransfer
import com.zoffcc.applications.trifa.HelperFiletransfer.get_incoming_filetransfer_local_filename
import com.zoffcc.applications.trifa.HelperFiletransfer.insert_into_filetransfer_db
import com.zoffcc.applications.trifa.HelperFiletransfer.move_tmp_file_to_real_file
import com.zoffcc.applications.trifa.HelperFiletransfer.set_message_accepted_from_id
import com.zoffcc.applications.trifa.HelperFiletransfer.update_filetransfer_db_full
import com.zoffcc.applications.trifa.HelperFriend.add_friend_avatar_chunk
import com.zoffcc.applications.trifa.HelperFriend.add_pushurl_for_friend
import com.zoffcc.applications.trifa.HelperFriend.del_friend_avatar
import com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_num
import com.zoffcc.applications.trifa.HelperFriend.main_get_friend
import com.zoffcc.applications.trifa.HelperFriend.remove_pushurl_for_friend
import com.zoffcc.applications.trifa.HelperFriend.send_friend_msg_receipt_v2_wrapper
import com.zoffcc.applications.trifa.HelperFriend.update_friend_in_db_capabilities
import com.zoffcc.applications.trifa.HelperFriend.update_friend_in_db_msgv3_capability
import com.zoffcc.applications.trifa.HelperFriend.update_friend_msgv3_capability
import com.zoffcc.applications.trifa.HelperGeneric.PubkeyShort
import com.zoffcc.applications.trifa.HelperGeneric.bytesToHex
import com.zoffcc.applications.trifa.HelperGeneric.get_friend_msgv3_capability
import com.zoffcc.applications.trifa.HelperGeneric.hexstring_to_bytebuffer
import com.zoffcc.applications.trifa.HelperGeneric.io_file_copy
import com.zoffcc.applications.trifa.HelperGeneric.play_ngc_incoming_audio_frame
import com.zoffcc.applications.trifa.HelperGeneric.read_chunk_from_SD_file
import com.zoffcc.applications.trifa.HelperGeneric.show_ngc_incoming_video_frame_v2
import com.zoffcc.applications.trifa.HelperGeneric.shrink_image_file
import com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper
import com.zoffcc.applications.trifa.HelperGroup.bytebuffer_to_hexstring
import com.zoffcc.applications.trifa.HelperGroup.bytes_to_hex
import com.zoffcc.applications.trifa.HelperGroup.fourbytes_of_long_to_hex
import com.zoffcc.applications.trifa.HelperGroup.get_last_group_message_in_this_group_within_n_seconds_from_sender_pubkey
import com.zoffcc.applications.trifa.HelperGroup.handle_incoming_group_file
import com.zoffcc.applications.trifa.HelperGroup.handle_incoming_sync_group_file
import com.zoffcc.applications.trifa.HelperGroup.handle_incoming_sync_group_message
import com.zoffcc.applications.trifa.HelperGroup.send_group_image
import com.zoffcc.applications.trifa.HelperGroup.send_ngch_request
import com.zoffcc.applications.trifa.HelperGroup.sync_group_message_history
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupnum__wrapper
import com.zoffcc.applications.trifa.HelperMessage.process_msgv3_high_level_ack
import com.zoffcc.applications.trifa.HelperMessage.sync_messagev2_answer
import com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_read_rcvd_timestamp_rawmsgbytes
import com.zoffcc.applications.trifa.HelperMessage.update_single_message_from_ftid
import com.zoffcc.applications.trifa.HelperMessage.update_single_message_from_messge_id
import com.zoffcc.applications.trifa.HelperRelay.get_own_relay_pubkey
import com.zoffcc.applications.trifa.HelperRelay.get_pushurl_for_friend
import com.zoffcc.applications.trifa.HelperRelay.have_own_relay
import com.zoffcc.applications.trifa.HelperRelay.invite_to_all_groups_own_relay
import com.zoffcc.applications.trifa.HelperRelay.is_any_relay
import com.zoffcc.applications.trifa.HelperRelay.is_own_relay
import com.zoffcc.applications.trifa.HelperRelay.send_all_friend_pubkeys_to_relay
import com.zoffcc.applications.trifa.HelperRelay.send_friend_pubkey_to_relay
import com.zoffcc.applications.trifa.HelperRelay.send_pushtoken_to_relay
import com.zoffcc.applications.trifa.HelperRelay.send_relay_pubkey_to_friend
import com.zoffcc.applications.trifa.TRIFAGlobals.ABSOLUTE_MINIMUM_GLOBAL_VIDEO_BITRATE
import com.zoffcc.applications.trifa.TRIFAGlobals.AVATAR_INCOMING_MAX_BYTE_SIZE
import com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE
import com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_VIDEO_BITRATE
import com.zoffcc.applications.trifa.TRIFAGlobals.GROUP_ID_LENGTH
import com.zoffcc.applications.trifa.TRIFAGlobals.HIGHER_GLOBAL_VIDEO_BITRATE
import com.zoffcc.applications.trifa.TRIFAGlobals.HIGH_GLOBAL_INCOMING_AV_BUFFER_MS
import com.zoffcc.applications.trifa.TRIFAGlobals.LOWER_GLOBAL_VIDEO_BITRATE
import com.zoffcc.applications.trifa.TRIFAGlobals.LOWER_NGC_VIDEO_BITRATE
import com.zoffcc.applications.trifa.TRIFAGlobals.LOWER_NGC_VIDEO_QUANTIZER
import com.zoffcc.applications.trifa.TRIFAGlobals.MEDIUM_GLOBAL_VIDEO_BITRATE
import com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_GROUP_HISTORY_SYNC_DOUBLE_INTERVAL_SECS
import com.zoffcc.applications.trifa.TRIFAGlobals.NGC_AUDIO_BITRATE
import com.zoffcc.applications.trifa.TRIFAGlobals.NORMAL_GLOBAL_INCOMING_AV_BUFFER_MS
import com.zoffcc.applications.trifa.TRIFAGlobals.NORMAL_GLOBAL_VIDEO_BITRATE
import com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE
import com.zoffcc.applications.trifa.TRIFAGlobals.UINT32_MAX_JAVA
import com.zoffcc.applications.trifa.TRIFAGlobals.UPDATE_MESSAGE_PROGRESS_AFTER_BYTES
import com.zoffcc.applications.trifa.TRIFAGlobals.UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES
import com.zoffcc.applications.trifa.TRIFAGlobals.UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES
import com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR
import com.zoffcc.applications.trifa.TRIFAGlobals.VFS_TMP_FILE_DIR
import com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_outgoung_ft_ts
import com.zoffcc.applications.trifa.TRIFAGlobals.global_self_connection_status
import com.zoffcc.applications.trifa.TRIFAGlobals.global_self_last_went_offline_timestamp
import com.zoffcc.applications.trifa.ToxVars.TOX_CAPABILITY_DECODE
import com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION
import com.zoffcc.applications.trifa.ToxVars.TOX_FILE_ID_LENGTH
import com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH
import com.zoffcc.applications.trifa.ToxVars.TOX_MAX_NGC_FILESIZE
import com.zoffcc.applications.trifa.ToxVars.TOX_MAX_NGC_FILESIZE_IGNORE_OVER_THAT
import com.zoffcc.applications.trifa.ToxVars.TOX_MAX_NGC_FILE_AND_HEADER_SIZE
import com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE
import com.zoffcc.applications.trifa.TrifaToxService.Companion.orma
import com.zoffcc.applications.trifa.TrifaToxService.Companion.resend_old_messages
import com.zoffcc.applications.trifa.TrifaToxService.Companion.resend_v3_messages
import com.zoffcc.applications.trifa.VideoInFrame.new_video_in_frame
import com.zoffcc.applications.trifa.VideoInFrame.setup_video_in_resolution
import com.zoffcc.applications.trifa2.timestampMs
import com.zoffcc.applications.trifa_material.trifa_material.BuildConfig
import contactstore
import global_prefs
import globalfrndstoreunreadmsgs
import globalgrpstoreunreadmsgs
import globalstore
import groupmessagestore
import grouppeerstore
import groupstore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lock_data_dir_input
import messagestore
import myUser
import org.briarproject.briar.desktop.contact.ContactItem
import org.briarproject.briar.desktop.contact.GroupItem
import org.briarproject.briar.desktop.contact.GroupPeerItem
import set_tox_online_state
import toxdatastore
import java.io.File
import java.io.PrintWriter
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

@Suppress("UNUSED_PARAMETER", "LocalVariableName", "PropertyName", "ClassName", "FunctionName", "SpellCheckingInspection")
class MainActivity
{
    companion object
    {
        private const val TAG = "trifa.MainActivity"

        // --------- global config ---------
        // --------- global config ---------
        const val CTOXCORE_NATIVE_LOGGING = false // set "false" for release builds
        const val AUDIO_PCM_DEBUG_FILES = false // set "false" for release builds
        const val DEBUG_COMPOSE_UI_UPDATES = false // set "false" for release builds
        const val DEBUG_SET_FAKE_WEBCAM = false // set "false" for release builds

        // --------- global config ---------
        // --------- global config ---------
        var native_lib_loaded = false
        @JvmStatic var native_notification_lib_loaded_error = -1
        @JvmStatic var native_ffmpegav_lib_loaded_error = -1
        var tox_service_fg: TrifaToxService? = null
        var tox_savefile_directory = "."
        @JvmStatic var ORBOT_PROXY_HOST = "127.0.0.1"
        @JvmStatic var ORBOT_PROXY_PORT: Long = 9050
        var PREF__udp_enabled = 1
        var PREF__audio_play_volume_percent = 100
        var PREF__audio_input_filter = 0
        var PREF__do_not_sync_av = 0
        var PREF__v4l2_capture_force_mjpeg: Int = 0 // 0 -> auto, 1 -> force MJPEG video capture with v4l2 devices
        var PREF__video_bitrate_mode: Int = 0 // 0 -> low, 1 -> normal, 2 -> high quality video capture and sending
        var PREF__orbot_enabled_to_int = 0
        var PREF__orbot_enabled_to_int_used_for_init = -1
        @JvmStatic var PREF__toxnoise_enabled_to_int_used_for_init = -1
        var PREF__local_discovery_enabled = 1
        var PREF__ipv6_enabled = 1
        var PREF__force_udp_only = 0
        @JvmStatic var PREF__DB_wal_mode = true
        @JvmStatic var DB_PREF__open_files_directly = false
        @JvmStatic var DB_PREF__notifications_active = true
        @JvmStatic var DB_PREF__windows_audio_in_source = "Microphone"
        @JvmStatic var DB_PREF__U_keep_nospam = true
        @JvmStatic var DB_PREF__send_push_notifications = false
        @JvmStatic var DB_PREF__use_other_toxproxies = false

        @JvmStatic var video_play_count_frames: Long = 0
        @JvmStatic var video_play_last_timestamp: Long = 0
        var video_play_fps_value: Int = 0
        @JvmStatic val video_play_measure_after_frame = 5
        const val ngc_audio_in_queue_max_capacity = 10
        var audio_queue_full_trigger = false
        var audio_queue_play_trigger = true
        var ngc_audio_in_queue: BlockingQueue<ByteArray> = LinkedBlockingQueue(ngc_audio_in_queue_max_capacity)
        const val tox_audio_in_queue_max_capacity = 10
        var tox_a_queue_full_trigger = false
        var tox_a_queue_stop_trigger = true
        var tox_audio_in_queue: BlockingQueue<ByteArray> = LinkedBlockingQueue(tox_audio_in_queue_max_capacity)
        var ta_2 = -1L
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
        const val AUDIO_VU_MIN_VALUE = -20f
        var ROTATE_INCOMING_NGC_VIDEO = true
        //
        var video_buffer_1: ByteBuffer? = null
        var buffer_size_in_bytes = 0
        var _recBuffer: ByteBuffer? = null

        @JvmField
        var PREF__database_files_dir = "."

        //
        // !!!!!! DEBUG !!!!!! change to real password later !!!!!!
        // !!!!!! DEBUG !!!!!! change to real password later !!!!!!
        //
        var password_hash = """passXY!7$9%"""
        var db_password = """"""

        //
        // !!!!!! DEBUG !!!!!! change to real password later !!!!!!
        // !!!!!! DEBUG !!!!!! change to real password later !!!!!!
        //
        var semaphore_tox_savedata: CustomSemaphore? = CustomSemaphore(1)
        fun main_init()
        {
            try
            {
                println("Version:" + BuildConfig.APP_VERSION)
            }
            catch (_: Exception)
            {}

            try
            {
                println("java.vm.name:" + System.getProperty("java.vm.name"))
                println("java.home:" + System.getProperty("java.home"))
                println("java.vendor:" + System.getProperty("java.vendor"))
                println("java.version:" + System.getProperty("java.version"))
                println("java.specification.vendor:" + System.getProperty("java.specification.vendor"))
                println("java.vendor.version:" + System.getProperty("java.vendor.version"))
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
            try
            {
                val locale = Locale.getDefault()
                Log.i(TAG, locale.displayCountry)
                Log.i(TAG, locale.displayLanguage)
                Log.i(TAG, locale.displayName)
                Log.i(TAG, locale.isO3Country)
                Log.i(TAG, locale.isO3Language)
                Log.i(TAG, locale.language)
                Log.i(TAG, locale.country)
            }
            catch (_: Exception)
            {
            }
            try
            {
                Thread.currentThread().name = "t_main"
            } catch (_: Exception)
            {
            }
            Log.i(TAG, "java.library.path:" + System.getProperty("java.library.path"))
            Log.i(TAG, "loaded:c-toxcore:v" + tox_version_major() + "." + tox_version_minor() + "." + tox_version_patch())
            Log.i(TAG, "loaded:jni-c-toxcore:v" + jnictoxcore_version())

            Log.i(TAG, "tox_service_fg:" + tox_service_fg)
            Log.i(TAG, "native_lib_loaded:" + native_lib_loaded)
            Log.i(TAG, "MainActivity:" + this)

            tox_service_fg = TrifaToxService()

            var udp_mode_int = 1
            try
            {
                if (!global_prefs.getBoolean("tox.settings.udp", true))
                {
                    udp_mode_int = 0
                }
            } catch (_: Exception)
            {
            }
            PREF__udp_enabled = udp_mode_int
            Log.i(TAG, "PREF__udp_enabled:" + PREF__udp_enabled)

            var local_discovery_int = 1
            try
            {
                if (!global_prefs.getBoolean("tox.settings.local_lan_discovery", true))
                {
                    local_discovery_int = 0
                }
            } catch (_: Exception)
            {
            }
            PREF__local_discovery_enabled = local_discovery_int
            Log.i(TAG, "PREF__local_discovery_enabled:" + PREF__local_discovery_enabled)

            var ipv6_mode_int = 1
            try
            {
                if (!global_prefs.getBoolean("tox.settings.ipv6", true))
                {
                    ipv6_mode_int = 0
                }
            } catch (_: Exception)
            {
            }
            PREF__ipv6_enabled = ipv6_mode_int
            Log.i(TAG, "PREF__ipv6_enabled:" + PREF__ipv6_enabled)

            var tox_tor_proxy_int = 0
            try
            {
                if (global_prefs.getBoolean("tox.settings.tor_proxy", false))
                {
                    tox_tor_proxy_int = 1
                }
            } catch (_: Exception)
            {
            }
            PREF__orbot_enabled_to_int = tox_tor_proxy_int
            Log.i(TAG, "PREF__orbot_enabled_to_int:" + PREF__orbot_enabled_to_int)

            lock_data_dir_input()

            if (!TrifaToxService.TOX_SERVICE_STARTED)
            {
                tox_savefile_directory = PREF__tox_savefile_dir + File.separator
                if (PREF__orbot_enabled_to_int == 1)
                {
                    PREF__udp_enabled = 0
                    PREF__force_udp_only = 0
                    PREF__local_discovery_enabled = 0
                }
                Log.i(TAG, "init:PREF__udp_enabled=$PREF__udp_enabled")
                Log.i(TAG, "init:PREF__force_udp_only=$PREF__force_udp_only")
                Log.i(TAG, "init:PREF__local_discovery_enabled=$PREF__local_discovery_enabled")
                Log.i(TAG, "init:PREF__orbot_enabled_to_int=$PREF__orbot_enabled_to_int")
                PREF__orbot_enabled_to_int_used_for_init = PREF__orbot_enabled_to_int
                init(tox_savefile_directory, PREF__udp_enabled, PREF__local_discovery_enabled, PREF__orbot_enabled_to_int_used_for_init, ORBOT_PROXY_HOST, ORBOT_PROXY_PORT, password_hash, PREF__ipv6_enabled, PREF__force_udp_only, PREF__ngc_video_bitrate, PREF__ngc_video_max_quantizer, PREF__ngc_audio_bitrate, PREF__ngc_audio_samplerate, PREF__ngc_audio_channels)
            }
            val my_tox_id_temp = get_my_toxid()
            Log.i(TAG, "MyToxID:$my_tox_id_temp")
            myUser.toxpk = my_tox_id_temp

            if (!TrifaToxService.TOX_SERVICE_STARTED)
            {
                tox_service_fg!!.tox_thread_start_fg()
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

            try
            {
                PrintWriter("toxid.txt", "UTF-8").use { out -> out.write(my_tox_id_temp) }
                Log.i(TAG, "also writing toxid to current directory: " + "toxid.txt")
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

                var use_tox_noise = 0
                try
                {
                    if (global_prefs.getBoolean("tox.settings.tox_noise", false))
                    {
                        use_tox_noise = 1
                    }
                } catch (_: Exception)
                {
                }
                PREF__toxnoise_enabled_to_int_used_for_init = 0 // HINT: noise setting disabled for now // use_tox_noise

                var noise_jni_name_addon = ""
                if (use_tox_noise == 1)
                {
                    noise_jni_name_addon = "_noise"
                }

                var libFile = File(resourcesDir, "libjni-c-toxcore${noise_jni_name_addon}.so")
                if (OperatingSystem.getCurrent() == OperatingSystem.LINUX)
                {
                    libFile = File(resourcesDir, "libjni-c-toxcore${noise_jni_name_addon}.so")
                } else if (OperatingSystem.getCurrent() == OperatingSystem.RASPI)
                {
                    libFile = File(resourcesDir, "libjni-c-toxcore_raspi${noise_jni_name_addon}.so")
                } else if (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS)
                {
                    libFile = File(resourcesDir, "jni-c-toxcore${noise_jni_name_addon}.dll")
                } else if (OperatingSystem.getCurrent() == OperatingSystem.MACOS)
                {
                    libFile = File(resourcesDir, "libjni-c-toxcore${noise_jni_name_addon}.jnilib")
                } else if (OperatingSystem.getCurrent() == OperatingSystem.MACARM)
                {
                    libFile = File(resourcesDir, "libjni-c-toxcore_arm64${noise_jni_name_addon}.jnilib")
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

                val libdir2: String = resourcesDir.path
                System.out.println("XXXXX7:" + libdir2)

                native_ffmpegav_lib_loaded_error = -1
                try
                {
                    native_ffmpegav_lib_loaded_error = ffmpegav_loadjni(libdir2)
                }
                catch(_: Exception)
                {
                }

                if (native_ffmpegav_lib_loaded_error == 0)
                {
                    globalstore.setNative_ffmpegav_lib_loaded(true)
                }

                if ((OperatingSystem.getCurrent() == OperatingSystem.LINUX)
                    || (OperatingSystem.getCurrent() == OperatingSystem.MACOS)
                    || (OperatingSystem.getCurrent() == OperatingSystem.MACARM))
                {
                    native_notification_lib_loaded_error = jninotifications_loadjni(libdir2)
                    if (native_notification_lib_loaded_error == 0)
                    {
                        globalstore.setNative_notification_lib_loaded(true)
                        try
                        {
                            Log.i(TAG, "jninotifications version: " + NTFYActivity.jninotifications_version())
                        }
                        catch(_: Exception)
                        {
                        }
                    }
                }
            }
        }

        class send_message_result
        {
            @JvmField
            var msg_num: Long = 0
            @JvmField
            var msg_v2 = false
            @JvmField
            var msg_hash_hex: String? = null
            @JvmField
            var msg_hash_v3_hex: String? = null
            @JvmField
            var raw_message_buf_hex: String? = null
            @JvmField
            var error_num: Long = 0
        }

        // -------- native methods --------
        // -------- native methods --------
        // -------- native methods --------
        @JvmStatic
        external fun init(data_dir: String?, udp_enabled: Int, local_discovery_enabled: Int, orbot_enabled: Int, orbot_host: String?, orbot_port: Long, tox_encrypt_passphrase_hash: String?, enable_ipv6: Int, force_udp_only_mode: Int, ngc_video_bitrate: Int, max_quantizer: Int, ngc_audio_bitrate: Int, ngc_audio_sampling_rate: Int, ngc_audio_channel_count: Int)

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
        external fun getNativeLibTOXGITHASH(): String?

        @JvmStatic
        external fun getNativeLibGITHASH(): String?

        @JvmStatic
        external fun libopus_version(): String?

        @JvmStatic
        external fun libvpx_version(): String?

        @JvmStatic
        external fun x264_version(): String?

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
        external fun tox_friend_get_connection_ip(friend_number: Long): String?

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

        @JvmStatic
        external fun tox_group_get_peer_connection_ip(group_number: Long, peer_id: Long): String?

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
        external fun tox_group_savedpeer_get_public_key(group_number: Long, slot_num: Long): String?

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

        @JvmStatic
        external fun toxav_ngc_video_encode(vbitrate: Int, max_quantizer: Int, width: Int, height: Int, y: ByteArray, y_bytes: Int, u: ByteArray, u_bytes: Int, v: ByteArray, v_bytes: Int, encoded_frame_bytes: ByteArray): Int

        @JvmStatic
        external fun toxav_ngc_video_decode(encoded_frame_bytes: ByteArray, encoded_frame_size_bytes: Int, width: Int, height: Int, y: ByteArray, u: ByteArray, v: ByteArray, flush_decoder: Int): Int

        @JvmStatic
        external fun toxav_ngc_audio_encode(pcm: ByteArray, sample_count_per_frame: Int, encoded_frame_bytes: ByteArray): Int

        @JvmStatic
        external fun toxav_ngc_audio_decode(encoded_frame_bytes: ByteArray, encoded_frame_size_bytes: Int, pcm_decoded: ByteArray): Int


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
        @Suppress("unused")
        @JvmStatic
        fun android_toxav_callback_call_cb_method(friend_number: Long, audio_enabled: Int, video_enabled: Int)
        {
            if (avstatestore.state.calling_state_get() != AVState.CALL_STATUS.CALL_STATUS_NONE)
            {
                // we are already in some other call state, maybe with another friend
                return
            }

            if (avstatestore.state.call_with_friend_pubkey_get() != null)
            {
                // we have some call with a friend already
                return
            }

            avstatestore.state.calling_state_set(AVState.CALL_STATUS.CALL_STATUS_INCOMING)
            avstatestore.state.call_with_friend_pubkey_set(tox_friend_get_public_key(friend_number))
            HelperNotification.displayNotification("Incoming call ...")
        }

        @JvmStatic
        fun android_toxav_callback_video_receive_frame_cb_method(friend_number: Long, frame_width_px: Long, frame_height_px: Long, ystride: Long, ustride: Long, vstride: Long)
        {
            if ((avstatestore.state.call_with_friend_pubkey_get() == null)
                || (avstatestore.state.call_with_friend_pubkey_get() != tox_friend_get_public_key(friend_number)))
            {
                // it's not the currently selected friend, so do not play the video frame
                return
            }

            if (avstatestore.state.calling_state_get() != AVState.CALL_STATUS.CALL_STATUS_CALLING)
            {
                // we are not in a call, ignore incoming video frames
                return
            }

            val y_layer_size = Math.max(frame_width_px, Math.abs(ystride)).toInt() * frame_height_px.toInt()
            val u_layer_size = Math.max(frame_width_px / 2, Math.abs(ustride)).toInt() * (frame_height_px.toInt() / 2)
            val v_layer_size = Math.max(frame_width_px / 2, Math.abs(vstride)).toInt() * (frame_height_px.toInt() / 2)
            val frame_width_px1 = Math.max(frame_width_px, Math.abs(ystride)).toInt()
            val frame_height_px1 = frame_height_px.toInt()
            buffer_size_in_bytes = y_layer_size + v_layer_size + u_layer_size
            if (video_buffer_1 == null) {
                Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:11:1")
                video_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes)
                set_JNI_video_buffer(video_buffer_1, frame_width_px1, frame_height_px1)
                setup_video_in_resolution(frame_width_px1, frame_height_px1, buffer_size_in_bytes)
                Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:11:2")
            } else {
                if (VideoInFrame.width != frame_width_px1 || VideoInFrame.height != frame_height_px1) {
                    Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:22:1")
                    video_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes)
                    set_JNI_video_buffer(video_buffer_1, frame_width_px1, frame_height_px1)
                    setup_video_in_resolution(frame_width_px1, frame_height_px1, buffer_size_in_bytes)
                    Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:22:2")
                }
            }
            new_video_in_frame(video_buffer_1, frame_width_px1, frame_height_px1)
            try
            {
                if (!("" + frame_width_px + "x" + frame_height_px).equals(avstatestorevplayfpsstate.state.incomingResolution)) {
                    avstatestorevplayfpsstate.updateIncomingResolution("" + frame_width_px + "x" + frame_height_px)
                }
            }
            catch(_: Exception)
            {
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        @Suppress("unused")
        @JvmStatic
        fun android_toxav_callback_call_state_cb_method(friend_number: Long, a_TOXAV_FRIEND_CALL_STATE: Int)
        {
            GlobalScope.launch {
                try
                {
                    if (avstatestorecallstate.state.call_state == AVState.CALL_STATUS.CALL_STATUS_INCOMING)
                    {
                        if (avstatestore.state.call_with_friend_pubkey_get() != tox_friend_get_public_key(friend_number))
                        {
                            if (a_TOXAV_FRIEND_CALL_STATE and ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_FINISHED.value > 0)
                            {
                                decline_incoming_av_call()
                            } else if (a_TOXAV_FRIEND_CALL_STATE and ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_ERROR.value > 0)
                            {
                                decline_incoming_av_call()
                            }
                        }
                    }

                    if ((avstatestore.state.call_with_friend_pubkey_get() == null) ||
                        (avstatestore.state.call_with_friend_pubkey_get() != tox_friend_get_public_key(friend_number)))
                    {
                        // not the friend we are in a call with. so ignore callback
                        return@launch
                    }

                    if (a_TOXAV_FRIEND_CALL_STATE and ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_SENDING_A.value +
                        ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_SENDING_V.value
                        + ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_ACCEPTING_A.value +
                        ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_ACCEPTING_V.value > 0)
                    {
                        Log.i(TAG, "toxav_call_state:from=$friend_number call starting")
                    } else if (a_TOXAV_FRIEND_CALL_STATE and ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_FINISHED.value > 0)
                    {
                        Log.i(TAG, "toxav_call_state:from=$friend_number call ending(1)")
                        Log.i(com.zoffcc.applications.trifa.TAG, "ffmpeg_devices_stop:003")
                        avstatestore.state.ffmpeg_devices_stop()
                        on_call_ended_actions()
                    } else if (avstatestore.state.calling_state_get() != AVState.CALL_STATUS.CALL_STATUS_NONE &&
                        a_TOXAV_FRIEND_CALL_STATE == ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_NONE.value)
                    {
                        Log.i(TAG, "toxav_call_state:from=$friend_number call ending(2)")
                        Log.i(com.zoffcc.applications.trifa.TAG, "ffmpeg_devices_stop:004")
                        avstatestore.state.ffmpeg_devices_stop()
                        on_call_ended_actions()
                    } else if (a_TOXAV_FRIEND_CALL_STATE and ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_ERROR.value > 0)
                    {
                        Log.i(TAG, "toxav_call_state:from=$friend_number call ERROR(3)")
                        Log.i(com.zoffcc.applications.trifa.TAG, "ffmpeg_devices_stop:005")
                        avstatestore.state.ffmpeg_devices_stop()
                        on_call_ended_actions()
                    }
                } catch (_: Exception)
                {
                }
            }

        }

        fun on_call_ended_actions()
        {
            avstatestore.state.calling_state_set(AVState.CALL_STATUS.CALL_STATUS_NONE)
            avstatestore.state.call_with_friend_pubkey_set(null)
            set_av_call_status(0)
            Thread.sleep(100)
            try
            {
                val collection: ArrayList<ByteArray> = ArrayList<ByteArray>()
                tox_audio_in_queue.drainTo(collection)
                tox_audio_in_queue.drainTo(collection)
                tox_audio_in_queue.drainTo(collection)
            }
            catch(_: Exception)
            {}
            VideoOutFrame.clear_video_out_frame()
            VideoInFrame.clear_video_in_frame()
            AudioBar.set_cur_value(0, audio_in_bar)
            AudioBar.set_cur_value(0, audio_out_bar)
            avstatestorevplayfpsstate.updateIncomingResolution("")
            avstatestorevplayfpsstate.update(0)
            avstatestorevplayfpsstate.updateNetworkRTT(0)
            avstatestorevplayfpsstate.updatePlayDelay(0)
            avstatestorevplayfpsstate.updateDecoderVBitrate(0)
            avstatestorevcapfpsstate.updateSourceResolution("")
            avstatestorevcapfpsstate.updateSourceFormat("")
            avstatestorevcapfpsstate.update(0)
            avstatestorevcapfpsstate.updateEncoderVBitrate(0)

        }

        @JvmStatic
        @Suppress("unused")
        fun android_toxav_callback_bit_rate_status_cb_method(friend_number: Long, audio_bit_rate: Long, video_bit_rate: Long)
        {
        }

        @JvmStatic
        fun android_toxav_callback_audio_receive_frame_cb_method(friend_number: Long, sample_count: Long, channels: Int, sampling_rate: Long)
        {
            if ((avstatestore.state.call_with_friend_pubkey_get() == null)
                || (avstatestore.state.call_with_friend_pubkey_get() != tox_friend_get_public_key(friend_number)))
            {
                // it's not the currently selected friend, so do not play the audio frame
                return
            }

            if (avstatestore.state.calling_state_get() != AVState.CALL_STATUS.CALL_STATUS_CALLING)
            {
                // we are not in a call, ignore incoming audio frames
                return
            }

            if ((sampling_rate.toInt() != AudioSelectOutBox.SAMPLE_RATE) ||
                (channels != AudioSelectOutBox.CHANNELS) || (_recBuffer == null) ||
                (sample_count.toInt() == 0))
            {
                Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:11:1")
                _recBuffer = ByteBuffer.allocateDirect((10000 * 2 * channels))
                set_JNI_audio_buffer2(_recBuffer)
                AudioSelectOutBox.init()
                AudioSelectOutBox.change_audio_format(sampling_rate.toInt(), channels)
                Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:11:2")
            }

            if (sampling_rate.toInt() != AudioSelectOutBox.SAMPLE_RATE ||
                channels != AudioSelectOutBox.CHANNELS)
            {
                Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:22:1:$sampling_rate".toString() + " " + AudioSelectOutBox.SAMPLE_RATE)
                AudioSelectOutBox.change_audio_format(sampling_rate.toInt(), channels)
                Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:22:2")
            }

            // HINT: this signals that the audio JNI buffer is not set yet
            //       do NOT move this further up!!
            if (sample_count.toInt() == 0)
            {
                Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:EE77:"
                        + friend_number + " " + sample_count + " " + channels + " " + sampling_rate)
                return
            }

            // Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:LLLLLAUDIO:001:"
            //        + (System.currentTimeMillis() - ta_2) + " "
            //        + friend_number + " " + sample_count + " " + channels + " " + sampling_rate)
            ta_2 = System.currentTimeMillis()

            try
            {
                _recBuffer!!.rewind()
                val want_bytes = (sample_count * 2 * channels).toInt()
                val audio_out_byte_buffer = ByteArray(want_bytes)
                _recBuffer!![audio_out_byte_buffer, 0, want_bytes]
                //
                if ((tox_audio_in_queue.remainingCapacity() < 1) && (!tox_a_queue_full_trigger))
                {
                    Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:--- DROP:1 !! FULL !! :trigger:" + tox_audio_in_queue.size)
                    tox_a_queue_full_trigger = true
                }
                else
                {
                    if (tox_a_queue_full_trigger)
                    {
                        if (tox_audio_in_queue.remainingCapacity() >= (tox_audio_in_queue_max_capacity - 2))
                        {
                            tox_a_queue_full_trigger = false
                            tox_audio_in_queue.offer(audio_out_byte_buffer)
                            // Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:release:")
                        }
                        else
                        {
                            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:--- DROP:2 ----:" +
                                    audio_queue_full_trigger)
                        }
                    }
                    else
                    {
                        tox_audio_in_queue.offer(audio_out_byte_buffer)
                        // Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:offer:2:" + tox_audio_in_queue.size)
                    }
                }
            }
            catch(_: Exception)
            {
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun android_toxav_callback_audio_receive_frame_pts_cb_method(friend_number: Long, sample_count: Long, channels: Int, sampling_rate: Long, pts: Long)
        {
            android_toxav_callback_audio_receive_frame_cb_method(friend_number, sample_count, channels, sampling_rate)
        }

        @JvmStatic
        @Suppress("unused")
        fun android_toxav_callback_video_receive_frame_pts_cb_method(friend_number: Long, frame_width_px: Long, frame_height_px: Long, ystride: Long, ustride: Long, vstride: Long, pts: Long)
        {
            android_toxav_callback_video_receive_frame_cb_method(friend_number, frame_width_px, frame_height_px, ystride,
                ustride, vstride)
        }

        @JvmStatic
        @Suppress("unused")
        fun android_toxav_callback_video_receive_frame_h264_cb_method(friend_number: Long, buf_size: Long)
        {
        }

        @JvmStatic
        @Suppress("unused")
        fun android_toxav_callback_group_audio_receive_frame_cb_method(conference_number: Long, peer_number: Long, sample_count: Long, channels: Int, sampling_rate: Long)
        {
        }

        @JvmStatic
        @Suppress("unused")
        fun android_toxav_callback_call_comm_cb_method(friend_number: Long, a_TOXAV_CALL_COMM_INFO: Long, comm_number: Long)
        {
            if (a_TOXAV_CALL_COMM_INFO == ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_DECODER_CURRENT_BITRATE.value.toLong())
            {
                //Log.i(TAG, "call_comm_cb: fnum: " + friend_number
                //        + " DECODER_CURRENT_BITRATE = " + comm_number)
                avstatestorevplayfpsstate.updateDecoderVBitrate(comm_number.toInt())
            }
            else if (a_TOXAV_CALL_COMM_INFO == ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_ENCODER_CURRENT_BITRATE.value.toLong())
            {
                //Log.i(TAG, "call_comm_cb: fnum: " + friend_number
                //        + " ENCODER_CURRENT_BITRATE = " + comm_number)
                avstatestorevcapfpsstate.updateEncoderVBitrate(comm_number.toInt())

            }
            else if (a_TOXAV_CALL_COMM_INFO == ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_NETWORK_ROUND_TRIP_MS.value.toLong())
            {
                //Log.i(TAG, "call_comm_cb: fnum: " + friend_number
                //        + " NETWORK_ROUND_TRIP_MS = " + comm_number)
                avstatestorevplayfpsstate.updateNetworkRTT(comm_number.toInt())
            }
            else if (a_TOXAV_CALL_COMM_INFO == ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_PLAY_DELAY.value.toLong())
            {
                //Log.i(TAG, "call_comm_cb: fnum: " + friend_number
                //        + " PLAY_DELAY = " + comm_number)
                avstatestorevplayfpsstate.updatePlayDelay(comm_number.toInt())
            }
            else
            {
                // Log.i(TAG, "call_comm_cb: fnum: " + friend_number
                //        + " TOXAV_CALL_COMM_INFO: " + a_TOXAV_CALL_COMM_INFO
                //        + " value: " + comm_number)
            }
        }

        // -------- called by AV native methods --------
        // -------- called by AV native methods --------
        // -------- called by AV native methods --------
        // -------- called by native methods --------
        // -------- called by native methods --------
        // -------- called by native methods --------
        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_self_connection_status_cb_method(a_TOX_CONNECTION: Int)
        {
            global_self_connection_status = a_TOX_CONNECTION
            // Log.i(TAG, "android_tox_callback_self_connection_status_cb_method: " + a_TOX_CONNECTION)
            update_savedata_file_wrapper()
            if (a_TOX_CONNECTION == TOX_CONNECTION.TOX_CONNECTION_TCP.value)
            {
                global_self_last_went_offline_timestamp = -1
                set_tox_online_state("tcp")
            } else if (a_TOX_CONNECTION == TOX_CONNECTION.TOX_CONNECTION_UDP.value)
            {
                global_self_last_went_offline_timestamp = -1
                set_tox_online_state("udp")
            } else
            {
                global_self_last_went_offline_timestamp = System.currentTimeMillis()
                set_tox_online_state("offline")
                // Log.i(TAG, "android_tox_callback_self_connection_status_cb_method: setting global_self_last_went_offline_timestamp")
                if (avstatestore.state.call_with_friend_pubkey_get() != null)
                {
                    val fnum = tox_friend_by_public_key(avstatestore.state.call_with_friend_pubkey_get())
                    if (fnum != -1L)
                    {
                        shutdown_av_call(fnum)
                    }
                }
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_friend_name_cb_method(friend_number: Long, friend_name: String?, length: Long)
        {
            try
            {
                val friend_pubkey = tox_friend_get_public_key(friend_number)!!
                val is_relay = is_any_relay(friend_pubkey)
                val ip_addr_str = get_friend_ip_str(friend_number)
                val push_url = get_pushurl_for_friend(friend_pubkey)
                contactstore.update(item = ContactItem(name = friend_name!!,
                    isConnected = tox_friend_get_connection_status(friend_number), pubkey = friend_pubkey,
                    ip_addr = ip_addr_str,
                    push_url = push_url,
                    is_relay = is_relay))
            } catch (_: Exception)
            {
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_friend_status_message_cb_method(friend_number: Long, status_message: String?, length: Long)
        {
            try
            {
                var fname = tox_friend_get_name(friend_number)
                if (fname == null)
                {
                    fname = "Friend"
                }
                val friend_pubkey = tox_friend_get_public_key(friend_number)!!
                val is_relay = is_any_relay(friend_pubkey)
                val ip_addr_str = get_friend_ip_str(friend_number)
                val push_url = get_pushurl_for_friend(friend_pubkey)
                contactstore.update(item = ContactItem(name = fname,
                    isConnected = tox_friend_get_connection_status(friend_number),
                    pubkey = friend_pubkey,
                    ip_addr = ip_addr_str,
                    push_url = push_url,
                    is_relay = is_relay))
            } catch (_: Exception)
            {
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_friend_lossless_packet_cb_method(friend_number: Long, data: ByteArray?, length: Long)
        {
            if (length > 0)
            {
                val fpubkey = tox_friend_get_public_key(friend_number)
                if (fpubkey == null)
                {
                    return
                }

                if (data!![0].toUByte().toInt() == TRIFAGlobals.CONTROL_PROXY_MESSAGE_TYPE.CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND.value)
                {
                    if (DB_PREF__use_other_toxproxies)
                    {
                        if (length == (TOX_PUBLIC_KEY_SIZE + 1).toLong())
                        {
                            val relay_pubkey: String = bytes_to_hex(data).substring(2)
                            val new_friendnumber = tox_friend_add_norequest(relay_pubkey)
                            if (new_friendnumber > -1)
                            {
                                if (new_friendnumber != UINT32_MAX_JAVA)
                                {
                                    update_savedata_file_wrapper()

                                    Log.i(TAG, "friend_lossless_packet_cb:recevied CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND")
                                    Log.i(TAG, "friend_lossless_packet_cb:recevied pubkey:" + relay_pubkey.uppercase())
                                    HelperFriend.add_friend_to_system(relay_pubkey.uppercase(), true, fpubkey)

                                    try
                                    {
                                        contactstore.add(item = ContactItem(name = "Relay #" + relay_pubkey.uppercase().take(6),
                                            isConnected = 0,
                                            pubkey = relay_pubkey.uppercase(),
                                            push_url = "",
                                            is_relay = true))
                                    } catch (_: Exception)
                                    {
                                    }
                                    SnackBarToast("Friend Relay updated or added")
                                }
                            }
                        }
                    }
                } else if (data[0].toUByte().toInt() == TRIFAGlobals.CONTROL_PROXY_MESSAGE_TYPE.CONTROL_PROXY_MESSAGE_TYPE_PUSH_URL_FOR_FRIEND.value)
                {
                    Log.i(TAG,
                          "android_tox_callback_friend_lossless_packet_cb_method:CONTROL_PROXY_MESSAGE_TYPE_PUSH_URL_FOR_FRIEND:len=" +
                          length)
                    if (length > "https://".length + 1)
                    {
                        val pushurl = String(Arrays.copyOfRange(data, 1, data.size), StandardCharsets.UTF_8)
                        Log.i(TAG,"android_tox_callback_friend_lossless_packet_cb_method:CONTROL_PROXY_MESSAGE_TYPE_PUSH_URL_FOR_FRIEND:pushurl=**********") // + pushurl)
                        add_pushurl_for_friend(pushurl, fpubkey)
                    } else
                    {
                        if (length == 0L)
                        {
                            remove_pushurl_for_friend(fpubkey)
                        }
                    }
                }
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_friend_status_cb_method(friend_number: Long, a_TOX_USER_STATUS: Int)
        {
        }

        @Suppress("unused")
        @JvmStatic
        fun android_tox_callback_friend_connection_status_cb_method(friend_number: Long, a_TOX_CONNECTION: Int)
        {
            // Log.i(TAG, "android_tox_callback_friend_connection_status_cb_method: friend number: " + friend_number + " " + a_TOX_CONNECTION)
            update_savedata_file_wrapper()
            try
            {
                var fname = tox_friend_get_name(friend_number)
                if (fname == null)
                {
                    fname = "Friend"
                }
                val friend_pubkey = tox_friend_get_public_key(friend_number)!!
                val is_relay = is_any_relay(friend_pubkey)
                val ip_addr_str = get_friend_ip_str(friend_number)
                val push_url = get_pushurl_for_friend(friend_pubkey)
                contactstore.update(item = ContactItem(name = fname,
                    isConnected = tox_friend_get_connection_status(friend_number),
                    pubkey = friend_pubkey,
                    ip_addr = ip_addr_str,
                    push_url = push_url,
                    is_relay = is_relay))
            } catch (_: Exception)
            {
            }
            val f: FriendList? = main_get_friend(friend_number)

            if (a_TOX_CONNECTION == TOX_CONNECTION.TOX_CONNECTION_NONE.value)
            {
                shutdown_av_call(friend_number)
            }

            if (a_TOX_CONNECTION != TOX_CONNECTION.TOX_CONNECTION_NONE.value)
            {
                // ******** friend just came online ********
                // Log.i(TAG, "android_tox_callback_friend_connection_status_cb_method:friend just came online: friend number: " + friend_number)
                try
                {
                    val fpubkey = tox_friend_get_public_key(friend_number)
                    if (f != null)
                    {
                        val friend_capabilities = tox_friend_get_capabilities(friend_number)
                        f.capabilities = friend_capabilities
                        update_friend_in_db_capabilities(f)
                        if (TOX_CAPABILITY_DECODE(f.capabilities).msgv3)
                        {
                            f.msgv3_capability = 1
                        }
                        else
                        {
                            f.msgv3_capability = 0
                        }
                        update_friend_in_db_msgv3_capability(f)
                    }

                    if (get_friend_msgv3_capability(fpubkey) == 1L)
                    {
                        resend_v3_messages(fpubkey)
                    }
                    else
                    {
                        resend_old_messages(fpubkey)
                    }
                } catch (_: java.lang.Exception)
                {
                }
            }

            if (f == null)
            {
                return
            }

            if (f.TOX_CONNECTION != a_TOX_CONNECTION)
            {
                if (f.TOX_CONNECTION == TOX_CONNECTION.TOX_CONNECTION_NONE.value)
                {
                    // ******** friend just came online ********
                    if (have_own_relay())
                    {
                        if (!is_any_relay(f.tox_public_key_string))
                        {
                            if (DB_PREF__use_other_toxproxies)
                            {
                                send_relay_pubkey_to_friend(get_own_relay_pubkey(),
                                    f.tox_public_key_string)

                                send_friend_pubkey_to_relay(get_own_relay_pubkey(),
                                    f.tox_public_key_string)
                            }
                        }
                        else
                        {
                            if (is_own_relay(f.tox_public_key_string))
                            {
                                invite_to_all_groups_own_relay(get_own_relay_pubkey())
                                send_all_friend_pubkeys_to_relay(get_own_relay_pubkey())
                            }
                        }

                        if (is_own_relay(f.tox_public_key_string))
                        {
                            send_pushtoken_to_relay()
                        }
                    }
                }
            }

            if (a_TOX_CONNECTION != TOX_CONNECTION.TOX_CONNECTION_NONE.value)
            {
                try
                {
                    val ip_addr_str = get_friend_ip_str(friend_number)
                    contactstore.update_ipaddr(pubkey = f.tox_public_key_string, ipaddr = ip_addr_str)
                }
                catch(_: Exception)
                {
                    contactstore.update_ipaddr(pubkey = f.tox_public_key_string, ipaddr = "")
                }
            }
            else
            {
                contactstore.update_ipaddr(pubkey = f.tox_public_key_string, ipaddr = "")
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_friend_typing_cb_method(friend_number: Long, typing: Int)
        {
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_friend_read_receipt_cb_method(friend_number: Long, message_id: Long)
        {
            // Log.i(TAG, "friend_read_receipt:friend_number=" + friend_number + " message_id=" + message_id)
            try
            {
                val toxpk = tox_friend_get_public_key(friend_number)!!.uppercase()
                if ((toxpk == null) || (toxpk.equals("-1", true)))
                {
                    Log.i(TAG, "android_tox_callback_friend_read_receipt_cb_method: tox pubkey error")
                    return
                }

                if (get_friend_msgv3_capability(toxpk) == 1L)
                {
                    // HINT: friend has msgV3 capability, ignore normal read receipts
                    Log.i(TAG, "friend_read_receipt:msgV3:ignore low level ACK, friend:" + get_friend_name_from_num(friend_number))
                    return
                }
                // there can be older messages with same message_id for this friend! so always take the latest one! -------
                val m = orma!!.selectFromMessage().message_idEq(message_id).
                    tox_friendpubkeyEq(toxpk).directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).orderByIdDesc().toList()[0]
                // there can be older messages with same message_id for this friend! so always take the latest one! -------
                // Log.i(TAG, "friend_read_receipt:m=" + m)
                // Log.i(TAG, "friend_read_receipt:m:message_id=" + m.message_id + " text=" + m.text + " friendpubkey=" + m.tox_friendpubkey + " read=" + m.read + " direction=" + m.direction)
                if (m != null)
                {
                    m.rcvd_timestamp = System.currentTimeMillis()
                    m.read = true
                    // Log.i(TAG,
                    //       "friend_read_receipt:friend:" + get_friend_name_from_num(friend_number) + " message:" + m.text +
                    //       " m=" + m)
                    update_message_in_db_read_rcvd_timestamp_rawmsgbytes(m)
                    // update message in UI
                    modify_text_message(m)
                }
            } catch (e: java.lang.Exception)
            {
                Log.i(TAG, "friend_read_receipt:EE:" + e.message)
                e.printStackTrace()
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_friend_request_cb_method(friend_public_key: String?, friend_request_message: String?, length: Long)
        {
            Log.i(TAG, "android_tox_callback_friend_request_cb_method: friend_public_key=" + friend_public_key)
            val new_friendnumber = tox_friend_add_norequest(friend_public_key)
            if (new_friendnumber > -1)
            {
                if (new_friendnumber != UINT32_MAX_JAVA)
                {
                    update_savedata_file_wrapper()
                    try
                    {
                        if (friend_public_key != null)
                        {
                            HelperFriend.add_friend_to_system(friend_public_key.uppercase(),
                                false, null)
                        }
                    }
                    catch(_: Exception)
                    {
                    }

                    try
                    {
                        contactstore.add(item = ContactItem(name = "new Friend #" + new_friendnumber,
                            isConnected = 0,
                            pubkey = friend_public_key!!,
                            push_url = "",
                            is_relay = false))
                    } catch (_: Exception)
                    {
                    }
                    SnackBarToast("Invited by a new Friend")
                }
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_friend_message_cb_method(friend_number: Long, message_type: Int, friend_message: String?, length: Long, msgV3hash_bin: ByteArray?, message_timestamp: Long)
        {
            // Log.i(TAG, "friend_message_cb_method: fn=" + friend_number + " friend_message=" + friend_message)
            val toxpk = tox_friend_get_public_key(friend_number)!!.uppercase()
            if ((toxpk == null) || (toxpk.equals("-1", true)))
            {
                Log.i(TAG, "android_tox_callback_friend_message_cb_method: tox pubkey error")
                return
            }

            var msgV3hash_hex_string: String? = null
            if (msgV3hash_bin != null)
            {
                msgV3hash_hex_string = bytesToHex(msgV3hash_bin, 0, msgV3hash_bin.size)
                val got_messages = orma!!.selectFromMessage().tox_friendpubkeyEq(toxpk).
                    directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value).msg_idv3_hashEq(msgV3hash_hex_string).count()
                // Log.i(TAG, "friend_message:friend:" + friend_number + " msgV3hash_hex_string:" + msgV3hash_hex_string +
                //            " got_messages=" + got_messages);
                // Log.i(TAG, "friend_message:friend:" + friend_number + " msgV3hash_hex_string:" + msgV3hash_hex_string +
                //            " got_messages=" + got_messages);
                if (got_messages > 0)
                {
                    // HINT: we already have received a message with this hash
                    // still send the msgV3 high level ACK, and then ignore
                    Log.i(TAG, "TOX_MESSAGEv3:ignore double message")
                    // Log.i(TAG, "send_msgv3_high_level_ack:001")
                    HelperMessage.send_msgv3_high_level_ack(friend_number, msgV3hash_hex_string)
                    return
                }
            }

            if (message_type == ToxVars.TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK.value)
            {
                process_msgv3_high_level_ack(friend_number, msgV3hash_hex_string, message_timestamp)
                return
            }

            // GlobalScope.launch(Dispatchers.IO) {
                var new_msgv3_cap = 0
                if (msgV3hash_bin != null)
                {
                    val got_messages_mirrored = orma!!.selectFromMessage().
                            tox_friendpubkeyEq(toxpk).directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).
                            msg_idv3_hashEq(msgV3hash_hex_string).count()
                    // Log.i(TAG, "update_friend_msgv3_capability:got_messages_mirrored=" + got_messages_mirrored + " hash1=" +
                    //           msgV3hash_bin + " " + msgV3hash_hex_string);
                    if (got_messages_mirrored > 0)
                    {
                        Log.i(TAG, "update_friend_msgv3_capability:got_messages_mirrored=" + got_messages_mirrored)
                        update_friend_msgv3_capability(friend_number, 0)
                        new_msgv3_cap = 0
                    }
                    else
                    {
                        update_friend_msgv3_capability(friend_number, 1)
                        new_msgv3_cap = 1
                    }
                }
                else
                {
                    // Log.i(TAG, "update_friend_msgv3_capability:hash0=" + msgV3hash_bin + " " + msgV3hash_hex_string);
                    update_friend_msgv3_capability(friend_number, 0)
                    new_msgv3_cap = 0
                }

                if (msgV3hash_hex_string != null)
                {
                    // Log.i(TAG, "send_msgv3_high_level_ack:002")
                    if (new_msgv3_cap == 1)
                    {
                        // send high level ack only if the friend actually understand it
                        // and it's not a mirrored high level ACK
                        HelperMessage.send_msgv3_high_level_ack(friend_number, msgV3hash_hex_string)
                    }
                    try
                    {
                        // ("msgv3:"+friend_message)
                        var timestamp_wrap: Long = message_timestamp * 1000
                        if (timestamp_wrap == 0L)
                        {
                            timestamp_wrap = System.currentTimeMillis()
                        }
                        val msg_id_db = received_message_to_db(toxpk = toxpk,
                            msg_idv3_hash = msgV3hash_hex_string,
                            msg_id_hash = "",
                            msg_version = 0,
                            sent_message_timestamp_ms = timestamp_wrap,
                            rcvd_message_timestamp_ms = System.currentTimeMillis(),
                            friend_message = friend_message)
                        val friendnum = tox_friend_by_public_key(toxpk)
                        val fname = tox_friend_get_name(friendnum)
                        val friend_user = User(fname!!, picture = "friend_avatar.png", toxpk = toxpk)
                        messagestore.send(MessageAction.ReceiveMessage(message = UIMessage(direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value, user = friend_user, timeMs = timestamp_wrap,
                            read = false,
                            sent_push = 0,
                            msg_id_hash = "",
                            msg_idv3_hash = msgV3hash_hex_string,
                            msg_version = 0,
                            recvTimeMs = System.currentTimeMillis(),
                            sentTimeMs = timestamp_wrap,
                            text = friend_message!!, toxpk = toxpk, trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, msgDatabaseId = msg_id_db, filename_fullpath = null)))
                    } catch (_: Exception)
                    {
                    }
                }
                else
                {
                    try
                    { // ("msgv1:"+friend_message)
                        var timestamp_wrap: Long = message_timestamp * 1000
                        if (timestamp_wrap == 0L)
                        {
                            timestamp_wrap = System.currentTimeMillis()
                        }
                        val msg_id_db = received_message_to_db(toxpk = toxpk,
                            sent_message_timestamp_ms = timestamp_wrap,
                            rcvd_message_timestamp_ms = timestamp_wrap,
                            msg_idv3_hash = "",
                            msg_id_hash = "",
                            msg_version = 0,
                            friend_message = friend_message)
                        val friendnum = tox_friend_by_public_key(toxpk)
                        val fname = tox_friend_get_name(friendnum)
                        val friend_user = User(fname!!, picture = "friend_avatar.png", toxpk = toxpk!!)
                        messagestore.send(MessageAction.ReceiveMessage(message = UIMessage(direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value, user = friend_user, timeMs = timestamp_wrap,
                            read = false,
                            sent_push = 0,
                            msg_id_hash = "",
                            msg_idv3_hash = "",
                            msg_version = 0,
                            recvTimeMs = timestamp_wrap,
                            sentTimeMs = timestamp_wrap,
                            text = friend_message!!, toxpk = toxpk!!, trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, msgDatabaseId = msg_id_db, filename_fullpath = null)))
                    } catch (_: Exception)
                    {
                    }
                }
            //}
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_friend_message_v2_cb_method(friend_number: Long, friend_message: String?, length: Long, ts_sec: Long, ts_ms: Long, raw_message: ByteArray?, raw_message_length: Long)
        {
            val toxpk = tox_friend_get_public_key(friend_number)!!.uppercase()
            if ((toxpk == null) || (toxpk.equals("-1", true)))
            {
                Log.i(TAG, "android_tox_callback_friend_message_v2_cb_method: tox pubkey error")
                return
            }

            val msg_type = 1
            val raw_message_buf = ByteBuffer.allocateDirect(raw_message_length.toInt())
            raw_message_buf.put(raw_message, 0, raw_message_length.toInt())
            val msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH)
            tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer) // val ts_sec = tox_messagev2_get_ts_sec(raw_message_buf)
            // val ts_ms = tox_messagev2_get_ts_ms(raw_message_buf)
            val msg_id_buffer_compat = ByteBufferCompat(msg_id_buffer)
            var msg_id_as_hex_string: String = ""
            try
            {
                msg_id_as_hex_string = bytesToHex(msg_id_buffer_compat.array()!!, msg_id_buffer_compat.arrayOffset(), msg_id_buffer_compat.limit())
            }
            catch(_: Exception)
            {
            }
            // Log.i(TAG, "TOX_FILE_KIND_MESSAGEV2:MSGv2HASH:2=" + msg_id_as_hex_string)

            val already_have_message = orma!!.selectFromMessage().tox_friendpubkeyEq(toxpk).
                msg_id_hashEq(msg_id_as_hex_string).count()
            val pin_timestamp = System.currentTimeMillis()

            if (already_have_message > 0)
            {
                // it's a double send, ignore it
                // send message receipt v2, most likely the other party did not get it yet
                // TODO: use received timstamp, not "now" here!
                Log.i(TAG, "TOX_FILE_KIND_MESSAGEV2:ignore double message")
                send_friend_msg_receipt_v2_wrapper(friend_number, msg_type,
                    msg_id_buffer,pin_timestamp / 1000)
                return
            }

            try
            {
                val message_timestamp = ts_sec * 1000
                val msg_id_db = received_message_to_db(toxpk = toxpk,
                    sent_message_timestamp_ms = message_timestamp,
                    rcvd_message_timestamp_ms = pin_timestamp,
                    msg_id_hash = msg_id_as_hex_string,
                    msg_idv3_hash = "",
                    msg_version = 1,
                    friend_message = friend_message)
                val friendnum = tox_friend_by_public_key(toxpk)
                val fname = tox_friend_get_name(friendnum)
                val friend_user = User(fname!!, picture = "friend_avatar.png", toxpk = toxpk!!)
                messagestore.send(MessageAction.ReceiveMessage(message = UIMessage(direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value, user = friend_user, timeMs = message_timestamp,
                    read = false,
                    sent_push = 0,
                    msg_id_hash = msg_id_as_hex_string,
                    msg_idv3_hash = "",
                    msg_version = 1,
                    recvTimeMs = pin_timestamp,
                    sentTimeMs = message_timestamp,
                    text = friend_message!!, toxpk = toxpk!!, trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, msgDatabaseId = msg_id_db, filename_fullpath = null)))
            } catch (_: Exception)
            {
            }
            send_friend_msg_receipt_v2_wrapper(friend_number, msg_type,
                msg_id_buffer, (pin_timestamp / 1000))
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_friend_sync_message_v2_cb_method(friend_number: Long, ts_sec: Long, ts_ms: Long, raw_message: ByteArray?, raw_message_length: Long, raw_data: ByteArray?, raw_data_length: Long)
        {
            // Log.i(TAG, "friend_sync_message_v2_cb::IN:fn=" + get_friend_name_from_num(friend_number))

            if (!is_own_relay(tox_friend_get_public_key(friend_number)))
            {
                // sync message only accepted from my own relay
                return
            }
            val raw_message_buf_wrapped = ByteBuffer.allocateDirect(raw_data_length.toInt())
            raw_message_buf_wrapped.put(raw_data, 0, raw_data_length.toInt())
            val raw_message_buf = ByteBuffer.allocateDirect(raw_message_length.toInt())
            raw_message_buf.put(raw_message, 0, raw_message_length.toInt())
            val msg_sec = tox_messagev2_get_ts_sec(raw_message_buf)
            val msg_ms = tox_messagev2_get_ts_ms(raw_message_buf)
            // Log.i(TAG, "friend_sync_message_v2_cb:sec=" + msg_sec + " ms=" + msg_ms);
            val msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH)
            tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer)
            val msg_id_buffer_compat = ByteBufferCompat(msg_id_buffer)
            val msg_id_as_hex_string = bytesToHex(msg_id_buffer_compat.array()!!,
                msg_id_buffer_compat.arrayOffset(),
                msg_id_buffer_compat.limit())
            // Log.i(TAG, "friend_sync_message_v2_cb:MSGv2HASH=" + msg_id_as_hex_string)
            val real_sender_as_hex_string = tox_messagev2_get_sync_message_pubkey(raw_message_buf)
            // Log.i(TAG, "friend_sync_message_v2_cb:real sender pubkey=" + real_sender_as_hex_string + " " +
            //            get_friend_name_from_pubkey(real_sender_as_hex_string))
            val msgv2_type = tox_messagev2_get_sync_message_type(raw_message_buf)
            // Log.i(TAG, "friend_sync_message_v2_cb:msg type=" +
            //         ToxVars.TOX_FILE_KIND.value_str(msgv2_type.toInt()))
            val msg_id_buffer_wrapped = ByteBuffer.allocateDirect(TOX_HASH_LENGTH)
            tox_messagev2_get_message_id(raw_message_buf_wrapped, msg_id_buffer_wrapped)
            val msg_id_buffer_wrapped_compat = ByteBufferCompat(msg_id_buffer_wrapped)
            val msg_id_as_hex_string_wrapped = bytesToHex(msg_id_buffer_wrapped_compat.array()!!,
                msg_id_buffer_wrapped_compat.arrayOffset(),
                msg_id_buffer_wrapped_compat.limit())
            // Log.i(TAG, "friend_sync_message_v2_cb:MSGv2HASH=" + msg_id_as_hex_string_wrapped + " len=" +
            //            msg_id_as_hex_string_wrapped.length)

            if (msgv2_type.toInt() == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_MESSAGEV2_SEND.value)
            {
                sync_messagev2_send(friend_number, raw_data, raw_data_length,
                    raw_message_buf_wrapped, msg_id_buffer,
                    real_sender_as_hex_string)
            }
            else if (msgv2_type.toInt() == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_MESSAGEV2_ANSWER.value)
            {
                sync_messagev2_answer(raw_message_buf_wrapped, friend_number,
                    msg_id_buffer,
                    real_sender_as_hex_string, msg_id_as_hex_string_wrapped)
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun sync_messagev2_send(friend_number: Long, raw_data: ByteArray?, raw_data_length: Long, raw_message_buf_wrapped: ByteBuffer?, msg_id_buffer: ByteBuffer?, real_sender_as_hex_string: String?)
        {
            val msg_wrapped_sec = tox_messagev2_get_ts_sec(raw_message_buf_wrapped)
            val msg_wrapped_ms = tox_messagev2_get_ts_ms(raw_message_buf_wrapped)
            // Log.i(TAG, "friend_sync_message_v2_cb:sec=" + msg_wrapped_sec + " ms=" + msg_wrapped_ms);
            val msg_text_buffer_wrapped = ByteBuffer.allocateDirect(raw_data_length.toInt())
            val text_length = tox_messagev2_get_message_text(raw_message_buf_wrapped,
                raw_data_length, 0, 0,
                msg_text_buffer_wrapped)
            val msg_text_buffer_wrapped_compat = ByteBufferCompat(msg_text_buffer_wrapped)
            var wrapped_msg_text_as_string = ""

            try
            {
                wrapped_msg_text_as_string = String(msg_text_buffer_wrapped_compat.array()!!,
                    msg_text_buffer_wrapped_compat.arrayOffset(), text_length.toInt(),
                    StandardCharsets.UTF_8)
            } catch (e: java.lang.Exception)
            {
                e.printStackTrace()
            }
            val msg_text_as_hex_string_wrapped = bytesToHex(msg_text_buffer_wrapped_compat.array()!!,
                msg_text_buffer_wrapped_compat.arrayOffset(),
                msg_text_buffer_wrapped_compat.limit())
            // Log.i(TAG, "friend_sync_message_v2_cb:len=" + text_length + " wrapped msg text str=" +
            //             wrapped_msg_text_as_string)
            // Log.i(TAG, "friend_sync_message_v2_cb:wrapped msg text hex=" + msg_text_as_hex_string_wrapped)

            try
            {
                if (tox_friend_by_public_key(real_sender_as_hex_string) == -1L)
                {
                    // pubkey does NOT belong to a friend. it is probably a group id
                    // check it here
                    val real_conference_id = real_sender_as_hex_string!!.lowercase()
                    val group_num = tox_group_by_groupid__wrapper(real_conference_id)
                    if (group_num > -1)
                    {
                        // sync message is a group (NGC) message
                        val real_sender_peer_pubkey = wrapped_msg_text_as_string.substring(0, 64)
                        val real_text_length = (text_length - 64)
                        val real_sender_text_ = wrapped_msg_text_as_string.substring(64)
                        var real_sender_text = ""
                        var real_send_message_id = ""


                        if ((real_sender_text_.length > 8) && (real_sender_text_.startsWith(":", 8)))
                        {
                            real_sender_text = real_sender_text_.substring(9)
                            real_send_message_id = real_sender_text_.substring(0, 8).lowercase()
                        } else
                        {
                            real_sender_text = real_sender_text_
                            real_send_message_id = ""
                        }
                        val sync_msg_received_timestamp = (msg_wrapped_sec * 1000) + msg_wrapped_ms

                        // add text as conference message
                        val sender_peer_num = HelperGroup.get_group_peernum_from_peer_pubkey(real_conference_id,
                            real_sender_peer_pubkey)
                        val gm = get_last_group_message_in_this_group_within_n_seconds_from_sender_pubkey(
                            real_conference_id, real_sender_peer_pubkey, sync_msg_received_timestamp,
                            real_send_message_id, MESSAGE_GROUP_HISTORY_SYNC_DOUBLE_INTERVAL_SECS.toLong(), real_sender_text)

                        // Log.i(TAG, "m.message_id_tox=" + real_send_message_id)

                        if (gm != null)
                        {
                            // Log.i(TAG, "friend_sync_message_v2_cb:potentially double message:2")
                            // ok it's a "potentially" double message
                            // just ignore it, but still send "receipt" to proxy so it won't send this message again
                            send_friend_msg_receipt_v2_wrapper(friend_number, 3, msg_id_buffer,
                                (System.currentTimeMillis() / 1000))
                            return
                        }

                        var syncer_pubkey: String? = null
                        try
                        {
                            syncer_pubkey = tox_friend_get_public_key(friend_number)
                        }
                        catch(_: Exception)
                        {
                        }

                        var peer_name: String = ""
                        val peer_name_saved = tox_group_peer_get_name(group_num, sender_peer_num)
                        if ((peer_name_saved != null) && (peer_name_saved.length > 0))
                        {
                                // HINT: use current peer name instead of name from sync message
                                peer_name = peer_name_saved
                        }

                        if (peer_name.isNullOrEmpty())
                        {
                            // TODO: get peer name from ?? somewhere
                            // Log.i(TAG, "sync_messagev2_send: WARNING: we do not have a peer name")
                        }

                        HelperGroup.group_message_add_from_sync(real_conference_id, syncer_pubkey,
                            sender_peer_num, real_sender_peer_pubkey,
                            TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, real_sender_text, real_text_length,
                            sync_msg_received_timestamp, real_send_message_id,
                            TRIFAGlobals.TRIFA_SYNC_TYPE.TRIFA_SYNC_TYPE_TOXPROXY.value,
                            peer_name)

                        send_friend_msg_receipt_v2_wrapper(friend_number, 3, msg_id_buffer,
                            (System.currentTimeMillis() / 1000))
                        return
                    }
                    else
                    {
                        // sync message from unkown original sender (or conference, which is not implemented in this client)
                        // still send "receipt" to our relay, or else it will send us this message forever
                        // Log.i(TAG, "friend_sync_message_v2_cb:send receipt for unknown message");
                        send_friend_msg_receipt_v2_wrapper(friend_number, 4, msg_id_buffer,
                            (System.currentTimeMillis() / 1000))
                    }
                    return
                }
                else // sync of friend message
                {
                    receive_incoming_message(2, 0,
                        tox_friend_by_public_key(real_sender_as_hex_string),
                        wrapped_msg_text_as_string, raw_data!!, raw_data_length,
                        real_sender_as_hex_string!!,
                        null, 0)
                }
            }
            catch(e: Exception)
            {
                e.printStackTrace()
            }

            // send message receipt v2 to own relay
            send_friend_msg_receipt_v2_wrapper(friend_number, 4, msg_id_buffer, (System.currentTimeMillis() / 1000))
        }

        fun receive_incoming_message(msg_type: Int, tox_message_type: Int, friend_number: Long,
                                     friend_message_text_utf8: String, raw_message: ByteArray,
                                     raw_message_length: Long, original_sender_pubkey: String,
                                     msgV3hash_bin: ByteArray?, message_timestamp: Long)
        {
            // msgV2 relay message
            val friend_number_real_sender: Long = tox_friend_by_public_key(original_sender_pubkey)

            val raw_message_buf = ByteBuffer.allocateDirect(raw_message_length.toInt())
            raw_message_buf.put(raw_message, 0, raw_message_length.toInt())
            val msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH)
            tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer)
            val msg_id_buffer_compat = ByteBufferCompat(msg_id_buffer)

            val ts_sec = tox_messagev2_get_ts_sec(raw_message_buf)
            val ts_ms = tox_messagev2_get_ts_ms(raw_message_buf)
            // Log.i(TAG, "receive_incoming_message:TOX_FILE_KIND_MESSAGEV2_SEND:raw_msg=" + bytes_to_hex(raw_message))

            val msg_id_as_hex_string = bytesToHex(msg_id_buffer_compat.array()!!,
                msg_id_buffer_compat.arrayOffset(),
                msg_id_buffer_compat.limit())
            // Log.i(TAG, "receive_incoming_message:TOX_FILE_KIND_MESSAGEV2_SEND:MSGv2HASH:2=$msg_id_as_hex_string")

            val already_have_message: Int = orma!!.selectFromMessage()
                .tox_friendpubkeyEq(tox_friend_get_public_key(friend_number_real_sender))
                .msg_id_hashEq(msg_id_as_hex_string).count()

            val pin_timestamp = System.currentTimeMillis()

            if (already_have_message > 0)
            {
                // it's a double send, ignore it
                // send message receipt v2, most likely the other party did not get it yet
                // Log.i(TAG,
                //      "receive_incoming_message:ACK1:" + get_friend_name_from_num(friend_number_real_sender) + " " +
                //      msg_id_as_hex_string);
                send_friend_msg_receipt_v2_wrapper(friend_number_real_sender, msg_type, msg_id_buffer,
                    (pin_timestamp / 1000))
                return
            }

            try
            {
                val message_timestamp = ts_sec * 1000
                val msg_id_db = received_message_to_db(toxpk = original_sender_pubkey,
                    sent_message_timestamp_ms = message_timestamp,
                    rcvd_message_timestamp_ms = pin_timestamp,
                    msg_id_hash = msg_id_as_hex_string,
                    msg_idv3_hash = "",
                    msg_version = 1,
                    friend_message = friend_message_text_utf8)
                val friendnum = tox_friend_by_public_key(original_sender_pubkey)
                var fname = tox_friend_get_name(friendnum)
                if (fname.isNullOrEmpty())
                {
                   fname = "unknown"
                   try {
                      val fname2 = main_get_friend(original_sender_pubkey).name
                      if (fname2.isNullOrEmpty()) {
                          fname = fname2
                      }
                   } catch (_: Exception) {}
                }

                if (fname.isNullOrEmpty()) {
                    // Log.i(TAG, "receive_incoming_message:WARNING:we do not have a friend name")
                }

                val friend_user = User(fname!!, picture = "friend_avatar.png", toxpk = original_sender_pubkey)
                messagestore.send(MessageAction.ReceiveMessage(message = UIMessage(direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value,
                    user = friend_user, timeMs = message_timestamp,
                    read = false,
                    sent_push = 0,
                    msg_id_hash = msg_id_as_hex_string,
                    msg_idv3_hash = "",
                    msg_version = 1,
                    recvTimeMs = pin_timestamp,
                    sentTimeMs = message_timestamp,
                    text = friend_message_text_utf8, toxpk = original_sender_pubkey,
                    trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value,
                    msgDatabaseId = msg_id_db, filename_fullpath = null)))
            } catch (_: Exception)
            {
            }

            send_friend_msg_receipt_v2_wrapper(friend_number_real_sender,
                msg_type, msg_id_buffer,
                (pin_timestamp / 1000))
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_friend_read_receipt_message_v2_cb_method(friend_number: Long, ts_sec: Long, msg_id: ByteArray?)
        {
            val msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH)
            msg_id_buffer.put(msg_id, 0, TOX_HASH_LENGTH)
            val msg_id_buffer_compat = ByteBufferCompat(msg_id_buffer)
            val message_id_hash_as_hex_string = bytesToHex(msg_id_buffer_compat.array()!!, msg_id_buffer_compat.arrayOffset(), msg_id_buffer_compat.limit())
            // Log.i(TAG, "receipt_message_v2_cb:MSGv2HASH:2=" + message_id_hash_as_hex_string)

            try
            {
                val toxpk = tox_friend_get_public_key(friend_number)!!.uppercase()
                if ((toxpk == null) || (toxpk.equals("-1", true)))
                {
                    Log.i(TAG, "receipt_message_v2_cb: tox pubkey error")
                    return
                }
                val m_try = orma!!.selectFromMessage().
                    msg_id_hashEq(message_id_hash_as_hex_string).
                    tox_friendpubkeyEq(toxpk).directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).
                    readEq(false).toList()
                if ((m_try == null) || (m_try.size < 1))
                {
                    // HINT: it must a an ACK send from a friends toxproxy to singal the receipt of the message on behalf of the friend
                    // TODO: write me
                    return
                }

                val m = orma!!.selectFromMessage().
                    msg_id_hashEq(message_id_hash_as_hex_string).
                    tox_friendpubkeyEq(toxpk).
                    directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).
                    readEq(false).
                    get(0)

                if (m != null)
                {
                    try
                    {
                        m.raw_msgv2_bytes = ""
                        m.rcvd_timestamp = System.currentTimeMillis()
                        m.read = true
                        update_message_in_db_read_rcvd_timestamp_rawmsgbytes(m)
                        m.resend_count = TRIFAGlobals.MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION
                        HelperMessage.update_message_in_db_resend_count(m)
                        // update message in UI
                        modify_text_message(m)
                    }
                    catch(_: Exception)
                    {

                    }
                }
            }
            catch(e: Exception)
            {
                e.printStackTrace()
            }
        }

        @JvmStatic
        @Suppress("unused")
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
        @Suppress("unused")
        fun android_tox_callback_file_chunk_request_cb_method(friend_number: Long, file_number: Long, position: Long, length: Long)
        {
            global_last_activity_outgoung_ft_ts = System.currentTimeMillis()

            try
            {
                val ft = orma!!.selectFromFiletransfer().
                directionEq(TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value).
                stateNotEq(ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value).
                tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                file_numberEq(file_number).orderByIdDesc().toList()[0]
                if (ft == null)
                {
                    Log.i(TAG, "file_chunk_request:ft=NULL")
                    return
                }
                // Log.i(TAG, "file_chunk_request:ft=" + ft.kind + ":" + ft);
                if (ft.kind == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                {
                    // TODO: write me!!
                } else if (ft.kind == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_FTV2.value)
                {
                    if (length == 0L)
                    {
                        Log.i(TAG, "file_chunk_request:file fully sent")
                        // transfer finished -----------
                        var filedb_id: Long = -1
                        // put into "FileDB" table
                        val file_ = FileDB()
                        file_.kind = ft.kind
                        file_.direction = ft.direction
                        file_.tox_public_key_string = ft.tox_public_key_string
                        file_.path_name = ft.path_name
                        file_.file_name = ft.file_name
                        file_.is_in_VFS = false
                        file_.filesize = ft.filesize
                        val row_id = orma!!.insertIntoFileDB(file_)
                        // Log.i(TAG, "file_chunk_request:FileDB:row_id=" + row_id);
                        filedb_id = orma!!.selectFromFileDB().tox_public_key_stringEq(ft.tox_public_key_string).file_nameEq(ft.file_name).path_nameEq(ft.path_name).directionEq(ft.direction).filesizeEq(ft.filesize).orderByIdDesc()[0].id
                        val msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft.id, friend_number)
                        val full_path_builder = ft.path_name + "/" + ft.file_name
                        HelperMessage.update_message_in_db_filename_fullpath_friendnum_and_filenum(friend_number,
                            file_number,
                            full_path_builder)
                        HelperMessage.set_message_state_from_friendnum_and_filenum(friend_number, file_number,
                            ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value)
                        HelperMessage.set_message_filedb_from_friendnum_and_filenum(friend_number, file_number, filedb_id)
                        HelperFiletransfer.set_filetransfer_for_message_from_friendnum_and_filenum(friend_number,
                            file_number, -1)
                        try
                        {
                            // Log.i(TAG, "file_chunk_request:file_READY:002");
                            if (ft.id != -1L)
                            {
                                // Log.i(TAG, "file_chunk_request:file_READY:003:f.id=" + ft.id + " msg_id=" + msg_id);
                                update_single_message_from_messge_id(msg_id, file_.filesize, true)
                            }
                        } catch (e: java.lang.Exception)
                        {
                            Log.i(TAG, "file_chunk_request:file_READY:EE:" + e.message)
                        }
                        // transfer finished -----------
                        HelperFiletransfer.delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number)
                    } else
                    {
                        val fname = File(ft.path_name + "/" + ft.file_name).absolutePath
                        // Log.i(TAG, "file_chunk_request:fname=" + fname);
                        val file_chunk_length = length
                        var bytes_chunk: ByteArray? = read_chunk_from_SD_file(fname, position, file_chunk_length)
                        if (bytes_chunk == null)
                        {
                            return
                        }
                        var file_chunk = ByteBuffer.allocateDirect((file_chunk_length + TOX_FILE_ID_LENGTH).toInt())
                        var file_id_hash_bytes: ByteBuffer? = hexstring_to_bytebuffer(ft.tox_file_id_hex)
                        // Log.i(TAG, "file_id_hash_bytes="+file_id_hash_bytes)
                        // Log.i(TAG, "ft.tox_file_id_hex="+ft.tox_file_id_hex)
                        file_chunk!!.put(file_id_hash_bytes)
                        file_chunk!!.put(bytes_chunk)
                        bytes_chunk = null
                        val res = tox_file_send_chunk(friend_number, file_number, position, file_chunk,
                            file_chunk_length + TOX_FILE_ID_LENGTH)
                        file_chunk = null
                        file_id_hash_bytes = null
                        if (ft.filesize < UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES)
                        {
                            if (ft.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES < position)
                            {
                                ft.current_position = position
                                HelperFiletransfer.update_filetransfer_db_current_position(ft)
                                if (ft.kind != ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                                {
                                    // update_all_messages_global(false);
                                    try
                                    {
                                        if (ft.id != -1L)
                                        {
                                            update_single_message_from_ftid(ft, true)
                                        }
                                    } catch (_: java.lang.Exception)
                                    {
                                    }
                                }
                            }
                        } else
                        {
                            if (ft.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES < position)
                            {
                                ft.current_position = position
                                HelperFiletransfer.update_filetransfer_db_current_position(ft)
                                if (ft.kind != ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                                {
                                    // update_all_messages_global(false);
                                    try
                                    {
                                        if (ft.id != -1L)
                                        {
                                            update_single_message_from_ftid(ft, true)
                                        }
                                    } catch (_: java.lang.Exception)
                                    {
                                    }
                                }
                            }
                        }
                    }
                } else  // TOX_FILE_KIND_DATA.value
                {
                    if (length == 0L)
                    {
                        Log.i(TAG, "file_chunk_request:file fully sent")
                        // transfer finished -----------
                        var filedb_id: Long = -1
                        if (ft.kind != ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                        {
                            // put into "FileDB" table
                            val file_ = FileDB()
                            file_.kind = ft.kind
                            file_.direction = ft.direction
                            file_.tox_public_key_string = ft.tox_public_key_string
                            file_.path_name = ft.path_name
                            file_.file_name = ft.file_name
                            file_.is_in_VFS = false
                            file_.filesize = ft.filesize
                            val row_id = orma!!.insertIntoFileDB(file_)
                            // Log.i(TAG, "file_chunk_request:FileDB:row_id=" + row_id);
                            filedb_id = orma!!.selectFromFileDB().tox_public_key_stringEq(ft.tox_public_key_string).
                            file_nameEq(ft.file_name).path_nameEq(ft.path_name).
                            directionEq(ft.direction).filesizeEq(ft.filesize).orderByIdDesc().toList()[0].id
                            // Log.i(TAG, "file_chunk_request:FileDB:filedb_id=" + filedb_id);
                        }
                        // Log.i(TAG, "file_chunk_request:file_READY:001:f.id=" + ft.id);
                        val msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(
                            ft.id, friend_number)
                        // Log.i(TAG, "file_chunk_request:file_READY:001a:msg_id=" + msg_id);
                        HelperMessage.update_message_in_db_filename_fullpath_friendnum_and_filenum(friend_number,
                            file_number,
                            ft.path_name + "/" +
                                    ft.file_name)
                        HelperMessage.set_message_state_from_friendnum_and_filenum(friend_number, file_number,
                            ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value)
                        HelperMessage.set_message_filedb_from_friendnum_and_filenum(
                            friend_number, file_number, filedb_id)
                        HelperFiletransfer.set_filetransfer_for_message_from_friendnum_and_filenum(friend_number,
                            file_number, -1)
                        try
                        {
                            // Log.i(TAG, "file_chunk_request:file_READY:002");
                            if (ft.id != -1L)
                            {
                                // Log.i(TAG, "file_chunk_request:file_READY:003:f.id=" + ft.id + " msg_id=" + msg_id);
                                update_single_message_from_messge_id(msg_id, ft.filesize, true)
                            }
                        } catch (e: java.lang.Exception)
                        {
                            Log.i(TAG, "file_chunk_request:file_READY:EE:" + e.message)
                        }
                        // transfer finished -----------
                        val avatar_chunk = ByteBuffer.allocateDirect(1)
                        val res = tox_file_send_chunk(friend_number, file_number, position, avatar_chunk, 0)
                        // Log.i(TAG, "file_chunk_request:res(2)=" + res);
                        // remove FT from DB
                        HelperFiletransfer.delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number)
                    } else
                    {
                        val fname = File(ft.path_name + "/" + ft.file_name).absolutePath
                        val file_chunk_length = length
                        val bytes_chunk: ByteArray? = read_chunk_from_SD_file(fname, position, file_chunk_length)
                        if (bytes_chunk == null)
                        {
                            return
                        }
                        // byte[] bytes_chunck = new byte[(int) file_chunk_length];
                        // avatar_bytes.position((int) position);
                        // avatar_bytes.get(bytes_chunck, 0, (int) file_chunk_length);
                        val file_chunk = ByteBuffer.allocateDirect(file_chunk_length.toInt())
                        file_chunk.put(bytes_chunk)
                        val res = tox_file_send_chunk(friend_number, file_number, position, file_chunk, file_chunk_length)
                        // Log.i(TAG, "file_chunk_request:res(1)=" + res);
                        // TODO: handle error codes from tox_file_send_chunk() here ----
                        if (ft.filesize < UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES)
                        {
                            if ((ft.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES) < position)
                            {
                                ft.current_position = position
                                HelperFiletransfer.update_filetransfer_db_current_position(ft)
                                if (ft.kind != ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                                {
                                    // update_all_messages_global(false);
                                    try
                                    {
                                        if (ft.id != -1L)
                                        {
                                            update_single_message_from_ftid(ft, true)
                                        }
                                    } catch (_: java.lang.Exception)
                                    {
                                    }
                                }
                            }
                        } else
                        {
                            if ((ft.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES) < position)
                            {
                                ft.current_position = position
                                HelperFiletransfer.update_filetransfer_db_current_position(ft)
                                if (ft.kind != ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                                {
                                    // update_all_messages_global(false);
                                    try
                                    {
                                        if (ft.id != -1L)
                                        {
                                            update_single_message_from_ftid(ft, true)
                                        }
                                    } catch (_: java.lang.Exception)
                                    {
                                    }
                                }
                            }
                        }
                        // Log.i(TAG, "file_chunk_request:ft:099:" + (System.currentTimeMillis() - ts01));
                    }
                }
            } catch (e: java.lang.Exception)
            {
                e.printStackTrace()
                Log.i(TAG, "file_chunk_request:EE1:" + e.message)
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_file_recv_cb_method(friend_number: Long, file_number: Long, a_TOX_FILE_KIND: Int, file_size: Long, filename: String?, filename_length: Long)
        {
            if (a_TOX_FILE_KIND == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
            {
                // Log.i(TAG, "file_recv:TOX_FILE_KIND_AVATAR");
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
                }
                else if (file_size == 0L)
                {
                    Log.i(TAG, "file_recv:avatar_size_zero")
                    // friend wants to unset avatar
                    val friend_pk = tox_friend_get_public_key(friend_number)
                    if (friend_pk != null)
                    {
                        del_friend_avatar(friend_pk)
                    }
                    try
                    {
                        tox_file_control(friend_number, file_number, ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value)
                    } catch (e: java.lang.Exception)
                    {
                        e.printStackTrace()
                    }
                    return
                }

                val friend_pk = tox_friend_get_public_key(friend_number)
                if (friend_pk != null)
                {
                    val f = Filetransfer()
                    f.tox_public_key_string = friend_pk.uppercase()
                    f.direction = TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING.value
                    f.file_number = file_number
                    f.kind = a_TOX_FILE_KIND
                    f.state = ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE.value
                    f.path_name = ""
                    f.file_name = ""
                    f.filesize = file_size
                    f.ft_accepted = true
                    f.ft_outgoing_started = true // dummy for incoming FTs, but still set it here
                    f.current_position = 0
                    f.message_id = -1
                    val ft_id: Long = insert_into_filetransfer_db(f)
                    f.id = ft_id
                    // TODO: we just accept incoming avatar, maybe make some checks first?
                    tox_file_control(friend_number, file_number, ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME.value)
                }
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
                val ft_id: Long = insert_into_filetransfer_db(f)
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
                m.direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value // msg received
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
                m.text = filename_corrected + "\n" + file_size + " bytes"
                if (a_TOX_FILE_KIND == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_FTV2.value)
                {
                    m.filetransfer_kind = ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_FTV2.value
                } else if (a_TOX_FILE_KIND == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA.value)
                {
                    m.filetransfer_kind = ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA.value
                }
                m.is_new = true
                var new_msg_id: Long = -1
                try
                {
                    new_msg_id = orma!!.insertIntoMessage(m)
                } catch (_: Exception)
                {
                }
                m.id = new_msg_id
                Log.i(TAG, "new_msg_id=$new_msg_id") // @formatter:off
                // @formatter:on
                f.message_id = new_msg_id
                update_filetransfer_db_full(f)

                try
                {
                    if ((!globalstore.isFocused()) || (globalstore.isMinimized()) || (!contactstore.state.visible) || (contactstore.state.selectedContactPubkey != friend_pk))
                    {
                        var fname: String? = ""
                        try
                        {
                            fname = " from " + tox_friend_get_name(tox_friend_by_public_key(friend_pk))
                        }
                        catch (e2: Exception)
                        {
                            e2.printStackTrace()
                        }
                        HelperNotification.displayNotification("new incoming File" + fname)
                        globalstore.increase_unread_message_count()
                        globalfrndstoreunreadmsgs.increase_unread_per_friend_message_count(friend_pk!!)
                    }
                } catch (_: Exception)
                {
                }

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
                val friend_user = User(fname!!, picture = "friend_avatar.png", toxpk = friend_pk!!)
                messagestore.send(MessageAction.ReceiveMessage(message = UIMessage(direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value, user = friend_user, timeMs = timestampMs(),
                    read = false,
                    sent_push = 0,
                    msg_id_hash = "",
                    msg_idv3_hash = "",
                    msg_version = 0,
                    recvTimeMs = System.currentTimeMillis(),
                    sentTimeMs = timestampMs(),
                    text = m.text, toxpk = friend_pk!!, trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value, msgDatabaseId = new_msg_id, filename_fullpath = null, file_state = m.state)))
            }
            Log.i(TAG, "file_recv:incoming regular file:999")
        }

        @OptIn(DelicateCoroutinesApi::class)
        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_file_recv_chunk_cb_method(friend_number: Long, file_number: Long, position: Long, data: ByteArray?, length: Long)
        {
            global_last_activity_outgoung_ft_ts = System.currentTimeMillis()
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
                    if (f.kind != ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                    {
                        val f1 = File(f.path_name + "/" + f.file_name)
                        val f2 = File(f1.parent)
                        f2.mkdirs()
                    }
                }
            } catch (e: java.lang.Exception)
            {
                return
            }

            if (length == 0L) // FT finished
            {
                try
                {
                    var filedb_id: Long = -1
                    if (f.kind != ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                    {
                        move_tmp_file_to_real_file(f.path_name, f.file_name, VFS_FILE_DIR + "/" + f.tox_public_key_string + "/", f.file_name)
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
                        Log.i(TAG, "callback_file_recv_chunk_cb: incoming avatar finished: " + f.filesize)
                    }
                    else
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
                        } catch (_: java.lang.Exception)
                        {
                        }
                    } // remove FT from DB
                    HelperFiletransfer.delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number)
                } catch (_: java.lang.Exception)
                {
                }
            } else  // normal chunck recevied ---------- (NOT start, and NOT end)
            {
                try
                {
                    if (f.kind == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR.value)
                    {
                        val avatar_chunk_hex = bytesToHex(data!!, 0, length.toInt()).uppercase()
                        // Log.i(TAG, "callback_file_recv_chunk_cb:incoming avatar chunk: " + position + " " + length) // + " " + avatar_chunk_hex)
                        add_friend_avatar_chunk(friend_pk, avatar_chunk_hex, (position == 0L))
                    }
                    else
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
                            } catch (_: java.lang.Exception)
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
                                    } catch (_: java.lang.Exception)
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
                                    } catch (_: java.lang.Exception)
                                    {
                                    }
                                }
                            }
                        }
                    }
                } catch (_: java.lang.Exception)
                {
                }
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_log_cb_method(a_TOX_LOG_LEVEL: Int, file: String?, line: Long, function: String?, message: String?)
        {
            if (CTOXCORE_NATIVE_LOGGING)
            {
                Log.i(TAG, "C-TOXCORE:" + ToxVars.TOX_LOG_LEVEL.value_str(a_TOX_LOG_LEVEL) + ":file=" + file + ":linenum=" + line + ":func=" + function + ":msg=" + message)
            }
        }

        // -------- called by native methods --------
        // -------- called by native methods --------
        // -------- called by native methods --------
        // -------- called by native Conference methods --------
        // -------- called by native Conference methods --------
        // -------- called by native Conference methods --------
        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_conference_invite_cb_method(friend_number: Long, a_TOX_CONFERENCE_TYPE: Int, cookie_buffer: ByteArray?, cookie_length: Long)
        {
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_conference_connected_cb_method(conference_number: Long)
        {
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_conference_message_cb_method(conference_number: Long, peer_number: Long, a_TOX_MESSAGE_TYPE: Int, message_orig: String?, length: Long)
        {
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_conference_title_cb_method(conference_number: Long, peer_number: Long, title: String?, title_length: Long)
        {
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_conference_peer_name_cb_method(conference_number: Long, peer_number: Long, name: String?, name_length: Long)
        {
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_conference_peer_list_changed_cb_method(conference_number: Long)
        {
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_conference_namelist_change_cb_method(conference_number: Long, peer_number: Long, a_TOX_CONFERENCE_STATE_CHANGE: Int)
        {
        }

        // -------- called by native Conference methods --------
        // -------- called by native Conference methods --------
        // -------- called by native Conference methods --------
        // -------- called by native new Group methods --------
        // -------- called by native new Group methods --------
        // -------- called by native new Group methods --------
        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_message_cb_method(group_number: Long, peer_id: Long, a_TOX_MESSAGE_TYPE: Int, message_orig: String?, length: Long, message_id: Long)
        {
            val res = tox_group_self_get_peer_id(group_number)
            if (res == peer_id)
            { // do not process our own sent group messages (again)
                return
            }

            val group_id = tox_group_by_groupnum__wrapper(group_number).lowercase()
            val tox_peerpk = tox_group_peer_get_public_key(group_number, peer_id)!!.uppercase()
            val message_id_hex = fourbytes_of_long_to_hex(message_id)
            val message_timestamp_ms = System.currentTimeMillis()
            val peernum = tox_group_peer_by_public_key(group_number, tox_peerpk)
            val peer_role = tox_group_peer_get_role(group_number, peernum)
            var fname = tox_group_peer_get_name(group_number, peernum)
            if (fname == null)
            {
                fname = ""
            }
            val msg_dbid = received_groupmessage_to_db(tox_peerpk = tox_peerpk!!,
                groupid = group_id,
                is_private_msg = 0,
                rcvd_message_timestamp_ms = message_timestamp_ms,
                sent_message_timestamp_ms = message_timestamp_ms,
                group_message = message_orig, message_id_hex = message_id_hex,
                peer_role = peer_role,
                was_synced = false, peername = fname)
            val peer_user = User(fname + " / " + PubkeyShort(tox_peerpk), picture = "friend_avatar.png", toxpk = tox_peerpk.uppercase(), color = ColorProvider.getColor(true, tox_peerpk.uppercase()))
            groupmessagestore.send(GroupMessageAction.ReceiveGroupMessage(
                UIGroupMessage(
                    was_synced = false,
                    is_private_msg = 0,
                    sentTimeMs = message_timestamp_ms,
                    rcvdTimeMs = message_timestamp_ms,
                    syncdTimeMs = message_timestamp_ms,
                    peer_role = peer_role,
                    msg_id_hash = "",
                    message_id_tox = message_id_hex, msgDatabaseId = msg_dbid,
                    user = peer_user, timeMs = message_timestamp_ms, text = message_orig!!,
                    toxpk = tox_peerpk, groupId = group_id!!.lowercase(),
                    trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, filename_fullpath = null)))
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_private_message_cb_method(group_number: Long, peer_id: Long, a_TOX_MESSAGE_TYPE: Int, message_orig: String?, length: Long)
        {
            val res = tox_group_self_get_peer_id(group_number)
            if (res == peer_id)
            { // do not process our own sent group messages (again)
                return
            }

            val group_id = tox_group_by_groupnum__wrapper(group_number).lowercase()
            val tox_peerpk = tox_group_peer_get_public_key(group_number, peer_id)!!.uppercase()
            val message_id_hex = "" // HINT: no message ID for ngc private messages
            val message_timestamp_ms = System.currentTimeMillis()
            val peernum = tox_group_peer_by_public_key(group_number, tox_peerpk)
            var peer_role = tox_group_peer_get_role(group_number, peernum)
            var fname = tox_group_peer_get_name(group_number, peernum)
            if (fname == null)
            {
                fname = ""
            }
            val msg_dbid = received_groupmessage_to_db(tox_peerpk = tox_peerpk!!,
                is_private_msg = 1,
                groupid = group_id,
                rcvd_message_timestamp_ms = message_timestamp_ms,
                sent_message_timestamp_ms = message_timestamp_ms,
                group_message = message_orig, message_id_hex = message_id_hex,
                was_synced = false, peer_role = peer_role,  peername = fname)
            val peer_user = User(fname + " / " + PubkeyShort(tox_peerpk), picture = "friend_avatar.png", toxpk = tox_peerpk.uppercase(), color = ColorProvider.getColor(true, tox_peerpk.uppercase()))
            groupmessagestore.send(GroupMessageAction.ReceiveGroupMessage(
                UIGroupMessage(
                    was_synced = false,
                    is_private_msg = 1,
                    sentTimeMs = message_timestamp_ms,
                    rcvdTimeMs = message_timestamp_ms,
                    syncdTimeMs = message_timestamp_ms,
                    peer_role = peer_role,
                    msg_id_hash = "",
                    message_id_tox = message_id_hex, msgDatabaseId = msg_dbid,
                    user = peer_user, timeMs = message_timestamp_ms, text = message_orig!!,
                    toxpk = tox_peerpk, groupId = group_id!!.lowercase(),
                    trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, filename_fullpath = null)))
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_privacy_state_cb_method(group_number: Long, a_TOX_GROUP_PRIVACY_STATE: Int)
        {
            try
            {
                val new_privacy_state = a_TOX_GROUP_PRIVACY_STATE
                val group_id = tox_group_by_groupnum__wrapper(group_number)
                val group_name = tox_group_get_name(group_number)
                val group_connection_status = tox_group_is_connected(group_number)
                val group_num_peers = tox_group_peer_count(group_number)
                groupstore.update(item = GroupItem(numPeers = group_num_peers.toInt(), name = group_name!!, isConnected = group_connection_status, groupId = group_id, privacyState = new_privacy_state))
            } catch (_: Exception)
            {
            }
            update_savedata_file_wrapper()
        }

        @JvmStatic
        @Suppress("unused")
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
                    val group_num_peers = tox_group_peer_count(new_group_num)
                    try
                    {
                        val group_new = GroupDB()
                        group_new.group_identifier = group_identifier
                        group_new.privacy_state = new_privacy_state
                        group_new.name = group_name!!
                        group_new.notification_silent = false
                        orma!!.insertIntoGroupDB(group_new)
                    } catch (_: Exception)
                    {
                    }
                    groupstore.add(item = GroupItem(numPeers = group_num_peers.toInt(), name = group_name!!, isConnected = 0, groupId = group_identifier, privacyState = new_privacy_state))
                } catch (_: Exception)
                {
                }
            }
            SnackBarToast("Invited to a new Group")
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_peer_join_cb_method(group_number: Long, peer_id: Long)
        {
            val group_id = tox_group_by_groupnum__wrapper(group_number).lowercase()

            // check if the joining peer is a founder or a moderator
            try
            {
                val peer_role = tox_group_peer_get_role(group_number, peer_id)
                val peer_pubkey = tox_group_peer_get_public_key(group_number, peer_id)
                if (
                    (
                        (peer_role == ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_FOUNDER.value)
                        ||
                        (peer_role == ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_MODERATOR.value)
                    )
                    &&
                    (!peer_pubkey.isNullOrEmpty())
                    )
                {
                    val messages_to_update = orma!!.selectFromGroupMessage().group_identifierEq(group_id)
                        .tox_group_peer_pubkeyEq(peer_pubkey.uppercase())
                        .tox_group_peer_roleEq(-1).toList()
                    val iter = messages_to_update.iterator()

                    orma!!.updateGroupMessage().group_identifierEq(group_id)
                        .tox_group_peer_pubkeyEq(peer_pubkey.uppercase())
                        .tox_group_peer_roleEq(-1)
                        .tox_group_peer_role(peer_role).execute()

                    while(iter.hasNext())
                    {
                        val gm = iter.next()
                        // Log.i(TAG, "GGGGGGG:found just came online:iter:" + gm.group_identifier.take(5) + " " + gm.tox_group_peer_pubkey.take(5) + " " + gm.tox_group_peer_role)
                    }
                }
            }
            catch(_: Exception)
            {
            }

            try
            {
                val new_privacy_state = tox_group_get_privacy_state(group_number)
                val group_name = tox_group_get_name(group_number)
                val group_connection_status = tox_group_is_connected(group_number)
                val group_num_peers = tox_group_peer_count(group_number)
                groupstore.update(item = GroupItem(numPeers = group_num_peers.toInt(), name = group_name!!, isConnected = group_connection_status, groupId = group_id, privacyState = new_privacy_state))
            } catch (_: Exception)
            {
            }

            try
            {
                if (groupstore.stateFlow.value.selectedGroupId == group_id)
                {
                    val ip_addr_str = get_group_peer_ip_str(group_number, peer_id)
                    // Log.i(TAG, "android_tox_callback_group_peer_join_cb_method: ip=" + ip_addr_str)
                    val peer_pubkey = tox_group_peer_get_public_key(group_number, peer_id)
                    var peer_name = tox_group_peer_get_name(group_number, peer_id)
                    val peer_connection_status = tox_group_peer_get_connection_status(group_number, peer_id)
                    val peer_role = tox_group_peer_get_role(group_number, peer_id)
                    if ((peer_name == null) || (peer_name.length < 1))
                    {
                        peer_name = "peer " + peer_id
                    }
                    grouppeerstore.update(item = GroupPeerItem(ip_addr = ip_addr_str, groupID = group_id, name = peer_name, connectionStatus = peer_connection_status, pubkey = peer_pubkey!!, peerRole = peer_role))
                }
            } catch (_: Exception)
            {
            }
            val privacy_state = tox_group_get_privacy_state(group_number)
            if (privacy_state == ToxVars.TOX_GROUP_PRIVACY_STATE.TOX_GROUP_PRIVACY_STATE_PUBLIC.value)
            {
                val peer_pubkey = tox_group_peer_get_public_key(group_number, peer_id)
                send_ngch_request(group_id, peer_pubkey)
            }

            update_savedata_file_wrapper()
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_peer_exit_cb_method(group_number: Long, peer_id: Long, a_Tox_Group_Exit_Type: Int)
        {
            try
            {
                val group_id = tox_group_by_groupnum__wrapper(group_number)
                if (groupstore.stateFlow.value.selectedGroupId == group_id)
                {
                    val peer_pubkey = tox_group_peer_get_public_key(group_number, peer_id)
                    grouppeerstore.remove(item = GroupPeerItem(
                        groupID = group_id, name = "",
                        connectionStatus = 0, pubkey = peer_pubkey!!,
                        peerRole = 2))
                }
            } catch (_: Exception)
            {
            }

            try
            {
                val new_privacy_state = tox_group_get_privacy_state(group_number)
                val group_id = tox_group_by_groupnum__wrapper(group_number)
                val group_name = tox_group_get_name(group_number)
                val group_connection_status = tox_group_is_connected(group_number)
                val group_num_peers = tox_group_peer_count(group_number)
                groupstore.update(item = GroupItem(numPeers = group_num_peers.toInt(), name = group_name!!, isConnected = group_connection_status, groupId = group_id, privacyState = new_privacy_state))
            } catch (_: Exception)
            {
            }
            update_savedata_file_wrapper()
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_peer_name_cb_method(group_number: Long, peer_id: Long)
        {
            try
            {
                val group_id = tox_group_by_groupnum__wrapper(group_number)
                if (groupstore.stateFlow.value.selectedGroupId == group_id)
                {
                    val ip_addr_str = get_group_peer_ip_str(group_number, peer_id)
                    val peer_pubkey = tox_group_peer_get_public_key(group_number, peer_id)
                    var peer_name = tox_group_peer_get_name(group_number, peer_id)
                    val peer_connection_status = tox_group_peer_get_connection_status(group_number, peer_id)
                    val peer_role = tox_group_peer_get_role(group_number, peer_id)
                    if ((peer_name == null) || (peer_name.length < 1))
                    {
                        peer_name = "peer " + peer_id
                    }
                    grouppeerstore.update(item = GroupPeerItem(ip_addr = ip_addr_str, groupID = group_id, name = peer_name, connectionStatus = peer_connection_status, pubkey = peer_pubkey!!, peerRole = peer_role))
                }
            } catch (_: Exception)
            {
            }
            update_savedata_file_wrapper()
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_join_fail_cb_method(group_number: Long, a_Tox_Group_Join_Fail: Int)
        {
            try
            {
                val new_privacy_state = tox_group_get_privacy_state(group_number)
                val group_id = tox_group_by_groupnum__wrapper(group_number)
                val group_name = tox_group_get_name(group_number)
                val group_connection_status = tox_group_is_connected(group_number)
                val group_num_peers = tox_group_peer_count(group_number)
                groupstore.update(item = GroupItem(numPeers = group_num_peers.toInt(), name = group_name!!, isConnected = group_connection_status, groupId = group_id, privacyState = new_privacy_state))
            } catch (_: Exception)
            {
            }
            update_savedata_file_wrapper()
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_self_join_cb_method(group_number: Long)
        {
            try
            {
                val new_privacy_state = tox_group_get_privacy_state(group_number)
                val group_id = tox_group_by_groupnum__wrapper(group_number)
                val group_name = tox_group_get_name(group_number)
                val group_connection_status = tox_group_is_connected(group_number)
                val group_num_peers = tox_group_peer_count(group_number)
                groupstore.update(item = GroupItem(numPeers = group_num_peers.toInt(), name = group_name!!, isConnected = group_connection_status, groupId = group_id, privacyState = new_privacy_state))
            } catch (_: Exception)
            {
            }
            update_savedata_file_wrapper()
            SnackBarToast("You joined a Group")
        }

        @OptIn(DelicateCoroutinesApi::class)
        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_moderation_cb_method(group_number: Long, source_peer_id: Long, target_peer_id: Long, a_Tox_Group_Mod_Event: Int)
        {
            // ** this happens non stop, so don't save here ** // update_savedata_file_wrapper()
            try
            {
                // Log.i(TAG, "android_tox_callback_group_moderation_cb_method:1: " + tox_group_get_name(group_number) + " s_peer:" + source_peer_id + " t_peer:" + target_peer_id + " " + ToxVars.Tox_Group_Mod_Event.value_str(a_Tox_Group_Mod_Event))
                // val peername_src = tox_group_peer_get_name(group_number, source_peer_id)
                // val peername_dst = tox_group_peer_get_name(group_number, target_peer_id)
                // Log.i(TAG, "android_tox_callback_group_moderation_cb_method:1: peername_src=" + peername_src + " peername_dst=" + peername_dst)
            }
            catch(_: Exception)
            {
            }
            if (target_peer_id != UINT32_MAX_JAVA)
            {
                GlobalScope.launch(Dispatchers.IO) {
                    try
                    {
                        try
                        {
                            Log.i(TAG, "android_tox_callback_group_moderation_cb_method:2: " + tox_group_get_name(group_number) + " " + ToxVars.Tox_Group_Mod_Event.value_str(a_Tox_Group_Mod_Event))
                        }
                        catch(_: Exception)
                        {
                        }
                        val group_id = tox_group_by_groupnum__wrapper(group_number)
                        if (group_id != null)
                        {
                            HelperGeneric.force_update_group_peerlist_ui(group_id)
                        }
                    } catch (_: Exception)
                    {
                    }
                }
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_connection_status_cb_method(group_number: Long, a_TOX_GROUP_CONNECTION_STATUS: Int)
        {
            try
            {
                val new_privacy_state = tox_group_get_privacy_state(group_number)
                val group_id = tox_group_by_groupnum__wrapper(group_number)
                val group_name = tox_group_get_name(group_number)
                val group_num_peers = tox_group_peer_count(group_number)
                groupstore.update(item = GroupItem(numPeers = group_num_peers.toInt(), name = group_name!!, isConnected = a_TOX_GROUP_CONNECTION_STATUS, groupId = group_id, privacyState = new_privacy_state))
            } catch (_: Exception)
            {
            }
            update_savedata_file_wrapper()
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_topic_cb_method(group_number: Long, peer_id: Long, topic: String?, topic_length: Long)
        {
            update_savedata_file_wrapper()
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_custom_packet_cb_method(group_number: Long, peer_id: Long, data: ByteArray?, length: Long)
        {
            if (data == null)
            {
                return
            }

            try
            {
                val res = tox_group_self_get_peer_id(group_number)
                if (res == peer_id)
                {
                    // HINT: ignore own packets
                    Log.i(TAG, "group_custom_packet_cb:gn=$group_number peerid=$peer_id ignoring own packet")
                    return
                }
            } catch (_: java.lang.Exception)
            {
            }

            // check for correct signature of packets
            val header_ngc_audio_v1 = (6 + 1 + 1 + 1 + 1).toLong()
            val header_ngc_video_v1 = (6 + 1 + 1 + 1 + 1 + 1).toLong()
            val header_ngc_video_v2 = (6 + 1 + 1 + 1 + 1 + 1 + 2 + 1).toLong()
            val header_ngc_histsync_and_files = (6 + 1 + 1 + 32 + 32 + 4 + 25 + 255).toLong()
            val header_ngc_files = (6 + 1 + 1 + 32 + 4 + 255).toLong()
            if (length <= TOX_MAX_NGC_FILE_AND_HEADER_SIZE && length >= header_ngc_files + 1)
            {
                // @formatter:off
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
                        val tox_peerpk = tox_group_peer_get_public_key(group_number, peer_id)!!.uppercase()
                        val peernum = tox_group_peer_by_public_key(group_number, tox_peerpk)
                        var tox_peer_name = tox_group_peer_get_name(group_number, peernum)
                        if (tox_peer_name == null)
                        {
                            tox_peer_name = "peer " + peernum
                        }
                        val hash_bytes = ByteBuffer.allocateDirect(TOX_HASH_LENGTH)
                        hash_bytes.put(data, 8, 32)
                        val message_id_hash = bytebuffer_to_hexstring(hash_bytes, true)

                        val msg_timestamp = System.currentTimeMillis()
                        val incoming_group_file_meta_data =
                            handle_incoming_group_file(
                                message_id_hash,
                                tox_peer_name,
                                tox_peerpk,
                                false, msg_timestamp, group_number, peer_id, data,
                                length, header_ngc_files)

                        if (incoming_group_file_meta_data != null)
                        {
                            add_ngc_incoming_file_to_ui(incoming_group_file_meta_data,
                                msg_timestamp, tox_peer_name, tox_peerpk,
                                incoming_group_file_meta_data.msg_id_hash,
                                group_id, false)
                        }
                    } else
                    {
                        // Log.i(TAG, "group_custom_packet_cb:wrong signature 2");
                    }
                } else
                {
                    // Log.i(TAG, "group_custom_packet_cb:wrong signature 1");
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
                    {
                        // disable ngc video version 1 -----------
                    } else if (data[6] == 0x02.toByte() && data[7] == 0x21.toByte() && data[8] == 480.toByte() && data[9] == 640.toByte() && data[10] == 1.toByte() && length >= header_ngc_video_v2 + 1)
                    {
                        // Log.i(TAG, "group_custom_packet_cb:video_v2:length=" + length)
                        show_ngc_incoming_video_frame_v2(group_number, peer_id, data, length)
                    } else
                    {
                        // Log.i(TAG, "group_custom_packet_cb:wrong signature 2");
                    }
                } else
                {
                    // Log.i(TAG, "group_custom_packet_cb:wrong signature 1");
                }
            }

            if (length <= TOX_MAX_NGC_FILE_AND_HEADER_SIZE && length >= header_ngc_audio_v1 + 1)
            {
                // @formatter:off
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
                    {
                        play_ngc_incoming_audio_frame(group_number, peer_id, data, length)
                    } else
                    {
                    }
                } else
                {
                }
            }
        }

        @JvmStatic
        @Suppress("unused")
        fun android_tox_callback_group_custom_private_packet_cb_method(group_number: Long, peer_id: Long, data: ByteArray?, length: Long)
        {
            try
            {
                val res = tox_group_self_get_peer_id(group_number)
                if (res == peer_id)
                {
                    // HINT: ignore own packets
                    Log.i(TAG, "group_custom_private_packet_cb:gn=$group_number peerid=$peer_id ignoring own packet")
                    return
                }
            } catch (_: java.lang.Exception)
            {
            }
            // check for correct signature of packets
            val header = (6 + 1 + 1).toLong()
            if (length > TOX_MAX_NGC_FILE_AND_HEADER_SIZE || length < header)
            {
                Log.i(TAG, "group_custom_private_packet_cb: data length has wrong size: $length")
                return
            }
            // @formatter:off
            /*
            | what      | Length in bytes| Contents                                           |
            |------     |--------        |------------------                                  |
            | magic     |       6        |  0x667788113435                                    |
            | version   |       1        |  0x01                                              |
             */
            // @formatter:on
            // @formatter:off
            /*
            | what      | Length in bytes| Contents                                           |
            |------     |--------        |------------------                                  |
            | magic     |       6        |  0x667788113435                                    |
            | version   |       1        |  0x01                                              |
             */
            // @formatter:on
            if (data!![0] == 0x66.toByte() && data[1] == 0x77.toByte() && data[2] == 0x88.toByte() && data[3] == 0x11.toByte() && data[4] == 0x34.toByte() && data[5] == 0x35.toByte())
            {
                if (data[6] == 0x1.toByte() && data[7] == 0x1.toByte())
                {
                    // Log.i(TAG, "group_custom_private_packet_cb: got ngch_request");
                    val privacy_state = tox_group_get_privacy_state(group_number)
                    if (privacy_state == ToxVars.TOX_GROUP_PRIVACY_STATE.TOX_GROUP_PRIVACY_STATE_PUBLIC.value)
                    {
                        sync_group_message_history(group_number, peer_id)
                    } else
                    {
                        Log.i(TAG, "group_custom_private_packet_cb: only sync history for public groups!")
                    }
                } else if (data[6] == 0x1.toByte() && data[7] == 0x2.toByte())
                {
                    val header_syncmsg = 6 + 1 + 1 + 4 + 32 + 4 + 25
                    if (length >= header_syncmsg + 1)
                    {
                        // Log.i(TAG, "group_custom_private_packet_cb: got ngch_syncmsg");
                        handle_incoming_sync_group_message(group_number, peer_id, data, length)
                    }
                } else if (data[6] == 0x1.toByte() && data[7] == 0x3.toByte())
                {
                    val header_syncfile = 6 + 1 + 1 + 32 + 32 + 4 + 25 + 255
                    if (length >= header_syncfile + 1)
                    {
                        Log.i(TAG, "group_custom_private_packet_cb: got ngch_syncfile")
                        handle_incoming_sync_group_file(group_number, peer_id, data, length)
                    }
                }
            }
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

        /*
         * put an incoming 1-on-1 text message into the database
         */
        fun received_message_to_db(toxpk: String?,
                                   msg_version: Int,
                                   msg_id_hash: String,
                                   msg_idv3_hash: String,
                                   sent_message_timestamp_ms: Long,
                                   rcvd_message_timestamp_ms: Long,
                                   friend_message: String?): Long
        {
            val m = com.zoffcc.applications.sorm.Message()
            m.tox_friendpubkey = toxpk
            m.direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value // msg received
            m.TOX_MESSAGE_TYPE = 0
            m.read = false
            m.is_new = true
            if ((contactstore.state.visible) && (contactstore.state.selectedContactPubkey == toxpk))
            {
                m.read = true
                m.is_new = false
            }
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value
            m.rcvd_timestamp = rcvd_message_timestamp_ms
            m.rcvd_timestamp_ms = 0
            m.sent_timestamp = sent_message_timestamp_ms
            m.sent_timestamp_ms = 0
            m.text = friend_message
            m.msg_version = msg_version
            m.msg_idv3_hash = msg_idv3_hash
            m.msg_id_hash = msg_id_hash
            m.sent_push = 0
            var row_id: Long = -1
            try
            {
                row_id = orma!!.insertIntoMessage(m)
                if ((!globalstore.isFocused()) || (globalstore.isMinimized()) || (!contactstore.state.visible) || (contactstore.state.selectedContactPubkey != toxpk))
                {
                    var fname: String? = ""
                    try
                    {
                        fname = " from " + tox_friend_get_name(tox_friend_by_public_key(toxpk))
                    }
                    catch (e2: Exception)
                    {
                        e2.printStackTrace()
                    }
                    HelperNotification.displayNotification("new Message" + fname)
                    globalstore.increase_unread_message_count()
                    globalfrndstoreunreadmsgs.increase_unread_per_friend_message_count(toxpk!!)
                }
            } catch (_: Exception)
            {
            }

            return row_id
        }

        fun received_groupmessage_to_db(tox_peerpk: String, is_private_msg: Int, groupid: String, rcvd_message_timestamp_ms: Long, sent_message_timestamp_ms: Long, group_message: String?, message_id_hex: String, was_synced: Boolean, peername: String, peer_role: Int): Long
        {
            val groupnum = tox_group_by_groupid__wrapper(groupid)
            val peernum = tox_group_peer_by_public_key(groupnum, tox_peerpk)
            val m = GroupMessage()
            m.tox_group_peer_pubkey = tox_peerpk
            m.direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value // msg received
            m.TOX_MESSAGE_TYPE = 0
            m.tox_group_peer_role = peer_role
            m.read = false
            m.is_new = true
            if ((groupstore.state.visible) && (groupstore.state.selectedGroupId == groupid))
            {
                m.read = true
                m.is_new = false
            }
            if (peername == null)
            {
                m.tox_group_peername = ""
            }
            else
            {
                m.tox_group_peername = peername
            }
            m.private_message = is_private_msg
            m.group_identifier = groupid.lowercase()
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value
            m.rcvd_timestamp = rcvd_message_timestamp_ms
            m.sent_timestamp = sent_message_timestamp_ms
            m.text = group_message
            m.message_id_tox = message_id_hex
            m.was_synced = was_synced
            var row_id: Long = -1
            try
            {
                row_id = orma!!.insertIntoGroupMessage(m)
                if ((!globalstore.isFocused()) || (globalstore.isMinimized()) || (!groupstore.state.visible) || (groupstore.state.selectedGroupId != groupid))
                {
                    var grptitle: String? = ""
                    try
                    {
                        val group_title = tox_group_get_name(groupnum)
                        if ((group_title != null) && (group_title.length > 0))
                        {
                            grptitle = " in " + group_title
                        }
                    }
                    catch(_: Exception)
                    {
                    }

                    var group_notification_silent = false
                    try {
                        if (orma!!.selectFromGroupDB().group_identifierEq(groupid.lowercase()).get(0).notification_silent) {
                            group_notification_silent = true
                        }
                    } catch (_: Exception) {
                    }
                    if (group_notification_silent == false)
                    {
                        HelperNotification.displayNotification("new Group Message" + grptitle)
                    }
                    globalstore.increase_unread_group_message_count()
                    globalgrpstoreunreadmsgs.increase_unread_per_group_message_count(groupid.lowercase())
                }
            } catch (_: Exception)
            {
            }

            return row_id
        }

        fun incoming_synced_group_text_msg(m: GroupMessage)
        {
            val msg_dbid = received_groupmessage_to_db(tox_peerpk = m.tox_group_peer_pubkey!!, groupid = m.group_identifier,
                rcvd_message_timestamp_ms = m.rcvd_timestamp,
                sent_message_timestamp_ms = m.sent_timestamp,
                is_private_msg = m.private_message,
                group_message = m.text, message_id_hex = m.message_id_tox, was_synced = m.was_synced,
                peer_role = m.tox_group_peer_role,
                peername = m.tox_group_peername)
            val peer_user = User(m.tox_group_peername + " / " + PubkeyShort(m.tox_group_peer_pubkey), picture = "friend_avatar.png", toxpk = m.tox_group_peer_pubkey.uppercase(), color = ColorProvider.getColor(true, m.tox_group_peer_pubkey.uppercase()))

            groupmessagestore.send(GroupMessageAction.ReceiveGroupMessage(
                UIGroupMessage(
                    was_synced = true,
                    is_private_msg = m.private_message,
                    sentTimeMs = m.sent_timestamp,
                    rcvdTimeMs = m.rcvd_timestamp,
                    syncdTimeMs = m.rcvd_timestamp,
                    peer_role = m.tox_group_peer_role,
                    msg_id_hash = m.msg_id_hash,
                    message_id_tox = m.message_id_tox, msgDatabaseId = msg_dbid,
                    user = peer_user, timeMs = m.sent_timestamp, text = m.text,
                    toxpk = m.tox_group_peer_pubkey, groupId = m.group_identifier,
                    trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value, filename_fullpath = null)))
        }

        class outgoing_file_wrapped
        {
            var filepath_wrapped: String? = null
            var filename_wrapped: String? = null
            var file_size_wrapped: Long = -1
        }

        fun add_ngc_incoming_file_to_ui(file_meta_data: HelperGroup.incoming_group_file_meta_data,
                               msg_timestamp: Long,
                               peer_name: String,
                               tox_peerpk: String,
                               msg_id_hash: String,
                               group_id: String,
                               was_synced: Boolean)
        {
            val peer_user = User(peer_name + " / " +
                    PubkeyShort(tox_peerpk), picture = "friend_avatar.png",
                toxpk = tox_peerpk.uppercase(),
                color = ColorProvider.getColor(true, tox_peerpk.uppercase()))

            var peer_role = -1
            try
            {
                val group_number = tox_group_by_groupid__wrapper(group_id)
                val peernum = tox_group_peer_by_public_key(group_number, tox_peerpk)
                val peer_role_get = tox_group_peer_get_role(group_number, peernum)
                if (peer_role_get >= 0)
                {
                    peer_role = peer_role_get
                }
            }
            catch(_: Exception)
            {
            }

            groupmessagestore.send(GroupMessageAction.ReceiveGroupMessage(
                UIGroupMessage(
                    is_private_msg = 0,
                    was_synced = was_synced,
                    sentTimeMs = msg_timestamp,
                    rcvdTimeMs = msg_timestamp,
                    syncdTimeMs = msg_timestamp,
                    msg_id_hash = msg_id_hash,
                    peer_role = peer_role,
                    message_id_tox = "", msgDatabaseId = file_meta_data.rowid,
                    user = peer_user, timeMs = msg_timestamp,
                    text = file_meta_data.message_text,
                    toxpk = tox_peerpk, groupId = group_id.lowercase(),
                    trifaMsgType = TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value,
                    filename_fullpath = file_meta_data.path_name +
                            file_meta_data.file_name)))
        }

        fun add_ngc_outgoing_file(filepath: String?, filename: String?, groupid: String?)
        {
            if ((filepath == null) || (filename == null) || (groupid == null))
            {
                Log.i(TAG, "add_outgoing_file:filepath or filename or friend_pubkey is null")
                return
            }

            var file_size: Long = -1
            file_size = try {
                File(filepath + File.separator + filename).length()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                // file length unknown?
                return
            }
            if (file_size < 1) {
                // file length "zero"?
                return
            }

            var ofw = outgoing_file_wrapped()
            ofw.file_size_wrapped = file_size
            ofw.filepath_wrapped = filepath
            ofw.filename_wrapped = filename

            if (file_size > TOX_MAX_NGC_FILESIZE_IGNORE_OVER_THAT)
            {
                Log.i(TAG, "add_outgoing_file:too large to process, size=" + ofw.file_size_wrapped)
                SnackBarToast("Error sending file to group, file is too large to process")
                return
            }

            if (file_size > TOX_MAX_NGC_FILESIZE)
            {
                // reducing the file size down to hopefully 37kbytes -------------------
                Log.i(TAG, "add_outgoing_file:shrink:start")
                ofw = shrink_image_file(ofw, groupid)
                Log.i(TAG, "add_outgoing_file:shrink:done:")
                // reducing the file size down to hopefully 37kbytes -------------------
                if (ofw.file_size_wrapped > TOX_MAX_NGC_FILESIZE)
                {
                    Log.i(TAG, "add_outgoing_file:failed to shrink file, size=" + ofw.file_size_wrapped)
                    SnackBarToast("Error sending file to group, failed to shrink to allowed size")
                    return
                }
            }
            else
            {
                Log.i(TAG, "add_outgoing_file:no_need_to_shrink_file")
                val filename_out = get_incoming_filetransfer_local_filename(
                    ofw.filename_wrapped, groupid.lowercase())
                val filename_out_with_path = VFS_FILE_DIR + File.separator + groupid.lowercase() +
                        File.separator + filename_out
                ofw.filepath_wrapped = File(filename_out_with_path).parent
                ofw.filename_wrapped = File(filename_out_with_path).name
                io_file_copy(File(filepath + File.separator + filename), File(filename_out_with_path))
            }
            Log.i(TAG, "add_outgoing_file:001")
            val group_num = tox_group_by_groupid__wrapper(groupid)
            val self_peernum = tox_group_self_get_peer_id(group_num)
            val self_name_in_group = tox_group_peer_get_name(group_num , self_peernum)
            // add FT message to UI
            val m = GroupMessage()
            m.tox_group_peer_pubkey = tox_group_self_get_public_key(group_num)!!.uppercase()
            m.direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value
            m.TOX_MESSAGE_TYPE = 0
            m.read = true // !!!! there is no "read status" with conferences in Tox !!!!
            m.is_new = false // own messages are always "not new"
            if (self_name_in_group == null)
            {
                m.tox_group_peername = ""
            }
            else
            {
                m.tox_group_peername = self_name_in_group
            }
            m.private_message = 0
            m.group_identifier = groupid.lowercase()
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value
            m.sent_timestamp = System.currentTimeMillis()
            m.rcvd_timestamp = System.currentTimeMillis() // since we do not have anything better assume "now"
            m.text = (ofw.filename_wrapped + "\n" + ofw.file_size_wrapped).toString() + " bytes"
            m.was_synced = false
            m.path_name = ofw.filepath_wrapped
            m.file_name = ofw.filename_wrapped
            m.filename_fullpath = File(ofw.filepath_wrapped + "/" + ofw.filename_wrapped).absolutePath
            try
            {
                m.filesize = File(ofw.filepath_wrapped + "/" + ofw.filename_wrapped).length()
            } catch (ee: java.lang.Exception)
            {
                m.filesize = 0
            }
            val hash_bytes = ByteBuffer.allocateDirect(TOX_HASH_LENGTH)
            tox_messagev3_get_new_message_id(hash_bytes)
            m.msg_id_hash = bytebuffer_to_hexstring(hash_bytes, true)
            m.message_id_tox = ""
            var row_id: Long = -1
            try
            {
                row_id = orma!!.insertIntoGroupMessage(m)
            } catch (_: Exception)
            {
            }

            m.tox_group_peer_role = -1
            try
            {
                val self_peer_role = tox_group_self_get_role(group_num)
                if (self_peer_role >= 0)
                {
                    m.tox_group_peer_role = self_peer_role
                }
            } catch (_: Exception)
            {
            }


            Log.i(TAG, "add_outgoing_file:090")
            // now send the file to the group as custom package ----------
            Log.i(TAG, "add_outgoing_file:091:send_group_image:start")
            val res = send_group_image(m)
            Log.i(TAG, "add_outgoing_file:092:send_group_image:done")
            // now send the file to the group as custom package ----------

            // update UI
            groupmessagestore.send(
                GroupMessageAction.SendGroupMessage(
                UIGroupMessage(
                    was_synced = m.was_synced,
                    is_private_msg = m.private_message,
                    sentTimeMs = m.sent_timestamp,
                    rcvdTimeMs = m.rcvd_timestamp,
                    syncdTimeMs = m.sent_timestamp,
                    msg_id_hash = m.msg_id_hash,
                    peer_role = m.tox_group_peer_role,
                    message_id_tox = m.message_id_tox, msgDatabaseId = row_id,
                    user = myUser, timeMs = m.sent_timestamp, text = m.text,
                    toxpk = m.tox_group_peer_pubkey, groupId = m.group_identifier,
                    trifaMsgType = m.TRIFA_MESSAGE_TYPE,
                    filename_fullpath = m.filename_fullpath)))
        }

        fun add_outgoing_file(filepath: String?, filename: String?, friend_pubkey: String?)
        {
            Log.i(TAG, "add_outgoing_file:regular file")
            if ((filepath == null) || (filename == null) || (friend_pubkey == null))
            {
                Log.i(TAG, "add_outgoing_file:filepath or filename or friend_pubkey is null")
                return
            }

            var file_size: Long = -1
            file_size = try {
                File(filepath + File.separator + filename).length()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                // file length unknown?
                return
            }
            if (file_size < 1) {
                // file length "zero"?
                return
            }
            val friendnum = tox_friend_by_public_key(friend_pubkey)
            Log.i(TAG, "add_outgoing_file:friendnum(2)=$friendnum")
            val f = Filetransfer()
            f.tox_public_key_string = friend_pubkey
            f.direction = TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value
            f.file_number = -1 // add later when we actually have the number
            f.kind = ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA.value
            f.state = ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE.value
            f.path_name = filepath
            f.file_name = filename
            f.filesize = file_size
            f.ft_accepted = false
            f.ft_outgoing_started = false
            f.current_position = 0
            Log.i(TAG, "add_outgoing_file:tox_public_key_string=" + f.tox_public_key_string)
            val ft_id = insert_into_filetransfer_db(f)
            f.id = ft_id
            // Message m_tmp = orma.selectFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(3)).orderByMessage_idDesc().get(0);
            Log.i(TAG, "add_outgoing_file:MM2MM:2:$ft_id")
            // ---------- DEBUG ----------
            val ft_tmp = orma!!.selectFromFiletransfer().idEq(ft_id).toList()[0]
            Log.i(TAG, "add_outgoing_file:MM2MM:4a:" + "fid=" + ft_tmp.id + " mid=" + ft_tmp.message_id)
            // ---------- DEBUG ----------
            // add FT message to UI
            val m = Message()
            m.tox_friendpubkey = friend_pubkey
            m.direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value // msg outgoing
            m.TOX_MESSAGE_TYPE = 0
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value
            m.filetransfer_id = ft_id
            m.filedb_id = -1
            m.state = ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE.value
            m.ft_accepted = false
            m.ft_outgoing_started = false
            m.ft_outgoing_queued = false
            m.filename_fullpath = File(filepath + File.separator + filename).absolutePath
            m.sent_timestamp = System.currentTimeMillis()
            m.text = filename + "\n" + file_size + " bytes"
            m.is_new = false // no notification for outgoing filetransfers
            m.filetransfer_kind = ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA.value
            m.read = false
            var new_msg_id: Long = -1
            try
            {
                new_msg_id = orma!!.insertIntoMessage(m)
            } catch (_: Exception)
            {
            }
            m.id = new_msg_id
            // ---------- DEBUG ----------
            Log.i(TAG, "add_outgoing_file:MM2MM:3:$new_msg_id")
            var m_tmp = orma!!.selectFromMessage().idEq(new_msg_id).toList()[0]
            Log.i(TAG, "add_outgoing_file:MM2MM:4:" + m.filetransfer_id + "::" + m_tmp)
            // ---------- DEBUG ----------
            f.message_id = new_msg_id
            update_filetransfer_db_full(f)
            // ---------- DEBUG ----------
            val ft_tmp2 = orma!!.selectFromFiletransfer().idEq(ft_id).toList()[0]
            Log.i(TAG, "add_outgoing_file:MM2MM:4b:" + "fid=" + ft_tmp2.id + " mid=" + ft_tmp2.message_id)
            // ---------- DEBUG ----------
            // ---------- DEBUG ----------
            m_tmp = orma!!.selectFromMessage().idEq(new_msg_id).toList()[0]
            Log.i(TAG, "add_outgoing_file:MM2MM:5:" + m.filetransfer_id + "::" + m_tmp)
            messagestore.send(MessageAction.SendMessage(UIMessage(
                direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value,
                msgDatabaseId = new_msg_id, user = myUser,
                timeMs = m.sent_timestamp,
                read = m.read,
                sent_push = m.sent_push,
                msg_id_hash = "",
                msg_idv3_hash = "",
                msg_version = 0,
                recvTimeMs = m.rcvd_timestamp,
                sentTimeMs = m.sent_timestamp,
                text = m.text,
                toxpk = friend_pubkey.uppercase(), trifaMsgType = m.TRIFA_MESSAGE_TYPE,
                file_state = m.state,
                filename_fullpath = m.filename_fullpath)))
        }

        fun sent_groupmessage_to_db(groupid: String, message_timestamp: Long, group_message: String?, message_id: Long, was_synced: Boolean): Long
        {
            val message_id_hex = fourbytes_of_long_to_hex(message_id)
            val groupnum = tox_group_by_groupid__wrapper(groupid)
            val peernum = tox_group_self_get_peer_id(groupnum)
            val peername = tox_group_peer_get_name(groupnum, peernum)
            val m = com.zoffcc.applications.sorm.GroupMessage()
            m.tox_group_peer_pubkey = tox_group_self_get_public_key(tox_group_by_groupid__wrapper(groupid))!!.uppercase()
            m.direction = TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value // msg sent
            m.TOX_MESSAGE_TYPE = 0
            m.read = true
            m.is_new = false
            if (peername == null)
            {
                m.tox_group_peername = ""
            }
            else
            {
                m.tox_group_peername = peername
            }
            m.private_message = 0
            m.group_identifier = groupid
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value
            m.sent_timestamp = System.currentTimeMillis()
            m.rcvd_timestamp = System.currentTimeMillis() // since we do not have anything better assume "now"
            m.text = group_message
            m.was_synced = was_synced
            m.message_id_tox = message_id_hex
            var row_id: Long = -1
            try
            {
                row_id = orma!!.insertIntoGroupMessage(m)
            } catch (_: Exception)
            {
            }

            return row_id
        }

        fun get_friend_ip_str(friend_pubkey: String?): String
        {
            if (friend_pubkey.isNullOrEmpty())
            {
                return ""
            }

            try
            {
                val friend_num : Long = tox_friend_by_public_key(friend_pubkey)
                if (friend_num != -1L)
                {
                    return get_friend_ip_str(friend_num)
                }
            }
            catch(_: Exception)
            {
                return ""
            }
            return ""
        }

        fun get_friend_ip_str(friend_number: Long): String
        {
            val friend_ip_addresses = tox_friend_get_connection_ip(friend_number)
            var ip_addr_str = ""
            try
            {
                val ip_addr = friend_ip_addresses!!.toByteArray(StandardCharsets.UTF_8).filterNot { it == 0.toByte() }.toByteArray()
                ip_addr_str = ip_addr.toString(StandardCharsets.UTF_8)
            } catch (e: Exception)
            {
            }
            return ip_addr_str
        }

        fun get_group_peer_ip_str(group_number: Long, peer_id: Long): String
        {
            val group_peer_ip_addresses = tox_group_get_peer_connection_ip(group_number, peer_id)
            var ip_addr_str = ""
            try
            {
                val ip_addr = group_peer_ip_addresses!!.toByteArray(StandardCharsets.UTF_8).filterNot { it == 0.toByte() }.toByteArray()
                ip_addr_str = ip_addr.toString(StandardCharsets.UTF_8)
            } catch (_: Exception)
            {
            }
            return ip_addr_str
        }

        @JvmStatic fun modify_message_with_ft(message: Message, filetransfer: Filetransfer?)
        {
            messagestore.send(MessageAction.UpdateMessage(message, filetransfer))
        }

        // Warning: this does NOT update all fields of the message!
        @JvmStatic fun modify_text_message(message: Message)
        {
            messagestore.send(MessageAction.UpdateTextMessage(message))
        }

        @JvmStatic fun modify_message_with_finished_ft(message: Message, file_size: Long)
        {
            messagestore.send(MessageAction.UpdateMessage(message, Filetransfer().filesize(file_size)))
        }

        @JvmStatic fun setVideo_play_fps(fps: Int)
        {
            video_play_fps_value = fps
            avstatestorevplayfpsstate.update(fps)
        }

        fun decline_incoming_av_call()
        {
            val calling_friend_pk = avstatestore.state.call_with_friend_pubkey_get()
            if (calling_friend_pk != null)
            {
                val fnum = tox_friend_by_public_key(calling_friend_pk)
                if (fnum != -1L)
                {
                    toxav_call_control(fnum, ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value)
                }
            }
            avstatestore.state.calling_state_set(AVState.CALL_STATUS.CALL_STATUS_NONE)
            avstatestore.state.call_with_friend_pubkey_set(null)
        }

        fun set_toxav_video_sending_quality(video_bitrate_mode: Int)
        {
            if (avstatestore.state.call_with_friend_pubkey_get() != null)
            {
                val friendnum = tox_friend_by_public_key(avstatestore.state.call_with_friend_pubkey_get())
                if (friendnum != -1L)
                {
                    if (PREF__video_bitrate_mode == 0)
                    {
                        toxav_option_set(friendnum,
                            ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MIN_BITRATE.value.toLong(),
                            ABSOLUTE_MINIMUM_GLOBAL_VIDEO_BITRATE.toLong())
                        toxav_option_set(friendnum,
                            ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value.toLong(),
                            LOWER_GLOBAL_VIDEO_BITRATE.toLong())
                        toxav_option_set(friendnum,
                            ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_DECODER_VIDEO_BUFFER_MS.value.toLong(),
                            HIGH_GLOBAL_INCOMING_AV_BUFFER_MS.toLong())
                    }
                    else if (PREF__video_bitrate_mode == 1)
                    {
                        toxav_option_set(friendnum,
                            ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MIN_BITRATE.value.toLong(),
                            ABSOLUTE_MINIMUM_GLOBAL_VIDEO_BITRATE.toLong())
                        toxav_option_set(friendnum,
                            ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value.toLong(),
                            MEDIUM_GLOBAL_VIDEO_BITRATE.toLong())
                        toxav_option_set(friendnum,
                            ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_DECODER_VIDEO_BUFFER_MS.value.toLong(),
                            NORMAL_GLOBAL_INCOMING_AV_BUFFER_MS.toLong())
                    }
                    else // PREF__video_bitrate_mode == 2
                    {
                        if (avstatestore.state.video_in_resolution_get().equals("1920x1080"))
                        {
                            toxav_option_set(friendnum,
                                ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MIN_BITRATE.value.toLong(),
                                LOWER_GLOBAL_VIDEO_BITRATE.toLong())
                            toxav_option_set(friendnum,
                                ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value.toLong(),
                                HIGHER_GLOBAL_VIDEO_BITRATE.toLong())
                        }
                        else
                        {
                            toxav_option_set(friendnum,
                                ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MIN_BITRATE.value.toLong(),
                                ABSOLUTE_MINIMUM_GLOBAL_VIDEO_BITRATE.toLong())
                            toxav_option_set(friendnum,
                                ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value.toLong(),
                                NORMAL_GLOBAL_VIDEO_BITRATE.toLong())
                        }
                        toxav_option_set(friendnum,
                            ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_DECODER_VIDEO_BUFFER_MS.value.toLong(),
                            NORMAL_GLOBAL_INCOMING_AV_BUFFER_MS.toLong())
                    }
                }
                else
                {
                    Log.i(TAG, "set_toxav_video_sending_quality: friend number invalid")
                }
            }
            else
            {
                Log.i(TAG, "set_toxav_video_sending_quality: not in a toxav call")
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        fun accept_incoming_av_call(friendpubkey: String)
        {
            val friend_number = tox_friend_by_public_key(friendpubkey)
            if (friend_number == -1L)
            {
                // friend with this pubkey not found
                return
            }
            val call_answer = toxav_answer(friend_number, GLOBAL_AUDIO_BITRATE.toLong(), GLOBAL_VIDEO_BITRATE.toLong())
            if (call_answer == 1)
            {
                avstatestore.state.calling_state_set(AVState.CALL_STATUS.CALL_STATUS_CALLING)
                avstatestore.state.call_with_friend_pubkey_set(tox_friend_get_public_key(friend_number))
                avstatestore.state.start_av_call()
                GlobalScope.launch {
                    delay(1000)
                    set_toxav_video_sending_quality(PREF__video_bitrate_mode)
                }
            }
        }

        fun shutdown_av_call(friend_number: Long)
        {
            if (avstatestorecallstate.state.call_state == AVState.CALL_STATUS.CALL_STATUS_INCOMING)
            {
                if (avstatestore.state.call_with_friend_pubkey_get() == tox_friend_get_public_key(friend_number))
                {
                    decline_incoming_av_call()
                }
            }
            else if (avstatestorecallstate.state.call_state == AVState.CALL_STATUS.CALL_STATUS_CALLING)
            {
                if (avstatestore.state.call_with_friend_pubkey_get() == tox_friend_get_public_key(friend_number))
                {
                    Log.i(com.zoffcc.applications.trifa.TAG, "ffmpeg_devices_stop:006")
                    avstatestore.state.ffmpeg_devices_stop()
                    toxav_call_control(friend_number, ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value)
                    on_call_ended_actions()
                }
            }
        }

        fun update_toxid_while_running()
        {
            val my_tox_id_temp = get_my_toxid()
            Log.i(TAG, "MyToxID updated:$my_tox_id_temp")
            myUser.toxpk = my_tox_id_temp
            try
            {
                toxdatastore.updateToxID(my_tox_id_temp)
            } catch (_: Exception)
            {
            }
        }
    }
}

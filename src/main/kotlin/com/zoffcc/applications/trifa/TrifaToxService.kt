@file:Suppress("ConvertToStringTemplate", "LocalVariableName", "ReplaceSizeCheckWithIsNotEmpty", "FunctionName", "UnnecessaryVariable")

package com.zoffcc.applications.trifa

import avstatestore
import avstatestorecallstate
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB.bootstrap_node_list
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB.get_tcprelay_nodelist_from_db
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB.get_udp_nodelist_from_db
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB.tcprelay_node_list
import com.zoffcc.applications.sorm.FriendList
import com.zoffcc.applications.sorm.GroupDB
import com.zoffcc.applications.sorm.Message
import com.zoffcc.applications.sorm.OrmaDatabase
import com.zoffcc.applications.sorm.OrmaDatabase.run_multi_sql
import com.zoffcc.applications.sorm.OrmaDatabase.set_schema_upgrade_callback
import com.zoffcc.applications.trifa.HelperFiletransfer.start_outgoing_ft
import com.zoffcc.applications.trifa.HelperFriend.friend_call_push_url
import com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_pubkey
import com.zoffcc.applications.trifa.HelperGeneric.get_friend_msgv3_capability
import com.zoffcc.applications.trifa.HelperGeneric.is_friend_online_real
import com.zoffcc.applications.trifa.HelperGeneric.tox_friend_resend_msgv3_wrapper
import com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper
import com.zoffcc.applications.trifa.HelperGroup.hex_to_bytes
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper
import com.zoffcc.applications.trifa.HelperMessage.tox_friend_send_message_wrapper
import com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_messageid
import com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_no_read_recvedts
import com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_resend_count
import com.zoffcc.applications.trifa.MainActivity.Companion.DB_PREF__send_push_notifications
import com.zoffcc.applications.trifa.MainActivity.Companion.ORMA_CURRENT_DB_SCHEMA_VERSION
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__DB_wal_mode
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__database_files_dir
import com.zoffcc.applications.trifa.MainActivity.Companion.add_tcp_relay_single_wrapper
import com.zoffcc.applications.trifa.MainActivity.Companion.audio_queue_play_trigger
import com.zoffcc.applications.trifa.MainActivity.Companion.bootstrap_single_wrapper
import com.zoffcc.applications.trifa.MainActivity.Companion.db_password
import com.zoffcc.applications.trifa.MainActivity.Companion.get_friend_ip_str
import com.zoffcc.applications.trifa.MainActivity.Companion.get_group_peer_ip_str
import com.zoffcc.applications.trifa.MainActivity.Companion.init_tox_callbacks
import com.zoffcc.applications.trifa.MainActivity.Companion.ngc_audio_in_queue
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_a_queue_stop_trigger
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_audio_in_queue
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_connection_status
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_chat_id
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_grouplist
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_health
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_number_groups
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_peerlist
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_privacy_state
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_is_connected
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_mid_peer_list_count
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_mid_peer_list_get
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_count
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_get_connection_status
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_get_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_get_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_get_role
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_iterate
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_iteration_interval
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_kill
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_reset_estimated_cpu_cycles
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_get_connection_status
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_get_friend_list
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_get_network_health
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_set_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_util_friend_resend_message_v2
import com.zoffcc.applications.trifa.TRIFAGlobals.GROUP_ID_LENGTH
import com.zoffcc.applications.trifa.TRIFAGlobals.MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION
import com.zoffcc.applications.trifa.TRIFAGlobals.TOX_BOOTSTRAP_AGAIN_AFTER_OFFLINE_MILLIS
import com.zoffcc.applications.trifa.TRIFAGlobals.TOX_ITERATE_MS_MIN_NORMAL
import com.zoffcc.applications.trifa.TRIFAGlobals.USE_MAX_NUMBER_OF_BOOTSTRAP_NODES
import com.zoffcc.applications.trifa.TRIFAGlobals.USE_MAX_NUMBER_OF_BOOTSTRAP_TCP_RELAYS
import com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping
import com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_outgoung_ft_ts
import com.zoffcc.applications.trifa.TRIFAGlobals.global_self_connection_status
import com.zoffcc.applications.trifa.TRIFAGlobals.global_self_last_went_offline_timestamp
import contactstore
import globalfrndstoreunreadmsgs
import globalgrpstoreunreadmsgs
import globalstore
import grouppeerstore
import groupstore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import online_button_text_wrapper
import org.briarproject.briar.desktop.contact.ContactItem
import org.briarproject.briar.desktop.contact.GroupItem
import org.briarproject.briar.desktop.contact.GroupPeerItem
import set_tox_group_health
import set_tox_network_health
import set_tox_running_state
import toxdatastore
import unlock_data_dir_input
import java.io.File
import java.nio.ByteBuffer
import java.util.*

class TrifaToxService
{
    fun tox_thread_start_fg()
    {
        Log.i(TAG, "tox_thread_start_fg")
        ToxServiceThread = object : Thread()
        {
            override fun run()
            {
                try
                {
                    Thread.currentThread().name = "t_tox_iter"
                } catch (_: Exception)
                {
                }

                set_schema_upgrade_callback { old_version, new_version ->
                    Log.i(TAG, "REAL:trying to upgrade schema from " + old_version + " to " + new_version)
                    upgrade_db_schema_do(old_version, new_version)
                }

                orma = OrmaDatabase(PREF__database_files_dir + "/main.db", db_password, PREF__DB_wal_mode);
                OrmaDatabase.init(ORMA_CURRENT_DB_SCHEMA_VERSION)
                // ------ correct startup order ------
                globalstore.setOrmaRunning(true)
                var debug__cipher_version: String? = "unknown"
                try
                {
                    debug__cipher_version = OrmaDatabase.run_query_for_single_result("PRAGMA cipher_version")
                } catch (e: java.lang.Exception)
                {
                    e.printStackTrace()
                }
                Log.i(TAG, "debug__cipher_version:" + debug__cipher_version)
                if (debug__cipher_version.isNullOrEmpty())
                {
                    globalstore.setNative_sqlite_type(SQLITE_TYPE.SQLITE)
                }
                else
                {
                    globalstore.setNative_sqlite_type(SQLITE_TYPE.SQLCIPHER)
                }
                load_db_prefs()
                try {
                    globalstore.try_clear_unread_message_count()
                } catch(_: Exception) {
                }

                try {
                    globalstore.try_clear_unread_group_message_count()
                } catch(_: Exception) {
                }

                val old_is_tox_started = is_tox_started
                Log.i(TAG, "is_tox_started:==============================")
                Log.i(TAG, "is_tox_started=" + is_tox_started)
                Log.i(TAG, "is_tox_started:==============================")
                is_tox_started = true
                if (!old_is_tox_started)
                {
                    init_tox_callbacks()
                    update_savedata_file_wrapper()
                } // ------ correct startup order ------

                try
                {
                    Log.i(TAG, "StartupSelfname: " + globalstore.getStartupSelfname())
                    Log.i(TAG, "FirstRun: " + globalstore.isFirstRun())
                    if (globalstore.isFirstRun())
                    {
                        globalstore.updateFirstRun(false)
                        tox_self_set_name(globalstore.getStartupSelfname())
                        update_savedata_file_wrapper()
                    }
                }
                catch(_: Exception)
                {
                }
                clear_friends()
                load_friends()
                clear_groups()
                load_groups()
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------

                ngc_audio_play_thread_running = true
                ngc_audio_play_thread_start()

                tox_audio_play_thread_running = true
                tox_audio_play_thread_start()

                if (!old_is_tox_started)
                {
                    TRIFAGlobals.bootstrapping = true
                    Log.i(TAG, "bootrapping:set to true")
                    bootstrap_me()
                }
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                var tox_iteration_interval_ms = tox_iteration_interval()
                Log.i(TAG, "tox_iteration_interval_ms=$tox_iteration_interval_ms")
                tox_iterate()
                global_self_connection_status == tox_self_get_connection_status()

                val current_health_init: Int = tox_self_get_network_health()
                val health_text_init: String = ToxVars.TOX_NETWORK_HEALTH.value_str(current_health_init).replace("TOX_NETWORK_HEALTH_", "")
                Log.i(TAG, "tox_network_health: " + health_text_init)

                val currenc_gc_health_init: Int = tox_group_get_health()
                val gc_health_text_init: String = ToxVars.TOX_GROUP_HEALTH.value_str(current_health_init).replace("TOX_GROUP_HEALTH_", "")
                Log.i(TAG, "tox_gc_health: " + gc_health_text_init)

                try
                {
                    set_tox_network_health(current_health_init)
                } catch (e: java.lang.Exception)
                {
                }
                var last_health_check_ms: Long = 0
                var last_network_health = current_health_init

                var last_gc_health = currenc_gc_health_init
                tox_reset_estimated_cpu_cycles()

                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                set_tox_running_state("running")
                globalstore.setToxRunning(true)
                while (!stop_me)
                {
                    try
                    {
                        if ((global_last_activity_outgoung_ft_ts > -1) && ((global_last_activity_outgoung_ft_ts + 200) > System.currentTimeMillis())) {
                            // HINT: iterate much faster if there are active filetransfers
                            sleep(0, 50)
                            // Log.i(TAG, "=====>>>>> tox_iteration_interval: "+ "2")
                        } else {
                            if (avstatestore.state.calling_state_get() != AVState.CALL_STATUS.CALL_STATUS_NONE)
                            {
                                // HINT: iterate faster if there are active toxav calls
                                sleep(4, 0)
                                // Log.i(TAG, "=====>>>>> tox_iteration_interval: "+ "4")
                            }
                            else
                            {
                                if (tox_iteration_interval_ms < TOX_ITERATE_MS_MIN_NORMAL)
                                {
                                    // HINT: never iterate faster than TOX_ITERATE_MS_MIN_NORMAL
                                    sleep(TOX_ITERATE_MS_MIN_NORMAL.toLong())
                                    Log.i(TAG, "=====>>>>> tox_iteration_interval: " + tox_iteration_interval_ms)
                                }
                                else
                                {
                                    sleep(tox_iteration_interval_ms)
                                    //if (tox_iteration_interval_ms != 50L)
                                    //{
                                    //    Log.i(TAG, "=====>>>>> tox_iteration_interval: " + tox_iteration_interval_ms)
                                    //}
                                }
                            }
                        }
                    } catch (e: InterruptedException)
                    {
                        e.printStackTrace()
                    } catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                    check_if_need_bootstrap_again()
                    tox_iterate()
                    // Log.i(TAG, "=====>>>>> tox_iterate()")
                    tox_iteration_interval_ms = tox_iteration_interval()
                    // Log.i(TAG, "=====>>>>> tox_iteration_interval: "+ tox_iteration_interval_ms)

                    // [ADDED] Check network health every 3 seconds
                    val current_time_ms = System.currentTimeMillis()
                    if ((current_time_ms - last_health_check_ms) >= 3000)
                    {
                        last_health_check_ms = current_time_ms

                        try
                        {
                            val current_health = tox_self_get_network_health()
                            if (current_health != last_network_health)
                            {
                                val health_text = ToxVars.TOX_NETWORK_HEALTH.value_str(current_health).replace("TOX_NETWORK_HEALTH_", "")
                                last_network_health = current_health

                                Log.i(TAG, "tox_network_health: " + health_text)

                                try
                                {
                                    set_tox_network_health(last_network_health)
                                } catch (e: java.lang.Exception)
                                {
                                }
                            }

                            val current_gc_health: Int = tox_group_get_health()
                            if (current_gc_health != last_gc_health)
                            {
                                val gc_health_text = ToxVars.TOX_GROUP_HEALTH.value_str(current_health).replace("TOX_GROUP_HEALTH_", "")
                                last_gc_health = current_gc_health

                                Log.i(TAG, "gc_health_text: " + gc_health_text)

                                try
                                {
                                    set_tox_group_health(last_gc_health)
                                } catch (e: java.lang.Exception)
                                {
                                }
                            }

                        } catch (e: java.lang.Exception)
                        {
                        }
                    }

                    // --- send pending 1-on-1 text messages here --------------
                    if (online_button_text_wrapper != "offline")
                    {
                        if ((last_resend_pending_messages4_ms + (10 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages4_ms = System.currentTimeMillis()
                            if (DB_PREF__send_push_notifications == true)
                            {
                                resend_push_for_v3_messages()
                            }
                        }

                        if ((last_resend_pending_messages0_ms + (30 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages0_ms = System.currentTimeMillis();
                            resend_old_messages(null)
                        }

                        if ((last_resend_pending_messages1_ms + (30 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages1_ms = System.currentTimeMillis();
                            resend_v3_messages(null)
                        }

                        if ((last_resend_pending_messages2_ms + (30 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages2_ms = System.currentTimeMillis();
                            resend_v2_messages(false)
                        }

                        if ((last_resend_pending_messages3_ms + (120 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages3_ms = System.currentTimeMillis();
                            resend_v2_messages(true)
                        }
                    }
                    // --- send pending 1-on-1 text messages here --------------

                    // --- start queued outgoing FTs here --------------
                    if (online_button_text_wrapper != "offline")
                    {
                        if (last_start_queued_fts_ms + 4 * 1000 < System.currentTimeMillis())
                        {
                            // Log.i(TAG, "start_queued_outgoing_FTs ============================================");
                            last_start_queued_fts_ms = System.currentTimeMillis()
                            try
                            {
                                val m_v1 = orma!!.selectFromMessage().directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).
                                TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE.value).ft_outgoing_queuedEq(true).
                                stateNotEq(ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL.value).orderBySent_timestampAsc().toList()
                                if (m_v1 != null && m_v1.size > 0)
                                {
                                    val ii: Iterator<Message> = m_v1.iterator()
                                    while (ii.hasNext())
                                    {
                                        val m_resend_ft = ii.next()
                                        if (m_resend_ft.sent_push < 1) {
                                            friend_call_push_url(m_resend_ft.tox_friendpubkey, m_resend_ft.sent_timestamp)
                                        }
                                        if (tox_friend_get_connection_status(
                                                tox_friend_by_public_key(m_resend_ft.tox_friendpubkey))
                                            != ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE.value)
                                        {
                                            start_outgoing_ft(m_resend_ft)
                                        }
                                    }
                                }
                            } catch (e: java.lang.Exception)
                            {
                            }
                        }
                    }
                    // --- start queued outgoing FTs here --------------

                    // --- refresh IP info for friends -----------------
                    if (last_refresh_friends_ip_info_ms + 60 * 1000 < System.currentTimeMillis())
                    {
                        last_refresh_friends_ip_info_ms = System.currentTimeMillis()
                        try
                        {
                            tox_self_get_friend_list()?.forEach {
                                // Log.i(TAG, "update ip status for friend:" + it)
                                try
                                {
                                    val ip_addr_str = get_friend_ip_str(it)
                                    val f_pubkey = tox_friend_get_public_key(it)
                                    contactstore.update_ipaddr(pubkey = f_pubkey!!, ipaddr = ip_addr_str)
                                }
                                catch(_: Exception)
                                {
                                    val f_pubkey = tox_friend_get_public_key(it)
                                    contactstore.update_ipaddr(pubkey = f_pubkey!!, ipaddr = "")
                                }
                            }
                        }
                        catch(_: java.lang.Exception)
                        {
                        }
                    }
                    // --- refresh IP info for friends -----------------
                }
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                ngc_audio_play_thread_running = false
                try
                {
                    set_tox_network_health(ToxVars.TOX_NETWORK_HEALTH.TOX_NETWORK_HEALTH_UNKNOWN.value)
                } catch (e: java.lang.Exception)
                {
                }

                try
                {
                    set_tox_group_health(ToxVars.TOX_GROUP_HEALTH.TOX_GROUP_HEALTH_UNKNOWN.value)
                } catch (e: java.lang.Exception)
                {
                }

                try
                {
                    ngc_audio_play_thread!!.join(500)
                }
                catch(e: Exception)
                {
                    e.printStackTrace()
                }

                tox_audio_play_thread_running = false
                try
                {
                    tox_audio_play_thread!!.join(500)
                }
                catch(e: Exception)
                {
                    e.printStackTrace()
                }

                try
                {
                    sleep(100) // wait a bit, for "something" to finish up in the native code
                } catch (e: Exception)
                {
                    e.printStackTrace()
                }

                update_savedata_file_wrapper()
                is_tox_started = false

                tox_reset_estimated_cpu_cycles()

                clear_friends()
                clear_groups()
                try {
                    globalstore.hard_clear_unread_message_count()
                } catch(_: Exception) {
                }
                try {
                    globalstore.hard_clear_unread_group_message_count()
                } catch(_: Exception) {
                }

                try
                {
                    tox_kill()
                } catch (e: Exception)
                {
                    e.printStackTrace()
                }
                try
                {
                    sleep(100) // wait a bit, for "something" to finish up in the native code
                } catch (e: Exception)
                {
                    e.printStackTrace()
                }

                set_tox_running_state("stopped")
                globalstore.setToxRunning(false)

                clear_friends()
                clear_groups()
                try {
                    globalstore.hard_clear_unread_message_count()
                } catch(_: Exception) {
                }
                try {
                    globalstore.hard_clear_unread_group_message_count()
                } catch(_: Exception) {
                }

                globalstore.setOrmaRunning(false)
                globalstore.setNative_sqlite_type(SQLITE_TYPE.UNLOADED)
                // ----------------- DB shutdown -----------------
                orma = null
                OrmaDatabase.shutdown()
                // ----------------- DB shutdown -----------------
                Log.i(TAG, "DB:shutdown ok")
                unlock_data_dir_input()
                try
                {
                    toxdatastore.updateToxID("")
                } catch (_: Exception)
                {
                }
            }
        }
        (ToxServiceThread as Thread).start()
    }

    fun wal_checkpoint()
    {
        try
        {
            // Log.i(TAG, "XXXXX:checkpoint:001=" + OrmaDatabase.run_query_for_single_result("PRAGMA busy_timeout = 10; PRAGMA wal_checkpoint(TRUNCATE);"))
        }
        catch(_: Exception)
        {
        }
    }

    fun tox_audio_play_thread_start()
    {
        var sw_t = -1L
        var update_audio_bar = 0
        Log.i(TAG, "[]tox_audio_frame:starting Thread")
        tox_audio_play_thread = object : Thread()
        {
            override fun run()
            {
                try
                {
                    Thread.currentThread().name = "t_a_play"
                } catch (_: Exception)
                {
                }

                try
                {
                    tox_a_queue_stop_trigger = true
                    while (tox_audio_play_thread_running)
                    {
                        // -- play incoming bytes --
                        // -- play incoming bytes --
                        try
                        {
                            if ((tox_audio_in_queue.size < 2) && (!tox_a_queue_stop_trigger))
                            {
                                tox_a_queue_stop_trigger = true
                                // Log.i(TAG, "[]tox_audio_frame:trigger:(PAUSE playing):" + tox_audio_in_queue.size)
                            }
                            else
                            {
                                if (tox_a_queue_stop_trigger)
                                {
                                    if (tox_audio_in_queue.size >= 5)
                                    {
                                        tox_a_queue_stop_trigger = false
                                        // Log.i(TAG, "[]tox_audio_frame:release:(resume playing):" + tox_audio_in_queue.size)
                                    }
                                    else
                                    {
                                        // Log.i(TAG, "[]tox_audio_frame:+++++++++:(paused):" + tox_a_queue_stop_trigger + " "
                                        // + tox_audio_in_queue.size + " " + tox_audio_in_queue.remainingCapacity())
                                        sleep(4)
                                    }
                                }

                                if (!tox_a_queue_stop_trigger)
                                {
                                    val buf: ByteArray = tox_audio_in_queue.poll()
                                    if (buf != null)
                                    {
                                        try
                                        {
                                            val want_bytes = buf.size
                                            val sample_count = want_bytes / 2

                                            // HINT: this acutally plays incoming Audio
                                            // HINT: this may block!!
                                            // Log.i(TAG, "[]tox_audio_frame:bytes_actually_written:sourceDataLine.write:loop_delta=" + (System.currentTimeMillis() - sw_t))
                                            sw_t = System.currentTimeMillis()
                                            val bytes_actually_written = AudioSelectOutBox.sourceDataLine.write(buf, 0, want_bytes)
                                            // Log.i(TAG, "[]tox_audio_frame:bytes_actually_written:sourceDataLine.write:delta=" + (System.currentTimeMillis() - sw_t))
                                            // Log.i(TAG, "[]tox_audio_frame:bytes_actually_written:ms=" + AudioSelectOutBox.sourceDataLine.microsecondPosition / 1000)
                                            if (bytes_actually_written != want_bytes)
                                            {
                                                Log.i(TAG, "[]tox_audio_frame:bytes_actually_written:ERR:=" + bytes_actually_written + " want_bytes=" + want_bytes)
                                            }
                                            else
                                            {
                                                // Log.i(TAG, "[]tox_audio_frame:bytes_actually_written:OK:=" + bytes_actually_written + " want_bytes=" + want_bytes)
                                                // sleep(30)
                                            }

                                            if (MainActivity.AUDIO_PCM_DEBUG_FILES)
                                            {
                                                val f = File("/tmp/toxaudio_play.txt")
                                                try
                                                {
                                                    f.appendBytes(buf)
                                                } catch (e: Exception)
                                                {
                                                    e.printStackTrace()
                                                }
                                            }

                                            update_audio_bar++
                                            if (update_audio_bar >= 1)
                                            {
                                                update_audio_bar = 0
                                                GlobalScope.launch {
                                                    var global_audio_out_vu: Float = MainActivity.AUDIO_VU_MIN_VALUE
                                                    if (sample_count > 0)
                                                    {
                                                        val vu_value = AudioBar.audio_vu(buf, sample_count)
                                                        global_audio_out_vu = if (vu_value > MainActivity.AUDIO_VU_MIN_VALUE)
                                                        {
                                                            vu_value
                                                        } else
                                                        {
                                                            0f
                                                        }
                                                    }
                                                    val global_audio_out_vu_ = global_audio_out_vu
                                                    AudioBar.set_cur_value(global_audio_out_vu_.toInt(), AudioBar.audio_out_bar)
                                                }
                                            }
                                        }
                                        catch(e: Exception)
                                        {
                                            e.printStackTrace()
                                            Log.i(TAG, "[]tox_audio_frame:EE:0021")
                                        }
                                    }
                                    else
                                    {
                                        Log.i(TAG, "[]tox_audio_frame:EE:0033")
                                    }
                                }
                            }
                        } catch (e: java.lang.Exception)
                        {
                            e.printStackTrace()
                            Log.i(TAG, "[]tox_audio_frame:EE:0064")
                        }
                        // -- play incoming bytes --
                        // -- play incoming bytes --
                        // XXXXXXXXXX// sleep(59)
                        if (avstatestorecallstate.state.call_state != AVState.CALL_STATUS.CALL_STATUS_CALLING)
                        {
                            // Log.i(TAG, "[]tox_audio_frame:long sleep SSSSSSSSSS")
                            sleep(200)
                        }
                    }
                } catch (e: Exception)
                {
                    e.printStackTrace()
                    Log.i(TAG, "[]tox_audio_frame:EE:0078")
                }
                Log.i(TAG, "[]tox_audio_frame: Thread ending")
            }
        }
        (tox_audio_play_thread as Thread).start()
    }

    fun ngc_audio_play_thread_start()
    {
        Log.i(TAG, "()PLAY_ngc_audio_frame:starting Thread")
        ngc_audio_play_thread = object : Thread()
        {
            override fun run()
            {
                try
                {
                    Thread.currentThread().name = "t_ngc_a_play"
                } catch (_: Exception)
                {
                }

                try
                {
                    val sampling_rate = 48000
                    val channels = 1
                    var update_audio_bar = 0
                    audio_queue_play_trigger = true
                    while (ngc_audio_play_thread_running)
                    {
                        // -- play incoming bytes --
                        // -- play incoming bytes --
                        try
                        {
                            if ((ngc_audio_in_queue.size < 2) && (!audio_queue_play_trigger))
                            {
                                audio_queue_play_trigger = true
                                // Log.i(TAG, "()PLAY_ngc_audio_frame:trigger:" + ngc_audio_in_queue.size)
                            }
                            else
                            {
                                if (audio_queue_play_trigger)
                                {
                                    if (ngc_audio_in_queue.size >= 5)
                                    {
                                        audio_queue_play_trigger = false
                                        // Log.i(TAG, "()PLAY_ngc_audio_frame:release:")
                                    }
                                    else
                                    {
                                        // Log.i(TAG, "()PLAY_ngc_audio_frame:+++++++++:" + audio_queue_play_trigger + " "
                                        //+ ngc_audio_in_queue.size + " " + ngc_audio_in_queue.remainingCapacity())
                                        sleep(4)
                                    }
                                }

                                if (!audio_queue_play_trigger)
                                {
                                    val buf: ByteArray = ngc_audio_in_queue.poll()
                                    if (buf != null)
                                    {
                                        if ((sampling_rate != AudioSelectOutBox.SAMPLE_RATE) ||
                                            (channels != AudioSelectOutBox.CHANNELS) ||
                                            (AudioSelectOutBox.sourceDataLine == null))
                                        {
                                            Log.i(TAG, "()PLAY_ngc_audio_frame:11:1");
                                            AudioSelectOutBox.init()
                                            AudioSelectOutBox.change_audio_format(sampling_rate, channels)
                                            Log.i(TAG, "()PLAY_ngc_audio_frame:11:2");
                                        }
                                        if (sampling_rate != AudioSelectOutBox.SAMPLE_RATE ||
                                            channels != AudioSelectOutBox.CHANNELS)
                                        {
                                            Log.i(TAG, "()PLAY_ngc_audio_frame:22:1:$sampling_rate" + " "
                                                    + AudioSelectOutBox.SAMPLE_RATE)
                                            AudioSelectOutBox.change_audio_format(sampling_rate, channels)
                                            Log.i(TAG, "()PLAY_ngc_audio_frame:22:2")
                                        }
                                        try
                                        {
                                            val want_bytes = buf.size
                                            val sample_count = want_bytes / 2

                                            // HINT: this acutally plays incoming Audio
                                            // HINT: this may block!!
                                            val bytes_actually_written = AudioSelectOutBox.sourceDataLine.write(buf, 0, want_bytes)
                                            if (bytes_actually_written != want_bytes)
                                            {
                                                // Log.i(TAG, "()PLAY_ngc_audio_frame:bytes_actually_written=" + bytes_actually_written + " want_bytes=" + want_bytes)
                                            }

                                            update_audio_bar++
                                            if (update_audio_bar >= 1)
                                            {
                                                update_audio_bar = 0
                                                GlobalScope.launch {
                                                    var global_audio_out_vu: Float = MainActivity.AUDIO_VU_MIN_VALUE
                                                    if (sample_count > 0)
                                                    {
                                                        val vu_value = AudioBar.audio_vu(buf, sample_count)
                                                        global_audio_out_vu = if (vu_value > MainActivity.AUDIO_VU_MIN_VALUE)
                                                        {
                                                            vu_value
                                                        } else
                                                        {
                                                            0f
                                                        }
                                                    }
                                                    val global_audio_out_vu_ = global_audio_out_vu
                                                    AudioBar.set_cur_value(global_audio_out_vu_.toInt(), AudioBar.audio_out_bar)
                                                }
                                            }
                                        }
                                        catch(e: Exception)
                                        {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        } catch (e: java.lang.Exception)
                        {
                        }
                        // -- play incoming bytes --
                        // -- play incoming bytes --
                        if ((HelperGeneric.ngc_video_packet_last_incoming_ts + 5000) < System.currentTimeMillis())
                        {
                            sleep(200)
                        }
                    }
                } catch (_: Exception)
                {
                }
                Log.i(TAG, "()PLAY_ngc_audio_frame: Thread ending")
            }
        }
        (ngc_audio_play_thread as Thread).start()
    }

    private fun load_db_prefs()
    {
        MainActivity.DB_PREF__open_files_directly = false
        try
        {
            if (HelperFriend.get_g_opts("DB_PREF__open_files_directly") != null)
            {
                if (HelperFriend.get_g_opts("DB_PREF__open_files_directly").equals("true"))
                {
                    MainActivity.DB_PREF__open_files_directly = true
                }
            }
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }

        MainActivity.DB_PREF__notifications_active = true
        try
        {
            if (HelperFriend.get_g_opts("DB_PREF__notifications_active") != null)
            {
                if (HelperFriend.get_g_opts("DB_PREF__notifications_active").equals("false"))
                {
                    MainActivity.DB_PREF__notifications_active = false
                }
            }
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }

        MainActivity.DB_PREF__send_push_notifications = false
        try
        {
            if (HelperFriend.get_g_opts("DB_PREF__send_push_notifications") != null)
            {
                if (HelperFriend.get_g_opts("DB_PREF__send_push_notifications").equals("true"))
                {
                    MainActivity.DB_PREF__send_push_notifications = true
                }
            }
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }

        MainActivity.DB_PREF__use_other_toxproxies = false
        try
        {
            if (HelperFriend.get_g_opts("DB_PREF__use_other_toxproxies") != null)
            {
                if (HelperFriend.get_g_opts("DB_PREF__use_other_toxproxies").equals("true"))
                {
                    MainActivity.DB_PREF__use_other_toxproxies = true
                }
            }
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }
    }

    private fun check_if_need_bootstrap_again()
    {
        if (global_self_connection_status == ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE.value)
        {
            if (global_self_last_went_offline_timestamp != -1L)
            {
                if (global_self_last_went_offline_timestamp + TOX_BOOTSTRAP_AGAIN_AFTER_OFFLINE_MILLIS <
                    System.currentTimeMillis())
                {
                    Log.i(TAG, "offline for too long --> bootstrap again ...")
                    global_self_last_went_offline_timestamp = System.currentTimeMillis()
                    bootstrapping = true
                    Log.i(TAG, "bootrapping:set to true[2]")
                    try
                    {
                        bootstrap_me()
                    } catch (e: java.lang.Exception)
                    {
                        e.printStackTrace()
                        Log.i(TAG, "bootstrap_me:001:EE:" + e.message)
                    }
                }
            }
        }
    }

    companion object
    {
        const val TAG = "trifa.ToxService"
        var ToxServiceThread: Thread? = null
        var stop_me = false
        var is_tox_started = false
        @JvmStatic
        public var orma: OrmaDatabase? = null
        public var TOX_SERVICE_STARTED = false
        var last_resend_pending_messages0_ms: Long = -1
        var last_resend_pending_messages1_ms: Long = -1
        var last_resend_pending_messages2_ms: Long = -1
        var last_resend_pending_messages3_ms: Long = -1
        var last_resend_pending_messages4_ms: Long = -1
        var last_start_queued_fts_ms: Long = -1
        var last_refresh_friends_ip_info_ms: Long = -1
        var ngc_audio_play_thread_running = false
        var ngc_audio_play_thread: Thread? = null
        var tox_audio_play_thread_running = false
        var tox_audio_play_thread: Thread? = null

        // ------------------------------
        fun bootstrap_me()
        {
            Log.i(TAG, "bootstrap_me")
            // TODO: bootstap_from_custom_nodes()
            // ----- UDP ------
            get_udp_nodelist_from_db(orma)
            Log.i(TAG, "bootstrap_node_list[sort]=" + bootstrap_node_list.toString())

            try
            {
                Collections.shuffle(bootstrap_node_list)
                Collections.shuffle(bootstrap_node_list)
            } catch (e: java.lang.Exception)
            {
                e.printStackTrace()
            }
            Log.i(TAG, "bootstrap_node_list[rand]=" + bootstrap_node_list.toString())
            try
            {
                val i2: Iterator<*> = bootstrap_node_list.iterator()
                var ee: BootstrapNodeEntryDB
                var used = 0
                while (i2.hasNext())
                {
                    ee = i2.next() as BootstrapNodeEntryDB
                    val bootstrap_result = bootstrap_single_wrapper(ee.ip, ee.port.toInt(), ee.key_hex)
                    Log.i(TAG, "bootstrap_single:res=$bootstrap_result")
                    if (bootstrap_result == 0)
                    {
                        used++
                        // Log.Log.i(TAG, "bootstrap_single:++:used=" + used);
                    }
                    if (used >= USE_MAX_NUMBER_OF_BOOTSTRAP_NODES)
                    {
                        Log.i(TAG, "bootstrap_single:break:used=$used")
                        break
                    }
                }
            } catch (e: java.lang.Exception)
            {
                e.printStackTrace()
            }
            // ----- UDP ------
            //
            // ----- TCP ------
            get_tcprelay_nodelist_from_db(orma)
            Log.i(TAG, "tcprelay_node_list[sort]=" + tcprelay_node_list.toString())
            try
            {
                Collections.shuffle(tcprelay_node_list)
                Collections.shuffle(tcprelay_node_list)
            } catch (e: java.lang.Exception)
            {
                e.printStackTrace()
            }
            Log.i(TAG, "tcprelay_node_list[rand]=" + tcprelay_node_list.toString())
            try
            {
                if (USE_MAX_NUMBER_OF_BOOTSTRAP_TCP_RELAYS > 0)
                {
                    val i2: Iterator<*> = tcprelay_node_list.iterator()
                    var ee: BootstrapNodeEntryDB
                    var used = 0
                    while (i2.hasNext())
                    {
                        ee = i2.next() as BootstrapNodeEntryDB
                        val bootstrap_result: Int = add_tcp_relay_single_wrapper(ee.ip, ee.port.toInt(), ee.key_hex)
                        Log.i(TAG, "add_tcp_relay_single:res=$bootstrap_result")
                        if (bootstrap_result == 0)
                        {
                            used++
                            // Log.Log.i(TAG, "add_tcp_relay_single:++:used=" + used);
                        }
                        if (used >= USE_MAX_NUMBER_OF_BOOTSTRAP_TCP_RELAYS)
                        {
                            Log.i(TAG, "add_tcp_relay_single:break:used=$used")
                            break
                        }
                    }
                }
            } catch (e: java.lang.Exception)
            {
                e.printStackTrace()
            }
            // ----- TCP ------
            // ----- TCP mobile ------
            // Log.i(TAG, "add_tcp_relay_single:res=" + MainActivity.add_tcp_relay_single_wrapper("127.0.0.1", 33447, "252E6D7F8168682363BC473C3951357FB2E28BC9A7B7E1F4CB3B302DC331BDAA".substring(0, (TOX_PUBLIC_KEY_SIZE * 2) - 0)));
            // ----- TCP mobile ------
            bootstrapping = false
        }

        // --------------- JNI ---------------
        // --------------- JNI ---------------
        // --------------- JNI ---------------
        @Suppress("UNUSED_PARAMETER")
        @JvmStatic
        fun logger(level: Int, text: String?)
        {
            Log.i(TAG, text!!)
        }

        @JvmStatic
        fun safe_string(input: ByteArray?): String
        { // Log.i(TAG, "safe_string:in=" + in);
            var out = ""
            try
            {
                out = String(input!!, charset("UTF-8")) // Best way to decode using "UTF-8"
            } catch (e: Exception)
            {
                e.printStackTrace()
                Log.i(TAG, "safe_string:EE:" + e.message)
                try
                {
                    out = String(input!!)
                } catch (e2: Exception)
                {
                    e2.printStackTrace()
                    Log.i(TAG, "safe_string:EE2:" + e2.message)
                }
            } // Log.i(TAG, "safe_string:out=" + out);
            return out
        } // --------------- JNI --------------- // --------------- JNI --------------- // --------------- JNI ---------------

        fun clear_grouppeers()
        {
            try
            {
                grouppeerstore.clear()
            } catch (_: Exception)
            {
            }
        }

        fun update_group_peers_ui_from_middleware(group_id: String)
        {
            // Only update the UI peer list if this group is currently selected in the UI
            if (groupstore.stateFlow.value.selectedGroupId != group_id)
            {
                return
            }

            try
            {
                // 1. Get the total number of peers tracked by the middleware (includes offline)
                val mid_peer_count = tox_group_mid_peer_list_count(group_id)

                // If the roster is empty, clear the UI for this group and exit
                if (mid_peer_count <= 0)
                {
                    grouppeerstore.replaceForGroup(group_id, emptyList())
                    return
                }

                val new_peers_list = mutableListOf<GroupPeerItem>()

                // 2. Iterate through the middleware roster
                for (i in 0 until mid_peer_count.toInt())
                {
                    try
                    {
                        // Fetch the ENTIRE peer record in a single JNI call.
                        // Returns an Array<Any?> mapping to the C MidPeerInfo struct.
                        val peer_info = tox_group_mid_peer_list_get(group_id, i.toLong()) ?: continue

                        // Extract fields from the returned Object array based on the C JNI implementation:
                        val peer_pubkey = peer_info[0] as? String ?: continue
                        // val peer_signing_key = peer_info[1] as? String
                        // val peer_status = (peer_info[2] as? Int) ?: 0
                        val peer_connection_status = (peer_info[3] as? Int) ?: 0
                        // val peer_has_signature = (peer_info[4] as? Int) ?: 0
                        // val peer_last_seen = (peer_info[5] as? Long) ?: 0L
                        val peer_name = peer_info[6] as? String
                        val peer_role = (peer_info[7] as? Int) ?: 2 // 2 == TOX_GROUP_ROLE_USER

                        // 3. If the peer is online, query Toxcore for their live IP address
                        var ip_addr_str = ""
                        if (peer_connection_status != 0) // 0 == TOX_CONNECTION_NONE
                        {
                            try
                            {
                                val group_number = tox_group_by_groupid__wrapper(group_id)
                                if (group_number != -1L)
                                {
                                    val tox_peer_id = tox_group_peer_by_public_key(group_number, peer_pubkey)
                                    if (tox_peer_id != -1L)
                                    {
                                        ip_addr_str = get_group_peer_ip_str(group_number, tox_peer_id)
                                    }
                                }
                            }
                            catch (_: Exception)
                            {
                                // IP resolution failed, but we still want to show the peer
                            }
                        }

                        // 4. Construct the UI item
                        new_peers_list.add(
                            GroupPeerItem(
                                groupID = group_id,
                                pubkey = peer_pubkey.uppercase(),
                                name = if (!peer_name.isNullOrEmpty()) peer_name else "peer $i",
                                connectionStatus = peer_connection_status,
                                peerRole = peer_role,
                                ip_addr = ip_addr_str
                            )
                        )
                    }
                    catch (_: Exception)
                    {
                        // Ignore individual peer parsing errors and continue to the next peer
                    }
                }

                // 5. Single atomic transaction to replace all peers for this group in the UI!
                grouppeerstore.replaceForGroup(group_id, new_peers_list)
            }
            catch (_: Exception)
            {
                // Log general failure if needed
            }
        }

        fun load_grouppeers(groupID: String)
        {
            val groupnum = HelperGroup.tox_group_by_groupid__wrapper(groupID)
            val num_peers: Long = tox_group_peer_count(groupnum)
            val group_peerlist = tox_group_get_peerlist(groupnum)
            if (num_peers > 0)
            {
                group_peerlist!!.forEach {
                    val ip_addr_str = get_group_peer_ip_str(groupnum, it)
                    val peer_pubkey = tox_group_peer_get_public_key(groupnum, it)
                    val peer_name = tox_group_peer_get_name(groupnum, it)
                    val peer_connection_status = tox_group_peer_get_connection_status(groupnum, it)
                    val peer_role = tox_group_peer_get_role(groupnum, it)
                    try
                    {
                        grouppeerstore.add(item = GroupPeerItem(
                            ip_addr = ip_addr_str,
                            name = if (peer_name != null) peer_name else ("peer " + it),
                            connectionStatus = peer_connection_status,
                            pubkey = peer_pubkey!!,
                            peerRole = peer_role,
                            groupID = groupID))
                    } catch (_: Exception)
                    {
                    }
                }
            }
        }

        fun resend_push_for_v3_messages()
        {
            try
            {
                // HINT: if we have not received a "read receipt" for msgV3 within 10 seconds, then we trigger a push again
                val cutoff_sent_time = System.currentTimeMillis() - (10 * 1000)

                // first check:
                val m_push_count = orma!!.selectFromMessage().
                directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).
                msg_versionEq(0).
                TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value).
                sent_pushEq(0).
                readEq(false).
                orderBySent_timestampAsc().
                sent_timestampLt(cutoff_sent_time).
                count()

                // Log.i(TAG, "resend_push_for_v3_messages:m_push_count=" + m_push_count)
                if (m_push_count < 1)
                {
                    return
                }

                val m_push: List<Message>? = orma!!.selectFromMessage().directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).
                msg_versionEq(0).
                TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value).
                sent_pushEq(0).
                readEq(false).
                orderBySent_timestampAsc().
                sent_timestampLt(cutoff_sent_time).
                toList()

                if ((m_push != null) && (m_push.size > 0))
                {
                    val ii = m_push.iterator()
                    while (ii.hasNext())
                    {
                        val m_resend_push = ii.next()
                        if ((m_resend_push.msg_idv3_hash != null) && (m_resend_push.msg_idv3_hash.length > 3))
                        {
                            friend_call_push_url(m_resend_push.tox_friendpubkey, m_resend_push.sent_timestamp)
                        }
                    }
                }
            } catch (e: java.lang.Exception)
            {
                e.printStackTrace()
                Log.i(TAG, "resend_push_for_v3_messages:EE:" + e.message)
            }
        }

        fun resend_v3_messages(friend_pubkey: String?)
        {
            // loop through "old msg version" msgV3 1-on-1 text messages that have "resend_count < MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION" --------------
            try
            {
                var max_resend_count_per_iteration = 20
                if (friend_pubkey != null)
                {
                    max_resend_count_per_iteration = 20
                }
                var cur_resend_count_per_iteration = 0

                // HINT: check if there is anything to resend first --------
                var check_count = 0
                try {
                    check_count = orma!!.selectFromMessage().directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value)
                        .msg_versionEq(0)
                        .TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value)
                        .resend_countLt(MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION).readEq(false)
                        .count()
                }
                catch (_: Exception) {}
                if (check_count == 0) { return }
                // HINT: check if there is anything to resend first --------

                Log.i(TAG, "resend_v3_messages: -- SQL --")
                var m_v1: List<Message>? = null
                m_v1 = if (friend_pubkey != null)
                {
                    orma!!.selectFromMessage().directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).msg_versionEq(0).tox_friendpubkeyEq(friend_pubkey).TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value).resend_countLt(MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION).readEq(false).orderBySent_timestampAsc().toList()
                } else
                {
                    orma!!.selectFromMessage().directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).msg_versionEq(0).TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value).resend_countLt(MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION).readEq(false).orderBySent_timestampAsc().toList()
                }
                Log.i(TAG, "resend_v3_messages: -- SQL --")
                if (m_v1 != null && m_v1.size > 0)
                {
                    Log.i(TAG, "resend_v3_messages: we have " + m_v1.size + " messages to resend")
                    var ii = m_v1.iterator()
                    var m_counter = 0
                    while (ii.hasNext())
                    {
                        val m_resend_v1 = ii.next()
                        m_counter++

                        // Log.i(TAG, "resend_v3_messages: " + m_counter + ": friend="
                        //        + get_friend_name_from_pubkey(m_resend_v1.tox_friendpubkey) + " text=" + m_resend_v1.text)
                    }
                    ii = m_v1.iterator()
                    while (ii.hasNext())
                    {
                        val m_resend_v1 = ii.next()
                        if (friend_pubkey == null)
                        {
                            if (is_friend_online_real(tox_friend_by_public_key(m_resend_v1.tox_friendpubkey)) == 0)
                            {
                                continue
                            }
                        }
                        Log.i(TAG, "resend_v3_messages:get_friend_msgv3_capability=" + get_friend_msgv3_capability(m_resend_v1.tox_friendpubkey))
                        if (get_friend_msgv3_capability(m_resend_v1.tox_friendpubkey) != 1L)
                        {
                            Log.i(TAG, "resend_v3_messages:RET:02:friend hash msgv3_capability:" +
                                    get_friend_name_from_pubkey(m_resend_v1.tox_friendpubkey))
                            continue
                        }
                        // Log.i(TAG, "resend_v3_messages:tox_friend_resend_msgv3_wrapper:msg_idv3_hash=" +  m_resend_v1.msg_idv3_hash + " text=" + m_resend_v1.text + " : m=" +
                        //         m_resend_v1 + " : " + get_friend_name_from_pubkey(m_resend_v1.tox_friendpubkey));
                        tox_friend_resend_msgv3_wrapper(m_resend_v1)
                        cur_resend_count_per_iteration++
                        if (cur_resend_count_per_iteration >= max_resend_count_per_iteration)
                        {
                            break
                        }
                    }
                }
            } catch (e: java.lang.Exception)
            {
                e.printStackTrace()
                Log.i(TAG, "resend_v3_messages:EE:" + e.message)
            }
            // loop through all pending outgoing 1-on-1 text messages --------------
        }

        fun resend_old_messages(friend_pubkey: String?)
        {
            try
            {
                var max_resend_count_per_iteration = 10
                if (friend_pubkey != null)
                {
                    max_resend_count_per_iteration = 20
                }
                var cur_resend_count_per_iteration = 0
                // HINT: cutoff time "now" minus 25 seconds
                val cutoff_sent_time = System.currentTimeMillis() - 25 * 1000

                // HINT: check if there is anything to resend first --------
                var check_count = 0
                try {
                    check_count = orma!!.selectFromMessage().directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value)
                        .TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value)
                        .msg_versionEq(0).readEq(false)
                        .resend_countLt(MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION)
                        .count()
                }
                catch (_: Exception) {}
                if (check_count == 0) { return }
                // HINT: check if there is anything to resend first --------

                Log.i(TAG, "resend_old_messages: -- SQL --")
                var m_v0: List<Message>? = null
                m_v0 = if (friend_pubkey != null)
                {
                    // HINT: this is the generic resend for all friends, that happens in regular intervals
                    //       only resend if the original sent timestamp is at least 25 seconds in the past
                    //       to try to avoid resending when the read receipt is very late.
                    orma!!.selectFromMessage().directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value).msg_versionEq(0).tox_friendpubkeyEq(friend_pubkey).readEq(false).resend_countLt(MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION).orderBySent_timestampAsc().sent_timestampLt(cutoff_sent_time).toList()
                } else
                {
                    // HINT: this is the specific resend for 1 friend only, when that friend comes online
                    orma!!.selectFromMessage().directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value).msg_versionEq(0).readEq(false).resend_countLt(MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION).orderBySent_timestampAsc().toList()
                }
                Log.i(TAG, "resend_old_messages: -- SQL --")
                if (m_v0 != null && m_v0.size > 0)
                {
                    Log.i(TAG, "resend_old_messages: we have " + m_v0.size + " messages to resend")
                    var ii = m_v0.iterator()
                    var m_counter = 0
                    while (ii.hasNext())
                    {
                        val m_resend_v0 = ii.next()
                        m_counter++

                        // Log.i(TAG, "resend_old_messages: " + m_counter + ": friend="
                        //        + get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey) + " text=" + m_resend_v0.text)
                    }
                    ii = m_v0.iterator()
                    while (ii.hasNext())
                    {
                        val m_resend_v0 = ii.next()
                        if (friend_pubkey == null)
                        {
                            if (is_friend_online_real(tox_friend_by_public_key(m_resend_v0.tox_friendpubkey)) == 0)
                            {
                                // Log.i(TAG, "resend_old_messages:RET:01:friend not online:" +
                                //            get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey))
                                continue
                            }
                        }
                        if (get_friend_msgv3_capability(m_resend_v0.tox_friendpubkey) == 1L)
                        {
                            Log.i(TAG, "resend_old_messages:RET:02:friend hash msgv3_capability:" +
                                        get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey))
                            continue
                        }
                        // Log.i(TAG, "resend_old_messages:tox_friend_resend_msgv3_wrapper:" + m_resend_v0.text + " : m=" +
                        //            m_resend_v0 + " : " + get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey))
                        tox_friend_resend_msgv3_wrapper(m_resend_v0)
                        cur_resend_count_per_iteration++
                        if (cur_resend_count_per_iteration >= max_resend_count_per_iteration)
                        {
                            break
                        }
                    }
                }
            } catch (e: java.lang.Exception)
            {
                e.printStackTrace()
            }
        }

        fun resend_v2_messages(at_relay: Boolean)
        {
            // loop through all pending outgoing 1-on-1 text messages V2 (resend) --------------
            try
            {
                val max_resend_count_per_iteration = 10
                var cur_resend_count_per_iteration = 0

                // HINT: check if there is anything to resend first --------
                var check_count = 0
                try {
                    check_count = orma!!.selectFromMessage().
                    directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value)
                        .msg_versionEq(1).readEq(false).
                        msg_at_relayEq(at_relay).count()
                }
                catch (_: Exception) {}
                if (check_count == 0) { return }
                // HINT: check if there is anything to resend first --------

                Log.i(TAG, "resend_v2_messages: -- SQL --")
                val m_v1 = orma!!.selectFromMessage().
                directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_SENT.value).TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value)
                    .msg_versionEq(1).readEq(false).
                    msg_at_relayEq(at_relay).orderBySent_timestampAsc().toList()
                Log.i(TAG, "resend_v2_messages: -- SQL --")
                if (m_v1 != null && m_v1.size > 0)
                {
                    Log.i(TAG, "resend_v2_messages: we have " + m_v1.size + " messages to resend")
                    var ii: Iterator<Message> = m_v1.iterator()
                    var m_counter = 0
                    while (ii.hasNext())
                    {
                        val m_resend_v2 = ii.next()
                        m_counter++

                        // Log.i(TAG, "resend_v2_messages: " + m_counter + ": friend="
                        //        + get_friend_name_from_pubkey(m_resend_v2.tox_friendpubkey) + " text=" + m_resend_v2.text)
                    }
                    ii = m_v1.iterator()
                    while (ii.hasNext())
                    {
                        val m_resend_v2 = ii.next()
                        if (is_friend_online_real(tox_friend_by_public_key(m_resend_v2.tox_friendpubkey)) == 0)
                        {
                            if (m_resend_v2.sent_push == 0)
                            {
                                friend_call_push_url(m_resend_v2.tox_friendpubkey, m_resend_v2.sent_timestamp)
                            }
                            continue
                        }
                        if (m_resend_v2.msg_id_hash == null ||
                            m_resend_v2.msg_id_hash.equals("", ignoreCase = true)) // resend msgV2 WITHOUT hash
                        {
                            val result: MainActivity.Companion.send_message_result? = tox_friend_send_message_wrapper(
                                m_resend_v2.tox_friendpubkey, 0, m_resend_v2.text, m_resend_v2.sent_timestamp / 1000)
                            if (result != null)
                            {
                                val res: Long = result.msg_num
                                if (res > -1)
                                {
                                    m_resend_v2.resend_count = 1 // we sent the message successfully
                                    m_resend_v2.message_id = res
                                    Log.i(TAG, "resend_v2_messages:A: message_id=" + res)
                                } else
                                {
                                    m_resend_v2.resend_count = 0 // sending was NOT successfull
                                    m_resend_v2.message_id = -1
                                    Log.i(TAG, "resend_v2_messages:B: message_id=" + "-1")
                                }
                                if (result.msg_v2)
                                {
                                    m_resend_v2.msg_version = 1
                                } else
                                {
                                    m_resend_v2.msg_version = 0
                                }
                                if (result.msg_hash_hex != null && !result.msg_hash_hex.equals("", true))
                                {
                                    // msgV2 message -----------
                                    m_resend_v2.msg_id_hash = result.msg_hash_hex
                                    // msgV2 message -----------
                                }
                                if (result.msg_hash_v3_hex != null && !result.msg_hash_v3_hex.equals("", true))
                                {
                                    // msgV3 message -----------
                                    m_resend_v2.msg_idv3_hash = result.msg_hash_v3_hex
                                    // msgV3 message -----------
                                }
                                if (result.raw_message_buf_hex != null &&
                                    !result.raw_message_buf_hex.equals("", true))
                                {
                                    // save raw message bytes of this v2 msg into the database
                                    // we need it if we want to resend it later
                                    m_resend_v2.raw_msgv2_bytes = result.raw_message_buf_hex
                                }
                                update_message_in_db_messageid(m_resend_v2)
                                update_message_in_db_resend_count(m_resend_v2)
                                update_message_in_db_no_read_recvedts(m_resend_v2)
                            }
                        } else  // resend msgV2 with hash
                        {
                            val raw_data_length = m_resend_v2.raw_msgv2_bytes.length / 2
                            val raw_msg_resend_data = hex_to_bytes(m_resend_v2.raw_msgv2_bytes)
                            val msg_text_buffer_resend_v2 = ByteBuffer.allocateDirect(raw_data_length)
                            msg_text_buffer_resend_v2.put(raw_msg_resend_data, 0, raw_data_length)
                            val res: Int = tox_util_friend_resend_message_v2(
                                tox_friend_by_public_key(m_resend_v2.tox_friendpubkey),
                                msg_text_buffer_resend_v2, raw_data_length.toLong())
                            Log.i(TAG, "resend_v2_messages:7: tox_util_friend_resend_message_v2 res: " + res)
                            /*
                            val relay = get_relay_for_friend(m_resend_v2.tox_friendpubkey)
                            if (relay != null)
                            {
                                val res_relay: Int = tox_util_friend_resend_message_v2(tox_friend_by_public_key(relay),
                                    msg_text_buffer_resend_v2,
                                    raw_data_length.toLong())
                            }
                            */
                        }
                        cur_resend_count_per_iteration++
                        if (cur_resend_count_per_iteration >= max_resend_count_per_iteration)
                        {
                            break
                        }
                    }
                }
            } catch (e: java.lang.Exception)
            {
                e.printStackTrace()
            }
            // loop through all pending outgoing 1-on-1 text messages V2 (resend the resend) --------------
        }
    }

    fun clear_friends()
    {
        try
        {
            contactstore.clear()
        } catch (_: Exception)
        {
        }
    }

    fun clear_groups()
    {
        try
        {
            groupstore.clear()
        } catch (_: Exception)
        {
        }
    }

    fun load_friends()
    {
        tox_self_get_friend_list()?.forEach {
            // Log.i(TAG, "friend:" + it)
            var f: FriendList? = null
            val f_pubkey = tox_friend_get_public_key(it)
            var fl: MutableList<FriendList>? = null
            if (f_pubkey != null)
            {
                fl = orma!!.selectFromFriendList().
                    tox_public_key_stringEq(f_pubkey.uppercase()).toList()
            }
            if ((fl != null) && (fl.size > 0))
            {
                f = fl.get(0)
            }
            else
            {
                f = null
            }
            var exists_in_db = false
            if (f == null)
            {
                Log.i(TAG, "loading_friend:c is null")
                f = FriendList()
                f.tox_public_key_string = "" + (Math.random() * 10000000.0).toLong()
                try
                {
                    f.tox_public_key_string = f_pubkey
                } catch (e: java.lang.Exception)
                {
                    e.printStackTrace()
                }
                f.name = "Friend #" + it
                exists_in_db = false
            } else
            {
                exists_in_db = true
            }

            var fname = tox_friend_get_name(it)
            if (fname == null)
            {
                fname = "Friend #" + it
            }
            f.name = fname

            try
            {
                // get the real "live" connection status of this friend
                // the value in the database may be old (and wrong)
                val status_new = tox_friend_get_connection_status(it)
                val combined_connection_status_: Int = status_new
                f.TOX_CONNECTION = combined_connection_status_
                f.TOX_CONNECTION_on_off = 0
                f.added_timestamp = System.currentTimeMillis()
            } catch (e: java.lang.Exception)
            {
                e.printStackTrace()
            }

            if (exists_in_db == false)
            {
                // Log.i(TAG, "loading_friend:1:insertIntoFriendList:" + " f=" + f);
                orma!!.insertIntoFriendList(f);
                // Log.i(TAG, "loading_friend:2:insertIntoFriendList:" + " f=" + f);
            }
            else
            {
                // Log.i(TAG, "loading_friend:1:updateFriendList:" + " f=" + f);
                orma!!.updateFriendList().tox_public_key_stringEq(f_pubkey)
                    .name(f.name).
                    status_message(f.status_message).
                    TOX_CONNECTION(f.TOX_CONNECTION).
                    TOX_CONNECTION_on_off(0).
                    TOX_USER_STATUS(f.TOX_USER_STATUS).execute();
                // Log.i(TAG, "loading_friend:1:updateFriendList:" + " f=" + f);
            }

            try
            {
                contactstore.add(item = ContactItem(name = fname,
                    isConnected = 0,
                    pubkey = tox_friend_get_public_key(it)!!,
                    push_url = f.push_url,
                    is_relay = f.is_relay))
            } catch (_: Exception)
            {
            }

            try {
                globalfrndstoreunreadmsgs.try_clear_unread_per_friend_message_count(tox_friend_get_public_key(it)!!)
            } catch(_: Exception) {
            }
        }
    }

    fun load_groups()
    {
        val num_groups: Long = tox_group_get_number_groups()
        val group_numbers = tox_group_get_grouplist()
        val groupid_buf3: ByteBuffer = ByteBuffer.allocateDirect(GROUP_ID_LENGTH * 2)
        var conf_ = 0
        while (conf_ < num_groups)
        {
            groupid_buf3.clear()
            if (tox_group_get_chat_id(group_numbers!![conf_], groupid_buf3) == 0)
            {
                val groupid_buffer = ByteArray(GROUP_ID_LENGTH)
                groupid_buf3.get(groupid_buffer, 0, GROUP_ID_LENGTH)
                val group_identifier: String = HelperGeneric.bytesToHex(groupid_buffer, 0, GROUP_ID_LENGTH).lowercase()
                val is_connected: Int = tox_group_is_connected(group_numbers!![conf_])
                var group_name: String? = tox_group_get_name(group_numbers!![conf_])
                val group_num_peers = tox_group_peer_count(group_numbers!![conf_])
                if (group_name == null)
                {
                    group_name = ""
                }
                val new_privacy_state: Int = tox_group_get_privacy_state(group_numbers!![conf_])

                try
                {
                    val group_new = GroupDB()
                    group_new.group_identifier = group_identifier
                    group_new.privacy_state = new_privacy_state
                    group_new.name = group_name
                    group_new.notification_silent = false
                    orma!!.insertIntoGroupDB(group_new)
                } catch (_: Exception)
                {
                }

                try
                {
                    groupstore.add(item = GroupItem(numPeers = group_num_peers.toInt(), name = group_name, isConnected = is_connected, groupId = group_identifier, privacyState = new_privacy_state))
                } catch (_: Exception)
                {
                }

                try {
                    globalgrpstoreunreadmsgs.try_clear_unread_per_group_message_count(group_identifier)
                } catch(_: Exception) {
                }
            }
            conf_++
        }
    }
}

fun upgrade_db_schema_do(old_version: Int, new_version: Int)
{
    Log.i(TrifaToxService.TAG, "upgrade_db_schema_do:old=" + old_version + " new=" + new_version)

    //noinspection StatementWithEmptyBody
    if (new_version == 1)
    {
        // @formatter:off
        val update_001 = """
                    CREATE TABLE IF NOT EXISTS "BootstrapNodeEntryDB" (
                        "num"	INTEGER NOT NULL,
                        "udp_node"	BOOLEAN NOT NULL,
                        "ip"	TEXT NOT NULL,
                        "port"	INTEGER NOT NULL,
                        "key_hex"	TEXT NOT NULL,
                        "id"	INTEGER,
                        PRIMARY KEY("id" AUTOINCREMENT)
                    );
                    CREATE TABLE IF NOT EXISTS "ConferenceDB" (
                        "who_invited__tox_public_key_string"	TEXT NOT NULL,
                        "name"	TEXT,
                        "peer_count"	INTEGER NOT NULL DEFAULT -1,
                        "own_peer_number"	INTEGER NOT NULL DEFAULT -1,
                        "kind"	INTEGER NOT NULL DEFAULT 0,
                        "tox_conference_number"	INTEGER NOT NULL DEFAULT -1,
                        "conference_active"	BOOLEAN NOT NULL DEFAULT false,
                        "notification_silent"	BOOLEAN DEFAULT false,
                        "conference_identifier"	TEXT,
                        PRIMARY KEY("conference_identifier")
                    );
                    CREATE TABLE IF NOT EXISTS "ConferenceMessage" (
                        "message_id_tox" TEXT,
                        "conference_identifier"	TEXT NOT NULL DEFAULT -1,
                        "tox_peerpubkey"	TEXT NOT NULL,
                        "tox_peername"	TEXT,
                        "direction"	INTEGER NOT NULL,
                        "TOX_MESSAGE_TYPE"	INTEGER NOT NULL,
                        "TRIFA_MESSAGE_TYPE"	INTEGER NOT NULL DEFAULT 0,
                        "sent_timestamp"	INTEGER,
                        "rcvd_timestamp"	INTEGER,
                        "read"	BOOLEAN NOT NULL DEFAULT 0,
                        "is_new"	BOOLEAN NOT NULL DEFAULT 1,
                        "text"	TEXT,
                        "was_synced" BOOLEAN NOT NULL DEFAULT 0,
                        "id"	INTEGER,
                        PRIMARY KEY("id" AUTOINCREMENT)
                    );
                    CREATE TABLE IF NOT EXISTS "ConferencePeerCacheDB" (
                        "conference_identifier"	TEXT NOT NULL,
                        "peer_pubkey"	TEXT NOT NULL,
                        "peer_name"	TEXT NOT NULL,
                        "last_update_timestamp"	INTEGER NOT NULL DEFAULT -1,
                        "id"	INTEGER,
                        PRIMARY KEY("id" AUTOINCREMENT)
                    );
                    CREATE TABLE IF NOT EXISTS "FileDB" (
                        "kind"	INTEGER NOT NULL,
                        "direction"	INTEGER NOT NULL,
                        "tox_public_key_string"	TEXT NOT NULL,
                        "path_name"	TEXT NOT NULL,
                        "file_name"	TEXT NOT NULL,
                        "filesize"	INTEGER NOT NULL DEFAULT -1,
                        "is_in_VFS"	BOOLEAN NOT NULL DEFAULT true,
                        "id"	INTEGER,
                        PRIMARY KEY("id" AUTOINCREMENT)
                    );
                    CREATE TABLE IF NOT EXISTS "Filetransfer" (
                        "tox_public_key_string"	TEXT NOT NULL,
                        "direction"	INTEGER NOT NULL,
                        "file_number"	INTEGER NOT NULL,
                        "kind"	INTEGER NOT NULL,
                        "state"	INTEGER NOT NULL,
                        "ft_accepted"	BOOLEAN NOT NULL DEFAULT false,
                        "ft_outgoing_started"	BOOLEAN NOT NULL DEFAULT false,
                        "path_name"	TEXT NOT NULL,
                        "file_name"	TEXT NOT NULL,
                        "fos_open"	BOOLEAN NOT NULL DEFAULT false,
                        "filesize"	INTEGER NOT NULL DEFAULT -1,
                        "current_position"	INTEGER NOT NULL DEFAULT 0,
                        "message_id"	INTEGER NOT NULL DEFAULT -1,
                        "id"	INTEGER,
                        PRIMARY KEY("id" AUTOINCREMENT)
                    );
                    CREATE TABLE IF NOT EXISTS "TRIFADatabaseGlobals" (
                        "key"	TEXT NOT NULL,
                        "value"	TEXT NOT NULL
                    );
                    CREATE TABLE IF NOT EXISTS "RelayListDB" (
                        "TOX_CONNECTION"	INTEGER NOT NULL DEFAULT 0,
                        "TOX_CONNECTION_on_off"	INTEGER NOT NULL DEFAULT 0,
                        "own_relay"	BOOLEAN NOT NULL DEFAULT false,
                        "last_online_timestamp"	INTEGER NOT NULL DEFAULT -1,
                        "tox_public_key_string_of_owner"	TEXT,
                        "tox_public_key_string"	TEXT,
                        PRIMARY KEY("tox_public_key_string")
                    );
                    CREATE TABLE IF NOT EXISTS "Message" (
                        "message_id"	INTEGER NOT NULL,
                        "tox_friendpubkey"	TEXT NOT NULL,
                        "direction"	INTEGER NOT NULL,
                        "TOX_MESSAGE_TYPE"	INTEGER NOT NULL,
                        "TRIFA_MESSAGE_TYPE"	INTEGER NOT NULL DEFAULT 0,
                        "state"	INTEGER NOT NULL DEFAULT 1,
                        "ft_accepted"	BOOLEAN NOT NULL DEFAULT false,
                        "ft_outgoing_started"	BOOLEAN NOT NULL DEFAULT false,
                        "filedb_id"	INTEGER NOT NULL DEFAULT -1,
                        "filetransfer_id"	INTEGER NOT NULL DEFAULT -1,
                        "sent_timestamp"	INTEGER DEFAULT 0,
                        "sent_timestamp_ms"	INTEGER DEFAULT 0,
                        "rcvd_timestamp"	INTEGER DEFAULT 0,
                        "rcvd_timestamp_ms"	INTEGER DEFAULT 0,
                        "read"	BOOLEAN NOT NULL,
                        "send_retries"	INTEGER NOT NULL DEFAULT 0,
                        "is_new"	BOOLEAN NOT NULL,
                        "text"	TEXT,
                        "filename_fullpath"	TEXT,
                        "msg_id_hash"	TEXT,
                        "raw_msgv2_bytes"	TEXT,
                        "msg_version"	INTEGER NOT NULL DEFAULT 0,
                        "resend_count"	INTEGER NOT NULL DEFAULT 2,
                        "id"	INTEGER,
                        PRIMARY KEY("id" AUTOINCREMENT)
                    );
                    CREATE TABLE IF NOT EXISTS "TRIFADatabaseGlobalsNew" (
                        "value"	TEXT NOT NULL,
                        "key"	TEXT,
                        PRIMARY KEY("key")
                    );
                    CREATE TABLE IF NOT EXISTS "FriendList" (
                        "name"	TEXT,
                        "alias_name"	TEXT,
                        "status_message"	TEXT,
                        "TOX_CONNECTION"	INTEGER NOT NULL DEFAULT 0,
                        "TOX_CONNECTION_real"	INTEGER NOT NULL DEFAULT 0,
                        "TOX_CONNECTION_on_off"	INTEGER NOT NULL DEFAULT 0,
                        "TOX_CONNECTION_on_off_real"	INTEGER NOT NULL DEFAULT 0,
                        "TOX_USER_STATUS"	INTEGER NOT NULL DEFAULT 0,
                        "avatar_pathname"	TEXT,
                        "avatar_filename"	TEXT,
                        "avatar_update"	BOOLEAN DEFAULT false,
                        "avatar_update_timestamp"	INTEGER NOT NULL DEFAULT -1,
                        "notification_silent"	BOOLEAN DEFAULT false,
                        "sort"	INTEGER NOT NULL DEFAULT 0,
                        "last_online_timestamp"	INTEGER NOT NULL DEFAULT -1,
                        "last_online_timestamp_real"	INTEGER NOT NULL DEFAULT -1,
                        "added_timestamp"	INTEGER NOT NULL DEFAULT -1,
                        "is_relay"	BOOLEAN DEFAULT false,
                        "tox_public_key_string"	TEXT,
                        PRIMARY KEY("tox_public_key_string")
                    );
                    CREATE INDEX IF NOT EXISTS "index_num_on_BootstrapNodeEntryDB" ON "BootstrapNodeEntryDB" (
                        "num"
                    );
                    CREATE INDEX IF NOT EXISTS "index_udp_node_on_BootstrapNodeEntryDB" ON "BootstrapNodeEntryDB" (
                        "udp_node"
                    );
                    CREATE INDEX IF NOT EXISTS "index_ip_on_BootstrapNodeEntryDB" ON "BootstrapNodeEntryDB" (
                        "ip"
                    );
                    CREATE INDEX IF NOT EXISTS "index_port_on_BootstrapNodeEntryDB" ON "BootstrapNodeEntryDB" (
                        "port"
                    );
                    CREATE INDEX IF NOT EXISTS "index_key_hex_on_BootstrapNodeEntryDB" ON "BootstrapNodeEntryDB" (
                        "key_hex"
                    );
                    CREATE INDEX IF NOT EXISTS "index_who_invited__tox_public_key_string_on_ConferenceDB" ON "ConferenceDB" (
                        "who_invited__tox_public_key_string"
                    );
                    CREATE INDEX IF NOT EXISTS "index_name_on_ConferenceDB" ON "ConferenceDB" (
                        "name"
                    );
                    CREATE INDEX IF NOT EXISTS "index_peer_count_on_ConferenceDB" ON "ConferenceDB" (
                        "peer_count"
                    );
                    CREATE INDEX IF NOT EXISTS "index_own_peer_number_on_ConferenceDB" ON "ConferenceDB" (
                        "own_peer_number"
                    );
                    CREATE INDEX IF NOT EXISTS "index_kind_on_ConferenceDB" ON "ConferenceDB" (
                        "kind"
                    );
                    CREATE INDEX IF NOT EXISTS "index_tox_conference_number_on_ConferenceDB" ON "ConferenceDB" (
                        "tox_conference_number"
                    );
                    CREATE INDEX IF NOT EXISTS "index_conference_active_on_ConferenceDB" ON "ConferenceDB" (
                        "conference_active"
                    );
                    CREATE INDEX IF NOT EXISTS "index_notification_silent_on_ConferenceDB" ON "ConferenceDB" (
                        "notification_silent"
                    );
                    CREATE INDEX IF NOT EXISTS "index_conference_identifier_on_ConferenceMessage" ON "ConferenceMessage" (
                        "conference_identifier"
                    );
                    CREATE INDEX IF NOT EXISTS "index_tox_peerpubkey_on_ConferenceMessage" ON "ConferenceMessage" (
                        "tox_peerpubkey"
                    );
                    CREATE INDEX IF NOT EXISTS "index_tox_peername_on_ConferenceMessage" ON "ConferenceMessage" (
                        "tox_peername"
                    );
                    CREATE INDEX IF NOT EXISTS "index_direction_on_ConferenceMessage" ON "ConferenceMessage" (
                        "direction"
                    );
                    CREATE INDEX IF NOT EXISTS "index_TOX_MESSAGE_TYPE_on_ConferenceMessage" ON "ConferenceMessage" (
                        "TOX_MESSAGE_TYPE"
                    );
                    CREATE INDEX IF NOT EXISTS "index_TRIFA_MESSAGE_TYPE_on_ConferenceMessage" ON "ConferenceMessage" (
                        "TRIFA_MESSAGE_TYPE"
                    );
                    CREATE INDEX IF NOT EXISTS "index_rcvd_timestamp_on_ConferenceMessage" ON "ConferenceMessage" (
                        "rcvd_timestamp"
                    );
                    CREATE INDEX IF NOT EXISTS "index_is_new_on_ConferenceMessage" ON "ConferenceMessage" (
                        "is_new"
                    );
                    CREATE UNIQUE INDEX IF NOT EXISTS "index_conference_identifier_peer_pubkey_on_ConferencePeerCacheDB" ON "ConferencePeerCacheDB" (
                        "conference_identifier",
                        "peer_pubkey"
                    );
                    CREATE INDEX IF NOT EXISTS "index_conference_identifier_on_ConferencePeerCacheDB" ON "ConferencePeerCacheDB" (
                        "conference_identifier"
                    );
                    CREATE INDEX IF NOT EXISTS "index_peer_pubkey_on_ConferencePeerCacheDB" ON "ConferencePeerCacheDB" (
                        "peer_pubkey"
                    );
                    CREATE INDEX IF NOT EXISTS "index_peer_name_on_ConferencePeerCacheDB" ON "ConferencePeerCacheDB" (
                        "peer_name"
                    );
                    CREATE INDEX IF NOT EXISTS "index_last_update_timestamp_on_ConferencePeerCacheDB" ON "ConferencePeerCacheDB" (
                        "last_update_timestamp"
                    );
                    CREATE INDEX IF NOT EXISTS "index_kind_on_FileDB" ON "FileDB" (
                        "kind"
                    );
                    CREATE INDEX IF NOT EXISTS "index_direction_on_FileDB" ON "FileDB" (
                        "direction"
                    );
                    CREATE INDEX IF NOT EXISTS "index_tox_public_key_string_on_FileDB" ON "FileDB" (
                        "tox_public_key_string"
                    );
                    CREATE INDEX IF NOT EXISTS "index_path_name_on_FileDB" ON "FileDB" (
                        "path_name"
                    );
                    CREATE INDEX IF NOT EXISTS "index_file_name_on_FileDB" ON "FileDB" (
                        "file_name"
                    );
                    CREATE INDEX IF NOT EXISTS "index_filesize_on_FileDB" ON "FileDB" (
                        "filesize"
                    );
                    CREATE INDEX IF NOT EXISTS "index_is_in_VFS_on_FileDB" ON "FileDB" (
                        "is_in_VFS"
                    );
                    CREATE INDEX IF NOT EXISTS "index_tox_public_key_string_on_Filetransfer" ON "Filetransfer" (
                        "tox_public_key_string"
                    );
                    CREATE INDEX IF NOT EXISTS "index_direction_on_Filetransfer" ON "Filetransfer" (
                        "direction"
                    );
                    CREATE INDEX IF NOT EXISTS "index_file_number_on_Filetransfer" ON "Filetransfer" (
                        "file_number"
                    );
                    CREATE INDEX IF NOT EXISTS "index_kind_on_Filetransfer" ON "Filetransfer" (
                        "kind"
                    );
                    CREATE INDEX IF NOT EXISTS "index_state_on_Filetransfer" ON "Filetransfer" (
                        "state"
                    );
                    CREATE INDEX IF NOT EXISTS "index_ft_accepted_on_Filetransfer" ON "Filetransfer" (
                        "ft_accepted"
                    );
                    CREATE INDEX IF NOT EXISTS "index_ft_outgoing_started_on_Filetransfer" ON "Filetransfer" (
                        "ft_outgoing_started"
                    );
                    CREATE INDEX IF NOT EXISTS "index_path_name_on_Filetransfer" ON "Filetransfer" (
                        "path_name"
                    );
                    CREATE INDEX IF NOT EXISTS "index_file_name_on_Filetransfer" ON "Filetransfer" (
                        "file_name"
                    );
                    CREATE INDEX IF NOT EXISTS "index_message_id_on_Filetransfer" ON "Filetransfer" (
                        "message_id"
                    );
                    CREATE INDEX IF NOT EXISTS "index_key_on_TRIFADatabaseGlobals" ON "TRIFADatabaseGlobals" (
                        "key"
                    );
                    CREATE INDEX IF NOT EXISTS "index_value_on_TRIFADatabaseGlobals" ON "TRIFADatabaseGlobals" (
                        "value"
                    );
                    CREATE INDEX IF NOT EXISTS "index_TOX_CONNECTION_on_RelayListDB" ON "RelayListDB" (
                        "TOX_CONNECTION"
                    );
                    CREATE INDEX IF NOT EXISTS "index_TOX_CONNECTION_on_off_on_RelayListDB" ON "RelayListDB" (
                        "TOX_CONNECTION_on_off"
                    );
                    CREATE INDEX IF NOT EXISTS "index_own_relay_on_RelayListDB" ON "RelayListDB" (
                        "own_relay"
                    );
                    CREATE INDEX IF NOT EXISTS "index_last_online_timestamp_on_RelayListDB" ON "RelayListDB" (
                        "last_online_timestamp"
                    );
                    CREATE INDEX IF NOT EXISTS "index_tox_public_key_string_of_owner_on_RelayListDB" ON "RelayListDB" (
                        "tox_public_key_string_of_owner"
                    );
                    CREATE INDEX IF NOT EXISTS "index_message_id_on_Message" ON "Message" (
                        "message_id"
                    );
                    CREATE INDEX IF NOT EXISTS "index_tox_friendpubkey_on_Message" ON "Message" (
                        "tox_friendpubkey"
                    );
                    CREATE INDEX IF NOT EXISTS "index_direction_on_Message" ON "Message" (
                        "direction"
                    );
                    CREATE INDEX IF NOT EXISTS "index_TOX_MESSAGE_TYPE_on_Message" ON "Message" (
                        "TOX_MESSAGE_TYPE"
                    );
                    CREATE INDEX IF NOT EXISTS "index_TRIFA_MESSAGE_TYPE_on_Message" ON "Message" (
                        "TRIFA_MESSAGE_TYPE"
                    );
                    CREATE INDEX IF NOT EXISTS "index_state_on_Message" ON "Message" (
                        "state"
                    );
                    CREATE INDEX IF NOT EXISTS "index_ft_accepted_on_Message" ON "Message" (
                        "ft_accepted"
                    );
                    CREATE INDEX IF NOT EXISTS "index_ft_outgoing_started_on_Message" ON "Message" (
                        "ft_outgoing_started"
                    );
                    CREATE INDEX IF NOT EXISTS "index_filedb_id_on_Message" ON "Message" (
                        "filedb_id"
                    );
                    CREATE INDEX IF NOT EXISTS "index_filetransfer_id_on_Message" ON "Message" (
                        "filetransfer_id"
                    );
                    CREATE INDEX IF NOT EXISTS "index_rcvd_timestamp_on_Message" ON "Message" (
                        "rcvd_timestamp"
                    );
                    CREATE INDEX IF NOT EXISTS "index_rcvd_timestamp_ms_on_Message" ON "Message" (
                        "rcvd_timestamp_ms"
                    );
                    CREATE INDEX IF NOT EXISTS "index_send_retries_on_Message" ON "Message" (
                        "send_retries"
                    );
                    CREATE INDEX IF NOT EXISTS "index_is_new_on_Message" ON "Message" (
                        "is_new"
                    );
                    CREATE INDEX IF NOT EXISTS "index_msg_id_hash_on_Message" ON "Message" (
                        "msg_id_hash"
                    );
                    CREATE INDEX IF NOT EXISTS "index_raw_msgv2_bytes_on_Message" ON "Message" (
                        "raw_msgv2_bytes"
                    );
                    CREATE INDEX IF NOT EXISTS "index_msg_version_on_Message" ON "Message" (
                        "msg_version"
                    );
                    CREATE INDEX IF NOT EXISTS "index_resend_count_on_Message" ON "Message" (
                        "resend_count"
                    );
                    CREATE INDEX IF NOT EXISTS "index_value_on_TRIFADatabaseGlobalsNew" ON "TRIFADatabaseGlobalsNew" (
                        "value"
                    );
                    CREATE INDEX IF NOT EXISTS "index_alias_name_on_FriendList" ON "FriendList" (
                        "alias_name"
                    );
                    CREATE INDEX IF NOT EXISTS "index_TOX_CONNECTION_on_FriendList" ON "FriendList" (
                        "TOX_CONNECTION"
                    );
                    CREATE INDEX IF NOT EXISTS "index_TOX_CONNECTION_real_on_FriendList" ON "FriendList" (
                        "TOX_CONNECTION_real"
                    );
                    CREATE INDEX IF NOT EXISTS "index_TOX_CONNECTION_on_off_on_FriendList" ON "FriendList" (
                        "TOX_CONNECTION_on_off"
                    );
                    CREATE INDEX IF NOT EXISTS "index_TOX_CONNECTION_on_off_real_on_FriendList" ON "FriendList" (
                        "TOX_CONNECTION_on_off_real"
                    );
                    CREATE INDEX IF NOT EXISTS "index_TOX_USER_STATUS_on_FriendList" ON "FriendList" (
                        "TOX_USER_STATUS"
                    );
                    CREATE INDEX IF NOT EXISTS "index_avatar_update_on_FriendList" ON "FriendList" (
                        "avatar_update"
                    );
                    CREATE INDEX IF NOT EXISTS "index_avatar_update_timestamp_on_FriendList" ON "FriendList" (
                        "avatar_update_timestamp"
                    );
                    CREATE INDEX IF NOT EXISTS "index_notification_silent_on_FriendList" ON "FriendList" (
                        "notification_silent"
                    );
                    CREATE INDEX IF NOT EXISTS "index_sort_on_FriendList" ON "FriendList" (
                        "sort"
                    );
                    CREATE INDEX IF NOT EXISTS "index_last_online_timestamp_on_FriendList" ON "FriendList" (
                        "last_online_timestamp"
                    );
                    CREATE INDEX IF NOT EXISTS "index_added_timestamp_on_FriendList" ON "FriendList" (
                        "added_timestamp"
                    );
                    CREATE INDEX IF NOT EXISTS "index_is_relay_on_FriendList" ON "FriendList" (
                        "is_relay"
                    );
                """
        // @formatter:on
        run_multi_sql(update_001)
    }

    if (new_version == 2)
    {
        val update_001 =
            "alter table Message add ft_outgoing_queued BOOLEAN NOT NULL DEFAULT false;" + "\n" +
                    "CREATE INDEX index_ft_outgoing_queued_on_Message ON Message (ft_outgoing_queued);"
        run_multi_sql(update_001)
    }

    if (new_version == 3)
    {
        val update_001 =
            "alter table Message add msg_at_relay BOOLEAN NOT NULL DEFAULT false;" + "\n" +
                    "CREATE INDEX index_msg_at_relay_on_Message ON Message (msg_at_relay);"
        run_multi_sql(update_001)
    }

    if (new_version == 4)
    {
        val update_001 = "alter table FriendList add push_url TEXT DEFAULT NULL;" + "\n" +
                "CREATE INDEX index_push_url_on_FriendList ON FriendList (push_url);"
        run_multi_sql(update_001)
    }

    if (new_version == 5)
    {
        val update_001 = "alter table Message add msg_idv3_hash TEXT DEFAULT NULL;" + "\n" +
                "CREATE INDEX index_msg_idv3_hash_on_Message ON Message (msg_idv3_hash);"
        run_multi_sql(update_001)
        val update_002 = "alter table Message add sent_push INTEGER DEFAULT '0';" + "\n" +
                "CREATE INDEX index_sent_push_on_Message ON Message (sent_push);"
        run_multi_sql(update_002)

        val update_003 = "alter table FriendList add capabilities INTEGER DEFAULT '0';" + "\n" +
                "CREATE INDEX index_capabilities_on_FriendList ON FriendList (capabilities);"
        run_multi_sql(update_003)
        val update_004 = "alter table FriendList add msgv3_capability INTEGER DEFAULT '0';" + "\n" +
                "CREATE INDEX index_msgv3_capability_on_FriendList ON FriendList (msgv3_capability);"
        run_multi_sql(update_004)
    }

    if (new_version == 6)
    {
        // @formatter:off
        val update_001 = "CREATE TABLE IF NOT EXISTS GroupDB ( " +
                "who_invited__tox_public_key_string  TEXT, "+
                "name TEXT, "+
                "topic TEXT, "+
                "peer_count  INTEGER NOT NULL DEFAULT -1, "+
                "own_peer_number  INTEGER NOT NULL DEFAULT -1, "+
                "privacy_state INTEGER NOT NULL DEFAULT 1, "+
                "tox_group_number  INTEGER NOT NULL DEFAULT -1, "+
                "group_active BOOLEAN DEFAULT false, "+
                "notification_silent BOOLEAN DEFAULT false, "+
                "group_identifier TEXT, "+
                "PRIMARY KEY(\"group_identifier\") "+
                ");"
        // @formatter:on
        run_multi_sql(update_001)

        // @formatter:off
        val update_002 = "CREATE TABLE IF NOT EXISTS GroupMessage ( " +
                "message_id_tox  TEXT , "+
                "group_identifier  TEXT NOT NULL DEFAULT \"-1\", "+
                "tox_group_peer_pubkey  TEXT NOT NULL, "+
                "private_message  INTEGER NOT NULL DEFAULT 0, "+
                "tox_group_peername  TEXT, "+
                "direction  INTEGER NOT NULL , "+
                "TOX_MESSAGE_TYPE  INTEGER NOT NULL , "+
                "TRIFA_MESSAGE_TYPE  INTEGER NOT NULL DEFAULT 0 , "+
                "sent_timestamp  INTEGER, "+
                "rcvd_timestamp  INTEGER, "+
                "read   BOOLEAN NOT NULL DEFAULT 0 , "+
                "is_new   BOOLEAN NOT NULL DEFAULT 1 , "+
                "text  TEXT, "+
                "was_synced   BOOLEAN NOT NULL DEFAULT 0 , "+
                "msg_id_hash   TEXT, "+
                "id INTEGER, "+
                "PRIMARY KEY(\"id\") "+
                ");"
        // @formatter:on
        run_multi_sql(update_002)
    }

    if (new_version == 7)
    {
        // @formatter:off
        val update_001 = "CREATE INDEX IF NOT EXISTS index_message_id_tox_on_GroupMessage ON GroupMessage (message_id_tox);\n" +
                "CREATE INDEX IF NOT EXISTS index_group_identifier_tox_on_GroupMessage ON GroupMessage (group_identifier);\n" +
                "CREATE INDEX IF NOT EXISTS index_tox_group_peer_pubkey_on_GroupMessage ON GroupMessage (tox_group_peer_pubkey);\n" +
                "CREATE INDEX IF NOT EXISTS index_direction_on_GroupMessage ON GroupMessage (direction);\n" +
                "CREATE INDEX IF NOT EXISTS index_TOX_MESSAGE_TYPE_on_GroupMessage ON GroupMessage (TOX_MESSAGE_TYPE);\n" +
                "CREATE INDEX IF NOT EXISTS index_TRIFA_MESSAGE_TYPE_on_GroupMessage ON GroupMessage (TRIFA_MESSAGE_TYPE);\n" +
                "CREATE INDEX IF NOT EXISTS index_rcvd_timestamp_on_GroupMessage ON GroupMessage (rcvd_timestamp);\n" +
                "CREATE INDEX IF NOT EXISTS index_sent_timestamp_on_GroupMessage ON GroupMessage (sent_timestamp);\n" +
                "CREATE INDEX IF NOT EXISTS index_private_message_on_GroupMessage ON GroupMessage (private_message);\n" +
                "CREATE INDEX IF NOT EXISTS index_tox_group_peername_on_GroupMessage ON GroupMessage (tox_group_peername);\n" +
                "CREATE INDEX IF NOT EXISTS index_was_synced_on_GroupMessage ON GroupMessage (was_synced);\n" +
                "CREATE INDEX IF NOT EXISTS index_is_new_on_GroupMessage ON GroupMessage (is_new);\n" +
                "CREATE INDEX IF NOT EXISTS index_msg_id_hash_on_GroupMessage ON GroupMessage (msg_id_hash);"
        run_multi_sql(update_001)
        // @formatter:on
    }

    if (new_version == 8)
    {
        // @formatter:off
        val update_001 = "CREATE INDEX IF NOT EXISTS index_who_invited__tox_public_key_string_on_GroupDB ON GroupDB (who_invited__tox_public_key_string);\n" +
                "CREATE INDEX IF NOT EXISTS index_name_on_GroupDB ON GroupDB (name);\n" +
                "CREATE INDEX IF NOT EXISTS index_topic_on_GroupDB ON GroupDB (topic);\n" +
                "CREATE INDEX IF NOT EXISTS index_peer_count_on_GroupDB ON GroupDB (peer_count);\n" +
                "CREATE INDEX IF NOT EXISTS index_own_peer_number_on_GroupDB ON GroupDB (own_peer_number);\n" +
                "CREATE INDEX IF NOT EXISTS index_privacy_state_on_GroupDB ON GroupDB (privacy_state);\n" +
                "CREATE INDEX IF NOT EXISTS index_tox_group_number_on_GroupDB ON GroupDB (tox_group_number);\n" +
                "CREATE INDEX IF NOT EXISTS index_group_active_on_GroupDB ON GroupDB (group_active);\n" +
                "CREATE INDEX IF NOT EXISTS index_notification_silent_on_GroupDB ON GroupDB (notification_silent);\n" +
                "CREATE INDEX IF NOT EXISTS index_group_identifier_on_GroupDB ON GroupDB (group_identifier);"

        run_multi_sql(update_001)
        // @formatter:on
    }

    if (new_version == 9)
    {
        val update_001 = "alter table Filetransfer add tox_file_id_hex TEXT DEFAULT NULL;" + "\n" +
                "CREATE INDEX index_tox_file_id_hex_on_Filetransfer ON Filetransfer (tox_file_id_hex);"
        run_multi_sql(update_001)
    }

    if (new_version == 10)
    {
        val update_001 = "alter table Message add filetransfer_kind INTEGER NOT NULL DEFAULT 0;" + "\n" +
                "CREATE INDEX index_filetransfer_kind_on_Message ON Message (filetransfer_kind);"
        run_multi_sql(update_001)
    }

    if (new_version == 11)
    {
        val update_001 = "alter table GroupMessage add path_name TEXT DEFAULT NULL;" + "\n" +
                "CREATE INDEX index_path_name_on_GroupMessage ON GroupMessage (path_name);"
        run_multi_sql(update_001)
        val update_002 = "alter table GroupMessage add file_name TEXT DEFAULT NULL;" + "\n" +
                "CREATE INDEX index_file_name_on_GroupMessage ON GroupMessage (file_name);"
        run_multi_sql(update_002)
        val update_003 = "alter table GroupMessage add filename_fullpath TEXT DEFAULT NULL;" + "\n" +
                "CREATE INDEX index_filename_fullpath_on_GroupMessage ON GroupMessage (filename_fullpath);"
        run_multi_sql(update_003)
        val update_004 = "alter table GroupMessage add filesize INTEGER NOT NULL DEFAULT 0;" + "\n" +
                "CREATE INDEX index_filesize_on_GroupMessage ON GroupMessage (filesize);"
        run_multi_sql(update_004)
    }

    if (new_version == 12)
    {
        val update_001 = "alter table FriendList add avatar_hex TEXT DEFAULT NULL;" + "\n" +
                "alter table FriendList add avatar_hash_hex TEXT DEFAULT NULL;"
        run_multi_sql(update_001)
    }

    if (new_version == 13)
    {
        val update_004 = "alter table GroupMessage add tox_group_peer_role INTEGER NOT NULL DEFAULT '-1';" + "\n" +
                "CREATE INDEX index_tox_group_peer_role_on_GroupMessage ON GroupMessage (tox_group_peer_role);"
        run_multi_sql(update_004)
    }

    if (new_version == 14)
    {
        val update_004 = "alter table GroupMessage add sent_privately_to_tox_group_peer_pubkey TEXT DEFAULT NULL;" + "\n" +
                "CREATE INDEX index_sent_privately_to_tox_group_peer_pubkey_on_GroupMessage ON GroupMessage (sent_privately_to_tox_group_peer_pubkey);";
        run_multi_sql(update_004);
    }
}

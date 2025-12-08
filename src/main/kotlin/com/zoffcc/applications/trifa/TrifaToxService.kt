@file:Suppress("ConvertToStringTemplate", "LocalVariableName", "ReplaceSizeCheckWithIsNotEmpty", "FunctionName")

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
import com.zoffcc.applications.trifa.HelperFiletransfer.start_outgoing_ft
import com.zoffcc.applications.trifa.HelperFriend.friend_call_push_url
import com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_pubkey
import com.zoffcc.applications.trifa.HelperGeneric.get_friend_msgv3_capability
import com.zoffcc.applications.trifa.HelperGeneric.is_friend_online_real
import com.zoffcc.applications.trifa.HelperGeneric.tox_friend_resend_msgv3_wrapper
import com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper
import com.zoffcc.applications.trifa.HelperGroup.hex_to_bytes
import com.zoffcc.applications.trifa.HelperMessage.tox_friend_send_message_wrapper
import com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_messageid
import com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_no_read_recvedts
import com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_resend_count
import com.zoffcc.applications.trifa.MainActivity.Companion.DB_PREF__send_push_notifications
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
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_number_groups
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_peerlist
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_privacy_state
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_is_connected
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_count
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_get_connection_status
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_get_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_get_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_get_role
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_iterate
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_iteration_interval
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_kill
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_get_connection_status
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_get_friend_list
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

                orma = OrmaDatabase(PREF__database_files_dir + "/main.db", db_password, PREF__DB_wal_mode);
                OrmaDatabase.init()
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
                                    // Log.i(TAG, "=====>>>>> tox_iteration_interval: "+ TOX_ITERATE_MS_MIN_NORMAL)
                                }
                                else
                                {
                                    sleep(tox_iteration_interval_ms)
                                    if (tox_iteration_interval_ms != 50L)
                                    {
                                        Log.i(TAG, "=====>>>>> tox_iteration_interval: " + tox_iteration_interval_ms)
                                    }
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
                                Log.i(TAG, "resend_old_messages:RET:01:" +
                                            get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey))
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

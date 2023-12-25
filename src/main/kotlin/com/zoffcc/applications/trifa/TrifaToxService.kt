package com.zoffcc.applications.trifa

import avstatestore
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB.bootstrap_node_list
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB.get_tcprelay_nodelist_from_db
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB.get_udp_nodelist_from_db
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB.tcprelay_node_list
import com.zoffcc.applications.sorm.FriendList
import com.zoffcc.applications.sorm.Message
import com.zoffcc.applications.sorm.OrmaDatabase
import com.zoffcc.applications.trifa.HelperFiletransfer.start_outgoing_ft
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
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__udp_enabled
import com.zoffcc.applications.trifa.MainActivity.Companion.add_tcp_relay_single_wrapper
import com.zoffcc.applications.trifa.MainActivity.Companion.audio_queue_play_trigger
import com.zoffcc.applications.trifa.MainActivity.Companion.bootstrap_single_wrapper
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
import globalstore
import grouppeerstore
import groupstore
import online_button_text_wrapper
import org.briarproject.briar.desktop.contact.ContactItem
import org.briarproject.briar.desktop.contact.GroupItem
import org.briarproject.briar.desktop.contact.GroupPeerItem
import set_tox_running_state
import toxdatastore
import unlock_data_dir_input
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
                com.zoffcc.applications.sorm.OrmaDatabase.init()
                // ------ correct startup order ------
                orma = com.zoffcc.applications.sorm.OrmaDatabase()
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
                        if ((last_resend_pending_messages4_ms + (5 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages4_ms = System.currentTimeMillis();
                            // TODO // resend_push_for_v3_messages();
                        }

                        if ((last_resend_pending_messages0_ms + (30 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages0_ms = System.currentTimeMillis();
                            resend_old_messages(null);
                        }

                        if ((last_resend_pending_messages1_ms + (30 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages1_ms = System.currentTimeMillis();
                            resend_v3_messages(null);
                        }

                        if ((last_resend_pending_messages2_ms + (30 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages2_ms = System.currentTimeMillis();
                            resend_v2_messages(false);
                        }

                        if ((last_resend_pending_messages3_ms + (120 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages3_ms = System.currentTimeMillis();
                            resend_v2_messages(true);
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
                update_savedata_file_wrapper()

                is_tox_started = false
                set_tox_running_state("stopped")
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
                orma = null
                com.zoffcc.applications.sorm.OrmaDatabase.shutdown()
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

    fun tox_audio_play_thread_start()
    {
        Log.i(TAG, "[]tox_audio_frame:starting Thread")
        tox_audio_play_thread = object : Thread()
        {
            override fun run()
            {
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
                                // Log.i(TAG, "[]tox_audio_frame:trigger:" + tox_audio_in_queue.size)
                            }
                            else
                            {
                                if (tox_a_queue_stop_trigger)
                                {
                                    if (tox_audio_in_queue.size >= 6)
                                    {
                                        tox_a_queue_stop_trigger = false
                                        // Log.i(TAG, "[]tox_audio_frame:release:" + tox_audio_in_queue.size)
                                    }
                                    else
                                    {
                                        //Log.i(TAG, "[]tox_audio_frame:+++++++++:" + tox_a_queue_play_trigger + " "
                                        //+ tox_audio_in_queue.size + " " + tox_audio_in_queue.remainingCapacity())
                                        sleep(20)
                                    }
                                } else
                                {
                                    val buf: ByteArray = tox_audio_in_queue.poll()
                                    if (buf != null)
                                    {
                                        try
                                        {
                                            val want_bytes = buf.size
                                            val sample_count = want_bytes / 2
                                            try
                                            {
                                                AudioSelectOutBox.semaphore_audio_out_convert.acquire_passthru()
                                                if (AudioSelectOutBox.semaphore_audio_out_convert_active_threads >= AudioSelectOutBox.semaphore_audio_out_convert_max_active_threads)
                                                {
                                                    Log.i(TAG, "[]tox_audio_frame:too many threads running: " + AudioSelectOutBox.semaphore_audio_out_convert_active_threads)
                                                    AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                                    continue
                                                }
                                                AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                            } catch (e: java.lang.Exception)
                                            {
                                                AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                            }

                                            val t_tox_audio_pcm_play = Thread{
                                                try
                                                {
                                                    AudioSelectOutBox.semaphore_audio_out_convert.acquire_passthru()
                                                    AudioSelectOutBox.semaphore_audio_out_convert_active_threads++
                                                    AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                                } catch (e: java.lang.Exception)
                                                {
                                                    AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                                }
                                                // HINT: this acutally plays incoming Audio
                                                // HINT: this may block!!
                                                try
                                                {
                                                    val bytes_actually_written = AudioSelectOutBox.sourceDataLine.write(buf, 0, want_bytes)
                                                    if (bytes_actually_written != want_bytes)
                                                    {
                                                        Log.i(TAG, "[]tox_audio_frame:bytes_actually_written=" + bytes_actually_written + " want_bytes=" + want_bytes)
                                                    }
                                                } catch (e: java.lang.Exception)
                                                {
                                                    Log.i(TAG, "[]tox_audio_frame:sourceDataLine.write:EE:" + e.message) // e.printStackTrace();
                                                }
                                                try
                                                {
                                                    AudioSelectOutBox.semaphore_audio_out_convert.acquire_passthru()
                                                    AudioSelectOutBox.semaphore_audio_out_convert_active_threads--
                                                    AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                                } catch (e: java.lang.Exception)
                                                {
                                                    Log.i(TAG, "[]tox_audio_frame:--:EEEEEE")
                                                    AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                                }
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
                                            t_tox_audio_pcm_play.start()
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
                        sleep(20)
                    }
                } catch (_: Exception)
                {
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
                    val sleep_millis: Long = 40
                    var sleep_millis_current: Long = sleep_millis
                    var d1: Long = 0
                    val sampling_rate = 48000
                    val channels = 1
                    val bytes_in_40ms = 1920
                    // val sample_count = bytes_in_40ms / 2
                    audio_queue_play_trigger = true
                    while (ngc_audio_play_thread_running)
                    {
                        // -- play incoming bytes --
                        // -- play incoming bytes --
                        try
                        {
                            if ((ngc_audio_in_queue.size < 3) && (!audio_queue_play_trigger))
                            {
                                audio_queue_play_trigger = true
                                // Log.i(TAG, "()PLAY_ngc_audio_frame:trigger:" + ngc_audio_in_queue.size)
                            }
                            else
                            {
                                if (audio_queue_play_trigger)
                                {
                                    if (ngc_audio_in_queue.size >= 6)
                                    {
                                        audio_queue_play_trigger = false
                                        // Log.i(TAG, "()PLAY_ngc_audio_frame:release:")
                                    }
                                    else
                                    {
                                        //Log.i(TAG, "()PLAY_ngc_audio_frame:+++++++++:" + audio_queue_play_trigger + " "
                                        //+ ngc_audio_in_queue.size + " " + ngc_audio_in_queue.remainingCapacity())
                                        sleep(20)
                                    }
                                } else
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
                                            try
                                            {
                                                AudioSelectOutBox.semaphore_audio_out_convert.acquire_passthru()
                                                if (AudioSelectOutBox.semaphore_audio_out_convert_active_threads >= AudioSelectOutBox.semaphore_audio_out_convert_max_active_threads)
                                                {
                                                    Log.i(TAG, "()PLAY_ngc_audio_frame:too many threads running: " + AudioSelectOutBox.semaphore_audio_out_convert_active_threads)
                                                    AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                                    continue
                                                }
                                                AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                            } catch (e: java.lang.Exception)
                                            {
                                                AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                            }

                                            val t_ngc_audio_pcm_play = Thread{
                                                try
                                                {
                                                    AudioSelectOutBox.semaphore_audio_out_convert.acquire_passthru()
                                                    AudioSelectOutBox.semaphore_audio_out_convert_active_threads++
                                                    AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                                } catch (e: java.lang.Exception)
                                                {
                                                    AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                                }
                                                // HINT: this acutally plays incoming Audio
                                                // HINT: this may block!!
                                                try
                                                {
                                                    val bytes_actually_written = AudioSelectOutBox.sourceDataLine.write(buf, 0, want_bytes)
                                                    if (bytes_actually_written != want_bytes)
                                                    {
                                                        Log.i(TAG, "()PLAY_ngc_audio_frame:bytes_actually_written=" + bytes_actually_written + " want_bytes=" + want_bytes)
                                                    }
                                                } catch (e: java.lang.Exception)
                                                {
                                                    Log.i(TAG, "()PLAY_ngc_audio_frame:sourceDataLine.write:EE:" + e.message) // e.printStackTrace();
                                                }
                                                try
                                                {
                                                    AudioSelectOutBox.semaphore_audio_out_convert.acquire_passthru()
                                                    AudioSelectOutBox.semaphore_audio_out_convert_active_threads--
                                                    AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                                } catch (e: java.lang.Exception)
                                                {
                                                    Log.i(TAG, "()PLAY_ngc_audio_frame:--:EEEEEE")
                                                    AudioSelectOutBox.semaphore_audio_out_convert.release_passthru()
                                                }
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
                                            t_ngc_audio_pcm_play.start()
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
                        sleep(20)
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

            if (PREF__udp_enabled == 1)
            {
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
            }
            else
            {
                bootstrap_single_wrapper("127.0.0.1", 7766, "2AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA1")
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

        fun bootstrap_me__obsolete()
        {
            Log.i(TAG, "bootstrap_me") // ----- UDP ------
            if (PREF__udp_enabled == 1)
            {
                bootstrap_single_wrapper("144.217.167.73",33445,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C")
                bootstrap_single_wrapper("tox.abilinski.com",33445,"10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E")
                bootstrap_single_wrapper("198.199.98.108",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F")
                bootstrap_single_wrapper("2604:a880:1:20::32f:1001",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F")
                bootstrap_single_wrapper("tox.kurnevsky.net",33445,"82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23")
                bootstrap_single_wrapper("2a03:b0c0:0:1010::4c:5001",33445,"82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23")
                bootstrap_single_wrapper("205.185.115.131",53,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68")
                bootstrap_single_wrapper("tox2.abilinski.com",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D")
                bootstrap_single_wrapper("2604:180:1:4ab::2",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D")
                bootstrap_single_wrapper("46.101.197.175",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707")
                bootstrap_single_wrapper("2a03:b0c0:3:d0::ac:5001",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707")
                bootstrap_single_wrapper("tox1.mf-net.eu",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506")
                bootstrap_single_wrapper("2a01:4f8:c2c:89f7::1",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506")
                bootstrap_single_wrapper("tox2.mf-net.eu",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F")
                bootstrap_single_wrapper("2001:41d0:8:7a96::1",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F")
                bootstrap_single_wrapper("195.201.7.101",33445,"B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107")
                bootstrap_single_wrapper("tox4.plastiras.org",33445,"836D1DA2BE12FE0E669334E437BE3FB02806F1528C2B2782113E0910C7711409")
                bootstrap_single_wrapper("188.225.9.167",33445,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67")
                bootstrap_single_wrapper("209:dead:ded:4991:49f3:b6c0:9869:3019",33445,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67")
                bootstrap_single_wrapper("122.116.39.151",33445,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E")
                bootstrap_single_wrapper("2001:b011:8:2f22:1957:7f9d:e31f:96dd",33445,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E")
                bootstrap_single_wrapper("tox3.plastiras.org",33445,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64")
                bootstrap_single_wrapper("2a01:4f8:211:c97::2",33445,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64")
                bootstrap_single_wrapper("104.225.141.59",43334,"933BA20B2E258B4C0D475B6DECE90C7E827FE83EFA9655414E7841251B19A72C")
                bootstrap_single_wrapper("139.162.110.188",33445,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55")
                bootstrap_single_wrapper("2400:8902::f03c:93ff:fe69:bf77",33445,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55")
                bootstrap_single_wrapper("198.98.49.206",33445,"28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255")
            } else
            {
                bootstrap_single_wrapper("127.0.0.1", 7766, "2AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA1")
            }
            // ----- UDP ------
            //
            // ----- TCP ------
            add_tcp_relay_single_wrapper("144.217.167.73",33445,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C")
            add_tcp_relay_single_wrapper("144.217.167.73",3389,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C")
            add_tcp_relay_single_wrapper("tox.abilinski.com",33445,"10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E")
            add_tcp_relay_single_wrapper("198.199.98.108",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F")
            add_tcp_relay_single_wrapper("2604:a880:1:20::32f:1001",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F")
            add_tcp_relay_single_wrapper("198.199.98.108",3389,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F")
            add_tcp_relay_single_wrapper("2604:a880:1:20::32f:1001",3389,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F")
            add_tcp_relay_single_wrapper("205.185.115.131",443,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68")
            add_tcp_relay_single_wrapper("205.185.115.131",3389,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68")
            add_tcp_relay_single_wrapper("205.185.115.131",53,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68")
            add_tcp_relay_single_wrapper("205.185.115.131",33445,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68")
            add_tcp_relay_single_wrapper("tox2.abilinski.com",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D")
            add_tcp_relay_single_wrapper("2604:180:1:4ab::2",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D")
            add_tcp_relay_single_wrapper("46.101.197.175",3389,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707")
            add_tcp_relay_single_wrapper("2a03:b0c0:3:d0::ac:5001",3389,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707")
            add_tcp_relay_single_wrapper("46.101.197.175",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707")
            add_tcp_relay_single_wrapper("2a03:b0c0:3:d0::ac:5001",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707")
            add_tcp_relay_single_wrapper("tox1.mf-net.eu",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506")
            add_tcp_relay_single_wrapper("2a01:4f8:c2c:89f7::1",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506")
            add_tcp_relay_single_wrapper("tox1.mf-net.eu",3389,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506")
            add_tcp_relay_single_wrapper("2a01:4f8:c2c:89f7::1",3389,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506")
            add_tcp_relay_single_wrapper("tox2.mf-net.eu",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F")
            add_tcp_relay_single_wrapper("2001:41d0:8:7a96::1",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F")
            add_tcp_relay_single_wrapper("tox2.mf-net.eu",3389,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F")
            add_tcp_relay_single_wrapper("2001:41d0:8:7a96::1",3389,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F")
            add_tcp_relay_single_wrapper("195.201.7.101",33445,"B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107")
            // ----- TCP ------
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
        fun safe_string(`in`: ByteArray?): String
        { // Log.i(TAG, "safe_string:in=" + in);
            var out = ""
            try
            {
                out = String(`in`!!, charset("UTF-8")) // Best way to decode using "UTF-8"
            } catch (e: Exception)
            {
                e.printStackTrace()
                Log.i(TAG, "safe_string:EE:" + e.message)
                try
                {
                    out = String(`in`!!)
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
                    val peer_pubkey = tox_group_peer_get_public_key(groupnum, it)
                    val peer_name = tox_group_peer_get_name(groupnum, it)
                    val peer_connection_status = tox_group_peer_get_connection_status(groupnum, it)
                    val peer_role = tox_group_peer_get_role(groupnum, it)
                    try
                    {
                        grouppeerstore.add(item = GroupPeerItem(
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
                var m_v1: List<Message>? = null
                m_v1 = if (friend_pubkey != null)
                {
                    orma!!.selectFromMessage().directionEq(1).msg_versionEq(0).tox_friendpubkeyEq(friend_pubkey).TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value).resend_countLt(MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION).readEq(false).orderBySent_timestampAsc().toList()
                } else
                {
                    orma!!.selectFromMessage().directionEq(1).msg_versionEq(0).TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value).resend_countLt(MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION).readEq(false).orderBySent_timestampAsc().toList()
                }
                if (m_v1 != null && m_v1.size > 0)
                {
                    Log.i(TAG, "resend_v3_messages: we have " + m_v1.size + " messages to resend")
                    val ii = m_v1.iterator()
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
                        if (get_friend_msgv3_capability(m_resend_v1.tox_friendpubkey) != 1L)
                        {
                            continue
                        }
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
                var m_v0: List<Message>? = null
                m_v0 = if (friend_pubkey != null)
                {
                    // HINT: this is the generic resend for all friends, that happens in regular intervals
                    //       only resend if the original sent timestamp is at least 25 seconds in the past
                    //       to try to avoid resending when the read receipt is very late.
                    orma!!.selectFromMessage().directionEq(1).TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value).msg_versionEq(0).tox_friendpubkeyEq(friend_pubkey).readEq(false).resend_countLt(2).orderBySent_timestampAsc().sent_timestampLt(cutoff_sent_time).toList()
                } else
                {
                    // HINT: this is the specific resend for 1 friend only, when that friend comes online
                    orma!!.selectFromMessage().directionEq(1).TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value).msg_versionEq(0).readEq(false).resend_countLt(2).orderBySent_timestampAsc().toList()
                }
                if (m_v0 != null && m_v0.size > 0)
                {
                    Log.i(TAG, "resend_old_messages: we have " + m_v0.size + " messages to resend")
                    val ii = m_v0.iterator()
                    while (ii.hasNext())
                    {
                        val m_resend_v0 = ii.next()
                        if (friend_pubkey == null)
                        {
                            if (is_friend_online_real(tox_friend_by_public_key(m_resend_v0.tox_friendpubkey)) == 0)
                            {
                                // Log.i(TAG, "resend_old_messages:RET:01:" +
                                //            get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey));
                                continue
                            }
                        }
                        if (get_friend_msgv3_capability(m_resend_v0.tox_friendpubkey) == 1L)
                        {
                            // Log.i(TAG, "resend_old_messages:RET:02:" +
                            //            get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey));
                            continue
                        }
                        // Log.i(TAG, "resend_old_messages:tox_friend_resend_msgv3_wrapper:" + m_resend_v0.text + " : m=" +
                        //            m_resend_v0 + " : " + get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey));
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
                val m_v1 = orma!!.selectFromMessage().
                directionEq(1).TRIFA_MESSAGE_TYPEEq(TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value)
                    .msg_versionEq(1).readEq(false).
                    msg_at_relayEq(at_relay).orderBySent_timestampAsc().toList()
                if (m_v1 != null && m_v1.size > 0)
                {
                    Log.i(TAG, "resend_v2_messages: we have " + m_v1.size + " messages to resend")
                    var ii: Iterator<Message> = m_v1.iterator()
                    var m_counter = 0
                    while (ii.hasNext())
                    {
                        val m_resend_v2 = ii.next()
                        m_counter++

                        Log.i(TAG, "resend_v2_messages: " + m_counter + ": friend="
                                + get_friend_name_from_pubkey(m_resend_v2.tox_friendpubkey) + " text=" + m_resend_v2.text)
                    }
                    ii = m_v1.iterator()
                    while (ii.hasNext())
                    {
                        val m_resend_v2 = ii.next()
                        if (is_friend_online_real(tox_friend_by_public_key(m_resend_v2.tox_friendpubkey)) == 0)
                        {
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
                                    Log.i(TAG, "resend_v2_messages:1: message_id=" + res)
                                } else
                                {
                                    m_resend_v2.resend_count = 0 // sending was NOT successfull
                                    m_resend_v2.message_id = -1
                                    Log.i(TAG, "resend_v2_messages:2: message_id=" + "-1")
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
            Log.i(TAG, "friend:" + it)

            var f: FriendList? = null
            val f_pubkey = tox_friend_get_public_key(it)
            var fl: MutableList<FriendList>? = null
            if (f_pubkey != null)
            {
                fl = orma!!.selectFromFriendList().
                    tox_public_key_stringEq(f_pubkey.toUpperCase()).toList()
            }
            if ((fl != null) && (fl.size > 0))
            {
                f = fl.get(0)
            }
            else
            {
                f = null;
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
                contactstore.add(item = ContactItem(name = fname, isConnected = 0, pubkey = tox_friend_get_public_key(it)!!))
            } catch (_: Exception)
            {
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
                    groupstore.add(item = GroupItem(numPeers = group_num_peers.toInt(), name = group_name, isConnected = is_connected, groupId = group_identifier, privacyState = new_privacy_state))
                } catch (_: Exception)
                {
                }
            }
            conf_++
        }
    }
}

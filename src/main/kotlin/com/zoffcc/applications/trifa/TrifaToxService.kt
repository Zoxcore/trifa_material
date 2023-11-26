package com.zoffcc.applications.trifa

import com.zoffcc.applications.sorm.BootstrapNodeEntryDB
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB.bootstrap_node_list
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB.get_tcprelay_nodelist_from_db
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB.get_udp_nodelist_from_db
import com.zoffcc.applications.sorm.BootstrapNodeEntryDB.tcprelay_node_list
import com.zoffcc.applications.sorm.Message
import com.zoffcc.applications.sorm.OrmaDatabase
import com.zoffcc.applications.trifa.HelperFiletransfer.start_outgoing_ft
import com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__udp_enabled
import com.zoffcc.applications.trifa.MainActivity.Companion.add_tcp_relay_single_wrapper
import com.zoffcc.applications.trifa.MainActivity.Companion.bootstrap_single_wrapper
import com.zoffcc.applications.trifa.MainActivity.Companion.init_tox_callbacks
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
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_get_friend_list
import com.zoffcc.applications.trifa.TRIFAGlobals.GROUP_ID_LENGTH
import com.zoffcc.applications.trifa.TRIFAGlobals.USE_MAX_NUMBER_OF_BOOTSTRAP_NODES
import com.zoffcc.applications.trifa.TRIFAGlobals.USE_MAX_NUMBER_OF_BOOTSTRAP_TCP_RELAYS
import com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_outgoung_ft_ts
import contactstore
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

                if (!old_is_tox_started)
                {
                    TRIFAGlobals.bootstrapping = true
                    Log.i(TAG, "bootrapping:set to true")
                    bootstrap_me()
                } // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                var tox_iteration_interval_ms = tox_iteration_interval()
                Log.i(TAG, "tox_iteration_interval_ms=$tox_iteration_interval_ms")
                tox_iterate() // ------- MAIN TOX LOOP ---------------------------------------------------------------
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
                            sleep(0, 250)
                        } else {
                            sleep(tox_iteration_interval_ms)
                        }
                    } catch (e: InterruptedException)
                    {
                        e.printStackTrace()
                    } catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                    tox_iterate() // Log.i(TAG, "=====>>>>> tox_iterate()");
                    tox_iteration_interval_ms = tox_iteration_interval()
                    // --- start queued outgoing FTs here --------------
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
    }

    companion object
    {
        const val TAG = "trifa.ToxService"
        var ToxServiceThread: Thread? = null
        var stop_me = false
        var is_tox_started = false
        public var orma: OrmaDatabase? = null
        public var TOX_SERVICE_STARTED = false
        var last_resend_pending_messages0_ms: Long = -1
        var last_resend_pending_messages1_ms: Long = -1
        var last_resend_pending_messages2_ms: Long = -1
        var last_resend_pending_messages3_ms: Long = -1
        var last_resend_pending_messages4_ms: Long = -1
        var last_start_queued_fts_ms: Long = -1

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
            var fname = tox_friend_get_name(it)
            if (fname == null)
            {
                fname = "Friend"
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

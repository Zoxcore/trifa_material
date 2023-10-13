package com.zoffcc.applications.trifa

import com.zoffcc.applications.sorm.OrmaDatabase.init
import com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper
import com.zoffcc.applications.trifa.MainActivity.Companion.PREF__udp_enabled
import com.zoffcc.applications.trifa.MainActivity.Companion.add_tcp_relay_single_wrapper
import com.zoffcc.applications.trifa.MainActivity.Companion.bootstrap_single_wrapper
import com.zoffcc.applications.trifa.MainActivity.Companion.init_tox_callbacks
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_iterate
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_iteration_interval
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_kill
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_get_friend_list
import contactstore
import org.briarproject.briar.desktop.contact.ContactItem
import set_tox_running_state
import toxdatastore
import unlock_data_dir_input

class TrifaToxService {
    fun tox_thread_start_fg() {
        Log.i(TAG, "tox_thread_start_fg")
        ToxServiceThread = object : Thread() {
            override fun run() {

                com.zoffcc.applications.sorm.OrmaDatabase.init()

                // ------ correct startup order ------
                val old_is_tox_started = is_tox_started
                Log.i(TAG, "is_tox_started:==============================")
                Log.i(TAG, "is_tox_started=" + is_tox_started)
                Log.i(TAG, "is_tox_started:==============================")
                is_tox_started = true
                if (!old_is_tox_started) {
                    init_tox_callbacks()
                    update_savedata_file_wrapper()
                }
                // ------ correct startup order ------

                clear_friend()
                load_friends()

                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                if (!old_is_tox_started) {
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
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                set_tox_running_state("running")
                while (!stop_me) {
                    try {
                        sleep(tox_iteration_interval_ms)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    tox_iterate()
                    // Log.i(TAG, "=====>>>>> tox_iterate()");
                    tox_iteration_interval_ms = tox_iteration_interval()
                }
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                try {
                    sleep(100) // wait a bit, for "something" to finish up in the native code
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    tox_kill()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    sleep(100) // wait a bit, for "something" to finish up in the native code
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                update_savedata_file_wrapper()
                is_tox_started = false
                set_tox_running_state("stopped")
                clear_friend()
                com.zoffcc.applications.sorm.OrmaDatabase.shutdown()
                unlock_data_dir_input()
                try {
                    toxdatastore.updateToxID("")
                } catch (_: Exception) {
                }
            }
        }
        (ToxServiceThread as Thread).start()
    }

    companion object {
        const val TAG = "trifa.ToxService"
        var ToxServiceThread: Thread? = null
        var stop_me = false
        var is_tox_started = false
        public var TOX_SERVICE_STARTED = false
        var last_resend_pending_messages0_ms: Long = -1
        var last_resend_pending_messages1_ms: Long = -1
        var last_resend_pending_messages2_ms: Long = -1
        var last_resend_pending_messages3_ms: Long = -1
        var last_resend_pending_messages4_ms: Long = -1
        var last_start_queued_fts_ms: Long = -1

        // ------------------------------
        fun bootstrap_me() {
            Log.i(TAG, "bootstrap_me")

            // ----- UDP ------
            if (PREF__udp_enabled == 1) {
                bootstrap_single_wrapper(
                    "85.143.221.42", 33445,
                    "DA4E4ED4B697F2E9B000EEFE3A34B554ACD3F45F5C96EAEA2516DD7FF9AF7B43"
                )
                bootstrap_single_wrapper(
                    "tox.verdict.gg", 33445,
                    "1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976"
                )
                bootstrap_single_wrapper(
                    "78.46.73.141", 33445,
                    "02807CF4F8BB8FB390CC3794BDF1E8449E9A8392C5D3F2200019DA9F1E812E46"
                )
                bootstrap_single_wrapper(
                    "tox.initramfs.io", 33445,
                    "3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25"
                )
                bootstrap_single_wrapper(
                    "46.229.52.198", 33445,
                    "813C8F4187833EF0655B10F7752141A352248462A567529A38B6BBF73E979307"
                )
                bootstrap_single_wrapper(
                    "144.217.167.73", 33445,
                    "7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C"
                )
                bootstrap_single_wrapper(
                    "tox.abilinski.com", 33445,
                    "10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E"
                )
                bootstrap_single_wrapper(
                    "tox.novg.net", 33445,
                    "D527E5847F8330D628DAB1814F0A422F6DC9D0A300E6C357634EE2DA88C35463"
                )
                bootstrap_single_wrapper(
                    "95.31.18.227", 33445,
                    "257744DBF57BE3E117FE05D145B5F806089428D4DCE4E3D0D50616AA16D9417E"
                )
                bootstrap_single_wrapper(
                    "198.199.98.108", 33445,
                    "BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F"
                )
                bootstrap_single_wrapper(
                    "tox.kurnevsky.net", 33445,
                    "82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23"
                )
                bootstrap_single_wrapper(
                    "81.169.136.229", 33445,
                    "E0DB78116AC6500398DDBA2AEEF3220BB116384CAB714C5D1FCD61EA2B69D75E"
                )
                bootstrap_single_wrapper(
                    "205.185.115.131", 53,
                    "3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68"
                )
                bootstrap_single_wrapper(
                    "tox2.abilinski.com", 33445,
                    "7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D"
                )
                bootstrap_single_wrapper(
                    "46.101.197.175", 33445,
                    "CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707"
                )
                bootstrap_single_wrapper(
                    "tox1.mf-net.eu", 33445,
                    "B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506"
                )
                bootstrap_single_wrapper(
                    "tox2.mf-net.eu", 33445,
                    "70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F"
                )
                bootstrap_single_wrapper(
                    "195.201.7.101", 33445,
                    "B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107"
                )
                bootstrap_single_wrapper(
                    "168.138.203.178", 33445,
                    "6D04D8248E553F6F0BFDDB66FBFB03977E3EE54C432D416BC2444986EF02CC17"
                )
                bootstrap_single_wrapper(
                    "209.59.144.175", 33445,
                    "214B7FEA63227CAEC5BCBA87F7ABEEDB1A2FF6D18377DD86BF551B8E094D5F1E"
                )
                bootstrap_single_wrapper(
                    "188.225.9.167", 33445,
                    "1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67"
                )
                bootstrap_single_wrapper(
                    "122.116.39.151", 33445,
                    "5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E"
                )
                bootstrap_single_wrapper(
                    "195.123.208.139", 33445,
                    "534A589BA7427C631773D13083570F529238211893640C99D1507300F055FE73"
                )
                bootstrap_single_wrapper(
                    "208.38.228.104", 33445,
                    "3634666A51CA5BE1579C031BD31B20059280EB7C05406ED466BD9DFA53373271"
                )
                bootstrap_single_wrapper(
                    "104.225.141.59", 43334,
                    "933BA20B2E258B4C0D475B6DECE90C7E827FE83EFA9655414E7841251B19A72C"
                )
                bootstrap_single_wrapper(
                    "137.74.42.224", 33445,
                    "A95177FA018066CF044E811178D26B844CBF7E1E76F140095B3A1807E081A204"
                )
                bootstrap_single_wrapper(
                    "198.98.49.206", 33445,
                    "28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255"
                )
            } else {
                bootstrap_single_wrapper(
                    "127.0.0.1", 7766,
                    "2AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA1"
                )
            }
            // ----- UDP ------
            //
            // ----- TCP ------
            add_tcp_relay_single_wrapper(
                "85.143.221.42", 3389,
                "DA4E4ED4B697F2E9B000EEFE3A34B554ACD3F45F5C96EAEA2516DD7FF9AF7B43"
            )
            add_tcp_relay_single_wrapper(
                "tox.verdict.gg", 33445,
                "1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976"
            )
            add_tcp_relay_single_wrapper(
                "78.46.73.141", 33445,
                "02807CF4F8BB8FB390CC3794BDF1E8449E9A8392C5D3F2200019DA9F1E812E46"
            )
            add_tcp_relay_single_wrapper(
                "tox.initramfs.io", 3389,
                "3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25"
            )
            add_tcp_relay_single_wrapper(
                "144.217.167.73", 3389,
                "7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C"
            )
            add_tcp_relay_single_wrapper(
                "tox.abilinski.com", 33445,
                "10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E"
            )
            add_tcp_relay_single_wrapper(
                "tox.novg.net", 33445,
                "D527E5847F8330D628DAB1814F0A422F6DC9D0A300E6C357634EE2DA88C35463"
            )
            add_tcp_relay_single_wrapper(
                "95.31.18.227", 33445,
                "257744DBF57BE3E117FE05D145B5F806089428D4DCE4E3D0D50616AA16D9417E"
            )
            add_tcp_relay_single_wrapper(
                "198.199.98.108", 3389,
                "BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F"
            )
            add_tcp_relay_single_wrapper(
                "tox.kurnevsky.net", 33445,
                "82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23"
            )
            add_tcp_relay_single_wrapper(
                "81.169.136.229", 33445,
                "E0DB78116AC6500398DDBA2AEEF3220BB116384CAB714C5D1FCD61EA2B69D75E"
            )
            add_tcp_relay_single_wrapper(
                "205.185.115.131", 3389,
                "3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68"
            )
            add_tcp_relay_single_wrapper(
                "tox2.abilinski.com", 33445,
                "7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D"
            )
            add_tcp_relay_single_wrapper(
                "46.101.197.175", 33445,
                "CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707"
            )
            add_tcp_relay_single_wrapper(
                "tox1.mf-net.eu", 3389,
                "B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506"
            )
            add_tcp_relay_single_wrapper(
                "tox2.mf-net.eu", 3389,
                "70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F"
            )
            add_tcp_relay_single_wrapper(
                "195.201.7.101", 33445,
                "B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107"
            )
            add_tcp_relay_single_wrapper(
                "168.138.203.178", 33445,
                "6D04D8248E553F6F0BFDDB66FBFB03977E3EE54C432D416BC2444986EF02CC17"
            )
            add_tcp_relay_single_wrapper(
                "209.59.144.175", 33445,
                "214B7FEA63227CAEC5BCBA87F7ABEEDB1A2FF6D18377DD86BF551B8E094D5F1E"
            )
            add_tcp_relay_single_wrapper(
                "188.225.9.167", 33445,
                "1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67"
            )
            add_tcp_relay_single_wrapper(
                "122.116.39.151", 33445,
                "5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E"
            )
            add_tcp_relay_single_wrapper(
                "195.123.208.139", 3389,
                "534A589BA7427C631773D13083570F529238211893640C99D1507300F055FE73"
            )
            add_tcp_relay_single_wrapper(
                "208.38.228.104", 33445,
                "3634666A51CA5BE1579C031BD31B20059280EB7C05406ED466BD9DFA53373271"
            )
            add_tcp_relay_single_wrapper(
                "137.74.42.224", 33445,
                "A95177FA018066CF044E811178D26B844CBF7E1E76F140095B3A1807E081A204"
            )
            add_tcp_relay_single_wrapper(
                "198.98.49.206", 33445,
                "28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255"
            )
            add_tcp_relay_single_wrapper(
                "5.19.249.240", 3389,
                "DA98A4C0CD7473A133E115FEA2EBDAEEA2EF4F79FD69325FC070DA4DE4BA3238"
            )
            // ----- TCP ------
        }

        // --------------- JNI ---------------
        // --------------- JNI ---------------
        // --------------- JNI ---------------
        @Suppress("UNUSED_PARAMETER")
        @JvmStatic
        fun logger(level: Int, text: String?) {
            Log.i(TAG, text!!)
        }

        @JvmStatic
        fun safe_string(`in`: ByteArray?): String {
            // Log.i(TAG, "safe_string:in=" + in);
            var out = ""
            try {
                out = String(`in`!!, charset("UTF-8")) // Best way to decode using "UTF-8"
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i(TAG, "safe_string:EE:" + e.message)
                try {
                    out = String(`in`!!)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                    Log.i(TAG, "safe_string:EE2:" + e2.message)
                }
            }

            // Log.i(TAG, "safe_string:out=" + out);
            return out
        } // --------------- JNI ---------------
        // --------------- JNI ---------------
        // --------------- JNI ---------------
    }

    fun clear_friend() {
        try {
            contactstore.clear()
        } catch (_: Exception) {
        }
    }

    fun load_friends() {
        tox_self_get_friend_list()?.forEach {
            Log.i(TAG, "friend:" + it)
            var fname = tox_friend_get_name(it)
            if (fname == null) {
                fname = "Friend"
            }
            try {
                contactstore.add(
                    item = ContactItem(
                        name = fname,
                        isConnected = 0,
                        pubkey = tox_friend_get_public_key(it)!!
                    )
                )
            } catch (_: Exception) {
            }
        }
    }
}

/*
 * Briar Desktop
 * Copyright (C) 2021-2022 The Briar Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
@file:Suppress("LocalVariableName", "ConvertToStringTemplate")

package org.briarproject.briar.desktop

import SETTINGS_HEADER_SIZE
import SnackBarToast
import UIScaleItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import avstatestorecallstate
import com.zoffcc.applications.trifa.HelperFriend.get_g_opts
import com.zoffcc.applications.trifa.HelperFriend.set_g_opts
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.HelperGeneric.long_to_hex
import com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper
import com.zoffcc.applications.trifa.HelperNotification
import com.zoffcc.applications.trifa.HelperOSFile.show_containing_dir_in_explorer
import com.zoffcc.applications.trifa.HelperRelay.add_or_update_own_relay
import com.zoffcc.applications.trifa.HelperRelay.get_own_relay_pubkey
import com.zoffcc.applications.trifa.HelperRelay.remove_own_relay_in_db
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.DB_PREF__notifications_active
import com.zoffcc.applications.trifa.MainActivity.Companion.DB_PREF__open_files_directly
import com.zoffcc.applications.trifa.MainActivity.Companion.DB_PREF__send_push_notifications
import com.zoffcc.applications.trifa.MainActivity.Companion.DB_PREF__use_other_toxproxies
import com.zoffcc.applications.trifa.MainActivity.Companion.password_hash
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_get_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_get_nospam
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_set_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_set_nospam
import com.zoffcc.applications.trifa.MainActivity.Companion.update_toxid_while_running
import com.zoffcc.applications.trifa.TAG
import com.zoffcc.applications.trifa.TrifaToxService.Companion.orma
import global_prefs
import globalstore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import savepathstore
import update_bootstrap_nodes_from_internet
import java.io.File
import kotlin.random.Random
import kotlin.random.nextUInt
import kotlin.random.nextULong

@Composable
fun SettingDetails()
{
    SettingDetail(i18n("ui.settings_headline")) {
        val global_store by globalstore.stateFlow.collectAsState()
        if (global_store.toxRunning)
        {
            set_own_name()
            set_own_nospam()
            Spacer(modifier = Modifier.height(60.dp))
        }
        if ((global_store.toxRunning) && (global_store.ormaRunning))
        {
            own_relay_settings()
            Spacer(modifier = Modifier.height(60.dp))
        }
        general_settings()
        Spacer(modifier = Modifier.height(60.dp))
        tox_settings()
        Spacer(modifier = Modifier.height(60.dp))
        if (global_store.ormaRunning)
        {
            database_settings()
            Spacer(modifier = Modifier.height(60.dp))
        }
        button_settings()
        Spacer(modifier = Modifier.height(60.dp))
        if ((global_store.toxRunning) && (global_store.ormaRunning))
        {
            // TODO: !! this is not yet fully working !! do NOT enable !!
            // change_tox_and_db_pass()
            // Spacer(modifier = Modifier.height(60.dp))
        }
        //
        // --------------------------------------
        // HINT: change locale at runtime:
        //
        // Locale.setDefault(Locale.GERMAN)
        // ResourceBundle.clearCache()
        // --------------------------------------
    }
}

@Composable
private fun button_settings()
{
    val savepathdata by savepathstore.stateFlow.collectAsState()
    if (!savepathdata.savePathEnabled)
    {
        Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
            Button(modifier = Modifier.width(400.dp),
                enabled = true,
                onClick = {
                    show_containing_dir_in_explorer(MainActivity.PREF__tox_savefile_dir + File.separator + ".")
                })
            {
                Text(i18n("ui.setting.open_data"))
            }
        }
    }

    Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
        Button(modifier = Modifier.width(400.dp),
            enabled = true,
            onClick = {
                GlobalScope.launch {
                    HelperNotification.displayNotification_with_force_option("test notification öäüß{}?°^!_;:,.-_|<>'1234567890", true)
                    SnackBarToast("Notification triggered")
                }
            })
        {
            Text(i18n("ui.setting.test_notification"))
        }
    }

    Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
        var loading_nodes by remember { mutableStateOf(false) }
        Button(modifier = Modifier.width(400.dp),
            enabled = if (loading_nodes) false else true,
            onClick = {
                GlobalScope.launch {
                    loading_nodes = true
                    try
                    {
                        update_bootstrap_nodes_from_internet()
                    }
                    catch(_: Exception)
                    {
                    }
                    loading_nodes = false
                    SnackBarToast(i18n("ui.setting.nodes_updated"))
                }
            })
        {
            Text(i18n("ui.setting.update_bootstrap"))
        }
    }
}

@Composable
private fun database_settings()
{
    if (orma != null)
    {
        // ---- open files directly ----
        var open_files_directly by remember { mutableStateOf(false) }
        try
        {
            if (get_g_opts("DB_PREF__open_files_directly") != null)
            {
                if (get_g_opts("DB_PREF__open_files_directly").equals("true"))
                {
                    open_files_directly = true
                }
            }
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }
        DetailItem(label = i18n("ui.setting.open_files"),
            description = (if (open_files_directly) i18n("enabled") else i18n("disabled"))) {
            Switch(
                checked = open_files_directly,
                onCheckedChange = {
                    set_g_opts("DB_PREF__open_files_directly", it.toString().lowercase())
                    println("DB_PREF__open_files_directly=" + it.toString().lowercase())
                    DB_PREF__open_files_directly = it
                    open_files_directly = it
                },
            )
        }
        // ---- open files directly ----
        // ---- notifications active ----
        var notifications_active by remember { mutableStateOf(true) }
        try
        {
            if (get_g_opts("DB_PREF__notifications_active") != null)
            {
                if (get_g_opts("DB_PREF__notifications_active").equals("false"))
                {
                    notifications_active = false
                }
            }
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }

        DetailItem(label = i18n("ui.setting.enable_notifications"),
            description = (if (notifications_active) i18n("enabled") else i18n("disabled"))) {
            Switch(
                checked = notifications_active,
                onCheckedChange = {
                    set_g_opts("DB_PREF__notifications_active", it.toString().lowercase())
                    println("DB_PREF__notifications_active=" + it.toString().lowercase())
                    DB_PREF__notifications_active = it
                    notifications_active = it
                },
            )
        }
        // ---- notifications active ----
        // ---- send push notifications active ----
        var push_notifications_active by remember { mutableStateOf(false) }
        try
        {
            if (get_g_opts("DB_PREF__send_push_notifications") != null)
            {
                if (get_g_opts("DB_PREF__send_push_notifications").equals("true"))
                {
                    push_notifications_active = true
                }
            }
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }

        DetailItem(label = i18n("ui.setting.push_notifications"),
            description = (if (push_notifications_active) i18n("enabled") else i18n("disabled"))) {
            Switch(
                checked = push_notifications_active,
                onCheckedChange = {
                    set_g_opts("DB_PREF__send_push_notifications", it.toString().lowercase())
                    println("DB_PREF__send_push_notifications=" + it.toString().lowercase())
                    DB_PREF__send_push_notifications = it
                    push_notifications_active = it
                },
            )
        }
        // ---- send push notifications active ----
        // ---- send push notifications active ----
        var use_other_toxproxies_active by remember { mutableStateOf(false) }
        try
        {
            if (get_g_opts("DB_PREF__use_other_toxproxies") != null)
            {
                if (get_g_opts("DB_PREF__use_other_toxproxies").equals("true"))
                {
                    use_other_toxproxies_active = true
                }
            }
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }

        DetailItem(label = i18n("ui.setting.toxproxies"),
            description = (if (use_other_toxproxies_active) i18n("enabled") else i18n("disabled"))) {
            Switch(
                checked = use_other_toxproxies_active,
                onCheckedChange = {
                    set_g_opts("DB_PREF__use_other_toxproxies", it.toString().lowercase())
                    println("DB_PREF__use_other_toxproxies=" + it.toString().lowercase())
                    DB_PREF__use_other_toxproxies = it
                    use_other_toxproxies_active = it
                },
            )
        }
        // ---- send push notifications active ----

        // ---- windows dshow audio in source name ----
        /*
            var windows_dshow_audio_in_source_name by remember { mutableStateOf("Microphone") }
            try
            {
                if (get_g_opts("DB_PREF__windows_audio_in_source") != null)
                {
                    windows_dshow_audio_in_source_name = get_g_opts("DB_PREF__windows_audio_in_source")
                }
            } catch (e: java.lang.Exception)
            {
                e.printStackTrace()
            }
            Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
                TextField(enabled = true, singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.padding(0.dp).weight(1.0f),
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrect = false,
                    ), value = windows_dshow_audio_in_source_name,
                    onValueChange = {
                        set_g_opts("DB_PREF__windows_audio_in_source", it)
                        println("DB_PREF__windows_audio_in_source=" + it)
                        DB_PREF__windows_audio_in_source = it
                        windows_dshow_audio_in_source_name = it
                    })
            }
            */
        // ---- windows dshow audio in source name ----

    }
}

@Composable
private fun general_settings()
{
    // ---- use custom font that has color emoji AND normal text ----
    var use_custom_font_with_color_emoji by remember { mutableStateOf(true) }
    try
    {
        if (!global_prefs.getBoolean("main.use_custom_font_with_color_emoji", true))
        {
            use_custom_font_with_color_emoji = false
        }
    } catch (_: Exception)
    {
    }
    DetailItem(label = i18n("use custom font with color emojis for text. this needs an app restart !!"),
        description = (if (use_custom_font_with_color_emoji) i18n("enabled") else i18n("disabled"))) {
        Switch(
            checked = use_custom_font_with_color_emoji,
            onCheckedChange = {
                global_prefs.putBoolean("main.use_custom_font_with_color_emoji", it)
                use_custom_font_with_color_emoji = it
            },
        )
    }
    // ---- use custom font that has color emoji AND normal text ----


    // ---- display status info for AV calls ----
    var display_status_info_for_av_calls by remember { mutableStateOf(false) }
    try
    {
        if (global_prefs.getBoolean("main.display_status_info_for_av_calls", false))
        {
            display_status_info_for_av_calls = true
        }
    } catch (_: Exception)
    {
    }
    DetailItem(label = i18n("display status info for AV calls. WARNING: this will make the UI slow down"),
        description = (if (display_status_info_for_av_calls) i18n("enabled") else i18n("disabled"))) {
        Switch(
            checked = display_status_info_for_av_calls,
            onCheckedChange = {
                global_prefs.putBoolean("main.display_status_info_for_av_calls", it)
                display_status_info_for_av_calls = it
                avstatestorecallstate.display_av_stats(display_status_info_for_av_calls)
            },
        )
    }
    // ---- display status info for AV calls ----


    // ---- auto connect tox with default profile ----
    var auto_connect_tox_with_default_profile by remember { mutableStateOf(false) }
    try
    {
        if (global_prefs.getBoolean("main.auto_connect_tox_with_default_profile", false))
        {
            auto_connect_tox_with_default_profile = true
        }
    } catch (_: Exception)
    {
    }
    DetailItem(label = i18n("ui.setting.auto_connect_tox_with_default_profile"),
        description = (if (auto_connect_tox_with_default_profile) i18n("enabled") else i18n("disabled"))) {
        Switch(
            checked = auto_connect_tox_with_default_profile,
            onCheckedChange = {
                global_prefs.putBoolean("main.auto_connect_tox_with_default_profile", it)
                auto_connect_tox_with_default_profile = it
            },
        )
    }
    // ---- auto connect tox with default profile ----


    // ---- set global density to scale the whole UI ----
    var ui_density by remember { mutableStateOf(globalstore.getUiDensity()) }
    DetailItem(label = i18n("ui.ui_density"),
        description = i18n("ui.ui_density")) {
        UIScaleItem(
            label = "" + "%06.2f".format(ui_density),
            description = i18n("ui.drag_slider_to_change")) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.FormatSize, null, Modifier.scale(0.7f))
                Slider(value = ui_density, onValueChange = {
                    ui_density = it
                }, onValueChangeFinished = {
                    globalstore.updateUiDensity(ui_density)
                    Log.i(TAG, "updateUiDensity:ui_density:1: $ui_density")
                }, valueRange = 0.25f..10f, steps = 64)
                Icon(Icons.Default.FormatSize, null)
            }
        }
    }

    //Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
    //}
    // ---- set global density to scale the whole UI ----



}

@Composable
private fun tox_settings()
{
    // ---- UDP ----
    var tox_udp_mode by remember { mutableStateOf(true) }
    try
    {
        if (!global_prefs.getBoolean("tox.settings.udp", true))
        {
            tox_udp_mode = false
        }
    } catch (_: Exception)
    {
    }

    DetailItem(label = i18n("ui.setting.udp_mode"),
        description = (if (tox_udp_mode) i18n("UDP mode enabled") else i18n("UDP mode disabled"))) {
        Switch(
            checked = tox_udp_mode,
            onCheckedChange = {
                global_prefs.putBoolean("tox.settings.udp", it)
                tox_udp_mode = it
            },
        )
    }
    // ---- UDP ----
    // ---- LAN discovery ----
    var tox_landiscovery_mode by remember { mutableStateOf(true) }
    try
    {
        if (!global_prefs.getBoolean("tox.settings.local_lan_discovery", true))
        {
            tox_landiscovery_mode = false
        }
    } catch (_: Exception)
    {
    }
    DetailItem(label = i18n("ui.setting.lan_discovery"),
        description =
        (if (tox_landiscovery_mode) i18n("Local LAN discovery enabled") else i18n("Local LAN discovery disabled"))) {
        Switch(
            checked = tox_landiscovery_mode,
            onCheckedChange = {
                global_prefs.putBoolean("tox.settings.local_lan_discovery", it)
                tox_landiscovery_mode = it
            },
        )
    }
    // ---- LAN discovery ----
    // ---- IPv6 ----
    var tox_ipv6_mode by remember { mutableStateOf(true) }
    try
    {
        if (!global_prefs.getBoolean("tox.settings.ipv6", true))
        {
            tox_ipv6_mode = false
        }
    } catch (_: Exception)
    {
    }
    DetailItem(label = i18n("IPv6"),
        description = (if (tox_ipv6_mode) i18n("IPv6 enabled") else i18n("IPv6 disabled"))) {
        Switch(
            checked = tox_ipv6_mode,
            onCheckedChange = {
                global_prefs.putBoolean("tox.settings.ipv6", it)
                tox_ipv6_mode = it
            },
        )
    }
    // ---- IPv6 ----
    // ---- tor proxy ----
    var tox_tor_proxy by remember { mutableStateOf(false) }
    try
    {
        if (global_prefs.getBoolean("tox.settings.tor_proxy", false))
        {
            tox_tor_proxy = true
        }
    } catch (_: Exception)
    {
    }
    DetailItem(label = i18n("ui.setting.tor_proxy"),
        description = (if (tox_tor_proxy) i18n("enabled") else i18n("disabled"))) {
        Switch(
            checked = tox_tor_proxy,
            onCheckedChange = {
                global_prefs.putBoolean("tox.settings.tor_proxy", it)
                tox_tor_proxy = it
            },
        )
    }
    // ---- tor proxy ----
    // ---- enable noise ----
    /*
    var tox_noise by remember { mutableStateOf(false) }
    try
    {
        if (global_prefs.getBoolean("tox.settings.tox_noise", false))
        {
            tox_noise = true
        }
    } catch (_: Exception)
    {
    }
    DetailItem(label = i18n("enable the experimental toxcore with NOISE handshake.\n!! this is experimental and also needs an app restart !!"),
        description = (if (tox_noise) i18n("enabled") else i18n("disabled"))) {
        Switch(
            checked = tox_noise,
            onCheckedChange = {
                global_prefs.putBoolean("tox.settings.tox_noise", it)
                tox_noise = it
            },
        )
    }
     */
    // ---- enable noise ----
}

@Composable
private fun set_own_name()
{
    // ---- change own name for one-on-one chats ----
    var tox_self_name = ""
    try
    {
        tox_self_name = tox_self_get_name()!!
    } catch (e: java.lang.Exception)
    {
        e.printStackTrace()
    }
    var tox_own_name by remember { mutableStateOf(tox_self_name) }

    Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
        TextField(enabled = true, singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.padding(0.dp).weight(1.0f),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR)),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
            ), value = tox_own_name,
            onValueChange = {
                tox_own_name = it
            })

        Button(modifier = Modifier.width(300.dp).padding(start = 20.dp, end = 20.dp),
            enabled = true,
            onClick = {
                if (tox_self_set_name(tox_own_name) == 1)
                {
                    HelperGeneric.update_savedata_file_wrapper()
                    SnackBarToast(i18n("ui.setting.changed_name"))
                } else
                {
                    SnackBarToast(i18n("ui.setting.error_while_trying"))
                }
            })
        {
            Text(i18n("ui.setting.update_name"))
        }
    }
    // ---- change own name for one-on-one chats ----
}

@Composable
private fun set_own_nospam()
{
    // ---- change own nospam value ----
    var tox_self_nospam = ""
    try
    {
        val tmp_nospam_long = tox_self_get_nospam()
        if (tmp_nospam_long != -1L)
        {
            tox_self_nospam = long_to_hex(tmp_nospam_long)
        }
    } catch (e: java.lang.Exception)
    {
        e.printStackTrace()
    }
    val tox_nospam_hex_len = 8
    val hex_Pattern = "[A-F0-9]*"
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var tox_own_nospam by remember { mutableStateOf(tox_self_nospam) }

    Column(modifier = Modifier.height(90.dp)) {
        Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
            TextField(enabled = true, singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
                modifier = Modifier.padding(0.dp).weight(1.0f),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR)),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                ), value = tox_own_nospam,
                isError = errorMessage != null,
                placeholder = {
                    Text("no spam must be " + tox_nospam_hex_len + " chars")
                },
                onValueChange = {
                    tox_own_nospam = it.uppercase()
                    errorMessage = if ((it.length == tox_nospam_hex_len) && (it.uppercase().matches(hex_Pattern.toRegex())))
                    {
                        null
                    } else
                    {
                        "Please enter a valid 8 char hex string."
                    }
                })
            Button(modifier = Modifier.width(300.dp).padding(start = 20.dp, end = 20.dp),
                enabled = true,
                onClick = {
                    try
                    {
                        if ((tox_own_nospam.length == tox_nospam_hex_len) && (tox_own_nospam.uppercase().matches(hex_Pattern.toRegex())))
                        {
                            val new_value = tox_own_nospam.toLong(radix = 16)
                            tox_self_set_nospam(new_value)
                            HelperGeneric.update_savedata_file_wrapper()
                            update_toxid_while_running()
                            SnackBarToast(i18n("ui.setting.new_nospam_set"))
                        }
                    }
                    catch(_: Exception)
                    {
                    }
                })
            {
                Text(i18n("ui.setting.update_tox_nospam"))
            }
            Button(modifier = Modifier.width(300.dp).padding(start = 0.dp, end = 20.dp),
                enabled = true,
                onClick = {
                    try
                    {
                        val rnd_value = Random.nextUInt()
                        tox_own_nospam = long_to_hex(rnd_value.toLong()).uppercase()
                        tox_self_set_nospam(rnd_value.toLong())
                        HelperGeneric.update_savedata_file_wrapper()
                        update_toxid_while_running()
                        SnackBarToast(i18n("ui.setting.new_nospam_set"))
                    }
                    catch(_: Exception)
                    {
                    }
                })
            {
                Text(i18n("ui.setting.update_random_nospam"))
            }
        }
        if (errorMessage != null)
        {
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(top = 3.dp, start = 20.dp).align(Alignment.Start)
            )
        }
    }
    // ---- change own nospam value ----
}

@Composable
private fun change_tox_and_db_pass()
{
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
        Button(modifier = Modifier.width(400.dp),
            enabled = true,
            onClick = { showChangePasswordDialog = true }
        )
        {
            Text(i18n("ui.setting.change_tox_and_db_pass"))
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(onDismiss = { showChangePasswordDialog = false })
    }
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                TextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                // Handle password change logic here
                // For example, validate and save the new password
                if (currentPassword == password_hash)
                {
                    if (newPassword != password_hash)
                    {
                        // changing tox save password
                        Log.i(TAG, "changing tox save password ...")
                        password_hash = newPassword
                        update_savedata_file_wrapper()
                        Log.i(TAG, "tox save written with new password. password_hash=" + password_hash)
                    }
                }
                onDismiss() // Close the dialog after action
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
private fun own_relay_settings()
{
    // ---- change own name for one-on-one chats ----
    var own_relay_toxid by remember { mutableStateOf(get_own_relay_pubkey()) }

    Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
        TextField(enabled = true, singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.padding(0.dp).weight(1.0f),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR)),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
            ), value = if (own_relay_toxid.isNullOrEmpty()) "" else own_relay_toxid,
            onValueChange = {
                own_relay_toxid = if (it.isNullOrEmpty()) "" else it
            })

        Button(modifier = Modifier.width(300.dp).padding(start = 20.dp, end = 20.dp),
            enabled = true,
            onClick = {
                if (own_relay_toxid.isNullOrEmpty())
                {
                    remove_own_relay_in_db()
                }
                else
                {
                    add_or_update_own_relay(own_relay_toxid.uppercase())
                }
            })
        {
            Text(i18n("ui.setting.add_toxproxy"))
        }
    }
    // ---- change own name for one-on-one chats ----
}

@Composable
fun SettingDetail(header: String, content: @Composable (ColumnScope.() -> Unit)) =
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(SETTINGS_HEADER_SIZE).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween)
        {
            Text(header, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.onSurface)
        }
        VerticallyScrollableArea { scrollState ->
            LazyColumn(state = scrollState) {
                item {
                    content()
                }
            }
        }

}

@Composable
fun DetailItem(
    label: String,
    description: String,
    setting: @Composable (RowScope.() -> Unit),
) = Box(modifier = Modifier.padding(start = 15.dp, end = 22.dp, top = 5.dp, bottom = 2.dp)) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Row(Modifier.fillMaxWidth()
            .height(SETTINGS_HEADER_SIZE)
            .padding(horizontal = 16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = description
            }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            setting()
        }
    }
}

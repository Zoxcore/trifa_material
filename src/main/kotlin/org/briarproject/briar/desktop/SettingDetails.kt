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

package org.briarproject.briar.desktop

import SETTINGS_HEADER_SIZE
import SnackBarToast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.HelperFriend.get_g_opts
import com.zoffcc.applications.trifa.HelperFriend.set_g_opts
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.HelperNotification
import com.zoffcc.applications.trifa.HelperOSFile.show_containing_dir_in_explorer
import com.zoffcc.applications.trifa.HelperRelay.add_or_update_own_relay
import com.zoffcc.applications.trifa.HelperRelay.get_own_relay_pubkey
import com.zoffcc.applications.trifa.HelperRelay.remove_own_relay_in_db
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.DB_PREF__notifications_active
import com.zoffcc.applications.trifa.MainActivity.Companion.DB_PREF__open_files_directly
import com.zoffcc.applications.trifa.MainActivity.Companion.DB_PREF__send_push_notifications
import com.zoffcc.applications.trifa.MainActivity.Companion.DB_PREF__use_other_toxproxies
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_get_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_set_name
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

@Composable
fun SettingDetails()
{
    SettingDetail(i18n("ui.settings_headline")) {
        val global_store by globalstore.stateFlow.collectAsState()
        if (global_store.toxRunning)
        {
            set_own_name()
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
                Text("Open data directory")
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
            Text("test Notification")
        }
    }

    Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
        var loading_nodes by remember { mutableStateOf(false) }
        Button(modifier = Modifier.width(400.dp),
            enabled = if (loading_nodes) false else true,
            onClick = {
                GlobalScope.launch {
                    loading_nodes = true
                    update_bootstrap_nodes_from_internet()
                    loading_nodes = false
                    SnackBarToast("Bootstrap nodes updated from internet")
                }
            })
        {
            Text("update bootstrap nodes from internet")
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
        DetailItem(label = i18n("Open files directly instead of showing the containing directory.\nthis can be potentially dangerous!"),
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

        DetailItem(label = i18n("enable notifications"),
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

        DetailItem(label = i18n("send push notifications"),
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

        DetailItem(label = i18n("Use ToxProxies of Friends"),
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
    var use_custom_font_with_color_emoji by remember { mutableStateOf(false) }
    try
    {
        if (global_prefs.getBoolean("main.use_custom_font_with_color_emoji", false))
        {
            use_custom_font_with_color_emoji = true
        }
    } catch (_: Exception)
    {
    }
    DetailItem(label = i18n("use custom font with color emojis for text.\n!! this is experimental and also needs an app restart !!"),
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

    DetailItem(label = i18n("UDP mode"),
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
    DetailItem(label = i18n("LAN discovery"),
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
    DetailItem(label = i18n("use Tor proxy [on localhost port: 9050]"),
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
                    SnackBarToast("You have changed your own name")
                } else
                {
                    SnackBarToast("Error while trying to set your own name")
                }
            })
        {
            Text("Update your Name")
        }
    }
    // ---- change own name for one-on-one chats ----
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
            Text("Add or Update your own Relay (ToxProxy)")
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

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import global_prefs
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun SettingDetails()
{
    SettingDetail(i18n("Settings")) {/*
        //    val dummylist = listOf("Sue Helen", "JR", "Pamela")
        DetailItem(label = i18n("settings.display.theme.title"), description = "${i18n("access.settings.current_value")}: " + i18n("settings.display.theme") + // NON-NLS
                ", " + i18n("access.settings.click_to_change_value")) {
            OutlinedExposedDropDownMenu(values = dummylist, selectedIndex = 0, onChange = { }, modifier = Modifier.widthIn(min = 200.dp))
        }

        DetailItem(label = i18n("settings.display.language.title"), description = "${i18n("access.settings.current_value")}: " + "value.displayName" + ", " + i18n("access.settings.click_to_change_value")) {
            OutlinedExposedDropDownMenu(values = dummylist, selectedIndex = 0, onChange = { }, modifier = Modifier.widthIn(min = 200.dp))
        }

        DetailItem(label = i18n("settings.security.title"), description = i18n("access.settings.click_to_change_password")) {
            OutlinedButton(onClick = { }) {
                Text(i18n("settings.security.password.change"))
            }
        }
        val notificationError = false
        val visualNotificationsChecked = false

        DetailItem(label = i18n("settings.notifications.visual.title"), description = (if (visualNotificationsChecked) i18n("access.settings.currently_enabled")
        else i18n("access.settings.currently_disabled")) + ". " + i18n("access.settings.click_to_toggle_notifications")) {
            Switch(checked = visualNotificationsChecked, onCheckedChange = { }, enabled = !notificationError)
        }
        val state_error = false
        if (state_error)
        {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).background(MaterialTheme.colors.onBackground).padding(all = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.Warning, i18n("warning"), Modifier.size(40.dp).padding(vertical = 4.dp), MaterialTheme.colors.onError)
                Text(text = "visualNotificationProviderState.message", color = MaterialTheme.colors.error, style = MaterialTheme.typography.body2, modifier = Modifier.fillMaxWidth())
            }
        }
        */

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

    }
}

@Composable
fun SettingDetail(header: String, content: @Composable (ColumnScope.() -> Unit)) = Column(Modifier.fillMaxSize()) {
    Row(Modifier.fillMaxWidth().height(SETTINGS_HEADER_SIZE).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(header, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.onSurface)
    }
    content()
}

@Composable
fun DetailItem(
    label: String,
    description: String,
    setting: @Composable (RowScope.() -> Unit),
) = Row(Modifier.fillMaxWidth().height(SETTINGS_HEADER_SIZE).padding(horizontal = 16.dp).semantics(mergeDescendants = true) { // it would be nicer to derive the contentDescriptions from the descendants automatically
    // which is currently not supported in Compose for Desktop
    // see https://github.com/JetBrains/compose-jb/issues/2111
    contentDescription = description
}, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label)
    setting()
}
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

package org.briarproject.briar.desktop.contact

import CONTACT_COLUMN_CONTACTNAME_LEN_THRESHOLD
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.HelperRelay.get_relay_for_friend
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.TAG
import globalfrndstoreunreadmsgs
import globalstore
import org.briarproject.briar.desktop.ui.NumberBadge
import org.briarproject.briar.desktop.ui.Tooltip
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun ContactItemView(
    contactItem: ContactItem,
    modifier: Modifier = Modifier,
) = Row(
    horizontalArrangement = spacedBy(8.dp),
    verticalAlignment = CenterVertically,
    modifier = modifier
        // allows content to be bottom-aligned
        .height(IntrinsicSize.Min)
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(0.dp),
        modifier = Modifier.weight(1f, fill = true),
    ) {

        Box(Modifier.align(Top).padding(vertical = 0.dp)) {
            ProfileCircle(45.dp, contactItem)
            val current_friendtorerunreadmessagesstore by globalfrndstoreunreadmsgs.stateFlow.collectAsState()
            var num_unread = current_friendtorerunreadmessagesstore.unread_per_friend_message_count.get(contactItem.pubkey)
            if (contactItem.is_relay) num_unread = 0
            NumberBadge(
                num = if (num_unread == null) 0 else num_unread,
                modifier = Modifier.align(TopEnd).offset(6.dp, (-6).dp)
            )
        }
        ContactItemViewInfo(
            contactItem = contactItem,
        )
    }
    ConnectionIndicator(
        modifier = Modifier.padding(end = 5.dp).requiredSize(16.dp),
        isConnected = contactItem.isConnected
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContactItemViewInfo(contactItem: ContactItem) = Column(
    horizontalAlignment = Start,
    modifier = Modifier.padding(start = 6.dp)
) {
    var show_name = if (contactItem.name.isEmpty()) contactItem.pubkey.toUpperCase().take(6) else contactItem.name
    val tooltip_name = if (contactItem.name.isEmpty()) "" else contactItem.name
    var name_style = if (contactItem.name.length > CONTACT_COLUMN_CONTACTNAME_LEN_THRESHOLD)
        MaterialTheme.typography.body1.copy(fontSize = 13.sp) else MaterialTheme.typography.body1
    val friend_relay = get_relay_for_friend(contactItem.pubkey.toUpperCase())
    val relay_str = if (friend_relay.isNullOrEmpty()) "" else ("\n" + "Relay (ToxProxy): " + friend_relay)
    val ip_addr_str =  contactItem.ip_addr
    // Log.i(TAG, "ContactItemViewInfo: ip_addr_str=" + ip_addr_str + " name=" + show_name)
    if (ip_addr_str.length > 0) {
        show_name = show_name + "\n" + ip_addr_str
        name_style = name_style.copy(fontSize = (name_style.fontSize.value - 4).sp)
        // Log.i(TAG, "ContactItemViewInfo: show_name2=" + show_name)
    }
    Tooltip(text = "Name: " + tooltip_name + "\n" +
            "Pubkey: " + contactItem.pubkey + relay_str + "\n" +
            "IP: " + ip_addr_str) {
        Text(
            text = show_name,
            style = name_style,
            maxLines = 2,
            overflow = Ellipsis,
        )
    }
}

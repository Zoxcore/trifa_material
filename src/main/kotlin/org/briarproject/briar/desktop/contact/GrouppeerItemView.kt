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
@file:Suppress("FunctionName", "SpellCheckingInspection", "CascadeIf", "LocalVariableName", "ReplaceIsEmptyWithIfEmpty", "ReplaceSizeCheckWithIsNotEmpty")

package org.briarproject.briar.desktop.contact

import DefaultFont
import GROUP_PEER_COLUMN_PEERNAME_LEN_THRESHOLD
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.briarproject.briar.desktop.ui.Tooltip

@Composable
fun GrouppeerItemView(
    grouppeerItem: GroupPeerItem,
    modifier: Modifier = Modifier,
    peercollapsed: Boolean,
) = Row(
    horizontalArrangement = spacedBy(if(peercollapsed) 0.dp else 8.dp),
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
        Box(Modifier.align(Top).padding(vertical = 8.dp)) {
            GroupPeerItemRoleCircle(peercollapsed, grouppeerItem, Modifier.size(if(peercollapsed) 8.dp else 15.dp))
            //NumberBadge(
            //    num = contactItem.unread,
            //    modifier = Modifier.align(TopEnd).offset(6.dp, (-6).dp)
            //)
        }
        if (!peercollapsed) { Spacer(Modifier.width(2.dp)) }
        GrouppeerItemViewInfo(
            grouppeerItem = grouppeerItem,
            peercollapsed = peercollapsed
        )
    }
    if (!peercollapsed)
    {
        ConnectionIndicator(
            modifier = Modifier.padding(end = 5.dp).requiredSize(16.dp),
            isConnected = grouppeerItem.connectionStatus
        )
    }
}

@Composable
fun GroupPeerItemRoleCircle(
    peercollapsed: Boolean,
    grouppeerItem: GroupPeerItem,
    modifier: Modifier = Modifier.size(if (peercollapsed) 3.dp else 10.dp),
) = Box(
    modifier = modifier
        .border(if (peercollapsed) 0.dp else 1.dp, Color.Black, CircleShape)
        .background(GroupPeerRoleAsBgColor(grouppeerItem.peerRole), CircleShape)
)
{
    Text(text = "" + GroupPeerRoleAsStringShort(grouppeerItem.peerRole),
        modifier = Modifier.align(Alignment.Center),
        style = TextStyle(fontSize = if (peercollapsed) 5.sp else 10.sp)
    )
}

fun GroupPeerRoleAsStringLong(peerRole: Int) : String
{
    if (peerRole == 0) // Founder
    {
        return "Founder"
    }
    else if (peerRole == 1) // Moderator
    {
        return "Moderator"
    }
    else if (peerRole == 2) // User
    {
        return "User"
    }
    else // Observer (muted)
    {
        return "Observer (muted)"
    }
}

fun GroupPeerRoleAsStringShort(peerRole: Int) : String
{
    if (peerRole == 0) // Founder
    {
        return "*"
    }
    else if (peerRole == 1) // Moderator
    {
        return "@"
    }
    else if (peerRole == 2) // User
    {
        return ""
    }
    else // Observer (muted)
    {
        return "~"
    }
}

fun GroupPeerRoleAsBgColor(peerRole: Int) : Color
{
    if (peerRole == 0) // Founder
    {
        return Color.Magenta
    }
    else if (peerRole == 1) // Moderator
    {
        return Color.Green
    }
    else if (peerRole == 2) // User
    {
        return Color.Transparent
    }
    else // Observer (muted)
    {
        return Color.Blue
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GrouppeerItemViewInfo(grouppeerItem: GroupPeerItem, peercollapsed: Boolean) = Column(
    horizontalAlignment = Start,
    modifier = Modifier.padding(start = 0.dp)
) {
    var show_peer_name = if (grouppeerItem.name.isEmpty()) grouppeerItem.pubkey.uppercase().take(6) else grouppeerItem.name
    val tooltip_name = if (grouppeerItem.name.isEmpty()) "" else grouppeerItem.name
    var name_style = if ((grouppeerItem.name.length > GROUP_PEER_COLUMN_PEERNAME_LEN_THRESHOLD) && (!peercollapsed))
        MaterialTheme.typography.body1.copy(
            fontSize = 12.sp,
            lineHeight = TextUnit.Unspecified
        ) else MaterialTheme.typography.body1.copy(lineHeight = TextUnit.Unspecified)
    val ip_addr_str =  grouppeerItem.ip_addr
    // Log.i(TAG, "GrouppeerItemViewInfo: ip_addr_str=" + ip_addr_str + " name=" + show_peer_name)
    if (peercollapsed) {
        show_peer_name = show_peer_name.take(2)
    } else
    {
        if (ip_addr_str.length > 0)
        {
            show_peer_name = show_peer_name + "\n" + ip_addr_str
            name_style = name_style.copy(fontSize = (name_style.fontSize.value - 4).sp)
        }
    }
    Tooltip(text = "Peer Name: " + tooltip_name + "\n"
            + "Peer Role: " + GroupPeerRoleAsStringLong(grouppeerItem.peerRole) + "\n"
            + "Pubkey: " + grouppeerItem.pubkey  + "\n" +
            "IP: " + ip_addr_str) {
        Text(
            text = show_peer_name,
            fontFamily = DefaultFont,
            style = name_style,
            maxLines = if (peercollapsed) 1 else 2,
            overflow = Ellipsis,
        )
    }
}

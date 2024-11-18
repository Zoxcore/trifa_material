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

import GROUPS_COLUMN_GROUPNAME_LEN_THRESHOLD
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_offline_peer_count
import com.zoffcc.applications.trifa.ToxVars
import com.zoffcc.applications.trifa.ToxVars.TOX_GROUP_PRIVACY_STATE
import globalgrpstoreunreadmsgs
import org.briarproject.briar.desktop.ui.NumberBadge
import org.briarproject.briar.desktop.ui.Tooltip
import randomDebugBorder

@Composable
@Preview
fun test__peercount_circle() = Row() {
    val peercollapsed = false
    PeerCountCircle(peercollapsed = peercollapsed, peerCount = 236)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupItemView(
    groupItem: GroupItem,
    modifier: Modifier = Modifier,
    peercollapsed: Boolean,
) = Row(
    horizontalArrangement = spacedBy(if(peercollapsed) 0.dp else 8.dp),
    verticalAlignment = CenterVertically,
    modifier = modifier.height(IntrinsicSize.Min)
) {
    Row(
        verticalAlignment = Bottom,
        horizontalArrangement = spacedBy(0.dp),
        modifier = Modifier.weight(1f, fill = true),
    ) {
        var icon = Icons.Filled.Public
        var description = "Public Group"
        if (groupItem.privacyState == TOX_GROUP_PRIVACY_STATE.TOX_GROUP_PRIVACY_STATE_PRIVATE.value)
        {
            icon = Icons.Filled.LockPerson
            description = "Private Group"
        }
        Tooltip(text = description) {
            Icon(imageVector = icon, modifier = Modifier.height(if(peercollapsed) 8.dp else 20.dp),
                contentDescription = description)
        }
        if(!peercollapsed) { Spacer(modifier = Modifier.randomDebugBorder().width(2.dp)) }
        Box() {
            Column(Modifier.align(BottomStart).randomDebugBorder()) {
                Spacer(modifier = Modifier.randomDebugBorder().height(16.dp))
                GroupItemViewInfo(
                    groupItem = groupItem,
                    peercollapsed = peercollapsed
                )
            }
            val current_groupstorerunreadmessagesstore by globalgrpstoreunreadmsgs.stateFlow.collectAsState()
            val num_unread = current_groupstorerunreadmessagesstore.unread_per_group_message_count.get(groupItem.groupId)
            NumberBadge(
                num = if (num_unread == null) 0 else num_unread,
                modifier = Modifier.align(TopStart).offset(6.dp, (-3).dp)
            )
        }
    }
    if(!peercollapsed)
    {
        ConnectionIndicator(
            modifier = Modifier.padding(end = 1.dp).requiredSize(16.dp),
            isConnected = if (groupItem.isConnected == 0) 0 else 2
        )
    }
    PeerCountCircle(
        peercollapsed = peercollapsed,
        peerCount = groupItem.numPeers.toLong(),
        modifier = Modifier.padding(end = 1.dp).requiredSize(if(peercollapsed) 13.dp else 28.dp)
    )
}

@Composable
fun PeerCountCircle(
    peercollapsed: Boolean,
    peerCount: Long,
    modifier: Modifier = Modifier.size(if (peercollapsed) 12.dp else 25.dp),
) = Box(
    modifier = modifier
        .border(if(peercollapsed) 0.dp else 1.dp, Color.Black, CircleShape)
        .background(Color.LightGray, CircleShape)
)
{
    Text(text = "" + peerCount,
        modifier = Modifier.align(Alignment.Center),
        style = TextStyle(fontSize = if(peercollapsed) 8.sp else 12.sp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupItemViewInfo(groupItem: GroupItem, peercollapsed: Boolean) = Column(
    horizontalAlignment = Start,
    modifier = Modifier.padding(start = 0.dp).randomDebugBorder()
) {
    var p_state = "private"
    if (groupItem.privacyState == ToxVars.TOX_GROUP_PRIVACY_STATE.TOX_GROUP_PRIVACY_STATE_PUBLIC.value) {
        p_state = "public"
    }
    val group_num_ = tox_group_by_groupid__wrapper(groupItem.groupId)
    val offline_num_peers = tox_group_offline_peer_count(group_num_)
    Tooltip(text = "Group Name: " + groupItem.name + "\n"
            + "Group ID: " + groupItem.groupId + "\n"
            + "Group Privacy State: " + p_state + "\n"
            + "Offline Peers: " + offline_num_peers + "\n"
    ) {
        Text(
            text = if(peercollapsed) groupItem.name.take(2) else groupItem.name,
            style = if ((groupItem.name.length > GROUPS_COLUMN_GROUPNAME_LEN_THRESHOLD) && (!peercollapsed))
                MaterialTheme.typography.body1.copy(fontSize = 13.sp) else MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = Ellipsis,
        )
    }
}

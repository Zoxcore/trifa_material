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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.HelperGroup
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_count
import com.zoffcc.applications.trifa.ToxVars

@Composable
fun GroupItemView(
    groupItem: GroupItem,
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
        GroupItemViewInfo(
            groupItem = groupItem,
        )
    }
    ConnectionIndicator(
        modifier = Modifier.padding(end = 1.dp).requiredSize(16.dp),
        isConnected = if (groupItem.isConnected == 0) 0 else 2
    )
    PeerCountCircle(
        modifier = Modifier.padding(end = 1.dp).requiredSize(28.dp),
        peerCount = groupItem.numPeers.toLong()
    )
}

@Composable
fun PeerCountCircle(
    modifier: Modifier = Modifier.size(25.dp),
    peerCount: Long,
) = Box(
    modifier = modifier
        .border(1.dp, Color.Black, CircleShape)
        .background(Color.LightGray, CircleShape)
)
{
    Text(text = "" + peerCount,
        modifier = Modifier.align(Alignment.Center),
        style = TextStyle(fontSize = 12.sp)
    )
}

@Composable
private fun GroupItemViewInfo(groupItem: GroupItem) = Column(
    horizontalAlignment = Start,
    modifier = Modifier.padding(start = 6.dp)
) {
    Text(
        text = groupItem.name,
        style = if (groupItem.name.length > 14) MaterialTheme.typography.body1.copy(fontSize = 13.sp) else MaterialTheme.typography.body1,
        maxLines = 1,
        overflow = Ellipsis,
    )
}

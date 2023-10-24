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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GrouppeerItemView(
    grouppeerItem: GroupPeerItem,
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
        //Box(Modifier.align(Top).padding(vertical = 8.dp)) {
            // ProfileCircle(36.dp, contactItem)
            //NumberBadge(
            //    num = contactItem.unread,
            //    modifier = Modifier.align(TopEnd).offset(6.dp, (-6).dp)
            //)
        //}
        GrouppeerItemViewInfo(
            grouppeerItem = grouppeerItem,
        )
    }
    ConnectionIndicator(
        modifier = Modifier.padding(end = 5.dp).requiredSize(16.dp),
        isConnected = grouppeerItem.connectionStatus
    )
}

fun GroupPeerRoleAsStringShort(peerRole: Int) : String
{
    if (peerRole == 0)
    {
        return "*"
    }
    else if (peerRole == 1)
    {
        return "@"
    }
    else if (peerRole == 2)
    {
        return ""
    }
    else
    {
        return "~"
    }
}

@Composable
private fun GrouppeerItemViewInfo(grouppeerItem: GroupPeerItem) = Column(
    horizontalAlignment = Start,
    modifier = Modifier.padding(start = 0.dp)
) {
    Text(
        text = GroupPeerRoleAsStringShort(grouppeerItem.peerRole) + grouppeerItem.name,
        style = TextStyle(fontSize = 15.sp),
        maxLines = 1,
        overflow = Ellipsis,
    )
}

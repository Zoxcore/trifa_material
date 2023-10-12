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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
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
        Box(Modifier.align(Top).padding(vertical = 8.dp)) {
            ProfileCircle(36.dp, contactItem)
            //NumberBadge(
            //    num = contactItem.unread,
            //    modifier = Modifier.align(TopEnd).offset(6.dp, (-6).dp)
            //)
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

@Composable
private fun ContactItemViewInfo(contactItem: ContactItem) = Column(
    horizontalAlignment = Start,
    modifier = Modifier.padding(start = 6.dp)
) {
    Text(
        text = contactItem.name,
        style = MaterialTheme.typography.body1,
        maxLines = 1,
        overflow = Ellipsis,
    )
}

/*
 * Briar Desktop
 * Copyright (C) 2021-2023 The Briar Project
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

package org.briarproject.briar.desktop.navigation

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import globalstore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.navigation.SidebarButtonState.None
import org.briarproject.briar.desktop.navigation.SidebarButtonState.UnreadMessages
import org.briarproject.briar.desktop.navigation.SidebarButtonState.Warning
import org.briarproject.briar.desktop.ui.UiMode
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import randomDebugBorder

val SIDEBAR_WIDTH = 56.dp

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun BriarSidebar(
    uiMode: UiMode,
    setUiMode: (UiMode) -> Unit,
)
{
    @Composable
    fun BriarSidebarButton(
        mode: UiMode,
        messageCount: Int = 0,
    ) = BriarSidebarButton(
        selected = uiMode == mode,
        onClick = {
                    setUiMode(mode)
            if (mode == UiMode.CONTACTS)
            {
                GlobalScope.launch { globalstore.try_clear_unread_message_count() }
            }
            else if (mode == UiMode.GROUPS)
            {
                GlobalScope.launch { globalstore.try_clear_unread_group_message_count() }
            }
                  },
        icon = mode.icon,
        contentDescription = "",
        sideBarButtonState = if (messageCount == 0) None else UnreadMessages(messageCount),
    )

    Column(
        modifier = Modifier.randomDebugBorder().width(SIDEBAR_WIDTH)
            .fillMaxHeight().padding(vertical = 4.dp).selectableGroup(),
        verticalArrangement = spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { // profile button
        Spacer(Modifier.height(4.dp))
        val current_globalstate by globalstore.stateFlow.collectAsState()
        BriarSidebarButton(UiMode.CONTACTS, messageCount = current_globalstate.contacts_unread_message_count)
        BriarSidebarButton(UiMode.GROUPS, messageCount = current_globalstate.contacts_unread_group_message_count)
        BriarSidebarButton(UiMode.SETTINGS)
        BriarSidebarButton(UiMode.ADDFRIEND)
        BriarSidebarButton(UiMode.ADDGROUP)
        BriarSidebarButton(UiMode.ABOUT)
    }
}

sealed class SidebarButtonState
{
    object None : SidebarButtonState()
    class UnreadMessages(val messageCount: Int) : SidebarButtonState()
    object Warning : SidebarButtonState()
}

@Composable
fun BriarSidebarButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    sideBarButtonState: SidebarButtonState,
) = BadgedBox(
    badge = {
        if (sideBarButtonState is UnreadMessages && sideBarButtonState.messageCount > 0)
        {
            Badge(
                modifier = Modifier.offset((-12).dp, 12.dp),
                backgroundColor = Color.Green,
            )
            {
                if (sideBarButtonState.messageCount < 90)
                {
                    Text("" + sideBarButtonState.messageCount)
                }
                else
                {
                    Text("+")
                }
            }
        } else if (sideBarButtonState is Warning)
        {
            Icon(Icons.Default.Error, i18n("ui.generic_error"), modifier = Modifier.offset((-12).dp, 12.dp).size(16.dp), tint = MaterialTheme.colors.error)
        }
    },
) {
    val tint = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
    IconButton(
        icon = icon,
        iconSize = 30.dp,
        iconTint = tint,
        contentDescription = contentDescription,
        onClick = onClick,
    )
}

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

package org.briarproject.briar.desktop.contact

data class GroupItem(
    val name: String,
    val isConnected: Int,
    val numPeers: Int,
    val groupId: String,
    val privacyState: Int
) {
    fun updateName(name: String) =
        copy(name = name)

    fun updateGroupId(groupId: String) =
        copy(groupId = groupId)

    fun updateNumPeers(numPeers: Int) =
        copy(numPeers = numPeers)

    fun updateIsConnected(isConnected: Int) =
        copy(isConnected = isConnected)

    fun updatePrivacyState(privacyState: Int) =
        copy(privacyState = privacyState)
}

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION

val notConnectedColor: Color = Color.Transparent
val connectedColor: Color = Color.Green
val connectedTCPColor: Color = Color.Yellow

@Composable
fun ConnectionIndicator(
    modifier: Modifier = Modifier.size(16.dp),
    isConnected: Int,
) = Box(
    modifier = if (isConnected == 0)
            modifier
            .background(getConnectionColor(isConnected), CircleShape)
         else
             modifier
            .border(1.dp, Color.Black, CircleShape)
            .background(getConnectionColor(isConnected), CircleShape)
)

fun getConnectionColor(connection_status : Int): Color {
    return when(connection_status) {
        TOX_CONNECTION.TOX_CONNECTION_TCP.value -> connectedTCPColor
        TOX_CONNECTION.TOX_CONNECTION_UDP.value -> connectedColor
        else -> notConnectedColor
    }
}

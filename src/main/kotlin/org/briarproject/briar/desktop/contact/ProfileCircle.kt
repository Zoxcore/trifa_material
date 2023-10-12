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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

/**
 * Display the avatar for a [ContactItem]. If it has an avatar image, display that, otherwise
 * display an [Identicon] based on the user's author id. Either way the profile image is displayed
 * within a circle.
 *
 * @param size the size of the circle. In order to avoid aliasing effects for Identicon-based profile images,
 *             pass a multiple of 9 here. That helps as the image is based on a 9x9 square grid.
 */
@Composable
fun ProfileCircle(size: Dp, contactItem: ContactItem) {
    ProfileCircle(size)
}

/**
 * Display an RSS avatar for RSS feeds.
 *
 * @param size the size of the circle.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun ProfileCircle(size: Dp) {
    val painter = painterResource("friend_avatar.png")
    Image(
        modifier = Modifier.size(size).clip(CircleShape),
        contentScale = ContentScale.Crop,
        painter = painter,
        contentDescription = "User picture"
    )
}

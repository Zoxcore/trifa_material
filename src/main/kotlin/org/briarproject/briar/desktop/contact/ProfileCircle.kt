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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.HelperGeneric.friend_get_avatar
import com.zoffcc.applications.trifa.HelperGeneric.friend_has_avatar
import com.zoffcc.applications.trifa.HelperOSFile
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.ByteArrayInputStream

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
    ProfileCircle2(size, contactItem)
}

/**
 * Display an [Identicon] as a profile image within a circle based on a user's author id.
 *
 * @param size the size of the circle.
 */
@Composable
fun ProfileCircle2(size: Dp, contactItem: ContactItem) {
    var circle_border_width = 1.dp
    var circle_border_color = Color.Black

    if (!contactItem.push_url.isNullOrEmpty()) {
        circle_border_width = 4.dp
        circle_border_color = Color(0xFFFFC300)
    }

    if (friend_has_avatar(contactItem.pubkey))
    {
        val avatar_bytes = friend_get_avatar(contactItem.pubkey)
        if (avatar_bytes != null)
        {
            HelperGeneric.AsyncImage(load = {
                loadImageBitmap(ByteArrayInputStream(avatar_bytes))
            }, painterFor = { remember { BitmapPainter(it) } },
                contentDescription = "Image",
                modifier = Modifier.size(size).clip(CircleShape)
                    .border(circle_border_width, circle_border_color, CircleShape))
        }
    }
    else
    {
        Canvas(
            Modifier.size(size).clip(CircleShape)
                .border(circle_border_width, circle_border_color, CircleShape)
        ) {
            IdenticonKt(contactItem.pubkey, this.size.width, this.size.height).draw(this)
        }
    }
}

/**
 * Display an RSS avatar for RSS feeds.
 *
 * @param size the size of the circle.
 */
/*
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
*/

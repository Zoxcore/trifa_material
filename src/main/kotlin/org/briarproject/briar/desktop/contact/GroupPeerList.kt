package org.briarproject.briar.desktop.contact

import GROUP_PEER_COLUMN_WIDTH
import GROUP_PEER_HEIGHT
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zoffcc.applications.trifa.HelperGroup
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_self_get_public_key
import com.zoffcc.applications.trifa.StateGroupPeers
import grouppeerstore
import org.briarproject.briar.desktop.ui.ListItemView
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import randomDebugBorder
import kotlin.math.max
import kotlin.math.min

@Composable
fun GroupPeerList(
    grouppeerList: StateGroupPeers,
) = Column(
    modifier = Modifier.fillMaxHeight().width(GROUP_PEER_COLUMN_WIDTH).background(Color.Transparent),
) {
    VerticallyScrollableArea(modifier = Modifier.randomDebugBorder().fillMaxSize()) { scrollState ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .semantics {
                    contentDescription = i18n("ui.access_group_peerlist")
                }
                .selectableGroup()
        ) {
            items(
                items = grouppeerList.grouppeers,
                key = { item -> item.pubkey },
                contentType = { item -> item::class }
            ) { item ->
                ListItemView(
                    onSelect = {
                               //grouppeerstore.select(item.pubkey)
                        },
                    selected = (grouppeerList.selectedGrouppeerPubkey == item.pubkey)
                ) {
                    val modifier = Modifier
                        .heightIn(min = GROUP_PEER_HEIGHT)
                        .fillMaxWidth()
                        .background(GroupPeerListBgColor(item))
                        .padding(vertical = 8.dp)
                        .padding(start = 3.dp, end = 3.dp)
                        GrouppeerItemView(
                            grouppeerItem = item,
                            modifier = modifier
                        )
                }
            }
        }
    }
}

fun GroupPeerListBgColor(item: GroupPeerItem): Color
{
    if (item.pubkey == tox_group_self_get_public_key(
            HelperGroup.tox_group_by_groupid__wrapper(item.groupID.lowercase()))
        )
    {
        return Color(0x12FF00FF)
    }
    else
    {
        return Color.Transparent
    }
}

private fun darkenColor(color: Int, fraction: Double): Int
{
    return max(color - color * fraction, 0.0).toInt()
}

private fun lightenColor(color: Int, fraction: Double): Int
{
    return min(color + color * fraction, 255.0).toInt()
}


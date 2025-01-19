@file:Suppress("FunctionName", "SpellCheckingInspection", "LiftReturnOrAssignment", "LocalVariableName")

package org.briarproject.briar.desktop.contact

import GROUP_COLLAPSED_PEER_COLUMN_WIDTH
import GROUP_PEER_COLUMN_WIDTH
import GROUP_PEER_HEIGHT
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
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
import com.zoffcc.applications.trifa.HelperGeneric.force_update_group_peerlist_ui
import com.zoffcc.applications.trifa.HelperGeneric.get_self_group_role
import com.zoffcc.applications.trifa.HelperGeneric.is_peer_self
import com.zoffcc.applications.trifa.HelperGeneric.is_self_group_role_admin
import com.zoffcc.applications.trifa.HelperGroup
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_mod_kick_peer
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_mod_set_role
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_self_get_public_key
import com.zoffcc.applications.trifa.StateGroupPeers
import com.zoffcc.applications.trifa.ToxVars
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.ui.ListItemView
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import randomDebugBorder
import randomDebugBorder2
import kotlin.math.max
import kotlin.math.min

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun GroupPeerList(
    grouppeerList: StateGroupPeers,
    peercollapsed: Boolean,
) = Column(
    modifier = Modifier.fillMaxHeight().
    width(if(peercollapsed) GROUP_COLLAPSED_PEER_COLUMN_WIDTH else GROUP_PEER_COLUMN_WIDTH).
    background(Color.Transparent)
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
                        .randomDebugBorder()
                        .background(GroupPeerListBgColor(item))
                        .padding(vertical = 8.dp)
                        .padding(start = if(peercollapsed) 2.dp else 3.dp, end = if(peercollapsed) 0.dp else 3.dp)
                    ContextMenuArea(items = {
                        val is_admin = is_self_group_role_admin(get_self_group_role(item.groupID))
                        val is_self = is_peer_self(item.groupID, item.pubkey)
                        if ((is_admin) && (!is_self))
                        {
                            listOf(
                                ContextMenuItem("kick") {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        try
                                        {
                                            val group_num = tox_group_by_groupid__wrapper(item.groupID)
                                            val peernum = tox_group_peer_by_public_key(group_num, item.pubkey)
                                            tox_group_mod_kick_peer(group_num, peernum)
                                            force_update_group_peerlist_ui(item.groupID)
                                        }
                                        catch(_: Exception)
                                        {
                                        }
                                    }
                                },
                                ContextMenuItem("make observer") {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        try
                                        {
                                            val group_num = HelperGroup.tox_group_by_groupid__wrapper(item.groupID)
                                            val peernum = MainActivity.tox_group_peer_by_public_key(group_num, item.pubkey)
                                            tox_group_mod_set_role(group_num, peernum, ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_OBSERVER.value)
                                            force_update_group_peerlist_ui(item.groupID)
                                        }
                                        catch(_: Exception)
                                        {
                                        }
                                    }
                                },
                                ContextMenuItem("make normal user") {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        try
                                        {
                                            val group_num = HelperGroup.tox_group_by_groupid__wrapper(item.groupID)
                                            val peernum = MainActivity.tox_group_peer_by_public_key(group_num, item.pubkey)
                                            tox_group_mod_set_role(group_num, peernum, ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_USER.value)
                                            force_update_group_peerlist_ui(item.groupID)
                                        }
                                        catch(_: Exception)
                                        {
                                        }
                                    }
                                },
                                ContextMenuItem("make moderator") {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        try
                                        {
                                            val group_num = HelperGroup.tox_group_by_groupid__wrapper(item.groupID)
                                            val peernum = MainActivity.tox_group_peer_by_public_key(group_num, item.pubkey)
                                            tox_group_mod_set_role(group_num, peernum, ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_MODERATOR.value)
                                            force_update_group_peerlist_ui(item.groupID)
                                        }
                                        catch(_: Exception)
                                        {
                                        }
                                    }
                                }
                            )
                        }
                        else
                        {
                            listOf()
                        }
                    }) {
                        GrouppeerItemView(
                            grouppeerItem = item,
                            modifier = modifier,
                            peercollapsed = peercollapsed
                        )
                    }
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


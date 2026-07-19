@file:Suppress("FunctionName", "SpellCheckingInspection", "LiftReturnOrAssignment", "LocalVariableName", "ConvertToStringTemplate")

package org.briarproject.briar.desktop.contact

import GROUP_COLLAPSED_PEER_COLUMN_WIDTH
import GROUP_PEER_COLUMN_WIDTH
import GROUP_PEER_HEIGHT
import SnackBarToast
import UIGroupMessage
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.HelperGeneric.force_update_group_peerlist_ui
import com.zoffcc.applications.trifa.HelperGeneric.get_self_group_role
import com.zoffcc.applications.trifa.HelperGeneric.is_peer_self
import com.zoffcc.applications.trifa.HelperGeneric.is_self_group_role_founder
import com.zoffcc.applications.trifa.HelperGeneric.is_self_group_role_moderator
import com.zoffcc.applications.trifa.HelperGroup
import com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_mod_kick_peer
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_mod_set_role
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_peer_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_self_get_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_send_private_message_by_peerpubkey
import com.zoffcc.applications.trifa.StateGroupPeers
import com.zoffcc.applications.trifa.TRIFAGlobals
import com.zoffcc.applications.trifa.ToxVars
import groupmessagestore
import groupstore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import myUser
import org.briarproject.briar.desktop.ui.ListItemView
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import randomDebugBorder
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
    var showPmDialog by remember { mutableStateOf(false) }
    var pmTextMessage by remember { mutableStateOf("") }
    var activePmPeer by remember { mutableStateOf<GroupPeerItem?>(null) }

    var showKickDialog by remember { mutableStateOf(false) }
    var kickPeer by remember { mutableStateOf<GroupPeerItem?>(null) }

    if (showKickDialog) {
        val kickPeerSnapshot = kickPeer!!
        AlertDialog(
            onDismissRequest = { showKickDialog = false },
            title = { Text("Kick Peer") },
            text = { Text("Are you sure you want to kick this peer out of the group?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showKickDialog = false
                        GlobalScope.launch(Dispatchers.IO) {
                            try
                            {
                                val group_num = tox_group_by_groupid__wrapper(kickPeerSnapshot.groupID)
                                val peernum = tox_group_peer_by_public_key(group_num, kickPeerSnapshot.pubkey)
                                tox_group_mod_kick_peer(group_num, peernum)
                                force_update_group_peerlist_ui(kickPeerSnapshot.groupID)
                            } catch (_: Exception)
                            {
                            }
                            kickPeer = null
                        }
                        // kick a peer
                    }
                ) {
                    Text("Kick", color = Color.Red)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { kickPeer = null; showKickDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    if (showPmDialog && activePmPeer != null) {
        val peerSnapshot = activePmPeer!!
        AlertDialog(modifier = Modifier.fillMaxWidth(),
            onDismissRequest = {
                showPmDialog = false // Close on clicking outside
                activePmPeer = null
            },
            title = { Text(text = "Send Message to ${peerSnapshot.name} / ${peerSnapshot.pubkey.take(8)}") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    OutlinedTextField(
                        value = pmTextMessage,
                        onValueChange = { pmTextMessage = it },
                        label = { Text("Your Message") },
                        singleLine = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pmTextMessage.isNotBlank()) {
                            if (peerSnapshot.groupID != null)
                            {
                                val timestamp = System.currentTimeMillis()
                                val groupnum: Long = tox_group_by_groupid__wrapper(peerSnapshot.groupID!!)
                                val my_group_peerpk = tox_group_self_get_public_key(groupnum)
                                val peer_id = tox_group_peer_by_public_key(groupnum, peerSnapshot.pubkey.uppercase())

                                val res = tox_group_send_private_message_by_peerpubkey(groupnum,
                                    peerSnapshot.pubkey,
                                    0, pmTextMessage)

                                if (res == 0)
                                {
                                    var peer_role = -1
                                    try
                                    {
                                        val self_peer_role = MainActivity.tox_group_self_get_role(groupnum)
                                        if (self_peer_role >= 0)
                                        {
                                            peer_role = self_peer_role
                                        }
                                    } catch (_: Exception)
                                    {
                                    }

                                    val db_msgid = MainActivity.sent_groupmessage_to_db(groupid = peerSnapshot.groupID, message_timestamp = timestamp,
                                        group_message = pmTextMessage, message_id = 0, was_synced = false, 1,
                                        sent_privately_to_tox_group_peer_pubkey = peerSnapshot.pubkey.uppercase())
                                    groupmessagestore.send(GroupMessageAction.SendGroupMessage(
                                        UIGroupMessage(
                                            was_synced = false,
                                            is_private_msg = 1,
                                            sentTimeMs = timestamp,
                                            rcvdTimeMs = timestamp,
                                            syncdTimeMs = timestamp,
                                            peer_role = peer_role,
                                            msg_id_hash = "",
                                            message_id_tox = "", msgDatabaseId = db_msgid,
                                            user = myUser, timeMs = timestamp, text = pmTextMessage,
                                            toxpk = my_group_peerpk,
                                            groupId = peerSnapshot.groupID!!.lowercase(),
                                            trifaMsgType = TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT.value,
                                            filename_fullpath = null,
                                            sent_privately_to_tox_group_peer_pubkey = peerSnapshot.pubkey.uppercase())))
                                } else
                                {
                                    SnackBarToast("Sending Group Message failed")
                                }
                            }



                            // Clear text and close dialog
                            pmTextMessage = ""
                            activePmPeer = null
                            showPmDialog = false
                        }
                    }
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pmTextMessage = "" // Clear text
                        showPmDialog = false // Close dialog
                        activePmPeer = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

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
                        val is_admin = is_self_group_role_founder(get_self_group_role(item.groupID))
                        val is_mod = is_self_group_role_moderator(get_self_group_role(item.groupID))
                        val is_self = is_peer_self(item.groupID, item.pubkey)
                        var menu_items_list = mutableListOf<ContextMenuItem>()
                        if ((is_admin || is_mod) && (!is_self))
                        {
                            menu_items_list.add(ContextMenuItem("kick") {
                                showKickDialog = true
                                kickPeer = item
                            })
                        }

                        if ((is_admin || is_mod) && (!is_self))
                        {
                            menu_items_list.add(ContextMenuItem("make observer") {
                                GlobalScope.launch(Dispatchers.IO) {
                                    try
                                    {
                                        val group_num = HelperGroup.tox_group_by_groupid__wrapper(item.groupID)
                                        val peernum = MainActivity.tox_group_peer_by_public_key(group_num, item.pubkey)
                                        tox_group_mod_set_role(group_num, peernum, ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_OBSERVER.value)
                                        force_update_group_peerlist_ui(item.groupID)
                                    } catch (_: Exception)
                                    {
                                    }
                                }
                            })
                        }

                        if ((is_admin || is_mod) && (!is_self))
                        {
                            menu_items_list.add(ContextMenuItem("make normal user") {
                                GlobalScope.launch(Dispatchers.IO) {
                                    try
                                    {
                                        val group_num = HelperGroup.tox_group_by_groupid__wrapper(item.groupID)
                                        val peernum = MainActivity.tox_group_peer_by_public_key(group_num, item.pubkey)
                                        tox_group_mod_set_role(group_num, peernum, ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_USER.value)
                                        force_update_group_peerlist_ui(item.groupID)
                                    } catch (_: Exception)
                                    {
                                    }
                                }
                            })
                        }

                        if ((is_admin) && (!is_self))
                        {
                            menu_items_list.add(ContextMenuItem("make moderator") {
                                GlobalScope.launch(Dispatchers.IO) {
                                    try
                                    {
                                        val group_num = HelperGroup.tox_group_by_groupid__wrapper(item.groupID)
                                        val peernum = MainActivity.tox_group_peer_by_public_key(group_num, item.pubkey)
                                        tox_group_mod_set_role(group_num, peernum, ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_MODERATOR.value)
                                        force_update_group_peerlist_ui(item.groupID)
                                    } catch (_: Exception)
                                    {
                                    }
                                }
                            })
                        }

                        if (!is_self)
                        {
                            menu_items_list.add(ContextMenuItem("send private message") {
                                activePmPeer = item.copy()
                                pmTextMessage = ""
                                showPmDialog = true
                            })
                        }
                        val a: List<ContextMenuItem> = menu_items_list.toList()
                        a
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


@file:Suppress("LocalVariableName")

package org.briarproject.briar.desktop.contact

import BG_COLOR_OWN_RELAY_CONTACT_ITEM
import BG_COLOR_RELAY_CONTACT_ITEM
import CONTACTITEM_HEIGHT
import CONTACT_COLUMN_WIDTH
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.zoffcc.applications.trifa.HelperGeneric.delete_friend_wrapper
import com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper
import com.zoffcc.applications.trifa.HelperGroup
import com.zoffcc.applications.trifa.HelperRelay.delete_relay
import com.zoffcc.applications.trifa.HelperRelay.is_any_relay
import com.zoffcc.applications.trifa.HelperRelay.is_own_relay
import com.zoffcc.applications.trifa.HelperRelay.remove_own_relay_in_db
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_invite_friend
import com.zoffcc.applications.trifa.StateContacts
import contactstore
import friendsettingsstore
import globalfrndstoreunreadmsgs
import globalstore
import groupstore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.ui.ListItemView
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import randomDebugBorder

@OptIn(DelicateCoroutinesApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContactList(
    contactList: StateContacts,
) = Column(
    modifier = Modifier.fillMaxHeight().width(CONTACT_COLUMN_WIDTH).background(Color.Transparent),
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ContactItem?>(null) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var contactToInvite by remember { mutableStateOf<ContactItem?>(null) }

    if (showDeleteDialog) {
        val itemToDeleteSnapshot = itemToDelete!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Contact") },
            text = { Text("Are you sure you want to delete this contact and all associated messages?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        contactstore.remove(item = ContactItem(name = "",
                            isConnected = 0,
                            is_relay = false,
                            push_url = "",
                            pubkey = itemToDeleteSnapshot!!.pubkey))
                        GlobalScope.launch(Dispatchers.IO) {
                            if (is_any_relay(itemToDeleteSnapshot!!.pubkey))
                            {
                                if (is_own_relay(itemToDeleteSnapshot!!.pubkey)) {
                                    remove_own_relay_in_db()
                                } else {
                                    delete_relay(itemToDeleteSnapshot!!.pubkey, true)
                                }
                            } else {
                                delete_friend_wrapper(itemToDeleteSnapshot!!.pubkey, "Friend removed")
                            }
                            itemToDelete = null
                        }
                        // delete a contact including all messages

                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null ; showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    if (showInviteDialog && contactToInvite != null) {
        val contactToInviteSnapshot = contactToInvite!!
        val dialogScope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = {
                showInviteDialog = false
                contactToInvite = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .padding(24.dp)
                .wrapContentHeight()
                .widthIn(max = 360.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Header Title
                    Text(
                        text = "Invite to Group",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = "Select a group to invite ${contactToInviteSnapshot.pubkey.take(6)} / ${contactToInviteSnapshot.name.take(20)} to:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )

                    // Scrollable Group Selection
                    Box(
                        modifier = Modifier
                            .weight(weight = 1f, fill = false)
                            .heightIn(max = 300.dp)
                    ) {
                        if (groupstore.state.groups.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No groups available",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(groupstore.state.groups) { group ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                dialogScope.launch {
                                                    val group_num = HelperGroup.tox_group_by_groupid__wrapper(group.groupId)
                                                    val friend_num = tox_friend_by_public_key(contactToInviteSnapshot.pubkey)
                                                    tox_group_invite_friend(group_num, friend_num)
                                                    update_savedata_file_wrapper()
                                                }
                                                showInviteDialog = false
                                                contactToInvite = null
                                            }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = group.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = "ID: ${group.groupId.take(8)}...",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bottom Action Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                showInviteDialog = false
                                contactToInvite = null
                            }
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }


    VerticallyScrollableArea(modifier = Modifier.randomDebugBorder().fillMaxSize()) { scrollState ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .semantics {
                    contentDescription = i18n("ui.access_contact_list")
                }
                .selectableGroup()
        ) {
            items(
                items = contactList.contacts,
                key = { item -> item.pubkey },
                contentType = { item -> item::class }
            ) { item ->
                val ListItemViewScope = rememberCoroutineScope()
                ListItemView(
                    onSelect = {
                                friendsettingsstore.visible(false)
                                ListItemViewScope.launch { globalstore.try_clear_unread_message_count() }
                                globalfrndstoreunreadmsgs.hard_clear_unread_per_friend_message_count(item.pubkey)
                                contactstore.select(item.pubkey)
                                contactstore.fullHistoryActive(false)
                               },
                    selected = (contactList.selectedContactPubkey == item.pubkey)
                ) {
                    var bgcolor = if (item.is_relay) Color(BG_COLOR_RELAY_CONTACT_ITEM) else Color.Transparent
                    try
                    {
                        if (is_own_relay(item.pubkey))
                        {
                            bgcolor = Color(BG_COLOR_OWN_RELAY_CONTACT_ITEM)
                        }
                    }
                    catch(_: Exception)
                    {
                    }
                    val modifier = Modifier
                        .heightIn(min = CONTACTITEM_HEIGHT)
                        .fillMaxWidth()
                        .background(bgcolor)
                        .padding(vertical = 8.dp)
                        .padding(start = 16.dp, end = 4.dp)
                    ContextMenuArea(items = {
                        listOf(
                            ContextMenuItem("invite to group") {
                                contactToInvite = item
                                showInviteDialog = true
                            },
                            ContextMenuItem("delete") {
                                itemToDelete = item
                                showDeleteDialog = true
                            },
                        )
                    }) {
                        ContactItemView(
                            contactItem = item,
                            modifier = modifier
                        )
                    }
                }
            }
        }
    }
}

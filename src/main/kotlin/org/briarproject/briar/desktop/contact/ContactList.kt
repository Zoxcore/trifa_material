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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.zoffcc.applications.trifa.HelperGeneric.delete_friend_wrapper
import com.zoffcc.applications.trifa.HelperGeneric.delete_only_msgs_and_files_friend_wrapper
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
import messagestore
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

    // New state variables for the separate "delete all messages and files" dialog
    var showDeleteAllMessagesDialog by remember { mutableStateOf(false) }
    var itemToDeleteAllMessages by remember { mutableStateOf<ContactItem?>(null) }

    var showInviteDialog by remember { mutableStateOf(false) }
    var contactToInvite by remember { mutableStateOf<ContactItem?>(null) }

    var showConfirmInviteDialog by remember { mutableStateOf(false) }
    var pendingInviteGroupId by remember { mutableStateOf<String?>(null) }
    var pendingInviteGroupName by remember { mutableStateOf<String?>(null) }

    val dialogScope = rememberCoroutineScope()

    if (showDeleteDialog) {
        val itemToDeleteSnapshot = itemToDelete!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            title = {
                Text(
                    text = "Delete Contact",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Are you sure you want to delete this contact and all associated messages and files?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Modern Contact Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar Circle
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                ProfileCircle(40.dp, itemToDeleteSnapshot)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    // First 6 of pubkey, then username
                                    text = "${itemToDeleteSnapshot.pubkey.take(6)} / ${itemToDeleteSnapshot.name.take(20).ifEmpty { "Unknown" }}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        contactstore.remove(item = ContactItem(name = "",
                            isConnected = 0,
                            is_relay = false,
                            push_url = "",
                            pubkey = itemToDeleteSnapshot.pubkey))
                        GlobalScope.launch(Dispatchers.IO) {
                            if (is_any_relay(itemToDeleteSnapshot.pubkey))
                            {
                                if (is_own_relay(itemToDeleteSnapshot.pubkey)) {
                                    remove_own_relay_in_db()
                                } else {
                                    delete_relay(itemToDeleteSnapshot.pubkey, true)
                                }
                            } else {
                                delete_friend_wrapper(itemToDeleteSnapshot.pubkey, "Friend removed")
                            }
                            itemToDelete = null
                        }
                        // delete a contact including all messages
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { itemToDelete = null ; showDeleteDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Separate dialog for deleting all messages and files
    if (showDeleteAllMessagesDialog) {
        val itemToDeleteAllMessagesSnapshot = itemToDeleteAllMessages!!
        AlertDialog(
            onDismissRequest = { showDeleteAllMessagesDialog = false },
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            title = {
                Text(
                    text = "Delete All Messages and Files",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Are you sure you want to delete all messages and files for this contact? The contact itself will remain",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Modern Contact Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar Circle
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                ProfileCircle(40.dp, itemToDeleteAllMessagesSnapshot)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    // First 6 of pubkey, then username
                                    text = "${itemToDeleteAllMessagesSnapshot.pubkey.take(6)} / ${itemToDeleteAllMessagesSnapshot.name.take(20).ifEmpty { "Unknown" }}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllMessagesDialog = false
                        GlobalScope.launch(Dispatchers.IO) {
                            delete_only_msgs_and_files_friend_wrapper(itemToDeleteAllMessagesSnapshot.pubkey, "Messages and Files removed")
                            messagestore.removeAllForPubkey(itemToDeleteAllMessagesSnapshot.pubkey)
                        }
                        itemToDeleteAllMessages = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete All", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { itemToDeleteAllMessages = null ; showDeleteAllMessagesDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (showConfirmInviteDialog && contactToInvite != null && pendingInviteGroupId != null) {
        val contactToInviteSnapshot = contactToInvite!!
        val groupIdSnapshot = pendingInviteGroupId!!
        val groupNameSnapshot = pendingInviteGroupName ?: "the selected group"

        AlertDialog(
            onDismissRequest = {
                showConfirmInviteDialog = false
            },
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            title = {
                Text(
                    text = "Confirm Invitation",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "You are about to invite the following contact:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Modern Contact Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar Circle
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                ProfileCircle(40.dp, contactToInviteSnapshot)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    // First 6 of pubkey, then username
                                    text = "${contactToInviteSnapshot.pubkey.take(6)} / ${contactToInviteSnapshot.name.take(20).ifEmpty { "Unknown" }}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Stylish Group Item
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "To the group",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${groupIdSnapshot.take(8)} / $groupNameSnapshot",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmInviteDialog = false
                        GlobalScope.launch {
                            val group_num = HelperGroup.tox_group_by_groupid__wrapper(groupIdSnapshot)
                            val friend_num = tox_friend_by_public_key(contactToInviteSnapshot.pubkey)
                            tox_group_invite_friend(group_num, friend_num)
                            update_savedata_file_wrapper()
                        }
                        contactToInvite = null
                        pendingInviteGroupId = null
                        pendingInviteGroupName = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Invite", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmInviteDialog = false
                        contactToInvite = null
                        pendingInviteGroupId = null
                        pendingInviteGroupName = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (showInviteDialog && contactToInvite != null) {
        val contactToInviteSnapshot = contactToInvite!!

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

                    val scrollState = rememberScrollState()

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
                            Row(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(scrollState),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Top padding equivalent to LazyColumn's contentPadding
                                    Spacer(modifier = Modifier.height(4.dp))

                                    groupstore.state.groups.forEach { group ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    pendingInviteGroupId = group.groupId
                                                    pendingInviteGroupName = group.name
                                                    showConfirmInviteDialog = true
                                                    showInviteDialog = false
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

                                    // Bottom padding equivalent to LazyColumn's contentPadding
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                @Composable
                                fun CustomVerticalScrollbar(
                                    scrollState: androidx.compose.foundation.ScrollState,
                                    modifier: Modifier = Modifier
                                ) {
                                    if (scrollState.maxValue > 0) {
                                        androidx.compose.foundation.layout.BoxWithConstraints(
                                            modifier = modifier
                                                .fillMaxHeight()
                                                .width(6.dp)
                                                .padding(vertical = 4.dp)
                                        ) {
                                            val totalContent = scrollState.maxValue + scrollState.viewportSize
                                            if (totalContent > 0 && scrollState.viewportSize > 0) {
                                                val density = androidx.compose.ui.platform.LocalDensity.current
                                                val trackHeightPx = with(density) { maxHeight.toPx() }
                                                val thumbHeightPx = (scrollState.viewportSize.toFloat() / totalContent) * trackHeightPx
                                                val thumbOffsetPx = (scrollState.value.toFloat() / totalContent) * trackHeightPx

                                                val thumbHeight = thumbHeightPx / density.density
                                                val thumbOffset = thumbOffsetPx / density.density

                                                val draggableDistance = trackHeightPx - thumbHeightPx
                                                val scrollRatio = if (draggableDistance > 0) scrollState.maxValue.toFloat() / draggableDistance else 0f

                                                androidx.compose.foundation.layout.Box(
                                                    modifier = Modifier
                                                        .offset(y = thumbOffset.dp)
                                                        .height(thumbHeight.dp)
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                        .pointerInput(scrollState.maxValue, trackHeightPx, scrollRatio) {
                                                            detectDragGestures { change, dragAmount ->
                                                                change.consume()
                                                                if (scrollRatio > 0f) {
                                                                    val scrollDelta = dragAmount.y * scrollRatio
                                                                    scrollState.dispatchRawDelta(scrollDelta)
                                                                }
                                                            }
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }

                                // The custom scrollbar placed next to the scrollable column
                                CustomVerticalScrollbar(
                                    scrollState = scrollState,
                                    modifier = Modifier.fillMaxHeight()
                                )
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
                            ContextMenuItem("delete all messages and files") {
                                itemToDeleteAllMessages = item
                                showDeleteAllMessagesDialog = true
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

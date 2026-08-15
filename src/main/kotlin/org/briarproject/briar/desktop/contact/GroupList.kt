package org.briarproject.briar.desktop.contact

import GROUPITEM_HEIGHT
import GROUPS_COLLAPSED_COLUMN_WIDTH
import GROUPS_COLUMN_WIDTH
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.HelperGeneric.delete_friend_wrapper
import com.zoffcc.applications.trifa.HelperRelay.delete_relay
import com.zoffcc.applications.trifa.HelperRelay.is_any_relay
import com.zoffcc.applications.trifa.HelperRelay.is_own_relay
import com.zoffcc.applications.trifa.HelperRelay.remove_own_relay_in_db
import com.zoffcc.applications.trifa.StateGroups
import contactstore
import globalstore
import groupsettingsstore
import groupstore
import globalgrpstoreunreadmsgs
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.ui.ListItemView
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun GroupList(
    groupList: StateGroups,
    peercollapsed: Boolean,
) = Column(
    modifier = Modifier.fillMaxHeight().width(if(peercollapsed) GROUPS_COLLAPSED_COLUMN_WIDTH else GROUPS_COLUMN_WIDTH).background(Color.Transparent),
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<GroupItem?>(null) }

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
                    text = "Delete Group",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Are you sure you want to delete this group and all associated messages?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

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
                                // First 8 of groupid, then name
                                text = "${itemToDeleteSnapshot.groupId.take(8)} / ${itemToDeleteSnapshot.name}",
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
                        showDeleteDialog = false
                        groupstore.remove(item = GroupItem(privacyState = 0, name = "", isConnected = 0, groupId = itemToDeleteSnapshot.groupId, numPeers = 0))
                        GlobalScope.launch(Dispatchers.IO) {
                            HelperGeneric.delete_group_wrapper(itemToDeleteSnapshot.groupId)
                            itemToDelete = null
                        }
                        // delete a group including all messages
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

    VerticallyScrollableArea(modifier = Modifier.fillMaxSize()) { scrollState ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .semantics {
                    contentDescription = i18n("ui.access_group_list")
                }
                .selectableGroup()
        ) {
            items(
                items = groupList.groups,
                key = { item -> item.groupId },
                contentType = { item -> item::class }
            ) { item ->
                val ListItemViewScope = rememberCoroutineScope()
                ListItemView(
                    onSelect = {
                        groupsettingsstore.visible(false)
                        ListItemViewScope.launch {
                            globalstore.try_clear_unread_group_message_count()
                        }
                        globalgrpstoreunreadmsgs.hard_clear_unread_per_group_message_count(item.groupId)
                        groupstore.select(item.groupId)
                        groupstore.fullHistoryActive(false)
                               },
                    selected = (groupList.selectedGroupId == item.groupId)
                ) {
                    val modifier = Modifier
                        .heightIn(min = GROUPITEM_HEIGHT)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .padding(start = if(peercollapsed) 2.dp else 3.dp, end = if(peercollapsed) 0.dp else 3.dp)
                    ContextMenuArea(items = {
                        listOf(
                            ContextMenuItem("delete") {
                                itemToDelete = item
                                showDeleteDialog = true
                            },
                        )
                    }) {
                        GroupItemView(
                            groupItem = item,
                            modifier = modifier,
                            peercollapsed = peercollapsed
                        )
                    }
                }
            }
        }
    }
}



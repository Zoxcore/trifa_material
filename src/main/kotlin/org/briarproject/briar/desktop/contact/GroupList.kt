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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
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
            title = { Text("Delete Group") },
            text = { Text("Are you sure you want to delete this group and all associated messages?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        groupstore.remove(item = GroupItem(privacyState = 0, name = "", isConnected = 0, groupId = itemToDeleteSnapshot.groupId, numPeers = 0))
                        GlobalScope.launch(Dispatchers.IO) {
                            HelperGeneric.delete_group_wrapper(itemToDeleteSnapshot.groupId)
                            itemToDelete = null
                        }
                        // delete a group including all messages
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



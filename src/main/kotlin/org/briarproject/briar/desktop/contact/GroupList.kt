package org.briarproject.briar.desktop.contact

import GROUPITEM_HEIGHT
import GROUPS_COLUMN_WIDTH
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
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.StateContacts
import com.zoffcc.applications.trifa.StateGroups
import contactstore
import groupstore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.ui.ListItemView
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun GroupList(
    groupList: StateGroups,
) = Column(
    modifier = Modifier.fillMaxHeight().width(GROUPS_COLUMN_WIDTH).background(Color.Transparent),
) {
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
                ListItemView(
                    onSelect = { groupstore.select(item.groupId) },
                    selected = (groupList.selectedGroupId == item.groupId)
                ) {
                    val modifier = Modifier
                        .heightIn(min = GROUPITEM_HEIGHT)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .padding(start = 3.dp, end = 3.dp)
                    ContextMenuArea(items = {
                        listOf(
                            ContextMenuItem("delete") {
                                groupstore.remove(item = GroupItem(privacyState = 0, name = "", isConnected = 0, groupId = item.groupId, numPeers = 0))
                                GlobalScope.launch(Dispatchers.IO) {
                                    HelperGeneric.delete_group_wrapper(item.groupId)
                                }
                                // delete a group including all messages
                            },
                        )
                    }) {
                        GroupItemView(
                            groupItem = item,
                            modifier = modifier
                        )
                    }
                }
            }
        }
    }
}



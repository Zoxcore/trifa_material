package org.briarproject.briar.desktop.contact

import CONTACT_COLUMN_WIDTH
import TOP_HEADER_SIZE
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
import com.zoffcc.applications.trifa.StateContacts
import com.zoffcc.applications.trifa.StateGroups
import contactstore
import groupstore
import org.briarproject.briar.desktop.ui.ListItemView
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun GroupList(
    groupList: StateGroups,
) = Column(
    modifier = Modifier.fillMaxHeight().width(CONTACT_COLUMN_WIDTH).background(Color.Transparent),
) {
    VerticallyScrollableArea(modifier = Modifier.fillMaxSize()) { scrollState ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .semantics {
                    contentDescription = i18n("access.group.list")
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
                        .heightIn(min = TOP_HEADER_SIZE)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .padding(start = 16.dp, end = 4.dp)
                    GroupItemView(
                        groupItem = item,
                        modifier = modifier
                    )
                }
            }
        }
    }
}



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
import org.briarproject.briar.desktop.ui.ListItemView
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun ContactList(
    contactList: StateContacts,
    isSelected: String?,
    selectContact: ContactItem?,
) = Column(
    modifier = Modifier.fillMaxHeight().width(CONTACT_COLUMN_WIDTH).background(Color.Transparent),
) {
    VerticallyScrollableArea(modifier = Modifier.fillMaxSize()) { scrollState ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .semantics {
                    contentDescription = i18n("access.contact.list")
                }
                .selectableGroup()
        ) {
            items(
                items = contactList.contacts,
                key = { item -> item.pubkey },
                contentType = { item -> item::class }
            ) { item ->
                ListItemView(
                    // onSelect = {},
                    // selected = isSelected(item),
                    // let divider start at horizontal position of text
                    dividerOffsetFromStart = (16 + 36 + 12).dp,
                ) {
                    val modifier = Modifier
                        .heightIn(min = TOP_HEADER_SIZE)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .padding(start = 16.dp, end = 4.dp)

                    when (item) {
                        is ContactItem -> {
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
}


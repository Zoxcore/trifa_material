package org.briarproject.briar.desktop.contact

import CONTACTITEM_HEIGHT
import CONTACT_COLUMN_WIDTH
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
import com.zoffcc.applications.trifa.HelperGeneric.delete_friend_wrapper
import com.zoffcc.applications.trifa.StateContacts
import contactstore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.ui.ListItemView
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun ContactList(
    contactList: StateContacts,
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
                    onSelect = { contactstore.select(item.pubkey) },
                    selected = (contactList.selectedContactPubkey == item.pubkey)
                ) {
                    val modifier = Modifier
                        .heightIn(min = CONTACTITEM_HEIGHT)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .padding(start = 16.dp, end = 4.dp)
                    ContextMenuArea(items = {
                        listOf(
                            ContextMenuItem("delete") {
                                contactstore.remove(item = ContactItem(name = "", isConnected = 0, pubkey = item.pubkey))
                                GlobalScope.launch(Dispatchers.IO) {
                                    delete_friend_wrapper(item.pubkey)
                                }
                                 // delete a contact including all messages
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



package org.briarproject.briar.desktop.contact

import BG_COLOR_OWN_RELAY_CONTACT_ITEM
import BG_COLOR_RELAY_CONTACT_ITEM
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zoffcc.applications.trifa.HelperGeneric.delete_friend_wrapper
import com.zoffcc.applications.trifa.HelperRelay.delete_relay
import com.zoffcc.applications.trifa.HelperRelay.is_any_relay
import com.zoffcc.applications.trifa.HelperRelay.is_own_relay
import com.zoffcc.applications.trifa.HelperRelay.remove_own_relay_in_db
import com.zoffcc.applications.trifa.StateContacts
import contactstore
import friendsettingsstore
import globalfrndstoreunreadmsgs
import globalstore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.ui.ListItemView
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import randomDebugBorder

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ContactList(
    contactList: StateContacts,
) = Column(
    modifier = Modifier.fillMaxHeight().width(CONTACT_COLUMN_WIDTH).background(Color.Transparent),
) {
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
                            ContextMenuItem("delete") {
                                contactstore.remove(item = ContactItem(name = "",
                                    isConnected = 0,
                                    is_relay = false,
                                    push_url = "",
                                    pubkey = item.pubkey))
                                GlobalScope.launch(Dispatchers.IO) {
                                    if (is_any_relay(item.pubkey))
                                    {
                                        if (is_own_relay(item.pubkey)) {
                                            remove_own_relay_in_db()
                                        } else {
                                            delete_relay(item.pubkey, true)
                                        }
                                    } else {
                                        delete_friend_wrapper(item.pubkey, "Friend removed")
                                    }
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



package com.zoffcc.applications.trifa

import global_semaphore_contactlist_ui
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.briarproject.briar.desktop.contact.ContactItem
import java.util.*
import kotlin.collections.ArrayList

data class StateContacts(val contacts: List<ContactItem> = emptyList(), val visible: Boolean = false,
                         val selectedContactPubkey: String? = null, val selectedContact: ContactItem? = null,
                         var messageFilterActive: Boolean = false, var messageFilterString: String = "")

const val TAG = "trifa.ContactsStore"

interface ContactStore
{
    fun add(item: ContactItem)
    fun remove(item: ContactItem)
    fun select(pubkey: String?)
    fun visible(value: Boolean)
    fun clear()
    fun messagefilterActive(value: Boolean)
    fun messagefilterString(value: String)
    fun messageresetFilter()
    fun update(item: ContactItem)
    fun update_ipaddr(pubkey: String, ipaddr: String)
    val stateFlow: StateFlow<StateContacts>
    val state get() = stateFlow.value
}

fun CoroutineScope.createContactStore(): ContactStore
{
    val mutableStateFlow = MutableStateFlow(StateContacts())
    // val channel: Channel<ContactItem> = Channel(Channel.UNLIMITED)

    return object : ContactStore
    {
        override val stateFlow: StateFlow<StateContacts> = mutableStateFlow

        init
        {
            // launch {
            //    channel.consumeAsFlow().collect { item ->
            //        mutableStateFlow.value = state.copy(contacts = (state.contacts + item))
            //    }
            //}
        }

        override fun add(item: ContactItem)
        {
            //launch {
                global_semaphore_contactlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                var found = false
                state.contacts.forEach {
                    if (item.pubkey == it.pubkey)
                    { // friend already in list
                        found = true
                    }
                }
                if (!found)
                {
                    var new_contacts: ArrayList<ContactItem> = ArrayList()
                    new_contacts.addAll(state.contacts)
                    new_contacts.forEach { item2 ->
                        if (item2.pubkey == item.pubkey)
                        {
                            new_contacts.remove(item2)
                        }
                    }
                    new_contacts.add(item)
                    // new_contacts.sortBy { it.pubkey }
                    new_contacts = getFriendListWithGroupingAndSorting(new_contacts)
                    mutableStateFlow.value = state.copy(contacts = new_contacts)
                }
                global_semaphore_contactlist_ui.release()
            //}
        }

        override fun remove(item: ContactItem)
        {
            //launch {
                global_semaphore_contactlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                var sel_pubkey = state.selectedContactPubkey
                var sel_item = state.selectedContact
                var new_contacts: ArrayList<ContactItem> = ArrayList()
                new_contacts.addAll(state.contacts)
                var to_remove_item: ContactItem? = null
                state.contacts.forEach {
                    if (item.pubkey == it.pubkey)
                    {
                        if (state.selectedContactPubkey == it.pubkey)
                        {
                            sel_pubkey = null
                            sel_item = null
                        }
                        new_contacts.forEach { item2 ->
                            if (item2.pubkey == it.pubkey)
                            {
                                to_remove_item = item2
                            }
                        }
                    }
                }
                if (to_remove_item != null)
                {
                    new_contacts.remove(to_remove_item)
                }
                // new_contacts.sortBy { it.pubkey }
                new_contacts = getFriendListWithGroupingAndSorting(new_contacts)
                mutableStateFlow.value = state.copy(contacts = new_contacts,
                    selectedContact = sel_item, selectedContactPubkey = sel_pubkey)
                global_semaphore_contactlist_ui.release()
            //}
        }

        override fun select(pubkey: String?)
        {
            //launch {
                global_semaphore_contactlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                var wanted_contact_item: ContactItem? = null
                state.contacts.forEach {
                    if (pubkey == it.pubkey)
                    {
                        wanted_contact_item = it
                    }
                }
                var used_pubkey = pubkey
                if (wanted_contact_item == null)
                {
                    used_pubkey = null
                }
                mutableStateFlow.value = state.copy(contacts = state.contacts, selectedContactPubkey = used_pubkey, selectedContact = wanted_contact_item)
                global_semaphore_contactlist_ui.release()
            //}
        }

        override fun visible(value: Boolean)
        {
            global_semaphore_contactlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            mutableStateFlow.value = state.copy(visible = value)
            global_semaphore_contactlist_ui.release()
        }

        override fun update(item: ContactItem)
        {
            //launch {
                global_semaphore_contactlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                var update_item: ContactItem? = null
                state.contacts.forEach {
                    if (item.pubkey == it.pubkey)
                    {
                        update_item = it.copy()
                    }
                }
                if (update_item != null)
                {
                    var new_contacts: ArrayList<ContactItem> = ArrayList()
                    new_contacts.addAll(state.contacts)
                    var to_remove_item: ContactItem? = null
                    new_contacts.forEach { item2 ->
                        if (item2.pubkey == update_item!!.pubkey)
                        {
                            to_remove_item = item2
                        }
                    }
                    if (to_remove_item != null)
                    {
                        new_contacts.remove(to_remove_item)
                    }
                    new_contacts.add(item)
                    // new_contacts.sortBy { it.pubkey }
                    new_contacts = getFriendListWithGroupingAndSorting(new_contacts)
                    mutableStateFlow.value = state.copy(contacts = new_contacts, selectedContactPubkey = state.selectedContactPubkey, selectedContact = state.selectedContact)
                } else
                {
                    mutableStateFlow.value = state.copy(contacts = (state.contacts + item), selectedContactPubkey = state.selectedContactPubkey, selectedContact = state.selectedContact)
                }
                global_semaphore_contactlist_ui.release()
            //}
        }

        override fun update_ipaddr(pubkey: String, ipaddr: String)
        {
            global_semaphore_contactlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            var update_item: ContactItem? = null
            state.contacts.forEach {
                if (pubkey == it.pubkey)
                {
                    update_item = it.copy()
                }
            }
            if (update_item != null)
            {
                var need_update = false
                var new_contacts: ArrayList<ContactItem> = ArrayList()
                new_contacts.addAll(state.contacts)
                new_contacts.forEach { item2 ->
                    if (item2.pubkey == update_item!!.pubkey)
                    {
                        if (!item2.ip_addr.equals(ipaddr, ignoreCase = true))
                        {
                            item2.ip_addr = ipaddr
                            need_update = true
                        }
                    }
                }
                if (need_update)
                {
                    new_contacts = getFriendListWithGroupingAndSorting(new_contacts)
                    mutableStateFlow.value = state.copy(contacts = new_contacts, selectedContactPubkey = state.selectedContactPubkey, selectedContact = state.selectedContact)
                }
            }
            global_semaphore_contactlist_ui.release()
        }

        override fun clear()
        {
            //launch {
                global_semaphore_contactlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                mutableStateFlow.value = state.copy(contacts = emptyList(), selectedContactPubkey = null, selectedContact = null)
                global_semaphore_contactlist_ui.release()
            //}
        }

        override fun messagefilterActive(value: Boolean)
        {
            global_semaphore_contactlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            mutableStateFlow.value = state.copy(messageFilterActive = value )
            global_semaphore_contactlist_ui.release()
        }

        override fun messageresetFilter()
        {
            global_semaphore_contactlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            mutableStateFlow.value = state.copy(messageFilterString = "", messageFilterActive = false)
            global_semaphore_contactlist_ui.release()
        }

        override fun messagefilterString(value: String)
        {
            global_semaphore_contactlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            mutableStateFlow.value = state.copy(messageFilterString = value)
            global_semaphore_contactlist_ui.release()
        }
    }
}

val friendsRolesOrder = mapOf(0 to 3, 4 to 2, 1 to 1, 2 to 0)

fun getFriendListWithGroupingAndSorting(friendlist: ArrayList<ContactItem>)
        : ArrayList<ContactItem>
{
    return ArrayList(friendlist.sortedWith(
        compareBy<ContactItem> { it.is_relay }.
        thenBy<ContactItem> {
            // HINT: sort by connection status
            // if offline the also show contact with push_urls set above fully offline contacts
            var tmp_sort_value = it.isConnected
            if (tmp_sort_value == 0) {
                if (!it.is_relay) {
                    if (!it.push_url.isNullOrEmpty()) {
                        tmp_sort_value = 4
                    }
                }
            }
            friendsRolesOrder[tmp_sort_value]
        }.
        thenBy { it.name.lowercase() }
    )
    )
}

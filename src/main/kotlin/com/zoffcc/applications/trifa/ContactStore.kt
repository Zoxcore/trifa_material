package com.zoffcc.applications.trifa

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.contact.ContactItem

data class StateContacts(val contacts: List<ContactItem> = emptyList(), val selectedContactPubkey: String? = null, val selectedContact: ContactItem? = null)

const val TAG = "trifa.ContactsStore"

interface ContactStore
{
    fun add(item: ContactItem)
    fun remove(item: ContactItem)
    fun select(pubkey: String?)
    fun clear()
    fun update(item: ContactItem)
    val stateFlow: StateFlow<StateContacts>
    val state get() = stateFlow.value
}

fun CoroutineScope.createContactStore(): ContactStore
{
    val mutableStateFlow = MutableStateFlow(StateContacts())
    val channel: Channel<ContactItem> = Channel(Channel.UNLIMITED)

    return object : ContactStore
    {
        override val stateFlow: StateFlow<StateContacts> = mutableStateFlow

        init
        {
            launch {
                channel.consumeAsFlow().collect { item ->
                    mutableStateFlow.value = state.copy(contacts = (state.contacts + item))
                }
            }
        }

        override fun add(item: ContactItem)
        {
            launch {
                var found = false
                state.contacts.forEach {
                    if (item.pubkey == it.pubkey)
                    { // friend already in list
                        found = true
                    }
                }
                if (!found)
                {
                    channel.send(item)
                }
            }
        }

        override fun remove(item: ContactItem)
        {
            launch {
                var found = false
                state.contacts.forEach {
                    if (item.pubkey == it.pubkey)
                    {
                        mutableStateFlow.value = state.copy(contacts = (state.contacts - item))
                    }
                }
            }
        }

        override fun select(pubkey: String?)
        {
            launch {
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
            }
        }

        override fun update(item: ContactItem)
        {
            launch {
                var update_item: ContactItem? = null
                state.contacts.forEach {
                    if (item.pubkey == it.pubkey)
                    {
                        update_item = it.copy()
                    }
                }
                if (update_item != null)
                {
                    mutableStateFlow.value = state.copy(contacts = (state.contacts + item - update_item!!), selectedContactPubkey = state.selectedContactPubkey, selectedContact = state.selectedContact)
                } else
                {
                    mutableStateFlow.value = state.copy(contacts = (state.contacts + item), selectedContactPubkey = state.selectedContactPubkey, selectedContact = state.selectedContact)
                }
            }
        }

        override fun clear()
        {
            launch {
                mutableStateFlow.value = state.copy(contacts = emptyList(), selectedContactPubkey = null, selectedContact = null)
            }
        }
    }
}

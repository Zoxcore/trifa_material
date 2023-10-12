package com.zoffcc.applications.trifa

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.contact.ContactItem

data class StateContacts(
    val contacts: List<ContactItem> = emptyList()
)

const val TAG = "trifa.ContactsStore"

interface ContactStore {
    fun add(item: ContactItem)
    fun remove(item: ContactItem)
    fun clear()
    fun update(item: ContactItem)
    val stateFlow: StateFlow<StateContacts>
    val state get() = stateFlow.value
}

fun CoroutineScope.createContactStore(): ContactStore {
    val mutableStateFlow = MutableStateFlow(StateContacts())
    val channel: Channel<ContactItem> = Channel(Channel.UNLIMITED)

    return object : ContactStore {
        override val stateFlow: StateFlow<StateContacts> = mutableStateFlow

        init {
            launch {
                channel.consumeAsFlow().collect { item ->
                    mutableStateFlow.value =
                        state.copy(
                            contacts = (state.contacts + item)
                        )
                }
            }
        }

        override fun add(item: ContactItem) {
            launch {
                var found = false
                state.contacts.forEach {
                    if (item.pubkey == it.pubkey) {
                        // friend already in list
                        found = true
                    }
                }
                if (!found) {
                    channel.send(item)
                }
            }
        }

        override fun remove(item: ContactItem) {
            launch {
                var found = false
                state.contacts.forEach {
                    if (item.pubkey == it.pubkey) {
                        mutableStateFlow.value =
                            state.copy(
                                contacts = (state.contacts - item)
                            )
                    }
                }
            }
        }

        override fun update(item: ContactItem) {
            launch {
                var update_item: ContactItem? = null
                state.contacts.forEach {
                    if (item.pubkey == it.pubkey) {
                        update_item = it.copy()
                    }
                }
                if (update_item != null) {
                    mutableStateFlow.value =
                        state.copy(
                            contacts = (state.contacts + item - update_item!!)
                        )
                } else {
                    mutableStateFlow.value =
                        state.copy(
                            contacts = (state.contacts + item)
                        )
                }
            }
        }

        override fun clear() {
            launch {
                mutableStateFlow.value =
                    state.copy(
                        contacts = emptyList()
                    )
            }
        }
    }
}

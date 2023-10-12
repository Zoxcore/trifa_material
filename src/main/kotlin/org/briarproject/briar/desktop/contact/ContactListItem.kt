package org.briarproject.briar.desktop.contact

data class ContactListItem(
    val name: String,
    val isConnected: Boolean,
    val pubkey: String
)

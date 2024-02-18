package com.zoffcc.applications.trifa

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class StateFriendSettings(val visible: Boolean = false,
                              val selectedContactPubkey: String? = null
)

const val StateFriendSettingsTAG = "trifa.FriendSettingsStore"

interface FriendSettingsStore
{
    val stateFlow: StateFlow<StateFriendSettings>
    val state get() = stateFlow.value
    fun visible(value: Boolean)
}

fun CoroutineScope.createFriendSettingsStore(): FriendSettingsStore
{
    val mutableStateFlow = MutableStateFlow(StateFriendSettings())

    return object : FriendSettingsStore
    {
        override val stateFlow: StateFlow<StateFriendSettings> = mutableStateFlow
        override fun visible(value: Boolean)
        {
            mutableStateFlow.value = state.copy(visible = value)
        }
    }
}

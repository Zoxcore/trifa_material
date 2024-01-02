package com.zoffcc.applications.trifa

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class StateGroupSettings(val visible: Boolean = false,
                              val selectedGroupId: String? = null
)

const val StateGroupSettingsTAG = "trifa.GroupSettingsStore"

interface GroupSettingsStore
{
    val stateFlow: StateFlow<StateGroupSettings>
    val state get() = stateFlow.value
    fun visible(value: Boolean)
}

fun CoroutineScope.createGroupSettingsStore(): GroupSettingsStore
{
    val mutableStateFlow = MutableStateFlow(StateGroupSettings())

    return object : GroupSettingsStore
    {
        override val stateFlow: StateFlow<StateGroupSettings> = mutableStateFlow
        override fun visible(value: Boolean)
        {
            mutableStateFlow.value = state.copy(visible = value)
        }
    }
}

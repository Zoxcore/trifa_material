package com.zoffcc.applications.trifa

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

data class toxdata_state(
    val mytoxid: String = "" // own Tox ID as hex string uppercase
)

interface ToxDataStore {
    fun updateToxID(t: String)
    val stateFlow: StateFlow<toxdata_state>
    val state get() = stateFlow.value
}

fun CoroutineScope.createToxDataStore(): ToxDataStore {
    val mutableStateFlow = MutableStateFlow(toxdata_state())
    val channeltoxid: Channel<String> = Channel(Channel.UNLIMITED)

    return object : ToxDataStore {
        override val stateFlow: StateFlow<toxdata_state> = mutableStateFlow

        override fun updateToxID(p: String) {
            launch {
                channeltoxid.send(p)
            }
        }

        init {
            launch {
                channeltoxid.consumeAsFlow().collect { item ->
                    mutableStateFlow.value =
                        state.copy(
                            mytoxid = item
                        )
                }
            }
        }
    }
}

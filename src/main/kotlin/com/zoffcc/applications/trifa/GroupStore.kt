package com.zoffcc.applications.trifa

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.contact.GroupItem

data class StateGroups(val groups: List<GroupItem> = emptyList(), val selectedGroupId: String? = null, val selectedGroup: GroupItem? = null)

interface GroupStore
{
    fun add(item: GroupItem)
    fun remove(item: GroupItem)
    fun select(pubkey: String?)
    fun clear()
    fun update(item: GroupItem)
    val stateFlow: StateFlow<StateGroups>
    val state get() = stateFlow.value
}

fun CoroutineScope.createGroupStore(): GroupStore
{
    val TAG = "trifa.GroupStore"

    val mutableStateFlow = MutableStateFlow(StateGroups())
    val channel_group_list: Channel<GroupItem> = Channel(Channel.UNLIMITED)

    return object : GroupStore
    {
        override val stateFlow: StateFlow<StateGroups> = mutableStateFlow

        init
        {
            launch {
                channel_group_list.consumeAsFlow().collect { item ->
                    mutableStateFlow.value = state.copy(groups = (state.groups + item))
                }
            }
        }

        override fun add(item: GroupItem)
        {
            launch {
                var found = false
                state.groups.forEach {
                    if (item.groupId == it.groupId)
                    { // group already in list
                        found = true
                    }
                }
                if (!found)
                {
                    channel_group_list.send(item)
                }
            }
        }

        override fun remove(item: GroupItem)
        {
            launch {
                var found = false
                state.groups.forEach {
                    if (item.groupId == it.groupId)
                    {
                        mutableStateFlow.value = state.copy(groups = (state.groups - item))
                    }
                }
            }
        }

        override fun select(groupId: String?)
        {
            launch {
                var wanted_group_item: GroupItem? = null
                state.groups.forEach {
                    if (groupId == it.groupId)
                    {
                        wanted_group_item = it
                    }
                }
                var used_groupid = groupId
                if (wanted_group_item == null)
                {
                    used_groupid = null
                }
                mutableStateFlow.value = state.copy(groups = state.groups, selectedGroupId = used_groupid, selectedGroup = wanted_group_item)
            }
        }

        override fun update(item: GroupItem)
        {
            launch {
                var update_item: GroupItem? = null
                state.groups.forEach {
                    if (item.groupId == it.groupId)
                    {
                        update_item = it.copy()
                    }
                }
                if (update_item != null)
                {
                    mutableStateFlow.value = state.copy(groups = (state.groups + item - update_item!!), selectedGroupId = state.selectedGroupId, selectedGroup = state.selectedGroup)
                } else
                {
                    mutableStateFlow.value = state.copy(groups = (state.groups + item), selectedGroupId = state.selectedGroupId, selectedGroup = state.selectedGroup)
                }
            }
        }

        override fun clear()
        {
            launch {
                mutableStateFlow.value = state.copy(groups = emptyList(), selectedGroupId = null, selectedGroup = null)
            }
        }
    }
}

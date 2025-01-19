@file:Suppress("FunctionName", "LiftReturnOrAssignment", "SpellCheckingInspection")

package com.zoffcc.applications.trifa

import global_semaphore_grouplist_ui
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.briarproject.briar.desktop.contact.GroupItem

data class StateGroups(val groups: List<GroupItem> = emptyList(), val visible: Boolean = false,
                       val selectedGroupId: String? = null, val selectedGroup: GroupItem? = null,
                       var groupmessageFilterActive: Boolean = false, var groupmessageFilterString: String = "")

interface GroupStore
{
    fun add(item: GroupItem)
    fun remove(item: GroupItem)
    fun select(pubkey: String?)
    fun visible(value: Boolean)
    fun clear()
    fun groupmessagefilterActive(value: Boolean)
    fun groupmessagefilterString(value: String)
    fun groupmessageresetFilter()
    fun update(item: GroupItem)
    val stateFlow: StateFlow<StateGroups>
    val state get() = stateFlow.value
}

fun SqliteEscapeLikeString(filterstring_raw: String): String
{
    try
    {
        // HINT:
        // It is standard SQL that in LIKE expressions:
        //
        // % matches any sequence of characters, including an empty one. It is equivalent to .* in a regular expression.
        // _ matches a single character. It is equivalent to . in a regular expression.
        //
        return filterstring_raw.replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
    } catch (_: Exception)
    {
        return filterstring_raw
    }
}

fun CoroutineScope.createGroupStore(): GroupStore
{
    val TAG = "trifa.GroupStore"

    val mutableStateFlow = MutableStateFlow(StateGroups())
    // val channel_group_list: Channel<GroupItem> = Channel(Channel.UNLIMITED)

    return object : GroupStore
    {
        override val stateFlow: StateFlow<StateGroups> = mutableStateFlow

        init
        {
            //launch {
            //    channel_group_list.consumeAsFlow().collect { item ->
            //        mutableStateFlow.value = state.copy(groups = (state.groups + item))
            //    }
            //}
        }

        override fun add(item: GroupItem)
        {
            //launch {
                global_semaphore_grouplist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                var found = false
                state.groups.forEach {
                    if (item.groupId == it.groupId)
                    { // group already in list
                        found = true
                    }
                }
                if (!found)
                {
                    var new_groups: ArrayList<GroupItem> = ArrayList()
                    new_groups.addAll(state.groups)
                    new_groups.forEach { item2 ->
                        if (item2.groupId == item.groupId)
                        {
                            new_groups.remove(item2)
                        }
                    }
                    new_groups.add(item)
                    new_groups.sortBy { it.groupId }
                    mutableStateFlow.value = state.copy(groups = new_groups)
                }
                global_semaphore_grouplist_ui.release()
            //}
        }

        override fun remove(item: GroupItem)
        {
            //launch {
                global_semaphore_grouplist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                var sel_groupid = state.selectedGroupId
                var sel_item = state.selectedGroup
                var new_groups: ArrayList<GroupItem> = ArrayList()
                new_groups.addAll(state.groups)
                var to_remove_item: GroupItem? = null
                state.groups.forEach {
                    if (item.groupId == it.groupId)
                    {
                        if (state.selectedGroupId == it.groupId)
                        {
                            sel_groupid = null
                            sel_item = null
                        }
                        new_groups.forEach { item2 ->
                            if (item2.groupId == it.groupId)
                            {
                                to_remove_item = item2
                            }
                        }
                    }
                }
                if (to_remove_item != null)
                {
                    new_groups.remove(to_remove_item)
                }
                new_groups.sortBy { it.groupId }
                mutableStateFlow.value = state.copy(groups = new_groups,
                    selectedGroup = sel_item, selectedGroupId = sel_groupid)
                global_semaphore_grouplist_ui.release()
            //}
        }

        override fun select(groupId: String?)
        {
            //launch {
                global_semaphore_grouplist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
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
                global_semaphore_grouplist_ui.release()
            //}
        }

        override fun visible(value: Boolean)
        {
            global_semaphore_grouplist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            mutableStateFlow.value = state.copy(visible = value)
            global_semaphore_grouplist_ui.release()
        }

        override fun update(item: GroupItem)
        {
            //launch {
                global_semaphore_grouplist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                var update_item: GroupItem? = null
                state.groups.forEach {
                    if (item.groupId == it.groupId)
                    {
                        update_item = it.copy()
                    }
                }
                if (update_item != null)
                {
                    var new_groups: ArrayList<GroupItem> = ArrayList()
                    new_groups.addAll(state.groups)
                    var to_remove_item: GroupItem? = null
                    new_groups.forEach { item2 ->
                        if (item2.groupId == update_item!!.groupId)
                        {
                            to_remove_item = item2
                        }
                    }
                    if (to_remove_item != null)
                    {
                        new_groups.remove(to_remove_item)
                    }
                    new_groups.add(item)
                    new_groups.sortBy { it.groupId }
                    mutableStateFlow.value = state.copy(groups = new_groups, selectedGroupId = state.selectedGroupId, selectedGroup = state.selectedGroup)
                } else
                {
                    mutableStateFlow.value = state.copy(groups = (state.groups + item), selectedGroupId = state.selectedGroupId, selectedGroup = state.selectedGroup)
                }
                global_semaphore_grouplist_ui.release()
            //}
        }

        override fun clear()
        {
            //launch {
                global_semaphore_grouplist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                mutableStateFlow.value = state.copy(groups = emptyList(), selectedGroupId = null, selectedGroup = null)
                global_semaphore_grouplist_ui.release()
            //}
        }

        override fun groupmessagefilterActive(value: Boolean)
        {
            global_semaphore_grouplist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            mutableStateFlow.value = state.copy(groupmessageFilterActive = value )
            global_semaphore_grouplist_ui.release()
        }

        override fun groupmessageresetFilter()
        {
            global_semaphore_grouplist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            mutableStateFlow.value = state.copy(groupmessageFilterString = "", groupmessageFilterActive = false)
            global_semaphore_grouplist_ui.release()
        }

        override fun groupmessagefilterString(value: String)
        {
            global_semaphore_grouplist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            mutableStateFlow.value = state.copy(groupmessageFilterString = value)
            global_semaphore_grouplist_ui.release()
        }
    }
}

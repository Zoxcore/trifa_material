@file:Suppress("FunctionName", "SpellCheckingInspection", "LocalVariableName")

package com.zoffcc.applications.trifa

import androidx.compose.ui.text.toLowerCase
import global_semaphore_grouppeerlist_ui
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.briarproject.briar.desktop.contact.GroupPeerItem

data class StateGroupPeers(val grouppeers: List<GroupPeerItem> = emptyList(),
                           val selectedGrouppeerPubkey: String? = null,
                           val selectedGrouppeer: GroupPeerItem? = null
)

const val StateGroupPeersTAG = "trifa.GroupPeerStore"

interface GroupPeerStore
{
    fun add(item: GroupPeerItem)
    fun remove(item: GroupPeerItem)
    fun select(pubkey: String?)
    fun clear()
    fun update(item: GroupPeerItem)
    fun update_ipaddr(groupID: String, pubkey: String, ipaddr: String)
    val stateFlow: StateFlow<StateGroupPeers>
    val state get() = stateFlow.value
}

fun CoroutineScope.createGroupPeerStore(): GroupPeerStore
{
    val mutableStateFlow = MutableStateFlow(StateGroupPeers())

    return object : GroupPeerStore
    {
        override val stateFlow: StateFlow<StateGroupPeers> = mutableStateFlow

        override fun add(item: GroupPeerItem)
        {
            //launch {
                @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                global_semaphore_grouppeerlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                var found = false
                state.grouppeers.forEach {
                    if (item.pubkey == it.pubkey)
                    { // friend already in list
                        found = true
                    }
                }
                if (!found)
                {
                    var new_peers: ArrayList<GroupPeerItem> = ArrayList()
                    new_peers.addAll(state.grouppeers)
                    new_peers.forEach { item2 ->
                        if (item2.pubkey == item.pubkey)
                        {
                            new_peers.remove(item2)
                        }
                    }
                    new_peers.add(item)
                    val self_group_pubkey = MainActivity.tox_group_self_get_public_key(
                        HelperGroup.tox_group_by_groupid__wrapper(item.groupID.lowercase()))
                    new_peers = getListWithGroupingAndSorting(new_peers, self_group_pubkey)
                    mutableStateFlow.value = state.copy(grouppeers = new_peers)
                }
                global_semaphore_grouppeerlist_ui.release()
            //}
        }

        override fun remove(item: GroupPeerItem)
        {
            //launch {
                @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                global_semaphore_grouppeerlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                var sel_pubkey = state.selectedGrouppeerPubkey
                var sel_item = state.selectedGrouppeer
                var new_peers: ArrayList<GroupPeerItem> = ArrayList()
                new_peers.addAll(state.grouppeers)
                var to_remove_item: GroupPeerItem? = null
                state.grouppeers.forEach {
                    if (item.pubkey == it.pubkey)
                    {
                        if (state.selectedGrouppeerPubkey == it.pubkey)
                        {
                            sel_pubkey = null
                            sel_item = null
                        }
                        new_peers.forEach { item2 ->
                            if (item2.pubkey == it.pubkey)
                            {
                                to_remove_item = item2
                            }
                        }
                    }
                }
                if (to_remove_item != null)
                {
                    new_peers.remove(to_remove_item)
                }
                val self_group_pubkey = MainActivity.tox_group_self_get_public_key(
                    HelperGroup.tox_group_by_groupid__wrapper(item.groupID.lowercase()))
                new_peers = getListWithGroupingAndSorting(new_peers, self_group_pubkey)
                mutableStateFlow.value = state.copy(grouppeers = new_peers,
                    selectedGrouppeer = sel_item, selectedGrouppeerPubkey = sel_pubkey)
                global_semaphore_grouppeerlist_ui.release()
            //}
        }

        override fun select(pubkey: String?)
        {
            //launch {
            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            global_semaphore_grouppeerlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            var wanted_contact_item: GroupPeerItem? = null
                state.grouppeers.forEach {
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
                mutableStateFlow.value = state.copy(grouppeers = state.grouppeers,
                    selectedGrouppeerPubkey = used_pubkey, selectedGrouppeer = wanted_contact_item)
                global_semaphore_grouppeerlist_ui.release()
            //}
        }

        override fun update(item: GroupPeerItem)
        {
            //launch {
                @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                global_semaphore_grouppeerlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                var update_item: GroupPeerItem? = null
                state.grouppeers.forEach {
                    if (item.pubkey == it.pubkey)
                    {
                        update_item = it.copy()
                    }
                }
                if (update_item != null)
                {
                    var new_peers: ArrayList<GroupPeerItem> = ArrayList()
                    new_peers.addAll(state.grouppeers)
                    var to_remove_item: GroupPeerItem? = null
                    new_peers.forEach { item2 ->
                        if (item2.pubkey == update_item!!.pubkey)
                        {
                            to_remove_item = item2
                        }
                    }
                    if (to_remove_item != null)
                    {
                        new_peers.remove(to_remove_item)
                    }
                    new_peers.add(item)
                    val self_group_pubkey = MainActivity.tox_group_self_get_public_key(
                        HelperGroup.tox_group_by_groupid__wrapper(item.groupID.lowercase()))
                    new_peers = getListWithGroupingAndSorting(new_peers, self_group_pubkey)
                    mutableStateFlow.value = state.copy(grouppeers = new_peers,
                        selectedGrouppeerPubkey = state.selectedGrouppeerPubkey,
                        selectedGrouppeer = state.selectedGrouppeer)
                } else
                {
                    mutableStateFlow.value = state.copy(grouppeers = (state.grouppeers + item),
                        selectedGrouppeerPubkey = state.selectedGrouppeerPubkey,
                        selectedGrouppeer = state.selectedGrouppeer)
                }
                global_semaphore_grouppeerlist_ui.release()
            //}
        }

        override fun update_ipaddr(groupID: String, pubkey: String, ipaddr: String)
        {
            //launch {
            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            global_semaphore_grouppeerlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
            var update_item: GroupPeerItem? = null
            state.grouppeers.forEach {
                if (pubkey == it.pubkey)
                {
                    update_item = it.copy()
                }
            }
            if (update_item != null)
            {
                var need_update = false
                var new_peers: ArrayList<GroupPeerItem> = ArrayList()
                new_peers.addAll(state.grouppeers)
                // var to_remove_item: GroupPeerItem? = null
                new_peers.forEach { item2 ->
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
                    val self_group_pubkey = MainActivity.tox_group_self_get_public_key(
                        HelperGroup.tox_group_by_groupid__wrapper(groupID.lowercase()))
                    new_peers = getListWithGroupingAndSorting(new_peers, self_group_pubkey)
                    mutableStateFlow.value = state.copy(grouppeers = new_peers,
                        selectedGrouppeerPubkey = state.selectedGrouppeerPubkey,
                        selectedGrouppeer = state.selectedGrouppeer)
                }
            }
            global_semaphore_grouppeerlist_ui.release()
            //}
        }

        override fun clear()
        {
            //launch {
                @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                global_semaphore_grouppeerlist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                mutableStateFlow.value = state.copy(grouppeers = emptyList(),
                    selectedGrouppeerPubkey = null, selectedGrouppeer = null)
                global_semaphore_grouppeerlist_ui.release()
            //}
        }
    }
}

val rolesOrder = mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3)

fun getListWithGroupingAndSorting(peerlist: ArrayList<GroupPeerItem>, self_group_pubkey: String?)
    : ArrayList<GroupPeerItem>
{
    val selfOrder = mapOf(true to 0, false to 1)
    return ArrayList(peerlist.sortedWith(
        compareBy<GroupPeerItem> { selfOrder[it.pubkey == self_group_pubkey] }.
        thenBy { rolesOrder[it.peerRole] }.
        thenBy { it.name.lowercase() }
    )
    )
}
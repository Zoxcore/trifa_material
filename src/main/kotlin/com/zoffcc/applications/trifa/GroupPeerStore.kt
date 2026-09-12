@file:Suppress("FunctionName", "SpellCheckingInspection", "LocalVariableName")

package com.zoffcc.applications.trifa

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.briarproject.briar.desktop.contact.GroupPeerItem

data class StateGroupPeers(
    val grouppeers: List<GroupPeerItem> = emptyList(),
    val selectedGrouppeerPubkey: String? = null,
    val selectedGrouppeer: GroupPeerItem? = null
)

const val StateGroupPeersTAG = "trifa.GroupPeerStore"

interface GroupPeerStore
{
    fun add(item: GroupPeerItem)
    fun remove(item: GroupPeerItem)
    fun select(pubkey: String?)
    fun replaceForGroup(groupID: String, items: List<GroupPeerItem>)
    fun clear()
    fun update(item: GroupPeerItem)
    fun update_ipaddr(groupID: String, pubkey: String, ipaddr: String)

    val stateFlow: StateFlow<StateGroupPeers>
    val state get() = stateFlow.value
}

fun CoroutineScope.createGroupPeerStore(): GroupPeerStore
{
    val mutableStateFlow = MutableStateFlow(StateGroupPeers())

    //
    // Private lock for this store only.
    // Much better than one global semaphore.
    //
    val mutationLock = Any()

    //
    // Cache self pubkey per group.
    // This avoids repeated native/JNI calls during frequent UI updates.
    //
    val selfPubkeyCache = HashMap<String, String?>()

    fun getSelfPubkeyLocked(groupIDLower: String): String?
    {
        // Must be called while holding mutationLock.
        return selfPubkeyCache.getOrPut(groupIDLower) {
            MainActivity.tox_group_self_get_public_key(
                HelperGroup.tox_group_by_groupid__wrapper(groupIDLower)
            )
        }
    }

    fun publish(newState: StateGroupPeers)
    {
        // StateFlow already avoids emission if newState.equals(oldState),
        // but this makes the intent explicit.
        if (mutableStateFlow.value != newState)
        {
            mutableStateFlow.value = newState
        }
    }

    return object : GroupPeerStore
    {
        override val stateFlow: StateFlow<StateGroupPeers> = mutableStateFlow

        override fun replaceForGroup(groupID: String, items: List<GroupPeerItem>)
        {
            val groupIDLower = groupID.lowercase()

            synchronized(mutationLock)
            {
                val currentState = mutableStateFlow.value
                val selfGroupPubkey = getSelfPubkeyLocked(groupIDLower)

                var selPubkey = currentState.selectedGrouppeerPubkey
                var selItem = currentState.selectedGrouppeer

                if (selItem != null && selItem.groupID.lowercase() == groupIDLower)
                {
                    val stillExists = items.any { it.pubkey == selPubkey }

                    if (!stillExists)
                    {
                        selPubkey = null
                        selItem = null
                    }
                    else
                    {
                        selItem = items.firstOrNull { it.pubkey == selPubkey }
                    }
                }

                val sortedPeers = getListWithGroupingAndSorting(
                    ArrayList(items),
                    selfGroupPubkey
                )

                publish(
                    currentState.copy(
                        grouppeers = sortedPeers,
                        selectedGrouppeerPubkey = selPubkey,
                        selectedGrouppeer = selItem
                    )
                )
            }
        }

        override fun add(item: GroupPeerItem)
        {
            synchronized(mutationLock)
            {
                val currentState = mutableStateFlow.value

                val alreadyExists = currentState.grouppeers.any {
                    it.pubkey == item.pubkey
                }

                if (alreadyExists)
                {
                    return@synchronized
                }

                val selfGroupPubkey = getSelfPubkeyLocked(item.groupID.lowercase())

                val newPeers = ArrayList(currentState.grouppeers)
                newPeers.add(item)

                val sortedPeers = getListWithGroupingAndSorting(
                    newPeers,
                    selfGroupPubkey
                )

                publish(
                    currentState.copy(
                        grouppeers = sortedPeers
                    )
                )
            }
        }

        override fun remove(item: GroupPeerItem)
        {
            synchronized(mutationLock)
            {
                val currentState = mutableStateFlow.value

                val selfGroupPubkey = getSelfPubkeyLocked(item.groupID.lowercase())

                var selPubkey = currentState.selectedGrouppeerPubkey
                var selItem = currentState.selectedGrouppeer

                if (selPubkey == item.pubkey)
                {
                    selPubkey = null
                    selItem = null
                }

                val newPeers = ArrayList(
                    currentState.grouppeers.filterNot {
                        it.pubkey == item.pubkey
                    }
                )

                val sortedPeers = getListWithGroupingAndSorting(
                    newPeers,
                    selfGroupPubkey
                )

                publish(
                    currentState.copy(
                        grouppeers = sortedPeers,
                        selectedGrouppeerPubkey = selPubkey,
                        selectedGrouppeer = selItem
                    )
                )
            }
        }

        override fun select(pubkey: String?)
        {
            synchronized(mutationLock)
            {
                val currentState = mutableStateFlow.value

                val selectedItem = currentState.grouppeers.firstOrNull {
                    it.pubkey == pubkey
                }

                val usedPubkey = if (selectedItem != null) pubkey else null

                publish(
                    currentState.copy(
                        selectedGrouppeerPubkey = usedPubkey,
                        selectedGrouppeer = selectedItem
                    )
                )
            }
        }

        override fun update(item: GroupPeerItem)
        {
            synchronized(mutationLock)
            {
                val currentState = mutableStateFlow.value

                val selfGroupPubkey = getSelfPubkeyLocked(item.groupID.lowercase())

                val exists = currentState.grouppeers.any {
                    it.pubkey == item.pubkey
                }

                val newPeers = if (exists)
                {
                    currentState.grouppeers.map {
                        if (it.pubkey == item.pubkey)
                        {
                            item
                        }
                        else
                        {
                            it
                        }
                    }
                }
                else
                {
                    currentState.grouppeers + item
                }

                val sortedPeers = getListWithGroupingAndSorting(
                    ArrayList(newPeers),
                    selfGroupPubkey
                )

                val selectedPubkey = currentState.selectedGrouppeerPubkey

                val selectedItem = if (selectedPubkey == item.pubkey)
                {
                    item
                }
                else
                {
                    currentState.selectedGrouppeer
                }

                publish(
                    currentState.copy(
                        grouppeers = sortedPeers,
                        selectedGrouppeerPubkey = selectedPubkey,
                        selectedGrouppeer = selectedItem
                    )
                )
            }
        }

        override fun update_ipaddr(groupID: String, pubkey: String, ipaddr: String)
        {
            synchronized(mutationLock)
            {
                val currentState = mutableStateFlow.value

                var needUpdate = false

                //
                // IMPORTANT:
                // Do not mutate peer.ip_addr in place.
                // Use copy() so StateFlow/Compose can see a new immutable item.
                //
                val newPeers = currentState.grouppeers.map { peer ->
                    if (peer.pubkey == pubkey && !peer.ip_addr.equals(ipaddr, ignoreCase = true))
                    {
                        needUpdate = true
                        peer.copy(ip_addr = ipaddr)
                    }
                    else
                    {
                        peer
                    }
                }

                if (!needUpdate)
                {
                    return@synchronized
                }

                val selfGroupPubkey = getSelfPubkeyLocked(groupID.lowercase())

                val sortedPeers = getListWithGroupingAndSorting(
                    ArrayList(newPeers),
                    selfGroupPubkey
                )

                val selectedPubkey = currentState.selectedGrouppeerPubkey

                val selectedItem = if (selectedPubkey == pubkey)
                {
                    newPeers.firstOrNull { it.pubkey == pubkey }
                }
                else
                {
                    currentState.selectedGrouppeer
                }

                publish(
                    currentState.copy(
                        grouppeers = sortedPeers,
                        selectedGrouppeerPubkey = selectedPubkey,
                        selectedGrouppeer = selectedItem
                    )
                )
            }
        }

        override fun clear()
        {
            synchronized(mutationLock)
            {
                publish(StateGroupPeers())
            }
        }
    }
}

val rolesOrder = mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3)

fun getListWithGroupingAndSorting(
    peerlist: ArrayList<GroupPeerItem>,
    self_group_pubkey: String?
): ArrayList<GroupPeerItem>
{
    val selfOrder = mapOf(true to 0, false to 1)
    val onlineOrder = mapOf(true to 0, false to 1)

    return ArrayList(
        peerlist.sortedWith(
            compareBy<GroupPeerItem> {
                selfOrder.getValue(it.pubkey == self_group_pubkey)
            }
                .thenBy {
                    onlineOrder.getValue(it.connectionStatus != ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE.value)
                }
                .thenBy {
                    rolesOrder[it.peerRole] ?: ToxVars.Tox_Group_Role.TOX_GROUP_ROLE_USER.value
                }
                .thenBy {
                    it.name?.lowercase() ?: ""
                }
                .thenBy {
                    //
                    // Extra stable tie-breaker.
                    // Prevents random jumping when two peers have the same name.
                    //
                    it.pubkey
                }
        )
    )
}

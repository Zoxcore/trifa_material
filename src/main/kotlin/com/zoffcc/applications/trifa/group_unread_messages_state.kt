package com.zoffcc.applications.trifa

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import kotlin.collections.HashMap

data class groupstore_unread_messages_state(
    val unread_per_group_message_count: HashMap<String, Int> = HashMap(),
)

interface GroupstoreUnreadMessages {
    fun increase_unread_per_group_message_count(groupid: String)
    fun get_unread_per_group_message_count(groupid: String): Int
    fun try_clear_unread_per_group_message_count(groupid: String?)
    fun hard_clear_unread_per_group_message_count(groupid: String)
    val stateFlow: StateFlow<groupstore_unread_messages_state>
    val state get() = stateFlow.value
}

@OptIn(DelicateCoroutinesApi::class)
fun CoroutineScope.createGroupstoreUnreadMessages(): GroupstoreUnreadMessages {
    val mutableStateFlow = MutableStateFlow(groupstore_unread_messages_state())
    return object : GroupstoreUnreadMessages
    {
        override val stateFlow: StateFlow<groupstore_unread_messages_state> = mutableStateFlow

        override fun increase_unread_per_group_message_count(groupid: String)
        {
            val tmp = HashMap(state.unread_per_group_message_count)
            val old_value_ = tmp.get(groupid)
            val old_value: Int = if (old_value_ == null) 0 else old_value_
            tmp.put(groupid, old_value + 1)
            mutableStateFlow.value = state.copy(unread_per_group_message_count = tmp)
        }

        override fun get_unread_per_group_message_count(groupid: String): Int
        {
            val tmp = state.unread_per_group_message_count.get(groupid)
            if (tmp == null)
            {
                return 0
            }
            else
            {
                return tmp
            }
        }

        override fun hard_clear_unread_per_group_message_count(groupid: String)
        {
            val tmp = HashMap(state.unread_per_group_message_count)
            tmp.remove(groupid)
            mutableStateFlow.value = state.copy(unread_per_group_message_count = tmp)
        }

        override fun try_clear_unread_per_group_message_count(groupid: String?)
        {
            var unread_count = 0
            try
            {
                unread_count = TrifaToxService.orma!!.selectFromGroupMessage()
                    .group_identifierEq(groupid!!.lowercase())
                    .directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value)
                    .is_newEq(true).count()
                if (unread_count != 0)
                {
                    val tmp = HashMap(state.unread_per_group_message_count)
                    tmp.put(groupid, unread_count)
                    mutableStateFlow.value = state.copy(unread_per_group_message_count = tmp)
                    return
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            val tmp = HashMap(state.unread_per_group_message_count)
            tmp.remove(groupid)
            mutableStateFlow.value = state.copy(unread_per_group_message_count = tmp)
        }
    }
}

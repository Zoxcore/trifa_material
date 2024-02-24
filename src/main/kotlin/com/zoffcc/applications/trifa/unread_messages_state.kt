package com.zoffcc.applications.trifa

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import kotlin.collections.HashMap

data class unread_messages_state(
    val unread_per_friend_message_count: HashMap<String, Int> = HashMap(),
)

interface UnreadMessages {
    fun increase_unread_per_friend_message_count(friend_pubkey: String)
    fun get_unread_per_friend_message_count(friend_pubkey: String): Int
    fun try_clear_unread_per_friend_message_count(friend_pubkey: String?)
    fun hard_clear_unread_per_friend_message_count(friend_pubkey: String)
    val stateFlow: StateFlow<unread_messages_state>
    val state get() = stateFlow.value
}

@OptIn(DelicateCoroutinesApi::class)
fun CoroutineScope.createUnreadMessages(): UnreadMessages {
    val mutableStateFlow = MutableStateFlow(unread_messages_state())
    return object : UnreadMessages
    {
        override val stateFlow: StateFlow<unread_messages_state> = mutableStateFlow

        override fun increase_unread_per_friend_message_count(friend_pubkey: String)
        {
            val tmp = HashMap(state.unread_per_friend_message_count)
            val old_value_ = tmp.get(friend_pubkey)
            val old_value: Int = if (old_value_ == null) 0 else old_value_
            tmp.put(friend_pubkey, old_value + 1)
            mutableStateFlow.value = state.copy(unread_per_friend_message_count = tmp)
        }

        override fun get_unread_per_friend_message_count(friend_pubkey: String): Int
        {
            val tmp = state.unread_per_friend_message_count.get(friend_pubkey)
            if (tmp == null)
            {
                return 0
            }
            else
            {
                return tmp
            }
        }

        override fun hard_clear_unread_per_friend_message_count(friend_pubkey: String)
        {
            val tmp = HashMap(state.unread_per_friend_message_count)
            tmp.remove(friend_pubkey)
            mutableStateFlow.value = state.copy(unread_per_friend_message_count = tmp)
        }

        override fun try_clear_unread_per_friend_message_count(friend_pubkey: String?)
        {
            var unread_count = 0
            try
            {
                unread_count = TrifaToxService.orma!!.selectFromMessage()
                    .tox_friendpubkeyEq(friend_pubkey!!.uppercase())
                    .directionEq(TRIFAGlobals.TRIFA_MSG_DIRECTION.TRIFA_MSG_DIRECTION_RECVD.value)
                    .is_newEq(true).count()
                if (unread_count != 0)
                {
                    val tmp = HashMap(state.unread_per_friend_message_count)
                    tmp.put(friend_pubkey, unread_count)
                    mutableStateFlow.value = state.copy(unread_per_friend_message_count = tmp)
                    return
                }
            }
            catch (e: Exception)
            {
                // e.printStackTrace()
            }
            val tmp = HashMap(state.unread_per_friend_message_count)
            tmp.remove(friend_pubkey)
            mutableStateFlow.value = state.copy(unread_per_friend_message_count = tmp)
        }
    }
}

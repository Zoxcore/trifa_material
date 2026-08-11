import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.zoffcc.applications.trifa.Log

sealed interface GroupMessageAction {
    data class SendMessagesBulk(val messages: List<UIGroupMessage>, val groupid: String) : GroupMessageAction
    data class SendGroupMessage(val groupmessage: UIGroupMessage) : GroupMessageAction
    data class ReceiveMessagesBulkWithClear(val messages: List<UIGroupMessage>, val groupid: String) : GroupMessageAction
    data class ReceiveGroupMessage(val groupmessage: UIGroupMessage) : GroupMessageAction
    data class ClearGroup(val groupmessage: Int) : GroupMessageAction
    data class DeleteGroupMessage(val messageId: Long) : GroupMessageAction
}

data class GroupMessageState(
    val groupmessages: SnapshotStateList<UIGroupMessage> = mutableStateListOf()
)

const val maxGroupMessages = MAX_GROUP_MESSAGES_TO_SHOW

fun groupchatReducer(state: GroupMessageState, action: GroupMessageAction): GroupMessageState {
    Snapshot.withMutableSnapshot {
        val messages = state.groupmessages

        when (action) {
            is GroupMessageAction.ReceiveMessagesBulkWithClear -> {
                messages.clear()
                messages.addAll(action.messages)
            }
            is GroupMessageAction.SendMessagesBulk -> {
                messages.addAll(action.messages)
            }
            is GroupMessageAction.SendGroupMessage -> {
                messages.add(action.groupmessage)
            }
            is GroupMessageAction.ReceiveGroupMessage -> {
                messages.add(action.groupmessage)
            }
            is GroupMessageAction.ClearGroup -> {
                messages.clear()
            }
            is GroupMessageAction.DeleteGroupMessage -> {
                messages.removeIf { it.id == action.messageId }
            }
            else ->
            {
                Log.i(com.zoffcc.applications.trifa.TAG, "GroupMessageAction.Default -> Clear (should never get here)")
                messages.clear()
            }
        }
        if (!groupstore.state.fullHistoryActive)
        {
            // Global trimming logic: Keeps the code clean and handles any action size safely
            val excess = messages.size - maxGroupMessages
            if (excess > 0)
            {
                messages.removeRange(0, excess)
            }
        }
    }
    return state
}

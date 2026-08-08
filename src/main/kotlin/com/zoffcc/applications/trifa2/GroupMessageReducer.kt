import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

sealed interface GroupMessageAction {
    data class SendMessagesBulk(val messages: List<UIGroupMessage>, val groupid: String) : GroupMessageAction
    data class SendGroupMessage(val groupmessage: UIGroupMessage) : GroupMessageAction
    data class ReceiveMessagesBulkWithClear(val messages: List<UIGroupMessage>, val groupid: String) : GroupMessageAction
    data class ReceiveGroupMessage(val groupmessage: UIGroupMessage) : GroupMessageAction
    data class ClearGroup(val groupmessage: Int) : GroupMessageAction
}

data class GroupMessageState(
    val groupmessages: SnapshotStateList<UIGroupMessage> = mutableStateListOf()
)

const val maxGroupMessages = MAX_GROUP_MESSAGES_TO_SHOW

fun groupchatReducer(state: GroupMessageState, action: GroupMessageAction): GroupMessageState {
    Snapshot.withMutableSnapshot {
        when (action) {
            is GroupMessageAction.ReceiveMessagesBulkWithClear -> {
                state.groupmessages.clear()
                state.groupmessages.addAll(action.messages)
            }
            is GroupMessageAction.SendMessagesBulk -> {
                state.groupmessages.addAll(action.messages)
            }
            is GroupMessageAction.SendGroupMessage -> {
                state.groupmessages.add(action.groupmessage)
            }
            is GroupMessageAction.ReceiveGroupMessage -> {
                state.groupmessages.add(action.groupmessage)
            }
            is GroupMessageAction.ClearGroup -> {
                state.groupmessages.clear()
            }
        }
        if (!groupstore.state.fullHistoryActive)
        {
            // Global trimming logic: Keeps the code clean and handles any action size safely
            val excess = state.groupmessages.size - maxGroupMessages
            if (excess > 0)
            {
                state.groupmessages.removeRange(0, excess)
            }
        }
    }
    return state
}

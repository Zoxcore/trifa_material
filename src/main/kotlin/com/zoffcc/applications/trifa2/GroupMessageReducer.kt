import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

sealed interface GroupMessageAction
{
    data class SendMessagesBulk(val messages: List<UIGroupMessage>, val groupid: String) : GroupMessageAction
    data class SendGroupMessage(val groupmessage: UIGroupMessage) : GroupMessageAction
    data class ReceiveMessagesBulkWithClear(val messages: List<UIGroupMessage>, val groupid: String) : GroupMessageAction
    data class ReceiveGroupMessage(val groupmessage: UIGroupMessage) : GroupMessageAction
    data class ClearGroup(val groupmessage: Int) : GroupMessageAction
}

data class GroupMessageState(val groupmessages: SnapshotStateList<UIGroupMessage> = mutableStateListOf())

const val maxGroupMessages = MAX_GROUP_MESSAGES_TO_SHOW
fun groupchatReducer(state: GroupMessageState, action: GroupMessageAction): GroupMessageState = when (action)
{
    is GroupMessageAction.ReceiveMessagesBulkWithClear ->
    {
        state.copy(groupmessages = (action.messages).toMutableStateList())
    }
    is GroupMessageAction.SendMessagesBulk ->
    {
        val m = state.groupmessages.toList()
        state.copy(groupmessages = (m + action.messages).toMutableStateList())
    }
    is GroupMessageAction.SendGroupMessage ->
    {
        state.copy(groupmessages = (state.groupmessages + action.groupmessage).takeLast(maxGroupMessages).toMutableStateList())
    }
    is GroupMessageAction.ReceiveGroupMessage ->
    {
        state.copy(groupmessages = (state.groupmessages + action.groupmessage).takeLast(maxGroupMessages).toMutableStateList())
    }
    is GroupMessageAction.ClearGroup ->
    {
        state.copy(mutableStateListOf())
    }
    else ->
    {
        state.copy(mutableStateListOf())
    }
}

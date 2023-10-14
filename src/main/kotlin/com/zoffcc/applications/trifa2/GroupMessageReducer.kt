sealed interface GroupMessageAction
{
    data class SendGroupMessage(val groupmessage: UIGroupMessage) : GroupMessageAction
    data class ReceiveGroupMessage(val groupmessage: UIGroupMessage) : GroupMessageAction
    data class ClearGroup(val groupmessage: Int) : GroupMessageAction
}

data class GroupMessageState(val groupmessages: List<UIGroupMessage> = emptyList())

const val maxGroupMessages = 5000
fun groupchatReducer(state: GroupMessageState, action: GroupMessageAction): GroupMessageState = when (action)
{
    is GroupMessageAction.SendGroupMessage ->
    {
        state.copy(groupmessages = (state.groupmessages + action.groupmessage).takeLast(maxGroupMessages))
    }
    is GroupMessageAction.ReceiveGroupMessage ->
    {
        state.copy(groupmessages = (state.groupmessages + action.groupmessage).takeLast(maxGroupMessages))
    }
    is GroupMessageAction.ClearGroup ->
    {
        state.copy(groupmessages = emptyList())
    }
    else ->
    {
        state.copy(groupmessages = emptyList())
    }
}

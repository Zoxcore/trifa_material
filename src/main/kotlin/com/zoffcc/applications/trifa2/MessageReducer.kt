sealed interface MessageAction
{
    data class SendMessage(val message: UIMessage) : MessageAction
    data class ReceiveMessage(val message: UIMessage) : MessageAction
    data class Clear(val dummy: Int) : MessageAction
}

data class MessageState(val messages: List<UIMessage> = emptyList())

const val maxMessages = 5000
fun chatReducer(state: MessageState, action: MessageAction): MessageState = when (action)
{
    is MessageAction.SendMessage ->
    {
        state.copy(messages = (state.messages + action.message).takeLast(maxMessages))
    }
    is MessageAction.ReceiveMessage ->
    {
        state.copy(messages = (state.messages + action.message).takeLast(maxMessages))
    }
    is MessageAction.Clear ->
    {
        state.copy(messages = emptyList())
    }
    else ->
    {
        state.copy(messages = emptyList())
    }
}

import com.zoffcc.applications.sorm.Message
import com.zoffcc.applications.trifa.Log

sealed interface MessageAction
{
    data class SendMessage(val message: UIMessage) : MessageAction
    data class ReceiveMessage(val message: UIMessage) : MessageAction
    data class UpdateMessage(val message_db: Message) : MessageAction
    data class Clear(val dummy: Int) : MessageAction
}

data class MessageState(var messages: List<UIMessage> = emptyList())

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
    is MessageAction.UpdateMessage ->
    {
        // HINT: update like this does not work :-(
        val TAG = "UpdateMessage"
        val item_position = state.messages.binarySearchBy(action.message_db.id) { it.id }
        Log.i(TAG, "item_position = " + item_position)
        val item = state.messages.get(item_position)
        item.text = "xxx"
        state
    }
    else ->
    {
        state.copy(messages = emptyList())
    }
}

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.zoffcc.applications.sorm.Filetransfer
import com.zoffcc.applications.sorm.Message
import com.zoffcc.applications.trifa.Log

sealed interface MessageAction
{
    data class SendMessagesBulk(val messages: List<UIMessage>, val toxpk: String) : MessageAction
    data class SendMessage(val message: UIMessage) : MessageAction
    data class ReceiveMessagesBulk(val messages: List<UIMessage>, val toxpk: String) : MessageAction
    data class ReceiveMessage(val message: UIMessage) : MessageAction
    data class UpdateMessage(val message_db: Message, val filetransfer_db: Filetransfer?) : MessageAction
    data class Clear(val dummy: Int) : MessageAction
}

data class MessageState(var messages: SnapshotStateList<UIMessage> = mutableStateListOf())

const val maxMessages = 5000
fun chatReducer(state: MessageState, action: MessageAction): MessageState = when (action)
{
    is MessageAction.SendMessagesBulk ->
    {
        val m = state.messages
        state.copy(messages = (m + action.messages).toMutableStateList())
    }
    is MessageAction.SendMessage ->
    {
        val m = state.messages
        m.add(action.message)
        m.takeLast(maxMessages)
        state.copy(messages = state.messages)
    }
    is MessageAction.ReceiveMessagesBulk ->
    {
        val m = state.messages
        state.copy(messages = (m + action.messages).toMutableStateList())
    }
    is MessageAction.ReceiveMessage ->
    {
        val m = state.messages
        m.add(action.message)
        m.takeLast(maxMessages)
        state.copy(messages = state.messages)
    }
    is MessageAction.Clear ->
    {
        state.messages.clear()
        state.copy(mutableStateListOf())
    }
    is MessageAction.UpdateMessage ->
    {
        val TAG = "UpdateMessage"
        val item_position = state.messages.binarySearchBy(action.message_db.id) { it.id }
        Log.i(TAG, "item_position = " + item_position)
        val item = state.messages[item_position]
        if (action.filetransfer_db != null)
        {
            state.messages[item_position] = item.copy(
                filesize = action.filetransfer_db.filesize,
                currentfilepos =  action.filetransfer_db.current_position,
                filename_fullpath = action.message_db.filename_fullpath
            )
        }
        else
        {
            state.messages[item_position] = item.copy()
        }
        state.copy(messages = state.messages)
    }
    else ->
    {
        state.messages.clear()
        state.copy(mutableStateListOf())
    }
}

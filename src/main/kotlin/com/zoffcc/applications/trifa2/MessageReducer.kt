import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.zoffcc.applications.sorm.Filetransfer
import com.zoffcc.applications.sorm.Message
import com.zoffcc.applications.trifa.Log

sealed interface MessageAction
{
    // data class SendMessagesBulk(val messages: List<UIMessage>, val toxpk: String) : MessageAction
    data class SendMessage(val message: UIMessage) : MessageAction
    data class ReceiveMessagesBulkWithClear(val messages: List<UIMessage>, val toxpk: String) : MessageAction
    data class ReceiveMessage(val message: UIMessage) : MessageAction
    data class UpdateMessage(val message_db: Message, val filetransfer_db: Filetransfer?) : MessageAction
    data class UpdateTextMessage(val message_db: Message) : MessageAction
    data class Clear(val dummy: Int) : MessageAction
}

data class MessageState(var messages: SnapshotStateList<UIMessage> = mutableStateListOf())

const val maxMessages = MAX_ONE_ON_ONE_MESSAGES_TO_SHOW
fun chatReducer(state: MessageState, action: MessageAction): MessageState = when (action)
{
    /*
    is MessageAction.SendMessagesBulk ->
    {
        val m = state.messages.toList()
        Log.i(com.zoffcc.applications.trifa.TAG, "MessageAction.SendMessagesBulk")
        state.copy(messages = (m + action.messages).toMutableStateList())
    }
     */
    is MessageAction.SendMessage ->
    {
        // val m = state.messages
        // m.add(action.message)
        // m.takeLast(maxMessages)
        Log.i(com.zoffcc.applications.trifa.TAG, "MessageAction.SendMessage")
        state.copy(messages = (state.messages + action.message).takeLast(maxMessages).toMutableStateList())
    }
    is MessageAction.ReceiveMessagesBulkWithClear ->
    {
        // state.messages.clear()
        // Log.i(com.zoffcc.applications.trifa.TAG, "MessageAction.ReceiveMessagesBulkWithClear")
        state.copy(messages = (action.messages).toMutableStateList())
    }
    is MessageAction.ReceiveMessage ->
    {
        // val m = state.messages
        // m.add(action.message)
        // m.takeLast(maxMessages)
        // Log.i(com.zoffcc.applications.trifa.TAG, "MessageAction.ReceiveMessage")
        state.copy(messages = (state.messages + action.message).takeLast(maxMessages).toMutableStateList())
    }
    is MessageAction.Clear ->
    {
        // state.messages.clear()
        // Log.i(com.zoffcc.applications.trifa.TAG, "MessageAction.Clear")
        state.copy(mutableStateListOf())
    }
    is MessageAction.UpdateMessage ->
    {
        val TAG = "UpdateMessage"
        try
        {
            val item_position = state.messages.binarySearchBy(action.message_db.id) { it.id }
            // Log.i(TAG, "item_position = " + item_position)
            val item = state.messages[item_position]
            if (action.filetransfer_db != null)
            {
                state.messages[item_position] = item.copy(file_state = action.message_db.state, filesize = action.filetransfer_db.filesize, currentfilepos = action.filetransfer_db.current_position, filename_fullpath = action.message_db.filename_fullpath)
            } else
            {
                Log.i(TAG, "UpdateMessage:ft=null");
                state.messages[item_position] = item.copy(file_state = action.message_db.state, filename_fullpath = null, currentfilepos = 0, filesize = 0)
            }
        }
        catch (e: Exception)
        {
        }
        // Log.i(TAG, "MessageAction.UpdateMessage")
        state.copy(messages = state.messages)
    }
    is MessageAction.UpdateTextMessage ->
    {
        val TAG = "UpdateTextMessage"
        try
        {
            val m = state.messages.toList()
            val item_position = m.binarySearchBy(action.message_db.id) { it.id }
            val item = m[item_position]
            state.messages[item_position] = item.copy(sentTimeMs = action.message_db.sent_timestamp,
                recvTimeMs = action.message_db.rcvd_timestamp,
                read = action.message_db.read,
                is_new = action.message_db.is_new,
                sent_push = action.message_db.sent_push,
                msg_version = action.message_db.msg_version,
                msg_id_hash = action.message_db.msg_id_hash,
                msg_idv3_hash = action.message_db.msg_idv3_hash
            )
        }
        catch (e: Exception)
        {
        }
        // Log.i(TAG, "MessageAction.UpdateTextMessage")
        state.copy(messages = state.messages)
    }
    else ->
    {
        // state.messages.clear()
        Log.i(com.zoffcc.applications.trifa.TAG, "MessageAction.Default -> Clear")
        state.copy(messages = mutableStateListOf())
    }
}

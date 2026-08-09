@file:Suppress("PropertyName", "LocalVariableName")

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot
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
fun chatReducer(state: MessageState, action: MessageAction): MessageState {
    Snapshot.withMutableSnapshot {
        when (action)
        {
            is MessageAction.SendMessage ->
            {
                state.messages.add(action.message)
            }
            is MessageAction.ReceiveMessagesBulkWithClear ->
            {
                state.messages.clear()
                state.messages.addAll(action.messages)
            }
            is MessageAction.ReceiveMessage ->
            {
                state.messages.add(action.message)
            }
            is MessageAction.Clear ->
            {
                state.messages.clear()
            }

            is MessageAction.UpdateMessage ->
            {
                val TAG = "UpdateMessage"
                val item_position = state.messages.indexOfFirst { it.id == action.message_db.id }
                if (item_position != -1)
                {
                    val item = state.messages[item_position]
                    val updatedItem = if (action.filetransfer_db != null)
                    {
                        val prev_pos = item.currentfilepos
                        val prev_pos_ts = item.currentfileposTimeMs
                        val cur_pos_ts = System.currentTimeMillis()
                        var start_ts = item.startfileposTimeMs
                        var start_file_pos = item.startfilepos

                        if (start_ts == 0L)
                        {
                            start_ts = cur_pos_ts
                        }
                        if ((start_file_pos == 0L) && (prev_pos == 0L) && (action.filetransfer_db.current_position > 0L))
                        {
                            start_file_pos = action.filetransfer_db.current_position
                        }

                        item.copy(
                            file_state = action.message_db.state,
                            filesize = action.filetransfer_db.filesize,
                            currentfilepos = action.filetransfer_db.current_position,
                            previousfilepos = prev_pos,
                            currentfileposTimeMs = cur_pos_ts,
                            startfilepos = start_file_pos,
                            startfileposTimeMs = start_ts,
                            previousfileposTimeMs = prev_pos_ts,
                            filename_fullpath = action.message_db.filename_fullpath
                        )
                    } else
                    {
                        Log.i(TAG, "UpdateMessage:ft=null")
                        val cur_pos_ts = System.currentTimeMillis()
                        item.copy(
                            file_state = action.message_db.state,
                            filename_fullpath = null,
                            currentfilepos = 0,
                            previousfilepos = 0,
                            previousfileposTimeMs = 0,
                            startfilepos = 0L,
                            startfileposTimeMs = cur_pos_ts,
                            currentfileposTimeMs = cur_pos_ts,
                            filesize = 0
                        )
                    }
                    state.messages[item_position] = updatedItem
                }
            }
            is MessageAction.UpdateTextMessage ->
            {
                val item_position = state.messages.indexOfFirst { it.id == action.message_db.id }
                if (item_position != -1)
                {
                    val item = state.messages[item_position]
                    state.messages[item_position] = item.copy(
                        sentTimeMs = action.message_db.sent_timestamp,
                        recvTimeMs = action.message_db.rcvd_timestamp,
                        read = action.message_db.read,
                        is_new = action.message_db.is_new,
                        sent_push = action.message_db.sent_push,
                        msg_version = action.message_db.msg_version,
                        msg_id_hash = action.message_db.msg_id_hash,
                        msg_idv3_hash = action.message_db.msg_idv3_hash
                    )
                }
            }
            else ->
            {
                Log.i(com.zoffcc.applications.trifa.TAG, "MessageAction.Default -> Clear (should never get here)")
                state.messages.clear()
            }
        }
        if (!contactstore.state.fullHistoryActive)
        {
            // Global trimming logic: Keeps the code clean and handles any action size safely
            val excess = state.messages.size - maxMessages
            if (excess > 0)
            {
                state.messages.removeRange(0, excess)
            }
        }
    }
    return state
}

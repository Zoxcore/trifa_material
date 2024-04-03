import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

interface MessageStore
{
    fun send(action: MessageAction)
    val stateFlow: MutableStateFlow<MessageState>
    val state get() = stateFlow.value
}

fun CoroutineScope.createMessageStore(): MessageStore
{
    val mutableStateFlow = MutableStateFlow(MessageState())
    val channel: Channel<MessageAction> = Channel(Channel.UNLIMITED)

    return object : MessageStore
    {
        init
        {
            launch {
                channel.consumeAsFlow().collect { action ->
                    mutableStateFlow.value = chatReducer(mutableStateFlow.value, action)
                }
            }
        }

        override fun send(action: MessageAction)
        {
            launch {
                global_semaphore_messagelist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                if (action is MessageAction.ReceiveMessage)
                {
                    if (contactstore.state.selectedContactPubkey == action.message.toxpk)
                    {
                        channel.send(action)
                    }
                } else if (action is MessageAction.UpdateMessage)
                {
                    if (contactstore.state.selectedContactPubkey == action.message_db.tox_friendpubkey)
                    {
                        channel.send(action)
                    }
                } else if (action is MessageAction.UpdateTextMessage)
                {
                    if (contactstore.state.selectedContactPubkey == action.message_db.tox_friendpubkey)
                    {
                        channel.send(action)
                    }
                } else if (action is MessageAction.ReceiveMessagesBulkWithClear)
                {
                    if (contactstore.state.selectedContactPubkey == action.toxpk)
                    {
                        channel.send(action)
                    }
                /*
                } else if (action is MessageAction.SendMessagesBulk)
                {
                    if (contactstore.state.selectedContactPubkey == action.toxpk)
                    {
                        channel.send(action)
                    }
                */
                } else
                {
                    channel.send(action)
                }
                global_semaphore_messagelist_ui.release()
            }
        }

        override val stateFlow: MutableStateFlow<MessageState> = mutableStateFlow
    }
}

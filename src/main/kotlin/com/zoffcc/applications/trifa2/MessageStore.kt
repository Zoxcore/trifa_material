import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

interface MessageStore
{
    fun send(action: MessageAction)
    val stateFlow: StateFlow<MessageState>
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
                if (action is MessageAction.ReceiveMessage)
                {
                    if (contactstore.state.selectedContactPubkey == action.message.toxpk)
                    {
                        channel.send(action)
                    }
                } else
                {
                    channel.send(action)
                }
            }
        }

        override val stateFlow: StateFlow<MessageState> = mutableStateFlow
    }
}

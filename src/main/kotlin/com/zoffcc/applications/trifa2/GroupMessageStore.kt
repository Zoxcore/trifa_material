import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

interface GroupMessageStore
{
    fun send(action: GroupMessageAction)
    val stateFlow: StateFlow<GroupMessageState>
    val state get() = stateFlow.value
}

fun CoroutineScope.createGroupMessageStore(): GroupMessageStore
{
    val mutableStateFlow = MutableStateFlow(GroupMessageState())
    val groupmessagechannel: Channel<GroupMessageAction> = Channel(Channel.UNLIMITED)

    return object : GroupMessageStore
    {
        init
        {
            launch {
                groupmessagechannel.consumeAsFlow().collect { action ->
                    mutableStateFlow.value = groupchatReducer(mutableStateFlow.value, action)
                }
            }
        }

        override fun send(action: GroupMessageAction)
        {
            launch {
                global_semaphore_groupmessagelist_ui.acquire((Throwable().stackTrace[0].fileName + ":" + Throwable().stackTrace[0].lineNumber))
                if (action is GroupMessageAction.ReceiveGroupMessage)
                {
                    if (groupstore.state.selectedGroupId == action.groupmessage.groupId)
                    {
                        groupmessagechannel.send(action)
                    }
                } else
                {
                    groupmessagechannel.send(action)
                }
                global_semaphore_groupmessagelist_ui.release()
            }
        }

        override val stateFlow: StateFlow<GroupMessageState> = mutableStateFlow
    }
}

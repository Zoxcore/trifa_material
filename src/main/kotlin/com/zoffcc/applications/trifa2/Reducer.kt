sealed interface Action {
    data class SendMessage(val message: Message) : Action
    data class ReceiveMessage(val message: Message) : Action
    data class Clear(val dummy: Int) : Action
}

data class State(
    val messages: List<Message> = emptyList()
)

const val maxMessages = 5000

fun chatReducer(state: State, action: Action): State =
    when (action) {
        is Action.SendMessage -> {
            state.copy(
                messages = (state.messages + action.message).takeLast(maxMessages)
            )
        }

        is Action.ReceiveMessage -> {
            state.copy(
                messages = (state.messages + action.message).takeLast(maxMessages)
            )
        }

        is Action.Clear -> {
            state.copy(
                messages = emptyList()
            )
        }

        else -> {
            state.copy(
                messages = emptyList()
            )
        }
    }

import kotlin.random.Random

data class UIGroupMessage private constructor(
    val user: User,
    val timeMs: Long,
    val text: String,
    val id: Long,
    val toxpk: String?,
    val groupId: String
) {
    constructor(
        user: User,
        timeMs: Long,
        text: String,
        toxpk: String?,
        groupId: String
    ) : this(
        user = user,
        timeMs = timeMs,
        text = text,
        id = Random.nextLong(),
        toxpk = toxpk,
        groupId = groupId
    )
}

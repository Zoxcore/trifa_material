import kotlin.random.Random

data class UIGroupMessage private constructor(
    val user: User,
    val timeMs: Long,
    val text: String,
    val id: Long,
    val msgDatabaseId: Long,
    val message_id_tox: String?,
    val toxpk: String?,
    val groupId: String,
    val trifaMsgType: Int,
    val filename_fullpath: String?
) {
    constructor(
        user: User,
        timeMs: Long,
        text: String,
        msgDatabaseId: Long,
        message_id_tox: String?,
        toxpk: String?,
        groupId: String,
        trifaMsgType: Int,
        filename_fullpath: String?
    ) : this(
        user = user,
        timeMs = timeMs,
        text = text,
        id = msgDatabaseId,
        msgDatabaseId = msgDatabaseId,
        message_id_tox = message_id_tox,
        toxpk = toxpk,
        groupId = groupId,
        trifaMsgType = trifaMsgType,
        filename_fullpath = filename_fullpath
    )
}

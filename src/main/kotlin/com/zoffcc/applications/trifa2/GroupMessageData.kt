import kotlin.random.Random

data class UIGroupMessage private constructor(
    val user: User,
    val timeMs: Long,
    val text: String,
    val id: Long,
    val msgDatabaseId: Long,
    val msg_id_hash: String?,
    val message_id_tox: String?,
    val toxpk: String?,
    val groupId: String,
    val was_synced: Boolean,
    val trifaMsgType: Int,
    val filename_fullpath: String?
) {
    constructor(
        user: User,
        timeMs: Long,
        text: String,
        msgDatabaseId: Long,
        msg_id_hash: String?,
        message_id_tox: String?,
        toxpk: String?,
        groupId: String,
        was_synced: Boolean,
        trifaMsgType: Int,
        filename_fullpath: String?
    ) : this(
        user = user,
        timeMs = timeMs,
        text = text,
        id = msgDatabaseId,
        msgDatabaseId = msgDatabaseId,
        msg_id_hash = msg_id_hash,
        message_id_tox = message_id_tox,
        toxpk = toxpk,
        groupId = groupId,
        was_synced = was_synced,
        trifaMsgType = trifaMsgType,
        filename_fullpath = filename_fullpath
    )
}

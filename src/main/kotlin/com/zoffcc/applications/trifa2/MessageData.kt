import androidx.compose.ui.graphics.Color
import com.zoffcc.applications.trifa.ChatColors
import com.zoffcc.applications.trifa.ToxVars

data class UIMessage private constructor(
    val user: User,
    val timeMs: Long,
    val sentTimeMs: Long,
    val recvTimeMs: Long,
    var text: String,
    val id: Long,
    val direction: Int,
    val read: Boolean,
    val sent_push: Int,
    val is_new: Boolean,
    val toxpk: String,
    val trifaMsgType: Int,
    val msg_idv3_hash: String?,
    val msg_id_hash: String?,
    val msg_version: Int,
    val msgDatabaseId: Long,
    val filesize: Long = 0L,
    val currentfilepos: Long = 0L,
    val filename_fullpath: String?,
    val file_state: Int = ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE.value
) {
    constructor(
        user: User,
        timeMs: Long,
        sentTimeMs: Long,
        recvTimeMs: Long,
        text: String,
        direction: Int,
        read: Boolean,
        sent_push: Int,
        is_new: Boolean = true,
        toxpk: String,
        trifaMsgType: Int,
        msg_idv3_hash: String?,
        msg_id_hash: String?,
        msg_version: Int,
        msgDatabaseId: Long,
        filesize: Long = 0L,
        currentfilepos: Long = 0L,
        filename_fullpath: String?,
        file_state: Int = ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE.value
    ) : this(
        user = user,
        timeMs = timeMs,
        sentTimeMs = sentTimeMs,
        recvTimeMs = recvTimeMs,
        text = text,
        direction = direction,
        read = read,
        sent_push = sent_push,
        is_new = is_new,
        id = msgDatabaseId,
        msgDatabaseId = msgDatabaseId,
        toxpk = toxpk,
        trifaMsgType = trifaMsgType,
        msg_idv3_hash = msg_idv3_hash,
        msg_id_hash = msg_id_hash,
        msg_version = msg_version,
        filesize = filesize,
        currentfilepos = currentfilepos,
        filename_fullpath = filename_fullpath,
        file_state = file_state
    )
}

data class User(
    val name: String,
    val color: Color = ColorProvider.getColor(),
    val picture: String?,
    var toxpk: String
)

object ColorProvider {
    fun getColor(isGroupPeer: Boolean = false, peer_pubkey : String? = null): Color {
        return when(isGroupPeer)
        {
            false -> Color(0xFFEA3468)
            true ->
            {
                Color(ChatColors.get_ngc_peer_color(peer_pubkey))
            }
        }
    }
}
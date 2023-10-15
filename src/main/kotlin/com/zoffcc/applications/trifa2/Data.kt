import androidx.compose.ui.graphics.Color
import com.zoffcc.applications.trifa.ChatColors
import kotlin.random.Random

data class UIMessage private constructor(
    val user: User,
    val timeMs: Long,
    val text: String,
    val id: Long,
    val toxpk: String?
) {
    constructor(
        user: User,
        timeMs: Long,
        text: String,
        toxpk: String?
    ) : this(
        user = user,
        timeMs = timeMs,
        text = text,
        id = Random.nextLong(),
        toxpk = toxpk
    )
}

data class User(
    val name: String,
    val color: Color = ColorProvider.getColor(),
    val picture: String?,
    val toxpk: String?
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
import androidx.compose.ui.graphics.Color
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
    val colors = mutableListOf(
        0xFFEA3468,
        0xFFB634EA,
        0xFF349BEA,
    )
    fun getColor(): Color {
        return Color(colors[0])
    }
}
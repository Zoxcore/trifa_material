import java.text.SimpleDateFormat
import java.util.*


val df_date_time_long = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

fun timeToString(timestampMs: Long): String {
    return try {
        df_date_time_long.format(Date(timestampMs))
    } catch (e: Exception) {
        "1970-02-02 12:00:01"
    }
}

fun timestampMs(): Long {
    return System.currentTimeMillis()
}

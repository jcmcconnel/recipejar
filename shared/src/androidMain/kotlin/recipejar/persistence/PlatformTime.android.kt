package recipejar.persistence

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun currentTimestampString(): String =
    SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).format(Date())

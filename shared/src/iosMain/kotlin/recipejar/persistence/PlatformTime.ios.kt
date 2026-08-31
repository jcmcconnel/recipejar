package recipejar.persistence

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual fun currentTimestampString(): String {
    val fmt = NSDateFormatter()
    fmt.dateFormat = "EEE MMM dd HH:mm:ss zzz yyyy"
    fmt.locale = NSLocale.currentLocale
    return fmt.stringFromDate(NSDate())
}

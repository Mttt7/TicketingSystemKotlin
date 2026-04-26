package mt.pollub.demo.shared.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


object GlobalUtils {
    const val DATE: String = "yyyy-MM-dd"
    const val DATE_TIME: String = "yyyy-MM-dd HH:mm:ss"

    fun formatDateTime(dateTime: LocalDateTime?): String {

        return dateTime?.format(DateTimeFormatter.ofPattern(DATE_TIME)) ?: "-"
    }
}
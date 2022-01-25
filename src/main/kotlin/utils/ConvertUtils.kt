package utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ConvertUtils {
    companion object {
        fun timestamp2DateTime(timestamp: Int): LocalDateTime {
            return LocalDateTime.ofEpochSecond(
                timestamp.toLong(),
                0,
                ZoneId.systemDefault().rules.getOffset(Instant.now())
            )
        }
    }
}
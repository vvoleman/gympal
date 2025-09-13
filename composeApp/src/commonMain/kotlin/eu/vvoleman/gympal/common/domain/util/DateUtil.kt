package eu.vvoleman.gympal.common.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun currentLocalDateTime(): LocalDateTime {
    val nowInstant = Clock.System.now()                // platform current instant
    val tz = TimeZone.currentSystemDefault()           // platform default time zone
    return nowInstant.toLocalDateTime(tz)
}

package com.awhogue.countdoodle.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs

data class CountdownParts(
    val months: Int,
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val isPast: Boolean,
)

/** Calendar-aware breakdown of the gap between two instants into months / days / h / m / s. */
fun countdownParts(
    nowEpochMillis: Long,
    targetEpochMillis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): CountdownParts {
    val a0 = Instant.ofEpochMilli(nowEpochMillis).atZone(zone).toLocalDateTime()
    val b0 = Instant.ofEpochMilli(targetEpochMillis).atZone(zone).toLocalDateTime()
    val past = b0.isBefore(a0)
    val a = if (past) b0 else a0
    val b = if (past) a0 else b0

    val months = ChronoUnit.MONTHS.between(a, b).toInt()
    val afterMonths = a.plusMonths(months.toLong())
    val days = ChronoUnit.DAYS.between(afterMonths, b).toInt()
    val afterDays = afterMonths.plusDays(days.toLong())
    val totalSec = ChronoUnit.SECONDS.between(afterDays, b)
    val hours = (totalSec / 3600).toInt()
    val minutes = ((totalSec % 3600) / 60).toInt()
    val seconds = (totalSec % 60).toInt()

    return CountdownParts(months, days, hours, minutes, seconds, past)
}

fun formatCountdown(
    nowEpochMillis: Long,
    targetEpochMillis: Long,
    hasTime: Boolean,
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    return if (hasTime) {
        formatTimed(nowEpochMillis, targetEpochMillis)
    } else {
        formatAllDay(nowEpochMillis, targetEpochMillis, zone)
    }
}

private fun formatTimed(nowMillis: Long, targetMillis: Long): String {
    val diff = targetMillis - nowMillis
    val past = diff < 0
    var remaining = abs(diff) / 1000  // seconds

    val days = remaining / 86_400; remaining %= 86_400
    val hours = remaining / 3_600;  remaining %= 3_600
    val minutes = remaining / 60;   val seconds = remaining % 60

    val parts = buildList {
        if (days > 0) add("${days}d")
        if (days > 0 || hours > 0) add("${hours}h")
        if (days > 0 || hours > 0 || minutes > 0) add("${minutes}m")
        add("${seconds}s")
    }
    val text = parts.joinToString(" ")
    return if (past) "Happened $text ago" else text
}

private fun formatAllDay(nowMillis: Long, targetMillis: Long, zone: ZoneId): String {
    val today: LocalDate = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    val targetDay: LocalDate = Instant.ofEpochMilli(targetMillis).atZone(zone).toLocalDate()
    val days = ChronoUnit.DAYS.between(today, targetDay).toInt()

    return when {
        days == 0 -> "Today"
        days == 1 -> "1 day"
        days > 1 -> "$days days"
        days == -1 -> "Happened 1 day ago"
        else -> "Happened ${-days} days ago"
    }
}

package com.awhogue.countdoodle.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs

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

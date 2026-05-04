package com.awhogue.countdoodle.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class CountdownTest {

    private val zone = ZoneId.of("America/Los_Angeles")

    private fun ldt(y: Int, mo: Int, d: Int, h: Int = 0, mi: Int = 0, s: Int = 0): Long =
        ZonedDateTime.of(LocalDateTime.of(y, mo, d, h, mi, s), zone).toInstant().toEpochMilli()

    // ----- Timed events -----

    @Test fun `timed shows days hours minutes seconds`() {
        val now = ldt(2026, 5, 1, 0, 0, 0)
        val target = ldt(2026, 5, 13, 4, 3, 12)
        assertThat(formatCountdown(now, target, hasTime = true, zone = zone))
            .isEqualTo("12d 4h 3m 12s")
    }

    @Test fun `timed collapses leading zero days`() {
        val now = ldt(2026, 5, 1, 0, 0, 0)
        val target = ldt(2026, 5, 1, 4, 3, 12)
        assertThat(formatCountdown(now, target, hasTime = true, zone = zone))
            .isEqualTo("4h 3m 12s")
    }

    @Test fun `timed collapses leading zero days and hours`() {
        val now = ldt(2026, 5, 1, 0, 0, 0)
        val target = ldt(2026, 5, 1, 0, 3, 12)
        assertThat(formatCountdown(now, target, hasTime = true, zone = zone))
            .isEqualTo("3m 12s")
    }

    @Test fun `timed shows only seconds when under one minute`() {
        val now = ldt(2026, 5, 1, 0, 0, 0)
        val target = ldt(2026, 5, 1, 0, 0, 12)
        assertThat(formatCountdown(now, target, hasTime = true, zone = zone))
            .isEqualTo("12s")
    }

    @Test fun `timed past event shows happened ago`() {
        val now = ldt(2026, 5, 13, 4, 3, 12)
        val target = ldt(2026, 5, 1, 0, 0, 0)
        assertThat(formatCountdown(now, target, hasTime = true, zone = zone))
            .isEqualTo("Happened 12d 4h 3m 12s ago")
    }

    // ----- All-day events -----

    @Test fun `all-day shows X days when several days away`() {
        val now = ldt(2026, 5, 1, 10, 30, 0)
        val target = ldt(2026, 5, 13, 0, 0, 0)
        assertThat(formatCountdown(now, target, hasTime = false, zone = zone))
            .isEqualTo("12 days")
    }

    @Test fun `all-day shows 1 day when one day away`() {
        val now = ldt(2026, 5, 1, 10, 30, 0)
        val target = ldt(2026, 5, 2, 0, 0, 0)
        assertThat(formatCountdown(now, target, hasTime = false, zone = zone))
            .isEqualTo("1 day")
    }

    @Test fun `all-day shows Today when target date equals today`() {
        val now = ldt(2026, 5, 1, 10, 30, 0)
        val target = ldt(2026, 5, 1, 0, 0, 0)
        assertThat(formatCountdown(now, target, hasTime = false, zone = zone))
            .isEqualTo("Today")
    }

    @Test fun `all-day past event shows Happened N days ago`() {
        val now = ldt(2026, 5, 5, 12, 0, 0)
        val target = ldt(2026, 5, 2, 0, 0, 0)
        assertThat(formatCountdown(now, target, hasTime = false, zone = zone))
            .isEqualTo("Happened 3 days ago")
    }

    @Test fun `all-day past event 1 day ago`() {
        val now = ldt(2026, 5, 2, 12, 0, 0)
        val target = ldt(2026, 5, 1, 0, 0, 0)
        assertThat(formatCountdown(now, target, hasTime = false, zone = zone))
            .isEqualTo("Happened 1 day ago")
    }

    @Test fun `all-day boundary at local midnight`() {
        val now = ZonedDateTime.of(LocalDate.of(2026, 5, 1).atTime(23, 59, 59), zone).toInstant().toEpochMilli()
        val target = ZonedDateTime.of(LocalDate.of(2026, 5, 2).atStartOfDay(), zone).toInstant().toEpochMilli()
        assertThat(formatCountdown(now, target, hasTime = false, zone = zone))
            .isEqualTo("1 day")
    }
}

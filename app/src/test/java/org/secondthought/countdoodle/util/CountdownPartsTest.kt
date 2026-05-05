package org.secondthought.countdoodle.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class CountdownPartsTest {

    private val zone = ZoneId.of("America/Los_Angeles")
    private fun ms(y: Int, mo: Int, d: Int, h: Int = 0, mi: Int = 0, s: Int = 0): Long =
        ZonedDateTime.of(LocalDateTime.of(y, mo, d, h, mi, s), zone).toInstant().toEpochMilli()

    @Test fun `whole years roll into months`() {
        val parts = countdownParts(ms(2026, 5, 4), ms(2027, 5, 4), zone)
        assertThat(parts.months).isEqualTo(12)
        assertThat(parts.days).isEqualTo(0)
        assertThat(parts.hours).isEqualTo(0)
    }

    @Test fun `months and days for one month plus three weeks`() {
        val parts = countdownParts(ms(2026, 5, 1), ms(2026, 6, 22), zone)
        assertThat(parts.months).isEqualTo(1)
        assertThat(parts.days).isEqualTo(21)
    }

    @Test fun `precise breakdown matches example`() {
        // 2026-05-01 00:00 -> 2026-06-22 14:18:56  =  1mo 21d 14h 18m 56s
        val parts = countdownParts(
            ms(2026, 5, 1, 0, 0, 0),
            ms(2026, 6, 22, 14, 18, 56),
            zone,
        )
        assertThat(parts.months).isEqualTo(1)
        assertThat(parts.days).isEqualTo(21)
        assertThat(parts.hours).isEqualTo(14)
        assertThat(parts.minutes).isEqualTo(18)
        assertThat(parts.seconds).isEqualTo(56)
        assertThat(parts.isPast).isFalse()
    }

    @Test fun `past flag set when target is before now`() {
        val parts = countdownParts(ms(2026, 6, 22), ms(2026, 5, 1), zone)
        assertThat(parts.isPast).isTrue()
        assertThat(parts.months).isEqualTo(1)
        assertThat(parts.days).isEqualTo(21)
    }

    @Test fun `sub-minute returns only seconds`() {
        val parts = countdownParts(ms(2026, 5, 1, 0, 0, 0), ms(2026, 5, 1, 0, 0, 42), zone)
        assertThat(parts.months).isEqualTo(0)
        assertThat(parts.days).isEqualTo(0)
        assertThat(parts.hours).isEqualTo(0)
        assertThat(parts.minutes).isEqualTo(0)
        assertThat(parts.seconds).isEqualTo(42)
    }

    @Test fun `zero diff has all zeros`() {
        val parts = countdownParts(ms(2026, 5, 1), ms(2026, 5, 1), zone)
        assertThat(parts.months).isEqualTo(0)
        assertThat(parts.days).isEqualTo(0)
        assertThat(parts.hours).isEqualTo(0)
        assertThat(parts.minutes).isEqualTo(0)
        assertThat(parts.seconds).isEqualTo(0)
        assertThat(parts.isPast).isFalse()
    }
}

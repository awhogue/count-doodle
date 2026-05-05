package com.awhogue.countdoodle.widget

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CountdownLabelTest {

    @Test fun `multiple days left`() {
        assertThat(countdownLabel(totalHours = 52L * 24, totalDays = 52L, isPast = false))
            .isEqualTo("52" to "days left")
    }

    @Test fun `singular day left`() {
        assertThat(countdownLabel(totalHours = 25L, totalDays = 1L, isPast = false))
            .isEqualTo("1" to "day left")
    }

    @Test fun `hours left when same calendar day`() {
        assertThat(countdownLabel(totalHours = 15L, totalDays = 0L, isPast = false))
            .isEqualTo("15" to "hours left")
    }

    @Test fun `singular hour left`() {
        assertThat(countdownLabel(totalHours = 1L, totalDays = 0L, isPast = false))
            .isEqualTo("1" to "hour left")
    }

    @Test fun `days ago for past`() {
        assertThat(countdownLabel(totalHours = -72L, totalDays = -3L, isPast = true))
            .isEqualTo("3" to "days ago")
    }

    @Test fun `hours ago for past same day`() {
        assertThat(countdownLabel(totalHours = -2L, totalDays = 0L, isPast = true))
            .isEqualTo("2" to "hours ago")
    }

    @Test fun `placeholder when under one hour`() {
        assertThat(countdownLabel(totalHours = 0L, totalDays = 0L, isPast = false))
            .isEqualTo("—" to "soon")
        assertThat(countdownLabel(totalHours = 0L, totalDays = 0L, isPast = true))
            .isEqualTo("—" to "just now")
    }
}

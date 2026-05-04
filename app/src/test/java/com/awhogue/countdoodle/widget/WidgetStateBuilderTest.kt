package com.awhogue.countdoodle.widget

import com.awhogue.countdoodle.data.Event
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WidgetStateBuilderTest {

    @Test fun `multi widget shows next N upcoming sorted ascending`() {
        val events = listOf(
            Event(id = 1, name = "Past", dateEpochMillis = 100L),
            Event(id = 2, name = "Soon", dateEpochMillis = 1500L),
            Event(id = 3, name = "Later", dateEpochMillis = 2500L),
            Event(id = 4, name = "Latest", dateEpochMillis = 4000L),
            Event(id = 5, name = "Mid", dateEpochMillis = 2000L),
        )
        val state = buildMultiWidgetState(events, now = 1000L, limit = 3)
        assertThat(state.events.map { it.name }).containsExactly("Soon", "Mid", "Later").inOrder()
    }

    @Test fun `multi widget empty when no upcoming`() {
        val events = listOf(Event(id = 1, name = "Old", dateEpochMillis = 100L))
        val state = buildMultiWidgetState(events, now = 1000L, limit = 3)
        assertThat(state.events).isEmpty()
    }

    @Test fun `single widget returns null when event id null`() {
        val state = buildSingleWidgetState(eventId = null, allEvents = emptyList())
        assertThat(state.event).isNull()
    }

    @Test fun `single widget resolves event from list`() {
        val ev = Event(id = 7, name = "X", dateEpochMillis = 100L)
        val state = buildSingleWidgetState(eventId = 7L, allEvents = listOf(ev))
        assertThat(state.event).isEqualTo(ev)
    }
}

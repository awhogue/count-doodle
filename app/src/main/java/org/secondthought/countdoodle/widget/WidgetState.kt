package org.secondthought.countdoodle.widget

import org.secondthought.countdoodle.data.Event

data class MultiWidgetState(val events: List<Event>)
data class SingleWidgetState(val event: Event?)

fun buildMultiWidgetState(allEvents: List<Event>, now: Long, limit: Int): MultiWidgetState {
    val upcoming = allEvents
        .filter { it.dateEpochMillis >= now }
        .sortedBy { it.dateEpochMillis }
        .take(limit)
    return MultiWidgetState(upcoming)
}

fun buildSingleWidgetState(eventId: Long?, allEvents: List<Event>): SingleWidgetState {
    val event = if (eventId == null) null else allEvents.firstOrNull { it.id == eventId }
    return SingleWidgetState(event)
}

package org.secondthought.countdoodle.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

object SingleEventWidgetConfig {
    val EVENT_ID = longPreferencesKey("event_id")

    suspend fun read(context: Context, glanceId: GlanceId): Long? {
        val prefs: Preferences = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
        return prefs[EVENT_ID]
    }

    suspend fun write(context: Context, glanceId: GlanceId, eventId: Long) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply { set(EVENT_ID, eventId) }
        }
    }
}

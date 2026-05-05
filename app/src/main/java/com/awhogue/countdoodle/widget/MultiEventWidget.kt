package com.awhogue.countdoodle.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.awhogue.countdoodle.CountDoodleApp
import com.awhogue.countdoodle.MainActivity
import com.awhogue.countdoodle.data.Event
import com.awhogue.countdoodle.util.formatCountdown

class MultiEventWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = CountDoodleApp.from(context)
        val now = System.currentTimeMillis()
        val events = app.repository.getUpcoming(now, limit = 4)
        provideContent { Render(events, now) }
    }

    @Composable
    private fun Render(events: List<Event>, now: Long) {
        val ctx = LocalContext.current
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF202124)))
                .padding(12.dp)
                .clickable(actionStartActivity(
                    Intent(ctx, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                )),
        ) {
            Text(
                text = "Upcoming",
                style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            if (events.isEmpty()) {
                Text(
                    text = "No upcoming events",
                    style = TextStyle(color = ColorProvider(Color.White))
                )
            } else {
                events.forEach { e ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(actionStartActivity(
                                Intent(ctx, MainActivity::class.java).apply {
                                    data = Uri.parse("countdoodle://event/${e.id}")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                            )),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${e.displayEmoji}  ${e.name}",
                            style = TextStyle(color = ColorProvider(Color.White))
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Text(
                            text = formatCountdown(now, e.dateEpochMillis, hasTime = false),
                            style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }
}

class MultiEventWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MultiEventWidget()
}

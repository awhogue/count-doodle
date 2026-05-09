package org.secondthought.countdoodle.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.secondthought.countdoodle.CountDoodleApp
import org.secondthought.countdoodle.MainActivity
import org.secondthought.countdoodle.data.Event
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MultiEventWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = CountDoodleApp.from(context)
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val events = app.repository.getUpcoming(startOfToday, limit = 50)
        provideContent { Render(events, now) }
    }

    @Composable
    private fun Render(events: List<Event>, now: Long) {
        val ctx = LocalContext.current
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF111214)))
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            if (events.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity(
                        Intent(ctx, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No upcoming events",
                        style = TextStyle(color = ColorProvider(Color.White.copy(alpha = 0.7f)))
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(events, itemId = { it.id }) { e ->
                        Column {
                            EventRow(e, now, ctx)
                            Spacer(GlanceModifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun EventRow(event: Event, now: Long, ctx: Context) {
        val zone = ZoneId.systemDefault()
        val nowMs = Instant.ofEpochMilli(now)
        val targetMs = Instant.ofEpochMilli(event.dateEpochMillis)
        val totalHours = ChronoUnit.HOURS.between(nowMs, targetMs)
        val totalDays = ChronoUnit.DAYS.between(
            nowMs.atZone(zone).toLocalDate(),
            targetMs.atZone(zone).toLocalDate(),
        )
        val isPast = totalDays < 0 || (totalDays == 0L && totalHours < 0)
        val (number, label) = countdownLabel(totalHours, totalDays, isPast)

        val subtitle = targetMs.atZone(zone)
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity(
                    Intent(ctx, MainActivity::class.java).apply {
                        data = Uri.parse("countdoodle://event/${event.id}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                )),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left color stripe
            Box(
                modifier = GlanceModifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(ColorProvider(Color(event.displayColorArgb))),
            ) {}
            Spacer(GlanceModifier.width(8.dp))

            // Emoji + name + date subtitle
            Column(modifier = GlanceModifier.defaultWeight()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.displayEmoji,
                        style = TextStyle(fontSize = 14.sp, color = ColorProvider(Color.White))
                    )
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        text = event.name,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        ),
                        maxLines = 1,
                    )
                }
                Text(
                    text = subtitle,
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                        fontSize = 10.sp,
                    ),
                    maxLines = 1,
                )
            }

            Spacer(GlanceModifier.width(10.dp))

            // Right side: count + label
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = number,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    ),
                )
                Text(
                    text = label,
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                        fontSize = 10.sp,
                    ),
                )
            }
        }
    }
}

/** Returns (number string, unit label) appropriate to the event's distance. */
internal fun countdownLabel(totalHours: Long, totalDays: Long, isPast: Boolean): Pair<String, String> {
    val absDays = kotlin.math.abs(totalDays).toInt()
    val absHours = kotlin.math.abs(totalHours).toInt()
    return when {
        absDays >= 1 -> {
            val unit = if (absDays == 1) "day" else "days"
            absDays.toString() to (if (isPast) "$unit ago" else "$unit left")
        }
        absHours >= 1 -> {
            val unit = if (absHours == 1) "hour" else "hours"
            absHours.toString() to (if (isPast) "$unit ago" else "$unit left")
        }
        else -> "—" to (if (isPast) "just now" else "soon")
    }
}

class MultiEventWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MultiEventWidget()
}

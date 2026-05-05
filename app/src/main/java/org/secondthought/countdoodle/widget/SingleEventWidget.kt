package org.secondthought.countdoodle.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.secondthought.countdoodle.CountDoodleApp
import org.secondthought.countdoodle.MainActivity
import org.secondthought.countdoodle.data.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WidgetEventState(val event: Event?, val photo: Bitmap?)

class SingleEventWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Render() }
    }

    @Composable
    private fun Render() {
        val ctx = LocalContext.current
        val prefs = currentState<Preferences>()
        val eventId: Long? = prefs[SingleEventWidgetConfig.EVENT_ID]

        val state by produceState(initialValue = WidgetEventState(null, null), key1 = eventId) {
            value = if (eventId == null) {
                WidgetEventState(null, null)
            } else withContext(Dispatchers.IO) {
                val event = CountDoodleApp.from(ctx).repository.getById(eventId)
                val bmp = event?.photoUri?.let { loadBitmap(ctx, Uri.parse(it)) }
                WidgetEventState(event, bmp)
            }
            Log.d(TAG, "Render produceState eventId=$eventId resolved=${value.event?.name} hasPhoto=${value.photo != null}")
        }

        val ev = state.event
        val now = System.currentTimeMillis()
        val bgColor = ev?.let { Color(it.displayColorArgb) } ?: Color(0xFF202124)

        val clickIntent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (ev != null) data = Uri.parse("countdoodle://event/${ev.id}")
        }
        val clickAction = actionStartActivity(clickIntent)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(bgColor))
                .clickable(clickAction),
            contentAlignment = Alignment.Center,
        ) {
            // Photo as background (if any), then a dark overlay for legibility.
            state.photo?.let { bmp ->
                Image(
                    provider = ImageProvider(bmp),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier.fillMaxSize(),
                )
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color.Black.copy(alpha = 0.35f))),
                ) {}
            }

            if (ev == null) {
                Box(modifier = GlanceModifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (eventId == null) "Tap to configure" else "Loading…",
                        style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Medium)
                    )
                }
            } else {
                EventBody(ev = ev, now = now)
            }
        }
    }

    @Composable
    private fun EventBody(ev: Event, now: Long) {
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val eventDay = java.time.Instant.ofEpochMilli(ev.dateEpochMillis).atZone(zone).toLocalDate()
        val days = java.time.temporal.ChronoUnit.DAYS.between(today, eventDay).toInt()
        val isPast = days < 0
        val number = if (isPast) -days else days
        val unit = when {
            number == 0 -> "today"
            number == 1 && isPast -> "day ago"
            number == 1 -> "day left"
            isPast -> "days ago"
            else -> "days left"
        }
        val subtitle = eventDay.format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))

        // Adaptive layout: compact when there's not enough vertical room for the
        // big-number + label + date stack.
        val size = LocalSize.current
        val compact = size.height < 130.dp

        if (compact) CompactBody(ev, number, unit) else FullBody(ev, number, unit, subtitle)
    }

    @Composable
    private fun CompactBody(ev: Event, number: Int, unit: String) {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ev.displayEmoji,
                    style = TextStyle(fontSize = 16.sp, color = ColorProvider(Color.White))
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = ev.name,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    maxLines = 1,
                )
            }
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = if (number == 0) unit else "$number $unit",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                ),
                maxLines = 1,
            )
        }
    }

    @Composable
    private fun FullBody(ev: Event, number: Int, unit: String, subtitle: String) {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ev.displayEmoji,
                    style = TextStyle(fontSize = 18.sp, color = ColorProvider(Color.White))
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = ev.name,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    maxLines = 1,
                )
            }
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = if (number == 0) "—" else number.toString(),
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                ),
            )
            Text(
                text = unit,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 13.sp,
                ),
            )
            Spacer(GlanceModifier.defaultWeight())
            Text(
                text = subtitle,
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.85f)),
                    fontSize = 11.sp,
                ),
                maxLines = 1,
            )
        }
    }

    companion object { private const val TAG = "CountDoodleWidget" }
}

private const val WIDGET_MAX_DIM = 512

private fun loadBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val cr = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val srcW = bounds.outWidth
        val srcH = bounds.outHeight
        if (srcW <= 0 || srcH <= 0) return null

        var sample = 1
        while (srcW / (sample * 2) >= WIDGET_MAX_DIM && srcH / (sample * 2) >= WIDGET_MAX_DIM) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        val decoded = cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: return null

        val maxDim = maxOf(decoded.width, decoded.height)
        val scaled = if (maxDim > WIDGET_MAX_DIM) {
            val s = WIDGET_MAX_DIM.toFloat() / maxDim
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * s).toInt().coerceAtLeast(1),
                (decoded.height * s).toInt().coerceAtLeast(1),
                true,
            ).also { if (it !== decoded) decoded.recycle() }
        } else decoded

        Log.d("CountDoodleWidget",
            "loadBitmap uri=$uri src=${srcW}x$srcH sample=$sample out=${scaled.width}x${scaled.height} bytes=${scaled.byteCount}")
        scaled
    } catch (t: Throwable) {
        Log.w("CountDoodleWidget", "loadBitmap failed for $uri: ${t.message}")
        null
    }
}

class SingleEventWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SingleEventWidget()
}

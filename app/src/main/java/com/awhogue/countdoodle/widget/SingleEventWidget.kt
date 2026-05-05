package com.awhogue.countdoodle.widget

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
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.awhogue.countdoodle.CountDoodleApp
import com.awhogue.countdoodle.MainActivity
import com.awhogue.countdoodle.data.Event
import com.awhogue.countdoodle.util.formatCountdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WidgetEventState(val event: Event?, val photo: Bitmap?)

class SingleEventWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

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

            Box(modifier = GlanceModifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
                if (ev == null) {
                    Text(
                        text = if (eventId == null) "Tap to configure" else "Loading…",
                        style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Medium)
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${ev.displayEmoji} ${ev.name}",
                            style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Medium)
                        )
                        Text(
                            // Widgets refresh on the order of tens of minutes, so always
                            // show days-only — h/m/s would go stale and lie.
                            text = formatCountdown(now, ev.dateEpochMillis, hasTime = false),
                            style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
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

package com.awhogue.countdoodle.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.awhogue.countdoodle.CountDoodleApp
import com.awhogue.countdoodle.data.Event
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest

class SingleEventWidgetConfigActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Default to cancel; if user picks an event we'll set RESULT_OK below.
        setResult(Activity.RESULT_CANCELED)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        val app = CountDoodleApp.from(applicationContext)

        setContent {
            MaterialTheme {
                Surface {
                    val events = remember { app.repository.observeAll() }
                    val state = events.collectAsState(initial = emptyList())
                    Scaffold(
                        topBar = { TopAppBar(title = { Text("Pick an event") }) }
                    ) { padding ->
                        if (state.value.isEmpty()) {
                            Box(
                                Modifier.fillMaxSize().padding(padding),
                                contentAlignment = Alignment.Center,
                            ) { Text("No events yet — open the app first.") }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    top = padding.calculateTopPadding() + 8.dp,
                                    bottom = padding.calculateBottomPadding() + 8.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                ),
                            ) {
                                items(state.value, key = { it.id }) { e ->
                                    EventOption(e) { confirmSelection(appWidgetId, e.id) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun confirmSelection(appWidgetId: Int, eventId: Long) {
        val appCtx = applicationContext
        kotlinx.coroutines.GlobalScope.launch {
            val mgr = GlanceAppWidgetManager(appCtx)
            val glanceId = mgr.getGlanceIdBy(appWidgetId)
            android.util.Log.d("CountDoodleWidget",
                "confirmSelection writing eventId=$eventId for glanceId=$glanceId")
            SingleEventWidgetConfig.write(appCtx, glanceId, eventId)
            // Triggering update so the composable observes the new state right away;
            // recomposition reads the eventId via currentState<Preferences>().
            SingleEventWidget().update(appCtx, glanceId)
        }
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
        )
        finish()
    }
}

@Composable
private fun EventOption(event: Event, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(event.displayColorArgb)),
            contentAlignment = Alignment.Center,
        ) { Text(event.displayEmoji, fontSize = 20.sp) }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(event.name, fontWeight = FontWeight.SemiBold)
        }
    }
}

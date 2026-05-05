package org.secondthought.countdoodle.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.secondthought.countdoodle.data.Event
import org.secondthought.countdoodle.util.formatCountdown
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    vm: EventListViewModel = viewModel(factory = EventListViewModel.Factory),
) {
    val events by vm.events.collectAsState()
    val zone = ZoneId.systemDefault()
    // Tick once per minute so "days left" rolls over at local midnight without
    // wasting cycles per-second.
    val nowMs by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(60_000L)
        }
    }
    val today = Instant.ofEpochMilli(nowMs).atZone(zone).toLocalDate()
    val (upcoming, past) = events.partition { e ->
        val eDay = Instant.ofEpochMilli(e.dateEpochMillis).atZone(zone).toLocalDate()
        !eDay.isBefore(today)
    }
    val pastDescending = past.sortedByDescending { it.dateEpochMillis }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Count Doodle") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        },
    ) { padding ->
        if (events.isEmpty()) {
            EmptyState(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 4.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp,
                    start = 14.dp,
                    end = 14.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(upcoming, key = { it.id }) { e ->
                    EventRow(e, today, zone) { onOpen(e.id) }
                }
                if (pastDescending.isNotEmpty()) {
                    item { SectionHeader("PAST") }
                    items(pastDescending, key = { "p${it.id}" }) { e ->
                        EventRow(e, today, zone) { onOpen(e.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun EventRow(event: Event, today: LocalDate, zone: ZoneId, onClick: () -> Unit) {
    val baseColor = Color(event.displayColorArgb)
    val rightColor = baseColor.shadeBy(0.55f)
    val eventDay = Instant.ofEpochMilli(event.dateEpochMillis).atZone(zone).toLocalDate()
    val days = ChronoUnit.DAYS.between(today, eventDay).toInt()
    val isPast = days < 0
    val number = if (isPast) -days else days
    val unit = when {
        number == 0 -> "today"
        number == 1 && isPast -> "day ago"
        number == 1 -> "day left"
        isPast -> "days ago"
        else -> "days left"
    }
    val subtitle = eventDay.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left section (~70% of width) — emoji + name + date subtitle.
        Row(
            modifier = Modifier
                .weight(0.70f)
                .fillMaxWidth()
                .background(baseColor)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = event.displayEmoji, fontSize = 32.sp)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = event.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    maxLines = 1,
                )
            }
        }
        // Right section (~30%) — count + label, on a slightly darker shade.
        Column(
            modifier = Modifier
                .weight(0.30f)
                .fillMaxWidth()
                .background(rightColor)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (number == 0) "—" else number.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
            )
            Text(
                text = unit,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No events yet", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(
                "Tap + to add your first countdown",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Multiplies RGB channels by [factor], preserving alpha. factor < 1 darkens. */
private fun Color.shadeBy(factor: Float): Color = Color(
    red = (red * factor).coerceIn(0f, 1f),
    green = (green * factor).coerceIn(0f, 1f),
    blue = (blue * factor).coerceIn(0f, 1f),
    alpha = alpha,
)

package com.awhogue.countdoodle.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.awhogue.countdoodle.data.Event
import com.awhogue.countdoodle.util.CountdownParts
import com.awhogue.countdoodle.util.countdownParts
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EventDetailScreen(
    eventId: Long,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    vm: EventDetailViewModel = viewModel(factory = EventDetailViewModel.Factory),
) {
    LaunchedEffect(eventId) { vm.setId(eventId) }
    val event by vm.event.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val ev = event
        if (ev != null) {
            // Background: photo (with scrim) or solid event color.
            if (ev.photoUri != null) {
                AsyncImage(
                    model = ev.photoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color(ev.displayColorArgb)))
            }

            DetailContent(ev)
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .align(Alignment.TopStart),
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        IconButton(
            onClick = onEdit,
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .align(Alignment.TopEnd),
        ) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White)
        }
    }
}

@Composable
private fun DetailContent(ev: Event) {
    val zone = ZoneId.systemDefault()
    val nowMs by produceState(initialValue = System.currentTimeMillis(), ev.id) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val parts = countdownParts(nowMs, ev.dateEpochMillis, zone)
    val subtitle = formatSubtitle(ev, zone)

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Big emoji disc — semi-transparent white circle on the colored background.
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = ev.displayEmoji, fontSize = 120.sp)
        }

        Spacer(Modifier.height(28.dp))
        Text(
            text = ev.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
        )

        Spacer(Modifier.height(24.dp))
        CountdownPill(parts)
    }
}

@Composable
private fun CountdownPill(parts: CountdownParts) {
    // Show only the leading non-zero unit families: drop empty leading sections,
    // but always include at least seconds at the tail.
    val units = buildList {
        if (parts.months > 0) add(parts.months to "months")
        if (parts.months > 0 || parts.days > 0) add(parts.days to "days")
        if (parts.months > 0 || parts.days > 0 || parts.hours > 0) add(parts.hours to "hours")
        if (parts.months > 0 || parts.days > 0 || parts.hours > 0 || parts.minutes > 0)
            add(parts.minutes to "minutes")
        add(parts.seconds to "seconds")
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.20f))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        units.forEachIndexed { i, (value, label) ->
            if (i > 0) Spacer(Modifier.width(18.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                )
                Text(
                    text = label.let {
                        if (value == 1) it.removeSuffix("s") else it
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                )
            }
        }
    }
}

private fun formatSubtitle(ev: Event, zone: ZoneId): String {
    val z = Instant.ofEpochMilli(ev.dateEpochMillis).atZone(zone)
    return if (ev.hasTime) {
        z.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy HH:mm"))
    } else {
        z.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy"))
    }
}

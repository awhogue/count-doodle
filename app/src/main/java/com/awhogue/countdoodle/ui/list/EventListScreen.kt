package com.awhogue.countdoodle.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.awhogue.countdoodle.data.Event
import com.awhogue.countdoodle.ui.components.LiveCountdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    vm: EventListViewModel = viewModel(factory = EventListViewModel.Factory),
) {
    val events by vm.events.collectAsState()

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
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(events, key = { it.id }) { event ->
                    EventRow(event, onClick = { onOpen(event.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No events yet", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Tap + to add your first countdown",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EventRow(event: Event, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(event.displayColorArgb))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(event.displayEmoji, fontSize = 26.sp)
        }
        Column(modifier = Modifier.padding(start = 16.dp).fillMaxWidth()) {
            Text(event.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            LiveCountdownText(
                targetEpochMillis = event.dateEpochMillis,
                hasTime = event.hasTime,
                color = Color.White.copy(alpha = 0.92f),
            )
        }
    }
}

package com.awhogue.countdoodle.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.awhogue.countdoodle.ui.components.LiveCountdownText

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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(ev.displayEmoji, fontSize = 64.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = ev.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                )
                Spacer(Modifier.height(24.dp))
                LiveCountdownText(
                    targetEpochMillis = ev.dateEpochMillis,
                    hasTime = ev.hasTime,
                    color = Color.White,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
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

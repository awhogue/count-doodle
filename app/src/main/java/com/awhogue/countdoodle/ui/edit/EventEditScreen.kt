package com.awhogue.countdoodle.ui.edit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val emojiOptions = listOf(
    "🎉","🎂","✈️","💍","🎓","🌴","🎄","🎁","🚀","❤️","🏖️","🏔️","🍰","🎵","📅","🏆"
)
private val colorOptions = listOf(
    0xFF1F6FEB.toInt(), 0xFFE85D75.toInt(), 0xFF34A853.toInt(), 0xFFFFB300.toInt(),
    0xFF8E44AD.toInt(), 0xFF16A085.toInt(), 0xFFD35400.toInt(), 0xFF2C3E50.toInt(),
    0xFFC0392B.toInt(), 0xFF2980B9.toInt(), 0xFF27AE60.toInt(), 0xFFF39C12.toInt(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    eventId: Long?,
    onDone: () -> Unit,
    vm: EventEditViewModel = viewModel(factory = EventEditViewModel.Factory),
) {
    LaunchedEffect(eventId) { vm.loadIfExisting(eventId) }
    val state by vm.form.collectAsState()
    val ctx = LocalContext.current

    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                ctx.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) { /* gallery picker may not grant */ }
            vm.setPhotoUri(uri.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.id == 0L) "New event" else "Edit event") },
                actions = {
                    if (state.id != 0L) {
                        IconButton(onClick = { vm.delete(onDone) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = vm::setName,
                label = { Text("Event name") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedButton(onClick = {
                DatePickerDialog(
                    ctx,
                    { _, y, m, d -> vm.setDate(LocalDate.of(y, m + 1, d)) },
                    state.date.year, state.date.monthValue - 1, state.date.dayOfMonth,
                ).show()
            }) {
                Text(state.date.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Include time", modifier = Modifier.padding(end = 8.dp))
                Switch(checked = state.hasTime, onCheckedChange = vm::setHasTime)
                if (state.hasTime) {
                    OutlinedButton(
                        modifier = Modifier.padding(start = 12.dp),
                        onClick = {
                            TimePickerDialog(
                                ctx,
                                { _, h, m -> vm.setTime(LocalTime.of(h, m)) },
                                state.time.hour, state.time.minute, false,
                            ).show()
                        }
                    ) {
                        Text(state.time.format(DateTimeFormatter.ofPattern("h:mm a")))
                    }
                }
            }

            Text("Emoji", fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(emojiOptions) { emoji ->
                    val selected = state.emoji == emoji
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (selected) Color.LightGray else Color.Transparent)
                            .clickable { vm.setEmoji(if (selected) null else emoji) },
                        contentAlignment = Alignment.Center,
                    ) { Text(emoji, fontSize = 24.sp) }
                }
            }

            Text("Background color", fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(colorOptions) { argb ->
                    val selected = state.backgroundColorArgb == argb
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(argb))
                            .clickable { vm.setColor(if (selected) null else argb) },
                    )
                }
            }

            Text("Photo", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = {
                    pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("Pick from gallery") }
                if (state.photoUri != null) {
                    OutlinedButton(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = { vm.setPhotoUri(null) }
                    ) { Text("Remove") }
                }
            }
            if (state.photoUri != null) {
                AsyncImage(
                    model = state.photoUri,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }

            Button(
                onClick = { vm.save(onDone) },
                enabled = state.isValid,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) { Text("Save") }
        }
    }
}

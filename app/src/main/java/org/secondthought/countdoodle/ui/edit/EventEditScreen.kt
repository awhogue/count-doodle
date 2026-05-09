package org.secondthought.countdoodle.ui.edit

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.emoji2.emojipicker.EmojiPickerView
import org.secondthought.countdoodle.util.searchEmojis
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    var showEmojiPicker by remember { mutableStateOf(false) }

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.25f))
                        .clickable { showEmojiPicker = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.emoji ?: state.name.ifBlank { "🙂" }.let {
                            if (state.emoji != null) state.emoji!! else org.secondthought.countdoodle.data.Defaults.emojiFor(it)
                        },
                        fontSize = 28.sp,
                    )
                }
                if (state.emoji != null) {
                    OutlinedButton(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = { vm.setEmoji(null) },
                    ) { Text("Clear") }
                }
            }

            Text("Background color", fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(colorOptions) { argb ->
                    val selected = state.backgroundColorArgb == argb
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .then(
                                if (selected) Modifier.border(3.dp, Color.Black, CircleShape)
                                else Modifier
                            )
                            .background(Color(argb))
                            .clickable { vm.setColor(if (selected) null else argb) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                            )
                        }
                    }
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

        if (showEmojiPicker) {
            Dialog(
                onDismissRequest = { showEmojiPicker = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(min = 360.dp, max = 560.dp),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 6.dp,
                ) {
                    var query by remember { mutableStateOf("") }
                    val results by remember(query) {
                        derivedStateOf { searchEmojis(query) }
                    }
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Search") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        if (query.isBlank()) {
                            AndroidView(
                                factory = { context ->
                                    EmojiPickerView(context).apply {
                                        setOnEmojiPickedListener { picked ->
                                            vm.setEmoji(picked.emoji)
                                            showEmojiPicker = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else if (results.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) { Text("No matches", fontSize = 14.sp) }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 48.dp),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                items(results, key = { it.emoji }) { entry ->
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clickable {
                                                vm.setEmoji(entry.emoji)
                                                showEmojiPicker = false
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(entry.emoji, fontSize = 28.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

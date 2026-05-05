package org.secondthought.countdoodle.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.secondthought.countdoodle.CountDoodleApp
import org.secondthought.countdoodle.data.Event
import org.secondthought.countdoodle.data.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class EditFormState(
    val id: Long = 0,
    val name: String = "",
    val date: LocalDate = LocalDate.now(),
    val hasTime: Boolean = false,
    val time: LocalTime = LocalTime.of(9, 0),
    val emoji: String? = null,
    val backgroundColorArgb: Int? = null,
    val photoUri: String? = null,
) {
    val isValid: Boolean get() = name.isNotBlank()
}

class EventEditViewModel(
    private val repo: EventRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    private val _form = MutableStateFlow(EditFormState())
    val form: StateFlow<EditFormState> = _form.asStateFlow()

    fun loadIfExisting(id: Long?) {
        if (id == null || id == 0L) return
        viewModelScope.launch {
            val e = repo.getById(id) ?: return@launch
            val zdt = Instant.ofEpochMilli(e.dateEpochMillis).atZone(zone)
            _form.value = EditFormState(
                id = e.id,
                name = e.name,
                date = zdt.toLocalDate(),
                hasTime = e.hasTime,
                time = zdt.toLocalTime().withSecond(0).withNano(0),
                emoji = e.emoji,
                backgroundColorArgb = e.backgroundColorArgb,
                photoUri = e.photoUri,
            )
        }
    }

    fun setName(v: String) = _form.update { it.copy(name = v) }
    fun setDate(v: LocalDate) = _form.update { it.copy(date = v) }
    fun setHasTime(v: Boolean) = _form.update { it.copy(hasTime = v) }
    fun setTime(v: LocalTime) = _form.update { it.copy(time = v) }
    fun setEmoji(v: String?) = _form.update { it.copy(emoji = v) }
    fun setColor(argb: Int?) = _form.update { it.copy(backgroundColorArgb = argb) }
    fun setPhotoUri(uri: String?) = _form.update { it.copy(photoUri = uri) }

    fun save(onSaved: () -> Unit) {
        val s = _form.value
        if (!s.isValid) return
        viewModelScope.launch {
            val time = if (s.hasTime) s.time else LocalTime.MIDNIGHT
            val instant = s.date.atTime(time).atZone(zone).toInstant()
            repo.upsert(
                Event(
                    id = s.id,
                    name = s.name.trim(),
                    dateEpochMillis = instant.toEpochMilli(),
                    hasTime = s.hasTime,
                    emoji = s.emoji,
                    backgroundColorArgb = s.backgroundColorArgb,
                    photoUri = s.photoUri,
                )
            )
            onSaved()
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = _form.value.id
        if (id == 0L) { onDone(); return }
        viewModelScope.launch {
            repo.delete(id)
            onDone()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CountDoodleApp
                EventEditViewModel(app.repository)
            }
        }
    }
}

package com.awhogue.countdoodle.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.awhogue.countdoodle.CountDoodleApp
import com.awhogue.countdoodle.data.Event
import com.awhogue.countdoodle.data.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class EventDetailViewModel(
    private val repo: EventRepository,
) : ViewModel() {

    private val idFlow = MutableStateFlow<Long?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val event: StateFlow<Event?> = idFlow
        .flatMapLatest { id -> if (id == null) flow { emit(null) } else repo.observeById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setId(id: Long) { idFlow.value = id }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CountDoodleApp
                EventDetailViewModel(app.repository)
            }
        }
    }
}

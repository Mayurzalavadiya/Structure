package com.example.app.ui.viewmodel

import androidx.lifecycle.*
import com.example.app.data.pojo.dataclass.Event
import com.example.app.data.repository.RoomDatabaseEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: RoomDatabaseEventRepository
) : ViewModel() {

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    var isSortAscending: Boolean = true

    fun insertEvent(user: Event) = viewModelScope.launch {
        repository.insertEvent(user)
        getAllEvents()
    }

    fun updateEvent(user: Event) = viewModelScope.launch {
        repository.updateEvent(user)
        getAllEvents()
    }

    fun deleteEvent(user: Event) = viewModelScope.launch {
        repository.deleteEvent(user)
        getAllEvents()
    }

    fun getEventById(id: Int): LiveData<Event?> {
        return liveData {
            emit(repository.getEventById(id))
        }
    }

    fun getAllEvents() {
        viewModelScope.launch {
            val allEvents = repository.getAllEvents()
           /* val sortedUsers = if (isSortAscending) {
                allUsers.sortedBy { it.title }
            } else {
                allUsers.sortedByDescending { it.title }
            }*/
            _events.postValue(allEvents)
        }
    }

    fun toggleSortOrder(isAscending: Boolean) {
        isSortAscending = isAscending
        getAllEvents()
    }
}

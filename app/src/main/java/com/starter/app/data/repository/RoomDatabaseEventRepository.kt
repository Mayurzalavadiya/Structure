package com.starter.app.data.repository

import com.starter.app.data.dao.EventDao
import com.starter.app.data.pojo.dataclass.Event
import javax.inject.Inject

class RoomDatabaseEventRepository @Inject constructor(private val userDao: EventDao) {

    // Insert user
    suspend fun insertEvent(user: Event) {
        userDao.insertEvent(user)
    }

    // Update user
    suspend fun updateEvent(user: Event) {
        userDao.updateEvent(user)
    }

    // Delete user
    suspend fun deleteEvent(user: Event) {
        userDao.deleteEvent(user)
    }

    // Get all users (returns list, not LiveData)
    suspend fun getAllEvents(): List<Event> {
        return userDao.getAllEvents()
    }

    // Get user by ID
    suspend fun getEventById(id: Int): Event? {
        return userDao.getEventById(id)
    }
}

package com.example.app.data.dao

import androidx.room.*
import com.example.app.data.pojo.dataclass.Event

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM event_table")
    suspend fun getAllEvents(): List<Event>

    @Query("SELECT * FROM event_table WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Int): Event?

}

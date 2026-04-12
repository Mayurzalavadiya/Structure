package com.starter.app.data.pojo.dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val image: String?
)

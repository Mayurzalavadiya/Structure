package com.example.app.data.repository

import com.example.app.data.pojo.DataWrapper
import com.example.app.data.pojo.User
import com.example.app.data.pojo.request.LoginRequest
import com.example.app.data.pojo.response.UsersResponse

interface UserRepository {
    /**
     * Same name of API nca
     */
    suspend fun login(request: LoginRequest): DataWrapper<User>
    suspend fun getUser(limit: Int, skip: Int): UsersResponse

}
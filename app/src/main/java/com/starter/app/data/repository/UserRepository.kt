package com.starter.app.data.repository

import com.starter.app.data.pojo.DataWrapper
import com.starter.app.data.pojo.User
import com.starter.app.data.pojo.request.LoginRequest
import com.starter.app.data.pojo.response.UsersResponse

interface UserRepository {
    /**
     * Same name of API nca
     */
    suspend fun login(request: LoginRequest): DataWrapper<User>
    suspend fun getUser(limit: Int, skip: Int): UsersResponse

}
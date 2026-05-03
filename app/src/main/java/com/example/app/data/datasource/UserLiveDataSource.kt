package com.example.app.data.datasource

import com.example.app.data.pojo.DataWrapper
import com.example.app.data.pojo.User
import com.example.app.data.pojo.request.LoginRequest
import com.example.app.data.pojo.response.UsersResponse
import com.example.app.data.repository.UserRepository
import com.example.app.data.service.AuthenticationService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLiveDataSource @Inject constructor(private val authenticationService: AuthenticationService) :
    BaseDataSource(), UserRepository {

    override suspend fun login(request: LoginRequest): DataWrapper<User> {
        return execute { authenticationService.login(request) }
    }

    override suspend fun getUser(limit: Int, skip: Int): UsersResponse {
        return authenticationService.getUser(limit, skip)
    }

}

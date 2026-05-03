package com.example.app.data.service

import com.example.app.data.URLFactory
import com.example.app.data.pojo.ResponseBody
import com.example.app.data.pojo.User
import com.example.app.data.pojo.request.LoginRequest
import com.example.app.data.pojo.response.UsersResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthenticationService {

    /**
     * API calling url and method
     */
    @POST(URLFactory.Method.LOGIN)
    suspend fun login(@Body request: LoginRequest): ResponseBody<User>

    @GET("users")
    suspend fun getUser(@Query("limit") limit: Int, @Query("skip") skip: Int): UsersResponse

}
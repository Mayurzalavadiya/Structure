package com.example.app.data

import okhttp3.HttpUrl

object URLFactory {

    // server details
    private const val IS_LOCAL = true
    private const val SCHEME = "http"
    private val HOST = if (IS_LOCAL) "18.169.15.187" else "skkyn.com"
    private val API_PATH = if (IS_LOCAL) "api/v1/" else "websitedata/api/v2/"
    private val PORT = if (IS_LOCAL) 8082 else 8082

    fun provideHttpUrl(): HttpUrl {
        return HttpUrl.Builder()
                .scheme(SCHEME)
                .host(HOST)
                .port(PORT)
                .addPathSegments(API_PATH)
                .build()
    }


    // API Methods
    object Method {
        const val LOGIN = "user/login"
        const val USERS = "users"
    }

}

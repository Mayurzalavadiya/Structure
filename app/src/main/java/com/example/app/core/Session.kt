package com.example.app.core

import com.example.app.data.pojo.User

interface Session {

    var apiKey: String

    var userSession: String

    var userId: String

    val deviceId: String

    var user: User?

    var language: String

    var isLogin: Boolean

    var isTutorial: Boolean

    var deviceToken: String

    fun clearSession()

    fun clearSessionKey(vararg key: String)

    fun getFirebaseDeviceId(callback: (deviceID: String) -> Unit)

    companion object {
        const val API_KEY = "api-key"
        const val USER_SESSION = "token"
        const val USER_ID = "USER_ID"
        const val DEVICE_TYPE = "A"
        const val LANGUAGE = "accept-language"
        const val DEVICE_TOKEN = "device-token"
        const val IS_LOGIN = "is-login"

        const val TUTORIAL = "tutorial"
    }
}

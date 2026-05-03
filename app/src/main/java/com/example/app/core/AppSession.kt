package com.example.app.core

import android.content.Context
import android.provider.Settings
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.example.app.data.pojo.User
import com.example.app.di.DiConstants
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AppSession @Inject
constructor(
    private val appPreferences: AppPreferences,
    private val context: Context,
    @Named(DiConstants.API_KEY)
    override var apiKey: String
) : Session {

    private val gson: Gson = Gson()

    override var user: User? = null
        get() {
            if (field == null) {
                val userJSON = appPreferences.getString(USER_JSON)
                field = gson.fromJson(userJSON, User::class.java)
            }
            return field
        }
        set(value) {
            field = value
            val userJson = gson.toJson(value)
            if (userJson != null)
                appPreferences.putString(USER_JSON, userJson)
        }

    override var isLogin: Boolean
        get() = appPreferences.getBoolean(Session.IS_LOGIN)
        set(isLogin) = appPreferences.putBoolean(Session.IS_LOGIN, isLogin)

    override var deviceToken: String
        get() = appPreferences.getString(Session.DEVICE_TOKEN)
        set(value) = appPreferences.putString(Session.DEVICE_TOKEN, value)

    override var isTutorial: Boolean
        get() = appPreferences.getBoolean(Session.TUTORIAL)
        set(isTutorial) = appPreferences.putBoolean(Session.TUTORIAL, isTutorial)

    override var userSession: String
        get() = appPreferences.getString(Session.USER_SESSION)
        set(userSession) = appPreferences.putString(Session.USER_SESSION, userSession)


    override var userId: String
        get() = appPreferences.getString(Session.USER_ID)
        set(userId) = appPreferences.putString(Session.USER_ID, userId)

    override fun getFirebaseDeviceId(callback: (deviceID: String) -> Unit) {
        FirebaseApp.initializeApp(context)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                deviceToken = task.result
                callback.invoke(task.result)
            } else {
                callback.invoke("")
            }
        }
    }

    override/* open below comment after Firebase integration *///token = FirebaseInstanceId.getInstance().getToken();
    val deviceId: String
        get() {
            var token = ""
            if (token.isEmpty())
                token = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

            return token
        }

    override//  return StringUtils.equalsIgnoreCase(appPreferences.getString(Common.LANGUAGE), "ar") ? LANGUAGE_ARABIC : LANGUAGE_ENGLISH;
    val language: String
        get() = "en"

    override fun clearSession() {
        appPreferences.clearAll()
    }

    override fun clearSessionKey(vararg key: String) {
        key.forEach {appPreferences.clear(it) }
    }


    companion object {
        const val USER_JSON = "user_json"
    }
}

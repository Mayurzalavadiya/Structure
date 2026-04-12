package com.starter.app.data.pojo.request

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("device_id")
    var deviceId: String,

    @SerializedName("device_type")
    var deviceType: String,

    @SerializedName("social_id")
    var socialId: String? = null,

    @SerializedName("country_code")
    var countryCode: String? = null,

    @SerializedName("phone")
    var phone: String? = null,

    @SerializedName("email")
    var email: String? = null,

    @SerializedName("password")
    var password: String? = null,

    @SerializedName("latitude")
    var latitude: String? = null,

    @SerializedName("longitude")
    var longitude: String? = null,

    @SerializedName("language")
    var language: String? = null,

    @SerializedName("app_version")
    var appVersion: String? = null,

    @SerializedName("uuid")
    var uuid: String? = null,

    @SerializedName("os_version")
    var osVersion: String? = null,

    @SerializedName("version_name")
    var versionName: String? = null,

    @SerializedName("device_name")
    var deviceName: String? = null,

    @SerializedName("model_name")
    var modelName: String? = null,

    @SerializedName("ip")
    var ip: String? = null
)
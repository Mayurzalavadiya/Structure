package com.starter.app.ui.fcm

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PushNotification(
    val title: String,
    val body: String,
    val tag: String,
    val id: String?= null,
    val initiativeId: String?= null,
    val commentReplyId: String? = null
) : Parcelable


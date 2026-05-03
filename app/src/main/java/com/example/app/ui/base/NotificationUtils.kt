package com.example.app.ui.base

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.app.R

object NotificationUtils {

    private const val CHANNEL_ID = "event_notifications"
    private const val CHANNEL_NAME = "Event Notifications"

    fun showNotification(context: Context,image:String, title: String, message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled events"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Convert image string to drawable resource ID
        val iconResId = context.resources.getIdentifier(image, "drawable", context.packageName)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(if (iconResId != 0) iconResId else R.mipmap.ic_launcher) // fallback
            .setAutoCancel(true)
            .build()


        val id = System.currentTimeMillis().toInt() // unique ID for multiple notifications
        notificationManager.notify(id, notification)
    }
}

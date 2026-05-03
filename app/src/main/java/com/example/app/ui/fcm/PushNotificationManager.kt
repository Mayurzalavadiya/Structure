package com.example.app.ui.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.app.R

class PushNotificationManager(val context: Context) {

    companion object {
        const val CHANNEL_NAME_GENERAL = "General Notifications"
        const val CHANNEL_ID_GENERAL = "General"
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                CHANNEL_NAME_GENERAL,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for general notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String, pendingIntent: PendingIntent?) {
        val notificationId = System.currentTimeMillis().toInt()
        val builder = createBasicNotification(title, message).apply {
            setAutoCancel(true)
            pendingIntent?.let { setContentIntent(it) }
        }

        notificationManager.notify(notificationId, builder.build())
    }


    private fun createBasicNotification(title: String, message: String): NotificationCompat.Builder {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        return NotificationCompat.Builder(context, CHANNEL_ID_GENERAL).apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle(title)
            setContentText(message)
            setStyle(NotificationCompat.BigTextStyle().bigText(message))
            setSound(defaultSoundUri)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setDefaults(Notification.DEFAULT_ALL)
        }
    }
}

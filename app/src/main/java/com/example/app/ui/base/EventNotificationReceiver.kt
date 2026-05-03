package com.example.app.ui.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class EventNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val image = intent.getStringExtra("image") ?: "Event Reminder"
        val title = intent.getStringExtra("title") ?: "Event Reminder"
        val description = intent.getStringExtra("description") ?: "You have an event"
        NotificationUtils.showNotification(context, image,title, description)
    }
}

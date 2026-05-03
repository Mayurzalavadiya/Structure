package com.example.app.ui.fcm

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.example.app.core.Session
import com.example.app.ui.activity.HomeActivity
import com.example.app.ui.activity.IsolatedActivity
import com.example.app.ui.fragment.AddEventFragment
import com.example.app.ui.manager.ActivityStarter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FirebasePushNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var session: Session

    @Inject
    lateinit var appNotificationManager: PushNotificationManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        token.let {
            session.deviceToken = it
        }
        println("token --> $token")
        Log.e(TAG, "onNewToken: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e(
            TAG,
            "onMessageReceived notification ${remoteMessage}"
        )
        try {
            Log.e(
                TAG,
                "onMessageReceived notificationData ${remoteMessage.data}"
            )
            remoteMessage.data["metaData"]?.let {
                val data =
                    Gson().fromJson(remoteMessage.data["metaData"], PushNotification::class.java)

                val firebaseNotification = PushNotification(
                    title = data.title,
                    body = data.body,
                    tag = data.tag,
                    id = data.id,
                    initiativeId = data.initiativeId,
                    commentReplyId = data.commentReplyId,
                )
                handlePushNotification(firebaseNotification)
            }

        } catch (e: Exception) {
            println("error -->${e.localizedMessage}")
        }
    }


    private fun sendNotificationToActivity() {
        val intent = Intent("com.sicseed.app.UPDATE_UI")
        intent.putExtra("action", "showCount")
        intent.putExtra("count", "0")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun sendChatNotificationToActivity() {
        val intent = Intent("com.sicseed.app.UPDATE_UI")
        intent.putExtra("action", "showCount")
        intent.putExtra("chatCount", "3")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }


    private fun handlePushNotification(pushNotification: PushNotification) {
        try {
            val pendingIntent: PendingIntent? =
//                when (pushNotification.tag) {
//                    PushNotificationType.COMMENTS.type, PushNotificationType.COMMENTS_REPLY.type -> {
//                        createPendingIntentForCommentsRequest(pushNotification)
//                    }



//                    else -> createPendingIntentForHome(pushNotification)
                createPendingIntentForHome(pushNotification)
//                }

            appNotificationManager.showNotification(
                title = pushNotification.title,
                message = pushNotification.body,
                pendingIntent = pendingIntent
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //HomePage
    private fun createPendingIntentForHome(pushNotification: PushNotification): PendingIntent? {
        val resultIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("NOTIFICATION", pushNotification)  // Pass PushNotification data
        }
        return TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(
                Random.nextInt(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }



    //Comments and Comments reply
    private fun createPendingIntentForCommentsRequest(pushNotification: PushNotification): PendingIntent? {
        // Create a Bundle with the required data

        /*val data = GetNotificationResponse.NotificationItem.MetaData(
            id = pushNotification.id?.toInt(),
            initiativeId = pushNotification.initiativeId?.toInt(),
            replyId = pushNotification.commentReplyId?.toInt()
        )*/

        // Intent to start the HomeActivity
        val homeIntent = Intent(this, HomeActivity::class.java)

        // Intent to start IsolatedActivity with CommentsFragment
        val fragmentIntent = Intent(this, IsolatedActivity::class.java).apply {
            putExtra(ActivityStarter.ACTIVITY_FIRST_PAGE, AddEventFragment::class.java)
//            putExtra(Keys.META_DATA, data) // Pass the Bundle with the Intent
        }

        return TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(homeIntent)
            addNextIntent(fragmentIntent)
            getPendingIntent(
                Random.nextInt(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun handleIntent(intent: Intent?) {
        try {
            intent?.extras?.let {
                val remoteMessage = RemoteMessage.Builder("FirebasePushNotificationService")
                    .also { builder ->
                        for (key in it.keySet()) {
                            it.getString(key)?.let { value ->
                                builder.addData(key, value)
                            }
                        }
                    }.build()
                onMessageReceived(remoteMessage)
            } ?: super.handleIntent(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling intent", e)
            super.handleIntent(intent)
        }
    }


    companion object {
        private const val TAG = "FirebasePushNotificationService.kt"
    }
}
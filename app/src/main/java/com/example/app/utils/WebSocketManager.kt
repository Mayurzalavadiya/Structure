package com.example.app.utils


import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

class WebSocketManager {

    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()

    fun connect(userId: String, onMessage: (String) -> Unit) {
        val url = "wss://e593-110-227-233-180.ngrok-free.app/ws/websocket" // STOMP endpoint

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)

        // 1️⃣ Handle lifecycle events
        val lifecycleDisposable = stompClient!!
            .lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> Log.d("STOMP", "Connection Opened")
                    LifecycleEvent.Type.ERROR -> Log.e("STOMP", "Connection Error", lifecycleEvent.exception)
                    LifecycleEvent.Type.CLOSED -> Log.d("STOMP", "Connection Closed")
                    else -> {}
                }
            }, { throwable ->
                Log.e("STOMP", "Lifecycle Error", throwable)
            })

        compositeDisposable.add(lifecycleDisposable)

        stompClient?.connect()

        // 2️⃣ Subscribe to topic after connection
        val topicPath = "/topic/notifications/$userId"
        val topicDisposable = stompClient!!
            .topic(topicPath)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage ->
                Log.d("STOMP", "Received: ${topicMessage.payload}")
                onMessage(topicMessage.payload)
            }, { error ->
                Log.e("STOMP", "Subscription error", error)
            })

        compositeDisposable.add(topicDisposable)
    }

    fun disconnect() {
        stompClient?.disconnect()
        compositeDisposable.clear()
    }
}


//how to use
/*

private val socketManager = WebSocketManager()


socketManager.connect(
userId = session.user?.userId.toString(),
onMessage = { message ->
    Log.d("WS_MESSAGE", "Received in callback: $message")
    runOnUiThread {
        showMessage(message)
    }
},
)

override fun onDestroy() {
    super.onDestroy()
    socketManager.disconnect()
}
*/

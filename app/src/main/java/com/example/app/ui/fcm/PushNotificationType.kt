package com.example.app.ui.fcm

enum class PushNotificationType(val type: String) {
    COMMENTS("comments"),///
    COMMENTS_REPLY("commentsReply"),///
    CHAT_REQUEST("chatRequest"),///
    INVESTMENT("investment"),
    NEW_POLL("newPoll"),
    TERMINATION_ALERT("terminationAlert"),
    INVESTOR_SUBMIT_FOR_LAUNCH_ALERT("investorSubmitForLaunchAlert"),
    READY_TO_SUBMIT("readyToSubmit"),
    FOUNDER_READY_FOR_LAUNCH("founderReadyForLaunch"),
    ADMIN_NOTIFICATION("adminNotification");


    companion object {
        fun getPushNotificationType(type: String?): PushNotificationType? {
            return PushNotificationType.entries.find { it.type == type }
        }
    }
}
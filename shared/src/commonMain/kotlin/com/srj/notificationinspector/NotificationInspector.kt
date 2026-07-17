package com.srj.notificationinspector

import com.srj.notificationinspector.model.NotificationLog

expect class PlatformNotificationPayload

fun interface NotificationReplayListener {
    fun onReplay(log: NotificationLog)
}

expect class NotificationInspector(context: PlatformContext) {
    fun capture(message: PlatformNotificationPayload)
    fun launch()
    fun replay(log: NotificationLog)
    fun shareText(text: String)

    companion object {
        var replayListener: NotificationReplayListener?
    }
}

package com.srj.notificationinspector

import com.srj.notificationinspector.model.NotificationLog

actual typealias PlatformNotificationPayload = Any

actual class NotificationInspector actual constructor(context: PlatformContext) {
    actual fun capture(message: Any) {
        // No-op stub for JVM/Desktop target
    }

    actual fun launch() {
        // No-op stub for JVM/Desktop target
    }

    actual fun replay(log: NotificationLog) {
        replayListener?.onReplay(log)
    }

    companion object {
        actual var replayListener: NotificationReplayListener? = null
    }
}

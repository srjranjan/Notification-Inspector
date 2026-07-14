package com.srj.notificationinspector

import com.srj.notificationinspector.model.NotificationLog

actual typealias PlatformNotificationPayload = Any

actual class NotificationInspector actual constructor(context: PlatformContext) {
    actual fun capture(message: PlatformNotificationPayload) {}
    actual fun launch() {}
    actual fun replay(log: NotificationLog) {}

    actual companion object {
        actual var replayListener: NotificationReplayListener? = null
    }
}

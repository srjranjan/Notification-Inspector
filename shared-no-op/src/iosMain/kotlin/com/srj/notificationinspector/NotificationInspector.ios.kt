@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.srj.notificationinspector

import com.srj.notificationinspector.model.NotificationLog
import platform.Foundation.NSDictionary

actual typealias PlatformNotificationPayload = NSDictionary

actual class NotificationInspector actual constructor(private val context: PlatformContext) {
    actual fun capture(message: NSDictionary) {}
    actual fun launch() {}
    actual fun replay(log: NotificationLog) {}
    actual fun shareText(text: String) {}

    actual companion object {
        actual var replayListener: NotificationReplayListener? = null
    }
}

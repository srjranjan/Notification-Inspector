package com.srj.notificationinspector

actual typealias PlatformNotificationPayload = Any

actual class NotificationInspector actual constructor(context: PlatformContext) {
    actual fun capture(message: PlatformNotificationPayload) {}
    actual fun launch() {}
}

package com.srj.notificationinspector

expect class PlatformNotificationPayload

expect class NotificationInspector(context: PlatformContext) {
    fun capture(message: PlatformNotificationPayload)
    fun launch()
}

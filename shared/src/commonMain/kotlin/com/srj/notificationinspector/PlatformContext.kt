package com.srj.notificationinspector

import com.srj.notificationinspector.repository.NotificationRepository

expect class PlatformContext

expect fun getNotificationRepository(context: PlatformContext): NotificationRepository

expect fun getCurrentTimeMillis(): Long


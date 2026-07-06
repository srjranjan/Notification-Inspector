package com.srj.notificationinspector

expect class PlatformContext

expect fun getNotificationRepository(context: PlatformContext): com.srj.notificationinspector.repository.NotificationRepository

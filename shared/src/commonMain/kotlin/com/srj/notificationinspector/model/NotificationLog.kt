package com.srj.notificationinspector.model

data class NotificationLog(
    val id: Long,
    val timestamp: Long,
    val title: String?,
    val body: String?,
    val rawPayload: String
)

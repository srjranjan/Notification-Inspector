package com.srj.notificationinspector.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_inspector_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val title: String?,
    val body: String?,
    val rawJsonPayload: String
)

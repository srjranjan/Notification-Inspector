package com.srj.notificationinspector.repository

import com.srj.notificationinspector.model.NotificationLog
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllLogs(): Flow<List<NotificationLog>>
    fun searchLogs(query: String): Flow<List<NotificationLog>>
    suspend fun getLogById(id: Long): NotificationLog?
    suspend fun insertLog(title: String?, body: String?, rawPayload: String)
    suspend fun clearAllLogs()
}

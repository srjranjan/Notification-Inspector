@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.srj.notificationinspector

import com.srj.notificationinspector.repository.NotificationRepository
import kotlinx.coroutines.flow.flowOf

actual class PlatformContext

actual fun getNotificationRepository(context: PlatformContext): NotificationRepository {
    return object : NotificationRepository {
        override fun getAllLogs() = flowOf(emptyList<com.srj.notificationinspector.model.NotificationLog>())
        override fun searchLogs(query: String) = flowOf(emptyList<com.srj.notificationinspector.model.NotificationLog>())
        override suspend fun getLogById(id: Long) = null
        override suspend fun insertLog(title: String?, body: String?, rawPayload: String) {}
        override suspend fun clearAllLogs() {}
        override suspend fun deleteLogById(id: Long) {}
    }
}

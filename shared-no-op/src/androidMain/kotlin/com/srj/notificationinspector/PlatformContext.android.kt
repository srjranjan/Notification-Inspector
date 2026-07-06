package com.srj.notificationinspector

actual class PlatformContext(val androidContext: android.content.Context)

actual fun getNotificationRepository(context: PlatformContext): com.srj.notificationinspector.repository.NotificationRepository {
    return object : com.srj.notificationinspector.repository.NotificationRepository {
        override fun getAllLogs() = kotlinx.coroutines.flow.flowOf(emptyList<com.srj.notificationinspector.model.NotificationLog>())
        override fun searchLogs(query: String) = kotlinx.coroutines.flow.flowOf(emptyList<com.srj.notificationinspector.model.NotificationLog>())
        override suspend fun getLogById(id: Long) = null
        override suspend fun insertLog(title: String?, body: String?, rawPayload: String) {}
        override suspend fun clearAllLogs() {}
    }
}

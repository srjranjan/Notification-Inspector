package com.srj.notificationinspector

import com.srj.notificationinspector.model.NotificationLog
import com.srj.notificationinspector.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

actual class PlatformContext

class InMemoryNotificationRepository : NotificationRepository {
    private val logs = MutableStateFlow<List<NotificationLog>>(emptyList())
    private var idCounter = 1L

    override fun getAllLogs(): Flow<List<NotificationLog>> = logs

    override fun searchLogs(query: String): Flow<List<NotificationLog>> {
        val cleanQuery = query.replace("%", "").lowercase()
        return logs.map { list ->
            list.filter {
                it.title?.lowercase()?.contains(cleanQuery) == true ||
                it.body?.lowercase()?.contains(cleanQuery) == true ||
                it.rawPayload.lowercase().contains(cleanQuery)
            }
        }
    }

    override suspend fun getLogById(id: Long): NotificationLog? {
        return logs.value.find { it.id == id }
    }

    override suspend fun insertLog(title: String?, body: String?, rawPayload: String): Long {
        val newId = idCounter++
        val newLog = NotificationLog(
            id = newId,
            timestamp = getCurrentTimeMillis(),
            title = title,
            body = body,
            rawPayload = rawPayload
        )
        logs.value = listOf(newLog) + logs.value
        return newId
    }

    override suspend fun clearAllLogs() {
        logs.value = emptyList()
    }
}

actual fun getNotificationRepository(context: PlatformContext): NotificationRepository {
    return InMemoryNotificationRepository()
}

actual fun getCurrentTimeMillis(): Long {
    return 1783382400000L
}

package com.srj.notificationinspector.db

import com.srj.notificationinspector.getCurrentTimeMillis
import com.srj.notificationinspector.model.NotificationLog
import com.srj.notificationinspector.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNotificationRepository(
    private val database: InspectorDatabase
) : NotificationRepository {

    private val dao = database.notificationDao()

    override fun getAllLogs(): Flow<List<NotificationLog>> {
        return dao.getAllLogs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchLogs(query: String): Flow<List<NotificationLog>> {
        return dao.searchLogs(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLogById(id: Long): NotificationLog? {
        return dao.getLogById(id)?.toDomain()
    }

    override suspend fun insertLog(title: String?, body: String?, rawPayload: String): Long {
        val entity = NotificationLogEntity(
            timestamp = getCurrentTimeMillis(),
            title = title,
            body = body,
            rawJsonPayload = rawPayload
        )
        return dao.insert(entity)
    }

    override suspend fun clearAllLogs() {
        dao.clearAllLogs()
    }

    override suspend fun deleteLogById(id: Long) {
        dao.deleteLogById(id)
    }

    // Mapper from Entity to Clean Domain model
    private fun NotificationLogEntity.toDomain(): NotificationLog {
        return NotificationLog(
            id = this.id,
            timestamp = this.timestamp,
            title = this.title,
            body = this.body,
            rawPayload = this.rawJsonPayload
        )
    }
}

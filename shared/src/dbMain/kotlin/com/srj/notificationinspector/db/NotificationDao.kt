package com.srj.notificationinspector.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_inspector_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<NotificationLogEntity>>

    @Query("SELECT * FROM notification_inspector_logs WHERE title LIKE :query OR body LIKE :query OR rawJsonPayload LIKE :query ORDER BY timestamp DESC")
    fun searchLogs(query: String): Flow<List<NotificationLogEntity>>

    @Query("SELECT * FROM notification_inspector_logs WHERE id = :id")
    suspend fun getLogById(id: Long): NotificationLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: NotificationLogEntity)

    @Query("DELETE FROM notification_inspector_logs")
    suspend fun clearAllLogs()
}

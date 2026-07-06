package com.srj.notificationinspector

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.srj.notificationinspector.db.InspectorDatabase
import com.srj.notificationinspector.db.InspectorDatabaseConstructor
import com.srj.notificationinspector.db.RoomNotificationRepository
import com.srj.notificationinspector.repository.NotificationRepository
import java.io.File

actual class PlatformContext

actual fun getNotificationRepository(context: PlatformContext): NotificationRepository {
    val homeDir = System.getProperty("user.home")
    val dbFile = File(homeDir, "notification_inspector.db")

    val database = Room.databaseBuilder<InspectorDatabase>(
        name = dbFile.absolutePath,
        factory = { InspectorDatabaseConstructor.initialize() }
    )
    .setDriver(BundledSQLiteDriver())
    .build()

    return RoomNotificationRepository(database)
}

actual fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}

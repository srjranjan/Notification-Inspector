package com.srj.notificationinspector

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.srj.notificationinspector.db.InspectorDatabase
import com.srj.notificationinspector.db.InspectorDatabaseConstructor
import com.srj.notificationinspector.db.RoomNotificationRepository
import com.srj.notificationinspector.repository.NotificationRepository

actual class PlatformContext(val androidContext: android.content.Context) {
    init {
        com.srj.notificationinspector.theme.ThemePreferences.context = androidContext.applicationContext
    }
}

actual fun getNotificationRepository(context: PlatformContext): NotificationRepository {
    val dbFile = context.androidContext.getDatabasePath("notification_inspector.db")
    val database = Room.databaseBuilder<InspectorDatabase>(
        context = context.androidContext.applicationContext,
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

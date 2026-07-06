@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.srj.notificationinspector

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.srj.notificationinspector.db.InspectorDatabase
import com.srj.notificationinspector.db.InspectorDatabaseConstructor
import com.srj.notificationinspector.db.RoomNotificationRepository
import com.srj.notificationinspector.repository.NotificationRepository
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970

actual class PlatformContext

actual fun getNotificationRepository(context: PlatformContext): NotificationRepository {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    val dbFilePath = documentDirectory?.path + "/notification_inspector.db"

    val database = Room.databaseBuilder<InspectorDatabase>(
        name = dbFilePath,
        factory = { InspectorDatabaseConstructor.initialize() }
    )
    .setDriver(BundledSQLiteDriver())
    .build()

    return RoomNotificationRepository(database)
}

actual fun getCurrentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

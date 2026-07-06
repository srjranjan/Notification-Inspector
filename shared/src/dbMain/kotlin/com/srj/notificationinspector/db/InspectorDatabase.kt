package com.srj.notificationinspector.db

import androidx.room.*

@Database(entities = [NotificationLogEntity::class], version = 1, exportSchema = false)
@ConstructedBy(InspectorDatabaseConstructor::class)
abstract class InspectorDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object InspectorDatabaseConstructor : RoomDatabaseConstructor<InspectorDatabase> {
    override fun initialize(): InspectorDatabase
}

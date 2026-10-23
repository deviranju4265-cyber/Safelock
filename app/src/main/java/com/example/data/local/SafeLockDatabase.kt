package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudentEntity::class,
        ScheduleEntity::class,
        ApprovedAppEntity::class,
        CachedRequestEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SafeLockDatabase : RoomDatabase() {
    abstract fun dao(): SafeLockDao

    companion object {
        @Volatile
        private var INSTANCE: SafeLockDatabase? = null

        fun getInstance(context: Context): SafeLockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafeLockDatabase::class.java,
                    "safelock_local.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

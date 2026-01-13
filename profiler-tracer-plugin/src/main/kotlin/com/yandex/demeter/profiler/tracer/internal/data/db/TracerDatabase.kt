package com.yandex.demeter.profiler.tracer.internal.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yandex.demeter.annotations.InternalDemeterApi

@InternalDemeterApi
@Database(
    entities = [TraceMetricRawEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TracerDatabase : RoomDatabase() {

    abstract fun traceMetricDao(): TraceMetricDao

    companion object {
        private const val DATABASE_NAME = "demeter_tracer.db"

        @Volatile
        private var instance: TracerDatabase? = null

        fun getInstance(context: Context): TracerDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): TracerDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                TracerDatabase::class.java,
                DATABASE_NAME
            ).fallbackToDestructiveMigration(true).build()
        }
    }
}

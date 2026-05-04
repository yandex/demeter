package com.yandex.demeter.profiler.coroutine.tracer.internal.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yandex.demeter.annotations.InternalDemeterApi

@InternalDemeterApi
@Database(
    entities = [CoroutineMetricRawEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CoroutineTracerDatabase : RoomDatabase() {

    abstract fun coroutineMetricDao(): CoroutineMetricDao

    companion object {
        private const val DATABASE_NAME = "demeter_coroutine_tracer.db"

        @Volatile
        private var instance: CoroutineTracerDatabase? = null

        fun getInstance(context: Context): CoroutineTracerDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): CoroutineTracerDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                CoroutineTracerDatabase::class.java,
                DATABASE_NAME
            ).fallbackToDestructiveMigration(true).build()
        }
    }
}

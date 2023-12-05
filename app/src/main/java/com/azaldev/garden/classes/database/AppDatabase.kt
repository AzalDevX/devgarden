package com.azaldev.garden.classes.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.azaldev.garden.classes.dao.AuthDao
import com.azaldev.garden.classes.entity.GlobalSettings
import com.azaldev.garden.classes.dao.GlobalSettingsDao
import com.azaldev.garden.classes.entity.Auth

@Database(entities = [GlobalSettings::class, Auth::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun GlobalSettingsDao(): GlobalSettingsDao
    abstract fun AuthDao(): AuthDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "garden_database"
                )
//                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
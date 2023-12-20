package com.azaldev.garden.classes.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.azaldev.garden.classes.dao.AuthDao
import com.azaldev.garden.classes.dao.GameDao
import com.azaldev.garden.classes.dao.GlobalSettingsDao
import com.azaldev.garden.classes.entity.GlobalSettings
import com.azaldev.garden.classes.entity.Auth
import com.azaldev.garden.classes.entity.Game

@Database(entities = [GlobalSettings::class, Auth::class, Game::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun GlobalSettingsDao(): GlobalSettingsDao
    abstract fun AuthDao(): AuthDao
    abstract fun GameDao(): GameDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
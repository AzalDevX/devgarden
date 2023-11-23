package com.azaldev.garden.classes.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.azaldev.garden.classes.entity.GlobalSettings

@Dao
interface GlobalSettingsDao {
    @Query("SELECT * FROM global_settings WHERE id = 1")
    fun getGlobalSettings(): GlobalSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGlobalSettings(globalSettings: GlobalSettings)

    @Query("DELETE FROM global_settings WHERE id = 1")
    fun deleteGlobalSettings()
}

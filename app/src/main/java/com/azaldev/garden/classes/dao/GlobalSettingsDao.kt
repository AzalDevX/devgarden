package com.azaldev.garden.classes.dao

import androidx.room.*
import com.azaldev.garden.classes.entity.GlobalSettings

@Dao
interface GlobalSettingsDao {
    @Query("SELECT * FROM GlobalSettings WHERE id = 1")
    fun getGlobalSettings(): GlobalSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGlobalSettings(globalSettings: GlobalSettings)

    @Delete
    fun deleteGlobalSettings(globalSettings: GlobalSettings)
}

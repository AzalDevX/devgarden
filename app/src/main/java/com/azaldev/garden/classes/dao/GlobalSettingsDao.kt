package com.azaldev.garden.classes.dao

import androidx.room.*
import com.azaldev.garden.classes.entity.GlobalSettings

@Dao
interface GlobalSettingsDao {
    @Query("SELECT * FROM GlobalSettings WHERE id = 1")
    fun get(): GlobalSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(globalSettings: GlobalSettings)

    @Delete
    fun delete(globalSettings: GlobalSettings)

    @Query("UPDATE GlobalSettings SET lang = :lang WHERE id = 1")
    fun updateLang(lang: String)

    @Query("UPDATE GlobalSettings SET theme = :theme WHERE id = 1")
    fun updateTheme(theme: String)

    @Query("UPDATE GlobalSettings SET student_groupname = :groupname, student_classcode = :classcode WHERE id = 1")
    fun updateStudent(groupname: String, classcode: String)

    fun getDefault(): GlobalSettings {
        val getDef = get()
        return getDef ?: run {
            // If the record doesn't exist, insert a default one
            val defaultSettings = GlobalSettings(id = 1, lang = "en", theme = "dark", student_groupname = null, student_classcode = null)
            insert(defaultSettings)
            defaultSettings
        }
    }
}

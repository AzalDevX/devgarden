package com.azaldev.garden.classes.dao

import androidx.room.*
import com.azaldev.garden.classes.entity.Auth

@Dao
interface AuthDao {

    @Query("SELECT * FROM Auth WHERE id = 1")
    fun getAuth(): Auth?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(newUser: Auth)

    @Query("SELECT * FROM Auth WHERE id = 1")
    fun get(): Auth?

    @Query("SELECT email FROM Auth WHERE id = 1")
    fun email(): String?

    @Query("SELECT password FROM Auth WHERE id = 1")
    fun password(): String?

    @Query("DELETE FROM Auth WHERE id = 1")
    fun delete(): Int?
}

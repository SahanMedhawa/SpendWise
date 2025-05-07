package com.example.spendwise.data

import androidx.room.*
import com.example.spendwise.models.Setting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<Setting>>

    @Query("SELECT * FROM settings WHERE `key` = :key")
    suspend fun getSetting(key: String): Setting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: Setting)

    @Update
    suspend fun updateSetting(setting: Setting)

    @Delete
    suspend fun deleteSetting(setting: Setting)

    @Query("DELETE FROM settings")
    suspend fun deleteAllSettings()
} 
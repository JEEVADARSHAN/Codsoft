package com.example.clock.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: Alarm)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(alarm: Alarm)

    @Query("DELETE FROM alarms WHERE name = :name AND hour = :hour AND minute = :minute")
    suspend fun delete(name: String, hour: Int, minute: Int)

    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): LiveData<List<Alarm>>
}
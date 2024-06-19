package com.example.clock.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData

class AlarmRepository(private val alarmDao: AlarmDao) {

    val allAlarms: LiveData<List<Alarm>> = alarmDao.getAllAlarms()

    suspend fun insert(alarm: Alarm) {
        alarmDao.insert(alarm)
    }

    suspend fun update(alarm: Alarm) {
        alarmDao.update(alarm)
    }

    suspend fun delete(name: String, hour: Int, minute: Int) {
        alarmDao.delete(name, hour, minute)
    }
}


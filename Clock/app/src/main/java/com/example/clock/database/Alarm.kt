package com.example.clock.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "alarms")
@TypeConverters(StringTypeConverter::class)
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val days: String,
    val hour: Int,
    val minute: Int,
    var isEnabled: Boolean
)
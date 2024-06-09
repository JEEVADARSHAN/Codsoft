package com.example.quote_app.Saving

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Quote::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun Dao(): QuoteDao
}
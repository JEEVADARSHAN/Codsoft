package com.example.quote_app.Saving

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "quotes", indices = [Index(value = ["quoteText"], unique = true)])
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "quoteText") val quote: String,
    @ColumnInfo(name = "author") val author: String
)
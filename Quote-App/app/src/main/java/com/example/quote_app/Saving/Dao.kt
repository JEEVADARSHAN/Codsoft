package com.example.quote_app.Saving

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes")
    fun getAllQuotes(): List<Quote>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuote(quote: Quote)

    @Delete
    suspend fun deleteQuote(quote: Quote)

    @Query("SELECT * FROM quotes WHERE quoteText = :text AND author = :author")
    suspend fun getQuoteByTextAndAuthor(text: String, author: String): Quote?

}
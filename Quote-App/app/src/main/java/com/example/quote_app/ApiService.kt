package com.example.quote_app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("random")
    fun getRandomQuotes(): Call<QuotesModel>

    @GET("random")
    fun getRandomQuoteWithTag(@Query("tags") tag: String): Call<QuotesModel>

}

package com.example.quote_app

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiCall {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.quotable.io/")
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ApiService::class.java)
    fun getRandomQuote(callback: (QuotesModel?) -> Unit) {
        api.getRandomQuotes().enqueue(object : Callback<QuotesModel> {
            override fun onResponse(call: Call<QuotesModel>, response: Response<QuotesModel>) {
                if (response.isSuccessful) {
                    val quote: QuotesModel? = response.body()

                    if (quote != null) {
                        callback(quote)
                    } else {
                        Log.e("ApiCall", "Received null quote from API")
                        callback(null)
                    }

                } else {
                    Log.e("ApiCall", "API call failed with code: ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<QuotesModel>, t: Throwable) {
                Log.e("ApiCall", "API call failed", t)
                callback(null)
            }
        })
    }
    fun getRandomQuoteWithTag(tag: String?,callback: (QuotesModel?) -> Unit) {
        if (tag != null) {
            api.getRandomQuoteWithTag(tag).enqueue(object : Callback<QuotesModel> {
                override fun onResponse(call: Call<QuotesModel>, response: Response<QuotesModel>) {
                    if (response.isSuccessful) {
                        val quote: QuotesModel? = response.body()

                        if (quote != null) {
                            callback(quote)
                        } else {
                            Log.e("ApiCall", "Received null quote from API")
                            callback(null)
                        }

                    }  else {
                        // Handle API call failure
                        Log.e("ApiCall", "API call failed with code: ${response.code()}")
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<QuotesModel>, t: Throwable) {
                    Log.e("ApiCall", "API call failed", t)
                    callback(null)
                }
            })
        }
    }
}

package com.example.caresync.Health_Article



import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/top-headlines")
    fun getHealthArticles(
        @Query("category") category: String = "health",
        @Query("country") country: String = "us",
        @Query("apiKey") apiKey: String
    ): Call<NewsApiResponse>
}

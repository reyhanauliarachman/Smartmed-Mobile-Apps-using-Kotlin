////NewsApiService
//package com.bangkit.braintumor.data
//
//import retrofit2.Call
//import retrofit2.http.GET
//import retrofit2.http.Query
//
//interface NewsApiService {
//    @GET("everything")
//    fun getArticles(
//        @Query("q") query: String,
//        @Query("language") language: String,
//        @Query("sortBy") sortBy: String,
//        @Query("from") from: String,
//        @Query("apiKey") apiKey: String
//    ): Call<ArticleResponse>
//}
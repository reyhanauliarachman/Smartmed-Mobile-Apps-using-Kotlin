////ApiClient
//package com.bangkit.braintumor.data
//
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//object ApiClient {
//    private const val BASE_URL = "https://newsapi.org/v2/"
//
//    val instance: NewsApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(NewsApiService::class.java)
//    }
//}
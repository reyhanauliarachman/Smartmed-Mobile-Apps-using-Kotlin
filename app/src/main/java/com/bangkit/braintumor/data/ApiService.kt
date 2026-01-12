//ApiService
package com.bangkit.braintumor.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @Multipart
    @POST("patients/")
    fun uploadPatientData(
        @Part("name") name: RequestBody,
        @Part("id") id: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("address") address: RequestBody,
        @Part("email") email: RequestBody,
        @Part("complications") complications: RequestBody,
        @Part("age") age: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<UploadResponse>

    @GET("patients/{id}")
    fun getPatientById(@Path("id") id: String): Call<PatientsResponse>

}
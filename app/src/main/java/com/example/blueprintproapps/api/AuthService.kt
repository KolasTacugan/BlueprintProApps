package com.example.blueprintproapps.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.blueprintproapps.models.*


interface ApiService {
    @POST("api/MobileAuth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/MobileAuth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("api/MobileAuth/change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<ChangePasswordResponse>

}
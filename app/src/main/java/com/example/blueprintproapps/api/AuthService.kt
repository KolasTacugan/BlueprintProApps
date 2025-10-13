package com.example.blueprintproapps.api

import com.example.blueprintproapps.models.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("api/MobileAuth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/MobileAuth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("api/MobileAuth/change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<ChangePasswordResponse>

    // CLIENT CALLS
    @GET("api/MobileClient/dashboard")
    fun getDashboard(): Call<DashboardResponse>

    @GET("api/MobileClient/marketplace")
    fun getMarketplace(): Call<MarketplaceResponse>


    @POST("api/MobileClient/AddToCart")
    fun addToCart(@Body request: CartRequest): Call<GenericResponse>

    @GET("api/MobileClient/GetCart")
    fun getCart(): Call<CartResponse>

    @POST("api/MobileClient/CreateCheckoutSession")
    fun createCheckoutSession(@Body cart: List<CartItemRequest>): Call<CheckoutResponse>

    @POST("api/MobileClient/CompletePurchase")
    fun completePurchase(@Body blueprintIds: List<Int>): Call<GenericResponse>

    @GET("api/MobileClient/Projects")
    fun getProjects(): Call<List<ProjectResponse>>

    @GET("api/MobileClient/Matches")
    fun getMatches(
        @Query("query") query: String? = null
    ): Call<List<MatchResponse>>

    // ✅ Send a match request
    @POST("api/MobileClient/RequestMatch")
    fun requestMatch(
        @Body request: MatchRequest
    ): Call<GenericResponse>
    @POST("api/MobileArchitect/RespondMatch")
    fun respondMatch(@Body request: MatchResponseRequest): Call<GenericResponse>
    @GET("api/MobileClient/Messages/{architectId}")
    fun getMessages(@Path("architectId") architectId: String): Call<List<MessageResponse>>

    @POST("api/MobileClient/SendMessage")
    fun sendMessage(@Body request: MessageRequest): Call<GenericResponse>

    @GET("api/MobileClient/ProjectTracker/{id}")
    fun getProjectTracker(@Path("id") projectId: Int): Call<ProjectTrackerResponse>
}

package com.example.blueprintproapps.api

import com.example.blueprintproapps.MainActivity
import com.example.blueprintproapps.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
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
    fun addToCart(@Body request: CartRequest): Call<CartResponse>
    @GET("api/MobileClient/GetCart")
    fun getCart(@Query("clientId") clientId: String): Call<List<CartItem>>

    @GET("api/MobileClient/GetCart")
    fun getCartResponse(@Query("clientId") clientId: String): Call<CartResponse>
    @POST("api/MobileClient/RemoveFromCart")
    fun removeFromCart(@Body request: RemoveCartRequest): Call<GenericResponsee>

    @POST("api/MobileClient/CreateCheckoutSession")
    fun createCheckoutSession(@Body cart: List<CartItemRequest>): Call<CheckoutResponse>

    @POST("api/MobileClient/CompletePurchase")
    fun completePurchase(
        @Body request: CompletePurchaseRequest
    ): Call<GenericResponse>

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
    @GET("api/MobileClient/Messages")
    fun getMessages(
        @Query("clientId") clientId: String,
        @Query("architectId") architectId: String
    ): Call<MessageListResponse>
    @GET("api/MobileClient/Messages/All")
    fun getAllMessages(@Query("clientId") clientId: String): Call<ConversationListResponse>

    @GET("api/MobileClient/AllMatches")
    fun getAllMatches(@Query("clientId") clientId: String): Call<MatchListResponse>

    @POST("api/MobileClient/SendMessage")
    fun sendMessage(
        @Body request: MessageRequest
    ): Call<GenericResponse>


    @GET("api/MobileClient/ProjectTracker/{id}")
    fun getProjectTracker(@Path("id") projectId: Int): Call<ProjectTrackerResponse>


    //ARCHITECT CALLS
    @GET("api/MobileArchitect/blueprints/{architectId}")
    fun getArchitectBlueprints(
        @Path("architectId") architectId: String
    ): Call<List<ArchitectBlueprintResponse>>

    @Multipart
    @POST("api/MobileArchitect/AddMarketplaceBlueprint")
    fun addMarketplaceBlueprint(
        @Part("BlueprintName") name: RequestBody,
        @Part("BlueprintPrice") price: RequestBody,
        @Part("BlueprintDescription") description: RequestBody,
        @Part("BlueprintStyle") style: RequestBody,
        @Part("IsForSale") isForSale: RequestBody,
        @Part("ArchitectId") architectId: RequestBody,
        @Part BlueprintImage: MultipartBody.Part
    ): Call<ResponseBody>

    @GET("api/MobileArchitect/getProjects/{architectId}")
    fun getArchitectProjects(
        @Path("architectId") architectId: String
    ): Call<List<ArchitectProjectResponse>>

    @GET("api/MobileArchitect/clientsForProject/{architectId}")
    fun getClientsForProject(@Path("architectId") architectId: String): Call<List<MatchedClientResponse>>

    @Multipart
    @POST("api/MobileArchitect/addProjectBlueprint")
    fun uploadProjectBlueprint(
        @Part BlueprintImage: MultipartBody.Part,
        @Part("blueprintName") blueprintName: okhttp3.RequestBody,
        @Part("blueprintPrice") blueprintPrice: okhttp3.RequestBody,
        @Part("blueprintDescription") blueprintDescription: okhttp3.RequestBody,
        @Part("clientId") clientId: okhttp3.RequestBody,
        @Part("projectTrack_dueDate") projectTrack_dueDate: okhttp3.RequestBody,
        @Part("architectId") architectId: okhttp3.RequestBody
    ): Call<UploadProjectBlueprintResponse>

    @GET("api/MobileArchitect/Architect/Messages/All")
    fun getAllMessagesForArchitect(
        @Query("architectId") architectId: String
    ): Call<ArchitectConversationListResponse>

    @GET("api/MobileArchitect/ArchitectMatches")
    fun getAllMatchesForArchitect(
        @Query("architectId") architectId: String
    ): Call<ArchitectMatchListResponse>

    @POST("api/MobileArchitect/Architect/SendMessage")
    fun sendArchitectMessage(
        @Body request: MessageRequest
    ): Call<GenericResponse>

    @GET("api/MobileArchitect/Architect/Messages")
    fun getArchitectMessages(
        @Query("clientId") clientId: String,
        @Query("architectId") architectId: String
    ): Call<MessageListResponse>

    @GET("api/MobileArchitect/matchRequests/{architectId}")
    fun getPendingMatches(
        @Path("architectId") architectId: String
    ): Call<List<ArchitectMatchRequest>>

    @POST("api/MobileArchitect/respondMatch")
    fun respondMatch(
        @Query("matchId") matchId: String,
        @Query("approve") approve: Boolean
    ): Call<Void>

    @GET("api/MobileArchitect/getProjectTracker/{blueprintId}")
    fun getArchitectProjectTracker(
        @Path("blueprintId") blueprintId: Int
    ): Call<ArchitectProjectTrackerResponse>

    @Multipart
    @POST("api/MobileArchitect/UploadProjectFile")
    fun uploadProjectFile(
        @Part("projectId") projectId: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<ArchitectApiResponse>

    @Multipart
    @POST("api/MobileArchitect/UploadComplianceFile")
    fun uploadComplianceFile(
        @Part("projectTrackId") projectTrackId: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<ArchitectApiResponse>

    @FormUrlEncoded
    @POST("api/MobileArchitect/SaveFinalizationNotes")
    fun saveFinalizationNotes(
        @Field("projectTrackId") projectTrackId: Int,
        @Field("notes") notes: String
    ): Call<ArchitectApiResponse>

    @FormUrlEncoded
    @POST("api/MobileArchitect/FinalizeProject")
    fun finalizeProject(
        @Field("projectId") projectId: String
    ): Call<ArchitectApiResponse>

    @FormUrlEncoded
    @POST("api/MobileArchitect/updateProjectStatus")
    fun updateProjectStatus(
        @Field("projectId") projectId: String,
        @Field("status") status: String
    ): Call<ArchitectApiResponse>
}

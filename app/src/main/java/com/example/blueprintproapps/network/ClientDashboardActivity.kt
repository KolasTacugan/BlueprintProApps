package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ClientProjectResponse
import com.example.blueprintproapps.models.ClientPurchasedBlueprint
import com.example.blueprintproapps.models.ConversationListResponse
import com.example.blueprintproapps.models.MatchListResponse
import com.example.blueprintproapps.models.ProfileApiResponse
import com.example.blueprintproapps.navigation.AppNavDestination
import com.example.blueprintproapps.navigation.AppNavigator
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Standalone Activity for the Client Dashboard.
 * Replicates the premium card-based UI of the fragment and web platform.
 */
class ClientDashboardActivity : AppCompatActivity() {

    private var profileCall: Call<ProfileApiResponse>? = null
    private var matchesCall: Call<MatchListResponse>? = null
    private var projectsCall: Call<List<ClientProjectResponse>>? = null
    private var conversationsCall: Call<ConversationListResponse>? = null
    private var purchaseCall: Call<List<ClientPurchasedBlueprint>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = AuthSessionManager.requireSession(this, UserRole.CLIENT) ?: return
        enableEdgeToEdge()
        setContentView(R.layout.activity_client_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Bind Views
        val findArchitect = findViewById<MaterialCardView>(R.id.findArchitect)
        val marketplaceBtn = findViewById<MaterialCardView>(R.id.marketplaceBtn)
        val chatIcon = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.chatIcon)
        val architectIcon = findViewById<ImageView>(R.id.architectIcon)
        val marketplaceIcon = findViewById<ImageView>(R.id.marketplaceIcon)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val ivEmptyRecent = findViewById<ImageView>(R.id.ivEmptyRecent)
        val tvStatPurchase = findViewById<TextView>(R.id.tvStatPurchase)

        // Stat TextViews
        val tvStatMatches = findViewById<TextView>(R.id.tvStatMatches)
        val tvStatProjects = findViewById<TextView>(R.id.tvStatProjects)

        // Apply Iconify Icons
        UiEffects.applyIconify(architectIcon, "md-search")
        UiEffects.applyIconify(marketplaceIcon, "md-store")
        UiEffects.applyIconify(chatIcon, "md-chat", android.graphics.Color.WHITE)
        UiEffects.applyIconify(ivEmptyRecent, "md-history")

        // Fetch Data
        val clientId = session.userId
        fetchClientProfile(clientId, tvUserName)
        loadDashboardStats(clientId, tvStatMatches, tvStatPurchase, tvStatProjects)

        // Click Listeners
        chatIcon.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }
        findArchitect.setOnClickListener {
            startActivity(Intent(this, MatchClientActivity::class.java))
        }
        marketplaceBtn.setOnClickListener {
            startActivity(Intent(this, MarketPlaceActivity::class.java))
        }

        // Apply Cascading Animation
        UiEffects.applyCascadingEntrance(
            listOf(findViewById<View>(R.id.statsStrip), findArchitect, marketplaceBtn),
            startDelay = 100L
        )

        // Navigation binding
        AppNavigator.bind(
            activity = this,
            bottomNavigationView = bottomNavigation,
            role = UserRole.CLIENT,
            currentDestination = AppNavDestination.HOME
        )
    }

    private fun fetchClientProfile(clientId: String, tvUserName: TextView) {
        profileCall = ApiClient.instance.getProfile(clientId)
        profileCall?.enqueue(object : Callback<ProfileApiResponse> {
            override fun onResponse(call: Call<ProfileApiResponse>, response: Response<ProfileApiResponse>) {
                val firstName = response.body()?.data?.firstName ?: "User"
                tvUserName.text = "Welcome, $firstName!"
            }
            override fun onFailure(call: Call<ProfileApiResponse>, t: Throwable) {}
        })
    }

    private fun loadDashboardStats(
        clientId: String,
        tvMatches: TextView,
        tvPurchase: TextView,
        tvProjects: TextView
    ) {
        // Fetch Matches
        matchesCall = ApiClient.instance.getAllMatches(clientId)
        matchesCall?.enqueue(object : Callback<MatchListResponse> {
            override fun onResponse(call: Call<MatchListResponse>, response: Response<MatchListResponse>) {
                tvMatches.text = (response.body()?.matches?.size ?: 0).toString()
            }
            override fun onFailure(call: Call<MatchListResponse>, t: Throwable) {}
        })

        // Fetch Projects
        projectsCall = ApiClient.instance.getClientProjects(clientId)
        projectsCall?.enqueue(object : Callback<List<ClientProjectResponse>> {
            override fun onResponse(call: Call<List<ClientProjectResponse>>, response: Response<List<ClientProjectResponse>>) {
                tvProjects.text = (response.body()?.size ?: 0).toString()
            }
            override fun onFailure(call: Call<List<ClientProjectResponse>>, t: Throwable) {}
        })

        // Fetch Purchases
        purchaseCall = ApiClient.instance.getPurchasedBlueprints(clientId)
        purchaseCall?.enqueue(object : Callback<List<ClientPurchasedBlueprint>> {
            override fun onResponse(call: Call<List<ClientPurchasedBlueprint>>, response: Response<List<ClientPurchasedBlueprint>>) {
                tvPurchase.text = (response.body()?.size ?: 0).toString()
            }
            override fun onFailure(call: Call<List<ClientPurchasedBlueprint>>, t: Throwable) {}
        })
    }

    override fun onDestroy() {
        profileCall?.cancel()
        matchesCall?.cancel()
        projectsCall?.cancel()
        conversationsCall?.cancel()
        purchaseCall?.cancel()
        super.onDestroy()
    }
}

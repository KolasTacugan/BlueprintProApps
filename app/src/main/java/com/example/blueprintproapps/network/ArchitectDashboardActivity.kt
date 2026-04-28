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
import com.example.blueprintproapps.models.ArchitectBlueprintResponse
import com.example.blueprintproapps.models.ArchitectConversationListResponse
import com.example.blueprintproapps.models.ArchitectMatchListResponse
import com.example.blueprintproapps.models.ArchitectProjectResponse
import com.example.blueprintproapps.models.CredentialStatusResponse
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
 * Standalone Activity for the Architect Dashboard.
 * Replicates the premium card-based UI of the fragment and web platform.
 */
class ArchitectDashboardActivity : AppCompatActivity() {

    private var profileCall: Call<ProfileApiResponse>? = null
    private var matchesCall: Call<ArchitectMatchListResponse>? = null
    private var projectsCall: Call<List<ArchitectProjectResponse>>? = null
    private var conversationsCall: Call<ArchitectConversationListResponse>? = null
    private var blueprintsCall: Call<List<ArchitectBlueprintResponse>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = AuthSessionManager.requireSession(this, UserRole.ARCHITECT) ?: return
        enableEdgeToEdge()
        setContentView(R.layout.activity_architect_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Bind Views
        val forMarketplaceBtn = findViewById<MaterialCardView>(R.id.forMarketplaceBtn)
        val forProjectBtn = findViewById<MaterialCardView>(R.id.forProjectBtn)
        val chatIcon = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.chatIcon)
        val marketplaceIcon = findViewById<ImageView>(R.id.marketplaceIcon)
        val projectIcon = findViewById<ImageView>(R.id.projectIcon)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val ivEmptyRecent = findViewById<ImageView>(R.id.ivEmptyRecent)
        val tvStatUploads = findViewById<TextView>(R.id.tvStatUploads)

        // Stat TextViews
        val tvStatMatches = findViewById<TextView>(R.id.tvStatMatches)
        val tvStatProjects = findViewById<TextView>(R.id.tvStatProjects)

        // Apply Iconify Icons
        UiEffects.applyIconify(marketplaceIcon, "md-store")
        UiEffects.applyIconify(projectIcon, "md-assignment")
        UiEffects.applyIconify(chatIcon, "md-chat", android.graphics.Color.WHITE)
        UiEffects.applyIconify(ivEmptyRecent, "md-history")

        // Fetch Data
        val architectId = session.userId
        fetchArchitectProfile(architectId, tvUserName)
        loadDashboardStats(architectId, tvStatMatches, tvStatUploads, tvStatProjects)
        checkAndShowCredentialReminder(architectId)

        // Click Listeners
        chatIcon.setOnClickListener {
            startActivity(Intent(this, ArchitectMessagesActivity::class.java))
        }
        forMarketplaceBtn.setOnClickListener {
            startActivity(Intent(this, ArchitectBlueprintActivity::class.java))
        }
        forProjectBtn.setOnClickListener {
            startActivity(Intent(this, ArchitectProjectActivity::class.java))
        }

        // Apply Cascading Animation
        UiEffects.applyCascadingEntrance(
            listOf(findViewById<View>(R.id.statsStrip), forMarketplaceBtn, forProjectBtn),
            startDelay = 100L
        )

        // Navigation binding
        AppNavigator.bind(
            activity = this,
            bottomNavigationView = bottomNavigation,
            role = UserRole.ARCHITECT,
            currentDestination = AppNavDestination.HOME
        )
    }

    private fun fetchArchitectProfile(architectId: String, tvUserName: TextView) {
        profileCall = ApiClient.instance.getProfile(architectId)
        profileCall?.enqueue(object : Callback<ProfileApiResponse> {
            override fun onResponse(call: Call<ProfileApiResponse>, response: Response<ProfileApiResponse>) {
                val firstName = response.body()?.data?.firstName ?: "Architect"
                tvUserName.text = "Welcome, $firstName!"
            }
            override fun onFailure(call: Call<ProfileApiResponse>, t: Throwable) {}
        })
    }

    private fun loadDashboardStats(
        architectId: String,
        tvMatches: TextView,
        tvUploads: TextView,
        tvProjects: TextView
    ) {
        // Fetch Matches
        matchesCall = ApiClient.instance.getAllMatchesForArchitect(architectId)
        matchesCall?.enqueue(object : Callback<ArchitectMatchListResponse> {
            override fun onResponse(call: Call<ArchitectMatchListResponse>, response: Response<ArchitectMatchListResponse>) {
                tvMatches.text = (response.body()?.matches?.size ?: 0).toString()
            }
            override fun onFailure(call: Call<ArchitectMatchListResponse>, t: Throwable) {}
        })

        // Fetch Projects
        projectsCall = ApiClient.instance.getArchitectProjects(architectId)
        projectsCall?.enqueue(object : Callback<List<ArchitectProjectResponse>> {
            override fun onResponse(call: Call<List<ArchitectProjectResponse>>, response: Response<List<ArchitectProjectResponse>>) {
                tvProjects.text = (response.body()?.size ?: 0).toString()
            }
            override fun onFailure(call: Call<List<ArchitectProjectResponse>>, t: Throwable) {}
        })

        // Fetch Uploads
        blueprintsCall = ApiClient.instance.getArchitectBlueprints(architectId)
        blueprintsCall?.enqueue(object : Callback<List<ArchitectBlueprintResponse>> {
            override fun onResponse(call: Call<List<ArchitectBlueprintResponse>>, response: Response<List<ArchitectBlueprintResponse>>) {
                tvUploads.text = (response.body()?.size ?: 0).toString()
            }
            override fun onFailure(call: Call<List<ArchitectBlueprintResponse>>, t: Throwable) {}
        })
    }
    private fun checkAndShowCredentialReminder(architectId: String?) {
        if (architectId.isNullOrBlank()) return

        ApiClient.instance.getCredentialStatus(architectId)
            .enqueue(object : Callback<CredentialStatusResponse> {

                override fun onResponse(
                    call: Call<CredentialStatusResponse>,
                    response: Response<CredentialStatusResponse>
                ) {
                    val body = response.body()
                    if (body != null && body.success && !body.hasCredentialFile) {
                        CredentialReminderBottomSheet()
                            .show(supportFragmentManager, "credential_reminder")
                    }
                }

                override fun onFailure(call: Call<CredentialStatusResponse>, t: Throwable) {
                    // silent — reminder is non-critical
                }
            })
    }

    private fun fetchArchitectProfile(architectId: String?, tvUserName: TextView) {
        if (architectId == null) return

        ApiClient.instance.getProfile(architectId)
            .enqueue(object : Callback<ProfileApiResponse> {

                override fun onResponse(
                    call: Call<ProfileApiResponse>,
                    response: Response<ProfileApiResponse>
                ) {
                    val body = response.body()

                    if (body != null && body.success && body.data != null) {
                        val firstName = body.data.firstName ?: "Architect"

                        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        prefs.edit().putString("firstName", firstName).apply()

                        tvUserName.text = firstName
                    }
                }

    override fun onDestroy() {
        profileCall?.cancel()
        matchesCall?.cancel()
        projectsCall?.cancel()
        conversationsCall?.cancel()
        blueprintsCall?.cancel()
        super.onDestroy()
    }
}

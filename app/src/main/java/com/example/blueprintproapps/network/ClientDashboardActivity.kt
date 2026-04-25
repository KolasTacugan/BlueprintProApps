package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ProfileApiResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_client_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ FIXED view types (must match XML)
        val findArchitect = findViewById<MaterialCardView>(R.id.findArchitect)
        val marketplaceBtn = findViewById<MaterialCardView>(R.id.marketplaceBtn)


        val chatIcon = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.chatIcon)
        val architectIcon = findViewById<ImageView>(R.id.architectIcon)
        val marketplaceIcon = findViewById<ImageView>(R.id.marketplaceIcon)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val tvUserName = findViewById<TextView>(R.id.tvUserName)

        // Apply Iconify Icons
        com.example.blueprintproapps.utils.UiEffects.applyIconify(architectIcon, "md-search")
        com.example.blueprintproapps.utils.UiEffects.applyIconify(marketplaceIcon, "md-store")
        
        // FAB Icon
        com.example.blueprintproapps.utils.UiEffects.applyIconify(chatIcon, "md-chat", android.graphics.Color.WHITE)

        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val clientId = prefs.getString("clientId", null)

        fetchClientProfile(clientId, tvUserName)

        chatIcon.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }

        findArchitect.setOnClickListener {
            startActivity(Intent(this, MatchClientActivity::class.java))
        }

        marketplaceBtn.setOnClickListener {
            startActivity(Intent(this, MarketPlaceActivity::class.java))
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> true

                R.id.nav_project -> {
                    val intent = Intent(this, ClientProjectsActivity::class.java)
                    intent.putExtra("clientId", clientId)
                    startActivity(intent)
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }

        // Menu Icons via Iconify
        com.example.blueprintproapps.utils.UiEffects.applyIconifyToMenu(this, bottomNavigation.menu, R.id.nav_home, "{md-home}")
        com.example.blueprintproapps.utils.UiEffects.applyIconifyToMenu(this, bottomNavigation.menu, R.id.nav_project, "{md-assignment}")
        com.example.blueprintproapps.utils.UiEffects.applyIconifyToMenu(this, bottomNavigation.menu, R.id.nav_profile, "{md-person}")
    }

    private fun fetchClientProfile(clientId: String?, tvUserName: TextView) {
        if (clientId == null) return

        ApiClient.instance.getProfile(clientId)
            .enqueue(object : Callback<ProfileApiResponse> {

                override fun onResponse(
                    call: Call<ProfileApiResponse>,
                    response: Response<ProfileApiResponse>
                ) {
                    val body = response.body()

                    if (body != null && body.success && body.data != null) {
                        val firstName = body.data.firstName ?: "User"

                        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        prefs.edit().putString("firstName", firstName).apply()

                        tvUserName.text = firstName
                    }
                }

                override fun onFailure(call: Call<ProfileApiResponse>, t: Throwable) {
                    // Optional error handling
                }
            })
    }
}

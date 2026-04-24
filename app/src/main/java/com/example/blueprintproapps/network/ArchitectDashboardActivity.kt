package com.example.blueprintproapps.network

import com.example.blueprintproapps.R
import com.example.blueprintproapps.utils.UiEffects

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ProfileApiResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_architect_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ FIXED: match XML view types
        val forMarketplaceBtn = findViewById<MaterialCardView>(R.id.forMarketplaceBtn)
        val forProjectBtn = findViewById<MaterialCardView>(R.id.forProjectBtn)


        val chatIcon = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.chatIcon)
        val marketplaceIcon = findViewById<ImageView>(R.id.marketplaceIcon)
        val projectIcon = findViewById<ImageView>(R.id.projectIcon)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val tvUserName = findViewById<TextView>(R.id.tvUserName)

        // Apply Iconify Icons
        com.example.blueprintproapps.utils.UiEffects.applyIconify(marketplaceIcon, "{md-store-mall-directory}")
        com.example.blueprintproapps.utils.UiEffects.applyIconify(projectIcon, "{md-assignment}")
        
        // Settings the FAB icon via drawable
        chatIcon.setImageDrawable(com.joanzapata.iconify.IconDrawable(this, "{md-chat}")
            .colorRes(android.R.color.white)
            .sizeDp(24))

        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val architectId = prefs.getString("architectId", null)

        fetchArchitectProfile(architectId, tvUserName)

        chatIcon.setOnClickListener {
            startActivity(Intent(this, ArchitectMessagesActivity::class.java))
        }

        forMarketplaceBtn.setOnClickListener {
            startActivity(Intent(this, ArchitectBlueprintActivity::class.java))
        }

        forProjectBtn.setOnClickListener {
            startActivity(Intent(this, ArchitectProjectActivity::class.java))
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_match_request -> {
                    startActivity(Intent(this, ArchitectMatchActivity::class.java))
                    true
                }

                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.nav_home

        // Menu Icons via Iconify
        UiEffects.applyIconifyToMenu(this, bottomNavigation.menu, R.id.nav_home, "{md-home}")
        UiEffects.applyIconifyToMenu(this, bottomNavigation.menu, R.id.nav_match_request, "{md-people}")
        UiEffects.applyIconifyToMenu(this, bottomNavigation.menu, R.id.nav_profile, "{md-person}")
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

                override fun onFailure(call: Call<ProfileApiResponse>, t: Throwable) {
                    // optional
                }
            })
    }
}

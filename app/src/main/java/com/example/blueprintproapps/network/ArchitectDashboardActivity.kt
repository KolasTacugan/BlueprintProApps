package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ProfileApiResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
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

        // ✅ Reference buttons
        val forMarketplaceBtn = findViewById<LinearLayout>(R.id.forMarketplaceBtn)
        val forProjectBtn = findViewById<LinearLayout>(R.id.forProjectBtn)

        val chatIcon = findViewById<ImageView>(R.id.chatIcon)

        // ✅ Navigate to your upload blueprint screens (you can change the target activities)
        chatIcon.setOnClickListener {
            val intent = Intent(this, ArchitectMessagesActivity::class.java)
            startActivity(intent)
        }
        // ✅ Navigate to your upload blueprint screens (you can change the target activities)
        forMarketplaceBtn.setOnClickListener {
            val intent = Intent(this, ArchitectBlueprintActivity::class.java)
            startActivity(intent)
        }

        forProjectBtn.setOnClickListener {
             val intent = Intent(this, ArchitectProjectActivity::class.java)
             startActivity(intent)
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val architectId = prefs.getString("architectId", null)

        fetchArchitectProfile(architectId, tvUserName)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_match_request -> {
                    val intent = Intent(this, ArchitectMatchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                     val intent = Intent(this, ProfileActivity::class.java)
                     startActivity(intent)
                    true
                }
                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.nav_home

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

                override fun onFailure(call: Call<ProfileApiResponse>, t: Throwable) {}
            })
    }

}

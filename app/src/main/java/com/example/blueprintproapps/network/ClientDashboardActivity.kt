package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.R
import com.google.android.material.bottomnavigation.BottomNavigationView

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

        // ✅ Reference your "Find Architect" button
        val findArchitect = findViewById<LinearLayout>(R.id.findArchitect)
        val marketplaceBtn = findViewById<LinearLayout>(R.id.marketplaceBtn)
        val chatIcon = findViewById<ImageView>(R.id.chatIcon)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val clientId = prefs.getString("clientId", null)

        chatIcon.setOnClickListener {
            val intent = Intent(this, MessagesActivity::class.java)
            startActivity(intent)
        }

        // ✅ Open MatchClientActivity when tapped
        findArchitect.setOnClickListener {
            val intent = Intent(this, MatchClientActivity::class.java)
            startActivity(intent)
        }
        marketplaceBtn.setOnClickListener {
            val intent = Intent(this, MarketPlaceActivity::class.java)
            startActivity(intent)
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already in ClientDashboardActivity
                    true
                }

                R.id.nav_project -> {
                    val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                    val clientId = prefs.getString("clientId", null)

                    val intent = Intent(this, ClientProjectsActivity::class.java)
                    intent.putExtra("clientId", clientId)
                    startActivity(intent)
                    true
                }

                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }
}

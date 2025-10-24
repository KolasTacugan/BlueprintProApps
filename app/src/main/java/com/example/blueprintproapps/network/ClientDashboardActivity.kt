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
    }
}

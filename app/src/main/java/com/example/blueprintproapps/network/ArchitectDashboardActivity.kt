package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.R

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
    }
}

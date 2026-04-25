package com.example.blueprintproapps.network

import com.example.blueprintproapps.R
import com.example.blueprintproapps.utils.UiEffects

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.ImageView
import com.example.blueprintproapps.utils.ParallaxEffect

class ChooseRoleActivity : AppCompatActivity() {

    private lateinit var parallaxEffect: ParallaxEffect

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose_role)

        val background = findViewById<ImageView>(R.id.ivBackground)
        val logo = findViewById<View>(R.id.ivLogo)
        val tagline = findViewById<TextView>(R.id.tvTagline)
        val clientButton = findViewById<View>(R.id.btnClient)
        val architectButton = findViewById<View>(R.id.btnArchitect)
        val ivClientIcon = findViewById<ImageView>(R.id.ivClientIcon)
        val ivArchitectIcon = findViewById<ImageView>(R.id.ivArchitectIcon)
        val loginLinkContainer = findViewById<View>(R.id.loginLinkContainer)

        // Window Insets for Content (Applied to ScrollView to keep background full-screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.roleScrollView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Apply Premium Effects
        parallaxEffect = ParallaxEffect(this)
        parallaxEffect.attach(background)

        UiEffects.applyIconify(ivClientIcon, "md-person")
        UiEffects.applyIconify(ivArchitectIcon, "md-account-balance")
        
        // Interactive visual feedback
        UiEffects.applyPressScaleEffect(clientButton)
        UiEffects.applyPressScaleEffect(architectButton)
        
        // Cascading Entrance for all components
        UiEffects.applyCascadingEntrance(listOf(logo, tagline, clientButton, architectButton, loginLinkContainer))

        val sharedPrefs: SharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        clientButton.setOnClickListener {
            saveRole(sharedPrefs, "Client")
            startActivity(Intent(this, RegisterClientActivity::class.java))
        }

        architectButton.setOnClickListener {
            saveRole(sharedPrefs, "Architect")
            startActivity(Intent(this, RegisterArchitectActivity::class.java))
        }

        loginLinkContainer.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::parallaxEffect.isInitialized) {
            parallaxEffect.detach()
        }
    }

    private fun saveRole(sharedPrefs: SharedPreferences, role: String) {
        sharedPrefs.edit().putString("userType", role).apply()
    }
}

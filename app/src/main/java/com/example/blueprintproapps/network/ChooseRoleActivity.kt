package com.example.blueprintproapps.network

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.R

class ChooseRoleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose_role)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val clientButton = findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnClient)
        val architectButton = findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnArchitect)
        val ivClientIcon = findViewById<android.widget.ImageView>(R.id.ivClientIcon)
        val ivArchitectIcon = findViewById<android.widget.ImageView>(R.id.ivArchitectIcon)

        // Iconify migration
        ivClientIcon.setImageDrawable(com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_person).colorRes(com.google.android.material.R.color.material_dynamic_primary50))
        ivArchitectIcon.setImageDrawable(com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_account_balance).colorRes(com.google.android.material.R.color.material_dynamic_primary50))

        val backToLogin = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBack)

        val sharedPrefs: SharedPreferences = getSharedPreferences("BlueprintPrefs", MODE_PRIVATE)

        clientButton.setOnClickListener {
            saveRole(sharedPrefs, "Client")
            val intent = Intent(this, RegisterClientActivity::class.java)
            startActivity(intent)
        }

        architectButton.setOnClickListener {
            saveRole(sharedPrefs, "Architect")
            val intent = Intent(this, RegisterArchitectActivity::class.java)
            startActivity(intent)
        }

        backToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    //test push
    private fun saveRole(sharedPrefs: SharedPreferences, role: String) {
        sharedPrefs.edit().putString("user_role", role).apply()
    }
}

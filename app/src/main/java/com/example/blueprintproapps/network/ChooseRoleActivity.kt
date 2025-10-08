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

        val clientButton = findViewById<LinearLayout>(R.id.clientButton)
        val architectButton = findViewById<LinearLayout>(R.id.architectButton)
        val backToLogin = findViewById<TextView>(R.id.backToLogin)

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

    private fun saveRole(sharedPrefs: SharedPreferences, role: String) {
        sharedPrefs.edit().putString("user_role", role).apply()
    }
}

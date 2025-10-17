package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.LoginRequest
import com.example.blueprintproapps.models.LoginResponse
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val rememberMe = findViewById<CheckBox>(R.id.rememberMe)
        val registerLink = findViewById<TextView>(R.id.registerLink)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)

        // Forgot Password
        forgotPassword.setOnClickListener {
            val intent = Intent(this@LoginActivity, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        // Register Link
        registerLink.setOnClickListener {
            val intent = Intent(this@LoginActivity, ChooseRoleActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email = email, password = password)

            ApiClient.instance.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null) {

                            val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            val editor = prefs.edit()

                            // ✅ Save based on role
                            when (loginResponse.role.lowercase()) {
                                "client" -> {
                                    editor.putString("clientId", loginResponse.userId)
                                    editor.putString("userType", "Client")
                                    editor.apply()

                                    Toast.makeText(this@LoginActivity, "Login successful! Welcome ${loginResponse.email}", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@LoginActivity, ClientDashboardActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                                "architect" -> {
                                    editor.putString("architectId", loginResponse.userId)
                                    editor.putString("userType", "Architect")
                                    editor.apply()

                                    Toast.makeText(this@LoginActivity, "Login successful! Welcome ${loginResponse.email}", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@LoginActivity, ArchitectDashboardActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                                else -> {
                                    Toast.makeText(this@LoginActivity, "Unknown role", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}

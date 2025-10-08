package com.example.blueprintproapps.network

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.RegisterRequest
import com.example.blueprintproapps.models.RegisterResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterClientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_client)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_scroll)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ References to UI
        val firstNameInput = findViewById<TextInputEditText>(R.id.firstNameInput)
        val lastNameInput = findViewById<TextInputEditText>(R.id.lastNameInput)
        val phoneInput = findViewById<TextInputEditText>(R.id.phoneInput)
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        val registerButton = findViewById<MaterialButton>(R.id.registerButton)
        val loginLink = findViewById<TextView>(R.id.loginLink)

        val sharedPrefs: SharedPreferences = getSharedPreferences("BlueprintPrefs", MODE_PRIVATE)
        val role = sharedPrefs.getString("user_role", "Client") // defaults to Client

        registerButton.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // ✅ Simple Validation
            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Create register request
            val request = RegisterRequest(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phone,
                email = email,
                password = password,
                role = role ?: "Client"
            )

            // ✅ Call API
            ApiClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@RegisterClientActivity,
                            "Registration successful! Please log in.",
                            Toast.LENGTH_LONG
                        ).show()

                        // Go to login
                        startActivity(Intent(this@RegisterClientActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@RegisterClientActivity, "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterClientActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // ✅ Navigate back to login
        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}

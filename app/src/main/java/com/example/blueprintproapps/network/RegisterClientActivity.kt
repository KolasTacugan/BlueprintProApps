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
        // ✅ References to UI Layouts for Validation
        val firstNameLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.firstNameLayout)
        val lastNameLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.lastNameLayout)
        val phoneLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.phoneLayout)
        val emailLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.emailLayout)
        val passwordLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.passwordLayout)
        val confirmPasswordLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.confirmPasswordLayout)

        // Iconify migration
        val iconColor = com.google.android.material.R.color.material_dynamic_primary50
        firstNameLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_person).colorRes(iconColor).sizeDp(24)
        lastNameLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_person).colorRes(iconColor).sizeDp(24)
        phoneLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_phone).colorRes(iconColor).sizeDp(24)
        emailLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_email).colorRes(iconColor).sizeDp(24)
        passwordLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_lock).colorRes(iconColor).sizeDp(24)
        confirmPasswordLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_lock).colorRes(iconColor).sizeDp(24)

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

            // ✅ Reset errors
            firstNameLayout.error = null
            lastNameLayout.error = null
            phoneLayout.error = null
            emailLayout.error = null
            passwordLayout.error = null
            confirmPasswordLayout.error = null

            var isValid = true

            // ✅ Validation
            if (firstName.isEmpty()) { firstNameLayout.error = "Required"; isValid = false }
            if (lastName.isEmpty()) { lastNameLayout.error = "Required"; isValid = false }
            if (phone.isEmpty()) { phoneLayout.error = "Required"; isValid = false }
            if (email.isEmpty()) { emailLayout.error = "Required"; isValid = false }
            if (password.isEmpty()) { passwordLayout.error = "Required"; isValid = false }

            if (password != confirmPassword) {
                confirmPasswordLayout.error = "Passwords do not match"
                isValid = false
            } else if (confirmPassword.isEmpty()) {
                confirmPasswordLayout.error = "Required"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            // ✅ Create register request
            val request = RegisterRequest(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phone,
                email = email,
                password = password,
                role = role ?: "Client"
            )

            registerButton.isEnabled = false
            registerButton.text = "Registering..."

            // ✅ Call API
            ApiClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    registerButton.isEnabled = true
                    registerButton.text = "Register"
                    
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
                    registerButton.isEnabled = true
                    registerButton.text = "Register"
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

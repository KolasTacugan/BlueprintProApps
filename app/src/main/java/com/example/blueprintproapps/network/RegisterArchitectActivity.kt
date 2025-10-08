package com.example.blueprintproapps.network

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
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

class RegisterArchitectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_architect)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_scroll)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ UI References
        val firstNameInput = findViewById<TextInputEditText>(R.id.firstNameInput)
        val lastNameInput = findViewById<TextInputEditText>(R.id.lastNameInput)
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        val licenseNoInput = findViewById<TextInputEditText>(R.id.licenseNoInput)

        val spinnerStyle = findViewById<Spinner>(R.id.spinnerStyle)
        val spinnerBudget = findViewById<Spinner>(R.id.spinnerBudget)
        val spinnerLocation = findViewById<Spinner>(R.id.spinnerLocation)
        val spinnerSpecialization = findViewById<Spinner>(R.id.spinnerSpecialization)
        val registerButton = findViewById<MaterialButton>(R.id.registerButtonArchitect)

        // ✅ Dropdown setup
        setupSpinner(spinnerStyle, listOf("Modern", "Traditional", "Contemporary", "Minimalist"))
        setupSpinner(spinnerBudget, listOf("Low", "Medium", "High"))
        setupSpinner(spinnerLocation, listOf("Urban", "Suburban", "Rural"))
        setupSpinner(spinnerSpecialization, listOf("Residential", "Commercial", "Industrial"))

        // ✅ Shared preferences for role
        val sharedPrefs: SharedPreferences = getSharedPreferences("BlueprintPrefs", MODE_PRIVATE)
        val role = sharedPrefs.getString("user_role", "Architect")

        // ✅ Register button click
        registerButton.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()
            val licenseNo = licenseNoInput.text.toString().trim()

            // Validation
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty() || licenseNo.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Create request
            val request = RegisterRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                phoneNumber = "",
                role = role ?: "Architect",
                licenseNo = licenseNo,
                style = spinnerStyle.selectedItem.toString(),
                specialization = spinnerSpecialization.selectedItem.toString(),
                location = spinnerLocation.selectedItem.toString(),
                laborCost = spinnerBudget.selectedItem.toString()
            )

            // ✅ API Call
            ApiClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@RegisterArchitectActivity,
                            "Registration successful! Please log in.",
                            Toast.LENGTH_LONG
                        ).show()

                        // Go to login screen
                        startActivity(Intent(this@RegisterArchitectActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@RegisterArchitectActivity, "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterArchitectActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = adapter
    }
}

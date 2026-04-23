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
        val phoneNumber = findViewById<TextInputEditText>(R.id.phoneNumberInput)

        // ✅ Layout References for Validation
        val firstNameLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.firstNameLayout)
        val lastNameLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.lastNameLayout)
        val emailLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.emailLayout)
        val passwordLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.passwordLayout)
        val confirmPasswordLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.confirmPasswordLayout)
        val licenseNoLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.licenseNoLayout)
        val phoneNumberLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.phoneNumberLayout)

        // Iconify migration
        val iconColor = com.google.android.material.R.color.material_dynamic_primary50
        firstNameLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_person).colorRes(iconColor).sizeDp(24)
        lastNameLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_person).colorRes(iconColor).sizeDp(24)
        emailLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_email).colorRes(iconColor).sizeDp(24)
        passwordLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_lock).colorRes(iconColor).sizeDp(24)
        confirmPasswordLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_lock).colorRes(iconColor).sizeDp(24)
        licenseNoLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_assignment_ind).colorRes(iconColor).sizeDp(24)
        phoneNumberLayout.startIconDrawable = com.joanzapata.iconify.IconDrawable(this, com.joanzapata.iconify.fonts.MaterialIcons.md_phone).colorRes(iconColor).sizeDp(24)

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
            val phone = phoneNumber.text.toString().trim()

            // ✅ Reset errors
            firstNameLayout.error = null
            lastNameLayout.error = null
            emailLayout.error = null
            passwordLayout.error = null
            confirmPasswordLayout.error = null
            licenseNoLayout.error = null
            phoneNumberLayout.error = null

            var isValid = true

            // Validation
            if (firstName.isEmpty()) { firstNameLayout.error = "Required"; isValid = false }
            if (lastName.isEmpty()) { lastNameLayout.error = "Required"; isValid = false }
            if (email.isEmpty()) { emailLayout.error = "Required"; isValid = false }
            if (phone.isEmpty()) { phoneNumberLayout.error = "Required"; isValid = false }
            if (licenseNo.isEmpty()) { licenseNoLayout.error = "Required"; isValid = false }
            if (password.isEmpty()) { passwordLayout.error = "Required"; isValid = false }

            if (password != confirmPassword) {
                confirmPasswordLayout.error = "Passwords do not match"
                isValid = false
            } else if (confirmPassword.isEmpty()) {
                confirmPasswordLayout.error = "Required"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            // ✅ Create request
            val request = RegisterRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                phoneNumber = phone,
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

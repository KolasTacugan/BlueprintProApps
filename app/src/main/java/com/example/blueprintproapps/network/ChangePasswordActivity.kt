package com.example.blueprintproapps.network

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ChangePasswordRequest
import com.example.blueprintproapps.models.ChangePasswordResponse
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailInput = findViewById<TextInputEditText>(R.id.etEmail)
        val newPasswordInput = findViewById<TextInputEditText>(R.id.etNewPassword)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val verifyButton = findViewById<Button>(R.id.btnVerifyEmail)
        val changeButton = findViewById<Button>(R.id.btnChangePassword)
        val passwordSection = findViewById<LinearLayout>(R.id.passwordSection)

        // Step 1️⃣: Verify email
        verifyButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Here we only check if the email exists by calling your ChangePassword API
            // The backend will respond with 404 if not found
            val request = ChangePasswordRequest(email = email, newPassword = "dummyTempPassword")

            ApiClient.instance.changePassword(request).enqueue(object : Callback<ChangePasswordResponse> {
                override fun onResponse(
                    call: Call<ChangePasswordResponse>,
                    response: Response<ChangePasswordResponse>
                ) {
                    if (response.code() == 404) {
                        Toast.makeText(this@ChangePasswordActivity, "Email not found", Toast.LENGTH_SHORT).show()
                    } else {
                        // Instead of actually changing the password, just reveal the fields
                        passwordSection.visibility = View.VISIBLE
                        Toast.makeText(this@ChangePasswordActivity, "Email verified. You can now change your password.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                    Toast.makeText(this@ChangePasswordActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Step 2️⃣: Change password
        changeButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val newPassword = newPasswordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill out all password fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = ChangePasswordRequest(email = email, newPassword = newPassword)

            ApiClient.instance.changePassword(request).enqueue(object : Callback<ChangePasswordResponse> {
                override fun onResponse(
                    call: Call<ChangePasswordResponse>,
                    response: Response<ChangePasswordResponse>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        Toast.makeText(
                            this@ChangePasswordActivity,
                            result?.message ?: "Password changed successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(this@ChangePasswordActivity, "Failed to change password", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                    Toast.makeText(this@ChangePasswordActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}

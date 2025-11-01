package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ProfileApiResponse
import com.example.blueprintproapps.models.ProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvCredentialsFile: TextView
    private lateinit var tvProBadge: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnEditProfile: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var btnFavorites: Button
    private lateinit var btnSubscription: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Initialize Views
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        tvCredentialsFile = findViewById(R.id.tvCredentialsFile)
        tvProBadge = findViewById(R.id.tvProBadge)
        imgProfile = findViewById(R.id.imgProfile)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnSettings = findViewById(R.id.btnSettings)
        btnFavorites = findViewById(R.id.btnFavorites)
        btnSubscription = findViewById(R.id.btnSubscription)

        // ✅ Get userId depending on role
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userType = prefs.getString("userType", null)
        val userId = when (userType) {
            "Architect" -> prefs.getString("architectId", null)
            "Client" -> prefs.getString("clientId", null)
            else -> null
        }

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
        // ✅ Fetch Profile
        loadProfile(userId)
    }

    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userType = prefs.getString("userType", null)

        val userId = when (userType) {
            "Architect" -> prefs.getString("architectId", null)
            "Client" -> prefs.getString("clientId", null)
            else -> null
        }

        if (!userId.isNullOrEmpty()) {
            loadProfile(userId)
        }
    }

    private fun loadProfile(userId: String) {
        ApiClient.instance.getProfile(userId).enqueue(object : Callback<ProfileApiResponse> {
            override fun onResponse(
                call: Call<ProfileApiResponse>,
                response: Response<ProfileApiResponse>
            ) {
                if (response.isSuccessful) {
                    val profile = response.body()?.data
                    if (profile != null) {
                        tvFullName.text = "${profile.firstName ?: ""} ${profile.lastName ?: ""}"
                        tvEmail.text = profile.email ?: "N/A"
                        tvPhone.text = profile.phoneNumber ?: "N/A"
                        tvCredentialsFile.text = profile.credentialsFilePath ?: "N/A"

                        if (!profile.profilePhoto.isNullOrEmpty()) {
                            Glide.with(this@ProfileActivity)
                                .load(profile.profilePhoto)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .into(imgProfile)
                        }

                        tvProBadge.visibility = if (profile.isPro) TextView.VISIBLE else TextView.GONE
                    } else {
                        Toast.makeText(this@ProfileActivity, "No profile data found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to load profile (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileApiResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}

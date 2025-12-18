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
import com.example.blueprintproapps.models.ArchitectSubscriptionRequest
import com.example.blueprintproapps.models.ArchitectSubscriptionResponse
import com.example.blueprintproapps.models.GenericResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.Dialog
import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.WebViewActivity
import com.example.blueprintproapps.adapter.ClientPurchasedBlueprintAdapter
import com.example.blueprintproapps.models.ClientPurchasedBlueprint

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
    private lateinit var rvPurchasedBlueprints: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val layoutArchitectCredentials =
            findViewById<LinearLayout>(R.id.layoutArchitectCredentials)

        // Initialize Views
        rvPurchasedBlueprints = findViewById(R.id.rvPurchasedBlueprints)
        rvPurchasedBlueprints.layoutManager = LinearLayoutManager(this)
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        tvCredentialsFile = findViewById(R.id.tvCredentialsFile)
        tvProBadge = findViewById(R.id.tvProBadge)
        imgProfile = findViewById(R.id.imgProfile)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnSubscription = findViewById(R.id.btnSubscription)

        // Open credentials
        tvCredentialsFile.setOnClickListener {
            val url = credentialsFilePath?.trim()
            if (url.isNullOrEmpty()) {
                Toast.makeText(this, "No file available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        // Get userType ONCE
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userType = prefs.getString("userType", null)

        // Enable or disable subscription button based on user role
        if (userType == "Client") {
            btnSubscription.visibility = View.GONE
            loadPurchasedBlueprints()
            layoutArchitectCredentials.visibility = View.GONE
        } else {
            btnSubscription.isEnabled = true
            btnSubscription.alpha = 1f

        }

        // Subscription modal
        btnSubscription.setOnClickListener {
            showSubscriptionDialog()
        }

        // Get correct userId ONCE
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
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            logoutUser()
        }
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
    private fun loadPurchasedBlueprints() {

        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val clientId = prefs.getString("clientId", null)

        if (clientId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.getPurchasedBlueprints(clientId)
            .enqueue(object : Callback<List<ClientPurchasedBlueprint>> {

                override fun onResponse(
                    call: Call<List<ClientPurchasedBlueprint>>,
                    response: Response<List<ClientPurchasedBlueprint>>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body().orEmpty()
                        rvPurchasedBlueprints.adapter =
                            ClientPurchasedBlueprintAdapter(list)
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Failed to load purchased blueprints",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<ClientPurchasedBlueprint>>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // -------------------------------------
    // SUBSCRIPTION MODAL
    // -------------------------------------
    private fun showSubscriptionDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.subscription_modal)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val btnFree = dialog.findViewById<Button>(R.id.btnFreeCurrent)
        val btnUpgrade = dialog.findViewById<Button>(R.id.btnUpgradePro)

        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val isPro = prefs.getBoolean("isPro", false)

        if (isPro) {
            btnFree.text = "Downgrade to Free"
            btnFree.isEnabled = true
            btnFree.alpha = 1f
            btnFree.setOnClickListener {
                downgradeToFree()
                dialog.dismiss()
            }

            btnUpgrade.text = "Current Plan"
            btnUpgrade.isEnabled = false
            btnUpgrade.alpha = 0.5f

        } else {
            btnFree.text = "Current Plan"
            btnFree.isEnabled = false
            btnFree.alpha = 0.5f

            btnUpgrade.text = "Get Started"
            btnUpgrade.isEnabled = true
            btnUpgrade.alpha = 1f
            btnUpgrade.setOnClickListener {
                upgradeToPro()
                dialog.dismiss()
            }
        }

        dialog.show()
    }


    // -------------------------------------
    // UPGRADE TO PRO
    // -------------------------------------
    private fun upgradeToPro() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val architectId = prefs.getString("architectId", null) ?: return

        val request = ArchitectSubscriptionRequest(architectId)

        ApiClient.instance.createArchitectSubscription(request)
            .enqueue(object : Callback<ArchitectSubscriptionResponse> {
                override fun onResponse(
                    call: Call<ArchitectSubscriptionResponse>,
                    response: Response<ArchitectSubscriptionResponse>
                ) {
                    val body = response.body()

                    if (response.isSuccessful && body?.success == true) {
                        val paymentUrl = body.paymentUrl ?: return

                        val intent = Intent(this@ProfileActivity, WebViewActivity::class.java)
                        intent.putExtra("url", paymentUrl)
                        startActivity(intent)

                    } else {
                        Toast.makeText(this@ProfileActivity, "Cannot create subscription session", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ArchitectSubscriptionResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }


    // -------------------------------------
    // DOWNGRADE
    // -------------------------------------
    private fun downgradeToFree() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val architectId = prefs.getString("architectId", null) ?: return

        val request = ArchitectSubscriptionRequest(architectId)

        ApiClient.instance.downgradeArchitectPlan(request)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@ProfileActivity, "Downgraded successfully", Toast.LENGTH_SHORT).show()
                        loadProfile(architectId)
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to downgrade", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }


    // -------------------------------------
    // LOAD PROFILE FROM API
    // -------------------------------------
    private fun loadProfile(userId: String) {
        ApiClient.instance.getProfile(userId)
            .enqueue(object : Callback<ProfileApiResponse> {
                override fun onResponse(
                    call: Call<ProfileApiResponse>,
                    response: Response<ProfileApiResponse>
                ) {
                    if (response.isSuccessful) {
                        val profile = response.body()?.data ?: return

                        tvFullName.text = "${profile.firstName ?: ""} ${profile.lastName ?: ""}"
                        tvEmail.text = profile.email ?: "N/A"
                        tvPhone.text = profile.phoneNumber ?: "N/A"
                        tvCredentialsFile.text = profile.credentialsFilePath ?: "N/A"

                        credentialsFilePath = profile.credentialsFilePath

                        if (!profile.profilePhoto.isNullOrEmpty()) {
                            Glide.with(this@ProfileActivity)
                                .load(profile.profilePhoto)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .into(imgProfile)
                        }

                        // Save PRO status
                        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        prefs.edit()
                            .putBoolean("isPro", profile.isPro)
                            .apply()

                        // Show or hide PRO badge
                        tvProBadge.visibility = if (profile.isPro) TextView.VISIBLE else TextView.GONE

                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProfileApiResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun logoutUser() {

        // Clear all saved user data inside MyAppPrefs
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        // Go back to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        )

        startActivity(intent)

        finish()
    }


}

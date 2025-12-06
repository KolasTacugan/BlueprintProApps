package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.architect.ArchitectMatchRequestAdapter
import com.example.blueprintproapps.models.ArchitectMatchRequest
import com.example.blueprintproapps.models.ClientProfileResponse
import com.example.blueprintproapps.utils.ClientProfileBottomSheet
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectMatchActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArchitectMatchRequestAdapter
    private lateinit var architectId: String
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_architect_match)

        recyclerView = findViewById(R.id.recyclerMatchRequests)
        bottomNav = findViewById(R.id.bottomNavigationView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // ✅ Get architectId from SharedPreferences (assign to the property, not local val)
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val storedArchitectId = prefs.getString("architectId", null)

        if (storedArchitectId.isNullOrEmpty()) {
            Toast.makeText(this, "ArchitectID not found", Toast.LENGTH_SHORT).show()
            return
        } else {
            architectId = storedArchitectId
        }

        // ✅ Load pending requests
        fetchPendingRequests()

        // Bottom navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, ArchitectDashboardActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchPendingRequests() {
        ApiClient.instance.getPendingMatches(architectId)
            .enqueue(object : Callback<List<ArchitectMatchRequest>> {
                override fun onResponse(
                    call: Call<List<ArchitectMatchRequest>>,
                    response: Response<List<ArchitectMatchRequest>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val requests = response.body()!!
                        adapter = ArchitectMatchRequestAdapter(
                            requests,
                            onAccept = { matchId -> respondToMatch(matchId, true) },
                            onDecline = { matchId -> respondToMatch(matchId, false) },
                            onProfileClick = { clientId -> openClientProfile(clientId) }
                        )
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(
                            this@ArchitectMatchActivity,
                            "No pending match requests.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<ArchitectMatchRequest>>, t: Throwable) {
                    Toast.makeText(this@ArchitectMatchActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun openClientProfile(clientId: String) {
        ApiClient.instance.getClientProfile(clientId)
            .enqueue(object : Callback<ClientProfileResponse> {
                override fun onResponse(
                    call: Call<ClientProfileResponse>,
                    response: Response<ClientProfileResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val sheet = ClientProfileBottomSheet(response.body()!!)
                        sheet.show(supportFragmentManager, "ClientProfileBottomSheet")
                    }
                }

                override fun onFailure(call: Call<ClientProfileResponse>, t: Throwable) {
                    Toast.makeText(this@ArchitectMatchActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun respondToMatch(matchId: String, approve: Boolean) {
        ApiClient.instance.respondMatch(matchId, approve)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ArchitectMatchActivity,
                            if (approve) "Request accepted!" else "Request declined!",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchPendingRequests() // Refresh list
                    } else {
                        Toast.makeText(
                            this@ArchitectMatchActivity,
                            "Failed to update match.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@ArchitectMatchActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
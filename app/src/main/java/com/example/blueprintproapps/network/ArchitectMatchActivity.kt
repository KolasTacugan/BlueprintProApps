package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectMatchRequestAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ArchitectMatchRequest
import com.example.blueprintproapps.models.ClientProfileResponse
import com.example.blueprintproapps.navigation.AppNavDestination
import com.example.blueprintproapps.navigation.AppNavigator
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
        val session = AuthSessionManager.requireSession(this, UserRole.ARCHITECT) ?: return
        architectId = session.userId
        setContentView(R.layout.activity_architect_match)

        recyclerView = findViewById(R.id.recyclerMatchRequests)
        bottomNav = findViewById(R.id.bottomNavigationView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ArchitectMatchRequestAdapter(
            mutableListOf(),                    // <-- START EMPTY
            onAccept = { matchId -> respondToMatch(matchId, true) },
            onDecline = { matchId -> respondToMatch(matchId, false) }
        )
        recyclerView.adapter = adapter

        // ✅ Load pending requests
        fetchPendingRequests()

        AppNavigator.bind(
            activity = this,
            bottomNavigationView = bottomNav,
            role = UserRole.ARCHITECT,
            currentDestination = AppNavDestination.WORK
        )
    }

    private fun fetchPendingRequests() {
        ApiClient.instance.getPendingMatches(architectId)
            .enqueue(object : Callback<List<ArchitectMatchRequest>> {
                override fun onResponse(
                    call: Call<List<ArchitectMatchRequest>>,
                    response: Response<List<ArchitectMatchRequest>>
                ) {
                    if (response.isSuccessful) {
                        val newList = response.body()!!
                        adapter.updateData(newList)   // ← THIS IS THE KEY
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
                        fetchPendingRequests()
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
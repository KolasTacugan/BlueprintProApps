package com.example.blueprintproapps.network

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.GenericResponse
import com.example.blueprintproapps.models.MatchRequest
import com.example.blueprintproapps.models.MatchResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MatchClientActivity : AppCompatActivity() {

    private lateinit var matchRecyclerView: RecyclerView
    private lateinit var matchAdapter: MatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_match_client)

        // Adjust layout for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RecyclerView
        matchRecyclerView = findViewById(R.id.matchRecyclerView)
        matchRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Setup adapter with click listener
        matchAdapter = MatchAdapter { architectId ->
            sendMatchRequest(architectId)
        }
        matchRecyclerView.adapter = matchAdapter

        // Load available architects
        fetchMatches()
    }

    /**
     * Fetch all available architects (GET /api/MobileClient/Matches)
     */
    private fun fetchMatches() {
        ApiClient.instance.getMatches().enqueue(object : Callback<List<MatchResponse>> {
            override fun onResponse(
                call: Call<List<MatchResponse>>,
                response: Response<List<MatchResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val matches = response.body()!!
                    matchAdapter.submitList(matches)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("MatchClientActivity", "Failed to load matches: ${response.code()} - $errorBody")
                    Toast.makeText(
                        this@MatchClientActivity,
                        "Failed to load matches (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<MatchResponse>>, t: Throwable) {
                Log.e("MatchClientActivity", "Network error", t)
                Toast.makeText(
                    this@MatchClientActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Send a match request (POST /api/MobileClient/RequestMatch)
     */
    private fun sendMatchRequest(architectId: String) {
        val request = MatchRequest(architectId)

        ApiClient.instance.requestMatch(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(
                call: Call<GenericResponse>,
                response: Response<GenericResponse>
            ) {
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    Toast.makeText(
                        this@MatchClientActivity,
                        body.message ?: "Match request sent!",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchMatches() // Refresh after success
                } else {
                    val message = body?.message ?: "Failed to send request"
                    Log.e("MatchClientActivity", "Match request failed: $message")
                    Toast.makeText(
                        this@MatchClientActivity,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Log.e("MatchClientActivity", "Error sending match request", t)
                Toast.makeText(
                    this@MatchClientActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}

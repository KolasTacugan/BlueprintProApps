package com.example.blueprintproapps.network

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
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
import com.example.blueprintproapps.utils.ArchitectDetailBottomSheet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MatchClientActivity : AppCompatActivity() {

    private lateinit var matchRecyclerView: RecyclerView
    private lateinit var matchAdapter: MatchAdapter
    private lateinit var clientPrompt: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatContainer: LinearLayout
    private lateinit var loadingSection: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_match_client)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        matchRecyclerView = findViewById(R.id.matchRecyclerView)
        clientPrompt = findViewById(R.id.clientPrompt)
        sendButton = findViewById(R.id.sendButton)
        chatContainer = findViewById(R.id.chatContainer)

        // RecyclerView setup
        matchRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        matchAdapter = MatchAdapter(
            onRequestClick = { architectId ->
                sendMatchRequest(architectId)
            },
            onProfileClick = { match ->
                showArchitectBottomSheet(match)
            }
        )

        matchRecyclerView.adapter = matchAdapter

        loadingSection = findViewById(R.id.loadingSection)

        // Initial load
        fetchMatches()

        // Handle send button click
        sendButton.setOnClickListener {
            val query = clientPrompt.text.toString().trim()
            if (query.isNotEmpty()) {
                addChatMessage(query, isClient = true) // Add to chat
                fetchMatches(query) // Refresh matches
                clientPrompt.text.clear()
            } else {
                Toast.makeText(this, "Please enter what you're looking for.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showArchitectBottomSheet(match: MatchResponse) {
        val sheet = ArchitectDetailBottomSheet(match)
        sheet.show(supportFragmentManager, "ArchitectDetailBottomSheet")
    }

    /**
     * Add a chat message to the chat container dynamically
     */
    private fun addChatMessage(message: String, isClient: Boolean) {
        val inflater = LayoutInflater.from(this)
        val chatView = inflater.inflate(R.layout.item_chat_message, chatContainer, false)

        val textView = chatView.findViewById<TextView>(R.id.chatMessage)
        textView.text = message

        // Differentiate style for client vs system messages
        val layoutParams = textView.layoutParams as LinearLayout.LayoutParams
        if (isClient) {
            layoutParams.marginEnd = 60
            textView.setBackgroundResource(R.drawable.bg_client_message)
        } else {
            layoutParams.marginStart = 60
            textView.setBackgroundResource(R.drawable.bg_system_message)
        }

        textView.layoutParams = layoutParams
        chatContainer.addView(chatView)
    }

    /**
     * Fetch architects with optional query
     */
    private fun fetchMatches(query: String? = null) {

        loadingSection.visibility = View.VISIBLE
        matchRecyclerView.visibility = View.GONE

        ApiClient.instance.getMatches(query).enqueue(object : Callback<List<MatchResponse>> {
            override fun onResponse(
                call: Call<List<MatchResponse>>,
                response: Response<List<MatchResponse>>
            ) {
                loadingSection.visibility = View.GONE
                matchRecyclerView.visibility = View.VISIBLE

                if (response.isSuccessful && response.body() != null) {
                    val matches = response.body()!!
                    matchAdapter.submitList(matches)

                    // Add system message about loaded results
                    addChatMessage("Found ${matches.size} architect(s) for: ${query ?: "all"}", isClient = false)
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
                loadingSection.visibility = View.GONE
                matchRecyclerView.visibility = View.GONE
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
     * Send a match request ensuring clientId is from login
     */
    private fun sendMatchRequest(architectId: String) {
        val clientId = getClientIdFromPreferences()
        if (clientId.isEmpty()) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = MatchRequest(architectId, clientId)

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
                    fetchMatches()
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

    /**
     * Read clientId stored at login
     */
    private fun getClientIdFromPreferences(): String {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        return prefs.getString("clientId", "") ?: ""
    }
}

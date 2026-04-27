package com.example.blueprintproapps.network

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ChatHeadAdapter
import com.example.blueprintproapps.adapter.MessagesAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.databinding.ActivityMessagesBinding
import com.example.blueprintproapps.models.ConversationListResponse
import com.example.blueprintproapps.models.MatchListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var chatHeadAdapter: ChatHeadAdapter
    private var lastRefreshTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Load logged-in user's profile photo in the top search bar
        val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val userProfileUrl = sharedPref.getString("profilePhoto", null)
//        userProfileUrl?.let {
//            Glide.with(this)
//                .load(it)
//                .placeholder(R.drawable.profile_pic) // default fallback
//                .circleCrop() // optional: make it circular
//                .into(binding.imgUserProfile) // make sure this matches your ImageView ID
//        }

        // ✅ Setup RecyclerViews
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this)
        binding.recyclerChatHeads.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // ✅ Get clientId from SharedPreferences
        val clientId = sharedPref.getString("clientId", null)
        if (clientId.isNullOrEmpty()) {
            Toast.makeText(this, "Missing client ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ Initialize Adapters
        messageAdapter = MessagesAdapter(emptyList()) { conversation ->
            openChatActivity(
                conversation.architectId ?: "",
                conversation.architectName ?: "Unknown"
            )
        }

        chatHeadAdapter = ChatHeadAdapter(emptyList()) { match ->
            openChatActivity(
                match.architectId ?: "",
                match.architectName ?: "Unknown"
            )
        }

        binding.recyclerMessages.adapter = messageAdapter
        binding.recyclerChatHeads.adapter = chatHeadAdapter

        // ✅ Load data
        loadConversations(clientId)
        loadMatches(clientId)
    }


    override fun onResume() {
        super.onResume()
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime > 3000) { // refresh only if 3s passed
            val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val clientId = sharedPref.getString("clientId", null)
            if (!clientId.isNullOrEmpty()) {
                loadConversations(clientId)
                loadMatches(clientId)
            }
            lastRefreshTime = now
        }
    }
    // 🗨️ Load all existing conversations
    private fun loadConversations(clientId: String) {
        ApiClient.instance.getAllMessages(clientId)
            .enqueue(object : Callback<ConversationListResponse> {
                override fun onResponse(
                    call: Call<ConversationListResponse>,
                    response: Response<ConversationListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val conversations = response.body()!!.messages
                        messageAdapter = MessagesAdapter(conversations) { convo ->
                            openChatActivity(
                                convo.architectId ?: "",
                                convo.architectName ?: "Unknown"
                            )
                        }
                        binding.recyclerMessages.adapter = messageAdapter
                    } else {
                        Toast.makeText(
                            this@MessagesActivity,
                            "No conversations found.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ConversationListResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MessagesActivity,
                        "Network error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // 👥 Load all matched architects
    private fun loadMatches(clientId: String) {
        ApiClient.instance.getAllMatches(clientId)
            .enqueue(object : Callback<MatchListResponse> {
                override fun onResponse(
                    call: Call<MatchListResponse>,
                    response: Response<MatchListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val matches = response.body()!!.matches
                        chatHeadAdapter = ChatHeadAdapter(matches) { match ->
                            openChatActivity(
                                match.architectId ?: "",
                                match.architectName ?: "Unknown"
                            )
                        }
                        binding.recyclerChatHeads.adapter = chatHeadAdapter
                    } else {
                        Toast.makeText(
                            this@MessagesActivity,
                            "No matches found.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MatchListResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MessagesActivity,
                        "Network error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // 🚀 Open ChatActivity
    private fun openChatActivity(receiverId: String, receiverName: String) {
        if (receiverId.isEmpty()) {
            Toast.makeText(this, "Invalid chat target.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("receiverId", receiverId)
            putExtra("receiverName", receiverName)
        }
        startActivity(intent)
    }
}

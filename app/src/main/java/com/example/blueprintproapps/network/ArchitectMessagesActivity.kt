package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectChatHeadAdapter
import com.example.blueprintproapps.adapter.ArchitectMessagesAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.databinding.ActivityArchitectMessagesBinding
import com.example.blueprintproapps.models.ArchitectConversationListResponse
import com.example.blueprintproapps.models.ArchitectMatchListResponse
import com.example.blueprintproapps.navigation.AppNavDestination
import com.example.blueprintproapps.navigation.AppNavigator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ArchitectMessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArchitectMessagesBinding
    private lateinit var messageAdapter: ArchitectMessagesAdapter
    private lateinit var chatHeadAdapter: ArchitectChatHeadAdapter
    private var lastRefreshTime = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = AuthSessionManager.requireSession(this, UserRole.ARCHITECT) ?: return
        binding = ActivityArchitectMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Setup RecyclerViews
        binding.architectRecyclerMessages.layoutManager = LinearLayoutManager(this)
        binding.architectRecyclerChatHeads.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val architectId = session.userId

//        architectProfileUrl?.let {
//            Glide.with(this)
//                .load(it)
//                .placeholder(R.drawable.profile_pic)
//                .circleCrop()
//                .into(binding.architectProfilePic)
//        }
        // ✅ Initialize Adapters
        messageAdapter = ArchitectMessagesAdapter(emptyList()) { conversation ->
            openChatActivity(
                conversation.clientId ?: "",
                conversation.clientName ?: "Unknown",
                conversation.profileUrl
            )
        }

        chatHeadAdapter = ArchitectChatHeadAdapter(emptyList()) { match ->
            openChatActivity(
                match.clientId ?: "",
                match.clientName ?: "Unknown",
                match.clientPhoto
            )
        }

        binding.architectRecyclerMessages.adapter = messageAdapter
        binding.architectRecyclerChatHeads.adapter = chatHeadAdapter

        AppNavigator.bind(
            activity = this,
            bottomNavigationView = binding.architectBottomNavigation,
            role = UserRole.ARCHITECT,
            currentDestination = AppNavDestination.MESSAGES
        )

        // ✅ Load data
        loadConversations(architectId)
        loadMatches(architectId)
    }
    override fun onResume() {
        super.onResume()

        val now = System.currentTimeMillis()
        if (now - lastRefreshTime > 3000) { // refresh only if at least 3 seconds passed
            val architectId = AuthSessionManager.requireSession(this, UserRole.ARCHITECT)?.userId ?: return
            loadConversations(architectId)
            loadMatches(architectId)
            lastRefreshTime = now
        }
    }
    // 🗨️ Load all conversations for architect
    private fun loadConversations(architectId: String) {
        ApiClient.instance.getAllMessagesForArchitect(architectId)
            .enqueue(object : Callback<ArchitectConversationListResponse> {
                override fun onResponse(
                    call: Call<ArchitectConversationListResponse>,
                    response: Response<ArchitectConversationListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val conversations = response.body()!!.messages
                        messageAdapter = ArchitectMessagesAdapter(conversations) { convo ->
                            openChatActivity(
                                convo.clientId ?: "",
                                convo.clientName ?: "Unknown",
                                convo.profileUrl
                            )
                        }
                        binding.architectRecyclerMessages.adapter = messageAdapter
                    } else {
                        Toast.makeText(
                            this@ArchitectMessagesActivity,
                            "No conversations found.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ArchitectConversationListResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ArchitectMessagesActivity,
                        "Network error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // 👥 Load all matched clients
    private fun loadMatches(architectId: String) {
        ApiClient.instance.getAllMatchesForArchitect(architectId)
            .enqueue(object : Callback<ArchitectMatchListResponse> {
                override fun onResponse(
                    call: Call<ArchitectMatchListResponse>,
                    response: Response<ArchitectMatchListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val matches = response.body()!!.matches
                        chatHeadAdapter = ArchitectChatHeadAdapter(matches) { match ->
                            openChatActivity(
                                match.clientId ?: "",
                                match.clientName ?: "Unknown",
                                match.clientPhoto
                            )
                        }
                        binding.architectRecyclerChatHeads.adapter = chatHeadAdapter
                    } else {
                        Toast.makeText(
                            this@ArchitectMessagesActivity,
                            "No matches found.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ArchitectMatchListResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ArchitectMessagesActivity,
                        "Network error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // 🚀 Open ChatActivity
    private fun openChatActivity(receiverId: String, receiverName: String, receiverPhoto: String? = null) {
        if (receiverId.isEmpty()) {
            Toast.makeText(this, "Invalid chat target.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ArchitectChatActivity::class.java).apply {
            putExtra("receiverId", receiverId)
            putExtra("clientName", receiverName)
            putExtra("clientPhoto", receiverPhoto)
        }
        startActivity(intent)
    }
}

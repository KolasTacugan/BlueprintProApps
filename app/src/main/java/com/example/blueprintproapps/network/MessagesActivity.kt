package com.example.blueprintproapps.network

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
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.databinding.ActivityMessagesBinding
import com.example.blueprintproapps.models.ConversationListResponse
import com.example.blueprintproapps.models.MatchListResponse
import com.example.blueprintproapps.navigation.AppNavDestination
import com.example.blueprintproapps.navigation.AppNavigator
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
        val session = AuthSessionManager.requireSession(this, UserRole.CLIENT) ?: return
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val clientId = session.userId
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

        // ✅ Initialize Adapters
        messageAdapter = MessagesAdapter(emptyList()) { conversation ->
            openChatActivity(
                conversation.architectId ?: "",
                conversation.architectName ?: "Unknown",
                conversation.profileUrl
            )
        }

        chatHeadAdapter = ChatHeadAdapter(emptyList()) { match ->
            openChatActivity(
                match.architectId ?: "",
                match.architectName ?: "Unknown",
                match.architectPhoto
            )
        }

        binding.recyclerMessages.adapter = messageAdapter
        binding.recyclerChatHeads.adapter = chatHeadAdapter

        AppNavigator.bind(
            activity = this,
            bottomNavigationView = binding.bottomNavigation,
            role = UserRole.CLIENT,
            currentDestination = AppNavDestination.MESSAGES
        )

        // ✅ Load data
        loadConversations(clientId)
        loadMatches(clientId)
    }


    override fun onResume() {
        super.onResume()
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime > 3000) { // refresh only if 3s passed
            val clientId = AuthSessionManager.requireSession(this, UserRole.CLIENT)?.userId ?: return
            loadConversations(clientId)
            loadMatches(clientId)
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
                                convo.architectName ?: "Unknown",
                                convo.profileUrl
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
                                match.architectName ?: "Unknown",
                                match.architectPhoto
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
    private fun openChatActivity(receiverId: String, receiverName: String, receiverPhoto: String? = null) {
        if (receiverId.isEmpty()) {
            Toast.makeText(this, "Invalid chat target.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("receiverId", receiverId)
            putExtra("receiverName", receiverName)
            putExtra("receiverPhoto", receiverPhoto)
        }
        startActivity(intent)
    }
}

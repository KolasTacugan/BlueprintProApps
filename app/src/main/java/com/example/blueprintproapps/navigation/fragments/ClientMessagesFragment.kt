package com.example.blueprintproapps.navigation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ChatHeadAdapter
import com.example.blueprintproapps.adapter.MessagesAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ConversationListResponse
import com.example.blueprintproapps.models.MatchListResponse
import com.example.blueprintproapps.network.ChatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientMessagesFragment : Fragment(R.layout.fragment_client_messages) {
    private lateinit var messages: androidx.recyclerview.widget.RecyclerView
    private lateinit var heads: androidx.recyclerview.widget.RecyclerView
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var chatHeadAdapter: ChatHeadAdapter
    private var conversationsCall: Call<ConversationListResponse>? = null
    private var matchesCall: Call<MatchListResponse>? = null
    private var viewDestroyed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false
        val session = AuthSessionManager.requireSession(requireActivity(), UserRole.CLIENT) ?: return
        messages = view.findViewById(R.id.recyclerMessages)
        heads = view.findViewById(R.id.recyclerChatHeads)
        messages.layoutManager = LinearLayoutManager(requireContext())
        heads.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        messageAdapter = MessagesAdapter(emptyList()) { openChat(it.architectId ?: "", it.architectName ?: "Unknown") }
        chatHeadAdapter = ChatHeadAdapter(emptyList()) { openChat(it.architectId ?: "", it.architectName ?: "Unknown") }
        messages.adapter = messageAdapter
        heads.adapter = chatHeadAdapter
        loadConversations(session.userId)
        loadMatches(session.userId)
    }

    private fun loadConversations(clientId: String) {
        conversationsCall = ApiClient.instance.getAllMessages(clientId)
        conversationsCall?.enqueue(object : Callback<ConversationListResponse> {
            override fun onResponse(call: Call<ConversationListResponse>, response: Response<ConversationListResponse>) {
                if (!isAdded || viewDestroyed || view == null) return
                val list = response.body()?.messages.orEmpty()
                messageAdapter = MessagesAdapter(list) { openChat(it.architectId ?: "", it.architectName ?: "Unknown") }
                messages.adapter = messageAdapter
            }
            override fun onFailure(call: Call<ConversationListResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                val safeContext = context ?: return
                Toast.makeText(safeContext, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadMatches(clientId: String) {
        matchesCall = ApiClient.instance.getAllMatches(clientId)
        matchesCall?.enqueue(object : Callback<MatchListResponse> {
            override fun onResponse(call: Call<MatchListResponse>, response: Response<MatchListResponse>) {
                if (!isAdded || viewDestroyed || view == null) return
                val list = response.body()?.matches.orEmpty()
                chatHeadAdapter = ChatHeadAdapter(list) { openChat(it.architectId ?: "", it.architectName ?: "Unknown") }
                heads.adapter = chatHeadAdapter
            }
            override fun onFailure(call: Call<MatchListResponse>, t: Throwable) = Unit
        })
    }

    private fun openChat(receiverId: String, receiverName: String) {
        if (receiverId.isEmpty()) return
        startActivity(Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra("receiverId", receiverId)
            putExtra("receiverName", receiverName)
        })
    }

    override fun onDestroyView() {
        conversationsCall?.cancel()
        matchesCall?.cancel()
        conversationsCall = null
        matchesCall = null
        viewDestroyed = true
        super.onDestroyView()
    }
}

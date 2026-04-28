package com.example.blueprintproapps.navigation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectChatHeadAdapter
import com.example.blueprintproapps.adapter.ArchitectMessagesAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ArchitectConversationListResponse
import com.example.blueprintproapps.models.ArchitectMatchListResponse
import com.example.blueprintproapps.network.ArchitectChatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectMessagesFragment : Fragment(R.layout.fragment_architect_messages) {
    private lateinit var messages: androidx.recyclerview.widget.RecyclerView
    private lateinit var heads: androidx.recyclerview.widget.RecyclerView
    private lateinit var messageAdapter: ArchitectMessagesAdapter
    private lateinit var chatHeadAdapter: ArchitectChatHeadAdapter
    private var conversationsCall: Call<ArchitectConversationListResponse>? = null
    private var matchesCall: Call<ArchitectMatchListResponse>? = null
    private var viewDestroyed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false
        val session = AuthSessionManager.requireSession(requireActivity(), UserRole.ARCHITECT) ?: return
        messages = view.findViewById(R.id.architectRecyclerMessages)
        heads = view.findViewById(R.id.architectRecyclerChatHeads)
        messages.layoutManager = LinearLayoutManager(requireContext())
        heads.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        messageAdapter = ArchitectMessagesAdapter(emptyList()) { openChat(it.clientId ?: "", it.clientName ?: "Unknown") }
        chatHeadAdapter = ArchitectChatHeadAdapter(emptyList()) { openChat(it.clientId ?: "", it.clientName ?: "Unknown") }
        messages.adapter = messageAdapter
        heads.adapter = chatHeadAdapter
        loadConversations(session.userId)
        loadMatches(session.userId)
    }

    private fun loadConversations(architectId: String) {
        conversationsCall = ApiClient.instance.getAllMessagesForArchitect(architectId)
        conversationsCall?.enqueue(object : Callback<ArchitectConversationListResponse> {
            override fun onResponse(call: Call<ArchitectConversationListResponse>, response: Response<ArchitectConversationListResponse>) {
                if (!isAdded || viewDestroyed || view == null) return
                val list = response.body()?.messages.orEmpty()
                messageAdapter = ArchitectMessagesAdapter(list) { openChat(it.clientId ?: "", it.clientName ?: "Unknown") }
                messages.adapter = messageAdapter
            }
            override fun onFailure(call: Call<ArchitectConversationListResponse>, t: Throwable) = Unit
        })
    }

    private fun loadMatches(architectId: String) {
        matchesCall = ApiClient.instance.getAllMatchesForArchitect(architectId)
        matchesCall?.enqueue(object : Callback<ArchitectMatchListResponse> {
            override fun onResponse(call: Call<ArchitectMatchListResponse>, response: Response<ArchitectMatchListResponse>) {
                if (!isAdded || viewDestroyed || view == null) return
                val list = response.body()?.matches.orEmpty()
                chatHeadAdapter = ArchitectChatHeadAdapter(list) { openChat(it.clientId ?: "", it.clientName ?: "Unknown") }
                heads.adapter = chatHeadAdapter
            }
            override fun onFailure(call: Call<ArchitectMatchListResponse>, t: Throwable) = Unit
        })
    }

    private fun openChat(receiverId: String, receiverName: String) {
        if (receiverId.isEmpty()) return
        startActivity(Intent(requireContext(), ArchitectChatActivity::class.java).apply {
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

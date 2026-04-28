package com.example.blueprintproapps.navigation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import com.example.blueprintproapps.models.ConversationResponse
import com.example.blueprintproapps.models.MatchListResponse
import com.example.blueprintproapps.models.MatchResponse2
import com.example.blueprintproapps.network.ChatActivity
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientMessagesFragment : Fragment(R.layout.fragment_client_messages) {
    private lateinit var messages: androidx.recyclerview.widget.RecyclerView
    private lateinit var heads: androidx.recyclerview.widget.RecyclerView
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var chatHeadAdapter: ChatHeadAdapter
    private lateinit var messageState: TextView
    private lateinit var retryButton: MaterialButton
    private var conversationsCall: Call<ConversationListResponse>? = null
    private var matchesCall: Call<MatchListResponse>? = null
    private var currentClientId: String? = null
    private var conversationsLoaded = false
    private var matchesLoaded = false
    private var latestConversations: List<ConversationResponse> = emptyList()
    private var latestMatches: List<MatchResponse2> = emptyList()
    private var viewDestroyed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false
        val session = AuthSessionManager.requireSession(requireActivity(), UserRole.CLIENT) ?: return
        currentClientId = session.userId
        messages = view.findViewById(R.id.recyclerMessages)
        heads = view.findViewById(R.id.recyclerChatHeads)
        messageState = view.findViewById(R.id.tvMessageState)
        retryButton = view.findViewById(R.id.btnRetryMessages)
        messages.layoutManager = LinearLayoutManager(requireContext())
        heads.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        messages.isNestedScrollingEnabled = false
        heads.isNestedScrollingEnabled = false
        messageAdapter = MessagesAdapter(emptyList()) { openChat(it.architectId, it.architectName ?: "Unknown", it.profileUrl) }
        chatHeadAdapter = ChatHeadAdapter(emptyList()) { openChat(it.architectId, it.architectName ?: "Unknown", it.architectPhoto) }
        messages.adapter = messageAdapter
        heads.adapter = chatHeadAdapter

        retryButton.setOnClickListener { currentClientId?.let { reload(it) } }
        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.icSectionMatchedContacts), "md-people")
        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.icSectionConversations), "md-chat")
        UiEffects.applyCascadingEntrance(
            listOf<View>(view.findViewById(R.id.messageStatsStrip), heads, messages),
            80L
        )

        reload(session.userId)
    }

    private fun reload(clientId: String) {
        conversationsLoaded = false
        matchesLoaded = false
        latestConversations = emptyList()
        latestMatches = emptyList()
        renderState("Loading messages...", showRetry = false)
        loadConversations(clientId)
        loadMatches(clientId)
    }

    private fun loadConversations(clientId: String) {
        conversationsCall = ApiClient.instance.getAllMessages(clientId)
        conversationsCall?.enqueue(object : Callback<ConversationListResponse> {
            override fun onResponse(call: Call<ConversationListResponse>, response: Response<ConversationListResponse>) {
                if (!isAdded || viewDestroyed || view == null) return
                val list = response.body()?.messages.orEmpty()
                latestConversations = list
                conversationsLoaded = true
                view?.findViewById<TextView>(R.id.tvConversationCount)?.text = list.size.toString()
                messageAdapter = MessagesAdapter(list) { openChat(it.architectId, it.architectName ?: "Unknown", it.profileUrl) }
                messages.adapter = messageAdapter
                updateStateIfReady()
            }
            override fun onFailure(call: Call<ConversationListResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                renderState("Network error while loading conversations.", showRetry = true)
            }
        })
    }

    private fun loadMatches(clientId: String) {
        matchesCall = ApiClient.instance.getAllMatches(clientId)
        matchesCall?.enqueue(object : Callback<MatchListResponse> {
            override fun onResponse(call: Call<MatchListResponse>, response: Response<MatchListResponse>) {
                if (!isAdded || viewDestroyed || view == null) return
                val list = response.body()?.matches.orEmpty().filter { isMessageable(it.matchStatus) }
                latestMatches = list
                matchesLoaded = true
                view?.findViewById<TextView>(R.id.tvMatchedCount)?.text = list.size.toString()
                chatHeadAdapter = ChatHeadAdapter(list) { openChat(it.architectId, it.architectName ?: "Unknown", it.architectPhoto) }
                heads.adapter = chatHeadAdapter
                updateStateIfReady()
            }
            override fun onFailure(call: Call<MatchListResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                renderState("Network error while loading approved matches.", showRetry = true)
            }
        })
    }

    private fun openChat(receiverId: String, receiverName: String, receiverPhoto: String? = null) {
        if (receiverId.isEmpty()) return
        startActivity(Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra("receiverId", receiverId)
            putExtra("receiverName", receiverName)
            putExtra("receiverPhoto", receiverPhoto)
        })
    }

    private fun updateStateIfReady() {
        if (!conversationsLoaded || !matchesLoaded) return
        val message = if (latestConversations.isEmpty() && latestMatches.isEmpty()) {
            "No approved matches or conversations yet. Find an architect to start messaging."
        } else {
            ""
        }
        renderState(message, showRetry = false)
    }

    private fun renderState(message: String, showRetry: Boolean) {
        if (!::messageState.isInitialized || !::retryButton.isInitialized) return
        messageState.text = message
        messageState.visibility = if (message.isBlank()) View.GONE else View.VISIBLE
        retryButton.visibility = if (showRetry) View.VISIBLE else View.GONE
    }

    private fun isMessageable(status: String?): Boolean {
        return status.isNullOrBlank() || status.equals("Approved", ignoreCase = true)
    }

    override fun onDestroyView() {
        conversationsCall?.cancel()
        matchesCall?.cancel()
        conversationsCall = null
        matchesCall = null
        currentClientId = null
        viewDestroyed = true
        super.onDestroyView()
    }
}

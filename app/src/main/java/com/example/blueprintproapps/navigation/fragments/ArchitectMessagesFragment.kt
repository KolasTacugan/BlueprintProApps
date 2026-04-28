package com.example.blueprintproapps.navigation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectChatHeadAdapter
import com.example.blueprintproapps.adapter.ArchitectMessagesAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ArchitectConversationListResponse
import com.example.blueprintproapps.models.ArchitectConversationResponse
import com.example.blueprintproapps.models.ArchitectMatchListResponse
import com.example.blueprintproapps.models.ArchitectMatchResponse
import com.example.blueprintproapps.network.ArchitectChatActivity
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectMessagesFragment : Fragment(R.layout.fragment_architect_messages) {
    private lateinit var messages: androidx.recyclerview.widget.RecyclerView
    private lateinit var heads: androidx.recyclerview.widget.RecyclerView
    private lateinit var messageAdapter: ArchitectMessagesAdapter
    private lateinit var chatHeadAdapter: ArchitectChatHeadAdapter
    private lateinit var messageState: TextView
    private lateinit var retryButton: MaterialButton
    private var conversationsCall: Call<ArchitectConversationListResponse>? = null
    private var matchesCall: Call<ArchitectMatchListResponse>? = null
    private var currentArchitectId: String? = null
    private var conversationsLoaded = false
    private var matchesLoaded = false
    private var latestConversations: List<ArchitectConversationResponse> = emptyList()
    private var latestMatches: List<ArchitectMatchResponse> = emptyList()
    private var viewDestroyed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false
        val session = AuthSessionManager.requireSession(requireActivity(), UserRole.ARCHITECT) ?: return
        currentArchitectId = session.userId
        messages = view.findViewById(R.id.architectRecyclerMessages)
        heads = view.findViewById(R.id.architectRecyclerChatHeads)
        messageState = view.findViewById(R.id.architectMessageState)
        retryButton = view.findViewById(R.id.architectBtnRetryMessages)
        messages.layoutManager = LinearLayoutManager(requireContext())
        heads.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        messages.isNestedScrollingEnabled = false
        heads.isNestedScrollingEnabled = false
        messageAdapter = ArchitectMessagesAdapter(emptyList()) { openChat(it.clientId, it.clientName ?: "Unknown", it.profileUrl) }
        chatHeadAdapter = ArchitectChatHeadAdapter(emptyList()) { openChat(it.clientId, it.clientName ?: "Unknown", it.clientPhoto) }
        messages.adapter = messageAdapter
        heads.adapter = chatHeadAdapter

        retryButton.setOnClickListener { currentArchitectId?.let { reload(it) } }
        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.architectIcSectionMatchedContacts), "md-people")
        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.architectIcSectionConversations), "md-chat")
        UiEffects.applyCascadingEntrance(
            listOf<View>(view.findViewById(R.id.architectMessageStatsStrip), heads, messages),
            80L
        )

        reload(session.userId)
    }

    private fun reload(architectId: String) {
        conversationsLoaded = false
        matchesLoaded = false
        latestConversations = emptyList()
        latestMatches = emptyList()
        renderState("Loading messages...", showRetry = false)
        loadConversations(architectId)
        loadMatches(architectId)
    }

    private fun loadConversations(architectId: String) {
        conversationsCall = ApiClient.instance.getAllMessagesForArchitect(architectId)
        conversationsCall?.enqueue(object : Callback<ArchitectConversationListResponse> {
            override fun onResponse(call: Call<ArchitectConversationListResponse>, response: Response<ArchitectConversationListResponse>) {
                if (!isAdded || viewDestroyed || view == null) return
                val list = response.body()?.messages.orEmpty()
                latestConversations = list
                conversationsLoaded = true
                view?.findViewById<TextView>(R.id.architectConversationCount)?.text = list.size.toString()
                messageAdapter = ArchitectMessagesAdapter(list) { openChat(it.clientId, it.clientName ?: "Unknown", it.profileUrl) }
                messages.adapter = messageAdapter
                updateStateIfReady()
            }
            override fun onFailure(call: Call<ArchitectConversationListResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                renderState("Network error while loading conversations.", showRetry = true)
            }
        })
    }

    private fun loadMatches(architectId: String) {
        matchesCall = ApiClient.instance.getAllMatchesForArchitect(architectId)
        matchesCall?.enqueue(object : Callback<ArchitectMatchListResponse> {
            override fun onResponse(call: Call<ArchitectMatchListResponse>, response: Response<ArchitectMatchListResponse>) {
                if (!isAdded || viewDestroyed || view == null) return
                val list = response.body()?.matches.orEmpty().filter { isMessageable(it.matchStatus) }
                latestMatches = list
                matchesLoaded = true
                view?.findViewById<TextView>(R.id.architectMatchedCount)?.text = list.size.toString()
                chatHeadAdapter = ArchitectChatHeadAdapter(list) { openChat(it.clientId, it.clientName ?: "Unknown", it.clientPhoto) }
                heads.adapter = chatHeadAdapter
                updateStateIfReady()
            }
            override fun onFailure(call: Call<ArchitectMatchListResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                renderState("Network error while loading approved matches.", showRetry = true)
            }
        })
    }

    private fun openChat(receiverId: String, receiverName: String, receiverPhoto: String? = null) {
        if (receiverId.isEmpty()) return
        startActivity(Intent(requireContext(), ArchitectChatActivity::class.java).apply {
            putExtra("receiverId", receiverId)
            putExtra("receiverName", receiverName)
            putExtra("receiverPhoto", receiverPhoto)
        })
    }

    private fun updateStateIfReady() {
        if (!conversationsLoaded || !matchesLoaded) return
        val message = if (latestConversations.isEmpty() && latestMatches.isEmpty()) {
            "No approved client matches or conversations yet."
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
        currentArchitectId = null
        viewDestroyed = true
        super.onDestroyView()
    }
}

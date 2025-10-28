package com.example.blueprintproapps.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ChatHeadAdapter
import com.example.blueprintproapps.adapter.MessagesAdapter
import com.example.blueprintproapps.models.ConversationResponse
import com.example.blueprintproapps.network.MessagesActivity

class MessagesFragment : Fragment() {

    private lateinit var recyclerMessages: RecyclerView
    private lateinit var recyclerChatHeads: RecyclerView   // ✅ Added this
    private lateinit var messageAdapter: MessagesAdapter


    private val conversationList = mutableListOf<ConversationResponse>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragmenent_messages, container, false)

        // 🟢 Initialize RecyclerViews
        recyclerMessages = view.findViewById(R.id.recyclerMessages)
         // ✅ FIXED

        // 🟣 Setup Messages list (vertical)
        recyclerMessages.layoutManager = LinearLayoutManager(requireContext())
        messageAdapter = MessagesAdapter(conversationList) { conversation ->
            val intent = Intent(requireContext(), MessagesActivity::class.java)
            intent.putExtra("architectId", conversation.architectId)
            startActivity(intent)
        }
        recyclerMessages.adapter = messageAdapter

        // 🔵 Setup Chat Heads (horizontal)
        recyclerChatHeads.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Optional: loadConversations() if you connect to backend later
        // loadConversations()

        return view
    }
}

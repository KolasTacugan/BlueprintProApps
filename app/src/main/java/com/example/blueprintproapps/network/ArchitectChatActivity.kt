package com.example.blueprintproapps.network

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectChatMessagesAdapter

import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.GenericResponse
import com.example.blueprintproapps.models.MessageListResponse
import com.example.blueprintproapps.models.MessageRequest
import com.example.blueprintproapps.models.MessageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectChatActivity : AppCompatActivity() {

    private lateinit var recyclerMessages: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var adapter: ArchitectChatMessagesAdapter
    private val messages = mutableListOf<MessageResponse>()

    private lateinit var architectId: String
    private lateinit var clientId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_architect_chat)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerMessages = findViewById(R.id.recyclerMessages)
        edtMessage = findViewById(R.id.edtMessage)
        btnSend = findViewById(R.id.btnSend)

        // ✅ Get architect ID from SharedPreferences
        val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        architectId = sharedPref.getString("architectId", null) ?: run {
            Toast.makeText(this, "Architect not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ Get client ID from Intent extras
        clientId = intent.getStringExtra("clientId") ?: run {
            Toast.makeText(this, "No client specified", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ Setup RecyclerView
        adapter = ArchitectChatMessagesAdapter(messages, architectId)
        recyclerMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerMessages.adapter = adapter

        // ✅ Load chat messages
        getMessages()

        // ✅ Send message on button click
        btnSend.setOnClickListener {
            val messageText = edtMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }
    }

    // ✅ Fetch all messages between architect and client
    private fun getMessages() {
        ApiClient.instance.getArchitectMessages(clientId, architectId)
            .enqueue(object : Callback<MessageListResponse> {
                override fun onResponse(
                    call: Call<MessageListResponse>,
                    response: Response<MessageListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        messages.clear()
                        messages.addAll(response.body()!!.messages)
                        adapter.notifyDataSetChanged()
                        recyclerMessages.scrollToPosition(messages.size - 1)
                    } else {
                        Toast.makeText(
                            this@ArchitectChatActivity,
                            "Failed to load messages",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MessageListResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ArchitectChatActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // ✅ Send message
    private fun sendMessage(messageText: String) {
        btnSend.isEnabled = false

        val messageRequest = MessageRequest(
            clientId = clientId,
            architectId = architectId,
            senderId = architectId,
            messageBody = messageText
        )

        ApiClient.instance.sendArchitectMessage(messageRequest)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(
                    call: Call<GenericResponse>,
                    response: Response<GenericResponse>
                ) {
                    btnSend.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        edtMessage.text.clear()
                        getMessages()
                    } else {
                        Toast.makeText(
                            this@ArchitectChatActivity,
                            "Failed to send message",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    btnSend.isEnabled = true
                    Toast.makeText(
                        this@ArchitectChatActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}

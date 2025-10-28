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
import com.example.blueprintproapps.adapter.ChatMessagesAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.GenericResponse
import com.example.blueprintproapps.models.MessageListResponse
import com.example.blueprintproapps.models.MessageRequest
import com.example.blueprintproapps.models.MessageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerMessages: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var adapter: ChatMessagesAdapter
    private val messages = mutableListOf<MessageResponse>()

    private lateinit var senderId: String
    private lateinit var receiverId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        // Handle window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerMessages = findViewById(R.id.recyclerMessages)
        edtMessage = findViewById(R.id.edtMessage)
        btnSend = findViewById(R.id.btnSend)


        // ✅ Get current user ID (client or architect) from SharedPreferences
        val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

        senderId = sharedPref.getString("clientId", null)
            ?: sharedPref.getString("userId", null)
                    ?: run {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                finish()
                return
            }



        // ✅ Get receiver ID from Intent extras
        receiverId = intent.getStringExtra("receiverId") ?: run {
            Toast.makeText(this, "No recipient specified", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ Setup RecyclerView
        adapter = ChatMessagesAdapter(messages, senderId)
        recyclerMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // ensures newest messages show at bottom
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

    // ✅ Fetch all messages between sender and receiver
    private fun getMessages() {
        ApiClient.instance.getMessages(senderId, receiverId)
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
                            this@ChatActivity,
                            "Failed to load messages",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MessageListResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ChatActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // ✅ Send message to server
    private fun sendMessage(messageText: String) {
        btnSend.isEnabled = false

        val messageRequest = MessageRequest(
            clientId = senderId,
            architectId = receiverId,
            senderId = senderId,
            messageBody = messageText
        )

        ApiClient.instance.sendMessage(messageRequest)
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
                            this@ChatActivity,
                            "Failed to send message",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    btnSend.isEnabled = true
                    Toast.makeText(
                        this@ChatActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}

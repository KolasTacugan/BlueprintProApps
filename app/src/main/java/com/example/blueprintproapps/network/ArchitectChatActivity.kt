package com.example.blueprintproapps.network

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
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
import android.widget.TextView

class ArchitectChatActivity : AppCompatActivity() {

    private lateinit var recyclerMessages: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var adapter: ArchitectChatMessagesAdapter
    private val messages = mutableListOf<MessageResponse>()

    private val handler = android.os.Handler()
    private val refreshRunnable = object : Runnable {
        override fun run() {
            getMessages()
            handler.postDelayed(this, 2000)
        }
    }
    private lateinit var architectId: String
    private lateinit var clientId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_architect_chat)

        // 🔑 Get receiver data
        val receiverId = intent.getStringExtra("receiverId") ?: run {
            Toast.makeText(this, "No client specified", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val receiverName = intent.getStringExtra("receiverName") ?: "Chat"
        val txtChatTitle = findViewById<TextView>(R.id.txtChatTitle)
        txtChatTitle.text = receiverName

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        recyclerMessages = findViewById(R.id.recyclerMessages)
        edtMessage = findViewById(R.id.edtMessage)
        btnSend = findViewById(R.id.btnSend)

        // Get architect ID
        val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        architectId = sharedPref.getString("architectId", null) ?: run {
            Toast.makeText(this, "Architect not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Get client ID
        clientId = intent.getStringExtra("receiverId") ?: run {
            Toast.makeText(this, "No client specified", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // RecyclerView setup
        adapter = ArchitectChatMessagesAdapter(messages, architectId)
        recyclerMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerMessages.adapter = adapter

        // Load chat messages initially
        getMessages()
        startAutoRefresh()

        // Send message
        btnSend.setOnClickListener {
            val msg = edtMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                sendMessage(msg)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable)
    }

    // Fetch chat messages
    private fun getMessages() {
        ApiClient.instance.getArchitectMessages(clientId, architectId)
            .enqueue(object : Callback<MessageListResponse> {
                override fun onResponse(
                    call: Call<MessageListResponse>,
                    response: Response<MessageListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {

                        val newMessages = response.body()!!.messages

                        messages.clear()
                        messages.addAll(newMessages)
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

    private fun startAutoRefresh() {
        handler.post(refreshRunnable)
    }


    // Sending a message
    private fun sendMessage(messageText: String) {
        btnSend.isEnabled = false

        val messageReq = MessageRequest(
            clientId = clientId,
            architectId = architectId,
            senderId = architectId,
            messageBody = messageText
        )

        ApiClient.instance.sendArchitectMessage(messageReq)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(
                    call: Call<GenericResponse>,
                    response: Response<GenericResponse>
                ) {
                    btnSend.isEnabled = true

                    if (response.isSuccessful && response.body()?.success == true) {
                        edtMessage.text.clear()

                        // Refresh messages immediately
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

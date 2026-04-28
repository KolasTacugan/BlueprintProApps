package com.example.blueprintproapps.network

import android.os.Bundle
import android.view.View
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
import com.example.blueprintproapps.adapter.ChatMessagesAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.GenericResponse
import com.example.blueprintproapps.models.MessageListResponse
import com.example.blueprintproapps.models.MessageRequest
import com.example.blueprintproapps.models.MessageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.TextView
import com.bumptech.glide.Glide

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerMessages: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: android.view.View
    private lateinit var adapter: ChatMessagesAdapter
    private val messages = mutableListOf<MessageResponse>()
    private var refreshHandler = android.os.Handler()
    private lateinit var refreshRunnable: Runnable
    private lateinit var senderId: String
    private lateinit var receiverId: String
    private var hasShownLoadError = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = AuthSessionManager.requireSession(this, UserRole.CLIENT) ?: return
        //enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        recyclerMessages = findViewById(R.id.recyclerMessages)
        edtMessage = findViewById(R.id.edtMessage)
        btnSend = findViewById(R.id.btnSend)

        val btnBack = findViewById<android.view.View>(R.id.btnBack)
        val txtRecipientName = findViewById<TextView>(R.id.txtRecipientName)
        val txtStatus = findViewById<TextView>(R.id.txtStatus)
        val imgRecipientProfile = findViewById<android.widget.ImageView>(R.id.imgRecipientProfile)
        val btnAttach = findViewById<View>(R.id.btnAttach)
        val btnInfo = findViewById<View>(R.id.btnInfo)

        val receiverName = intent.getStringExtra("receiverName") ?: "Chat"
        val receiverPhoto = intent.getStringExtra("receiverPhoto")
        
        txtRecipientName.text = receiverName
        txtStatus.text = "Approved match"
        btnBack.setOnClickListener { finish() }
        btnAttach.visibility = View.GONE
        btnInfo.setOnClickListener {
            Toast.makeText(this, "Profile details are available from Messages.", Toast.LENGTH_SHORT).show()
        }

        Glide.with(this)
            .load(receiverPhoto)
            .placeholder(R.drawable.sample_profile)
            .into(imgRecipientProfile)

        senderId = session.userId

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
        startAutoRefresh()

        // ✅ Send message on button click
        btnSend.setOnClickListener {
            val messageText = edtMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }
    }

    private fun startAutoRefresh() {
        refreshRunnable = Runnable {
            getMessages()
            refreshHandler.postDelayed(refreshRunnable, 5000)
        }
        refreshHandler.post(refreshRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshHandler.removeCallbacks(refreshRunnable)
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
                        if (messages.isNotEmpty()) {
                            recyclerMessages.scrollToPosition(messages.size - 1)
                        }
                    } else {
                        showLoadErrorOnce("Failed to load messages")
                    }
                }

                override fun onFailure(call: Call<MessageListResponse>, t: Throwable) {
                    showLoadErrorOnce("Error: ${t.message}")
                }
            })
    }

    private fun showLoadErrorOnce(message: String) {
        if (hasShownLoadError) return
        hasShownLoadError = true
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

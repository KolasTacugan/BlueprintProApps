package com.example.blueprintproapps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.MessageResponse
import com.example.blueprintproapps.utils.DateTimeUtils

class ChatMessagesAdapter(
    private val messages: MutableList<MessageResponse>,   // <-- MUTABLE LIST
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SENT = 1
    private val TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SENT) {
            val view = inflater.inflate(R.layout.item_chat_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        val formattedTime = DateTimeUtils.formatPhilippineTime(message.messageDate)

        if (holder is SentMessageViewHolder) {
            holder.txtMessage.text = message.messageBody
            holder.txtTime.text = formattedTime
        } else if (holder is ReceivedMessageViewHolder) {
            holder.txtMessage.text = message.messageBody
            holder.txtTime.text = formattedTime
        }
    }

    override fun getItemCount(): Int = messages.size

    // 🔥 FUNCTION TO UPDATE MESSAGES IN REAL-TIME
    fun updateMessages(newList: List<MessageResponse>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMessage: TextView = itemView.findViewById(R.id.txtMessageSent)
        val txtTime: TextView = itemView.findViewById(R.id.txtSentTime)
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMessage: TextView = itemView.findViewById(R.id.txtMessageReceived)
        val txtTime: TextView = itemView.findViewById(R.id.txtReceivedTime)
    }
}

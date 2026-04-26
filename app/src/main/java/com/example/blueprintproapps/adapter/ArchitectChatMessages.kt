package com.example.blueprintproapps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.MessageResponse

class ArchitectChatMessagesAdapter(
    private val messages: MutableList<MessageResponse>,
    private val currentArchitectId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SENT = 1
    private val TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentArchitectId)
            TYPE_SENT
        else
            TYPE_RECEIVED
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

        // Format date: "2026-04-26T13:44:30" -> "1:44 PM"
        val formattedTime = try {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).parse(message.messageDate)
            java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(date)
        } catch (e: Exception) {
            message.messageDate // fallback
        }

        if (holder is SentMessageViewHolder) {
            holder.txtMessage.text = message.messageBody
            holder.txtTime.text = formattedTime
        } else if (holder is ReceivedMessageViewHolder) {
            holder.txtMessage.text = message.messageBody
            holder.txtTime.text = formattedTime
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMessage: TextView = itemView.findViewById(R.id.txtMessageSent)
        val txtTime: TextView = itemView.findViewById(R.id.txtSentTime)
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMessage: TextView = itemView.findViewById(R.id.txtMessageReceived)
        val txtTime: TextView = itemView.findViewById(R.id.txtReceivedTime)
    }

    // ✅ Real-time updating method
    fun updateMessages(newList: List<MessageResponse>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }
}

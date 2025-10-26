package com.example.blueprintproapps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.ConversationResponse
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private val conversations: List<ConversationResponse>,
    private val onItemClick: ((ConversationResponse) -> Unit)? = null
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtMessage: TextView = itemView.findViewById(R.id.txtMessage)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(conversations[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val convo = conversations[position]

        // 👤 Display architect name or ID
        holder.txtName.text = convo.architectName ?: convo.architectId

        // 💬 Show last message preview
        holder.txtMessage.text = convo.lastMessage ?: "(No messages yet)"

        // 🕒 Format and show message time
        holder.txtTime.text = formatDate(convo.lastMessageTime)

        // 🖼️ Load architect profile image
        convo.profileUrl?.let {
            Glide.with(holder.itemView.context)
                .load(it)
                .placeholder(R.drawable.sample_profile)
                .into(holder.imgProfile)
        }
    }

    override fun getItemCount(): Int = conversations.size

    private fun formatDate(rawDate: String?): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            rawDate?.let { formatter.format(parser.parse(it)!!) } ?: ""
        } catch (e: Exception) {
            rawDate ?: ""
        }
    }
}

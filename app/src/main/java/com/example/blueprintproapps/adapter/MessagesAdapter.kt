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
import com.example.blueprintproapps.utils.DateTimeUtils

class MessagesAdapter(
    private val conversations: List<ConversationResponse>,
    private val onItemClick: ((ConversationResponse) -> Unit)? = null
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtMessage: TextView = itemView.findViewById(R.id.txtMessage)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(conversations[position])
                }
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
        holder.txtTime.text = DateTimeUtils.formatPhilippineTime(convo.lastMessageTime)

        // 🔴 Unread indicator
        holder.unreadIndicator.visibility = if (convo.unreadCount > 0) View.VISIBLE else View.GONE

        // 🖼️ Load architect profile image
        Glide.with(holder.itemView.context)
            .load(convo.profileUrl)
            .placeholder(R.drawable.sample_profile)
            .into(holder.imgProfile)
    }

    override fun getItemCount(): Int = conversations.size

}

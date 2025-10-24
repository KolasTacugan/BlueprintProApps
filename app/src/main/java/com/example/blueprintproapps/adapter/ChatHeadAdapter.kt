package com.example.blueprintproapps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.MatchResponse2

class ChatHeadAdapter(
    private val chatHeads: List<MatchResponse2>,
    private val onItemClick: ((MatchResponse2) -> Unit)? = null
) : RecyclerView.Adapter<ChatHeadAdapter.ChatHeadViewHolder>() {

    inner class ChatHeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
        val txtName: TextView = itemView.findViewById(R.id.txtName)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(chatHeads[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHeadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_head, parent, false)
        return ChatHeadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHeadViewHolder, position: Int) {
        val match = chatHeads[position]

        // Display name or fallback
        holder.txtName.text = match.architectName ?: "Unknown Architect"

        // Handle possible relative photo URL
        val photoUrl = match.architectPhoto?.replace("~", "https://yourdomain.com")

        Glide.with(holder.itemView.context)
            .load(photoUrl ?: R.drawable.sample_profile)
            .placeholder(R.drawable.sample_profile)
            .into(holder.imgProfile)
    }

    override fun getItemCount(): Int = chatHeads.size
}

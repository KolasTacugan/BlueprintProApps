package com.example.blueprintproapps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.ArchitectMatchResponse

class ArchitectChatHeadAdapter(
    private val chatHeads: List<ArchitectMatchResponse>,
    private val onItemClick: ((ArchitectMatchResponse) -> Unit)? = null
) : RecyclerView.Adapter<ArchitectChatHeadAdapter.ChatHeadViewHolder>() {

    inner class ChatHeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
        val txtName: TextView = itemView.findViewById(R.id.txtName)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(chatHeads[position])
                }
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
        val firstName = match.clientName
            ?.trim()
            ?.split(" ")
            ?.firstOrNull()
            ?: "Unknown"

        holder.txtName.text = firstName


        val photoUrl = match.clientPhoto  // already a full URL from API

        Glide.with(holder.itemView.context)
            .load(photoUrl ?: R.drawable.sample_profile)
            .placeholder(R.drawable.sample_profile)
            .error(R.drawable.sample_profile)
            .into(holder.imgProfile)
    }


    override fun getItemCount(): Int = chatHeads.size
}

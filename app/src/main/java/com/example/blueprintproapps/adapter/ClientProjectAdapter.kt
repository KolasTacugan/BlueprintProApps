package com.example.blueprintproapps.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.ClientProjectResponse
import com.example.blueprintproapps.network.ClientProjectTrackerActivity
import com.squareup.picasso.Picasso

class ClientProjectAdapter(
    private val items: MutableList<ClientProjectResponse>,
    private val context: Context,
    private val onItemClick: (ClientProjectResponse) -> Unit,
    private val onArchitectNameClick: (String) -> Unit
) : RecyclerView.Adapter<ClientProjectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val blueprintImage: ImageView = view.findViewById(R.id.blueprintImage)
        val projectTitle: TextView = view.findViewById(R.id.projectTitle)
        val projectStatus: TextView = view.findViewById(R.id.projectStatus)
        val architectName: TextView = view.findViewById(R.id.architectName)
        val trackBtn: Button = view.findViewById(R.id.trackBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_client_project, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.projectTitle.text = item.project_Title
        holder.projectStatus.text = item.project_Status

        // 🔥 SET THE ARCHITECT NAME (this was missing)
        holder.architectName.text = item.architectName

        holder.architectName.setOnClickListener {
            if (item.user_architectId.isNullOrEmpty()) {
                Toast.makeText(context, "Architect ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onArchitectNameClick(item.user_architectId)
        }

        // IMAGE
        if (!item.blueprint_ImageUrl.isNullOrEmpty()) {
            Picasso.get().load(item.blueprint_ImageUrl).into(holder.blueprintImage)
        } else {
            holder.blueprintImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // ITEM CLICK
        holder.itemView.setOnClickListener { onItemClick(item) }

        // TRACK BUTTON
        holder.trackBtn.setOnClickListener {
            val intent = Intent(context, ClientProjectTrackerActivity::class.java)
            intent.putExtra("blueprintId", item.blueprint_Id)
            intent.putExtra("projectStatus", item.project_Status)
            intent.putExtra("blueprintName", item.blueprint_Name)
            context.startActivity(intent)
        }
    }

    fun updateData(newItems: List<ClientProjectResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

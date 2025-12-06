package com.example.blueprintproapps.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.ArchitectProjectResponse
import com.example.blueprintproapps.network.ArchitectProjectTrackerActivity
import com.squareup.picasso.Picasso

class ArchitectProjectAdapter(
    private val items: MutableList<ArchitectProjectResponse>,
    private val context: Context,
    private val onDeleteClick: (String) -> Unit,
    private val onClientClick: (String) -> Unit
) : RecyclerView.Adapter<ArchitectProjectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val blueprintImage: ImageView = view.findViewById(R.id.blueprintImage)
        val blueprintName: TextView = view.findViewById(R.id.blueprintName)
        val blueprintBudget: TextView = view.findViewById(R.id.blueprintBudget)
        val clientName: TextView = view.findViewById(R.id.clientName)
        val trackBtn: Button = view.findViewById(R.id.trackBtn)
        val deleteBtn: Button = view.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_archi_project_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // BASIC BINDING
        holder.blueprintName.text = item.project_Title
        holder.blueprintBudget.text = "₱${item.project_Budget ?: "0"}"
        holder.clientName.text = item.clientName ?: "Unknown Client"

        // 🔥 Pass clientId to activity through callback
        holder.clientName.setOnClickListener {
            item.clientId?.let { id ->
                onClientClick(id)
            } ?: Toast.makeText(context, "Client ID is missing", Toast.LENGTH_SHORT).show()
        }

        // IMAGE
        if (!item.blueprintImage.isNullOrEmpty()) {
            Picasso.get().load(item.blueprintImage).into(holder.blueprintImage)
        } else {
            holder.blueprintImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // DELETE BUTTON VISIBILITY
        holder.deleteBtn.visibility =
            if (item.project_Status == "Ongoing") View.VISIBLE else View.GONE

        // DELETE ACTION
        holder.deleteBtn.setOnClickListener {
            onDeleteClick(item.project_Id)
        }

        // TRACK BUTTON
        holder.trackBtn.setOnClickListener {
            val intent = Intent(context, ArchitectProjectTrackerActivity::class.java)
            intent.putExtra("projectId", item.project_Id)
            intent.putExtra("blueprintId", item.blueprint_Id)
            intent.putExtra("projectStatus", item.project_Status)
            context.startActivity(intent)
        }
    }

    fun updateData(newItems: List<ArchitectProjectResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

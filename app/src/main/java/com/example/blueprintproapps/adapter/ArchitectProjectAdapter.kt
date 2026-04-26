package com.example.blueprintproapps.adapter

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.ArchitectProjectResponse
import com.example.blueprintproapps.network.ArchitectProjectTrackerActivity
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.progressindicator.LinearProgressIndicator

class ArchitectProjectAdapter(
    private var items: MutableList<ArchitectProjectResponse>,
    private val context: Context,
    private val onDeleteClick: (String) -> Unit,
    private val onClientClick: (String) -> Unit
) : RecyclerView.Adapter<ArchitectProjectAdapter.ViewHolder>() {

    private var lastPosition = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val blueprintImage: android.widget.ImageView = view.findViewById(R.id.blueprintImage)
        val blueprintName: TextView = view.findViewById(R.id.blueprintName)
        val blueprintBudget: TextView = view.findViewById(R.id.blueprintBudget)
        val clientName: TextView = view.findViewById(R.id.clientName)
        val projectStatusBadge: TextView = view.findViewById(R.id.projectStatusBadge)
        val trackBtn: android.view.View = view.findViewById(R.id.trackBtn)
        val deleteBtn: android.view.View = view.findViewById(R.id.deleteBtn)
        val projectProgress: LinearProgressIndicator = view.findViewById(R.id.projectProgress)
        val progressPercentage: TextView = view.findViewById(R.id.progressPercentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_archi_project_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.blueprintName.text = item.project_Title
        holder.blueprintBudget.text = "₱${item.project_Budget ?: "0"}"
        holder.clientName.text = "Client: ${item.clientName}"
        holder.projectStatusBadge.text = item.project_Status

        // Progress Mapping
        val progressValue = when (item.project_Status) {
            "Ongoing" -> 20
            "Review" -> 40
            "Compliance" -> 60
            "Finalization" -> 80
            "Finished" -> 100
            else -> 10
        }
        holder.projectProgress.progress = progressValue
        holder.progressPercentage.text = "$progressValue%"

        // Status Colors
        val statusColor = when (item.project_Status) {
            "Ongoing" -> "#3B82F6"
            "Review" -> "#F59E0B"
            "Compliance" -> "#8B5CF6"
            "Finalization" -> "#06B6D4"
            "Finished" -> "#10B981"
            "Deleted" -> "#EF4444"
            else -> "#64748B"
        }
        holder.projectStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor(statusColor))

        // Client click
        holder.clientName.setOnClickListener {
            onClientClick(item.clientId)
        }

        // Image with Glide
        Glide.with(context)
            .load(item.blueprintImage)
            .placeholder(R.drawable.ic_placeholder_blueprint)
            .error(R.drawable.ic_placeholder_blueprint)
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .into(holder.blueprintImage)

        // DELETE BUTTON VISIBILITY
        holder.deleteBtn.visibility = if (item.project_Status != "Deleted") View.VISIBLE else View.GONE

        // Actions
        holder.deleteBtn.setOnClickListener {
            onDeleteClick(item.project_Id)
        }

        holder.trackBtn.setOnClickListener {
            val intent = Intent(context, ArchitectProjectTrackerActivity::class.java)
            intent.putExtra("projectId", item.project_Id)
            intent.putExtra("blueprintId", item.blueprint_Id)
            intent.putExtra("projectStatus", item.project_Status)
            context.startActivity(intent)
        }

        UiEffects.applyPressScaleEffect(holder.itemView)
        setAnimation(holder.itemView, position)
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    fun updateData(newItems: List<ArchitectProjectResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

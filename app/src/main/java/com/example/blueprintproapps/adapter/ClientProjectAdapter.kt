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
import com.example.blueprintproapps.models.ClientProjectResponse
import com.example.blueprintproapps.network.ClientProjectTrackerActivity
import com.example.blueprintproapps.utils.ProjectStatusFormatter
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.progressindicator.LinearProgressIndicator

class ClientProjectAdapter(
    private var items: MutableList<ClientProjectResponse>,
    private val context: Context,
    private val onItemClick: (ClientProjectResponse) -> Unit,
    private val onArchitectNameClick: (String) -> Unit
) : RecyclerView.Adapter<ClientProjectAdapter.ViewHolder>() {

    private var lastPosition = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val blueprintImage: android.widget.ImageView = view.findViewById(R.id.blueprintImage)
        val projectTitle: TextView = view.findViewById(R.id.projectTitle)
        val projectStatus: TextView = view.findViewById(R.id.projectStatus)
        val architectName: TextView = view.findViewById(R.id.architectName)
        val tvProjectBudget: TextView = view.findViewById(R.id.tvProjectBudget)
        val trackBtn: android.view.View = view.findViewById(R.id.trackBtn)
        val projectProgress: LinearProgressIndicator = view.findViewById(R.id.projectProgress)
        val progressPercentage: TextView = view.findViewById(R.id.progressPercentage)
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
        holder.architectName.text = "Architect: ${item.architectName}"
        holder.tvProjectBudget.text = "₱${item.project_Budget ?: "0"}"

        val progressValue = ProjectStatusFormatter.progressFor(item.project_Status)
        holder.projectProgress.progress = progressValue
        holder.progressPercentage.text = "$progressValue%"

        val statusColor = ProjectStatusFormatter.colorFor(item.project_Status)
        holder.projectStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor(statusColor))

        // Architect click
        holder.architectName.setOnClickListener {
            onArchitectNameClick(item.user_architectId)
        }

        // Image with Glide
        Glide.with(context)
            .load(item.blueprint_ImageUrl)
            .placeholder(R.drawable.ic_placeholder_blueprint)
            .error(R.drawable.ic_placeholder_blueprint)
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .into(holder.blueprintImage)

        // Interactivity
        UiEffects.applyPressScaleEffect(holder.itemView)
        setAnimation(holder.itemView, position)
        
        holder.itemView.setOnClickListener { onItemClick(item) }

        holder.trackBtn.setOnClickListener {
            val intent = Intent(context, ClientProjectTrackerActivity::class.java)
            intent.putExtra("blueprintId", item.blueprint_Id)
            intent.putExtra("projectStatus", item.project_Status)
            intent.putExtra("blueprintName", item.blueprint_Name)
            context.startActivity(intent)
        }
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    fun updateData(newItems: List<ClientProjectResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

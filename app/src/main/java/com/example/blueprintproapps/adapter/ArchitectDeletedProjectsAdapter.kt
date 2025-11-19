package com.example.blueprintproapps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.DeletedProjectResponse

class ArchitectDeletedProjectsAdapter(
    private val list: List<DeletedProjectResponse>,
    private val onRestore: (String) -> Unit,
    private val onPermanentDelete: (String) -> Unit
) : RecyclerView.Adapter<ArchitectDeletedProjectsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectName: TextView = itemView.findViewById(R.id.deletedProjectName)
        val clientName: TextView = itemView.findViewById(R.id.deletedProjectClient)
        val restoreBtn: ImageButton = itemView.findViewById(R.id.restoreBtn)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.deleteForeverBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_architect_deleted_project, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.projectName.text = item.project_Title
        holder.clientName.text = "Client: ${item.clientName}"

        holder.restoreBtn.setOnClickListener { onRestore(item.project_Id) }
        holder.deleteBtn.setOnClickListener { onPermanentDelete(item.project_Id) }
    }

    override fun getItemCount() = list.size
}

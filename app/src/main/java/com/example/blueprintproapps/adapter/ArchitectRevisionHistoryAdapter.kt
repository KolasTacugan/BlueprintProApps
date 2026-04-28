package com.example.blueprintproapps.adapter

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectProjectFileResponse
import com.example.blueprintproapps.utils.UiEffects

class ArchitectRevisionHistoryAdapter(
    private val revisionList: List<ArchitectProjectFileResponse>
) : RecyclerView.Adapter<ArchitectRevisionHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val revisionName: TextView = view.findViewById(R.id.revisionName)
        val openBtn: Button = view.findViewById(R.id.openBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_architect_revision_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val revision = revisionList[position]
        holder.revisionName.text = "Revision v${revision.projectFile_Version}"

        UiEffects.applyPressScaleEffect(holder.itemView)

        holder.openBtn.setOnClickListener {
            val filePath = revision.projectFile_Path?.trim()

            if (filePath.isNullOrEmpty()) {
                Toast.makeText(holder.itemView.context, "No file path available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val uri = if (!filePath.startsWith("http")) {
                    Uri.parse("${ApiClient.getBaseUrl()}$filePath")
                } else {
                    Uri.parse(filePath)
                }

                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                holder.itemView.context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(holder.itemView.context, "Unable to open file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = revisionList.size
}

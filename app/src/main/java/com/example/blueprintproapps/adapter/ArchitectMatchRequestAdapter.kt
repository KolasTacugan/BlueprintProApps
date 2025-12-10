package com.example.blueprintproapps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.ArchitectMatchRequest

class ArchitectMatchRequestAdapter(
    private val requests: MutableList<ArchitectMatchRequest>,
    private val onAccept: (String) -> Unit,
    private val onDecline: (String) -> Unit,
    private val onProfileClick: (String) -> Unit      // ✅ NEW
) : RecyclerView.Adapter<ArchitectMatchRequestAdapter.MatchViewHolder>() {

    inner class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val clientName: TextView = view.findViewById(R.id.tvClientName)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnDecline: Button = view.findViewById(R.id.btnDecline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_architect_match_request, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val request = requests[position]

        // Set client name
        holder.clientName.text = request.clientName

        // Accept & Decline
        holder.btnAccept.setOnClickListener { onAccept(request.matchId) }
        holder.btnDecline.setOnClickListener { onDecline(request.matchId) }
    }

    fun updateData(newRequests: List<ArchitectMatchRequest>) {
        requests.clear()
        requests.addAll(newRequests)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = requests.size
}

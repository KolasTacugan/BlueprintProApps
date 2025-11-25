package com.example.blueprintproapps.network

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.MatchResponse

class MatchAdapter(private val onRequestClick: (String) -> Unit,
                   private val onProfileClick: (MatchResponse) -> Unit) :
    ListAdapter<MatchResponse, MatchAdapter.MatchViewHolder>(MatchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_architect_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

    inner class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.architectName)
        private val tvStyle: TextView = itemView.findViewById(R.id.architectStyle)
        private val tvBudget: TextView = itemView.findViewById(R.id.architectBudget)
        private val btnMatch: Button = itemView.findViewById(R.id.matchButton)

        fun bind(match: MatchResponse) {
            tvName.text = match.architectName
            tvStyle.text = match.architectStyle ?: "No style specified"
            tvBudget.text = "Budget: ${match.architectBudget ?: "Not specified"}"
            btnMatch.text = if (match.matchStatus == "Pending") "Pending..." else "Match"
            btnMatch.isEnabled = match.matchStatus != "Pending"
            btnMatch.setOnClickListener { onRequestClick(match.architectId) }

            itemView.setOnClickListener {
                onProfileClick(match)
            }
        }
    }
}

class MatchDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<MatchResponse>() {
    override fun areItemsTheSame(oldItem: MatchResponse, newItem: MatchResponse): Boolean {
        return oldItem.architectId == newItem.architectId
    }

    override fun areContentsTheSame(oldItem: MatchResponse, newItem: MatchResponse): Boolean {
        return oldItem == newItem
    }
}


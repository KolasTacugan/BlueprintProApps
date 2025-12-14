package com.example.blueprintproapps.network

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
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
        private val tvSimilarity: TextView = itemView.findViewById(R.id.similarityScore)

        fun bind(match: MatchResponse) {

            // Basic data
            tvName.text = match.architectName
            tvStyle.text = match.architectStyle ?: "No style specified"
            tvBudget.text = "Budget: ${match.architectBudget ?: "Not specified"}"

            val percent = match.similarityPercentage ?: 0.0
            tvSimilarity.text = "${percent}%"

            itemView.setOnClickListener {
                onProfileClick(match)
            }

            // --- MATCH STATUS LOGIC (Same as Web Version) ---
            when (match.realMatchStatus) {

                // No relationship yet → Match (WHITE text)
                null, "", "None" -> {
                    btnMatch.text = "Match"
                    btnMatch.isEnabled = true
                    btnMatch.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.white)
                    )
                    btnMatch.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.primary)
                    )
                }

                // Pending → BLACK text
                "Pending" -> {
                    btnMatch.text = "Pending"
                    btnMatch.isEnabled = false
                    btnMatch.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.black)
                    )
                    btnMatch.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.warning)
                    )
                }

                // Approved → BLACK text
                "Approved" -> {
                    btnMatch.text = "Matched"
                    btnMatch.isEnabled = false
                    btnMatch.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.black)
                    )
                    btnMatch.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.success)
                    )
                }

                else -> {
                    btnMatch.text = "Match"
                    btnMatch.isEnabled = true
                    btnMatch.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.white)
                    )
                    btnMatch.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.primary)
                    )
                }
            }


            // Request button press
            btnMatch.setOnClickListener {
                onRequestClick(match.architectId)

                val updated = match.copy(realMatchStatus = "Pending")
                val updatedList = currentList.toMutableList()
                updatedList[adapterPosition] = updated
                submitList(updatedList)
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


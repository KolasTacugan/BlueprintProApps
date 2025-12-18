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
import com.example.blueprintproapps.models.MatchListDiffCallback
import com.example.blueprintproapps.models.MatchListItem
import com.example.blueprintproapps.models.MatchResponse

class MatchAdapter(
    private val onRequestClick: (String) -> Unit,
    private val onProfileClick: (MatchResponse) -> Unit
) : ListAdapter<MatchListItem, RecyclerView.ViewHolder>(MatchListDiffCallback()) {

    companion object {
        private const val TYPE_MATCH = 0
        private const val TYPE_FOOTER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MatchListItem.Architect -> TYPE_MATCH
            is MatchListItem.Footer -> TYPE_FOOTER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_MATCH -> MatchViewHolder(
                inflater.inflate(R.layout.item_architect_match, parent, false)
            )
            else -> FooterViewHolder(
                inflater.inflate(R.layout.item_ranking_footer, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is MatchListItem.Architect ->
                (holder as MatchViewHolder).bind(item.match)

            is MatchListItem.Footer ->
                (holder as FooterViewHolder).bind(
                    item.shown,
                    item.total
                )
        }
    }

    // 🔹 Public submit function (used by Activity)
    fun submitMatches(matches: List<MatchResponse>, totalArchitects: Int) {
        val items = mutableListOf<MatchListItem>()

        matches.forEach {
            items.add(MatchListItem.Architect(it))
        }

        if (matches.isNotEmpty()) {
            items.add(
                MatchListItem.Footer(
                    shown = matches.size,
                    total = totalArchitects
                )
            )
        }

        submitList(items)
    }

    // ===========================
    // 🔹 MATCH VIEW HOLDER
    // ===========================
    inner class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.architectName)
        private val tvStyle: TextView = itemView.findViewById(R.id.architectStyle)
        private val tvBudget: TextView = itemView.findViewById(R.id.architectBudget)
        private val tvSimilarity: TextView = itemView.findViewById(R.id.similarityScore)
        private val btnMatch: Button = itemView.findViewById(R.id.matchButton)

        fun bind(match: MatchResponse) {

            tvName.text = match.architectName
            tvStyle.text = match.architectStyle ?: "No style specified"
            tvBudget.text = "Budget: ${match.architectBudget ?: "Not specified"}"

            tvSimilarity.text = "${match.similarityPercentage ?: 0.0}%"

            itemView.setOnClickListener {
                onProfileClick(match)
            }

            when (match.realMatchStatus) {
                null, "", "None" -> {
                    btnMatch.text = "Match"
                    btnMatch.isEnabled = true
                    btnMatch.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.primary)
                    )
                    btnMatch.setTextColor(Color.WHITE)
                }

                "Pending" -> {
                    btnMatch.text = "Pending"
                    btnMatch.isEnabled = false
                    btnMatch.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.warning)
                    )
                    btnMatch.setTextColor(Color.BLACK)
                }

                "Approved" -> {
                    btnMatch.text = "Matched"
                    btnMatch.isEnabled = false
                    btnMatch.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.success)
                    )
                    btnMatch.setTextColor(Color.BLACK)
                }
            }

            btnMatch.setOnClickListener {
                onRequestClick(match.architectId)

                val updated = match.copy(realMatchStatus = "Pending")
                val newList = currentList.toMutableList()

                val index = newList.indexOfFirst {
                    it is MatchListItem.Architect &&
                            it.match.architectId == match.architectId
                }

                if (index != -1) {
                    newList[index] = MatchListItem.Architect(updated)
                    submitList(newList)
                }
            }
        }
    }

    // ===========================
    // 🔹 FOOTER VIEW HOLDER
    // ===========================
    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val footerText: TextView = itemView.findViewById(R.id.footerText)

        fun bind(shown: Int, total: Int) {
            footerText.text =
                        "Lower-ranked architects were omitted due to low relevance."
        }
    }
}

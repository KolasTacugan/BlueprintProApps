package com.example.blueprintproapps.models

import androidx.recyclerview.widget.DiffUtil

class MatchListDiffCallback : DiffUtil.ItemCallback<MatchListItem>() {

    override fun areItemsTheSame(
        oldItem: MatchListItem,
        newItem: MatchListItem
    ): Boolean {

        return when {
            oldItem is MatchListItem.Architect &&
                    newItem is MatchListItem.Architect ->
                oldItem.match.architectId == newItem.match.architectId

            oldItem is MatchListItem.Footer &&
                    newItem is MatchListItem.Footer ->
                true // only ONE footer exists

            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItem: MatchListItem,
        newItem: MatchListItem
    ): Boolean {
        return oldItem == newItem
    }
}

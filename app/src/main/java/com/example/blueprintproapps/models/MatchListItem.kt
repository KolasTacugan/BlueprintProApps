package com.example.blueprintproapps.models

sealed class MatchListItem {

    data class Architect(
        val match: MatchResponse
    ) : MatchListItem()

    data class Footer(
        val shown: Int,
        val total: Int
    ) : MatchListItem()
}

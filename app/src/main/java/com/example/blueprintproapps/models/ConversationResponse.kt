package com.example.blueprintproapps.models



data class ConversationResponse(
    val architectId: String,
    val architectName: String?,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val profileUrl: String?,
    val unreadCount: Int
)

data class ConversationListResponse(
    val success: Boolean,

    val messages: List<ConversationResponse>
)

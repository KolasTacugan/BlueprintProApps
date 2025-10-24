package com.example.blueprintproapps.models

data class ArchitectConversationResponse(
    val clientId: String,
    val clientName: String?,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val profileUrl: String?,
    val unreadCount: Int
)

data class ArchitectConversationListResponse(
    val success: Boolean,
    val messages: List<ArchitectConversationResponse>
)

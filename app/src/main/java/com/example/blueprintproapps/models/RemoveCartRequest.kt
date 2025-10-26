package com.example.blueprintproapps.models

data class RemoveCartRequest(
    val clientId: String,
    val blueprintId: Int
)

data class GenericResponsee(
    val success: Boolean,
    val message: String
)

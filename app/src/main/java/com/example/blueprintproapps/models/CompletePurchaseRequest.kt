package com.example.blueprintproapps.models

data class CompletePurchaseRequest(
    val clientId: String,
    val blueprintIds: List<Int>
)
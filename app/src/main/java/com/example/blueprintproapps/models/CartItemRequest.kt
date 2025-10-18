package com.example.blueprintproapps.models

data class CartItemRequest(
    val cartItemId: Int = 0,             // optional, can default to 0
    val blueprintId: Int,
    val name: String,
    val image: String,                   // 👈 REQUIRED by backend
    val price: Double,
    val quantity: Int
)

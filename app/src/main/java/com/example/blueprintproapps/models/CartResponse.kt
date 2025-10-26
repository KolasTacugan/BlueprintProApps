package com.example.blueprintproapps.models

import com.google.gson.annotations.SerializedName

data class CartResponse(
    val cartItems: List<CartItem>,
    val totalPrice: Double,
)
data class CartItem(
    val cartItemId: Int,
    val blueprintId: Int,
    val blueprintName: String,
    val blueprintImage: String,
    val blueprintPrice: Double,
    val quantity: Int
)
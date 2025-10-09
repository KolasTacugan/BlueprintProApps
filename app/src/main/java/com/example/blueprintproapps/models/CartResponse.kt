package com.example.blueprintproapps.models

data class CartResponse(
    val cartItems: List<CartItem>,
    val totalPrice: Double
)
data class CartItem(
    val cartId: Int,
    val blueprintId: Int,
    val blueprintName: String,
    val blueprintImage: String?,
    val quantity: Int,
    val price: Double
)
package com.example.blueprintproapps.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blueprintproapps.databinding.ItemCartBinding
import com.example.blueprintproapps.models.CartItem

class CartAdapter(
    private val onRemoveClick: (CartItem) -> Unit // ✅ You forgot this constructor parameter
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DiffCallback()) {

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.blueprintName.text = item.blueprintName
            binding.quantity.text = "Qty: ${item.quantity}"
            binding.blueprintPrice.text = "₱${"%.2f".format(item.blueprintPrice)}"

            // 🖼 Load image or show a temporary built-in placeholder
            if (item.blueprintImage.endsWith(".docx", true)) {
                binding.blueprintImage.setImageResource(android.R.drawable.ic_menu_save)
            } else {
                Glide.with(binding.root.context)
                    .load(item.blueprintImage)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_delete)
                    .into(binding.blueprintImage)
            }

            // 🗑️ Remove button click event
            binding.removeBtn.setOnClickListener {
                onRemoveClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem) =
            oldItem.cartItemId == newItem.cartItemId

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem == newItem
    }
}

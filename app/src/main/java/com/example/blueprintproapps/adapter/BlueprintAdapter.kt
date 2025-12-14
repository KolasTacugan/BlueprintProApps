package com.example.blueprintproapps.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.BlueprintResponse
import com.example.blueprintproapps.models.CartRequest
import com.example.blueprintproapps.models.CartResponse
import com.example.blueprintproapps.utils.BlueprintDetailsBottomSheet
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class BlueprintAdapter(
    private val items: MutableList<BlueprintResponse>,
    private val context: Context,
    private val cartUpdateListener: OnCartUpdateListener // ✅ Listener for cart updates
) : RecyclerView.Adapter<BlueprintAdapter.ViewHolder>() {

    // 🔔 Callback interface to notify the activity
    interface OnCartUpdateListener {
        fun onItemAdded()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val blueprintName: TextView = view.findViewById(R.id.blueprintName)
        val blueprintPrice: TextView = view.findViewById(R.id.blueprintPrice)
        val blueprintImage: ImageView = view.findViewById(R.id.blueprintImage)
        val blueprintDescription: TextView = view.findViewById(R.id.blueprintDescription)
        val addToCartBtn: Button = view.findViewById(R.id.addToCartBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blueprint_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size
    fun updateList(newItems: List<BlueprintResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.blueprintName.text = item.blueprintName
        holder.blueprintPrice.text = formatPeso(item.blueprintPrice)

        holder.blueprintDescription.text =
            item.blueprintDescription ?: "No description available"

        holder.itemView.setOnClickListener {
            val bottomSheet = BlueprintDetailsBottomSheet(item)
            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "BlueprintDetails")
        }

        if (!item.blueprintImage.isNullOrEmpty()) {
            Picasso.get()
                .load(item.blueprintImage)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.blueprintImage)
        } else {
            holder.blueprintImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // ✅ Change button appearance depending on isAddedToCart flag
        if (item.isAddedToCart) {
            holder.addToCartBtn.text = "Item Added"
            holder.addToCartBtn.isEnabled = false
            holder.addToCartBtn.setBackgroundColor(context.getColor(R.color.gray))
        } else {
            holder.addToCartBtn.text = "Add to Cart"
            holder.addToCartBtn.isEnabled = true
            holder.addToCartBtn.setBackgroundColor(context.getColor(R.color.accent))
        }

        holder.addToCartBtn.setOnClickListener {
            val sharedPrefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val clientId = sharedPrefs.getString("clientId", null)

    private fun formatPeso(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return "₱${formatter.format(amount)}"
    }

            if (clientId == null) {
                Toast.makeText(context, "Please log in first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = CartRequest(
                clientId = clientId,
                blueprintId = item.blueprintId,
                quantity = 1
            )
            val currentPosition = holder.adapterPosition


            ApiClient.instance.addToCart(request).enqueue(object : Callback<CartResponse> {
                override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()

                        // 🟩 Mark this item as added
                        item.isAddedToCart = true
                        notifyItemChanged(currentPosition)

                        // Notify Marketplace activity
                        cartUpdateListener.onItemAdded()
                    } else {
                        Toast.makeText(context, "Failed to add. Please try again.", Toast.LENGTH_SHORT).show()
                        Log.e("AddToCart", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AddToCartError", t.message ?: "Unknown error")
                }
            })
        }
    }



}


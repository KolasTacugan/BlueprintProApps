package com.example.blueprintproapps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.BlueprintResponse
import com.squareup.picasso.Picasso

class BlueprintAdapter(private val items: List<BlueprintResponse>) :
    RecyclerView.Adapter<BlueprintAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val blueprintName: TextView = view.findViewById(R.id.blueprintName)
        val blueprintPrice: TextView = view.findViewById(R.id.blueprintPrice)
        val blueprintImage: ImageView = view.findViewById(R.id.blueprintImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blueprint_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.blueprintName.text = item.blueprintName
        holder.blueprintPrice.text = "₱${item.blueprintPrice}"

        if (!item.blueprintImage.isNullOrEmpty()) {
            Picasso.get().load(item.blueprintImage).into(holder.blueprintImage)
        } else {
            holder.blueprintImage.setImageResource(android.R.drawable.ic_menu_gallery)
            // fallback
        }
    }
}

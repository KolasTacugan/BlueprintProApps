package com.example.blueprintproapps.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import android.widget.Button
import com.squareup.picasso.Picasso
import com.example.blueprintproapps.models.ArchitectBlueprintResponse
import com.example.blueprintproapps.network.EditMarketplaceBlueprintActivity

class ArchitectBlueprintAdapter(
    private val items: MutableList<ArchitectBlueprintResponse>,
    private val context: Context
) : RecyclerView.Adapter<ArchitectBlueprintAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val blueprintName: TextView = view.findViewById(R.id.blueprintName)
        val blueprintPrice: TextView = view.findViewById(R.id.blueprintPrice)
        val blueprintImage: ImageView = view.findViewById(R.id.blueprintImage)
        val editButton: Button = view.findViewById(R.id.editBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_archi_blueprint_card, parent, false) // ✅ new layout
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
        }

        holder.editButton.setOnClickListener {
            val intent = Intent(context, EditMarketplaceBlueprintActivity::class.java)
            intent.putExtra("blueprintId", item.blueprintId)
            intent.putExtra("blueprintName", item.blueprintName)
            intent.putExtra("blueprintPrice", item.blueprintPrice)
            intent.putExtra("blueprintStyle", item.blueprintStyle)
            intent.putExtra("blueprintImage", item.blueprintImage)
            context.startActivity(intent)
        }

    }
    fun updateData(newItems: List<ArchitectBlueprintResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

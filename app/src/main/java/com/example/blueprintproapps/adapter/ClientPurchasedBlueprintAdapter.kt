package com.example.blueprintproapps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.ClientPurchasedBlueprint

class ClientPurchasedBlueprintAdapter (
    private val items: List<ClientPurchasedBlueprint>
) : RecyclerView.Adapter<ClientPurchasedBlueprintAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBlueprintName: TextView = view.findViewById(R.id.tvBlueprintName)
        val tvArchitectName: TextView = view.findViewById(R.id.tvArchitectName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_purchased_blueprint, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvBlueprintName.text = item.blueprintName
        holder.tvArchitectName.text = "Architect: ${item.architectName}"
    }

    override fun getItemCount() = items.size
}
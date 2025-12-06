package com.example.blueprintproapps.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.BlueprintResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import android.widget.ImageView
import android.widget.TextView

class BlueprintDetailsBottomSheet(private val blueprint: BlueprintResponse) :
    BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_blueprint_details, container, false)

        val detailsImage = view.findViewById<ImageView>(R.id.detailsImage)
        val detailsName = view.findViewById<TextView>(R.id.detailsName)
        val detailsStyle = view.findViewById<TextView>(R.id.detailsStyle)
        val detailsPrice = view.findViewById<TextView>(R.id.detailsPrice)
        val detailsDescription = view.findViewById<TextView>(R.id.detailsDescription)

        // Load image
        Picasso.get()
            .load(blueprint.blueprintImage)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(detailsImage)

        // Set data
        detailsName.text = blueprint.blueprintName
        detailsStyle.text = "Style: ${blueprint.blueprintStyle ?: "N/A"}"
        detailsPrice.text = "₱${blueprint.blueprintPrice}"
        detailsDescription.text = blueprint.blueprintDescription ?: "No description available."

        return view
    }

    // 🛡️ Add FLAG_SECURE here to block screenshots & screen recording
    override fun onStart() {
        super.onStart()
        dialog?.window?.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
    }
}

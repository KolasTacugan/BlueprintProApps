package com.example.blueprintproapps.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.blueprintproapps.R
import com.example.blueprintproapps.models.ClientProfileResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import android.widget.ImageView
import android.widget.TextView
import com.example.blueprintproapps.api.ApiClient

class ClientProfileBottomSheet(
    private val profile: ClientProfileResponse
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_client_profile, container, false)

        val imgPhoto = view.findViewById<ImageView>(R.id.clientPhoto)
        val txtName = view.findViewById<TextView>(R.id.clientName)
        val txtEmail = view.findViewById<TextView>(R.id.clientEmail)
        val txtPhone = view.findViewById<TextView>(R.id.clientPhone)

        Picasso.get()
            .load(getFullUrl(profile.profilePhoto))
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(imgPhoto)

        txtName.text = profile.name
        txtEmail.text = "Email: ${profile.email}"
        txtPhone.text = "Phone: ${profile.phone ?: "N/A"}"

        return view
    }

    private fun getFullUrl(path: String?): String? {
        if (path.isNullOrEmpty()) return null

        val baseUrl = ApiClient.getBaseUrl().trimEnd('/')

        return if (path.startsWith("/")) "$baseUrl$path" else path
    }

}

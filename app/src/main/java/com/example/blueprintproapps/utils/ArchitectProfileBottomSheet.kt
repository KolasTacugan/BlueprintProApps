package com.example.blueprintproapps.utils

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectProfileResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectProfileBottomSheet(
    private val architectId: String
) : BottomSheetDialogFragment() {

    private lateinit var imgPhoto: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtPhone: TextView
    private lateinit var txtLicense: TextView
    private lateinit var txtStyle: TextView
    private lateinit var txtSpecialization: TextView
    private lateinit var txtLocation: TextView
    private lateinit var txtCredentials: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_architect_profile, container, false)

        imgPhoto = view.findViewById(R.id.archPhoto)
        txtName = view.findViewById(R.id.archName)
        txtEmail = view.findViewById(R.id.archEmail)
        txtPhone = view.findViewById(R.id.archPhone)
        txtLicense = view.findViewById(R.id.archLicense)
        txtStyle = view.findViewById(R.id.archStyle)
        txtSpecialization = view.findViewById(R.id.archSpecialization)
        txtLocation = view.findViewById(R.id.archLocation)
        txtCredentials = view.findViewById(R.id.archCredentials)

        // Load architect data
        loadArchitectProfile()

        return view
    }

    private fun loadArchitectProfile() {
        ApiClient.instance.getArchitectProfile(architectId)
            .enqueue(object : Callback<ArchitectProfileResponse> {
                override fun onResponse(
                    call: Call<ArchitectProfileResponse>,
                    response: Response<ArchitectProfileResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful) {
                        response.body()?.let { updateUI(it) }
                    }
                }

                override fun onFailure(call: Call<ArchitectProfileResponse>, t: Throwable) {
                    if (!isAdded) return
                    txtName.text = "Failed to load profile"
                }
            })
    }

    private fun updateUI(profile: ArchitectProfileResponse) {

        val baseUrl = ApiClient.getBaseUrl().trimEnd('/')

        // --- FIX PHOTO URL ---
        val fullPhotoUrl = profile.photo?.let { path ->
            if (path.startsWith("/")) "$baseUrl$path" else path
        }

        Picasso.get()
            .load(fullPhotoUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(imgPhoto)


        // Set text fields
        txtName.text = profile.fullName
        txtEmail.text = "Email: ${profile.email}"
        txtPhone.text = "Phone: ${profile.phone ?: "N/A"}"
        txtLicense.text = "License No: ${profile.license ?: "N/A"}"
        txtStyle.text = "Style: ${profile.style ?: "N/A"}"
        txtSpecialization.text = "Specialization: ${profile.specialization ?: "N/A"}"
        txtLocation.text = "Location: ${profile.location ?: "N/A"}"


        // --- FIX CREDENTIALS URL ---
        val fullCredentialsUrl = profile.credentialsFile?.let { path ->
            if (path.startsWith("/")) "$baseUrl$path" else path
        }

        if (!fullCredentialsUrl.isNullOrBlank()) {

            txtCredentials.text = "Tap to view credentials"
            txtCredentials.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.primary)
            )

            txtCredentials.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullCredentialsUrl))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "No app available to open this file.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        } else {
            txtCredentials.text = "Architect hasn't uploaded credentials yet"
            txtCredentials.setTextColor(Color.GRAY)
            txtCredentials.setOnClickListener(null)
        }
    }
}
package com.example.blueprintproapps.utils

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.MatchResponse
import com.example.blueprintproapps.models.ProfileApiResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectDetailBottomSheet(private val match: MatchResponse) :
    BottomSheetDialogFragment() {

    private lateinit var name: TextView
    private lateinit var style: TextView
    private lateinit var budget: TextView
    private lateinit var email: TextView
    private lateinit var credentials: TextView
    private lateinit var image: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.architect_details_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name = view.findViewById(R.id.detailsName)
        style = view.findViewById(R.id.detailsStyle)
        budget = view.findViewById(R.id.detailsBudget)
        email = view.findViewById(R.id.detailsEmail)
        credentials = view.findViewById(R.id.detailsCredentials)
        image = view.findViewById(R.id.detailsImage)

        // Initial Match Data
        name.text = match.architectName
        style.text = match.architectStyle ?: "Not specified"
        budget.text = "Budget: ${match.architectBudget ?: "N/A"}"

        // Load full profile (photo, email, credentials)
        loadProfile(match.architectId)
    }


    private fun loadProfile(architectId: String) {
        ApiClient.instance.getProfile(architectId)
            .enqueue(object : Callback<ProfileApiResponse> {

                override fun onResponse(
                    call: Call<ProfileApiResponse>,
                    response: Response<ProfileApiResponse>
                ) {
                    if (!response.isSuccessful) {
                        Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val data = response.body()?.data ?: return

                    email.text = "Email: ${data.email ?: "N/A"}"

                    // Show credentials file
                    if (!data.credentialsFilePath.isNullOrEmpty()) {

                        credentials.text = "View Credentials"

                        // Make it look clickable
                        credentials.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
                        credentials.setOnClickListener {
                            openCredentialsInBrowser(data.credentialsFilePath!!)
                        }

                    } else {
                        credentials.text = "Credentials: None"
                    }

                    // Load profile picture
                    if (!data.profilePhoto.isNullOrEmpty()) {
                        Picasso.get().load(data.profilePhoto).into(image)
                    }
                }

                override fun onFailure(call: Call<ProfileApiResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), t.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun openCredentialsInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Cannot open credentials file", Toast.LENGTH_SHORT).show()
        }
    }
}

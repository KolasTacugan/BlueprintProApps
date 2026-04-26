package com.example.blueprintproapps.navigation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.models.ProfileApiResponse
import com.example.blueprintproapps.network.EditProfileActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var profileCall: Call<ProfileApiResponse>? = null
    private var viewDestroyed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false
        val session = AuthSessionManager.requireSession(requireActivity()) ?: return
        val tvFullName = view.findViewById<TextView>(R.id.tvFullName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val tvPhone = view.findViewById<TextView>(R.id.tvPhone)
        val imgProfile = view.findViewById<ImageView>(R.id.imgProfile)
        val btnEditProfile = view.findViewById<ImageButton>(R.id.btnEditProfile)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
        btnLogout.setOnClickListener {
            AuthSessionManager.logout(requireActivity())
        }
        profileCall = ApiClient.instance.getProfile(session.userId)
        profileCall?.enqueue(object : Callback<ProfileApiResponse> {
                override fun onResponse(call: Call<ProfileApiResponse>, response: Response<ProfileApiResponse>) {
                    if (!isAdded || viewDestroyed || view == null) return
                    val profile = response.body()?.data ?: return
                    tvFullName.text = "${profile.firstName ?: ""} ${profile.lastName ?: ""}"
                    tvEmail.text = profile.email ?: "N/A"
                    tvPhone.text = profile.phoneNumber ?: "N/A"
                    if (!profile.profilePhoto.isNullOrEmpty()) {
                        val safeContext = context ?: return
                        Glide.with(safeContext).load(profile.profilePhoto).into(imgProfile)
                    }
                }
                override fun onFailure(call: Call<ProfileApiResponse>, t: Throwable) {
                    if (!isAdded || viewDestroyed || view == null) return
                    val safeContext = context ?: return
                    Toast.makeText(safeContext, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        profileCall?.cancel()
        profileCall = null
        viewDestroyed = true
        super.onDestroyView()
    }
}

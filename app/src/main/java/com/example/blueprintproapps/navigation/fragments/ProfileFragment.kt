package com.example.blueprintproapps.navigation.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blueprintproapps.R
import com.example.blueprintproapps.WebViewActivity
import com.example.blueprintproapps.adapter.ClientPurchasedBlueprintAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ArchitectSubscriptionRequest
import com.example.blueprintproapps.models.ArchitectSubscriptionResponse
import com.example.blueprintproapps.models.ClientPurchasedBlueprint
import com.example.blueprintproapps.models.GenericResponse
import com.example.blueprintproapps.models.ProfileApiResponse
import com.example.blueprintproapps.models.ProfileResponse
import com.example.blueprintproapps.network.EditProfileActivity
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var profileCall: Call<ProfileApiResponse>? = null
    private var purchasesCall: Call<List<ClientPurchasedBlueprint>>? = null
    private var subscriptionCall: Call<ArchitectSubscriptionResponse>? = null
    private var downgradeCall: Call<GenericResponse>? = null
    private var viewDestroyed = false
    private var currentUserId: String? = null
    private var currentRole: UserRole? = null
    private var credentialsFilePath: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false

        val session = AuthSessionManager.requireSession(requireActivity()) ?: return
        currentUserId = session.userId
        currentRole = session.role

        view.findViewById<ImageButton>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            AuthSessionManager.logout(requireActivity())
        }
        view.findViewById<MaterialButton>(R.id.btnRetryProfile).setOnClickListener {
            loadProfile(session.userId, session.role)
        }
        view.findViewById<TextView>(R.id.tvCredentialsFile).setOnClickListener {
            openCredentialsFile()
        }

        view.findViewById<RecyclerView>(R.id.rvPurchasedBlueprints).apply {
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.icArchitectDetails), "md-verified-user")
        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.icPurchasedBlueprints), "md-store")
        UiEffects.applyCascadingEntrance(
            listOf(
                view.findViewById(R.id.profileHeaderCard),
                view.findViewById(R.id.layoutArchitectCredentials),
                view.findViewById(R.id.layoutPurchasedBlueprints)
            ),
            80L
        )

        loadProfile(session.userId, session.role)
    }

    private fun loadProfile(userId: String, role: UserRole) {
        renderProfileState("Loading profile...", showRetry = false)
        profileCall = ApiClient.instance.getProfile(userId)
        profileCall?.enqueue(object : Callback<ProfileApiResponse> {
            override fun onResponse(call: Call<ProfileApiResponse>, response: Response<ProfileApiResponse>) {
                if (!isAdded || viewDestroyed || view == null) return
                val profile = response.body()?.data
                if (!response.isSuccessful || profile == null) {
                    renderProfileState("Failed to load profile.", showRetry = true)
                    return
                }

                bindProfile(profile, role)
                renderProfileState("", showRetry = false)
            }

            override fun onFailure(call: Call<ProfileApiResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                renderProfileState("Network error while loading profile.", showRetry = true)
            }
        })
    }

    private fun bindProfile(profile: ProfileResponse, role: UserRole) {
        val root = view ?: return
        val fullName = listOfNotNull(profile.firstName, profile.lastName)
            .joinToString(" ")
            .ifBlank { "BlueprintPro User" }

        root.findViewById<TextView>(R.id.tvFullName).text = fullName
        root.findViewById<TextView>(R.id.tvEmail).text = profile.email ?: "N/A"
        root.findViewById<TextView>(R.id.tvPhone).text = profile.phoneNumber ?: "N/A"
        root.findViewById<TextView>(R.id.tvRoleBadge).text = profile.role ?: role.name.lowercase().replaceFirstChar { it.titlecase() }
        root.findViewById<TextView>(R.id.tvProBadge).visibility = if (profile.isPro) View.VISIBLE else View.GONE

        val imgProfile = root.findViewById<ImageView>(R.id.imgProfile)
        Glide.with(this)
            .load(profile.profilePhoto)
            .placeholder(R.drawable.sample_profile)
            .error(R.drawable.sample_profile)
            .circleCrop()
            .into(imgProfile)

        if (role == UserRole.ARCHITECT) {
            bindArchitectProfile(profile)
        } else {
            bindClientProfile()
        }
    }

    private fun bindArchitectProfile(profile: ProfileResponse) {
        val root = view ?: return
        root.findViewById<LinearLayout>(R.id.layoutArchitectCredentials).visibility = View.VISIBLE
        root.findViewById<LinearLayout>(R.id.layoutPurchasedBlueprints).visibility = View.GONE

        root.findViewById<TextView>(R.id.tvArchitectStyle).text = "Style: ${profile.style ?: "N/A"}"
        root.findViewById<TextView>(R.id.tvArchitectSpecialization).text = "Specialization: ${profile.specialization ?: "N/A"}"
        root.findViewById<TextView>(R.id.tvArchitectLocation).text = "Location: ${profile.location ?: "N/A"}"
        root.findViewById<TextView>(R.id.tvArchitectBudget).text = "Labor cost: ${profile.budget ?: "N/A"}"
        root.findViewById<TextView>(R.id.tvPortfolioText).text = profile.portfolioText?.takeIf { it.isNotBlank() }
            ?: "Portfolio summary unavailable. Upload credentials in Edit Profile to improve matching."

        credentialsFilePath = profile.credentialsFilePath
        root.findViewById<TextView>(R.id.tvCredentialsFile).text = if (profile.credentialsFilePath.isNullOrBlank()) {
            "Credentials: Not uploaded"
        } else {
            "Open credentials file"
        }

        root.findViewById<MaterialButton>(R.id.btnManagePlan).apply {
            text = if (profile.isPro) "Downgrade to Free" else "Upgrade to Pro"
            setOnClickListener {
                if (profile.isPro) downgradeToFree() else upgradeToPro()
            }
        }
    }

    private fun bindClientProfile() {
        val root = view ?: return
        root.findViewById<LinearLayout>(R.id.layoutArchitectCredentials).visibility = View.GONE
        root.findViewById<LinearLayout>(R.id.layoutPurchasedBlueprints).visibility = View.VISIBLE
        loadPurchasedBlueprints()
    }

    private fun loadPurchasedBlueprints() {
        val clientId = currentUserId ?: return
        val root = view ?: return
        val emptyState = root.findViewById<TextView>(R.id.tvPurchasedEmpty)
        emptyState.visibility = View.VISIBLE
        emptyState.text = "Loading purchases..."

        purchasesCall = ApiClient.instance.getPurchasedBlueprints(clientId)
        purchasesCall?.enqueue(object : Callback<List<ClientPurchasedBlueprint>> {
            override fun onResponse(
                call: Call<List<ClientPurchasedBlueprint>>,
                response: Response<List<ClientPurchasedBlueprint>>
            ) {
                if (!isAdded || viewDestroyed || view == null) return
                val items = if (response.isSuccessful) response.body().orEmpty() else emptyList()
                view?.findViewById<RecyclerView>(R.id.rvPurchasedBlueprints)?.adapter = ClientPurchasedBlueprintAdapter(items)
                emptyState.text = if (items.isEmpty()) "No purchased blueprints yet." else ""
                emptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onFailure(call: Call<List<ClientPurchasedBlueprint>>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                emptyState.text = "Failed to load purchased blueprints."
                emptyState.visibility = View.VISIBLE
            }
        })
    }

    private fun upgradeToPro() {
        val architectId = currentUserId ?: return
        subscriptionCall = ApiClient.instance.createArchitectSubscription(ArchitectSubscriptionRequest(architectId))
        subscriptionCall?.enqueue(object : Callback<ArchitectSubscriptionResponse> {
            override fun onResponse(
                call: Call<ArchitectSubscriptionResponse>,
                response: Response<ArchitectSubscriptionResponse>
            ) {
                if (!isAdded || viewDestroyed || view == null) return
                val paymentUrl = response.body()?.paymentUrl
                if (response.isSuccessful && !paymentUrl.isNullOrBlank()) {
                    startActivity(Intent(requireContext(), WebViewActivity::class.java).putExtra("url", paymentUrl))
                } else {
                    Toast.makeText(context, "Cannot create subscription session", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ArchitectSubscriptionResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                Toast.makeText(context, "Network error. Try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun downgradeToFree() {
        val architectId = currentUserId ?: return
        downgradeCall = ApiClient.instance.downgradeArchitectPlan(ArchitectSubscriptionRequest(architectId))
        downgradeCall?.enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (!isAdded || viewDestroyed || view == null) return
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "Downgraded successfully", Toast.LENGTH_SHORT).show()
                    currentRole?.let { loadProfile(architectId, it) }
                } else {
                    Toast.makeText(context, "Failed to downgrade", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                Toast.makeText(context, "Network error. Try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openCredentialsFile() {
        val url = resolveFileUrl(credentialsFilePath)
        if (url.isNullOrBlank()) {
            Toast.makeText(context, "No credentials file available", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun resolveFileUrl(path: String?): String? {
        val value = path?.trim().orEmpty()
        if (value.isEmpty()) return null
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        return "${ApiClient.getBaseUrl().trimEnd('/')}/${value.trimStart('/')}"
    }

    private fun renderProfileState(message: String, showRetry: Boolean) {
        val root = view ?: return
        val state = root.findViewById<TextView>(R.id.tvProfileState)
        val retry = root.findViewById<MaterialButton>(R.id.btnRetryProfile)
        state.text = message
        state.visibility = if (message.isBlank()) View.GONE else View.VISIBLE
        retry.visibility = if (showRetry) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        profileCall?.cancel()
        purchasesCall?.cancel()
        subscriptionCall?.cancel()
        downgradeCall?.cancel()
        profileCall = null
        purchasesCall = null
        subscriptionCall = null
        downgradeCall = null
        currentUserId = null
        currentRole = null
        credentialsFilePath = null
        viewDestroyed = true
        super.onDestroyView()
    }
}

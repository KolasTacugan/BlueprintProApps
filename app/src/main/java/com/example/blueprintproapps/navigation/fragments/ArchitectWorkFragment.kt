package com.example.blueprintproapps.navigation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectMatchRequestAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ArchitectMatchRequest
import com.example.blueprintproapps.models.ArchitectMatchListResponse
import com.example.blueprintproapps.models.ClientProfileResponse
import com.example.blueprintproapps.network.ArchitectBlueprintActivity
import com.example.blueprintproapps.network.ArchitectProjectActivity
import com.example.blueprintproapps.utils.ClientProfileBottomSheet
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectWorkFragment : Fragment(R.layout.fragment_architect_work) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArchitectMatchRequestAdapter
    private lateinit var architectId: String
    private lateinit var workState: TextView
    private lateinit var retryButton: MaterialButton
    private var pendingMatchesCall: Call<List<ArchitectMatchRequest>>? = null
    private var approvedMatchesCall: Call<ArchitectMatchListResponse>? = null
    private var respondMatchCall: Call<Void>? = null
    private var clientProfileCall: Call<ClientProfileResponse>? = null
    private var viewDestroyed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false
        val session = AuthSessionManager.requireSession(requireActivity(), UserRole.ARCHITECT) ?: return
        architectId = session.userId
        recyclerView = view.findViewById(R.id.recyclerMatchRequests)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.isNestedScrollingEnabled = false
        adapter = ArchitectMatchRequestAdapter(
            mutableListOf(),
            onAccept = { matchId -> respondToMatch(matchId, true) },
            onDecline = { matchId -> respondToMatch(matchId, false) },
            onClientClick = { clientId -> openClientProfile(clientId) }
        )
        recyclerView.adapter = adapter

        workState = view.findViewById(R.id.tvArchitectWorkState)
        retryButton = view.findViewById(R.id.btnRetryArchitectWork)
        retryButton.setOnClickListener {
            fetchPendingRequests()
            fetchApprovedMatches()
        }

        val projectWorkspace = view.findViewById<MaterialCardView>(R.id.cardProjectWorkspace)
        val blueprintManager = view.findViewById<MaterialCardView>(R.id.cardBlueprintManager)
        projectWorkspace.setOnClickListener { startActivity(Intent(requireContext(), ArchitectProjectActivity::class.java)) }
        blueprintManager.setOnClickListener { startActivity(Intent(requireContext(), ArchitectBlueprintActivity::class.java)) }

        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.icSectionWorkspace), "md-dashboard")
        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.icSectionRequests), "md-people")
        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.icProjectWorkspace), "md-assignment")
        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.icBlueprintManager), "md-store")
        UiEffects.applyCascadingEntrance(
            listOf<View>(view.findViewById(R.id.architectWorkStatsStrip), projectWorkspace, blueprintManager, recyclerView),
            80L
        )
        UiEffects.applyPressScaleEffect(projectWorkspace)
        UiEffects.applyPressScaleEffect(blueprintManager)

        fetchPendingRequests()
        fetchApprovedMatches()
    }

    private fun fetchPendingRequests() {
        renderState("Loading requests...", showRetry = false)
        pendingMatchesCall = ApiClient.instance.getPendingMatches(architectId)
        pendingMatchesCall?.enqueue(object : Callback<List<ArchitectMatchRequest>> {
                override fun onResponse(call: Call<List<ArchitectMatchRequest>>, response: Response<List<ArchitectMatchRequest>>) {
                    if (!isAdded || viewDestroyed || view == null) return
                    if (response.isSuccessful) {
                        val requests = response.body().orEmpty()
                        adapter.updateData(requests)
                        view?.findViewById<TextView>(R.id.tvPendingRequests)?.text = requests.size.toString()
                        renderState(
                            if (requests.isEmpty()) "No pending match requests right now." else "",
                            showRetry = false
                        )
                    } else {
                        renderState("Failed to load match requests.", showRetry = true)
                    }
                }

                override fun onFailure(call: Call<List<ArchitectMatchRequest>>, t: Throwable) {
                    if (!isAdded || viewDestroyed || view == null) return
                    renderState("Network error while loading requests.", showRetry = true)
                }
            })
    }

    private fun fetchApprovedMatches() {
        approvedMatchesCall = ApiClient.instance.getAllMatchesForArchitect(architectId)
        approvedMatchesCall?.enqueue(object : Callback<ArchitectMatchListResponse> {
            override fun onResponse(call: Call<ArchitectMatchListResponse>, response: Response<ArchitectMatchListResponse>) {
                if (!isAdded || viewDestroyed || view == null) return
                val approved = response.body()?.matches.orEmpty().count { it.matchStatus.equals("Approved", ignoreCase = true) || it.matchStatus.isNullOrBlank() }
                view?.findViewById<TextView>(R.id.tvApprovedMatches)?.text = approved.toString()
            }

            override fun onFailure(call: Call<ArchitectMatchListResponse>, t: Throwable) = Unit
        })
    }

    private fun openClientProfile(clientId: String) {
        clientProfileCall = ApiClient.instance.getClientProfile(clientId)
        clientProfileCall?.enqueue(object : Callback<ClientProfileResponse> {
            override fun onResponse(call: Call<ClientProfileResponse>, response: Response<ClientProfileResponse>) {
                if (!isAdded || viewDestroyed || view == null) return
                val profile = response.body() ?: return
                ClientProfileBottomSheet(profile).show(parentFragmentManager, "ClientProfileBottomSheet")
            }

            override fun onFailure(call: Call<ClientProfileResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                Toast.makeText(context, "Failed to load client profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun respondToMatch(matchId: String, approve: Boolean) {
        respondMatchCall = ApiClient.instance.respondMatch(matchId, approve)
        respondMatchCall?.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!isAdded || viewDestroyed || view == null) return
                    if (response.isSuccessful) {
                        Toast.makeText(context, if (approve) "Request accepted." else "Request declined.", Toast.LENGTH_SHORT).show()
                        fetchPendingRequests()
                        fetchApprovedMatches()
                    } else {
                        Toast.makeText(context, "Failed to update request.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    if (!isAdded || viewDestroyed || view == null) return
                    Toast.makeText(context, "Network error. Try again.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun renderState(message: String, showRetry: Boolean) {
        if (!::workState.isInitialized || !::retryButton.isInitialized) return
        workState.text = message
        workState.visibility = if (message.isBlank()) View.GONE else View.VISIBLE
        retryButton.visibility = if (showRetry) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        pendingMatchesCall?.cancel()
        approvedMatchesCall?.cancel()
        respondMatchCall?.cancel()
        clientProfileCall?.cancel()
        pendingMatchesCall = null
        approvedMatchesCall = null
        respondMatchCall = null
        clientProfileCall = null
        viewDestroyed = true
        super.onDestroyView()
    }
}

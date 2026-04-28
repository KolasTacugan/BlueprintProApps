package com.example.blueprintproapps.navigation.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ClientProjectAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ClientProjectResponse
import com.example.blueprintproapps.network.MarketPlaceActivity
import com.example.blueprintproapps.network.MatchClientActivity
import com.example.blueprintproapps.utils.ArchitectProfileBottomSheet
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientWorkFragment : Fragment(R.layout.fragment_client_projects) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClientProjectAdapter
    private lateinit var projectState: TextView
    private lateinit var retryButton: MaterialButton
    private val projectList = ArrayList<ClientProjectResponse>()
    private var clientId: String? = null
    private var projectsCall: Call<List<ClientProjectResponse>>? = null
    private var viewDestroyed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false
        val session = AuthSessionManager.requireSession(requireActivity(), UserRole.CLIENT) ?: return
        clientId = session.userId
        recyclerView = view.findViewById(R.id.recyclerViewClientProjects)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.isNestedScrollingEnabled = false
        adapter = ClientProjectAdapter(projectList, requireContext(), onItemClick = { }, onArchitectNameClick = { architectId ->
            ArchitectProfileBottomSheet(architectId).show(parentFragmentManager, "ArchitectProfile")
        })
        recyclerView.adapter = adapter

        projectState = view.findViewById(R.id.tvProjectState)
        retryButton = view.findViewById(R.id.btnRetryProjects)
        retryButton.setOnClickListener { fetchProjects() }

        val findArchitect = view.findViewById<MaterialCardView>(R.id.cardFindArchitect)
        val marketplace = view.findViewById<MaterialCardView>(R.id.cardMarketplace)
        findArchitect.setOnClickListener { startActivity(Intent(requireContext(), MatchClientActivity::class.java)) }
        marketplace.setOnClickListener { startActivity(Intent(requireContext(), MarketPlaceActivity::class.java)) }

        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.icSectionWorkActions), "md-flash-on")
        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.icSectionProjects), "md-assignment")
        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.icFindArchitect), "md-search")
        UiEffects.applyIconify(view.findViewById<ImageView>(R.id.icMarketplace), "md-store")
        UiEffects.applyCascadingEntrance(
            listOf<View>(view.findViewById(R.id.projectStatsStrip), findArchitect, marketplace, recyclerView),
            80L
        )
        UiEffects.applyPressScaleEffect(findArchitect)
        UiEffects.applyPressScaleEffect(marketplace)

        fetchProjects()
    }

    private fun fetchProjects() {
        val id = clientId ?: return
        renderState("Loading projects...", showRetry = false)
        projectsCall = ApiClient.instance.getClientProjects(id)
        projectsCall?.enqueue(object : Callback<List<ClientProjectResponse>> {
            override fun onResponse(call: Call<List<ClientProjectResponse>>, response: Response<List<ClientProjectResponse>>) {
                if (!isAdded || viewDestroyed || view == null) return
                if (response.isSuccessful) {
                    val projects = response.body().orEmpty()
                        .filterNot { it.project_Status.equals("Deleted", ignoreCase = true) }
                    projectList.clear()
                    projectList.addAll(projects)
                    adapter.updateData(projects)
                    bindProjectStats(projects)
                    renderState(
                        if (projects.isEmpty()) "No projects yet. Find an architect or buy a marketplace blueprint to get started." else "",
                        showRetry = false
                    )
                } else {
                    renderState("Failed to load projects.", showRetry = true)
                }
            }
            override fun onFailure(call: Call<List<ClientProjectResponse>>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                Log.e("API", t.message.toString())
                renderState("Network error while loading projects.", showRetry = true)
            }
        })
    }

    private fun bindProjectStats(projects: List<ClientProjectResponse>) {
        val root = view ?: return
        val finished = projects.count { it.project_Status.equals("Finished", ignoreCase = true) }
        root.findViewById<TextView>(R.id.tvActiveProjects).text = (projects.size - finished).coerceAtLeast(0).toString()
        root.findViewById<TextView>(R.id.tvFinishedProjects).text = finished.toString()
        root.findViewById<TextView>(R.id.tvTotalProjects).text = projects.size.toString()
    }

    private fun renderState(message: String, showRetry: Boolean) {
        if (!::projectState.isInitialized || !::retryButton.isInitialized) return
        projectState.text = message
        projectState.visibility = if (message.isBlank()) View.GONE else View.VISIBLE
        retryButton.visibility = if (showRetry) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        projectsCall?.cancel()
        projectsCall = null
        viewDestroyed = true
        super.onDestroyView()
    }
}

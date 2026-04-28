package com.example.blueprintproapps.navigation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ClientProjectResponse
import com.example.blueprintproapps.models.ConversationListResponse
import com.example.blueprintproapps.models.MatchListResponse
import com.example.blueprintproapps.models.ClientPurchasedBlueprint
import com.example.blueprintproapps.models.ProfileApiResponse
import com.example.blueprintproapps.network.MessagesActivity
import com.example.blueprintproapps.network.MarketPlaceActivity
import com.example.blueprintproapps.network.MatchClientActivity
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fragment-based Home screen for the Client role.
 *
 * Layout sections (matching the web dashboard design):
 *  1. Greeting – personalised "Welcome, [Name]!" with subtitle
 *  2. Stats Strip – Matches / Projects / Messages counts
 *  3. Quick Actions – Find Architect & Marketplace cards
 *  4. Recent Activity – latest conversation or match summary
 *  5. Project Overview – progress bar with phase & percentage
 */
class ClientHomeFragment : Fragment(R.layout.fragment_client_home) {
    
    // ── Helper Models ──────────────────────────────────────────────────
    private enum class ActivityType { MESSAGE, MATCH }
    private data class ActivityItem(
        val title: String,
        val subtitle: String,
        val icon: String,
        val type: ActivityType,
        val id: String,
        val photoUrl: String? = null
    )

    // ── API call handles (for cancellation on destroy) ──────────────
    private var profileCall: Call<ProfileApiResponse>? = null
    private var matchesCall: Call<MatchListResponse>? = null
    private var projectsCall: Call<List<ClientProjectResponse>>? = null
    private var conversationsCall: Call<ConversationListResponse>? = null
    private var purchaseCall: Call<List<ClientPurchasedBlueprint>>? = null

    private var viewDestroyed = false
    private var currentUserId: String? = null

    /** Possible states the dashboard can be in. */
    private enum class DashboardUiState { LOADING, SUCCESS, EMPTY, ERROR }

    // ── Lifecycle ───────────────────────────────────────────────────
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false

        val session = AuthSessionManager.requireSession(requireActivity(), UserRole.CLIENT)
            ?: return
        currentUserId = session.userId

        // Bind header views
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)

        // Bind quick-action views
        val architectIcon = view.findViewById<ImageView>(R.id.architectIcon)
        val marketplaceIcon = view.findViewById<ImageView>(R.id.marketplaceIcon)
        val chatIcon = view.findViewById<FloatingActionButton>(R.id.chatIcon)
        val findArchitect = view.findViewById<MaterialCardView>(R.id.findArchitect)
        val marketplaceBtn = view.findViewById<MaterialCardView>(R.id.marketplaceBtn)
        val retryBtn = view.findViewById<MaterialButton>(R.id.btnRetryDashboard)

        // Section header icons
        val icQuickActions = view.findViewById<ImageView>(R.id.icSectionQuickActions)
        val icRecentActivity = view.findViewById<ImageView>(R.id.icSectionRecentActivity)
        val icProjectProgress = view.findViewById<ImageView>(R.id.icSectionProjectProgress)
        val ivEmptyRecent = view.findViewById<ImageView>(R.id.ivEmptyRecent)

        // Apply Iconify icons – quick-action cards
        UiEffects.applyIconify(architectIcon, "md-search")
        UiEffects.applyIconify(marketplaceIcon, "md-store")
        UiEffects.applyIconify(chatIcon, "md-chat", android.graphics.Color.WHITE)

        // Apply Iconify icons – section headers
        UiEffects.applyIconify(icQuickActions, "md-flash-on")
        UiEffects.applyIconify(icRecentActivity, "md-trending-up")
        UiEffects.applyIconify(icProjectProgress, "md-insert-chart")
        UiEffects.applyIconify(ivEmptyRecent, "md-history")

        // Click handlers
        findArchitect.setOnClickListener {
            startActivity(Intent(requireContext(), MatchClientActivity::class.java))
        }
        marketplaceBtn.setOnClickListener {
            startActivity(Intent(requireContext(), MarketPlaceActivity::class.java))
        }
        chatIcon.setOnClickListener {
            startActivity(Intent(requireContext(), MessagesActivity::class.java))
        }
        retryBtn.setOnClickListener {
            currentUserId?.let { loadDashboardModules(it) }
        }

        // Cascading entrance animation for the main content sections
        val statsStrip = view.findViewById<View>(R.id.statsStrip)
        val recentCard = view.findViewById<View>(R.id.recentActivityCard)
        val progressContainer = view.findViewById<View>(R.id.llProjectsContainer)
        UiEffects.applyCascadingEntrance(
            listOf(statsStrip, findArchitect, marketplaceBtn, recentCard, progressContainer),
            startDelay = 80L
        )

        // Apply interactive press effects
        val cardMatches = view.findViewById<View>(R.id.statsStrip).findViewById<View>(R.id.tvStatMatches).parent.parent as View
        val cardPurchase = view.findViewById<View>(R.id.statsStrip).findViewById<View>(R.id.tvStatPurchase).parent.parent as View
        val cardProjects = view.findViewById<View>(R.id.statsStrip).findViewById<View>(R.id.tvStatProjects).parent.parent as View

        UiEffects.applyPressScaleEffect(cardMatches)
        UiEffects.applyPressScaleEffect(cardPurchase)
        UiEffects.applyPressScaleEffect(cardProjects)
        UiEffects.applyPressScaleEffect(findArchitect)
        UiEffects.applyPressScaleEffect(marketplaceBtn)

        // Kick off data loading
        fetchClientProfile(session.userId, tvUserName)
        loadDashboardModules(session.userId)
    }

    // ── Profile fetch ───────────────────────────────────────────────
    private fun fetchClientProfile(clientId: String, tvUserName: TextView) {
        profileCall = ApiClient.instance.getProfile(clientId)
        profileCall?.enqueue(object : Callback<ProfileApiResponse> {
            override fun onResponse(
                call: Call<ProfileApiResponse>,
                response: Response<ProfileApiResponse>
            ) {
                if (!isAdded || viewDestroyed || view == null) return
                val firstName = response.body()?.data?.firstName ?: return
                tvUserName.text = "Welcome, $firstName!"
                UiEffects.applyBlueprintBranding(tvUserName)
            }

            override fun onFailure(call: Call<ProfileApiResponse>, t: Throwable) = Unit
        })
    }

    // ── Dashboard data loading (parallel requests) ──────────────────
    private fun loadDashboardModules(clientId: String) {
        renderDashboardState(DashboardUiState.LOADING)

        matchesCall = ApiClient.instance.getAllMatches(clientId)
        projectsCall = ApiClient.instance.getClientProjects(clientId)
        conversationsCall = ApiClient.instance.getAllMessages(clientId)
        purchaseCall = ApiClient.instance.getPurchasedBlueprints(clientId)

        var matches: List<com.example.blueprintproapps.models.MatchResponse2>? = null
        var projects: List<ClientProjectResponse>? = null
        var conversations: List<com.example.blueprintproapps.models.ConversationResponse>? = null
        var purchases: List<ClientPurchasedBlueprint>? = null
        var failed = false

        fun maybeRender() {
            if (failed) return
            if (matches == null || projects == null || conversations == null || purchases == null) return
            if (!isAdded || viewDestroyed || view == null) return

            val hasData = matches!!.isNotEmpty() || projects!!.isNotEmpty() || conversations!!.isNotEmpty() || purchases!!.isNotEmpty()
            if (!hasData) {
                renderDashboardState(DashboardUiState.EMPTY)
                bindDashboardData(emptyList(), emptyList(), emptyList(), emptyList())
                return
            }
            renderDashboardState(DashboardUiState.SUCCESS)
            bindDashboardData(matches!!, projects!!, conversations!!, purchases!!)
        }

        matchesCall?.enqueue(object : Callback<MatchListResponse> {
            override fun onResponse(
                call: Call<MatchListResponse>,
                response: Response<MatchListResponse>
            ) {
                if (!isAdded || viewDestroyed || view == null) return
                matches = if (response.isSuccessful) response.body()?.matches.orEmpty() else emptyList()
                maybeRender()
            }

            override fun onFailure(call: Call<MatchListResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                failed = true
                renderDashboardState(DashboardUiState.ERROR)
            }
        })

        projectsCall?.enqueue(object : Callback<List<ClientProjectResponse>> {
            override fun onResponse(
                call: Call<List<ClientProjectResponse>>,
                response: Response<List<ClientProjectResponse>>
            ) {
                if (!isAdded || viewDestroyed || view == null) return
                projects = if (response.isSuccessful) response.body().orEmpty() else emptyList()
                maybeRender()
            }

            override fun onFailure(call: Call<List<ClientProjectResponse>>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                failed = true
                renderDashboardState(DashboardUiState.ERROR)
            }
        })

        conversationsCall?.enqueue(object : Callback<ConversationListResponse> {
            override fun onResponse(
                call: Call<ConversationListResponse>,
                response: Response<ConversationListResponse>
            ) {
                if (!isAdded || viewDestroyed || view == null) return
                conversations = if (response.isSuccessful) response.body()?.messages.orEmpty() else emptyList()
                maybeRender()
            }

            override fun onFailure(call: Call<ConversationListResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                failed = true
                renderDashboardState(DashboardUiState.ERROR)
            }
        })

        purchaseCall?.enqueue(object : Callback<List<ClientPurchasedBlueprint>> {
            override fun onResponse(
                call: Call<List<ClientPurchasedBlueprint>>,
                response: Response<List<ClientPurchasedBlueprint>>
            ) {
                if (!isAdded || viewDestroyed || view == null) return
                purchases = if (response.isSuccessful) response.body().orEmpty() else emptyList()
                maybeRender()
            }

            override fun onFailure(call: Call<List<ClientPurchasedBlueprint>>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                failed = true
                renderDashboardState(DashboardUiState.ERROR)
            }
        })
    }

    // ── UI state rendering ──────────────────────────────────────────
    private fun renderDashboardState(state: DashboardUiState) {
        val root = view ?: return
        val status = root.findViewById<TextView>(R.id.tvDashboardState)
        val retry = root.findViewById<MaterialButton>(R.id.btnRetryDashboard)

        when (state) {
            DashboardUiState.LOADING -> {
                status.visibility = View.VISIBLE
                status.text = "Loading dashboard…"
                retry.visibility = View.GONE
            }
            DashboardUiState.SUCCESS -> {
                // Hide the status text entirely on success
                status.visibility = View.GONE
                retry.visibility = View.GONE
            }
            DashboardUiState.EMPTY -> {
                status.visibility = View.VISIBLE
                status.text = "No dashboard data yet."
                retry.visibility = View.GONE
            }
            DashboardUiState.ERROR -> {
                status.visibility = View.VISIBLE
                status.text = "Failed to load dashboard."
                retry.visibility = View.VISIBLE
            }
        }
    }

    // ── Bind live data into the redesigned layout ────────────────────
    private fun bindDashboardData(
        matches: List<com.example.blueprintproapps.models.MatchResponse2>,
        projects: List<ClientProjectResponse>,
        conversations: List<com.example.blueprintproapps.models.ConversationResponse>,
        purchases: List<ClientPurchasedBlueprint>
    ) {
        val root = view ?: return

        // Stats strip
        root.findViewById<TextView>(R.id.tvStatMatches).text = matches.size.toString()
        root.findViewById<TextView>(R.id.tvStatPurchase).text = purchases.size.toString()
        root.findViewById<TextView>(R.id.tvStatProjects).text = projects.size.toString()

        // Recent activity list
        val emptyState = root.findViewById<View>(R.id.emptyRecentState)
        val listContainer = root.findViewById<android.widget.LinearLayout>(R.id.llRecentActivityList)
        listContainer.removeAllViews()

        val hasActivity = conversations.isNotEmpty() || matches.isNotEmpty()

        if (!hasActivity) {
            emptyState.visibility = View.VISIBLE
            listContainer.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            listContainer.visibility = View.VISIBLE

            // Mix messages and matches for a unified activity feed
            val activities = mutableListOf<ActivityItem>()
            conversations.take(2).forEach {
                activities.add(ActivityItem(
                    title = it.architectName ?: "Unknown Architect",
                    subtitle = "Message: ${it.lastMessage ?: "No content"}",
                    icon = "md-chat-bubble",
                    type = ActivityType.MESSAGE,
                    id = it.architectId,
                    photoUrl = it.profileUrl
                ))
            }
            matches.take(2).forEach {
                activities.add(ActivityItem(
                    title = it.architectName ?: "Architect Match",
                    subtitle = "You were matched with ${it.architectName}",
                    icon = "md-people",
                    type = ActivityType.MATCH,
                    id = it.architectId,
                    photoUrl = it.architectPhoto
                ))
            }

            activities.forEach { item ->
                val itemView = layoutInflater.inflate(R.layout.item_recent_activity, listContainer, false)
                val ivIcon = itemView.findViewById<ImageView>(R.id.ivActivityIcon)
                val tvTitle = itemView.findViewById<TextView>(R.id.tvActivityTitle)
                val tvSubtitle = itemView.findViewById<TextView>(R.id.tvActivitySubtitle)

                tvTitle.text = item.title
                tvSubtitle.text = item.subtitle
                
                // Load profile picture with Glide or fallback to Iconify
                val finalPhotoUrl = if (item.photoUrl == "null" || item.photoUrl.isNullOrEmpty()) null else item.photoUrl
                
                if (finalPhotoUrl != null) {
                    // Photo exists: clear icon-specific styling and load image
                    ivIcon.background = null
                    ivIcon.setPadding(0, 0, 0, 0)
                    com.bumptech.glide.Glide.with(this)
                        .load(finalPhotoUrl)
                        .error(R.drawable.profile_pic)
                        .circleCrop()
                        .into(ivIcon)
                } else {
                    // No photo: use a circular background and white icon for uniformity
                    com.bumptech.glide.Glide.with(this).clear(ivIcon)
                    ivIcon.setBackgroundResource(R.drawable.circle_background_blue)
                    val padding = (8 * resources.displayMetrics.density).toInt()
                    ivIcon.setPadding(padding, padding, padding, padding)
                    UiEffects.applyIconify(ivIcon, item.icon, android.graphics.Color.WHITE)
                }

                itemView.setOnClickListener {
                    // Both messages and matches lead to the specific chat conversation
                    val destination = if (requireContext() is com.example.blueprintproapps.network.MessagesActivity) {
                        // This shouldn't happen here as it's a fragment, but let's be safe
                        com.example.blueprintproapps.network.ChatActivity::class.java
                    } else {
                        com.example.blueprintproapps.network.ChatActivity::class.java
                    }
                    
                    val intent = Intent(requireContext(), destination).apply {
                        putExtra("receiverId", item.id)
                        putExtra("receiverName", item.title)
                    }
                    startActivity(intent)
                }
                UiEffects.applyPressScaleEffect(itemView)
                listContainer.addView(itemView)
            }
        }

        // Project progress list
        val projectsContainer = root.findViewById<android.widget.LinearLayout>(R.id.llProjectsContainer)
        projectsContainer.removeAllViews()

        if (projects.isEmpty()) {
            val emptyProjectView = layoutInflater.inflate(R.layout.item_dashboard_project, projectsContainer, false)
            val projectTitleView = emptyProjectView.findViewById<TextView>(R.id.tvProjectTitle)
            val progressBar = emptyProjectView.findViewById<ProgressBar>(R.id.progressProject)
            val phaseView = emptyProjectView.findViewById<TextView>(R.id.tvProjectPhase)
            val percentView = emptyProjectView.findViewById<TextView>(R.id.tvProjectPercent)

            projectTitleView.text = "No active project"
            progressBar.progress = 0
            phaseView.text = "—"
            percentView.text = "0%"

            projectsContainer.addView(emptyProjectView)
        } else {
            projects.forEach { project ->
                val projectView = layoutInflater.inflate(R.layout.item_dashboard_project, projectsContainer, false)
                val projectTitleView = projectView.findViewById<TextView>(R.id.tvProjectTitle)
                val progressBar = projectView.findViewById<ProgressBar>(R.id.progressProject)
                val phaseView = projectView.findViewById<TextView>(R.id.tvProjectPhase)
                val percentView = projectView.findViewById<TextView>(R.id.tvProjectPercent)

                val progress = mapStatusToProgress(project.project_Status)
                projectTitleView.text = project.project_Title ?: "Untitled Project"
                progressBar.progress = progress
                phaseView.text = mapStatusToPhaseLabel(project.project_Status)
                percentView.text = "$progress%"

                projectsContainer.addView(projectView)
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private fun mapStatusToProgress(status: String?): Int {
        val s = status?.lowercase().orEmpty()
        return when {
            "finish" in s || "done" in s || "complete" in s -> 100
            "final" in s -> 90
            "compliance" in s || "permit" in s -> 66
            "review" in s || "development" in s -> 33
            else -> 0
        }
    }

    private fun mapStatusToPhaseLabel(status: String?): String {
        val s = status?.lowercase().orEmpty()
        return when {
            "final" in s || "done" in s || "finish" in s || "complete" in s -> "Completed"
            "compliance" in s || "permit" in s -> "Compliance & Permitting"
            "doc" in s || "construction" in s -> "Construction Docs"
            "review" in s || "development" in s -> "Design Development"
            "design" in s || "schematic" in s -> "Schematic Design"
            "start" in s || "ongoing" in s || "planning" in s || "concept" in s -> "Planning"
            else -> status ?: "Initiated"
        }
    }

    override fun onDestroyView() {
        profileCall?.cancel()
        matchesCall?.cancel()
        projectsCall?.cancel()
        conversationsCall?.cancel()
        profileCall = null
        matchesCall = null
        projectsCall = null
        conversationsCall = null
        currentUserId = null
        viewDestroyed = true
        super.onDestroyView()
    }
}

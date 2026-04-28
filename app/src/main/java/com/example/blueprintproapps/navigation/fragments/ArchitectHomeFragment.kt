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
import com.example.blueprintproapps.models.ArchitectConversationListResponse
import com.example.blueprintproapps.models.ArchitectProjectResponse
import com.example.blueprintproapps.models.ArchitectMatchListResponse
import com.example.blueprintproapps.models.ArchitectBlueprintResponse
import com.example.blueprintproapps.models.ProfileApiResponse
import com.example.blueprintproapps.network.ArchitectBlueprintActivity
import com.example.blueprintproapps.network.ArchitectMessagesActivity
import com.example.blueprintproapps.network.ArchitectProjectActivity
import com.example.blueprintproapps.network.ArchitectMatchActivity
import com.example.blueprintproapps.network.ArchitectChatActivity
import com.example.blueprintproapps.utils.ProjectStatusFormatter
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fragment-based Home screen for the Architect role.
 *
 * Layout sections (matching the web dashboard design):
 *  1. Greeting – personalised "Welcome, [Name]!" with subtitle
 *  2. Stats Strip – Matches / Projects / Messages counts
 *  3. Quick Actions – Blueprint Manager & Project Workspace cards
 *  4. Recent Activity – latest conversation or match summary
 *  5. Project Overview – progress bar with phase & percentage
 */
class ArchitectHomeFragment : Fragment(R.layout.fragment_architect_home) {
    
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
    private var matchesCall: Call<ArchitectMatchListResponse>? = null
    private var projectsCall: Call<List<ArchitectProjectResponse>>? = null
    private var conversationsCall: Call<ArchitectConversationListResponse>? = null
    private var blueprintsCall: Call<List<ArchitectBlueprintResponse>>? = null

    private var viewDestroyed = false
    private var currentUserId: String? = null

    /** Possible states the dashboard can be in. */
    private enum class DashboardUiState { LOADING, SUCCESS, EMPTY, ERROR }

    // ── Lifecycle ───────────────────────────────────────────────────
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false

        val session = AuthSessionManager.requireSession(requireActivity(), UserRole.ARCHITECT)
            ?: return
        currentUserId = session.userId

        // Bind header views
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)

        // Bind quick-action views
        val marketplaceIcon = view.findViewById<ImageView>(R.id.marketplaceIcon)
        val projectIcon = view.findViewById<ImageView>(R.id.projectIcon)
        val chatIcon = view.findViewById<FloatingActionButton>(R.id.chatIcon)
        val forMarketplaceBtn = view.findViewById<MaterialCardView>(R.id.forMarketplaceBtn)
        val forProjectBtn = view.findViewById<MaterialCardView>(R.id.forProjectBtn)
        val retryBtn = view.findViewById<MaterialButton>(R.id.btnRetryDashboard)

        // Section header icons
        val icQuickActions = view.findViewById<ImageView>(R.id.icSectionQuickActions)
        val icRecentActivity = view.findViewById<ImageView>(R.id.icSectionRecentActivity)
        val icProjectProgress = view.findViewById<ImageView>(R.id.icSectionProjectProgress)
        val ivEmptyRecent = view.findViewById<ImageView>(R.id.ivEmptyRecent)

        // Apply Iconify icons – quick-action cards
        UiEffects.applyIconify(marketplaceIcon, "md-store")
        UiEffects.applyIconify(projectIcon, "md-assignment")
        UiEffects.applyIconify(chatIcon, "md-chat", android.graphics.Color.WHITE)

        // Apply Iconify icons – section headers
        UiEffects.applyIconify(icQuickActions, "md-flash-on")
        UiEffects.applyIconify(icRecentActivity, "md-trending-up")
        UiEffects.applyIconify(icProjectProgress, "md-insert-chart")
        UiEffects.applyIconify(ivEmptyRecent, "md-history")

        // Click handlers
        forMarketplaceBtn.setOnClickListener {
            startActivity(Intent(requireContext(), ArchitectBlueprintActivity::class.java))
        }
        forProjectBtn.setOnClickListener {
            startActivity(Intent(requireContext(), ArchitectProjectActivity::class.java))
        }
        chatIcon.setOnClickListener {
            startActivity(Intent(requireContext(), ArchitectMessagesActivity::class.java))
        }
        retryBtn.setOnClickListener {
            currentUserId?.let { loadDashboardModules(it) }
        }

        // Cascading entrance animation for the main content sections
        val statsStrip = view.findViewById<View>(R.id.statsStrip)
        val recentCard = view.findViewById<View>(R.id.recentActivityCard)
        val progressContainer = view.findViewById<View>(R.id.llProjectsContainer)
        UiEffects.applyCascadingEntrance(
            listOf(statsStrip, forMarketplaceBtn, forProjectBtn, recentCard, progressContainer),
            startDelay = 80L
        )

        // Apply interactive press effects
        val cardMatches = view.findViewById<View>(R.id.statsStrip).findViewById<View>(R.id.tvStatMatches).parent.parent as View
        val cardUploads = view.findViewById<View>(R.id.statsStrip).findViewById<View>(R.id.tvStatUploads).parent.parent as View
        val cardProjects = view.findViewById<View>(R.id.statsStrip).findViewById<View>(R.id.tvStatProjects).parent.parent as View

        UiEffects.applyPressScaleEffect(cardMatches)
        UiEffects.applyPressScaleEffect(cardUploads)
        UiEffects.applyPressScaleEffect(cardProjects)
        UiEffects.applyPressScaleEffect(forMarketplaceBtn)
        UiEffects.applyPressScaleEffect(forProjectBtn)

        // Kick off data loading
        fetchArchitectProfile(session.userId, tvUserName)
        loadDashboardModules(session.userId)
    }

    // ── Profile fetch ───────────────────────────────────────────────
    private fun fetchArchitectProfile(architectId: String, tvUserName: TextView) {
        profileCall = ApiClient.instance.getProfile(architectId)
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
    private fun loadDashboardModules(architectId: String) {
        renderDashboardState(DashboardUiState.LOADING)

        matchesCall = ApiClient.instance.getAllMatchesForArchitect(architectId)
        projectsCall = ApiClient.instance.getArchitectProjects(architectId)
        conversationsCall = ApiClient.instance.getAllMessagesForArchitect(architectId)
        blueprintsCall = ApiClient.instance.getArchitectBlueprints(architectId)

        var matches: List<com.example.blueprintproapps.models.ArchitectMatchResponse>? = null
        var projects: List<ArchitectProjectResponse>? = null
        var conversations: List<com.example.blueprintproapps.models.ArchitectConversationResponse>? = null
        var blueprints: List<ArchitectBlueprintResponse>? = null
        var failed = false

        fun maybeRender() {
            if (failed) return
            if (matches == null || projects == null || conversations == null || blueprints == null) return
            if (!isAdded || viewDestroyed || view == null) return

            val hasData = matches!!.isNotEmpty() || projects!!.isNotEmpty() || conversations!!.isNotEmpty() || blueprints!!.isNotEmpty()
            if (!hasData) {
                renderDashboardState(DashboardUiState.EMPTY)
                bindDashboardData(emptyList(), emptyList(), emptyList(), emptyList())
                return
            }
            renderDashboardState(DashboardUiState.SUCCESS)
            bindDashboardData(matches!!, projects!!, conversations!!, blueprints!!)
        }

        matchesCall?.enqueue(object : Callback<ArchitectMatchListResponse> {
            override fun onResponse(
                call: Call<ArchitectMatchListResponse>,
                response: Response<ArchitectMatchListResponse>
            ) {
                if (!isAdded || viewDestroyed || view == null) return
                matches = if (response.isSuccessful) response.body()?.matches.orEmpty() else emptyList()
                maybeRender()
            }

            override fun onFailure(call: Call<ArchitectMatchListResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                failed = true
                renderDashboardState(DashboardUiState.ERROR)
            }
        })

        projectsCall?.enqueue(object : Callback<List<ArchitectProjectResponse>> {
            override fun onResponse(
                call: Call<List<ArchitectProjectResponse>>,
                response: Response<List<ArchitectProjectResponse>>
            ) {
                if (!isAdded || viewDestroyed || view == null) return
                projects = if (response.isSuccessful) response.body().orEmpty() else emptyList()
                maybeRender()
            }

            override fun onFailure(call: Call<List<ArchitectProjectResponse>>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                failed = true
                renderDashboardState(DashboardUiState.ERROR)
            }
        })

        conversationsCall?.enqueue(object : Callback<ArchitectConversationListResponse> {
            override fun onResponse(
                call: Call<ArchitectConversationListResponse>,
                response: Response<ArchitectConversationListResponse>
            ) {
                if (!isAdded || viewDestroyed || view == null) return
                conversations = if (response.isSuccessful) response.body()?.messages.orEmpty() else emptyList()
                maybeRender()
            }

            override fun onFailure(call: Call<ArchitectConversationListResponse>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                failed = true
                renderDashboardState(DashboardUiState.ERROR)
            }
        })

        blueprintsCall?.enqueue(object : Callback<List<ArchitectBlueprintResponse>> {
            override fun onResponse(
                call: Call<List<ArchitectBlueprintResponse>>,
                response: Response<List<ArchitectBlueprintResponse>>
            ) {
                if (!isAdded || viewDestroyed || view == null) return
                blueprints = if (response.isSuccessful) response.body().orEmpty() else emptyList()
                maybeRender()
            }

            override fun onFailure(call: Call<List<ArchitectBlueprintResponse>>, t: Throwable) {
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
        matches: List<com.example.blueprintproapps.models.ArchitectMatchResponse>,
        projects: List<ArchitectProjectResponse>,
        conversations: List<com.example.blueprintproapps.models.ArchitectConversationResponse>,
        blueprints: List<ArchitectBlueprintResponse>
    ) {
        val root = view ?: return

        // Stats strip
        root.findViewById<TextView>(R.id.tvStatMatches).text = matches.size.toString()
        root.findViewById<TextView>(R.id.tvStatUploads).text = blueprints.size.toString()
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
                    title = it.clientName ?: "Unknown Client",
                    subtitle = "Message: ${it.lastMessage ?: "No content"}",
                    icon = "md-chat-bubble",
                    type = ActivityType.MESSAGE,
                    id = it.clientId ?: "",
                    photoUrl = it.profileUrl
                ))
            }
            matches.take(2).forEach {
                activities.add(ActivityItem(
                    title = it.clientName ?: "Client Match",
                    subtitle = "New match with ${it.clientName}",
                    icon = "md-people",
                    type = ActivityType.MATCH,
                    id = it.clientId ?: "",
                    photoUrl = it.clientPhoto
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
                    val intent = Intent(requireContext(), ArchitectChatActivity::class.java).apply {
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

                val progress = ProjectStatusFormatter.progressFor(project.project_Status)
                projectTitleView.text = project.project_Title ?: "Untitled Project"
                progressBar.progress = progress
                phaseView.text = ProjectStatusFormatter.phaseFor(project.project_Status)
                percentView.text = "$progress%"

                projectsContainer.addView(projectView)
            }
        }
    }

    override fun onDestroyView() {
        profileCall?.cancel()
        matchesCall?.cancel()
        projectsCall?.cancel()
        conversationsCall?.cancel()
        blueprintsCall?.cancel()
        profileCall = null
        matchesCall = null
        projectsCall = null
        conversationsCall = null
        blueprintsCall = null
        currentUserId = null
        viewDestroyed = true
        super.onDestroyView()
    }
}

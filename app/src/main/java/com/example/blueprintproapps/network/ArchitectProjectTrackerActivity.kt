package com.example.blueprintproapps.network

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectProjectTrackerAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectProjectTrackerResponse
import com.example.blueprintproapps.utils.UiEffects
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectProjectTrackerActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private var trackerStatus: String? = null
    private var projectId: String = ""
    private var projectStatus: String = ""
    private var blueprintId: Int = 0

    private lateinit var step1Circle: FrameLayout
    private lateinit var step2Circle: FrameLayout
    private lateinit var step3Circle: FrameLayout
    private lateinit var line1: View
    private lateinit var line2: View
    private lateinit var label1: TextView
    private lateinit var label2: TextView
    private lateinit var label3: TextView
    private lateinit var ic1: ImageView
    private lateinit var ic2: ImageView
    private lateinit var ic3: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var projectIdLabel: TextView
    private lateinit var statusBadge: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_architect_project_tracker)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        initViews()

        projectId = intent.getStringExtra("projectId") ?: ""
        blueprintId = intent.getIntExtra("blueprintId", 0)
        projectStatus = intent.getStringExtra("projectStatus") ?: ""

        if (blueprintId == 0) {
            Log.e("TrackerError", "❌ Missing blueprintId in Intent.")
            return
        }

        loadTrackerData(blueprintId)

        // Set Project ID
        projectIdLabel.text = "ID: #${projectId.takeLast(6).uppercase()}"
        statusBadge.text = projectStatus.uppercase()
        
        // Status Badge Color
        val statusColor = when (projectStatus) {
            "Ongoing" -> "#3B82F6"
            "Finished" -> "#10B981"
            "Deleted" -> "#EF4444"
            else -> "#64748B"
        }
        statusBadge.setTextColor(Color.parseColor(statusColor))

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initViews() {
        viewPager = findViewById(R.id.stepViewPager)
        step1Circle = findViewById(R.id.step1Circle)
        step2Circle = findViewById(R.id.step2Circle)
        step3Circle = findViewById(R.id.step3Circle)
        line1 = findViewById(R.id.line1)
        line2 = findViewById(R.id.line2)
        label1 = findViewById(R.id.label1)
        label2 = findViewById(R.id.label2)
        label3 = findViewById(R.id.label3)
        ic1 = findViewById(R.id.ic1)
        ic2 = findViewById(R.id.ic2)
        ic3 = findViewById(R.id.ic3)
        backButton = findViewById(R.id.backButton)
        projectIdLabel = findViewById(R.id.projectIdLabel)
        statusBadge = findViewById(R.id.statusBadge)

        // Interactivity
        UiEffects.applyPressScaleEffect(step1Circle)
        UiEffects.applyPressScaleEffect(step2Circle)
        UiEffects.applyPressScaleEffect(step3Circle)

        step1Circle.setOnClickListener { viewPager.currentItem = 0 }
        step2Circle.setOnClickListener { if (step2Circle.isEnabled) viewPager.currentItem = 1 }
        step3Circle.setOnClickListener { if (step3Circle.isEnabled) viewPager.currentItem = 2 }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateStepperUI(position)
            }
        })
    }

    private fun loadTrackerData(blueprintId: Int) {
        val api = ApiClient.instance
        api.getArchitectProjectTracker(blueprintId).enqueue(object :
            Callback<ArchitectProjectTrackerResponse> {
            override fun onResponse(
                call: Call<ArchitectProjectTrackerResponse>,
                response: Response<ArchitectProjectTrackerResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val tracker = response.body()!!
                    trackerStatus = tracker.projectTrackerStatus ?: "Review"
                    setupViewPager(
                        trackerStatus ?: "Review",
                        tracker.project_Id ?: "",
                        blueprintId,
                        tracker.currentFilePath,
                        ArrayList(tracker.revisionHistory),
                        tracker.projectTrack_Id
                    )
                }
            }
            override fun onFailure(call: Call<ArchitectProjectTrackerResponse>, t: Throwable) {
                Log.e("TrackerError", "❌ Error loading tracker: ${t.message}")
            }
        })
    }

    private fun setupViewPager(
        status: String,
        projectId: String,
        blueprintId: Int,
        currentFilePath: String?,
        revisionHistory: ArrayList<com.example.blueprintproapps.models.ArchitectProjectFileResponse>,
        projectTrackId: Int
    ) {
        val fragments = listOf(
            ArchitectStepReviewFragment.newInstance(projectId, status, blueprintId, currentFilePath, revisionHistory),
            ArchitectStepComplianceFragment.newInstance(projectTrackId.toString(), blueprintId, status, projectId),
            ArchitectStepFinalizationFragment.newInstance(
                projectId = projectId,
                blueprintId = blueprintId,
                projectTrackId = projectTrackId,
                status = status,
                finalBlueprintUrl = currentFilePath,
                projectStatus = projectStatus
            )
        )

        val adapter = ArchitectProjectTrackerAdapter(this, fragments)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false
        applyProjectStatus(status)
    }

    private fun applyProjectStatus(status: String) {
        val stepIndex = when (status) {
            "Review" -> 0
            "Compliance" -> 1
            "Finalization" -> 2
            else -> 0
        }

        step1Circle.isEnabled = true
        step2Circle.isEnabled = stepIndex >= 1 || projectStatus == "Finished"
        step3Circle.isEnabled = stepIndex >= 2 || projectStatus == "Finished"

        val targetPage = if (projectStatus == "Finished") 2 else stepIndex
        viewPager.setCurrentItem(targetPage, false)
        updateStepperUI(targetPage)
    }

    private fun updateStepperUI(currentIndex: Int) {
        val activeColor = Color.parseColor("#344EAD")
        val inactiveColor = Color.parseColor("#E2E8F0")
        val activeLabelColor = Color.parseColor("#344EAD")
        val inactiveLabelColor = Color.parseColor("#64748B")

        step1Circle.backgroundTintList = ColorStateList.valueOf(activeColor)
        ic1.imageTintList = ColorStateList.valueOf(Color.WHITE)
        label1.textColor = activeLabelColor

        if (currentIndex >= 1) {
            step2Circle.backgroundTintList = ColorStateList.valueOf(activeColor)
            ic2.imageTintList = ColorStateList.valueOf(Color.WHITE)
            label2.textColor = activeLabelColor
            line1.backgroundColor = activeColor
        } else {
            step2Circle.backgroundTintList = ColorStateList.valueOf(inactiveColor)
            ic2.imageTintList = ColorStateList.valueOf(inactiveLabelColor)
            label2.textColor = inactiveLabelColor
            line1.backgroundColor = inactiveColor
        }

        if (currentIndex >= 2) {
            step3Circle.backgroundTintList = ColorStateList.valueOf(activeColor)
            ic3.imageTintList = ColorStateList.valueOf(Color.WHITE)
            label3.textColor = activeLabelColor
            line2.backgroundColor = activeColor
        } else {
            step3Circle.backgroundTintList = ColorStateList.valueOf(inactiveColor)
            ic3.imageTintList = ColorStateList.valueOf(inactiveLabelColor)
            label3.textColor = inactiveLabelColor
            line2.backgroundColor = inactiveColor
        }
    }

    private var View.backgroundColor: Int
        get() = 0
        set(value) { this.setBackgroundColor(value) }

    private var TextView.textColor: Int
        get() = 0
        set(value) { this.setTextColor(value) }

    fun reloadTrackerData() {
        loadTrackerData(blueprintId)
    }
}

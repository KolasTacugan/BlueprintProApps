package com.example.blueprintproapps.network

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectProjectTrackerAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectProjectTrackerResponse
import com.google.android.material.progressindicator.LinearProgressIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectProjectTrackerActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var stepProgress: LinearProgressIndicator
    private var trackerStatus: String? = null
    private var projectId: String = ""
    private var blueprintId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_architect_project_tracker)

        viewPager = findViewById(R.id.stepViewPager)
        stepProgress = findViewById(R.id.stepProgress)

        projectId = intent.getStringExtra("projectId") ?: ""
        blueprintId = intent.getIntExtra("blueprintId", 0)

        if (blueprintId == 0) {
            Log.e("TrackerError", "❌ Missing blueprintId in Intent.")
            return
        }

        loadTrackerData(blueprintId)
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

                    Log.d("TrackerSuccess", "✅ Loaded tracker with status: $trackerStatus")

                    setupViewPager(
                        trackerStatus ?: "Review",
                        tracker.project_Id ?: "",
                        blueprintId,
                        tracker.currentFilePath,
                        ArrayList(tracker.revisionHistory),
                        tracker.projectTrack_Id
                    )
                } else {
                    Log.e("TrackerError", "❌ Failed response: ${response.code()}")
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
            ArchitectStepFinalizationFragment.newInstance( projectId = projectId,
                blueprintId = blueprintId,
                projectTrackId = projectTrackId,
                status = status,
                finalBlueprintUrl = currentFilePath)
        )

        val adapter = ArchitectProjectTrackerAdapter(this, fragments)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false // disable swiping

        val btnReview = findViewById<Button>(R.id.btnReview)
        val btnCompliance = findViewById<Button>(R.id.btnCompliance)
        val btnFinalization = findViewById<Button>(R.id.btnFinalization)

        // Define which buttons are enabled based on project status
        when (status) {
            "Review" -> {
                btnReview.isEnabled = true
                btnCompliance.isEnabled = false
                btnFinalization.isEnabled = false
                viewPager.currentItem = 0
            }
            "Compliance" -> {
                btnReview.isEnabled = true
                btnCompliance.isEnabled = true
                btnFinalization.isEnabled = false
                viewPager.currentItem = 1
            }
            "Finalization" -> {
                btnReview.isEnabled = true
                btnCompliance.isEnabled = true
                btnFinalization.isEnabled = true
                viewPager.currentItem = 2
            }
            else -> {
                btnReview.isEnabled = true
                btnCompliance.isEnabled = false
                btnFinalization.isEnabled = false
                viewPager.currentItem = 0
            }
        }

        // Button click listeners
        btnReview.setOnClickListener { viewPager.currentItem = 0 }
        btnCompliance.setOnClickListener {
            if (btnCompliance.isEnabled) viewPager.currentItem = 1
        }
        btnFinalization.setOnClickListener {
            if (btnFinalization.isEnabled) viewPager.currentItem = 2
        }

        // Progress bar updates
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val progress = ((position + 1) / 3f) * 100
                stepProgress.progress = progress.toInt()
            }
        })
    }

    fun reloadTrackerData() {
        loadTrackerData(blueprintId)
    }

}
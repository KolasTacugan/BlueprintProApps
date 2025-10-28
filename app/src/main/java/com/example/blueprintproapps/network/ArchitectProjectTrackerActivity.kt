package com.example.blueprintproapps.network

import android.os.Bundle
import android.util.Log
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
                        ArrayList(tracker.revisionHistory)
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
        revisionHistory: ArrayList<com.example.blueprintproapps.models.ArchitectProjectFileResponse>
    ) {
        val fragments = listOf(
            ArchitectStepReviewFragment.newInstance(projectId, status, blueprintId, currentFilePath, revisionHistory),
            ArchitectStepComplianceFragment.newInstance(projectId, status),
            ArchitectStepFinalizationFragment.newInstance(projectId, status)
        )

        val adapter = ArchitectProjectTrackerAdapter(this, fragments)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = true

        when (status) {
            "Review" -> viewPager.currentItem = 0
            "Compliance" -> viewPager.currentItem = 1
            "Finalization" -> viewPager.currentItem = 2
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val progress = ((position + 1) / 3f) * 100
                stepProgress.progress = progress.toInt()
            }
        })
    }
}

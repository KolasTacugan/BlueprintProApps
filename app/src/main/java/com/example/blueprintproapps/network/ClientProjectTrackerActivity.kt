package com.example.blueprintproapps.network

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ClientProjectTrackerAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ClientProjectTrackerResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientProjectTrackerActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ClientProjectTrackerAdapter

    private lateinit var btnReview: Button
    private lateinit var btnCompliance: Button
    private lateinit var btnFinalization: Button

    private var blueprintId: Int = 0
    private var projectStatus: String = ""
    private var isRated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_project_tracker)

        // get data from intent
        blueprintId = intent.getIntExtra("blueprintId", 0)
        projectStatus = intent.getStringExtra("projectStatus") ?: ""

        if (blueprintId == 0) {
            Toast.makeText(this, "Invalid Blueprint ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        viewPager = findViewById(R.id.stepViewPager)
        btnReview = findViewById(R.id.btnReview)
        btnCompliance = findViewById(R.id.btnCompliance)
        btnFinalization = findViewById(R.id.btnFinalization)

        fetchProjectTracker()

        btnReview.setOnClickListener { viewPager.currentItem = 0 }
        btnCompliance.setOnClickListener { viewPager.currentItem = 1 }
        btnFinalization.setOnClickListener { viewPager.currentItem = 2 }
    }

    private fun fetchProjectTracker() {

        ApiClient.instance.getProjectTracker(blueprintId)
            .enqueue(object : Callback<ClientProjectTrackerResponse> {

                override fun onResponse(
                    call: Call<ClientProjectTrackerResponse>,
                    response: Response<ClientProjectTrackerResponse>
                ) {
                    Log.d("TRACKER_JSON", "RAW: ${response.raw()}")
                    Log.d("TRACKER_JSON", "CODE: ${response.code()}")

                    // -------------------------------
                    // 1️⃣ HANDLE FINISHED PROJECTS WITH NO TRACKER
                    // -------------------------------
                    if (response.code() == 404) {
                        Log.w("TRACKER_JSON", "Tracker not found -> Using fallback")

                        val fallback = ClientProjectTrackerResponse(
                            projectTrack_Id = 0,
                            project_Id = "",
                            currentFileName = "",
                            currentFilePath = "",
                            currentRevision = 0,
                            status = "Finished",
                            finalizationNotes = "",
                            compliance = null,
                            revisionHistory = emptyList(),
                            projectStatus = "Finished",
                            isRated = false,
                            architectName = ""
                        )

                        setupViewPager(fallback)
                        applyStatusUI("Finished")
                        return
                    }

                    // -------------------------------
                    // 2️⃣ NORMAL SUCCESS RESPONSE
                    // -------------------------------
                    if (response.isSuccessful && response.body() != null) {

                        val data = response.body()!!

                        val safeData = data.copy(
                            revisionHistory = data.revisionHistory ?: emptyList(),
                            compliance = data.compliance
                        )

                        isRated = safeData.isRated

                        setupViewPager(safeData)
                        applyStatusUI(safeData.status)
                        return
                    }

                    // -------------------------------
                    // 3️⃣ OTHER ERRORS
                    // -------------------------------
                    Log.e("TRACKER_JSON", "ERROR: ${response.errorBody()?.string()}")
                    Toast.makeText(
                        this@ClientProjectTrackerActivity,
                        "Failed to load project tracker",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(call: Call<ClientProjectTrackerResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ClientProjectTrackerActivity,
                        "Failed to load tracker.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    private fun setupViewPager(data: ClientProjectTrackerResponse) {
        adapter = ClientProjectTrackerAdapter(this, data)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false // disable manual swiping
    }

    private fun applyStatusUI(status: String) {

        // Determine step index
        val stepIndex = when (status) {
            "Review" -> 0
            "Compliance" -> 1
            "Finalization" -> 2
            else -> 0
        }

        // Enable/Disable buttons based on status
        btnReview.isEnabled = true
        btnCompliance.isEnabled = stepIndex >= 1
        btnFinalization.isEnabled = stepIndex >= 2

        // Set correct page
        viewPager.setCurrentItem(stepIndex, false)

        // Special case: Finished projects → allow all steps
        if (projectStatus == "Finished") {
            btnReview.isEnabled = true
            btnCompliance.isEnabled = true
            btnFinalization.isEnabled = true

            viewPager.setCurrentItem(2, false)
        }

        // Button click behavior
        btnReview.setOnClickListener { viewPager.currentItem = 0 }

        btnCompliance.setOnClickListener {
            if (btnCompliance.isEnabled) {
                viewPager.currentItem = 1
            }
        }

        btnFinalization.setOnClickListener {
            if (btnFinalization.isEnabled) {
                viewPager.currentItem = 2
            }
        }
    }

}

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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ClientProjectTrackerAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ClientProjectTrackerResponse
import com.example.blueprintproapps.utils.UiEffects
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientProjectTrackerActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ClientProjectTrackerAdapter

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
    private lateinit var architectNameLabel: TextView
    private lateinit var statusBadge: TextView

    private var blueprintId: Int = 0
    private var projectStatus: String = ""
    private var isRated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_client_project_tracker)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // get data from intent
        blueprintId = intent.getIntExtra("blueprintId", 0)
        projectStatus = intent.getStringExtra("projectStatus") ?: ""

        if (blueprintId == 0) {
            Toast.makeText(this, "Invalid Blueprint ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        initViews()
        fetchProjectTracker()

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
        architectNameLabel = findViewById(R.id.architectNameLabel)
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

    private fun fetchProjectTracker() {
        ApiClient.instance.getProjectTracker(blueprintId)
            .enqueue(object : Callback<ClientProjectTrackerResponse> {
                override fun onResponse(
                    call: Call<ClientProjectTrackerResponse>,
                    response: Response<ClientProjectTrackerResponse>
                ) {
                    if (response.code() == 404) {
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
                        applyProjectStatus(fallback.status)
                        return
                    }

                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        val safeData = data.copy(
                            revisionHistory = data.revisionHistory ?: emptyList(),
                            compliance = data.compliance
                        )
                        isRated = safeData.isRated
                        architectNameLabel.text = "Architect: ${safeData.architectName}"
                        statusBadge.text = projectStatus.uppercase()
                        
                        // Status Badge Color
                        val statusColor = when (projectStatus) {
                            "Ongoing" -> "#3B82F6"
                            "Finished" -> "#10B981"
                            else -> "#64748B"
                        }
                        statusBadge.setTextColor(Color.parseColor(statusColor))

                        setupViewPager(safeData)
                        applyProjectStatus(safeData.status)
                        return
                    }

                    Toast.makeText(this@ClientProjectTrackerActivity, "Failed to load project tracker", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<ClientProjectTrackerResponse>, t: Throwable) {
                    Toast.makeText(this@ClientProjectTrackerActivity, "Failed to load tracker.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupViewPager(data: ClientProjectTrackerResponse) {
        adapter = ClientProjectTrackerAdapter(this, data)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false
    }

    private fun applyProjectStatus(status: String) {
        val stepIndex = when (status) {
            "Review" -> 0
            "Compliance" -> 1
            "Finalization" -> 2
            else -> 0
        }

        // Enable steps based on progress
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

        // Step 1
        step1Circle.backgroundTintList = ColorStateList.valueOf(activeColor)
        ic1.imageTintList = ColorStateList.valueOf(Color.WHITE)
        label1.textColor = activeLabelColor

        // Step 2
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

        // Step 3
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
        get() = 0 // Not needed
        set(value) { this.setBackgroundColor(value) }

    private var TextView.textColor: Int
        get() = 0 // Not needed
        set(value) { this.setTextColor(value) }
}

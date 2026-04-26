package com.example.blueprintproapps.network

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectProjectAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ArchitectProjectResponse
import com.example.blueprintproapps.models.ClientProfileResponse
import com.example.blueprintproapps.utils.ClientProfileBottomSheet
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectProjectActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArchitectProjectAdapter
    private lateinit var backBtn: ImageButton
    private lateinit var addBtn: FloatingActionButton
    private lateinit var moreOptionsBtn: FloatingActionButton
    private lateinit var searchEditText: EditText
    private lateinit var filterBtn: ImageButton
    private lateinit var filterScroll: View
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var emptyState: View

    private var isFabOpen = false
    private lateinit var architectId: String
    private var allProjects: List<ArchitectProjectResponse> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = AuthSessionManager.requireSession(this, UserRole.ARCHITECT) ?: return
        architectId = session.userId
        enableEdgeToEdge()
        setContentView(R.layout.activity_architect_project)

        val main = findViewById<View>(R.id.main) ?: findViewById<View>(android.R.id.content)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerViewBlueprints)
        backBtn = findViewById(R.id.backButton)
        addBtn = findViewById(R.id.addProjectBtn)
        moreOptionsBtn = findViewById(R.id.moreOptionsBtn)
        searchEditText = findViewById(R.id.searchEditText)
        filterBtn = findViewById(R.id.filterBtn)
        filterScroll = findViewById(R.id.filterScroll)
        filterChipGroup = findViewById(R.id.filterChipGroup)
        emptyState = findViewById(R.id.emptyState)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ArchitectProjectAdapter(
            mutableListOf(),
            this,
            onDeleteClick = { projectId ->
                showDeleteConfirmation(projectId)
            },
            onClientClick = { clientId ->
                fetchClientProfile(clientId)
            }
        )
        recyclerView.adapter = adapter

        // Search logic
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProjects()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Filter button toggle logic
        filterBtn.setOnClickListener {
            if (filterScroll.visibility == View.VISIBLE) {
                filterScroll.visibility = View.GONE
            } else {
                filterScroll.visibility = View.VISIBLE
                filterScroll.alpha = 0f
                filterScroll.animate().alpha(1f).setDuration(300).start()
            }
        }

        // Filter chip logic
        filterChipGroup.setOnCheckedChangeListener { _, _ ->
            filterProjects()
        }

        moreOptionsBtn.setOnClickListener {
            toggleFabMenu()
        }

        // 🔙 Back to Architect Dashboard
        backBtn.setOnClickListener {
            onBackPressed()
        }

        // ➕ Add Project
        addBtn.setOnClickListener {
            val intent = Intent(this, UploadProjectBlueprintActivity::class.java)
            startActivity(intent)
        }

        loadProjects()

        findViewById<View>(R.id.clearFilterBtn).setOnClickListener {
            searchEditText.text.clear()
            filterChipGroup.check(R.id.chipAll)
            filterProjects()
        }
    }

    private fun filterProjects() {
        val query = searchEditText.text.toString().lowercase()
        val checkedChipId = filterChipGroup.checkedChipId
        
        val filtered = allProjects.filter { project ->
            val matchesQuery = project.project_Title.lowercase().contains(query) || 
                             project.clientName.lowercase().contains(query)
            
            val matchesFilter = when (checkedChipId) {
                R.id.chipOngoing -> project.project_Status == "Ongoing"
                R.id.chipFinished -> project.project_Status == "Finished"
                R.id.chipDeleted -> project.project_Status == "Deleted"
                else -> true // All
            }
            
            matchesQuery && matchesFilter
        }
        
        adapter.updateData(filtered)
        emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun fetchClientProfile(clientId: String) {
        ApiClient.instance.getClientProfile(clientId)
            .enqueue(object : Callback<ClientProfileResponse> {
                override fun onResponse(
                    call: Call<ClientProfileResponse>,
                    response: Response<ClientProfileResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val bottomSheet = ClientProfileBottomSheet(response.body()!!)
                        bottomSheet.show(supportFragmentManager, "ClientProfile")
                    } else {
                        Toast.makeText(this@ArchitectProjectActivity, "Failed to load client profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ClientProfileResponse>, t: Throwable) {
                    Toast.makeText(this@ArchitectProjectActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun toggleFabMenu() {
        if (isFabOpen) {
            isFabOpen = false
            moreOptionsBtn.animate().rotation(0f).setDuration(300).start()
            addBtn.animate().translationY(0f).alpha(0f).setDuration(300).withEndAction {
                addBtn.visibility = View.GONE
            }.start()
        } else {
            isFabOpen = true
            moreOptionsBtn.animate().rotation(45f).setDuration(300).start()
            addBtn.visibility = View.VISIBLE
            addBtn.alpha = 0f
            addBtn.animate().translationY(-20f).alpha(1f).setDuration(300).start()
        }
    }

    private fun loadProjects() {
        ApiClient.instance.getArchitectProjects(architectId)
            .enqueue(object : Callback<List<ArchitectProjectResponse>> {
                override fun onResponse(
                    call: Call<List<ArchitectProjectResponse>>,
                    response: Response<List<ArchitectProjectResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        allProjects = response.body()!!
                        filterProjects() // Initial filter (All)
                    } else {
                        Toast.makeText(this@ArchitectProjectActivity, "Failed to load projects", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ArchitectProjectResponse>>, t: Throwable) {
                    Toast.makeText(this@ArchitectProjectActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showDeleteConfirmation(projectId: String) {
        AlertDialog.Builder(this, R.style.PremiumAlertDialog)
            .setTitle("Delete Project?")
            .setMessage("Are you sure you want to move this project to the bin?")
            .setPositiveButton("Delete") { _, _ ->
                deleteProject(projectId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteProject(projectId: String) {
        ApiClient.instance.deleteProject(projectId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ArchitectProjectActivity, "Project moved to bin", Toast.LENGTH_SHORT).show()
                    loadProjects()
                } else {
                    Toast.makeText(this@ArchitectProjectActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ArchitectProjectActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadProjects()
    }
}

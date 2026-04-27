package com.example.blueprintproapps.network

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectProjectAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectProjectResponse
import com.example.blueprintproapps.models.ClientProfileResponse
import com.example.blueprintproapps.utils.ClientProfileBottomSheet
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
    private lateinit var deletedProjectsBtn: FloatingActionButton


    private val fabColorNormal = android.graphics.Color.parseColor("#0D3C80")
    private val fabColorExpanded = android.graphics.Color.WHITE

    private val iconColorNormal = android.graphics.Color.WHITE
    private val iconColorExpanded = android.graphics.Color.parseColor("#0D3C80")

    private var isFabOpen = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_architect_project)

        recyclerView = findViewById(R.id.recyclerViewBlueprints)
        backBtn = findViewById(R.id.backButton)
        addBtn = findViewById(R.id.addProjectBtn)
        moreOptionsBtn = findViewById(R.id.moreOptionsBtn)
        deletedProjectsBtn = findViewById(R.id.deletedProjectsBtn)

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

        moreOptionsBtn.setOnClickListener {
            if (isFabOpen) {
                closeFabMenu()
            } else {
                openFabMenu()
            }
        }

        deletedProjectsBtn.setOnClickListener {
            closeFabMenu()

            val sharedPrefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val architectId = sharedPrefs.getString("architectId", null)

            val intent = Intent(this, ArchitectDeletedProjectsActivity::class.java)
            intent.putExtra("architectId", architectId)
            startActivity(intent)
        }



        // 🔙 Back to Architect Dashboard
        backBtn.setOnClickListener {
            val intent = Intent(this, ArchitectDashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        // ➕ Add Project (upload blueprint for project)
        addBtn.setOnClickListener {
            val intent = Intent(this, UploadProjectBlueprintActivity::class.java)
            startActivity(intent)
        }

        loadProjects()
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

    private fun openFabMenu() {
        isFabOpen = true

        moreOptionsBtn.backgroundTintList = ColorStateList.valueOf(fabColorExpanded)
        moreOptionsBtn.imageTintList = ColorStateList.valueOf(iconColorExpanded)

        addBtn.visibility = View.VISIBLE
        deletedProjectsBtn.visibility = View.VISIBLE

        addBtn.animate().translationY(-90f).alpha(1f).setDuration(200).start()
        deletedProjectsBtn.animate().translationY(-160f).alpha(1f).setDuration(200).start()

        moreOptionsBtn.animate().rotation(90f).setDuration(200).start()
    }

    private fun closeFabMenu() {
        isFabOpen = false

        moreOptionsBtn.backgroundTintList = ColorStateList.valueOf(fabColorNormal)
        moreOptionsBtn.imageTintList = ColorStateList.valueOf(iconColorNormal)

        addBtn.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
            addBtn.visibility = View.GONE
        }.start()

        deletedProjectsBtn.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
            deletedProjectsBtn.visibility = View.GONE
        }.start()

        moreOptionsBtn.animate().rotation(0f).setDuration(200).start()
    }

    private fun loadProjects() {
        val sharedPrefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val architectId = sharedPrefs.getString("architectId", null)

        if (architectId.isNullOrEmpty()) {
            Toast.makeText(this, "Architect ID not found. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.getArchitectProjects(architectId)
            .enqueue(object : Callback<List<ArchitectProjectResponse>> {
                override fun onResponse(
                    call: Call<List<ArchitectProjectResponse>>,
                    response: Response<List<ArchitectProjectResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val projectList = response.body()!!
                        adapter.updateData(projectList)
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
        AlertDialog.Builder(this)
            .setTitle("Delete Project?")
            .setMessage("Are you sure you want to delete this project? Once deleted, it cannot be restored.")
            .setPositiveButton("Delete") { _, _ ->
                deleteProject(projectId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteProject(projectId: String) {
        val api = ApiClient.instance

        api.deleteProject(projectId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ArchitectProjectActivity, "Project Deleted", Toast.LENGTH_SHORT).show()
                    loadProjects() // refresh list
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

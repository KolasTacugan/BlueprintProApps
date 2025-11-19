package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectDeletedProjectsAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.DeletedProjectResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectDeletedProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArchitectDeletedProjectsAdapter
    private val deletedList = mutableListOf<DeletedProjectResponse>()

    private lateinit var btnBack: ImageView
    private lateinit var architectId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_architect_deleted_projects)

        // Get architectId properly
        architectId = intent.getStringExtra("architectId")
            ?: getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("architectId", null)
                    ?: ""

        if (architectId.isEmpty()) {
            Toast.makeText(this, "Architect ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Back button
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        recyclerView = findViewById(R.id.recyclerViewDeletedProjects)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ArchitectDeletedProjectsAdapter(
            deletedList,
            onRestore = { id -> showRestoreConfirmation(id) },
            onPermanentDelete = { id -> showPermanentDeleteConfirmation(id) }
        )

        recyclerView.adapter = adapter
        loadDeletedProjects()
    }

    // ---------------------------------------
    // Load deleted projects
    // ---------------------------------------
    private fun loadDeletedProjects() {
        ApiClient.instance.getDeletedProjects(architectId)
            .enqueue(object : Callback<List<DeletedProjectResponse>> {
                override fun onResponse(
                    call: Call<List<DeletedProjectResponse>>,
                    response: Response<List<DeletedProjectResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        deletedList.clear()
                        deletedList.addAll(response.body()!!)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            this@ArchitectDeletedProjectsActivity,
                            "Failed to load deleted projects",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<DeletedProjectResponse>>, t: Throwable) {
                    Toast.makeText(
                        this@ArchitectDeletedProjectsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // ---------------------------------------
    // Confirmation Before Restore
    // ---------------------------------------
    private fun showRestoreConfirmation(projectId: String) {
        AlertDialog.Builder(this)
            .setTitle("Restore Project?")
            .setMessage("Are you sure you want to restore this project?")
            .setPositiveButton("Restore") { _, _ ->
                restoreProject(projectId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------------------------------------
    // Restore Project
    // ---------------------------------------
    private fun restoreProject(projectId: String) {
        ApiClient.instance.restoreProject(projectId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ArchitectDeletedProjectsActivity,
                            "Project Restored",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Refresh deleted list
                        loadDeletedProjects()

                        // Return to ArchitectProjectActivity to refresh the active projects
                        val intent = Intent(
                            this@ArchitectDeletedProjectsActivity,
                            ArchitectProjectActivity::class.java
                        )
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(
                            this@ArchitectDeletedProjectsActivity,
                            "Restore failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(
                        this@ArchitectDeletedProjectsActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // ---------------------------------------
    // Confirmation Before Permanent Delete
    // ---------------------------------------
    private fun showPermanentDeleteConfirmation(projectId: String) {
        AlertDialog.Builder(this)
            .setTitle("Permanently Delete?")
            .setMessage("This cannot be undone. Delete this project permanently?")
            .setPositiveButton("Delete") { _, _ ->
                deleteProjectPermanently(projectId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------------------------------------
    // Permanently Delete Project
    // ---------------------------------------
    private fun deleteProjectPermanently(projectId: String) {
        ApiClient.instance.permanentlyDeleteProject(projectId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ArchitectDeletedProjectsActivity,
                            "Deleted Permanently",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Refresh list
                        loadDeletedProjects()

                    } else {
                        Toast.makeText(
                            this@ArchitectDeletedProjectsActivity,
                            "Delete failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(
                        this@ArchitectDeletedProjectsActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}

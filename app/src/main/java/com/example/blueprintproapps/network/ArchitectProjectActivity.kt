package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectProjectAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectProjectResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectProjectActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArchitectProjectAdapter
    private lateinit var backBtn: ImageButton
    private lateinit var addBtn: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_architect_project)

        recyclerView = findViewById(R.id.recyclerViewBlueprints)
        backBtn = findViewById(R.id.backButton)
        addBtn = findViewById(R.id.addProjectBtn)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ArchitectProjectAdapter(mutableListOf(), this)
        recyclerView.adapter = adapter

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
}

package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectBlueprintAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectBlueprintResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ArchitectBlueprintActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArchitectBlueprintAdapter
    private lateinit var backBtn: ImageButton
    private lateinit var addBtn: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_architect_blueprint)

        recyclerView = findViewById(R.id.recyclerViewBlueprints)
        backBtn = findViewById(R.id.backButton)
        addBtn = findViewById(R.id.addBlueprintBtn)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ArchitectBlueprintAdapter(mutableListOf(), this)
        recyclerView.adapter = adapter

        // 🔙 Back to Architect Dashboard
        backBtn.setOnClickListener {
            val intent = Intent(this, ArchitectDashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        // ➕ Add Blueprint (go to upload)
        addBtn.setOnClickListener {
            // Uncomment when activity is ready
            val intent = Intent(this, UploadMarketplaceBlueprintActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Add Blueprint clicked", Toast.LENGTH_SHORT).show()
        }
        loadBlueprints()
    }

    private fun loadBlueprints() {
        val sharedPrefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val architectId = sharedPrefs.getString("architectId", null)

        if (architectId.isNullOrEmpty()) {
            Toast.makeText(this, "Architect ID not found. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.getArchitectBlueprints(architectId)
            .enqueue(object : retrofit2.Callback<List<ArchitectBlueprintResponse>> {
                override fun onResponse(
                    call: Call<List<ArchitectBlueprintResponse>>,
                    response: Response<List<ArchitectBlueprintResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val blueprintList = response.body()!!
                        adapter.updateData(blueprintList)
                    } else {
                        Toast.makeText(this@ArchitectBlueprintActivity, "Failed to load blueprints", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(
                    call: retrofit2.Call<List<ArchitectBlueprintResponse>>,
                    t: Throwable
                ) {
                    Toast.makeText(this@ArchitectBlueprintActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

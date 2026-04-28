package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.view.View
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectBlueprintAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ArchitectBlueprintResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ArchitectBlueprintActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArchitectBlueprintAdapter
    private lateinit var backBtn: ImageButton
    private lateinit var addBtn: FloatingActionButton
    private lateinit var stateText: TextView
    private lateinit var architectId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = AuthSessionManager.requireSession(this, UserRole.ARCHITECT) ?: return
        architectId = session.userId
        enableEdgeToEdge()
        setContentView(R.layout.activity_architect_blueprint)

        recyclerView = findViewById(R.id.recyclerViewBlueprints)
        backBtn = findViewById(R.id.backButton)
        addBtn = findViewById(R.id.addBlueprintBtn)
        stateText = findViewById(R.id.blueprintState)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ArchitectBlueprintAdapter(mutableListOf(), this)
        recyclerView.adapter = adapter

        backBtn.setOnClickListener { finish() }

        // ➕ Add Blueprint (go to upload)
        addBtn.setOnClickListener {
            // Uncomment when activity is ready
            val intent = Intent(this, UploadMarketplaceBlueprintActivity::class.java)
            startActivity(intent)
        }
        loadBlueprints()
    }
    override fun onResume() {
        super.onResume()
        loadBlueprints()
    }
    private fun loadBlueprints() {
        renderState("Loading blueprints...", showList = false)
        ApiClient.instance.getArchitectBlueprints(architectId)
            .enqueue(object : retrofit2.Callback<List<ArchitectBlueprintResponse>> {
                override fun onResponse(
                    call: Call<List<ArchitectBlueprintResponse>>,
                    response: Response<List<ArchitectBlueprintResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val blueprintList = response.body()!!
                        adapter.updateData(blueprintList)
                        renderState(
                            if (blueprintList.isEmpty()) "No marketplace blueprints yet. Tap + to upload your first design." else "",
                            showList = blueprintList.isNotEmpty()
                        )
                    } else {
                        renderState("Failed to load blueprints.", showList = false)
                        Toast.makeText(this@ArchitectBlueprintActivity, "Failed to load blueprints", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(
                    call: retrofit2.Call<List<ArchitectBlueprintResponse>>,
                    t: Throwable
                ) {
                    renderState("Network error while loading blueprints.", showList = false)
                    Toast.makeText(this@ArchitectBlueprintActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun renderState(message: String, showList: Boolean) {
        stateText.text = message
        stateText.visibility = if (message.isBlank()) View.GONE else View.VISIBLE
        recyclerView.visibility = if (showList) View.VISIBLE else View.GONE
    }
}

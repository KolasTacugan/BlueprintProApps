package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ClientProjectAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ClientProjectResponse
import com.example.blueprintproapps.utils.ArchitectProfileBottomSheet
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClientProjectAdapter
    private var projectList = ArrayList<ClientProjectResponse>()
    private var clientId: String? = null

    private lateinit var backBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_projects)

        clientId = intent.getStringExtra("clientId")

        recyclerView = findViewById(R.id.recyclerViewClientProjects)
        recyclerView.layoutManager = LinearLayoutManager(this)
        backBtn = findViewById(R.id.backButton)
        // 🔥 ATTACH EMPTY ADAPTER FIRST
        adapter = ClientProjectAdapter(projectList, this,
            onItemClick = { project ->
                // existing code
            },
            onArchitectNameClick = { architectId ->
                openArchitectProfileBottomSheet(architectId)
            }
        )

        recyclerView.adapter = adapter

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ClientProjectsActivity, ClientDashboardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

        backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fetchProjects()
    }

    private fun openArchitectProfileBottomSheet(architectId: String) {
        val sheet = ArchitectProfileBottomSheet(architectId)
        sheet.show(supportFragmentManager, "ArchitectProfile")
    }

    private fun fetchProjects() {

        if (clientId.isNullOrEmpty()) {
            Toast.makeText(this, "Client ID not found.", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = ApiClient.instance

        apiService.getClientProjects(clientId!!).enqueue(object : Callback<List<ClientProjectResponse>> {
            override fun onResponse(
                call: Call<List<ClientProjectResponse>>,
                response: Response<List<ClientProjectResponse>>
            ) {
                if (response.isSuccessful) {

                    // 🔥 UPDATE EXISTING LIST INSTEAD OF REPLACING IT
                    projectList.clear()
                    projectList.addAll(response.body() ?: emptyList())

                    // 🔥 TELL RECYCLER TO RE-RENDER
                    adapter.notifyDataSetChanged()

                } else {
                    Toast.makeText(this@ClientProjectsActivity, "Failed to load", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ClientProjectResponse>>, t: Throwable) {
                Log.e("API", t.message.toString())
                Toast.makeText(this@ClientProjectsActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
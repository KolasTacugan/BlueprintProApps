package com.example.blueprintproapps.network

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ClientProjectAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ClientProjectResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClientProjectAdapter
    private var projectList = ArrayList<ClientProjectResponse>()
    private var clientId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_projects)

        clientId = intent.getStringExtra("clientId")

        recyclerView = findViewById(R.id.recyclerViewClientProjects)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 🔥 ATTACH EMPTY ADAPTER FIRST
        adapter = ClientProjectAdapter(projectList, this) {}
        recyclerView.adapter = adapter

        fetchProjects()
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

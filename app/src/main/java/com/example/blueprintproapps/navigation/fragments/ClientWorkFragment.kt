package com.example.blueprintproapps.navigation.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ClientProjectAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ClientProjectResponse
import com.example.blueprintproapps.utils.ArchitectProfileBottomSheet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientWorkFragment : Fragment(R.layout.fragment_client_projects) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClientProjectAdapter
    private val projectList = ArrayList<ClientProjectResponse>()
    private var clientId: String? = null
    private var projectsCall: Call<List<ClientProjectResponse>>? = null
    private var viewDestroyed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false
        val session = AuthSessionManager.requireSession(requireActivity(), UserRole.CLIENT) ?: return
        clientId = session.userId
        recyclerView = view.findViewById(R.id.recyclerViewClientProjects)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ClientProjectAdapter(projectList, requireContext(), onItemClick = { }, onArchitectNameClick = { architectId ->
            ArchitectProfileBottomSheet(architectId).show(parentFragmentManager, "ArchitectProfile")
        })
        recyclerView.adapter = adapter
        fetchProjects()
    }

    private fun fetchProjects() {
        val id = clientId ?: return
        projectsCall = ApiClient.instance.getClientProjects(id)
        projectsCall?.enqueue(object : Callback<List<ClientProjectResponse>> {
            override fun onResponse(call: Call<List<ClientProjectResponse>>, response: Response<List<ClientProjectResponse>>) {
                if (!isAdded || viewDestroyed || view == null) return
                if (response.isSuccessful) {
                    projectList.clear()
                    projectList.addAll(response.body().orEmpty())
                    adapter.notifyDataSetChanged()
                } else {
                    val safeContext = context ?: return
                    Toast.makeText(safeContext, "Failed to load", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<ClientProjectResponse>>, t: Throwable) {
                if (!isAdded || viewDestroyed || view == null) return
                Log.e("API", t.message.toString())
                val safeContext = context ?: return
                Toast.makeText(safeContext, "Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        projectsCall?.cancel()
        projectsCall = null
        viewDestroyed = true
        super.onDestroyView()
    }
}

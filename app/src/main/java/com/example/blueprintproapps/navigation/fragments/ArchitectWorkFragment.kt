package com.example.blueprintproapps.navigation.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectMatchRequestAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ArchitectMatchRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectWorkFragment : Fragment(R.layout.fragment_architect_work) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArchitectMatchRequestAdapter
    private lateinit var architectId: String
    private var pendingMatchesCall: Call<List<ArchitectMatchRequest>>? = null
    private var respondMatchCall: Call<Void>? = null
    private var viewDestroyed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false
        val session = AuthSessionManager.requireSession(requireActivity(), UserRole.ARCHITECT) ?: return
        architectId = session.userId
        recyclerView = view.findViewById(R.id.recyclerMatchRequests)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ArchitectMatchRequestAdapter(
            mutableListOf(),
            onAccept = { matchId -> respondToMatch(matchId, true) },
            onDecline = { matchId -> respondToMatch(matchId, false) }
        )
        recyclerView.adapter = adapter
        fetchPendingRequests()
    }

    private fun fetchPendingRequests() {
        pendingMatchesCall = ApiClient.instance.getPendingMatches(architectId)
        pendingMatchesCall?.enqueue(object : Callback<List<ArchitectMatchRequest>> {
                override fun onResponse(call: Call<List<ArchitectMatchRequest>>, response: Response<List<ArchitectMatchRequest>>) {
                    if (!isAdded || viewDestroyed || view == null) return
                    if (response.isSuccessful) {
                        adapter.updateData(response.body().orEmpty())
                    }
                }

                override fun onFailure(call: Call<List<ArchitectMatchRequest>>, t: Throwable) {
                    if (!isAdded || viewDestroyed || view == null) return
                    val safeContext = context ?: return
                    Toast.makeText(safeContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun respondToMatch(matchId: String, approve: Boolean) {
        respondMatchCall = ApiClient.instance.respondMatch(matchId, approve)
        respondMatchCall?.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!isAdded || viewDestroyed || view == null) return
                    if (response.isSuccessful) {
                        fetchPendingRequests()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) = Unit
            })
    }

    override fun onDestroyView() {
        pendingMatchesCall?.cancel()
        respondMatchCall?.cancel()
        pendingMatchesCall = null
        respondMatchCall = null
        viewDestroyed = true
        super.onDestroyView()
    }
}

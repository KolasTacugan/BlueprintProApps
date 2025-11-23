package com.example.blueprintproapps.network

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ClientRevisionHistoryAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ClientProjectFile

class ClientStepReviewFragment : Fragment() {

    private lateinit var currentRevisionName: TextView
    private lateinit var revisionsRecyclerView: RecyclerView

    companion object {
        fun newInstance(
            fileName: String?,
            filePath: String?,
            revision: Int?,
            revisionHistory: List<ClientProjectFile>?
        ): ClientStepReviewFragment {

            val fragment = ClientStepReviewFragment()
            val args = Bundle()

            args.putString("fileName", fileName ?: "")
            args.putString("filePath", filePath ?: "")
            args.putInt("revision", revision ?: 0)

            // Force-convert to ArrayList<Parcelable>
            val safeList = ArrayList<ClientProjectFile>()
            if (!revisionHistory.isNullOrEmpty()) {
                safeList.addAll(revisionHistory)
            }

            args.putParcelableArrayList("revisionHistory", safeList)

            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_client_step_review, container, false)

        currentRevisionName = v.findViewById(R.id.currentRevisionName)
        revisionsRecyclerView = v.findViewById(R.id.revisionsRecyclerView)

        val fileName = arguments?.getString("fileName") ?: ""
        val filePath = arguments?.getString("filePath") ?: ""
        val revision = arguments?.getInt("revision") ?: 0
        val revisionHistory = arguments?.getParcelableArrayList<ClientProjectFile>("revisionHistory")
            ?: arrayListOf()

        // Current file linking
        if (filePath.isNotEmpty()) {
            currentRevisionName.text = "Current Version"

            currentRevisionName.setOnClickListener {
                val uri = if (filePath.startsWith("http"))
                    Uri.parse(filePath)
                else
                    Uri.parse(ApiClient.getBaseUrl() + filePath)

                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        } else {
            currentRevisionName.text = "No file uploaded yet."
        }

        // Revision list
        revisionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        if (revisionHistory.isNullOrEmpty()) {
            revisionsRecyclerView.adapter = ClientRevisionHistoryAdapter(arrayListOf())
        } else {
            revisionsRecyclerView.adapter = ClientRevisionHistoryAdapter(revisionHistory)
        }

        return v
    }
}

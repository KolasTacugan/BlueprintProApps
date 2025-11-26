package com.example.blueprintproapps.network

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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

            // Force safe Parcelable list
            val safeList = ArrayList<ClientProjectFile>()
            if (!revisionHistory.isNullOrEmpty()) safeList.addAll(revisionHistory)

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
        val openCurrentBtn = v.findViewById<Button>(R.id.openCurrentBtn)

        val fileName = arguments?.getString("fileName") ?: ""
        val filePath = arguments?.getString("filePath") ?: ""
        val revision = arguments?.getInt("revision") ?: 0
        val revisionHistory = arguments?.getParcelableArrayList<ClientProjectFile>("revisionHistory")
            ?: arrayListOf()

        // --- CURRENT FILE DISPLAY ---
        if (filePath.isNotEmpty()) {
            currentRevisionName.text = "Current Version"
        } else {
            currentRevisionName.text = "No file uploaded yet."
        }

        // --- OPEN CURRENT VERSION BUTTON ---
        openCurrentBtn.setOnClickListener {
            if (filePath.isEmpty()) {
                Toast.makeText(requireContext(), "No file uploaded yet.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                // Clean and normalize
                val cleanPath = filePath
                    .replace("\\\\".toRegex(), "/")
                    .replace("//", "/")
                    .removePrefix("/")

                // Build URL
                val fullUrl =
                    if (filePath.startsWith("http"))
                        filePath
                    else
                        ApiClient.getBaseUrl().trimEnd('/') + "/" + cleanPath

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Unable to open file", Toast.LENGTH_SHORT).show()
            }
        }

        // --- REVISION HISTORY LIST ---
        revisionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        revisionsRecyclerView.adapter = ClientRevisionHistoryAdapter(revisionHistory)

        return v
    }
}

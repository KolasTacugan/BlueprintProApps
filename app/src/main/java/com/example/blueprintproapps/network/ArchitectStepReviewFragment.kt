package com.example.blueprintproapps.network

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ArchitectRevisionHistoryAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectApiResponse
import com.example.blueprintproapps.models.ArchitectProjectFileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectStepReviewFragment : Fragment() {

    private lateinit var nextBtn: Button
    private lateinit var uploadBtn: Button
    private lateinit var viewPager: ViewPager2
    private lateinit var revisionsRecyclerView: RecyclerView
    private lateinit var currentRevisionName: TextView

    private var projectId: String? = null
    private var currentStatus: String? = null
    private var blueprintId: Int = 0
    private var currentFilePath: String? = null
    private var revisionHistory: ArrayList<ArchitectProjectFileResponse> = arrayListOf()

    companion object {
        fun newInstance(
            projectId: String,
            status: String,
            blueprintId: Int,
            currentFilePath: String?,
            revisionHistory: ArrayList<ArchitectProjectFileResponse>
        ): ArchitectStepReviewFragment {
            val fragment = ArchitectStepReviewFragment()
            val args = Bundle()
            args.putString("projectId", projectId)
            args.putString("status", status)
            args.putInt("blueprintId", blueprintId)
            args.putString("currentFilePath", currentFilePath)
            args.putParcelableArrayList("revisionHistory", revisionHistory)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_architect_step_review, container, false)

        nextBtn = view.findViewById(R.id.nextBtn)
        uploadBtn = view.findViewById(R.id.uploadRevisionBtn)
        revisionsRecyclerView = view.findViewById(R.id.revisionsRecyclerView)
        currentRevisionName = view.findViewById(R.id.currentRevisionName)
        viewPager = requireActivity().findViewById(R.id.stepViewPager)

        projectId = arguments?.getString("projectId")
        currentStatus = arguments?.getString("status")
        blueprintId = arguments?.getInt("blueprintId") ?: 0
        currentFilePath = arguments?.getString("currentFilePath")
        revisionHistory = arguments?.getParcelableArrayList("revisionHistory") ?: arrayListOf()

        // ✅ Display current file like web
        if (!currentFilePath.isNullOrEmpty()) {
            currentRevisionName.text = "Current_Version"
            currentRevisionName.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentFilePath))
                startActivity(intent)
            }
        } else {
            currentRevisionName.text = "No file uploaded yet."
        }

        // ✅ Display revision list like web
        revisionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ArchitectRevisionHistoryAdapter(revisionHistory)
        revisionsRecyclerView.adapter = adapter

        // ✅ Hide upload/next if not in Review
        if (currentStatus != "Review") {
            uploadBtn.visibility = View.GONE
            nextBtn.visibility = View.GONE
        }

        nextBtn.setOnClickListener {
            if (!projectId.isNullOrEmpty()) {
                updateStep("Compliance", 1)
            }
        }

        return view
    }

    private fun updateStep(newStatus: String, nextPage: Int) {
        ApiClient.instance.updateProjectStatus(projectId!!, newStatus)
            .enqueue(object : Callback<ArchitectApiResponse> {
                override fun onResponse(
                    call: Call<ArchitectApiResponse>,
                    response: Response<ArchitectApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(context, "Moved to $newStatus", Toast.LENGTH_SHORT).show()
                        viewPager.currentItem = nextPage
                    } else {
                        Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ArchitectApiResponse>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

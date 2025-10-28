package com.example.blueprintproapps.network

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectStepFinalizationFragment : Fragment() {

    private lateinit var finalizeBtn: Button
    private var projectId: String? = null
    private var currentStatus: String? = null

    companion object {
        fun newInstance(projectId: String, status: String): ArchitectStepFinalizationFragment {
            val fragment = ArchitectStepFinalizationFragment()
            val args = Bundle()
            args.putString("projectId", projectId)
            args.putString("status", status)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_architect_step_finalization, container, false)
        finalizeBtn = view.findViewById(R.id.finalizeProjectBtn)

        projectId = arguments?.getString("projectId")
        currentStatus = arguments?.getString("status")

        if (currentStatus != "Finalization") {
            finalizeBtn.isEnabled = false
        }

        finalizeBtn.setOnClickListener {
            updateStep("Completed")
        }

        return view
    }

    private fun updateStep(newStatus: String) {
        ApiClient.instance.updateProjectStatus(projectId!!, newStatus)
            .enqueue(object : Callback<ArchitectApiResponse> {
                override fun onResponse(
                    call: Call<ArchitectApiResponse>,
                    response: Response<ArchitectApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(context, "Project $newStatus!", Toast.LENGTH_SHORT).show()
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

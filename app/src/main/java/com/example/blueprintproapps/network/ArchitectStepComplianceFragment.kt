package com.example.blueprintproapps.network

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectStepComplianceFragment : Fragment() {

    private lateinit var nextBtn: Button
    private lateinit var uploadBtn: Button
    private lateinit var fileTypeGroup: RadioGroup
    private lateinit var viewPager: ViewPager2

    private var projectId: String? = null
    private var currentStatus: String? = null

    companion object {
        fun newInstance(projectId: String, status: String): ArchitectStepComplianceFragment {
            val fragment = ArchitectStepComplianceFragment()
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
        val view = inflater.inflate(R.layout.fragment_architect_step_compliance, container, false)

        nextBtn = view.findViewById(R.id.nextBtn)
        uploadBtn = view.findViewById(R.id.uploadComplianceBtn)
        fileTypeGroup = view.findViewById(R.id.fileTypeGroup)
        viewPager = requireActivity().findViewById(R.id.stepViewPager)

        projectId = arguments?.getString("projectId")
        currentStatus = arguments?.getString("status")

        // Disable upload if not current step
        if (currentStatus != "Compliance") {
            uploadBtn.isEnabled = false
            nextBtn.isEnabled = false
        }

        // Handle next step
        nextBtn.setOnClickListener {
            updateStep("Finalization", 2)
        }

        // Radio button logic
        fileTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedType = if (checkedId == R.id.zoningRadio) "Zoning" else "Others"
            Toast.makeText(context, "$selectedType selected", Toast.LENGTH_SHORT).show()
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

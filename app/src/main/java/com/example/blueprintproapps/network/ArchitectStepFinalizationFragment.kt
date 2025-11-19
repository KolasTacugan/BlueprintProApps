package com.example.blueprintproapps.network

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectApiResponse
import com.example.blueprintproapps.models.ArchitectProjectTrackerResponse
import com.example.blueprintproapps.models.ProjectTrackerResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchitectStepFinalizationFragment : Fragment() {

    private lateinit var saveNotesBtn: Button
    private lateinit var finalizeBtn: Button
    private lateinit var finalBlueprintName: TextView
    private lateinit var notesEditText: EditText
    private var projectId: String? = null
    private var blueprintId: Int = 0
    private var projectTrackId: Int = 0
    private var currentStatus: String? = null
    private var finalBlueprintUrl: String? = null

    companion object {
        fun newInstance(
            projectId: String,
            blueprintId: Int,
            projectTrackId: Int,
            status: String,
            finalBlueprintUrl: String?,
            projectStatus: String?
        ): ArchitectStepFinalizationFragment {
            val fragment = ArchitectStepFinalizationFragment()
            val args = Bundle()
            args.putString("projectId", projectId)
            args.putInt("blueprintId", blueprintId)
            args.putInt("projectTrackId", projectTrackId)
            args.putString("status", status)
            args.putString("finalBlueprintUrl", finalBlueprintUrl)
            args.putString("projectStatus", projectStatus)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_architect_step_finalization, container, false)

        saveNotesBtn = view.findViewById(R.id.saveNotesBtn)
        finalizeBtn = view.findViewById(R.id.finalizeProjectBtn)
        finalBlueprintName = view.findViewById(R.id.finalFileNameText)
        notesEditText = view.findViewById(R.id.finalizationNotesEditText)

        projectId = arguments?.getString("projectId")
        blueprintId = arguments?.getInt("blueprintId") ?: 0
        projectTrackId = arguments?.getInt("projectTrackId") ?: 0
        currentStatus = arguments?.getString("status")
        finalBlueprintUrl = arguments?.getString("finalBlueprintUrl")

        // ✅ Clickable "Final Blueprint" link
        if (!finalBlueprintUrl.isNullOrEmpty()) {
            finalBlueprintName.text = "Final_Blueprint.pdf"
            finalBlueprintName.setOnClickListener {
                val uri = Uri.parse(finalBlueprintUrl)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        } else {
            finalBlueprintName.text = "No Final Blueprint available."
            finalBlueprintName.setTextColor(resources.getColor(android.R.color.darker_gray))
        }

        // ✅ Disable buttons if not in Finalization stage
        if (currentStatus != "Finalization") {
            saveNotesBtn.visibility = View.GONE
            finalizeBtn.visibility = View.GONE
            notesEditText.isEnabled = false
        }

        // ✅ Fetch and display notes from server
        fetchFinalizationNotes()

        // ✅ Save Notes Button
        saveNotesBtn.setOnClickListener {
            val notes = notesEditText.text.toString().trim()
            if (notes.isEmpty()) {
                Toast.makeText(requireContext(), "Please write some notes first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ApiClient.instance.saveFinalizationNotes(projectTrackId, notes)
                .enqueue(object : Callback<ArchitectApiResponse> {
                    override fun onResponse(
                        call: Call<ArchitectApiResponse>,
                        response: Response<ArchitectApiResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(requireContext(), "Notes saved successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to save notes.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ArchitectApiResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // ✅ Finish Project Button
        finalizeBtn.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Finalize Project?")
                .setMessage(
                    "Before finalizing, please double-check all steps (Review, Compliance, Finalization). " +
                            "Once completed, this project will be marked as *Finished* and can no longer be modified.\n\n" +
                            "Are you sure you want to continue?"
                )
                .setPositiveButton("Yes, Finish Project") { _, _ ->
                    finalizeProject()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val projectStatus = arguments?.getString("projectStatus") ?: ""
        if (projectStatus == "Finished") {
            saveNotesBtn.visibility = View.GONE
            finalizeBtn.visibility = View.GONE
            notesEditText.isEnabled = false
        }

        return view
    }

    private fun fetchFinalizationNotes() {
        // make sure blueprintId is available (set earlier from arguments)
        ApiClient.instance.getArchitectProjectTracker(blueprintId)
            .enqueue(object : Callback<ArchitectProjectTrackerResponse> {
                override fun onResponse(
                    call: Call<ArchitectProjectTrackerResponse>,
                    response: Response<ArchitectProjectTrackerResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val tracker = response.body()!!
                        // notesEditText must be initialized (findViewById) before calling this
                        notesEditText.setText(tracker.finalizationNotes ?: "")
                    } else {
                        Toast.makeText(requireContext(), "Failed to load notes: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ArchitectProjectTrackerResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed to load notes: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun finalizeProject() {
        if (projectId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Missing project ID", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.finalizeProject(projectId!!)
            .enqueue(object : Callback<ArchitectApiResponse> {
                override fun onResponse(
                    call: Call<ArchitectApiResponse>,
                    response: Response<ArchitectApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "🎉 Project finalized successfully!", Toast.LENGTH_LONG).show()
                        finalizeBtn.visibility = View.GONE
                        saveNotesBtn.visibility = View.GONE
                        notesEditText.isEnabled = false
                    } else {
                        Toast.makeText(requireContext(), "Failed to finalize project.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ArchitectApiResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

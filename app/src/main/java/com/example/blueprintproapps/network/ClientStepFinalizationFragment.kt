package com.example.blueprintproapps.network

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ClientApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientStepFinalizationFragment : Fragment() {

    private lateinit var finalFileText: TextView
    private lateinit var notesText: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var submitRatingBtn: Button

    private var projectId: String = ""
    private var projectStatus: String = ""
    private var isRated: Boolean = false
    private var finalBlueprintUrl: String? = null
    private var finalizationNotes: String = ""

    companion object {
        fun newInstance(
            finalizationNotes: String,
            projectStatus: String,
            isRated: Boolean,
            projectId: String,
            finalBlueprintUrl: String?
        ): ClientStepFinalizationFragment {

            val f = ClientStepFinalizationFragment()
            val args = Bundle()

            args.putString("projectId", projectId)
            args.putString("projectStatus", projectStatus)
            args.putBoolean("isRated", isRated)
            args.putString("finalBlueprintUrl", finalBlueprintUrl)
            args.putString("finalizationNotes", finalizationNotes)

            f.arguments = args
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_client_step_finalization, container, false)

        finalFileText = v.findViewById(R.id.finalFileNameText)
        notesText = v.findViewById(R.id.finalizationNotesText)
        ratingBar = v.findViewById(R.id.ratingBar)
        submitRatingBtn = v.findViewById(R.id.submitRatingBtn)

        projectId = arguments?.getString("projectId") ?: ""
        projectStatus = arguments?.getString("projectStatus") ?: ""
        isRated = arguments?.getBoolean("isRated") ?: false
        finalBlueprintUrl = arguments?.getString("finalBlueprintUrl")
        finalizationNotes = arguments?.getString("finalizationNotes") ?: ""

        // ============= FINAL BLUEPRINT LINK ====================
        if (!finalBlueprintUrl.isNullOrEmpty()) {
            finalFileText.text = "Final_Blueprint"

            val openUrl =
                if (finalBlueprintUrl!!.startsWith("http"))
                    finalBlueprintUrl
                else
                    ApiClient.getBaseUrl() + finalBlueprintUrl!!

            finalFileText.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(openUrl)))
            }

        } else {
            finalFileText.text = "No final file uploaded yet."
        }

        // ============= NOTES ====================
        notesText.text = finalizationNotes

        // ============= RATING VISIBILITY ====================
        if (projectStatus == "Finished" && !isRated) {
            ratingBar.visibility = View.VISIBLE
            submitRatingBtn.visibility = View.VISIBLE
        } else {
            ratingBar.visibility = View.GONE
            submitRatingBtn.visibility = View.GONE
        }

        // ============= SUBMIT RATING ====================
        submitRatingBtn.setOnClickListener {

            val rating = ratingBar.rating.toInt()

            if (projectId.isBlank()) {
                Toast.makeText(requireContext(), "Missing project ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ApiClient.instance.submitRating(projectId, rating)
                .enqueue(object : Callback<ClientApiResponse> {
                    override fun onResponse(
                        call: Call<ClientApiResponse>,
                        response: Response<ClientApiResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(requireContext(), "Thanks for the rating!", Toast.LENGTH_SHORT).show()

                            ratingBar.visibility = View.GONE
                            submitRatingBtn.visibility = View.GONE
                        } else {
                            Toast.makeText(requireContext(), "Failed to submit rating.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ClientApiResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        return v
    }
}

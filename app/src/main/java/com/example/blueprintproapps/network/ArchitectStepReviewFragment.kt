package com.example.blueprintproapps.network

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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
import com.example.blueprintproapps.models.ArchitectProjectTrackerResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.AlertDialog

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
    private val PICK_FILE_REQUEST_CODE = 101
    private var selectedFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blueprintId = arguments?.getInt("blueprintId") ?: 0
    }

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
        uploadBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // allow any file type, or use "application/pdf"
            startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST_CODE)
        }

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
                AlertDialog.Builder(requireContext())
                    .setTitle("Proceed to Next Step")
                    .setMessage(
                        "Are you sure you want to proceed to the next step?\n\n" +
                                "⚠️ This action cannot be reverted.\n" +
                                "Please double-check all information in this step before continuing."
                    )
                    .setPositiveButton("Proceed") { _, _ ->
                        updateStep(projectId!!, "Compliance")
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Missing project ID", Toast.LENGTH_SHORT).show()
            }
        }

        val openCurrentBtn = view.findViewById<Button>(R.id.openCurrentBtn)
        openCurrentBtn.setOnClickListener {
            val filePath = arguments?.getString("currentFilePath")

            if (filePath.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No file to open.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                // ✅ Same logic as the revision adapter
                val uri = if (!filePath.startsWith("http")) {
                    Uri.parse("${ApiClient.getBaseUrl()}$filePath")
                } else {
                    Uri.parse(filePath)
                }

                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Unable to open file", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && data != null && data.data != null) {
            selectedFileUri = data.data
            Toast.makeText(requireContext(), "File selected: ${selectedFileUri?.lastPathSegment}", Toast.LENGTH_SHORT).show()
            uploadFile()
        }
    }

    private fun uploadFile() {
        if (selectedFileUri == null || projectId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No file selected or project missing", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val contentResolver = requireContext().contentResolver

            // 🧩 Read file bytes from the selected URI
            val inputStream = contentResolver.openInputStream(selectedFileUri!!)
            val fileBytes = inputStream?.readBytes()
            inputStream?.close()

            if (fileBytes == null) {
                Toast.makeText(requireContext(), "Failed to read file", Toast.LENGTH_SHORT).show()
                return
            }

            // 🧩 Detect original MIME type (e.g., image/png, application/pdf, etc.)
            val mimeType = contentResolver.getType(selectedFileUri!!) ?: "application/octet-stream"

            // 🧩 Get the original file name (keeps original extension)
            val fileName = getFileNameFromUri(selectedFileUri!!)

            // 🧱 Create request body for the file
            val requestFile = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())

            // 🧱 Match backend param name: "file"
            val body = MultipartBody.Part.createFormData(
                "file",  // must match IFormFile parameter name in C#
                fileName, // ✅ use original filename and extension
                requestFile
            )

            // 🧱 Prepare text field for projectId
            val projectIdPart = projectId!!.toRequestBody("text/plain".toMediaTypeOrNull())

            // 🚀 Make API call
            ApiClient.instance.uploadProjectFile(projectIdPart, body)
                .enqueue(object : Callback<ArchitectApiResponse> {
                    override fun onResponse(
                        call: Call<ArchitectApiResponse>,
                        response: Response<ArchitectApiResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(requireContext(), "Revision uploaded successfully!", Toast.LENGTH_SHORT).show()
                            reloadRevisionList()
                        } else {
                            Log.e("Upload", "Server response: ${response.code()} - ${response.message()}")
                            Toast.makeText(requireContext(), "Upload failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ArchitectApiResponse>, t: Throwable) {
                        Log.e("Upload", "Error uploading file", t)
                        Toast.makeText(requireContext(), "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })

        } catch (e: Exception) {
            Log.e("Upload", "Exception while uploading", e)
            Toast.makeText(requireContext(), "Failed to upload file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reloadRevisionList() {
        ApiClient.instance.getArchitectProjectTracker(blueprintId)
            .enqueue(object : Callback<ArchitectProjectTrackerResponse> {
                override fun onResponse(
                    call: Call<ArchitectProjectTrackerResponse>,
                    response: Response<ArchitectProjectTrackerResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val newRevisions = response.body()!!.revisionHistory
                        revisionHistory.clear()
                        revisionHistory.addAll(newRevisions)
                        revisionsRecyclerView.adapter?.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<ArchitectProjectTrackerResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed to reload revisions", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "file_${System.currentTimeMillis()}"
    }

    private fun updateStep(projectId: String, status: String) {
        ApiClient.instance.updateProjectStatus(projectId, status)
            .enqueue(object : Callback<ArchitectApiResponse> {
                override fun onResponse(
                    call: Call<ArchitectApiResponse>,
                    response: Response<ArchitectApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "Step updated successfully!", Toast.LENGTH_SHORT).show()

                        // ✅ Reload the latest project tracker data
                        reloadRevisionList()

                        // ✅ Also refresh the parent activity (status, tabs, etc.)
                        (activity as? ArchitectProjectTrackerActivity)?.reloadTrackerData()

                        // ✅ Automatically move to next tab (Compliance)
                        viewPager.setCurrentItem(1, true) // index 1 = Compliance fragment (adjust if different)
                    } else {
                        Toast.makeText(requireContext(), "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ArchitectApiResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


}

package com.example.blueprintproapps.network

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectApiResponse
import com.example.blueprintproapps.models.ArchitectProjectTrackerResponse
import com.example.blueprintproapps.models.ProjectTrackerResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class ArchitectStepComplianceFragment : Fragment() {

    private lateinit var nextBtn: Button
    private lateinit var uploadBtn: Button
    private lateinit var fileTypeGroup: RadioGroup
    private lateinit var viewPager: ViewPager2
    private lateinit var zoningContainer: LinearLayout
    private lateinit var otherContainer: LinearLayout
    private var projectTrackId: String? = null
    private var currentStatus: String? = null
    private var projectId: String? = null

    companion object {
        private const val FILE_PICK_REQUEST = 1001

        fun newInstance(projectTrackId: String, blueprintId: Int, status: String, projectId: String): ArchitectStepComplianceFragment {
            val fragment = ArchitectStepComplianceFragment()
            val args = Bundle()
            args.putString("projectTrackId", projectTrackId)
            args.putInt("blueprintId", blueprintId)
            args.putString("status", status)
            args.putString("projectId", projectId)
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
        zoningContainer = view.findViewById(R.id.zoningFilesContainer)
        otherContainer = view.findViewById(R.id.otherFilesContainer)

        projectId = arguments?.getString("projectId")
        projectTrackId = arguments?.getString("projectTrackId")
        currentStatus = arguments?.getString("status")

        // Disable buttons if not the current step
        if (currentStatus != "Compliance") {
            uploadBtn.visibility = View.GONE
            nextBtn.visibility = View.GONE
        }

        uploadBtn.setOnClickListener {
            pickFile()
        }

        nextBtn.setOnClickListener {
            if (!projectId.isNullOrEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Proceed to Next Step")
                    .setMessage(
                        "Are you sure you want to proceed to the next step?\n\n" +
                                "⚠️ This action cannot be reverted.\n" +
                                "Please double-check all compliance files before continuing."
                    )
                    .setPositiveButton("Proceed") { _, _ ->
                        ApiClient.instance.updateProjectStatus(projectId!!, "Finalization")
                            .enqueue(object : Callback<ArchitectApiResponse> {
                                override fun onResponse(
                                    call: Call<ArchitectApiResponse>,
                                    response: Response<ArchitectApiResponse>
                                ) {
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        Toast.makeText(requireContext(), "Step updated successfully!", Toast.LENGTH_SHORT).show()

                                        // ✅ Reload this fragment's compliance data
                                        fetchProjectTrackerData()

                                        // ✅ Ask parent activity to refresh main tracker data (tabs/status bar)
                                        (activity as? ArchitectProjectTrackerActivity)?.reloadTrackerData()
                                    } else {
                                        Toast.makeText(requireContext(), "Failed: ${response.body()?.message ?: response.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<ArchitectApiResponse>, t: Throwable) {
                                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Missing project ID", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchProjectTrackerData()
    }

    private fun fetchProjectTrackerData() {
        val projectTrackId = arguments?.getString("projectTrackId") ?: return
        val blueprintId = arguments?.getInt("blueprintId") ?: return

        Log.d("ComplianceFragment", "🔹 Fetching tracker for blueprintId=$blueprintId trackId=$projectTrackId")

        ApiClient.instance.getArchitectProjectTracker(blueprintId)
            .enqueue(object : Callback<ArchitectProjectTrackerResponse> {
                override fun onResponse(
                    call: Call<ArchitectProjectTrackerResponse>,
                    response: Response<ArchitectProjectTrackerResponse>
                ) {
                    Log.d("ComplianceFragment", "✅ API response: ${response.body()}")
                    if (response.isSuccessful) {
                        val tracker = response.body()
                        zoningContainer.removeAllViews()
                        otherContainer.removeAllViews()

                        tracker?.compliance?.let { compliance ->
                            if (!compliance.zoningFile.isNullOrEmpty()) {
                                val zoningLink = createFileLinkUI("Zoning File", compliance.zoningFile!!)
                                zoningContainer.addView(zoningLink)
                            }
                            if (!compliance.othersFile.isNullOrEmpty()) {
                                val otherLink = createFileLinkUI("Others File", compliance.othersFile!!)
                                otherContainer.addView(otherLink)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ArchitectProjectTrackerResponse>, t: Throwable) {
                    Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ✅ Launch file picker
    private fun pickFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_PICK_REQUEST)
    }

    // ✅ Handle file picker result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICK_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                uploadComplianceFile(uri)
            }
        }
    }

    // ✅ Upload compliance file to backend
    private fun uploadComplianceFile(uri: Uri) {
        val fileType =
            if (fileTypeGroup.checkedRadioButtonId == R.id.zoningRadio) "Zoning" else "Others"

        val pfd = requireContext().contentResolver.openFileDescriptor(uri, "r", null) ?: return
        val file = File(requireContext().cacheDir, getFileName(uri))
        FileInputStream(pfd.fileDescriptor).use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }

        val safeProjectTrackId = projectTrackId ?: return

        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val trackIdBody = safeProjectTrackId.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileTypeBody = fileType.toRequestBody("text/plain".toMediaTypeOrNull())

        ApiClient.instance.uploadComplianceFile(
            trackIdBody,
            fileTypeBody,
            multipartBody
        )
            .enqueue(object : Callback<ArchitectApiResponse> {
                override fun onResponse(
                    call: Call<ArchitectApiResponse>,
                    response: Response<ArchitectApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(context, "File uploaded successfully", Toast.LENGTH_SHORT).show()
                        fetchProjectTrackerData() // ✅ refresh list after upload
                    } else {
                        Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ArchitectApiResponse>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // ✅ Get clean file name from URI
    private fun getFileName(uri: Uri): String {
        var name = "unknown_file"
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) {
                name = it.getString(nameIndex)
            }
        }
        return name
    }

    // ✅ Clean, readable clickable link
    private fun createFileLinkUI(label: String, fileName: String): View {
        val textView = TextView(context)
        textView.text = label
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // ✅ Fix: prepend full URL path
        val fileUrl = "http://10.0.2.2:5169/uploads/compliance/$fileName"

        textView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
            startActivity(intent)
        }

        return textView
    }
}

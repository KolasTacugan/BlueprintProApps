package com.example.blueprintproapps.network

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectApiResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    companion object {
        private const val FILE_PICK_REQUEST = 1001

        fun newInstance(projectTrackId: String, status: String): ArchitectStepComplianceFragment {
            val fragment = ArchitectStepComplianceFragment()
            val args = Bundle()
            args.putString("projectTrackId", projectTrackId)
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
        zoningContainer = view.findViewById(R.id.zoningFilesContainer)
        otherContainer = view.findViewById(R.id.otherFilesContainer)

        projectTrackId = arguments?.getString("projectTrackId")
        currentStatus = arguments?.getString("status")

        // Disable buttons if not the current step
        if (currentStatus != "Compliance") {
            uploadBtn.isEnabled = false
            nextBtn.isEnabled = false
        }

        uploadBtn.setOnClickListener {
            pickFile()
        }

        nextBtn.setOnClickListener {
            showNextStepDialog()
        }

        return view
    }

    // ✅ Step confirmation dialog before proceeding
    private fun showNextStepDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Proceed")
            .setMessage("Are you sure to proceed to next step? This action can't be reverted. Please check the information carefully.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Proceed") { _, _ ->
                updateStep("Finalization", 2)
            }
            .show()
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

    // ✅ Upload compliance file using your API definition
    private fun uploadComplianceFile(uri: Uri) {
        val fileType =
            if (fileTypeGroup.checkedRadioButtonId == R.id.zoningRadio) "Zoning" else "Others"

        val pfd = requireContext().contentResolver.openFileDescriptor(uri, "r", null) ?: return
        val file = File(requireContext().cacheDir, getFileName(uri))
        FileInputStream(pfd.fileDescriptor).use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }

        val safeProjectTrackId = projectTrackId ?: return

        // ✅ Create RequestBodies
        val trackIdBody = safeProjectTrackId.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileTypeBody = fileType.toRequestBody("text/plain".toMediaTypeOrNull())

        // ✅ Create MultipartBody.Part for the file
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

        // ✅ Call the API
        ApiClient.instance.uploadComplianceFile(trackIdBody, fileTypeBody, filePart)
            .enqueue(object : Callback<ArchitectApiResponse> {
                override fun onResponse(
                    call: Call<ArchitectApiResponse>,
                    response: Response<ArchitectApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(context, "File uploaded successfully", Toast.LENGTH_SHORT).show()

                        // ✅ Show clickable link to file
                        val link = createFileLink(file.name)
                        if (fileType == "Zoning") zoningContainer.addView(link)
                        else otherContainer.addView(link)
                    } else {
                        Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ArchitectApiResponse>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    // ✅ Helper: Display uploaded filename in UI
    private fun addFileToList(fileName: String, fileType: String) {
        val textView = TextView(context)
        textView.text = fileName
        textView.setPadding(8, 4, 8, 4)
        if (fileType == "Zoning") zoningContainer.addView(textView)
        else otherContainer.addView(textView)
    }

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

    private fun createFileLink(fileName: String): TextView {
        return TextView(requireContext()).apply {
            text = fileName
            setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark)) // use a link color
            setPadding(8, 8, 8, 8)
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("${ApiClient.getBaseUrl()}/wwwroot/uploads/compliance/$fileName")
                startActivity(intent)
            }
        }
    }

    private fun updateStep(newStatus: String, nextPage: Int) {
        ApiClient.instance.updateProjectStatus(projectTrackId!!, newStatus)
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
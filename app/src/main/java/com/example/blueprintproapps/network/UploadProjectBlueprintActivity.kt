package com.example.blueprintproapps.network

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.api.ApiService
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.MatchedClientResponse
import com.example.blueprintproapps.models.UploadProjectBlueprintResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Calendar

class UploadProjectBlueprintActivity : AppCompatActivity() {

    private lateinit var blueprintImageView: ImageView
    private lateinit var selectImageBtn: Button
    private lateinit var projectNameInput: EditText
    private lateinit var budgetInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var dueDateBtn: Button
    private lateinit var clientDropdown: Spinner
    private lateinit var uploadBtn: Button

    private var imageUri: Uri? = null
    private var dueDateValue: String = ""
    private var selectedClientId: String = ""
    private lateinit var architectId: String

    private val apiService: ApiService = ApiClient.instance
    private val clientList = mutableListOf<MatchedClientResponse>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            imageUri = it
            blueprintImageView.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = AuthSessionManager.requireSession(this, UserRole.ARCHITECT) ?: return
        architectId = session.userId
        setContentView(R.layout.activity_upload_project_blueprint)

        blueprintImageView = findViewById(R.id.blueprintImageView)
        selectImageBtn = findViewById(R.id.selectImageBtn)
        projectNameInput = findViewById(R.id.projectNameInput)
        budgetInput = findViewById(R.id.budgetInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        dueDateBtn = findViewById(R.id.dueDateBtn)
        clientDropdown = findViewById(R.id.clientDropdown)
        uploadBtn = findViewById(R.id.uploadBtn)

        selectImageBtn.setOnClickListener { pickImageLauncher.launch("image/*") }
        dueDateBtn.setOnClickListener { showDatePicker() }

        fetchClients()

        uploadBtn.setOnClickListener {
            uploadProjectBlueprint()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                // ✅ Standard ISO format with leading zeros (YYYY-MM-DD)
                val date = String.format("%04d-%02d-%02d", year, month + 1, day)
                dueDateValue = date
                dueDateBtn.text = date
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun fetchClients() {
        Log.d("CLIENT_LIST", "Architect ID used in API call: $architectId")

        val call = apiService.getClientsForProject(architectId)
        call.enqueue(object : Callback<List<MatchedClientResponse>> {
            override fun onResponse(
                call: Call<List<MatchedClientResponse>>,
                response: Response<List<MatchedClientResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    clientList.clear()
                    clientList.addAll(response.body()!!)
                    val names = clientList.map { it.clientName }
                    val adapter = ArrayAdapter(
                        this@UploadProjectBlueprintActivity,
                        android.R.layout.simple_spinner_item,
                        names
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    clientDropdown.adapter = adapter

                    clientDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: android.view.View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedClientId = clientList[position].clientId
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                } else {
                    Log.e("CLIENT_LIST", "Failed with code: ${response.code()}")
                    Toast.makeText(this@UploadProjectBlueprintActivity, "Failed to load clients", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MatchedClientResponse>>, t: Throwable) {
                Toast.makeText(this@UploadProjectBlueprintActivity, "Failed to load clients", Toast.LENGTH_SHORT).show()
                Log.e("CLIENT_LIST", "Error: ${t.message}")
            }
        })
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream from URI")
        val tempFile = File.createTempFile("upload_", ".tmp", cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun uploadProjectBlueprint() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        if (dueDateValue.isEmpty()) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedClientId.isEmpty()) {
            Toast.makeText(this, "Please select a client", Toast.LENGTH_SHORT).show()
            return
        }

        val file = uriToFile(imageUri!!)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("BlueprintImage", file.name, requestFile)

        // ✅ Sanitize budget: Remove non-numeric characters (like ₱ or commas)
        val rawBudget = budgetInput.text.toString().replace(Regex("[^0-9]"), "")
        if (rawBudget.isEmpty()) {
            Toast.makeText(this, "Please enter a valid budget", Toast.LENGTH_SHORT).show()
            return
        }

        val projectName = projectNameInput.text.toString().trim().toRequestBody("text/plain".toMediaType())
        val budget = rawBudget.toRequestBody("text/plain".toMediaType())
        val description = descriptionInput.text.toString().trim().toRequestBody("text/plain".toMediaType())
        val clientId = selectedClientId.toRequestBody("text/plain".toMediaType())
        val dueDate = dueDateValue.toRequestBody("text/plain".toMediaType())
        val architectId = this.architectId.toRequestBody("text/plain".toMediaType())

        apiService.uploadProjectBlueprint(
            body,
            projectName,
            budget,
            description,
            clientId,
            dueDate,
            architectId
        ).enqueue(object : Callback<UploadProjectBlueprintResponse> {
            override fun onResponse(
                call: Call<UploadProjectBlueprintResponse>,
                response: Response<UploadProjectBlueprintResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UploadProjectBlueprintActivity, "Project uploaded successfully", Toast.LENGTH_SHORT).show()
                    file.delete()
                    finish()
                } else {
                    val errorHtml = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("UPLOAD_FAILED", "Server Error (${response.code()}): $errorHtml")
                    Toast.makeText(this@UploadProjectBlueprintActivity, "Upload failed. Check logs.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UploadProjectBlueprintResponse>, t: Throwable) {
                Toast.makeText(this@UploadProjectBlueprintActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

package com.example.blueprintproapps.network

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.EditProfileResponse
import com.example.blueprintproapps.models.GetEditProfileResponse
import com.example.blueprintproapps.utils.FileUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var btnChangePhoto: Button
    private lateinit var btnUploadPdf: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnSaveChanges: Button

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText

    private var selectedImageFile: File? = null
    private var selectedPdfFile: File? = null

    private val PICK_IMAGE = 1001
    private val PICK_PDF = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        initViews()
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userType = prefs.getString("userType", null)

        if (userType == "Client") {
            btnUploadPdf.isEnabled = false
            btnUploadPdf.alpha = 0.5f
        } else {
            btnUploadPdf.isEnabled = true
            btnUploadPdf.alpha = 1f
        }
        loadExistingProfile()
        setupClicks()
    }

    private fun initViews() {
        imgProfile = findViewById(R.id.imgProfile)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        btnUploadPdf = findViewById(R.id.btnUploadPdf)
        btnBack = findViewById(R.id.btnBack)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)

        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
    }

    private fun loadExistingProfile() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        val userType = prefs.getString("userType", null)

        val userId = when (userType) {
            "Architect" -> prefs.getString("architectId", null)
            "Client" -> prefs.getString("clientId", null)
            else -> null
        }

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.getEditProfile(userId)
            .enqueue(object : Callback<GetEditProfileResponse> {
                override fun onResponse(
                    call: Call<GetEditProfileResponse>,
                    response: Response<GetEditProfileResponse>
                ) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val data = response.body()?.data ?: return

                    // ✅ Set UI fields
                    etFirstName.setText(data.firstName)
                    etLastName.setText(data.lastName)
                    etEmail.setText(data.email)
                    etPhone.setText(data.phoneNumber)

                    // ✅ Load profile image
                    if (!data.profilePhoto.isNullOrEmpty()) {
                        Glide.with(this@EditProfileActivity)
                            .load(data.profilePhoto)
                            .placeholder(R.drawable.ic_user_placeholder)
                            .into(imgProfile)
                    }

                    // ✅ Save to shared prefs
                    val editor = prefs.edit()
                    editor.putString("firstName", data.firstName)
                    editor.putString("lastName", data.lastName)
                    editor.putString("email", data.email)
                    editor.putString("phone", data.phoneNumber)
                    editor.putString("profilePhoto", data.profilePhoto)
                    editor.apply()
                }

                override fun onFailure(call: Call<GetEditProfileResponse>, t: Throwable) {
                    Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun setupClicks() {
        btnBack.setOnClickListener { finish() }

        btnChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        btnUploadPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, PICK_PDF)
        }

        btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri? = data.data
            if (uri != null) {
                when (requestCode) {
                    PICK_IMAGE -> {
                        selectedImageFile = FileUtil.from(this, uri)
                        Glide.with(this).load(uri).into(imgProfile)
                    }
                    PICK_PDF -> {
                        selectedPdfFile = FileUtil.from(this, uri)
                        Toast.makeText(this, "PDF Selected", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveChanges() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        val userType = prefs.getString("userType", null)

        val userId = when (userType) {
            "Architect" -> prefs.getString("architectId", null)
            "Client" -> prefs.getString("clientId", null)
            else -> null
        }

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val rbUserId = userId.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbFirst = etFirstName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val rbLast = etLastName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val rbEmail = etEmail.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val rbPhone = etPhone.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = selectedImageFile?.let {
            MultipartBody.Part.createFormData(
                "ProfilePhoto",
                it.name,
                it.asRequestBody("image/*".toMediaTypeOrNull())
            )
        }

        val pdfPart = selectedPdfFile?.let {
            MultipartBody.Part.createFormData(
                "CredentialsFile",
                it.name,
                it.asRequestBody("application/pdf".toMediaTypeOrNull())
            )
        }

        ApiClient.instance.editProfile(
            rbUserId,
            rbFirst,
            rbLast,
            rbEmail,
            rbPhone,
            imagePart,
            pdfPart
        ).enqueue(object : Callback<EditProfileResponse> {
            override fun onResponse(
                call: Call<EditProfileResponse>,
                response: Response<EditProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()?.data

                    // ✅ Save updated profile locally
                    val editor = prefs.edit()
                    editor.putString("firstName", data?.user_fname)
                    editor.putString("lastName", data?.user_lname)
                    editor.putString("email", data?.email)
                    editor.putString("phone", data?.phoneNumber)
                    editor.putString("profilePhoto", data?.user_profilePhoto)
                    editor.apply()

                    Toast.makeText(this@EditProfileActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<EditProfileResponse>, t: Throwable) {
                Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}

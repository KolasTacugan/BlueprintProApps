package com.example.blueprintproapps.network

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.EditProfileResponse
import com.example.blueprintproapps.models.GetEditProfileResponse
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
    private lateinit var layoutCredentials: LinearLayout
    private lateinit var tvUploadedFile: TextView

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText

    private var selectedImageFile: File? = null
    private var selectedPdfFile: File? = null
    private lateinit var userId: String
    private lateinit var userRole: UserRole

    private val PICK_IMAGE = 1001
    private val PICK_PDF = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = AuthSessionManager.requireSession(this) ?: return
        userId = session.userId
        userRole = session.role
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        initViews()
        layoutCredentials.visibility = if (userRole == UserRole.ARCHITECT) View.VISIBLE else View.GONE
        btnUploadPdf.isEnabled = userRole == UserRole.ARCHITECT
        loadExistingProfile()
        setupClicks()
        
        // Iconify Icons
        com.example.blueprintproapps.utils.UiEffects.applyIconify(btnBack, "md-arrow-back", android.graphics.Color.WHITE)
        
        val cameraIcon = com.joanzapata.iconify.IconDrawable(this, "md-camera-alt")
            .colorRes(R.color.primary)
            .sizeDp(18)
        btnChangePhoto.setCompoundDrawablesWithIntrinsicBounds(cameraIcon, null, null, null)

        val uploadIcon = com.joanzapata.iconify.IconDrawable(this, "md-file-upload")
            .colorRes(R.color.primary)
            .sizeDp(18)
        btnUploadPdf.setCompoundDrawablesWithIntrinsicBounds(uploadIcon, null, null, null)
    }

    private fun initViews() {
        imgProfile = findViewById(R.id.imgProfile)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        btnUploadPdf = findViewById(R.id.btnUploadPdf)
        btnBack = findViewById(R.id.btnBack)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        layoutCredentials = findViewById(R.id.layoutCredentials)
        tvUploadedFile = findViewById(R.id.tvUploadedFile)

        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
    }

    private fun getRealFileName(uri: Uri): String {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    name = it.getString(it.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                name = name?.substring(cut + 1)
            }
        }
        return (name ?: "file_${System.currentTimeMillis()}").replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    private fun copyUriToCache(uri: Uri): File {
        val target = File(cacheDir, "${System.currentTimeMillis()}_${getRealFileName(uri)}")
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open selected file")
        inputStream.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return target
    }

    private fun displayFileName(path: String?): String {
        val value = path?.trim().orEmpty()
        if (value.isEmpty()) return "No file selected"
        return value.substringAfterLast('/').ifBlank { value }
    }


    private fun loadExistingProfile() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

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
                    tvUploadedFile.text = displayFileName(data.credentialsFile)

                    // ✅ Load profile image
                    if (!data.profilePhoto.isNullOrEmpty()) {
                        val placeholderDrawable = com.joanzapata.iconify.IconDrawable(this@EditProfileActivity, "md-person")
                            .colorRes(android.R.color.darker_gray)
                            .sizeDp(48)
                        Glide.with(this@EditProfileActivity)
                            .load(data.profilePhoto)
                            .placeholder(placeholderDrawable)
                            .into(imgProfile)
                    }

                    // ✅ Save to shared prefs
                    val editor = prefs.edit()
                    editor.putString("firstName", data.firstName)
                    editor.putString("lastName", data.lastName)
                    editor.putString("email", data.email)
                    editor.putString("phone", data.phoneNumber)
                    editor.putString("profilePhoto", data.profilePhoto)
                    editor.putString("credentialsFile", data.credentialsFile)
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
            if (userRole != UserRole.ARCHITECT) return@setOnClickListener
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
                        try {
                            selectedImageFile?.delete()
                            selectedImageFile = copyUriToCache(uri)
                            Glide.with(this).load(uri).into(imgProfile)
                        } catch (ex: Exception) {
                            Toast.makeText(this, "Failed to read selected image: ${ex.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    PICK_PDF -> {
                        try {
                            selectedPdfFile?.delete()
                            selectedPdfFile = copyUriToCache(uri)
                            tvUploadedFile.text = getRealFileName(uri)
                            Toast.makeText(this, "PDF selected", Toast.LENGTH_SHORT).show()
                        } catch (ex: Exception) {
                            Toast.makeText(this, "Failed to read selected PDF: ${ex.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun saveChanges() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        when {
            firstName.isEmpty() -> {
                Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show()
                return
            }
            lastName.isEmpty() -> {
                Toast.makeText(this, "Please enter your last name", Toast.LENGTH_SHORT).show()
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return
            }
            phone.length != 11 || !phone.all { it.isDigit() } -> {
                Toast.makeText(this, "Please enter an 11-digit phone number", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val rbUserId = userId.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbFirst = firstName.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbLast = lastName.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbEmail = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbPhone = phone.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = selectedImageFile?.let {
            MultipartBody.Part.createFormData(
                "ProfilePhoto",
                it.name, // <-- NOW CORRECT EXTENSION
                it.asRequestBody("image/*".toMediaTypeOrNull())
            )
        }


        val pdfPart = if (userRole == UserRole.ARCHITECT) selectedPdfFile?.let {
            MultipartBody.Part.createFormData(
                "CredentialsFile",
                it.name,
                it.asRequestBody("application/pdf".toMediaTypeOrNull())
            )
        } else null

        btnSaveChanges.isEnabled = false

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
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data

                    val editor = prefs.edit()
                    editor.putString("firstName", data?.user_fname ?: firstName)
                    editor.putString("lastName", data?.user_lname ?: lastName)
                    editor.putString("email", data?.email ?: email)
                    editor.putString("phone", data?.phoneNumber ?: phone)
                    editor.putString("profilePhoto", data?.user_profilePhoto ?: prefs.getString("profilePhoto", null))
                    editor.putString("credentialsFile", data?.user_CredentialsFile ?: prefs.getString("credentialsFile", null))
                    editor.apply()

                    selectedImageFile?.delete()
                    selectedPdfFile?.delete()

                    Toast.makeText(this@EditProfileActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    btnSaveChanges.isEnabled = true
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<EditProfileResponse>, t: Throwable) {
                btnSaveChanges.isEnabled = true
                Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}

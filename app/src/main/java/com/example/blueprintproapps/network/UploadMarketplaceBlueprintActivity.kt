package com.example.blueprintproapps.network

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UploadMarketplaceBlueprintActivity : AppCompatActivity() {

    private lateinit var previewImage: ImageView
    private lateinit var selectImageBtn: Button
    private lateinit var uploadBtn: Button
    private lateinit var blueprintName: EditText
    private lateinit var blueprintPrice: EditText
    private lateinit var blueprintDescription: EditText
    private lateinit var styleSpinner: Spinner
    private lateinit var backBtn: ImageButton

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_marketplace_blueprint)

        previewImage = findViewById(R.id.previewImage)
        selectImageBtn = findViewById(R.id.selectImageBtn)
        uploadBtn = findViewById(R.id.uploadBtn)
        blueprintName = findViewById(R.id.blueprintName)
        blueprintPrice = findViewById(R.id.blueprintPrice)
        blueprintDescription = findViewById(R.id.blueprintDescription)
        styleSpinner = findViewById(R.id.styleSpinner)
        backBtn = findViewById(R.id.backButton)

        // Setup style spinner
        val styles = arrayOf("Modern", "Traditional", "Contemporary", "Minimalist")
        styleSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, styles)

        selectImageBtn.setOnClickListener { pickImage() }
        uploadBtn.setOnClickListener { uploadBlueprint() }
        backBtn.setOnClickListener {
            val intent = Intent(this, ArchitectBlueprintActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Blueprint Image"), PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java") // keep override signature compatible
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            previewImage.setImageURI(imageUri)
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri) ?: throw IllegalArgumentException("Cannot open input stream from URI")
        val tempFile = File.createTempFile("upload_", ".tmp", cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun uploadBlueprint() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val name = blueprintName.text.toString().trim()
        val price = blueprintPrice.text.toString().trim()
        val desc = blueprintDescription.text.toString().trim()
        val style = styleSpinner.selectedItem.toString()

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a blueprint name", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert URI to a File
        val file: File = try {
            uriToFile(imageUri!!)
        } catch (ex: Exception) {
            Toast.makeText(this, "Failed to read selected image: ${ex.message}", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare multipart parts
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val bodyPart = MultipartBody.Part.createFormData("BlueprintImage", file.name, requestFile)

        val namePart = name.toRequestBody("text/plain".toMediaType())
        val pricePart = price.toRequestBody("text/plain".toMediaType())
        val descPart = desc.toRequestBody("text/plain".toMediaType())
        val stylePart = style.toRequestBody("text/plain".toMediaType())
        val isForSalePart = "true".toRequestBody("text/plain".toMediaType())
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userArchitectId = prefs.getString("architectId", null)
        Log.d("UploadBlueprint", "Architect ID from SharedPreferences: $userArchitectId")
        if (userArchitectId == null) {
            Toast.makeText(this, "Error: Architect ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }
        val architectIdPart = userArchitectId.toRequestBody("text/plain".toMediaType())

        val api = ApiClient.instance

        api.addMarketplaceBlueprint(
            namePart,
            pricePart,
            descPart,
            stylePart,
            isForSalePart,
            architectIdPart,
            bodyPart
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UploadMarketplaceBlueprintActivity, "Uploaded successfully!", Toast.LENGTH_SHORT).show()
                    file.delete()
                    finish()
                } else {
                    Toast.makeText(this@UploadMarketplaceBlueprintActivity, "Upload failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@UploadMarketplaceBlueprintActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

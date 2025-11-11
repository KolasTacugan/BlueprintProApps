package com.example.blueprintproapps.network

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.api.ApiService
import com.squareup.picasso.Picasso
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

class EditMarketplaceBlueprintActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var editName: EditText
    private lateinit var editPrice: EditText
    private lateinit var editStyle: Spinner
    private lateinit var saveBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var editDescription: EditText

    private var blueprintId: Int = 0
    private var selectedImageUri: Uri? = null
    private var currentImage: String? = null
    private lateinit var selectImageBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_marketplace_blueprint)

        imageView = findViewById(R.id.previewImage)
        editName = findViewById(R.id.blueprintName)
        editPrice = findViewById(R.id.blueprintPrice)
        editStyle = findViewById(R.id.styleSpinner)
        editDescription = findViewById(R.id.blueprintDescription)
        saveBtn = findViewById(R.id.saveBtn)
        deleteBtn = findViewById(R.id.deleteBlueprintBtn)
        selectImageBtn = findViewById(R.id.selectImageBtn)

        saveBtn.text = "Save Changes"
        deleteBtn.visibility = android.view.View.VISIBLE

        // Get data from intent
        editDescription.setText(intent.getStringExtra("blueprintDescription"))
        blueprintId = intent.getIntExtra("blueprintId", 0)
        editName.setText(intent.getStringExtra("blueprintName"))
        editPrice.setText(intent.getStringExtra("blueprintPrice"))
        val styles = arrayOf("Modern", "Traditional", "Contemporary", "Minimalist")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, styles)
        editStyle.adapter = adapter

        // Set the selected style from the intent
        val currentStyle = intent.getStringExtra("blueprintStyle")
        if (currentStyle != null) {
            val index = styles.indexOf(currentStyle)
            if (index >= 0) {
                editStyle.setSelection(index)
            }
        }
        currentImage = intent.getStringExtra("blueprintImage")

        // Show current image
        if (!currentImage.isNullOrEmpty()) {
            Picasso.get().load(currentImage).into(imageView)
        }

        // Choose new image
        selectImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }

        // Save changes
        saveBtn.setOnClickListener { updateBlueprint() }

        // Delete blueprint
        deleteBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Blueprint")
                .setMessage("Are you sure you want to delete this blueprint? Once deleted, it can't be restored.")
                .setPositiveButton("Proceed") { _, _ ->
                    deleteBlueprint(blueprintId)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK && data?.data != null) {
            selectedImageUri = data.data
            imageView.setImageURI(selectedImageUri)
        }
    }

    // Reuse Upload function’s way of converting Uri to File
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

    private fun updateBlueprint() {
        val name = editName.text.toString().trim()
        val price = editPrice.text.toString().trim()
        val style = editStyle.selectedItem.toString()

        val progress = ProgressDialog(this)
        progress.setMessage("Saving changes...")
        progress.show()

        val description = editDescription.text.toString().trim()
        val descriptionPart = description.toRequestBody("text/plain".toMediaType())

        val idPart = blueprintId.toString().toRequestBody("text/plain".toMediaType())
        val namePart = name.toRequestBody("text/plain".toMediaType())
        val pricePart = price.toRequestBody("text/plain".toMediaType())
        val stylePart = style.toRequestBody("text/plain".toMediaType())

        var imagePart: MultipartBody.Part? = null
        if (selectedImageUri != null) {
            val file = uriToFile(selectedImageUri!!)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            imagePart = MultipartBody.Part.createFormData("BlueprintImage", file.name, requestFile)
        }

        ApiClient.instance.editBlueprint(
            idPart,
            namePart,
            pricePart,
            stylePart,
            descriptionPart,
            imagePart
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progress.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(this@EditMarketplaceBlueprintActivity, "Updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditMarketplaceBlueprintActivity, "Update failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(this@EditMarketplaceBlueprintActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteBlueprint(blueprintId: Int) {
        val apiService = ApiClient.instance
        val call = apiService.deleteBlueprint(blueprintId)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditMarketplaceBlueprintActivity, "Blueprint deleted successfully.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditMarketplaceBlueprintActivity, "Failed to delete blueprint.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@EditMarketplaceBlueprintActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

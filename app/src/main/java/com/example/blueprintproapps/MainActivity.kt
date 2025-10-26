package com.example.blueprintproapps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.CompletePurchaseRequest
import com.example.blueprintproapps.models.GenericResponse
import com.example.blueprintproapps.network.MarketPlaceActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Handle deep link when app is first launched
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }


    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data

        if (data != null && data.scheme == "blueprintpro") {
            when (data.host) {
                "payment-success" -> {
                    Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show()
                    completePurchase()
                }
                "payment-cancel" -> {
                    Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun completePurchase() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val clientId = prefs.getString("clientId", null)
        val blueprintIdsSet = prefs.getStringSet("purchasedBlueprintIds", emptySet()) ?: emptySet()
        val blueprintIds = blueprintIdsSet.map { it.toInt() }

        if (clientId.isNullOrEmpty()) {
            Toast.makeText(this, "ClientId not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (blueprintIds.isEmpty()) {
            Toast.makeText(this, "No blueprints to complete purchase", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CompletePurchaseRequest(clientId, blueprintIds)

        val api = ApiClient.instance
        api.completePurchase(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@MainActivity, "Purchase completed!", Toast.LENGTH_LONG).show()
                    // ✅ Clear purchased IDs
                    prefs.edit().remove("purchasedBlueprintIds").apply()

                    // ✅ Go back to MarketplaceActivity
                    val intent = Intent(this@MainActivity, MarketPlaceActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish() // optional: close MainActivity if you want
                } else {
                    Toast.makeText(this@MainActivity, "Failed to finalize purchase", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Kotlin data class matching your backend


}

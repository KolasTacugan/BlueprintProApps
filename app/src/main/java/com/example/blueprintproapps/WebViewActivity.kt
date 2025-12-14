package com.example.blueprintproapps

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ArchitectSubscriptionCompleteRequest
import com.example.blueprintproapps.models.GenericResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        val url = intent.getStringExtra("url")
        if (url == null) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ Handle success/cancel deep links
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

                if (url == null) return false

                return when {
                    url.startsWith("blueprintpro://subscription-success") -> {
                        onSubscriptionSuccess()
                        true
                    }
                    url.startsWith("blueprintpro://subscription-cancel") -> {
                        onSubscriptionCancel()
                        true
                    }
                    else -> false
                }
            }
        }

        webView.loadUrl(url)
    }

    private fun onSubscriptionSuccess() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val architectId = prefs.getString("architectId", null)

        if (architectId == null) {
            Toast.makeText(this, "Architect ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val request = ArchitectSubscriptionCompleteRequest(architectId)

        ApiClient.instance.completeSubscription(request)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(
                    call: Call<GenericResponse>,
                    response: Response<GenericResponse>
                ) {
                    Toast.makeText(
                        this@WebViewActivity,
                        "Subscription Activated!",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(
                        this@WebViewActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun onSubscriptionCancel() {
        Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show()
        finish()
    }
}
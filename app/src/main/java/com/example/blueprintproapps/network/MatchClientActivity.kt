package com.example.blueprintproapps.network

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.MatchResponse
import com.example.blueprintproapps.utils.ArchitectDetailBottomSheet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ScrollView

class MatchClientActivity : AppCompatActivity() {

    private lateinit var root: ConstraintLayout
    private lateinit var appLogo: ImageView
    private lateinit var clientPrompt: EditText
    private lateinit var loadingSection: LinearLayout
    private lateinit var matchRecyclerView: RecyclerView
    private lateinit var matchAdapter: MatchAdapter

    private var hasSearched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_client)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(
                v.paddingLeft,
                topInset,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        root = findViewById(R.id.root)
        appLogo = findViewById(R.id.appLogo)
        clientPrompt = findViewById(R.id.clientPrompt)
        loadingSection = findViewById(R.id.loadingSection)
        matchRecyclerView = findViewById(R.id.matchRecyclerView)

        matchRecyclerView.layoutManager = LinearLayoutManager(this)
        matchAdapter = MatchAdapter(
            onRequestClick = { architectId ->
                sendMatchRequest(architectId)
            },
            onProfileClick = { ArchitectDetailBottomSheet(
                match = it,
                clientQuery = clientPrompt.text.toString()
            ).show(supportFragmentManager, "ArchitectDetail")
            }
        )
        matchRecyclerView.adapter = matchAdapter

        clientPrompt.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val query = clientPrompt.text.toString().trim()
                if (query.isNotEmpty()) {
                    if (!hasSearched) animateToSearchState()
                    performSearch(query)
                }
                true
            } else false
        }
    }

    private fun animateToSearchState() {
        hasSearched = true

        findViewById<View>(R.id.searchTitle).visibility = View.GONE
        findViewById<View>(R.id.searchSubtitle).visibility = View.GONE

        val set = ConstraintSet()
        set.clone(root)

        // Logo: force top-left
        set.clear(appLogo.id, ConstraintSet.BOTTOM)
        set.clear(appLogo.id, ConstraintSet.END)

        set.connect(appLogo.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 34)
        set.connect(appLogo.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 10)

        set.constrainWidth(appLogo.id, 86)
        set.constrainHeight(appLogo.id, 86)

        // Search bar beside logo
        set.clear(clientPrompt.id, ConstraintSet.TOP)
        set.clear(clientPrompt.id, ConstraintSet.START)

        set.connect(clientPrompt.id, ConstraintSet.START, appLogo.id, ConstraintSet.END, 12)
        set.connect(clientPrompt.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 12)
        set.connect(clientPrompt.id, ConstraintSet.TOP, appLogo.id, ConstraintSet.TOP)
        set.connect(clientPrompt.id, ConstraintSet.BOTTOM, appLogo.id, ConstraintSet.BOTTOM)

        set.applyTo(root)
    }

    private fun performSearch(query: String) {
        lockSearchInput()

        // Show loading UI
        loadingSection.visibility = View.VISIBLE
        matchRecyclerView.visibility = View.GONE

        val clientId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            .getString("clientId", null) ?: return

        ApiClient.instance.getMatches(clientId, query)
            .enqueue(object : Callback<List<MatchResponse>> {

                override fun onResponse(
                    call: Call<List<MatchResponse>>,
                    response: Response<List<MatchResponse>>
                ) {
                    loadingSection.visibility = View.GONE
                    matchRecyclerView.visibility = View.VISIBLE
                    matchAdapter.submitList(response.body())

                    unlockSearchInput() // ✅ allow user to tap again
                }

                override fun onFailure(call: Call<List<MatchResponse>>, t: Throwable) {
                    loadingSection.visibility = View.GONE
                    Log.e("MatchClientActivity", "Error", t)

                    unlockSearchInput() // ✅ also unlock on failure
                }
            })
    }

    private fun sendMatchRequest(architectId: String) {
        val clientId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            .getString("clientId", "") ?: ""

        if (clientId.isEmpty()) return

        ApiClient.instance.requestMatch(
            com.example.blueprintproapps.models.MatchRequest(architectId, clientId)
        ).enqueue(object : Callback<com.example.blueprintproapps.models.GenericResponse> {

            override fun onResponse(
                call: Call<com.example.blueprintproapps.models.GenericResponse>,
                response: Response<com.example.blueprintproapps.models.GenericResponse>
            ) {
                if (response.isSuccessful) {
                    // Optional: refresh list so Pending comes from backend
                    // performSearch(clientPrompt.text.toString())
                }
            }

            override fun onFailure(call: Call<com.example.blueprintproapps.models.GenericResponse>, t: Throwable) {
                Log.e("MatchClientActivity", "Match request failed", t)
            }
        })
    }

    private fun lockSearchInput() {
        // Remove focus and hide keyboard
        root.requestFocus()

        clientPrompt.clearFocus()
        clientPrompt.isFocusable = false
        clientPrompt.isFocusableInTouchMode = false
        clientPrompt.isCursorVisible = false

        val imm = getSystemService(INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(clientPrompt.windowToken, 0)
    }

    private fun unlockSearchInput() {
        // Keep it unfocused, but allow tapping again
        clientPrompt.isFocusable = true
        clientPrompt.isFocusableInTouchMode = true
        clientPrompt.isCursorVisible = true
    }


}

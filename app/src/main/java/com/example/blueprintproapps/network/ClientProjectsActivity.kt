package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.ClientProjectAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.ClientProjectResponse
import com.example.blueprintproapps.navigation.AppNavDestination
import com.example.blueprintproapps.navigation.AppNavigator
import com.example.blueprintproapps.utils.ArchitectProfileBottomSheet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClientProjectAdapter
    private var allProjects = ArrayList<ClientProjectResponse>()
    private var clientId: String? = null

    private lateinit var backBtn: ImageButton
    private lateinit var filterBtn: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var filterScroll: android.view.View
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var emptyState: android.view.View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val session = AuthSessionManager.requireSession(this, UserRole.CLIENT) ?: return
        setContentView(R.layout.activity_client_projects)

        val main = findViewById<android.view.View>(R.id.main) ?: findViewById<android.view.View>(android.R.id.content)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        clientId = session.userId

        recyclerView = findViewById(R.id.recyclerViewClientProjects)
        backBtn = findViewById(R.id.backButton)
        filterBtn = findViewById(R.id.filterBtn)
        searchEditText = findViewById(R.id.searchEditText)
        filterScroll = findViewById(R.id.filterScroll)
        filterChipGroup = findViewById(R.id.filterChipGroup)
        emptyState = findViewById(R.id.emptyState)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ClientProjectAdapter(mutableListOf(), this,
            onItemClick = { project ->
                // Existing click logic if needed
            },
            onArchitectNameClick = { architectId ->
                openArchitectProfileBottomSheet(architectId)
            }
        )
        recyclerView.adapter = adapter

        // Search logic
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProjects()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Filter button toggle logic
        filterBtn.setOnClickListener {
            if (filterScroll.visibility == android.view.View.VISIBLE) {
                filterScroll.visibility = android.view.View.GONE
            } else {
                filterScroll.visibility = android.view.View.VISIBLE
                filterScroll.alpha = 0f
                filterScroll.animate().alpha(1f).setDuration(300).start()
            }
        }

        // Filter chips logic
        filterChipGroup.setOnCheckedChangeListener { _, _ ->
            filterProjects()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ClientProjectsActivity, ClientDashboardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        AppNavigator.bind(
            activity = this,
            bottomNavigationView = bottomNavigation,
            role = UserRole.CLIENT,
            currentDestination = AppNavDestination.WORK
        )

        fetchProjects()

        findViewById<android.view.View>(R.id.clearFilterBtn).setOnClickListener {
            searchEditText.text.clear()
            filterChipGroup.check(R.id.chipAll)
            filterProjects()
        }
    }

    private fun filterProjects() {
        val query = searchEditText.text.toString().lowercase()
        val checkedChipId = filterChipGroup.checkedChipId
        
        val filtered = allProjects.filter { project ->
            val matchesQuery = project.project_Title.lowercase().contains(query) || 
                             project.architectName.lowercase().contains(query)
            
            val matchesFilter = when (checkedChipId) {
                R.id.chipOngoing -> project.project_Status == "Ongoing"
                R.id.chipFinished -> project.project_Status == "Finished"
                else -> true
            }
            
            matchesQuery && matchesFilter
        }
        
        adapter.updateData(filtered)
        emptyState.visibility = if (filtered.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun openArchitectProfileBottomSheet(architectId: String) {
        val sheet = ArchitectProfileBottomSheet(architectId)
        sheet.show(supportFragmentManager, "ArchitectProfile")
    }

    private fun fetchProjects() {
        if (clientId.isNullOrEmpty()) {
            Toast.makeText(this, "Client ID not found.", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.getClientProjects(clientId!!).enqueue(object : Callback<List<ClientProjectResponse>> {
            override fun onResponse(
                call: Call<List<ClientProjectResponse>>,
                response: Response<List<ClientProjectResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    allProjects.clear()
                    allProjects.addAll(response.body()!!)
                    filterProjects()
                } else {
                    Toast.makeText(this@ClientProjectsActivity, "Failed to load", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ClientProjectResponse>>, t: Throwable) {
                Log.e("API", t.message.toString())
                Toast.makeText(this@ClientProjectsActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
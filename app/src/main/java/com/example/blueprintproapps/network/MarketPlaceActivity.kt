package com.example.blueprintproapps.network

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.BlueprintAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.BlueprintResponse
import com.example.blueprintproapps.models.CartItem
import com.example.blueprintproapps.models.MarketplaceResponse
import com.example.blueprintproapps.utils.CartBottomSheet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import android.view.WindowManager


class MarketPlaceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlueprintAdapter
    private val blueprintList = mutableListOf<BlueprintResponse>()
    private val displayedList = mutableListOf<BlueprintResponse>()
    // ✅ Cart badge elements
    private lateinit var cartBtn: com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
    private lateinit var categoryTabs: TabLayout
    private lateinit var searchEditText: EditText
    private lateinit var marketplaceState: TextView
    private var cartItemCount = 0
    private lateinit var clientId: String
    private var selectedCategory = "All"
    private var cartBlueprintIds = emptySet<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = AuthSessionManager.requireSession(this, UserRole.CLIENT) ?: return
        clientId = session.userId

        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        setContentView(R.layout.activity_market_place)

        recyclerView = findViewById(R.id.blueprintRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        marketplaceState = findViewById(R.id.marketplaceState)
        searchEditText = findViewById(R.id.searchEditText)
        categoryTabs = findViewById(R.id.categoryTabs)

        findViewById<MaterialToolbar>(R.id.topBar).setNavigationOnClickListener { finish() }

        // ✅ Initialize cart button
        cartBtn = findViewById(R.id.cartBtn)
        cartBtn.text = "Cart ($cartItemCount)"

        // ✅ Initialize adapter with cart update listener
        adapter = BlueprintAdapter(displayedList, this, object : BlueprintAdapter.OnCartUpdateListener {
            override fun onItemAdded() {
                cartItemCount++
                cartBtn.text = "Cart ($cartItemCount)"
            }
        })

        recyclerView.adapter = adapter

        cartBtn.setOnClickListener {
            val cartBottomSheet = CartBottomSheet()

            cartBottomSheet.onCartClosed = {
                refreshMarketplace()   // ✅ Your new function
            }
            cartBottomSheet.show(supportFragmentManager, "CartBottomSheet")
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = filterBlueprints()
            override fun afterTextChanged(s: Editable?) = Unit
        })
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchEditText.clearFocus()
                true
            } else {
                false
            }
        }

        categoryTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedCategory = tab.text.toString()
                filterBlueprints()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        fetchMarketplace()
        fetchCartCount()
    }

    private fun refreshMarketplace() {
        fetchCartCount()     // ✅ Updates the cart badge AND sets isAddedToCart flags
        filterBlueprints()
    }

    private fun filterBlueprints() {
        val query = searchEditText.text.toString().trim()
        val filtered = blueprintList.filter { blueprint ->
            val matchesCategory = selectedCategory == "All" ||
                blueprint.blueprintStyle?.contains(selectedCategory, ignoreCase = true) == true
            val matchesQuery = query.isBlank() ||
                blueprint.blueprintName.contains(query, ignoreCase = true) ||
                blueprint.blueprintDescription?.contains(query, ignoreCase = true) == true ||
                blueprint.blueprintStyle?.contains(query, ignoreCase = true) == true

            matchesCategory && matchesQuery
        }

        adapter.updateList(filtered)
        renderState(
            if (filtered.isEmpty()) "No blueprints found for the current search or category." else "",
            showList = filtered.isNotEmpty()
        )
    }



    private fun fetchMarketplace() {
        renderState("Loading marketplace...", showList = false)
        ApiClient.instance.getMarketplace().enqueue(object : Callback<MarketplaceResponse> {
            override fun onResponse(
                call: Call<MarketplaceResponse>,
                response: Response<MarketplaceResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && !data.Blueprints.isNullOrEmpty()) {
                        blueprintList.clear()
                        blueprintList.addAll(data.Blueprints)
                        applyCartFlags()
                        setupCategoryTabs()

                        filterBlueprints()
                        Log.d("StripeKey", data.StripePublishableKey)
                    } else {
                        blueprintList.clear()
                        adapter.updateList(emptyList())
                        setupCategoryTabs()
                        renderState("No marketplace blueprints are available yet.", showList = false)
                        Log.e("Marketplace", "No blueprints found or body is null")
                    }
                } else {
                    renderState("Failed to load marketplace.", showList = false)
                    Log.e("Marketplace", "Response error code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MarketplaceResponse>, t: Throwable) {
                renderState("Network error while loading marketplace.", showList = false)
                Log.e("MarketplaceError", t.message ?: "Unknown error")
            }
        })
    }

    private fun fetchCartCount() {
        ApiClient.instance.getCart(clientId).enqueue(object : Callback<List<CartItem>> {
            override fun onResponse(call: Call<List<CartItem>>, response: Response<List<CartItem>>) {
                if (response.isSuccessful) {
                    val cartItems = response.body() ?: emptyList()
                    cartItemCount = cartItems.size
                    cartBtn.text = "Cart ($cartItemCount)"
                    Log.d("CartCount", "Cart count updated: $cartItemCount")

                    cartBlueprintIds = cartItems.map { it.blueprintId }.toSet()
                    applyCartFlags()
                    filterBlueprints()
                } else {
                    Log.e("CartCountError", "Response code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                Log.e("CartCountError", "Failed: ${t.message}")
            }
        })
    }

    private fun applyCartFlags() {
        blueprintList.forEach { bp ->
            bp.isAddedToCart = cartBlueprintIds.contains(bp.blueprintId)
        }
    }

    private fun setupCategoryTabs() {
        val previousCategory = selectedCategory
        val styles = blueprintList.mapNotNull { it.blueprintStyle?.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
        val categories = listOf("All") + styles

        categoryTabs.clearOnTabSelectedListeners()
        categoryTabs.removeAllTabs()
        categories.forEach { category ->
            categoryTabs.addTab(categoryTabs.newTab().setText(category), category == previousCategory)
        }
        if (categoryTabs.selectedTabPosition == -1 && categoryTabs.tabCount > 0) {
            categoryTabs.getTabAt(0)?.select()
            selectedCategory = "All"
        }
        categoryTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedCategory = tab.text.toString()
                filterBlueprints()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun renderState(message: String, showList: Boolean) {
        marketplaceState.text = message
        marketplaceState.visibility = if (message.isBlank()) View.GONE else View.VISIBLE
        recyclerView.visibility = if (showList) View.VISIBLE else View.GONE
    }
    override fun onResume() {
        super.onResume()


        fetchCartCount()
    }


}

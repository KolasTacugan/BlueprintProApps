package com.example.blueprintproapps.network

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.BlueprintAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.BlueprintResponse
import com.example.blueprintproapps.models.CartItem
import com.example.blueprintproapps.models.MarketplaceResponse
import com.example.blueprintproapps.utils.CartBottomSheet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MarketPlaceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlueprintAdapter
    private val blueprintList = mutableListOf<BlueprintResponse>()

    // ✅ Cart badge elements
    private lateinit var cartCountText: TextView
    private var cartItemCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_place)

        recyclerView = findViewById(R.id.blueprintRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // ✅ Initialize cart badge
        cartCountText = findViewById(R.id.cartCount)
        cartCountText.text = cartItemCount.toString()

        // ✅ Initialize adapter with cart update listener
        adapter = BlueprintAdapter(blueprintList, this, object : BlueprintAdapter.OnCartUpdateListener {
            override fun onItemAdded() {
                cartItemCount++
                cartCountText.text = cartItemCount.toString()
            }
        })

        recyclerView.adapter = adapter

        val cartIcon: ImageView = findViewById(R.id.cartIcon)
        cartIcon.setOnClickListener {
            val cartBottomSheet = CartBottomSheet()
            cartBottomSheet.show(supportFragmentManager, "CartBottomSheet")
        }

        fetchMarketplace()
        fetchCartCount()

    }

    private fun fetchMarketplace() {
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
                        adapter.notifyDataSetChanged()
                        Log.d("StripeKey", data.StripePublishableKey)
                    } else {
                        Log.e("Marketplace", "No blueprints found or body is null")
                    }
                } else {
                    Log.e("Marketplace", "Response error code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MarketplaceResponse>, t: Throwable) {
                Log.e("MarketplaceError", t.message ?: "Unknown error")
            }
        })
    }
    private fun fetchCartCount() {
        val sharedPrefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val clientId = sharedPrefs.getString("clientId", null)

        if (clientId == null) {
            Log.d("CartCount", "No clientId found. User not logged in.")
            return
        }

        ApiClient.instance.getCart(clientId).enqueue(object : Callback<List<CartItem>> {
            override fun onResponse(call: Call<List<CartItem>>, response: Response<List<CartItem>>) {
                if (response.isSuccessful) {
                    val cartItems = response.body() ?: emptyList()
                    cartItemCount = cartItems.size
                    cartCountText.text = cartItemCount.toString()
                    Log.d("CartCount", "Cart count updated: $cartItemCount")
                } else {
                    Log.e("CartCountError", "Response code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                Log.e("CartCountError", "Failed: ${t.message}")
            }
        })
    }


}

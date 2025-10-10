package com.example.blueprintproapps.network

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.adapter.BlueprintAdapter
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.BlueprintResponse
import com.example.blueprintproapps.models.MarketplaceResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MarketPlaceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlueprintAdapter
    private val blueprintList = mutableListOf<BlueprintResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_place)

        recyclerView = findViewById(R.id.blueprintRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = BlueprintAdapter(blueprintList)
        recyclerView.adapter = adapter

        fetchMarketplace()
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



}


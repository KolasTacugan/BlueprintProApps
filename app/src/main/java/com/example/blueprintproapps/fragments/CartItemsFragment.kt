package com.example.blueprintproapps.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.databinding.FragmentCartItemsBinding
import com.example.blueprintproapps.adapter.CartAdapter
import com.example.blueprintproapps.models.CartItem
import com.example.blueprintproapps.models.GenericResponsee
import com.example.blueprintproapps.models.RemoveCartRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartItemsFragment : Fragment() {

    private var _binding: FragmentCartItemsBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter
    private val api = ApiClient.instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartItemsBinding.inflate(inflater, container, false)

        // ✅ Pass the onRemoveClick callback to the adapter
        cartAdapter = CartAdapter { item ->
            removeFromCart(item)
        }

        binding.cartRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.cartRecyclerView.adapter = cartAdapter

        val prefs = requireContext().getSharedPreferences("MyAppPrefs", AppCompatActivity.MODE_PRIVATE)
        val clientId = prefs.getString("clientId", null)

        if (!clientId.isNullOrEmpty()) {
            fetchCart(clientId)
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun fetchCart(clientId: String) {
        api.getCart(clientId).enqueue(object : Callback<List<CartItem>> {
            override fun onResponse(call: Call<List<CartItem>>, response: Response<List<CartItem>>) {
                if (response.isSuccessful) {
                    val cartItems = response.body().orEmpty()

                    if (cartItems.isNotEmpty()) {
                        binding.emptyCartText.visibility = View.GONE
                        binding.cartRecyclerView.visibility = View.VISIBLE
                        cartAdapter.submitList(cartItems)

                        // Compute total
                        val totalPrice = cartItems.sumOf { it.blueprintPrice }
                        binding.cartTotalText.text = "Total: ₱${"%.2f".format(totalPrice)}"
                        binding.checkoutBtn.isEnabled = totalPrice > 0
                    } else {
                        showEmptyCart()
                    }
                } else {
                    showEmptyCart()
                }
            }

            override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                Toast.makeText(context, "Failed to fetch cart: ${t.message}", Toast.LENGTH_SHORT).show()
                showEmptyCart()
            }
        })
    }

    private fun showEmptyCart() {
        binding.emptyCartText.visibility = View.VISIBLE
        binding.cartRecyclerView.visibility = View.GONE
        binding.cartTotalText.text = "Total: ₱0.00"
        binding.checkoutBtn.isEnabled = false
    }

    private fun removeFromCart(item: CartItem) {
        val prefs = requireContext().getSharedPreferences("MyAppPrefs", AppCompatActivity.MODE_PRIVATE)
        val clientId = prefs.getString("clientId", null) ?: return

        val request = RemoveCartRequest(clientId = clientId, blueprintId = item.blueprintId)

        api.removeFromCart(request).enqueue(object : Callback<GenericResponsee> {
            override fun onResponse(call: Call<GenericResponsee>, response: Response<GenericResponsee>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "Item removed successfully", Toast.LENGTH_SHORT).show()
                    fetchCart(clientId) // ✅ Refresh list
                } else {
                    Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponsee>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

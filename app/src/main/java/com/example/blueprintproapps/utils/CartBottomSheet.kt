package com.example.blueprintproapps.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.example.blueprintproapps.adapter.CartPagerAdapter
import com.example.blueprintproapps.databinding.CartModalBinding

class CartBottomSheet : BottomSheetDialogFragment() {

    var onCartClosed: (() -> Unit)? = null  // ✅ Callback for when sheet is closed
    private var _binding: CartModalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CartModalBinding.inflate(inflater, container, false)

        // Setup ViewPager2 with tabs
        val pagerAdapter = CartPagerAdapter(this)
        binding.cartViewPager.adapter = pagerAdapter
        TabLayoutMediator(binding.cartTabs, binding.cartViewPager) { tab, position ->
            tab.text = "Cart"
        }.attach()

        // Close button → dismiss the sheet
        binding.cartCloseBtn.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    // 🛡️ THIS IS WHAT BLOCKS SCREENSHOTS FOR THE BOTTOM SHEET
    override fun onStart() {
        super.onStart()
        dialog?.window?.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    // ✅ Proper place to trigger the refresh callback
    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onCartClosed?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

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
            tab.text = if (position == 0) "Cart" else "Saved"
        }.attach()

        // Close button
        binding.cartCloseBtn.setOnClickListener { dismiss() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



package com.example.blueprintproapps.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.blueprintproapps.fragments.CartItemsFragment
import com.example.blueprintproapps.fragments.SavedItemsFragment

class CartPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) CartItemsFragment() else SavedItemsFragment()
    }
}
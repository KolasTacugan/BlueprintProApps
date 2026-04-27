package com.example.blueprintproapps.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.blueprintproapps.fragments.CartItemsFragment


class CartPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 1

    override fun createFragment(position: Int): Fragment {
        return CartItemsFragment()
    }

}
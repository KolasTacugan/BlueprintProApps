package com.example.blueprintproapps.navigation

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.example.blueprintproapps.R
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.navigation.fragments.ArchitectHomeFragment
import com.example.blueprintproapps.navigation.fragments.ArchitectMessagesFragment
import com.example.blueprintproapps.navigation.fragments.ArchitectWorkFragment
import com.example.blueprintproapps.navigation.fragments.ClientHomeFragment
import com.example.blueprintproapps.navigation.fragments.ClientMessagesFragment
import com.example.blueprintproapps.navigation.fragments.ClientWorkFragment
import com.example.blueprintproapps.navigation.fragments.ProfileFragment
import com.example.blueprintproapps.network.ArchitectDashboardActivity
import com.example.blueprintproapps.network.ArchitectMatchActivity
import com.example.blueprintproapps.network.ArchitectMessagesActivity
import com.example.blueprintproapps.network.ClientDashboardActivity
import com.example.blueprintproapps.network.ClientProjectsActivity
import com.example.blueprintproapps.network.MessagesActivity
import com.example.blueprintproapps.network.ProfileActivity
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.bottomnavigation.BottomNavigationView

object AppNavigator {

    fun bindHost(
        activity: AppCompatActivity,
        bottomNavigationView: BottomNavigationView,
        role: UserRole,
        initialDestination: AppNavDestination?
    ) {
        val menuRes = when (role) {
            UserRole.CLIENT -> R.menu.bottom_nav_menu
            UserRole.ARCHITECT -> R.menu.bottom_nav_menu_architect
        }
        bottomNavigationView.menu.clear()
        bottomNavigationView.inflateMenu(menuRes)
        applyIcons(activity, bottomNavigationView, role)

        val startDestination = initialDestination ?: AppNavDestination.HOME
        showFragment(activity, role, startDestination)
        bottomNavigationView.selectedItemId = startDestination.menuId

        bottomNavigationView.setOnItemSelectedListener { item ->
            val destination = AppNavDestination.entries.firstOrNull { it.menuId == item.itemId }
                ?: return@setOnItemSelectedListener false
            showFragment(activity, role, destination)
            true
        }
    }

    fun bind(
        activity: AppCompatActivity,
        bottomNavigationView: BottomNavigationView,
        role: UserRole,
        currentDestination: AppNavDestination
    ) {
        val menuRes = when (role) {
            UserRole.CLIENT -> R.menu.bottom_nav_menu
            UserRole.ARCHITECT -> R.menu.bottom_nav_menu_architect
        }

        bottomNavigationView.menu.clear()
        bottomNavigationView.inflateMenu(menuRes)
        applyIcons(activity, bottomNavigationView, role)
        bottomNavigationView.selectedItemId = currentDestination.menuId

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == currentDestination.menuId) {
                return@setOnItemSelectedListener true
            }
            val destination = AppNavDestination.entries.firstOrNull { it.menuId == item.itemId }
                ?: return@setOnItemSelectedListener false
            val intent = Intent(activity, AppHostActivity::class.java).apply {
                putExtra(AppHostActivity.EXTRA_INITIAL_DESTINATION, destination.name)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            activity.startActivity(intent)
            true
        }
    }

    private fun destinationFor(role: UserRole, itemId: Int): Class<*>? {
        return when (role) {
            UserRole.CLIENT -> {
                when (itemId) {
                    R.id.nav_home -> ClientDashboardActivity::class.java
                    R.id.nav_work -> ClientProjectsActivity::class.java
                    R.id.nav_messages -> MessagesActivity::class.java
                    R.id.nav_profile -> ProfileActivity::class.java
                    else -> null
                }
            }

            UserRole.ARCHITECT -> {
                when (itemId) {
                    R.id.nav_home -> ArchitectDashboardActivity::class.java
                    R.id.nav_work -> ArchitectMatchActivity::class.java
                    R.id.nav_messages -> ArchitectMessagesActivity::class.java
                    R.id.nav_profile -> ProfileActivity::class.java
                    else -> null
                }
            }
        }
    }

    private fun showFragment(
        activity: AppCompatActivity,
        role: UserRole,
        destination: AppNavDestination
    ) {
        val fragment = fragmentFor(role, destination)
        activity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.navHostContainer, fragment, destination.name)
            .commit()
    }

    private fun fragmentFor(role: UserRole, destination: AppNavDestination): Fragment {
        return when (destination) {
            AppNavDestination.HOME -> {
                if (role == UserRole.CLIENT) ClientHomeFragment() else ArchitectHomeFragment()
            }
            AppNavDestination.WORK -> {
                if (role == UserRole.CLIENT) ClientWorkFragment() else ArchitectWorkFragment()
            }
            AppNavDestination.MESSAGES -> {
                if (role == UserRole.CLIENT) ClientMessagesFragment() else ArchitectMessagesFragment()
            }
            AppNavDestination.PROFILE -> ProfileFragment()
        }
    }

    private fun applyIcons(
        activity: AppCompatActivity,
        bottomNavigationView: BottomNavigationView,
        role: UserRole
    ) {
        UiEffects.applyIconifyToMenu(activity, bottomNavigationView.menu, R.id.nav_home, "{md-home}")
        UiEffects.applyIconifyToMenu(activity, bottomNavigationView.menu, R.id.nav_work, "{md-assignment}")
        UiEffects.applyIconifyToMenu(activity, bottomNavigationView.menu, R.id.nav_messages, "{md-chat}")
        UiEffects.applyIconifyToMenu(activity, bottomNavigationView.menu, R.id.nav_profile, "{md-person}")
    }
}

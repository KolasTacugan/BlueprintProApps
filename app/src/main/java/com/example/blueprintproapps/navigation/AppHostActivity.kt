package com.example.blueprintproapps.navigation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.blueprintproapps.R
import com.example.blueprintproapps.auth.AuthSessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class AppHostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = AuthSessionManager.requireSession(this) ?: return
        enableEdgeToEdge()
        setContentView(R.layout.activity_app_host)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val initialDestination = savedInstanceState
            ?.getString(KEY_SELECTED_DESTINATION)
            ?.let { runCatching { AppNavDestination.valueOf(it) }.getOrNull() }
            ?: intent.getStringExtra(EXTRA_INITIAL_DESTINATION)
                ?.let { runCatching { AppNavDestination.valueOf(it) }.getOrNull() }
            ?: AppNavDestination.HOME

        AppNavigator.bindHost(
            activity = this,
            bottomNavigationView = bottomNav,
            role = session.role,
            initialDestination = initialDestination
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val destination = AppNavDestination.entries.firstOrNull { it.menuId == bottomNav.selectedItemId }
        outState.putString(KEY_SELECTED_DESTINATION, destination?.name ?: AppNavDestination.HOME.name)
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val EXTRA_INITIAL_DESTINATION = "initialDestination"
        private const val KEY_SELECTED_DESTINATION = "selectedDestination"
    }
}

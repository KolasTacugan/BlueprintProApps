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
        val initialDestination = if (savedInstanceState == null) {
            intent.getStringExtra(EXTRA_INITIAL_DESTINATION)
                ?.let { runCatching { AppNavDestination.valueOf(it) }.getOrNull() }
                ?: AppNavDestination.HOME
        } else {
            null
        }

        AppNavigator.bindHost(
            activity = this,
            bottomNavigationView = bottomNav,
            role = session.role,
            initialDestination = initialDestination
        )
    }

    companion object {
        const val EXTRA_INITIAL_DESTINATION = "initialDestination"
    }
}

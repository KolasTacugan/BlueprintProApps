package com.example.blueprintproapps.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.blueprintproapps.models.LoginResponse
import com.example.blueprintproapps.network.LoginActivity

enum class UserRole(val prefValue: String) {
    CLIENT("Client"),
    ARCHITECT("Architect");

    companion object {
        fun from(value: String?): UserRole? {
            return when (value?.trim()?.lowercase()) {
                "client" -> CLIENT
                "architect" -> ARCHITECT
                else -> null
            }
        }
    }
}

data class AuthSession(
    val role: UserRole,
    val userId: String
) {
    val clientId: String?
        get() = if (role == UserRole.CLIENT) userId else null

    val architectId: String?
        get() = if (role == UserRole.ARCHITECT) userId else null
}

object AuthSessionManager {
    const val PREFS_NAME = "MyAppPrefs"
    const val EXTRA_SELECTED_ROLE = "selectedRole"

    private const val KEY_USER_TYPE = "userType"
    private const val KEY_CLIENT_ID = "clientId"
    private const val KEY_ARCHITECT_ID = "architectId"
    private const val KEY_REMEMBER_ME = "rememberMe"

    private val authKeys = listOf(
        KEY_USER_TYPE,
        KEY_CLIENT_ID,
        KEY_ARCHITECT_ID,
        KEY_REMEMBER_ME,
        "userId",
        "firstName",
        "lastName",
        "email",
        "phone",
        "profilePhoto",
        "isPro",
        "purchasedBlueprintIds"
    )

    fun getValidSession(context: Context, clearInvalid: Boolean = true): AuthSession? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val role = UserRole.from(prefs.getString(KEY_USER_TYPE, null))
        val userId = when (role) {
            UserRole.CLIENT -> prefs.getString(KEY_CLIENT_ID, null)
            UserRole.ARCHITECT -> prefs.getString(KEY_ARCHITECT_ID, null)
            null -> null
        }?.trim()

        if (role != null && !userId.isNullOrEmpty()) {
            return AuthSession(role, userId)
        }

        if (clearInvalid && hasAnyAuthState(context)) {
            clearAuthData(context)
        }
        return null
    }

    fun saveLoginSession(
        context: Context,
        response: LoginResponse,
        shouldRemember: Boolean
    ): AuthSession? {
        val role = UserRole.from(response.role)
        val userId = response.userId.trim()
        if (role == null || userId.isEmpty()) {
            clearAuthData(context)
            return null
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_CLIENT_ID)
            .remove(KEY_ARCHITECT_ID)
            .putString(KEY_USER_TYPE, role.prefValue)
            .putString(if (role == UserRole.CLIENT) KEY_CLIENT_ID else KEY_ARCHITECT_ID, userId)
            .putBoolean(KEY_REMEMBER_ME, shouldRemember)
            .apply()

        return AuthSession(role, userId)
    }

    fun requireSession(
        activity: Activity,
        requiredRole: UserRole? = null,
        message: String = "Please log in again."
    ): AuthSession? {
        val session = getValidSession(activity)
        if (session == null || (requiredRole != null && session.role != requiredRole)) {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            redirectToLogin(activity)
            return null
        }
        return session
    }

    fun clearAuthData(context: Context) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        authKeys.forEach { key -> editor.remove(key) }
        editor.apply()
    }

    fun logout(activity: Activity) {
        clearAuthData(activity)
        redirectToLogin(activity)
    }

    fun redirectToLogin(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
        activity.startActivity(intent)
        activity.finish()
    }

    private fun hasAnyAuthState(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return authKeys.any { prefs.contains(it) }
    }
}

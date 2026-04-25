package com.example.blueprintproapps.network

import android.content.Context
import android.graphics.Color
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.LoginRequest
import com.example.blueprintproapps.models.LoginResponse
import com.example.blueprintproapps.utils.ParallaxEffect
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.MaterialIcons
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class LoginActivity : AppCompatActivity() {

    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var loginButton: MaterialButton
    private lateinit var loginProgressLayout: View
    private lateinit var parallaxEffect: ParallaxEffect

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Session Check
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userType = prefs.getString("userType", null)
        if (userType != null) {
            navigateToDashboard(userType)
            return
        }

        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginScrollView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Views
        val emailInput = findViewById<TextInputEditText>(R.id.edtEmail)
        val passwordInput = findViewById<TextInputEditText>(R.id.edtPassword)
        emailLayout = findViewById(R.id.layoutEmail)
        passwordLayout = findViewById(R.id.layoutPassword)
        loginButton = findViewById(R.id.btnLogin)
        loginProgressLayout = findViewById(R.id.loginProgressLayout)
        val rememberMe = findViewById<MaterialCheckBox>(R.id.rememberMe)
        val registerLink = findViewById<TextView>(R.id.tvRegister)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)
        val background = findViewById<View>(R.id.ivBackground)

        val brandName = findViewById<View>(R.id.tvBrandName)
        val logo = findViewById<ImageView>(R.id.ivLogo)

        // Effects
        parallaxEffect = ParallaxEffect(this)
        parallaxEffect.attach(background)
        
        UiEffects.applyFocusGlow(emailLayout, emailInput)
        UiEffects.applyFocusGlow(passwordLayout, passwordInput)
        UiEffects.applyBlueprintBranding(brandName)
        UiEffects.applyPressScaleEffect(loginButton)

        setupIcons()
        applyEntranceAnimations()
        setupValidation(emailInput, passwordInput)

        forgotPassword.setOnClickListener { startActivity(Intent(this, ChangePasswordActivity::class.java)) }
        registerLink.setOnClickListener { startActivity(Intent(this, ChooseRoleActivity::class.java)) }
        loginButton.setOnClickListener {
            val em = emailInput.text.toString().trim()
            val pw = passwordInput.text.toString().trim()
            if (validateInputs(em, pw)) performLogin(em, pw, rememberMe.isChecked) else vibrateError()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::parallaxEffect.isInitialized) {
            parallaxEffect.detach()
        }
    }

    private fun setupIcons() {
        emailLayout.startIconDrawable = IconDrawable(this, MaterialIcons.md_email).colorRes(R.color.primary).sizeDp(20)
        passwordLayout.startIconDrawable = IconDrawable(this, MaterialIcons.md_lock).colorRes(R.color.primary).sizeDp(20)
    }

    private fun applyEntranceAnimations() {
        val views = listOf(
            findViewById<View>(R.id.ivLogo),
            findViewById<View>(R.id.tvBrandName),
            findViewById<View>(R.id.tvTagline),
            findViewById<View>(R.id.loginTitle),
            emailLayout,
            passwordLayout,
            findViewById<View>(R.id.registerLinkContainer)
        )
        UiEffects.applyCascadingEntrance(views)
    }

    private fun setupValidation(eI: TextInputEditText, pI: TextInputEditText) {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                emailLayout.error = null; passwordLayout.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        eI.addTextChangedListener(watcher)
        pI.addTextChangedListener(watcher)
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true
        if (email.isEmpty()) { emailLayout.error = "Required"; isValid = false }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailLayout.error = "Invalid format"; isValid = false }
        if (password.isEmpty()) { passwordLayout.error = "Required"; isValid = false }
        return isValid
    }

    private fun performLogin(email: String, password: String, shouldRemember: Boolean) {
        setLoading(true)
        val request = LoginRequest(email = email, password = password)
        ApiClient.instance.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                setLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let { 
                        saveSession(it, shouldRemember)
                        showSnackbar("Welcome, ${it.email}")
                        navigateToDashboard(it.role)
                    }
                } else {
                    vibrateError(); showSnackbar("Invalid credentials", true)
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                setLoading(false); vibrateError(); showSnackbar("Error: ${t.message}", true)
            }
        })
    }

    private fun setLoading(isLoading: Boolean) {
        loginButton.isEnabled = !isLoading
        loginButton.text = if (isLoading) "" else "Sign In"
        loginProgressLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
        if (isError) {
            snackbar.setBackgroundTint(resources.getColor(R.color.error, theme))
            snackbar.setTextColor(Color.WHITE)
        }
        snackbar.show()
    }

    private fun saveSession(response: LoginResponse, shouldRemember: Boolean) {
        val editor = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit()
        val role = response.role.lowercase()
        editor.putString("userType", if (role == "client") "Client" else "Architect")
        editor.putString(if (role == "client") "clientId" else "architectId", response.userId)
        editor.putBoolean("rememberMe", shouldRemember)
        editor.apply()
    }

    private fun navigateToDashboard(userType: String) {
        val target = if (userType.equals("Client", ignoreCase = true)) ClientDashboardActivity::class.java else ArchitectDashboardActivity::class.java
        startActivity(Intent(this, target)); finish()
    }

    private fun vibrateError() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(200)
        }
    }
}

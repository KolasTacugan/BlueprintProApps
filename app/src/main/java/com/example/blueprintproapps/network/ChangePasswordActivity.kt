package com.example.blueprintproapps.network

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ChangePasswordRequest
import com.example.blueprintproapps.models.ChangePasswordResponse
import com.example.blueprintproapps.utils.ParallaxEffect
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.MaterialIcons
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var verifyButton: MaterialButton
    private lateinit var changeButton: MaterialButton
    private lateinit var verifyProgressLayout: View
    private lateinit var changeProgressLayout: View
    private lateinit var emailLayout: TextInputLayout
    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var parallaxEffect: ParallaxEffect

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.resetScrollView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailInput = findViewById<TextInputEditText>(R.id.etEmail)
        val newPasswordInput = findViewById<TextInputEditText>(R.id.etNewPassword)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        verifyButton = findViewById(R.id.btnVerifyEmail)
        changeButton = findViewById(R.id.btnChangePassword)
        verifyProgressLayout = findViewById(R.id.verifyProgressLayout)
        changeProgressLayout = findViewById(R.id.changeProgressLayout)
        val passwordSection = findViewById<LinearLayout>(R.id.passwordSection)
        val background = findViewById<View>(R.id.ivBackground)

        emailLayout = findViewById(R.id.emailLayout)
        newPasswordLayout = findViewById(R.id.newPasswordLayout)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)

        // Effects
        parallaxEffect = ParallaxEffect(this)
        parallaxEffect.attach(background)

        UiEffects.applyFocusGlow(emailLayout, emailInput)
        UiEffects.applyFocusGlow(newPasswordLayout, newPasswordInput)
        UiEffects.applyFocusGlow(confirmPasswordLayout, confirmPasswordInput)

        setupIcons()
        setupValidation(listOf(emailInput, newPasswordInput, confirmPasswordInput))
        
        // Cascading Entrance
        val logo = findViewById<View>(R.id.ivLogo)
        UiEffects.applyCascadingEntrance(listOf(logo, emailLayout))

        verifyButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) { emailLayout.error = "Required"; vibrateError() }
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailLayout.error = "Invalid format"; vibrateError() }
            else performVerification(email, passwordSection)
        }

        changeButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val newPw = newPasswordInput.text.toString().trim()
            val confPw = confirmPasswordInput.text.toString().trim()
            if (validate(newPw, confPw)) performChange(email, newPw) else vibrateError()
        }

        findViewById<View>(R.id.loginLinkContainer).setOnClickListener {
            val intent = android.content.Intent(this, LoginActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::parallaxEffect.isInitialized) {
            parallaxEffect.detach()
        }
    }

    private fun setupIcons() {
        val color = R.color.primary
        emailLayout.startIconDrawable = IconDrawable(this, MaterialIcons.md_email).colorRes(color).sizeDp(20)
        newPasswordLayout.startIconDrawable = IconDrawable(this, MaterialIcons.md_lock).colorRes(color).sizeDp(20)
        confirmPasswordLayout.startIconDrawable = IconDrawable(this, MaterialIcons.md_lock).colorRes(color).sizeDp(20)
    }

    private fun setupValidation(inputs: List<TextInputEditText>) {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                emailLayout.error = null; newPasswordLayout.error = null; confirmPasswordLayout.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        inputs.forEach { it.addTextChangedListener(watcher) }
    }

    private fun validate(p: String, cp: String): Boolean {
        var valid = true
        if (p.isEmpty()) { newPasswordLayout.error = "Required"; valid = false }
        else if (p.length < 6) { newPasswordLayout.error = "Min 6 characters"; valid = false }
        if (cp.isEmpty()) { confirmPasswordLayout.error = "Required"; valid = false }
        else if (p != cp) { confirmPasswordLayout.error = "Mismatch"; valid = false }
        return valid
    }

    private fun performVerification(email: String, section: View) {
        setVerifyLoading(true)
        val request = ChangePasswordRequest(email = email, newPassword = "dummy")
        ApiClient.instance.changePassword(request).enqueue(object : Callback<ChangePasswordResponse> {
            override fun onResponse(call: Call<ChangePasswordResponse>, response: Response<ChangePasswordResponse>) {
                setVerifyLoading(false)
                if (response.code() == 404) { vibrateError(); emailLayout.error = "Not found" }
                else { section.visibility = View.VISIBLE; showSnackbar("Email verified") }
            }
            override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                setVerifyLoading(false); vibrateError(); showSnackbar("Error: ${t.message}", true)
            }
        })
    }

    private fun performChange(email: String, pw: String) {
        setChangeLoading(true)
        val request = ChangePasswordRequest(email = email, newPassword = pw)
        ApiClient.instance.changePassword(request).enqueue(object : Callback<ChangePasswordResponse> {
            override fun onResponse(call: Call<ChangePasswordResponse>, response: Response<ChangePasswordResponse>) {
                setChangeLoading(false)
                if (response.isSuccessful) { showSnackbar("Password reset success!"); finish() }
                else { vibrateError(); showSnackbar("Reset failed", true) }
            }
            override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                setChangeLoading(false); vibrateError(); showSnackbar("Error: ${t.message}", true)
            }
        })
    }

    private fun setVerifyLoading(isLoading: Boolean) {
        verifyButton.isEnabled = !isLoading
        verifyButton.text = if (isLoading) "" else "Verify Email"
        verifyProgressLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setChangeLoading(isLoading: Boolean) {
        changeButton.isEnabled = !isLoading
        changeButton.text = if (isLoading) "" else "Update Password"
        changeProgressLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
        if (isError) {
            snackbar.setBackgroundTint(resources.getColor(R.color.error, theme))
            snackbar.setTextColor(Color.WHITE)
        }
        snackbar.show()
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

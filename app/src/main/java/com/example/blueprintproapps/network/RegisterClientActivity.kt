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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.RegisterRequest
import com.example.blueprintproapps.models.RegisterResponse
import com.example.blueprintproapps.utils.ParallaxEffect
import com.example.blueprintproapps.utils.UiEffects
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.MaterialIcons
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterClientActivity : AppCompatActivity() {

    private lateinit var registerButton: MaterialButton
    private lateinit var registerProgressLayout: View
    private lateinit var layouts: List<TextInputLayout>
    private lateinit var parallaxEffect: ParallaxEffect

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_client)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_scroll)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Views
        val firstNameInput = findViewById<TextInputEditText>(R.id.firstNameInput)
        val lastNameInput = findViewById<TextInputEditText>(R.id.lastNameInput)
        val phoneInput = findViewById<TextInputEditText>(R.id.phoneInput)
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        
        val firstNameLayout = findViewById<TextInputLayout>(R.id.firstNameLayout)
        val lastNameLayout = findViewById<TextInputLayout>(R.id.lastNameLayout)
        val phoneLayout = findViewById<TextInputLayout>(R.id.phoneLayout)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordLayout)
        val confirmPasswordLayout = findViewById<TextInputLayout>(R.id.confirmPasswordLayout)
        
        layouts = listOf(firstNameLayout, lastNameLayout, phoneLayout, emailLayout, passwordLayout, confirmPasswordLayout)
        registerButton = findViewById(R.id.registerButton)
        registerProgressLayout = findViewById(R.id.registerProgressLayout)
        val loginLink = findViewById<TextView>(R.id.loginLink)
        val background = findViewById<View>(R.id.ivBackground)

        val title = findViewById<View>(R.id.tvRegisterTitle)
        
        // Effects
        parallaxEffect = ParallaxEffect(this)
        parallaxEffect.attach(background)

        UiEffects.applyBlueprintBranding(title)
        
        val registerCard = findViewById<View>(R.id.registerCard) 
        val viewsToAnimate = listOf(title, registerCard) 
        UiEffects.applyCascadingEntrance(viewsToAnimate.filterNotNull())

        layouts.zip(listOf(firstNameInput, lastNameInput, phoneInput, emailInput, passwordInput, confirmPasswordInput)).forEach {
            UiEffects.applyFocusGlow(it.first, it.second)
        }

        val strengthView = findViewById<View>(R.id.passwordStrength)
        UiEffects.setupPasswordStrength(
            passwordInput,
            strengthView.findViewById(R.id.strengthBar1),
            strengthView.findViewById(R.id.strengthBar2),
            strengthView.findViewById(R.id.strengthBar3),
            strengthView.findViewById(R.id.strengthText)
        )

        setupIcons()
        setupValidation(listOf(firstNameInput, lastNameInput, phoneInput, emailInput, passwordInput, confirmPasswordInput))

        val sharedPrefs = getSharedPreferences("BlueprintPrefs", MODE_PRIVATE)
        val role = sharedPrefs.getString("user_role", "Client")

        registerButton.setOnClickListener {
            if (validate(firstNameInput, lastNameInput, phoneInput, emailInput, passwordInput, confirmPasswordInput)) {
                performRegistration(
                    firstNameInput.text.toString().trim(),
                    lastNameInput.text.toString().trim(),
                    phoneInput.text.toString().trim(),
                    emailInput.text.toString().trim(),
                    passwordInput.text.toString().trim(),
                    role ?: "Client"
                )
            } else {
                vibrateError()
            }
        }

        loginLink.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)); finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        parallaxEffect.detach()
    }

    private fun setupIcons() {
        val color = R.color.primary
        layouts[0].startIconDrawable = IconDrawable(this, MaterialIcons.md_person).colorRes(color).sizeDp(20)
        layouts[1].startIconDrawable = IconDrawable(this, MaterialIcons.md_person).colorRes(color).sizeDp(20)
        layouts[2].startIconDrawable = IconDrawable(this, MaterialIcons.md_phone).colorRes(color).sizeDp(20)
        layouts[3].startIconDrawable = IconDrawable(this, MaterialIcons.md_email).colorRes(color).sizeDp(20)
        layouts[4].startIconDrawable = IconDrawable(this, MaterialIcons.md_lock).colorRes(color).sizeDp(20)
        layouts[5].startIconDrawable = IconDrawable(this, MaterialIcons.md_lock).colorRes(color).sizeDp(20)
    }

    private fun setupValidation(inputs: List<TextInputEditText>) {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layouts.forEach { it.error = null }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        inputs.forEach { it.addTextChangedListener(watcher) }
    }

    private fun validate(f: TextInputEditText, l: TextInputEditText, p: TextInputEditText, e: TextInputEditText, pw: TextInputEditText, cpw: TextInputEditText): Boolean {
        var valid = true
        if (f.text.isNullOrBlank()) { layouts[0].error = "Required"; valid = false }
        if (l.text.isNullOrBlank()) { layouts[1].error = "Required"; valid = false }
        if (p.text.isNullOrBlank()) { layouts[2].error = "Required"; valid = false }
        if (e.text.isNullOrBlank()) { layouts[3].error = "Required"; valid = false }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e.text!!).matches()) { layouts[3].error = "Invalid"; valid = false }
        if (pw.text.isNullOrBlank()) { layouts[4].error = "Required"; valid = false }
        if (cpw.text.isNullOrBlank()) { layouts[5].error = "Required"; valid = false }
        else if (pw.text.toString() != cpw.text.toString()) { layouts[5].error = "Mismatch"; valid = false }
        return valid
    }

    private fun performRegistration(f: String, l: String, p: String, e: String, pw: String, r: String) {
        setLoading(true)
        val request = RegisterRequest(f, l, p, e, pw, r)
        ApiClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                setLoading(false)
                if (response.isSuccessful) {
                    showSnackbar("Success! You can now login.")
                    startActivity(Intent(this@RegisterClientActivity, LoginActivity::class.java))
                    finish()
                } else {
                    vibrateError(); showSnackbar("Registration failed.", true)
                }
            }
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                setLoading(false); vibrateError(); showSnackbar("Error: ${t.message}", true)
            }
        })
    }

    private fun setLoading(isLoading: Boolean) {
        registerButton.isEnabled = !isLoading
        registerButton.text = if (isLoading) "" else "Create Account"
        registerProgressLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(findViewById(R.id.register_scroll), message, Snackbar.LENGTH_LONG)
        if (isError) {
            snackbar.setBackgroundTint(resources.getColor(R.color.error, theme))
            snackbar.setTextColor(android.graphics.Color.WHITE)
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

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
import android.widget.ArrayAdapter
import android.widget.Spinner
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

class RegisterArchitectActivity : AppCompatActivity() {

    private lateinit var registerButton: MaterialButton
    private lateinit var registerProgressLayout: View
    private lateinit var layouts: List<TextInputLayout>
    private lateinit var parallaxEffect: ParallaxEffect

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_architect)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_scroll)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Views
        val firstNameInput = findViewById<TextInputEditText>(R.id.firstNameInput)
        val lastNameInput = findViewById<TextInputEditText>(R.id.lastNameInput)
        val phoneNumberInput = findViewById<TextInputEditText>(R.id.phoneNumberInput)
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        val licenseNoInput = findViewById<TextInputEditText>(R.id.licenseNoInput)
        
        val firstNameLayout = findViewById<TextInputLayout>(R.id.firstNameLayout)
        val lastNameLayout = findViewById<TextInputLayout>(R.id.lastNameLayout)
        val phoneL = findViewById<TextInputLayout>(R.id.phoneNumberLayout)
        val emailL = findViewById<TextInputLayout>(R.id.emailLayout)
        val passL = findViewById<TextInputLayout>(R.id.passwordLayout)
        val confPassL = findViewById<TextInputLayout>(R.id.confirmPasswordLayout)
        val licL = findViewById<TextInputLayout>(R.id.licenseNoLayout)
        
        layouts = listOf(firstNameLayout, lastNameLayout, phoneL, emailL, passL, confPassL, licL)
        registerButton = findViewById(R.id.registerButtonArchitect)
        registerProgressLayout = findViewById(R.id.registerProgressLayoutArchitect)
        val loginLinkContainer = findViewById<View>(R.id.loginLinkContainer)
        val background = findViewById<View>(R.id.ivBackground)
        
        val logo = findViewById<View>(R.id.ivLogo)
        val registerTitle = findViewById<View>(R.id.tvRegisterTitle) 
        
        // Effects
        parallaxEffect = ParallaxEffect(this)
        parallaxEffect.attach(background)
        
        // Cascading Entrance
        UiEffects.applyCascadingEntrance(listOf(logo, registerTitle, firstNameLayout, lastNameLayout, loginLinkContainer))

        layouts.zip(listOf(firstNameInput, lastNameInput, phoneNumberInput, emailInput, passwordInput, confirmPasswordInput, licenseNoInput)).forEach {
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
        setupSpinners()
        setupValidation(listOf(firstNameInput, lastNameInput, phoneNumberInput, emailInput, passwordInput, confirmPasswordInput, licenseNoInput))

        val sharedPrefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val role = sharedPrefs.getString("userType", "Architect")

        registerButton.setOnClickListener {
            if (validate(firstNameInput, lastNameInput, phoneNumberInput, emailInput, passwordInput, confirmPasswordInput, licenseNoInput)) {
                performRegistration(
                    firstNameInput.text.toString().trim(),
                    lastNameInput.text.toString().trim(),
                    phoneNumberInput.text.toString().trim(),
                    emailInput.text.toString().trim(),
                    passwordInput.text.toString().trim(),
                    role ?: "Architect",
                    licenseNoInput.text.toString().trim(),
                    findViewById<Spinner>(R.id.spinnerStyle).selectedItem.toString(),
                    findViewById<Spinner>(R.id.spinnerSpecialization).selectedItem.toString(),
                    findViewById<Spinner>(R.id.spinnerLocation).selectedItem.toString(),
                    findViewById<Spinner>(R.id.spinnerBudget).selectedItem.toString()
                )
            } else { vibrateError() }
        }

        loginLinkContainer.setOnClickListener { 
            startActivity(Intent(this, LoginActivity::class.java))
            finish() 
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        parallaxEffect.detach()
    }

    private fun setupIcons() {
        val color = android.graphics.Color.parseColor("#8AADF4")
        UiEffects.applyIconify(layouts[0].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-person", color)
        UiEffects.applyIconify(layouts[1].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-person", color)
        UiEffects.applyIconify(layouts[2].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-phone", color)
        UiEffects.applyIconify(layouts[3].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-email", color)
        UiEffects.applyIconify(layouts[4].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-lock", color)
        UiEffects.applyIconify(layouts[5].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-lock", color)
        UiEffects.applyIconify(layouts[6].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-assignment-ind", color)
    }

    private fun setupSpinners() {
        val styleS = findViewById<Spinner>(R.id.spinnerStyle)
        val budgetS = findViewById<Spinner>(R.id.spinnerBudget)
        val locS = findViewById<Spinner>(R.id.spinnerLocation)
        val specS = findViewById<Spinner>(R.id.spinnerSpecialization)

        fun fill(s: Spinner, items: List<String>) {
            s.adapter = ArrayAdapter(this, R.layout.spinner_item, items)
        }
        fill(styleS, listOf("Modern", "Traditional", "Contemporary", "Minimalist"))
        fill(budgetS, listOf("Low", "Medium", "High"))
        fill(locS, listOf("Urban", "Suburban", "Rural"))
        fill(specS, listOf("Residential", "Commercial", "Industrial"))
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

    private fun validate(f: TextInputEditText, l: TextInputEditText, p: TextInputEditText, e: TextInputEditText, pw: TextInputEditText, cpw: TextInputEditText, lic: TextInputEditText): Boolean {
        var v = true
        if (f.text.isNullOrBlank()) { layouts[0].error = "Required"; v = false }
        if (l.text.isNullOrBlank()) { layouts[1].error = "Required"; v = false }
        if (p.text.isNullOrBlank()) { layouts[2].error = "Required"; v = false }
        if (e.text.isNullOrBlank()) { layouts[3].error = "Required"; v = false }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e.text!!).matches()) { layouts[3].error = "Invalid"; v = false }
        if (pw.text.isNullOrBlank()) { layouts[4].error = "Required"; v = false }
        if (cpw.text.isNullOrBlank()) { layouts[5].error = "Required"; v = false }
        else if (pw.text.toString() != cpw.text.toString()) { layouts[5].error = "Mismatch"; v = false }
        if (lic.text.isNullOrBlank()) { layouts[6].error = "Required"; v = false }
        return v
    }

    private fun performRegistration(f: String, l: String, p: String, e: String, pw: String, r: String, lic: String, s: String, sp: String, loc: String, bud: String) {
        setLoading(true)
        val request = RegisterRequest(f, l, p, e, pw, r, lic, s, sp, loc, bud)
        ApiClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                setLoading(false)
                if (response.isSuccessful) {
                    showSnackbar("Success! You can now login.")
                    startActivity(Intent(this@RegisterArchitectActivity, LoginActivity::class.java))
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
        registerButton.text = if (isLoading) "" else "Register"
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

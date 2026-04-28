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
import com.example.blueprintproapps.auth.AuthSessionManager
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

    private var currentStep = 2
    private val MAX_STEPS = 3
    
    private lateinit var step1Container: View
    private lateinit var step2Container: View
    private lateinit var btnBack: MaterialButton
    private lateinit var stepProgress: com.google.android.material.progressindicator.LinearProgressIndicator
    private lateinit var tvStepIndicator: TextView

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
        val loginLinkContainer = findViewById<View>(R.id.loginLinkContainer)
        val background = findViewById<View>(R.id.ivBackground)

        val logo = findViewById<View>(R.id.ivLogo)
        val registerTitle = findViewById<View>(R.id.tvRegisterTitle)

        step1Container = findViewById(R.id.step1Container)
        step2Container = findViewById(R.id.step2Container)
        btnBack = findViewById(R.id.btnBack)
        stepProgress = findViewById(R.id.stepProgress)
        tvStepIndicator = findViewById(R.id.tvStepIndicator)

        // Effects
        parallaxEffect = ParallaxEffect(this)
        parallaxEffect.attach(background)
        
        val allInputs = listOf<android.widget.EditText>(firstNameInput, lastNameInput, phoneInput, emailInput, passwordInput, confirmPasswordInput)

        UiEffects.applyCascadingEntrance(listOf(logo, registerTitle, tvStepIndicator, stepProgress, firstNameLayout, lastNameLayout, phoneLayout, emailLayout, loginLinkContainer))

        layouts.zip(allInputs).forEach {
            UiEffects.applyFocusGlow(it.first, it.second)
        }



        setupIcons()
        setupValidation(allInputs)

        val role = intent.getStringExtra(AuthSessionManager.EXTRA_SELECTED_ROLE) ?: "Client"

        btnBack.setOnClickListener {
            if (currentStep > 2) {
                val outView = getStepContainer(currentStep)
                currentStep--
                val inView = getStepContainer(currentStep)
                updateStepUI()
                UiEffects.applyStepTransition(outView, inView, forward = false)
            } else {
                finish()
            }
        }
        
        // initialize UI state
        updateStepUI()

        registerButton.setOnClickListener {
            if (validateStep(currentStep)) {
                if (currentStep < MAX_STEPS) {
                    val outView = getStepContainer(currentStep)
                    currentStep++
                    val inView = getStepContainer(currentStep)
                    updateStepUI()
                    UiEffects.applyStepTransition(outView, inView, forward = true)
                } else {
                    performRegistration(
                        firstNameInput.text.toString().trim(),
                        lastNameInput.text.toString().trim(),
                        phoneInput.text.toString().trim(),
                        emailInput.text.toString().trim(),
                        passwordInput.text.toString().trim(),
                        role ?: "Client"
                    )
                }
            } else {
                vibrateError()
            }
        }

        loginLinkContainer.setOnClickListener { 
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
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
        val color = android.graphics.Color.parseColor("#8AADF4")
        UiEffects.applyIconify(findViewById<TextInputLayout>(R.id.firstNameLayout).findViewById(com.google.android.material.R.id.text_input_start_icon), "md-person", color)
        UiEffects.applyIconify(findViewById<TextInputLayout>(R.id.lastNameLayout).findViewById(com.google.android.material.R.id.text_input_start_icon), "md-person", color)
        UiEffects.applyIconify(findViewById<TextInputLayout>(R.id.phoneLayout).findViewById(com.google.android.material.R.id.text_input_start_icon), "md-phone", color)
        UiEffects.applyIconify(findViewById<TextInputLayout>(R.id.emailLayout).findViewById(com.google.android.material.R.id.text_input_start_icon), "md-email", color)
        UiEffects.applyIconify(findViewById<TextInputLayout>(R.id.passwordLayout).findViewById(com.google.android.material.R.id.text_input_start_icon), "md-lock", color)
        UiEffects.applyIconify(findViewById<TextInputLayout>(R.id.confirmPasswordLayout).findViewById(com.google.android.material.R.id.text_input_start_icon), "md-lock", color)
    }

    private fun setupValidation(inputs: List<android.widget.EditText>) {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layouts.forEach { it.error = null }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        inputs.forEach { it.addTextChangedListener(watcher) }
    }

    private fun validateStep(step: Int): Boolean {
        var valid = true
        val f = findViewById<TextInputEditText>(R.id.firstNameInput)
        val l = findViewById<TextInputEditText>(R.id.lastNameInput)
        val p = findViewById<TextInputEditText>(R.id.phoneInput)
        val e = findViewById<TextInputEditText>(R.id.emailInput)
        val pw = findViewById<TextInputEditText>(R.id.passwordInput)
        val cpw = findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        
        if (step == 2) {
            if (f.text.isNullOrBlank()) { layouts[0].error = "Required"; valid = false }
            if (l.text.isNullOrBlank()) { layouts[1].error = "Required"; valid = false }
            if (p.text.isNullOrBlank()) { layouts[2].error = "Required"; valid = false }
            else if (!android.util.Patterns.PHONE.matcher(p.text!!).matches()) { layouts[2].error = "Invalid format"; valid = false }
            if (e.text.isNullOrBlank()) { layouts[3].error = "Required"; valid = false }
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e.text!!).matches()) { layouts[3].error = "Invalid format"; valid = false }
        } else if (step == 3) {
            if (pw.text.isNullOrBlank()) { layouts[4].error = "Required"; valid = false }
            else if (pw.text!!.length < 6) { layouts[4].error = "Min 6 characters"; valid = false }
            if (cpw.text.isNullOrBlank()) { layouts[5].error = "Required"; valid = false }
            else if (pw.text.toString() != cpw.text.toString()) { layouts[5].error = "Mismatch"; valid = false }
        }
        return valid
    }

    private fun getStepContainer(step: Int): View {
        return when (step) {
            2 -> step1Container
            3 -> step2Container
            else -> step1Container
        }
    }

    private fun updateStepUI() {
        stepProgress.max = MAX_STEPS
        stepProgress.progress = currentStep
        tvStepIndicator.text = "Step $currentStep of $MAX_STEPS: ${if (currentStep == 2) "Profile Details" else "Security"}"
        
        btnBack.visibility = View.VISIBLE
        registerButton.text = if (currentStep == MAX_STEPS) "Create Account" else "Next Step"
    }

    private fun performRegistration(f: String, l: String, p: String, e: String, pw: String, r: String) {
        setLoading(true)
        val request = RegisterRequest(
            email = e,
            password = pw,
            firstName = f,
            lastName = l,
            phoneNumber = p,
            role = r
        )
        ApiClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                setLoading(false)
                if (response.isSuccessful) {
                    AuthSessionManager.clearAuthData(this@RegisterClientActivity)
                    showSnackbar("Success! You can now login.")
                    val intent = Intent(this@RegisterClientActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
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
        btnBack.isEnabled = !isLoading
        registerButton.text = if (isLoading) "" else if (currentStep == MAX_STEPS) "Create Account" else "Next Step"
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

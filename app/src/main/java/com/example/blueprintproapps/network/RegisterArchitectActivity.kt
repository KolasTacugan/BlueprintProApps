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

    private var currentStep = 2
    private val MAX_STEPS = 4
    
    private lateinit var step1Container: View
    private lateinit var step2Container: View
    private lateinit var step3Container: View
    private lateinit var btnBack: MaterialButton
    private lateinit var stepProgress: com.google.android.material.progressindicator.LinearProgressIndicator
    private lateinit var tvStepIndicator: TextView

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
        
        val styleL = findViewById<TextInputLayout>(R.id.styleLayout)
        val budgetL = findViewById<TextInputLayout>(R.id.budgetLayout)
        val locationL = findViewById<TextInputLayout>(R.id.locationLayout)
        val specL = findViewById<TextInputLayout>(R.id.specializationLayout)
        
        layouts = listOf(firstNameLayout, lastNameLayout, phoneL, emailL, passL, confPassL, licL, styleL, budgetL, locationL, specL)
        registerButton = findViewById(R.id.registerButtonArchitect)
        registerProgressLayout = findViewById(R.id.registerProgressLayoutArchitect)
        val loginLinkContainer = findViewById<View>(R.id.loginLinkContainer)
        val background = findViewById<View>(R.id.ivBackground)
        
        val logo = findViewById<View>(R.id.ivLogo)
        val registerTitle = findViewById<View>(R.id.tvRegisterTitle) 
        
        step1Container = findViewById(R.id.step1Container)
        step2Container = findViewById(R.id.step2Container)
        step3Container = findViewById(R.id.step3Container)
        btnBack = findViewById(R.id.btnBackArchitect)
        stepProgress = findViewById(R.id.stepProgress)
        tvStepIndicator = findViewById(R.id.tvStepIndicator)
        
        // Effects
        parallaxEffect = ParallaxEffect(this)
        parallaxEffect.attach(background)
        
        // Cascading Entrance
        UiEffects.applyCascadingEntrance(listOf(logo, registerTitle, tvStepIndicator, stepProgress, firstNameLayout, lastNameLayout, phoneL, emailL, loginLinkContainer))

        val allInputs = listOf<android.widget.EditText>(firstNameInput, lastNameInput, phoneNumberInput, emailInput, passwordInput, confirmPasswordInput, licenseNoInput,
            findViewById(R.id.autoStyle), findViewById(R.id.autoBudget), findViewById(R.id.autoLocation), findViewById(R.id.autoSpecialization))

        layouts.zip(allInputs).forEach {
            UiEffects.applyFocusGlow(it.first, it.second)
        }



        setupIcons()
        setupSpinners()
        setupValidation(allInputs)

        val sharedPrefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val role = sharedPrefs.getString("userType", "Architect")

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
                        phoneNumberInput.text.toString().trim(),
                        emailInput.text.toString().trim(),
                        passwordInput.text.toString().trim(),
                        role ?: "Architect",
                        licenseNoInput.text.toString().trim(),
                        findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.autoStyle).text.toString(),
                        findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.autoSpecialization).text.toString(),
                        findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.autoLocation).text.toString(),
                        findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.autoBudget).text.toString()
                    )
                }
            } else { vibrateError() }
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
        UiEffects.applyIconify(layouts[0].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-person", color)
        UiEffects.applyIconify(layouts[1].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-person", color)
        UiEffects.applyIconify(layouts[2].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-phone", color)
        UiEffects.applyIconify(layouts[3].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-email", color)
        UiEffects.applyIconify(layouts[4].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-lock", color)
        UiEffects.applyIconify(layouts[5].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-lock", color)
        UiEffects.applyIconify(layouts[6].findViewById(com.google.android.material.R.id.text_input_start_icon), "md-assignment-ind", color)
    }

    private fun setupSpinners() {
        val styleS = findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.autoStyle)
        val budgetS = findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.autoBudget)
        val locS = findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.autoLocation)
        val specS = findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.autoSpecialization)

        fun fill(s: com.google.android.material.textfield.MaterialAutoCompleteTextView, items: List<String>) {
            val adapter = ArrayAdapter(this, R.layout.dropdown_item, items)
            s.setAdapter(adapter)
        }
        fill(styleS, listOf("Modern", "Traditional", "Contemporary", "Minimalist"))
        fill(budgetS, listOf("Low", "Medium", "High"))
        fill(locS, listOf("Urban", "Suburban", "Rural"))
        fill(specS, listOf("Residential", "Commercial", "Industrial"))
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
        var v = true
        val f = findViewById<TextInputEditText>(R.id.firstNameInput)
        val l = findViewById<TextInputEditText>(R.id.lastNameInput)
        val p = findViewById<TextInputEditText>(R.id.phoneNumberInput)
        val e = findViewById<TextInputEditText>(R.id.emailInput)
        val pw = findViewById<TextInputEditText>(R.id.passwordInput)
        val cpw = findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        val lic = findViewById<TextInputEditText>(R.id.licenseNoInput)
        
        if (step == 2) {
            if (f.text.isNullOrBlank()) { layouts[0].error = "Required"; v = false }
            if (l.text.isNullOrBlank()) { layouts[1].error = "Required"; v = false }
            if (p.text.isNullOrBlank()) { layouts[2].error = "Required"; v = false }
            else if (!android.util.Patterns.PHONE.matcher(p.text!!).matches()) { layouts[2].error = "Invalid format"; v = false }
            if (e.text.isNullOrBlank()) { layouts[3].error = "Required"; v = false }
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e.text!!).matches()) { layouts[3].error = "Invalid format"; v = false }
        } else if (step == 3) {
            if (pw.text.isNullOrBlank()) { layouts[4].error = "Required"; v = false }
            else if (pw.text!!.length < 6) { layouts[4].error = "Min 6 characters"; v = false }
            if (cpw.text.isNullOrBlank()) { layouts[5].error = "Required"; v = false }
            else if (pw.text.toString() != cpw.text.toString()) { layouts[5].error = "Mismatch"; v = false }
        } else if (step == 4) {
            val s = findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.autoStyle)
            val b = findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.autoBudget)
            val loc = findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.autoLocation)
            val spec = findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.autoSpecialization)
            
            if (lic.text.isNullOrBlank()) { layouts[6].error = "Required"; v = false }
            if (s.text.isNullOrBlank()) { findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.styleLayout).error = "Required"; v = false }
            if (b.text.isNullOrBlank()) { findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.budgetLayout).error = "Required"; v = false }
            if (loc.text.isNullOrBlank()) { findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.locationLayout).error = "Required"; v = false }
            if (spec.text.isNullOrBlank()) { findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.specializationLayout).error = "Required"; v = false }
        }
        return v
    }

    private fun getStepContainer(step: Int): View {
        return when (step) {
            2 -> step1Container
            3 -> step2Container
            4 -> step3Container
            else -> step1Container
        }
    }

    private fun updateStepUI() {
        stepProgress.max = MAX_STEPS
        stepProgress.progress = currentStep
        tvStepIndicator.text = "Step $currentStep of $MAX_STEPS: " + when(currentStep) {
            2 -> "Profile Details"
            3 -> "Security"
            else -> "Professional Credentials"
        }
        
        btnBack.visibility = View.VISIBLE
        registerButton.text = if (currentStep == MAX_STEPS) "Complete Registration" else "Next Step"
    }

    private fun performRegistration(f: String, l: String, p: String, e: String, pw: String, r: String, lic: String, s: String, sp: String, loc: String, bud: String) {
        setLoading(true)
        val request = RegisterRequest(
            email = e,
            password = pw,
            firstName = f,
            lastName = l,
            phoneNumber = p,
            role = r,
            licenseNo = lic,
            style = s,
            specialization = sp,
            location = loc,
            laborCost = bud
        )
        ApiClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                setLoading(false)
                if (response.isSuccessful) {
                    showSnackbar("Success! You can now login.")
                    val intent = Intent(this@RegisterArchitectActivity, LoginActivity::class.java)
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
        registerButton.text = if (isLoading) "" else if (currentStep == MAX_STEPS) "Complete Registration" else "Next Step"
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

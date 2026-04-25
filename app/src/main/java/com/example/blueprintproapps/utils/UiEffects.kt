package com.example.blueprintproapps.utils

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.blueprintproapps.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

object UiEffects {

    /**
     * Applies the signature Blueprint branding: Gradient for text, Solid for icons.
     */
    fun applyBlueprintBranding(vararg views: View) {
        val startColor = Color.parseColor("#8AADF4") // Soft Blue (Original Logo Match)
        val endColor = Color.parseColor("#3F51B5")   // Royal Blue (For depth)

        views.forEach { view ->
            when (view) {
                is TextView -> {
                    val shader = LinearGradient(
                        0f, 0f, 0f, view.textSize,
                        startColor, endColor, Shader.TileMode.CLAMP
                    )
                    view.paint.shader = shader
                    view.invalidate()
                }
            }
        }
    }

    /**
     * Animates a list of views in a cascading sequence (sliding up and fading in).
     */
    fun applyCascadingEntrance(views: List<View>, startDelay: Long = 100L) {
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 50f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600L)
                .setStartDelay(startDelay + (index * 100L))
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }
    fun applyFocusGlow(layout: TextInputLayout, editText: android.widget.EditText) {
        val originalElevation = layout.elevation
        val focusedElevation = 12f
        val originalStrokeWidth = layout.boxStrokeWidth
        val focusedStrokeWidth = originalStrokeWidth + 2

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                layout.animate().z(focusedElevation).setDuration(200).start()
                layout.boxStrokeWidth = focusedStrokeWidth
            } else {
                layout.animate().z(originalElevation).setDuration(200).start()
                layout.boxStrokeWidth = originalStrokeWidth
            }
        }
    }

    /**
     * Logic for the Password Strength Meter
     */
    fun setupPasswordStrength(
        editText: android.widget.EditText,
        bar1: View,
        bar2: View,
        bar3: View,
        label: TextView
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                if (password.isEmpty()) {
                    label.visibility = View.GONE
                    resetBars(bar1, bar2, bar3)
                    return
                }

                label.visibility = View.VISIBLE
                val strength = calculateStrength(password)
                updateStrengthUI(strength, bar1, bar2, bar3, label)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun calculateStrength(password: String): Int {
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isDigit() } && password.any { it.isLetter() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return score
    }

    private fun updateStrengthUI(score: Int, b1: View, b2: View, b3: View, lbl: TextView) {
        val red = Color.parseColor("#FF5252")
        val yellow = Color.parseColor("#FFD740")
        val green = Color.parseColor("#69F0AE")
        val gray = Color.parseColor("#E0E0E0")

        when (score) {
            0, 1 -> {
                b1.backgroundTintList = ColorStateList.valueOf(red)
                b2.backgroundTintList = ColorStateList.valueOf(gray)
                b3.backgroundTintList = ColorStateList.valueOf(gray)
                lbl.text = "Weak"
                lbl.setTextColor(red)
            }
            2 -> {
                b1.backgroundTintList = ColorStateList.valueOf(yellow)
                b2.backgroundTintList = ColorStateList.valueOf(yellow)
                b3.backgroundTintList = ColorStateList.valueOf(gray)
                lbl.text = "Fair"
                lbl.setTextColor(yellow)
            }
            3 -> {
                b1.backgroundTintList = ColorStateList.valueOf(green)
                b2.backgroundTintList = ColorStateList.valueOf(green)
                b3.backgroundTintList = ColorStateList.valueOf(green)
                lbl.text = "Strong"
                lbl.setTextColor(green)
            }
        }
    }

    private fun resetBars(b1: View, b2: View, b3: View) {
        val gray = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
        b1.backgroundTintList = gray
        b2.backgroundTintList = gray
        b3.backgroundTintList = gray
    }

    /**
     * Applies a premium scale effect when a view is pressed.
     */
    fun applyPressScaleEffect(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(150).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                }
            }
            false // Continue to handle click events
        }
    }

    /**
     * Applies an Iconify icon to an ImageView with optional styling.
     */
    fun applyIconify(imageView: ImageView, icon: String, color: Int = Color.parseColor("#82B1FF")) {
        val cleanIcon = icon.replace("{", "").replace("}", "")
        val drawable = com.joanzapata.iconify.IconDrawable(imageView.context, cleanIcon)
            .color(color)
            .sizeDp(24)
        imageView.setImageDrawable(drawable)
    }

    /**
     * Applies an Iconify icon to a Menu Item.
     */
    fun applyIconifyToMenu(context: android.content.Context, menu: android.view.Menu, itemId: Int, icon: String) {
        val item = menu.findItem(itemId)
        if (item != null) {
            val cleanIcon = icon.replace("{", "").replace("}", "")
            item.icon = com.joanzapata.iconify.IconDrawable(context, cleanIcon)
                .colorRes(android.R.color.white)
                .sizeDp(24)
        }
    }

    /**
     * Applies a smooth transition between two step views.
     */
    fun applyStepTransition(outView: View, inView: View, forward: Boolean) {
        val outTranslation = if (forward) -100f else 100f
        val inTranslation = if (forward) 100f else -100f

        outView.animate()
            .alpha(0f)
            .translationX(outTranslation)
            .setDuration(250)
            .withEndAction {
                outView.visibility = View.GONE
                outView.translationX = 0f // Reset

                inView.translationX = inTranslation
                inView.alpha = 0f
                inView.visibility = View.VISIBLE
                inView.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(250)
                    .start()
            }
            .start()
    }
}

package com.example.blueprintproapps.network

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.auth.AuthSessionManager
import com.example.blueprintproapps.auth.UserRole
import com.example.blueprintproapps.models.*
import com.example.blueprintproapps.utils.ArchitectDetailBottomSheet
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MatchClientActivity : AppCompatActivity() {

    private lateinit var root: ConstraintLayout
    private lateinit var appLogo: ImageView
    private lateinit var clientPrompt: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var loadingSection: LinearLayout
    private lateinit var matchRecyclerView: RecyclerView
    private lateinit var matchAdapter: MatchAdapter
    private lateinit var aiFeedbackContainer: LinearLayout
    private lateinit var aiFeedbackText: TextView
    private lateinit var chatHistoryContainer: LinearLayout
    // Was ScrollView — now a plain LinearLayout inside the NestedScrollView
    private lateinit var clarificationScrollView: LinearLayout
    private lateinit var clarificationInner: LinearLayout

    private var lastQuery: String = ""
    private var hasSearched = false
    private lateinit var clientId: String

    private val questionStates = mutableListOf<QuestionState>()
    private var currentQuestions: List<ClarificationQuestion> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = AuthSessionManager.requireSession(this, UserRole.CLIENT) ?: return
        clientId = session.userId
        setContentView(R.layout.activity_match_client)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, topInset, v.paddingRight, v.paddingBottom)
            insets
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        root                    = findViewById(R.id.root)
        appLogo                 = findViewById(R.id.appLogo)
        clientPrompt            = findViewById(R.id.clientPrompt)
        sendButton              = findViewById(R.id.sendButton)
        loadingSection          = findViewById(R.id.loadingSection)
        matchRecyclerView       = findViewById(R.id.matchRecyclerView)
        aiFeedbackContainer     = findViewById(R.id.aiFeedbackContainer)
        aiFeedbackText          = findViewById(R.id.aiFeedbackText)
        chatHistoryContainer    = findViewById(R.id.chatHistoryContainer)
        clarificationScrollView = findViewById(R.id.clarificationScrollView)
        clarificationInner      = findViewById(R.id.clarificationInner)

        matchRecyclerView.layoutManager = LinearLayoutManager(this)

        matchAdapter = MatchAdapter(
            onRequestClick = { architectId -> sendMatchRequest(architectId) },
            onProfileClick = { match ->
                ArchitectDetailBottomSheet(
                    match = match,
                    clientQuery = lastQuery
                ).show(supportFragmentManager, "ArchitectDetail")
            }
        )
        matchRecyclerView.adapter = matchAdapter

        // ── Editor action (keyboard send key)
        clientPrompt.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.action == KeyEvent.ACTION_DOWN)
            ) {
                submitCurrentQuery()
                true
            } else false
        }

        // ── Send button
        sendButton.setOnClickListener { submitCurrentQuery() }
    }

    private fun submitCurrentQuery() {
        val query = clientPrompt.text.toString().trim()
        if (query.isNotEmpty()) {
            if (!hasSearched) animateToSearchState()
            performSearch(query)
            clientPrompt.setText("")
        }
    }

    // ─── Layout transition ────────────────────────────────────────────────────
    // In the new chat design the layout is always in its final state — nothing
    // to animate.  We only need to record that the first search happened.

    private fun animateToSearchState() {
        hasSearched = true
    }

    // ─── API call ─────────────────────────────────────────────────────────────

    private fun performSearch(query: String, clarifications: String? = null) {
        lastQuery = query
        lockSearchInput()

        loadingSection.visibility          = View.VISIBLE
        matchRecyclerView.visibility       = View.GONE
        clarificationScrollView.visibility = View.GONE
        aiFeedbackContainer.visibility     = View.GONE

        ApiClient.instance.getMatches(clientId, query, clarifications)
            .enqueue(object : Callback<MatchesApiResponse> {

                override fun onResponse(
                    call: Call<MatchesApiResponse>,
                    response: Response<MatchesApiResponse>
                ) {
                    loadingSection.visibility = View.GONE

                    val body = response.body()
                    if (body == null) {
                        unlockSearchInput()
                        return
                    }

                    if (body.needsClarification) {
                        if (clarifications == null) {
                            addUserBubble(query)
                        }
                        showClarificationUI(body.questions ?: emptyList())
                        unlockSearchInput()
                        return
                    }

                    clarificationScrollView.visibility = View.GONE
                    aiFeedbackContainer.visibility     = View.GONE
                    matchRecyclerView.visibility       = View.GONE

                    when {
                        body.outOfScope -> {
                            aiFeedbackContainer.visibility = View.VISIBLE
                            aiFeedbackText.text =
                                "This request seems unrelated to architecture. " +
                                "Try describing a building or space you want to design."
                        }

                        body.showFeedback -> {
                            aiFeedbackContainer.visibility = View.VISIBLE
                            aiFeedbackText.text =
                                "Not seeing strong matches yet. Try refining your project description."
                        }

                        else -> {
                            val listWithFooter = mutableListOf<MatchListItem>()
                            body.matches.orEmpty().forEach {
                                listWithFooter.add(MatchListItem.Architect(it))
                            }
                            listWithFooter.add(
                                MatchListItem.Footer(
                                    shown = body.matches.orEmpty().size,
                                    total = body.totalArchitects
                                )
                            )
                            matchAdapter.submitList(listWithFooter)
                            matchRecyclerView.visibility = View.VISIBLE
                        }
                    }

                    unlockSearchInput()
                }

                override fun onFailure(call: Call<MatchesApiResponse>, t: Throwable) {
                    loadingSection.visibility = View.GONE
                    Log.e("MatchClientActivity", "Search failed", t)
                    unlockSearchInput()
                }
            })
    }

    // ─── Chat bubbles ─────────────────────────────────────────────────────────

    // Kept as addChatBubble for backward-compat call sites; delegates to addUserBubble.
    private fun addChatBubble(text: String) = addUserBubble(text)

    /** Right-aligned bubble — user's message / answer. */
    private fun addUserBubble(text: String) {
        chatHistoryContainer.visibility = View.VISIBLE

        val bubble = TextView(this).apply {
            this.text = text
            textSize  = 14f
            setTextColor(Color.WHITE)
            setLineSpacing(dp(3).toFloat(), 1f)
            typeface = Typeface.create("raleway_regular", Typeface.NORMAL)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                // Flat bottom-right corner = sent bubble
                setCornerRadii(floatArrayOf(
                    dp(16).toFloat(), dp(16).toFloat(),  // top-left
                    dp(16).toFloat(), dp(16).toFloat(),  // top-right
                    0f, 0f,                              // bottom-right (flat)
                    dp(16).toFloat(), dp(16).toFloat()   // bottom-left
                ))
                setColor(ContextCompat.getColor(this@MatchClientActivity, R.color.primary))
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin   = dp(6)
            lp.marginStart = dp(52)
            layoutParams = lp
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        row.addView(bubble)
        chatHistoryContainer.addView(row)
    }

    // ─── Clarification UI ────────────────────────────────────────────────────

    private fun showClarificationUI(questions: List<ClarificationQuestion>) {
        currentQuestions = questions
        questionStates.clear()
        repeat(questions.size) { questionStates.add(QuestionState()) }

        clarificationInner.removeAllViews()

        questions.forEachIndexed { idx, q ->
            buildQuestionCard(idx, q)
        }

        // "Find Architects" CTA — full-width, below all questions
        val findBtn = Button(this).apply {
            text      = "Find Architects"
            textSize  = 15f
            isAllCaps = false
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                shape        = GradientDrawable.RECTANGLE
                cornerRadius = dp(12).toFloat()
                setColor(ContextCompat.getColor(this@MatchClientActivity, R.color.primary))
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin    = dp(20)
            lp.bottomMargin = dp(4)
            layoutParams    = lp
        }
        findBtn.setOnClickListener { onFindArchitectsTapped() }
        clarificationInner.addView(findBtn)

        clarificationScrollView.visibility = View.VISIBLE
    }

    /**
     * Renders one question as a left-aligned AI chat bubble containing
     * the question text, chip-style options, a skip link, and a free-text input.
     */
    private fun buildQuestionCard(idx: Int, q: ClarificationQuestion) {
        // ── Outer row (aligns bubble to the left)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.START
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(10)
            layoutParams = lp
        }

        // ── Bubble container (navy, flat bottom-left = AI side)
        val bubble = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setCornerRadii(floatArrayOf(
                    dp(16).toFloat(), dp(16).toFloat(),  // top-left
                    dp(16).toFloat(), dp(16).toFloat(),  // top-right
                    dp(16).toFloat(), dp(16).toFloat(),  // bottom-right
                    0f, 0f                               // bottom-left (flat = AI side)
                ))
                setColor(Color.parseColor("#0A3D62"))
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.marginEnd = dp(52)
            layoutParams = lp
        }

        // ── Question label
        val label = TextView(this).apply {
            text      = q.question
            textSize  = 14f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setLineSpacing(dp(2).toFloat(), 1f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        bubble.addView(label)

        // ── Chip group (pill-shaped, wrapping)
        val chipGroup = ChipGroup(this).apply {
            isSingleSelection    = true
            isSelectionRequired  = false
            chipSpacingHorizontal = dp(6)
            chipSpacingVertical  = dp(4)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(10)
            layoutParams = lp
        }

        q.options.forEach { option ->
            val chip = Chip(this).apply {
                text             = option
                isCheckable      = true
                isCheckedIconVisible = false
                textSize         = 13f
                chipCornerRadius = dp(20).toFloat()
                chipStrokeWidth  = dp(1).toFloat()

                // Unselected: outlined, light stroke, white text
                // Selected:   white fill, navy text
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf()
                )
                chipBackgroundColor = ColorStateList(
                    states,
                    intArrayOf(Color.WHITE, Color.parseColor("#1E5276"))
                )
                setTextColor(ColorStateList(
                    states,
                    intArrayOf(Color.parseColor("#0A3D62"), Color.WHITE)
                ))
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#AACCE0"))
            }
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selected = group.findViewById<Chip>(checkedIds[0]).text.toString()
                questionStates[idx].selectedOption = selected
                questionStates[idx].skipped        = false
            } else {
                questionStates[idx].selectedOption = null
            }
        }
        bubble.addView(chipGroup)

        // ── Skip link (plain text, subtle)
        val skipLink = TextView(this).apply {
            text      = "Skip this question"
            textSize  = 12f
            setTextColor(Color.parseColor("#AACCE0"))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(6)
            layoutParams = lp
        }
        skipLink.setOnClickListener {
            questionStates[idx].skipped        = true
            questionStates[idx].selectedOption = null
            chipGroup.clearCheck()
        }
        bubble.addView(skipLink)

        // ── Free-text input (subtle, no heavy border)
        val customInput = EditText(this).apply {
            hint = "Or type your own answer..."
            textSize = 13f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#7AAFC8"))
            background = GradientDrawable().apply {
                shape        = GradientDrawable.RECTANGLE
                cornerRadius = dp(6).toFloat()
                setColor(Color.parseColor("#1E5276"))
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(8)
            layoutParams = lp
            setPadding(dp(10), dp(7), dp(10), dp(7))
        }
        customInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                questionStates[idx].customText = s?.toString() ?: ""
            }
        })
        bubble.addView(customInput)

        row.addView(bubble)
        clarificationInner.addView(row)
    }

    private fun onFindArchitectsTapped() {
        val parts = mutableListOf<String>()

        currentQuestions.forEachIndexed { idx, q ->
            val state = questionStates[idx]
            val answer = when {
                state.customText.isNotBlank() -> state.customText.trim()
                state.skipped                 -> null
                state.selectedOption != null  -> state.selectedOption!!
                else                          -> null
            }
            if (answer != null) parts.add("${q.question}: $answer.")
        }

        val clarifications = if (parts.isNotEmpty()) parts.joinToString(" ") else null

        // Show one user bubble per answered question (just the answer, not the question prefix)
        currentQuestions.forEachIndexed { idx, _ ->
            val state = questionStates[idx]
            val displayAnswer = when {
                state.customText.isNotBlank() -> state.customText.trim()
                state.skipped                 -> null
                state.selectedOption != null  -> state.selectedOption!!
                else                          -> null
            }
            if (displayAnswer != null) addUserBubble(displayAnswer)
        }

        clarificationScrollView.visibility = View.GONE
        performSearch(lastQuery, clarifications)
    }

    // ─── Match request ────────────────────────────────────────────────────────

    private fun sendMatchRequest(architectId: String) {
        ApiClient.instance.requestMatch(
            MatchRequest(architectId, clientId)
        ).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {}
            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Log.e("MatchClientActivity", "Match request failed", t)
            }
        })
    }

    // ─── Input lock ───────────────────────────────────────────────────────────

    private fun lockSearchInput() {
        root.requestFocus()
        clientPrompt.clearFocus()
        clientPrompt.isFocusable            = false
        clientPrompt.isFocusableInTouchMode = false
        clientPrompt.isCursorVisible        = false

        val imm = getSystemService(INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(clientPrompt.windowToken, 0)
    }

    private fun unlockSearchInput() {
        clientPrompt.isFocusable            = true
        clientPrompt.isFocusableInTouchMode = true
        clientPrompt.isCursorVisible        = true
    }

    // ─── Utility ─────────────────────────────────────────────────────────────

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()
}

private data class QuestionState(
    var selectedOption: String? = null,
    var customText: String      = "",
    var skipped: Boolean        = false
)
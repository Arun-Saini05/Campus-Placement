package com.smartcampus.app.ui.officer

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.smartcampus.app.R
import com.smartcampus.app.api.ApiClient
import com.smartcampus.app.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentDirectoryActivity : AppCompatActivity() {
    private lateinit var session: SessionManager
    private lateinit var container: LinearLayout
    private lateinit var resultsContainer: LinearLayout
    
    private var selectedBranch: String? = null
    private var selectedStatus: String? = "ALL"
    private lateinit var etCgpa: TextInputEditText

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic)
        session = SessionManager(this)
        
        findViewById<TextView>(R.id.tvPageTitle).text = "Student Directory"
        container = findViewById(R.id.layoutContent)
        
        setupProfessionalDirectory()
        
        resultsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(resultsContainer)
        
        loadStudents()
    }

    private fun setupProfessionalDirectory() {
        // Talent Pool Header
        val headerCard = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#122529"))
            radius = dp(12).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(24) }
        }
        val headerInner = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        headerInner.addView(TextView(this).apply { text = "👥"; textSize = 24f })
        val headerTextCol = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(12) }
        }
        headerTextCol.addView(TextView(this).apply { text = "Active Talent Pool"; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD); textSize = 14f })
        headerTextCol.addView(TextView(this).apply { text = "Search and filter candidates for recruitment drives."; setTextColor(Color.parseColor("#8B949E")); textSize = 11f })
        headerInner.addView(headerTextCol)
        headerCard.addView(headerInner)
        container.addView(headerCard)

        // Filters Card
        val filterCard = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = dp(20).toFloat()
            setContentPadding(dp(20), dp(20), dp(20), dp(20))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(24) }
        }
        val filterInner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // Placement Status
        filterInner.addView(TextView(this).apply { text = "PLACEMENT STATUS"; textSize = 10f; setTextColor(Color.parseColor("#8B949E")); setTypeface(null, Typeface.BOLD); setPadding(dp(4), 0, 0, dp(8)) })
        val statusGroup = ChipGroup(this).apply { isSingleSelection = true; isSelectionRequired = true }
        listOf("ALL", "PLACED", "UNPLACED").forEach { status ->
            val chip = Chip(this).apply {
                text = status; isCheckable = true; isChecked = status == "ALL"
                setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD); textSize = 11f
                chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#21262D"))
                chipStrokeWidth = 0f
                setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        selectedStatus = status
                        chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#6C63FF"))
                    } else {
                        chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#21262D"))
                    }
                }
            }
            statusGroup.addView(chip)
        }
        filterInner.addView(statusGroup)

        // Branch Selection
        filterInner.addView(TextView(this).apply { text = "DEPARTMENT"; textSize = 10f; setTextColor(Color.parseColor("#8B949E")); setTypeface(null, Typeface.BOLD); setPadding(dp(4), dp(16), 0, dp(8)) })
        val branches = arrayOf("All Departments", "Computer Science", "Information Tech", "ECE", "Electrical", "Mechanical", "Civil")
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@StudentDirectoryActivity, android.R.layout.simple_spinner_dropdown_item, branches)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44))
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                selectedBranch = if (pos == 0) null else branches[pos]
                (v as? TextView)?.setTextColor(Color.WHITE)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        filterInner.addView(spinner)

        // CGPA Filter
        val tilCgpa = TextInputLayout(this).apply {
            hint = "Minimum CGPA"
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxStrokeColorStateList(ColorStateList.valueOf(Color.parseColor("#6C63FF")))
            setBoxCornerRadii(dp(12).toFloat(), dp(12).toFloat(), dp(12).toFloat(), dp(12).toFloat())
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(16) }
        }
        etCgpa = TextInputEditText(tilCgpa.context).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(Color.WHITE); textSize = 14f
        }
        tilCgpa.addView(etCgpa)
        filterInner.addView(tilCgpa)

        // Action Button
        val btnFilter = MaterialButton(this).apply {
            text = "Update Directory"; textSize = 14f; isAllCaps = false
            cornerRadius = dp(24); setBackgroundColor(Color.parseColor("#00BCD4"))
            setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(50)
            ).apply { topMargin = dp(20) }
        }
        btnFilter.setOnClickListener { loadStudents() }
        filterInner.addView(btnFilter)

        filterCard.addView(filterInner)
        container.addView(filterCard)
    }

    private fun loadStudents() {
        resultsContainer.removeAllViews()
        val loading = TextView(this).apply { 
            text = "Searching directory..."; setTextColor(Color.parseColor("#8B949E"))
            gravity = Gravity.CENTER; setPadding(0, dp(40), 0, dp(40))
        }
        resultsContainer.addView(loading)

        val cgpa = etCgpa.text.toString().toFloatOrNull()
        ApiClient.getApi().searchStudents(session.authToken, selectedBranch, cgpa, selectedStatus)
            .enqueue(object : Callback<List<JsonObject>> {
                override fun onResponse(call: Call<List<JsonObject>>, response: Response<List<JsonObject>>) {
                    resultsContainer.removeAllViews()
                    if (response.isSuccessful && response.body() != null) {
                        val students = response.body()!!
                        resultsContainer.addView(TextView(this@StudentDirectoryActivity).apply { 
                            text = "RESULTS (${students.size})"; textSize = 10f; setTextColor(Color.parseColor("#8B949E"))
                            setTypeface(null, Typeface.BOLD); setPadding(dp(4), 0, 0, dp(12))
                        })
                        if (students.isEmpty()) addEmptyState()
                        else students.forEach { addStudentCard(it) }
                    }
                }
                override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) {
                    resultsContainer.removeAllViews()
                    Toast.makeText(this@StudentDirectoryActivity, "Sync Error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addStudentCard(student: JsonObject) {
        val name = student.get("name")?.asString ?: "Candidate"
        val branch = student.get("branch")?.asString ?: "N/A"
        val cgpa = student.get("cgpa")?.asFloat ?: 0f
        val status = student.get("placementStatus")?.asString ?: "UNPLACED"

        val card = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = dp(16).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
        }

        val outer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }

        // Avatar
        val avatar = TextView(this).apply {
            text = name.firstOrNull()?.uppercase() ?: "S"
            textSize = 18f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.parseColor("#30363D")) }
        }
        outer.addView(avatar)

        // Info Col
        val infoCol = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(12) }
        }
        infoCol.addView(TextView(this).apply { text = name; textSize = 15f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD) })
        infoCol.addView(TextView(this).apply { text = "$branch • Sem ${student.get("semester")?.asInt ?: 0}"; textSize = 11f; setTextColor(Color.parseColor("#8B949E")) })
        
        val cgpaRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(4), 0, 0); gravity = Gravity.CENTER_VERTICAL }
        cgpaRow.addView(TextView(this).apply { text = "CGPA: $cgpa"; textSize = 12f; setTextColor(Color.parseColor("#00BCD4")); setTypeface(null, Typeface.BOLD) })
        infoCol.addView(cgpaRow)
        outer.addView(infoCol)

        // Status Badge
        val badge = TextView(this).apply {
            text = status; textSize = 9f; setTextColor(Color.WHITE)
            setPadding(dp(10), dp(4), dp(10), dp(4)); setTypeface(null, Typeface.BOLD)
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(Color.parseColor(if (status == "PLACED") "#3FB950" else "#F0883E"))
            }
        }
        outer.addView(badge)

        card.addView(outer)
        resultsContainer.addView(card)
    }

    private fun addEmptyState() {
        val empty = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(0, dp(60), 0, dp(60)) }
        empty.addView(TextView(this).apply { text = "🔎"; textSize = 40f })
        empty.addView(TextView(this).apply { text = "No matching candidates found"; textSize = 14f; setTextColor(Color.parseColor("#8B949E")); setPadding(0, dp(8), 0, 0) })
        resultsContainer.addView(empty)
    }
}

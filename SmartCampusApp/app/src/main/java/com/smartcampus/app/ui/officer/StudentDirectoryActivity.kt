package com.smartcampus.app.ui.officer

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
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
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentDirectoryActivity : AppCompatActivity() {
    private lateinit var session: SessionManager
    private lateinit var container: LinearLayout
    private lateinit var resultsContainer: LinearLayout
    
    // Filters
    private var selectedBranch: String? = null
    private var minCgpa: Float? = null
    private var selectedStatus: String? = "ALL" // ALL, PLACED, UNPLACED

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic)
        session = SessionManager(this)
        
        findViewById<TextView>(R.id.tvPageTitle).text = "Student Directory"
        container = findViewById(R.id.layoutContent)
        
        setupFilters()
        
        resultsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(resultsContainer)
        
        loadStudents()
    }

    private fun setupFilters() {
        val filterCard = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = dp(16).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(16) }
        }

        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // Branch Spinner
        val branches = arrayOf("All Branches", "Computer Science", "Electrical Engineering", "Mechanical Engineering", "Civil Engineering")
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@StudentDirectoryActivity, android.R.layout.simple_spinner_dropdown_item, branches)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(12) }
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBranch = if (position == 0) null else branches[position]
                // Text color hack for default spinner
                (view as? TextView)?.setTextColor(Color.WHITE)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        inner.addView(TextView(this).apply { text = "Branch"; setTextColor(Color.parseColor("#8B949E")); textSize = 12f })
        inner.addView(spinner)

        // CGPA Input
        val cgpaInput = EditText(this).apply {
            hint = "Min CGPA (e.g. 7.5)"
            setHintTextColor(Color.parseColor("#8B949E"))
            setTextColor(Color.WHITE)
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            background = null // Remove underline
            setPadding(0, dp(8), 0, dp(12))
        }
        inner.addView(TextView(this).apply { text = "Minimum CGPA"; setTextColor(Color.parseColor("#8B949E")); textSize = 12f })
        inner.addView(cgpaInput)

        // Status Chips
        inner.addView(TextView(this).apply { text = "Placement Status"; setTextColor(Color.parseColor("#8B949E")); textSize = 12f })
        val chipGroup = ChipGroup(this).apply {
            isSingleSelection = true
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(12) }
        }

        val statuses = listOf("ALL", "PLACED", "UNPLACED")
        statuses.forEach { status ->
            val chip = Chip(this).apply {
                text = status
                isCheckable = true
                isChecked = status == selectedStatus
                setTextColor(Color.WHITE)
                setChipBackgroundColorResource(R.color.bg_dark)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedStatus = status
                }
            }
            chipGroup.addView(chip)
        }
        inner.addView(chipGroup)

        // Search Button
        val btnSearch = MaterialButton(this).apply {
            text = "Apply Filters"
            setBackgroundColor(Color.parseColor("#00E5FF"))
            setTextColor(Color.BLACK)
            cornerRadius = dp(24)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48))
        }
        btnSearch.setOnClickListener {
            val cgpaStr = cgpaInput.text.toString()
            minCgpa = if (cgpaStr.isNotEmpty()) cgpaStr.toFloatOrNull() else null
            loadStudents()
        }
        inner.addView(btnSearch)

        filterCard.addView(inner)
        container.addView(filterCard)
    }

    private fun loadStudents() {
        resultsContainer.removeAllViews()
        resultsContainer.addView(TextView(this).apply {
            text = "Loading students..."
            setTextColor(Color.WHITE)
            setPadding(0, dp(16), 0, dp(16))
        })

        ApiClient.getApi().searchStudents(session.authToken, selectedBranch, minCgpa, selectedStatus)
            .enqueue(object : Callback<List<JsonObject>> {
                override fun onResponse(call: Call<List<JsonObject>>, response: Response<List<JsonObject>>) {
                    resultsContainer.removeAllViews()
                    if (response.isSuccessful && response.body() != null) {
                        val students = response.body()!!
                        
                        resultsContainer.addView(TextView(this@StudentDirectoryActivity).apply {
                            text = "Found ${students.size} student(s)"
                            textSize = 14f
                            setTextColor(Color.parseColor("#8B949E"))
                            setPadding(0, 0, 0, dp(12))
                        })

                        if (students.isEmpty()) {
                            resultsContainer.addView(TextView(this@StudentDirectoryActivity).apply {
                                text = "No students match the criteria."
                                setTextColor(Color.WHITE)
                            })
                            return
                        }

                        students.forEach { addStudentCard(it) }
                    } else {
                        Toast.makeText(this@StudentDirectoryActivity, "Failed to load", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) {
                    resultsContainer.removeAllViews()
                    Toast.makeText(this@StudentDirectoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addStudentCard(student: JsonObject) {
        val name = student.get("name")?.asString ?: "Unknown"
        val branch = student.get("branch")?.asString ?: "N/A"
        val sem = student.get("semester")?.asInt ?: 0
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

        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // Top Row: Name and Status Badge
        val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        topRow.addView(TextView(this).apply {
            text = name; textSize = 16f; setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        
        // Status Badge
        val isPlaced = status == "PLACED"
        val badge = TextView(this).apply {
            text = status
            textSize = 10f
            setTextColor(Color.WHITE)
            setPadding(dp(8), dp(4), dp(8), dp(4))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor(if (isPlaced) "#3FB950" else "#F0883E"))
                cornerRadius = dp(12).toFloat()
            }
        }
        topRow.addView(badge)
        inner.addView(topRow)

        // Branch & Semester
        inner.addView(TextView(this).apply {
            text = "$branch • Sem $sem"
            textSize = 12f; setTextColor(Color.parseColor("#8B949E"))
            setPadding(0, dp(4), 0, dp(4))
        })

        // CGPA
        inner.addView(TextView(this).apply {
            text = "CGPA: $cgpa"
            textSize = 14f; setTextColor(Color.parseColor("#00E5FF"))
            setTypeface(typeface, Typeface.BOLD)
        })

        card.addView(inner)
        resultsContainer.addView(card)
    }
}

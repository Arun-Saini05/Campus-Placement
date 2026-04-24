package com.smartcampus.app.ui.officer

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smartcampus.app.R
import com.smartcampus.app.api.ApiClient
import com.smartcampus.app.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EligibleStudentsActivity : AppCompatActivity() {
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic)
        session = SessionManager(this)

        val driveId = intent.getIntExtra("driveId", 0)
        val companyName = intent.getStringExtra("companyName") ?: "Drive"

        findViewById<TextView>(R.id.tvPageTitle).text = "$companyName — Eligible Students"

        val container = findViewById<LinearLayout>(R.id.layoutContent)

        // Load eligible students
        ApiClient.getApi().getEligibleStudents(session.authToken, driveId).enqueue(object : Callback<List<JsonObject>> {
            override fun onResponse(call: Call<List<JsonObject>>, response: Response<List<JsonObject>>) {
                if (response.isSuccessful && response.body() != null) {
                    val students = response.body()!!

                    // Summary card
                    container.addView(TextView(this@EligibleStudentsActivity).apply {
                        text = "${students.size} eligible student(s) found"
                        textSize = 16f
                        setTextColor(resources.getColor(R.color.accent, null))
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                        setPadding(0, 0, 0, 24)
                    })

                    if (students.isEmpty()) {
                        container.addView(TextView(this@EligibleStudentsActivity).apply {
                            text = "No students match the eligibility criteria for this drive."
                            textSize = 14f
                            setTextColor(resources.getColor(R.color.text_secondary, null))
                            setPadding(0, 0, 0, 24)
                        })
                    }

                    students.forEach { student ->
                        val card = MaterialCardView(this@EligibleStudentsActivity).apply {
                            setCardBackgroundColor(resources.getColor(R.color.bg_card, null))
                            radius = 16f
                            setContentPadding(24, 24, 24, 24)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { bottomMargin = 12 }
                        }

                        val inner = LinearLayout(this@EligibleStudentsActivity).apply {
                            orientation = LinearLayout.VERTICAL
                        }

                        // Student name
                        inner.addView(TextView(this@EligibleStudentsActivity).apply {
                            text = student.get("name")?.asString ?: "Student"
                            textSize = 16f
                            setTextColor(resources.getColor(R.color.text_primary, null))
                            setTypeface(typeface, android.graphics.Typeface.BOLD)
                        })

                        // Email
                        inner.addView(TextView(this@EligibleStudentsActivity).apply {
                            text = student.get("email")?.asString ?: ""
                            textSize = 13f
                            setTextColor(resources.getColor(R.color.text_tertiary, null))
                        })

                        // Branch & Semester
                        val branch = student.get("branch")?.asString ?: "N/A"
                        val semester = student.get("semester")?.asInt ?: 0
                        inner.addView(TextView(this@EligibleStudentsActivity).apply {
                            text = "$branch • Semester $semester"
                            textSize = 13f
                            setTextColor(resources.getColor(R.color.text_secondary, null))
                            setPadding(0, 8, 0, 0)
                        })

                        // CGPA
                        val cgpa = student.get("cgpa")?.asFloat ?: 0f
                        inner.addView(TextView(this@EligibleStudentsActivity).apply {
                            text = "CGPA: $cgpa"
                            textSize = 14f
                            setTextColor(resources.getColor(R.color.accent, null))
                            setTypeface(typeface, android.graphics.Typeface.BOLD)
                            setPadding(0, 4, 0, 0)
                        })

                        // Skills
                        val skillsArray = student.getAsJsonArray("skills")
                        if (skillsArray != null && skillsArray.size() > 0) {
                            val skillsList = (0 until skillsArray.size()).joinToString(", ") {
                                skillsArray[it].asString
                            }
                            inner.addView(TextView(this@EligibleStudentsActivity).apply {
                                text = "Skills: $skillsList"
                                textSize = 12f
                                setTextColor(resources.getColor(R.color.text_secondary, null))
                                setPadding(0, 8, 0, 0)
                            })
                        }

                        card.addView(inner)
                        container.addView(card)
                    }
                } else {
                    Toast.makeText(this@EligibleStudentsActivity, "Failed to load students", Toast.LENGTH_SHORT).show()
                }

                // Back button (always added)
                addBackButton(container)
            }

            override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) {
                Toast.makeText(this@EligibleStudentsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                addBackButton(container)
            }
        })
    }

    private fun addBackButton(container: LinearLayout) {
        val btnBack = MaterialButton(this).apply {
            text = "← Back to Dashboard"
            setBackgroundColor(resources.getColor(R.color.bg_card, null))
            setTextColor(resources.getColor(R.color.text_secondary, null))
            cornerRadius = 16
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 140
            ).apply { topMargin = 24 }
        }
        btnBack.setOnClickListener { finish() }
        container.addView(btnBack)
    }
}

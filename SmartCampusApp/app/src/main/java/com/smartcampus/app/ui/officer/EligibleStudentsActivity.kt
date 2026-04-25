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
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EligibleStudentsActivity : AppCompatActivity() {
    private lateinit var session: SessionManager
    private lateinit var container: LinearLayout

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic)
        session = SessionManager(this)

        val driveId = intent.getIntExtra("driveId", 0)
        val companyName = intent.getStringExtra("companyName") ?: "Drive"

        findViewById<TextView>(R.id.tvPageTitle).text = "Track Drive"
        container = findViewById(R.id.layoutContent)

        // Add Header Card
        addHeaderCard(companyName)

        // Load eligible students
        loadEligibleStudents(driveId)
    }

    private fun addHeaderCard(companyName: String) {
        val card = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = dp(20).toFloat()
            setContentPadding(dp(20), dp(20), dp(20), dp(20))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(24) }
        }
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        inner.addView(TextView(this).apply {
            text = companyName
            textSize = 22f; setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        })
        inner.addView(TextView(this).apply {
            text = "Eligible Candidates Pipeline"
            textSize = 13f; setTextColor(Color.parseColor("#8B949E"))
            setPadding(0, dp(4), 0, 0)
        })
        card.addView(inner)
        container.addView(card)
    }

    private fun loadEligibleStudents(driveId: Int) {
        ApiClient.getApi().getEligibleStudents(session.authToken, driveId).enqueue(object : Callback<List<JsonObject>> {
            override fun onResponse(call: Call<List<JsonObject>>, response: Response<List<JsonObject>>) {
                if (response.isSuccessful && response.body() != null) {
                    val students = response.body()!!
                    
                    container.addView(TextView(this@EligibleStudentsActivity).apply {
                        text = "STUDENTS (${students.size})"; textSize = 11f
                        setTextColor(Color.parseColor("#8B949E"))
                        setTypeface(null, Typeface.BOLD); letterSpacing = 0.1f
                        setPadding(dp(4), 0, 0, dp(12))
                    })

                    if (students.isEmpty()) {
                        addEmptyState()
                    } else {
                        students.forEach { addStudentCard(it) }
                    }
                } else {
                    Toast.makeText(this@EligibleStudentsActivity, "Failed to load pipeline", Toast.LENGTH_SHORT).show()
                }
                addBackButton()
            }

            override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) {
                Toast.makeText(this@EligibleStudentsActivity, "Network Error", Toast.LENGTH_SHORT).show()
                addBackButton()
            }
        })
    }

    private fun addStudentCard(student: JsonObject) {
        val name = student.get("name")?.asString ?: "Candidate"
        val branch = student.get("branch")?.asString ?: "N/A"
        val cgpa = student.get("cgpa")?.asFloat ?: 0f
        val email = student.get("email")?.asString ?: ""
        val skills = student.getAsJsonArray("skills")

        val card = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = dp(16).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
        }

        val outer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // Top Row: Avatar + Info + CGPA
        val topRow = LinearLayout(this).apply { 
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL 
        }

        // Avatar
        val avatar = TextView(this).apply {
            text = name.firstOrNull()?.uppercase() ?: "S"
            textSize = 18f; setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#30363D"))
            }
        }
        topRow.addView(avatar)

        // Name and Email
        val nameCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(12) }
        }
        nameCol.addView(TextView(this).apply {
            text = name; textSize = 15f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD)
        })
        nameCol.addView(TextView(this).apply {
            text = email; textSize = 11f; setTextColor(Color.parseColor("#8B949E"))
        })
        topRow.addView(nameCol)

        // CGPA Badge
        val cgpaBadge = TextView(this).apply {
            text = "%.2f".format(cgpa)
            textSize = 13f; setTextColor(Color.parseColor("#00BCD4"))
            setTypeface(null, Typeface.BOLD); setPadding(dp(8), dp(4), dp(8), dp(4))
            background = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(Color.parseColor("#122529"))
            }
        }
        topRow.addView(cgpaBadge)
        outer.addView(topRow)

        // Middle Row: Branch & Semester Tags
        val tagsRow = LinearLayout(this).apply { 
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(12), 0, 0)
        }
        tagsRow.addView(createTag(branch, "#3FB950"))
        tagsRow.addView(createTag("Semester ${student.get("semester")?.asInt ?: 0}", "#6C63FF"))
        outer.addView(tagsRow)

        // Bottom Row: Skills Chips
        if (skills != null && skills.size() > 0) {
            val skillsLayout = LinearLayout(this).apply { 
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(12), 0, 0)
            }
            // Show only first 3 skills to keep it clean
            for (i in 0 until minOf(skills.size(), 3)) {
                skillsLayout.addView(createSkillChip(skills[i].asString))
            }
            if (skills.size() > 3) {
                skillsLayout.addView(createSkillChip("+${skills.size() - 3}"))
            }
            outer.addView(skillsLayout)
        }

        // Action Button
        val btnTrack = MaterialButton(this).apply {
            text = "Full Profile"; textSize = 11f; isAllCaps = false
            cornerRadius = dp(12); setBackgroundColor(Color.parseColor("#30363D"))
            setTextColor(Color.WHITE); layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(36)
            ).apply { topMargin = dp(16) }
        }
        outer.addView(btnTrack)

        card.addView(outer)
        container.addView(card)
    }

    private fun createTag(text: String, color: String): TextView {
        return TextView(this).apply {
            this.text = text; textSize = 10f; setTextColor(Color.parseColor(color))
            setTypeface(null, Typeface.BOLD); setPadding(dp(8), dp(2), dp(8), dp(2))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(8) }
            background = GradientDrawable().apply {
                cornerRadius = dp(4).toFloat()
                setStroke(dp(1), Color.parseColor(color))
            }
        }
    }

    private fun createSkillChip(text: String): TextView {
        return TextView(this).apply {
            this.text = text; textSize = 9f; setTextColor(Color.parseColor("#8B949E"))
            setPadding(dp(10), dp(4), dp(10), dp(4))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(6) }
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(Color.parseColor("#21262D"))
            }
        }
    }

    private fun addEmptyState() {
        val emptyLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, dp(100), 0, 0)
        }
        emptyLayout.addView(TextView(this).apply {
            text = "📂"; textSize = 40f; gravity = Gravity.CENTER
        })
        emptyLayout.addView(TextView(this).apply {
            text = "No candidates match this criteria"; textSize = 14f
            setTextColor(Color.parseColor("#8B949E")); gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
        })
        container.addView(emptyLayout)
    }

    private fun addBackButton() {
        val btn = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "Return to Dashboard"; textSize = 13f; isAllCaps = false
            cornerRadius = dp(24); setTextColor(Color.parseColor("#8B949E"))
            strokeColor = ColorStateList.valueOf(Color.parseColor("#30363D"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
            ).apply { topMargin = dp(24); bottomMargin = dp(32) }
            setOnClickListener { finish() }
        }
        container.addView(btn)
    }
}

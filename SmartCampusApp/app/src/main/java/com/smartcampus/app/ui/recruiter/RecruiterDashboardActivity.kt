package com.smartcampus.app.ui.recruiter

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.smartcampus.app.R
import com.smartcampus.app.ui.auth.LoginActivity
import com.smartcampus.app.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class RecruiterDashboardActivity : AppCompatActivity() {
    private lateinit var session: SessionManager
    private lateinit var container: LinearLayout
    private lateinit var layoutStats: LinearLayout

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic)
        session = SessionManager(this)
        findViewById<TextView>(R.id.tvPageTitle).text = "Recruiter Hub"
        container = findViewById(R.id.layoutContent)
    }

    override fun onResume() {
        super.onResume()
        container.removeAllViews()
        setupSections()

        addHeaderSection()
        addQuickActions()
        
        // Render mock stats to ensure consistent UI formatting for now
        addStatsGrid(12, 450, 15, 8)
        
        addLogoutButton()
    }

    private fun setupSections() {
        layoutStats = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        container.addView(layoutStats)
    }

    private fun addHeaderSection() {
        container.addView(TextView(this).apply {
            text = "Recruiter Analytics\n& Candidate Search"
            textSize = 26f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setLineSpacing(0f, 1.1f)
        }, 0)
        container.addView(TextView(this).apply {
            text = "Welcome back, ${session.userName}. Here is your hiring pipeline overview."
            textSize = 13f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            setPadding(0, dp(4), 0, dp(16))
        }, 1)
    }

    private fun addQuickActions() {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(20) }
        }

        val btnSearch = MaterialButton(this).apply {
            text = "🔍 Search Candidates"
            setBackgroundColor(Color.parseColor("#00BCD4"))
            setTextColor(Color.WHITE)
            cornerRadius = dp(24)
            textSize = 12f
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(8) }
        }

        val btnJobs = MaterialButton(this).apply {
            text = "💼 Post a Job"
            setBackgroundColor(Color.parseColor("#3FB950"))
            setTextColor(Color.WHITE)
            cornerRadius = dp(24)
            textSize = 12f
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginStart = dp(8) }
        }

        row.addView(btnSearch)
        row.addView(btnJobs)
        container.addView(row, 2)
    }

    private fun addStatsGrid(jobsPosted: Int, appsReceived: Int, interviews: Int, hires: Int) {
        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
        }
        row1.addView(buildStatCard("JOBS POSTED", "$jobsPosted", "Active openings", Color.parseColor("#00E5FF")))
        row1.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(12), 0) })
        row1.addView(buildStatCard("APPLICATIONS", "$appsReceived", "Total received", Color.parseColor("#3FB950")))
        layoutStats.addView(row1)

        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(24) }
        }
        row2.addView(buildStatCard("INTERVIEWS", "$interviews", "Scheduled", Color.parseColor("#F0883E")))
        row2.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(12), 0) })
        row2.addView(buildStatCard("TOTAL HIRES", "$hires", "Selected candidates", Color.parseColor("#6C63FF")))
        layoutStats.addView(row2)
    }

    private fun buildStatCard(label: String, value: String, sub: String, color: Int): MaterialCardView {
        val card = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = dp(16).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        inner.addView(TextView(this).apply { 
            text = label
            textSize = 10f
            setTextColor(Color.parseColor("#8B949E"))
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.1f 
        })
        inner.addView(TextView(this).apply { 
            text = value
            textSize = 24f
            setTextColor(color)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(4)) 
        })
        inner.addView(TextView(this).apply { 
            text = sub
            textSize = 10f
            setTextColor(Color.parseColor("#8B949E")) 
        })
        card.addView(inner)
        return card
    }

    private fun addLogoutButton() {
        container.addView(MaterialButton(this).apply {
            text = "Logout"
            setBackgroundColor(resources.getColor(R.color.error, null))
            cornerRadius = dp(12)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)).apply { 
                topMargin = dp(16)
                bottomMargin = dp(32) 
            }
            setOnClickListener { 
                session.logout()
                startActivity(Intent(this@RecruiterDashboardActivity, LoginActivity::class.java).apply { 
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK 
                }) 
            }
        })
    }
}


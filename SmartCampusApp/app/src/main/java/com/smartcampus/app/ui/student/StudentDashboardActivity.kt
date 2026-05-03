package com.smartcampus.app.ui.student

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smartcampus.app.R
import com.smartcampus.app.api.ApiClient
import com.smartcampus.app.models.StudentProfile
import com.smartcampus.app.ui.auth.LoginActivity
import com.smartcampus.app.ui.jobs.ApplicationTrackerActivity
import com.smartcampus.app.ui.jobs.JobListActivity
import com.smartcampus.app.ui.notifications.NotificationCenterActivity
import com.smartcampus.app.ui.resume.ResumeBuilderActivity
import com.smartcampus.app.utils.SessionManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)

        session = SessionManager(this)

        // Set user name
        findViewById<TextView>(R.id.tvUserName).text = session.userName

        // Navigation click listeners
        findViewById<MaterialCardView>(R.id.cardProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardSkills).setOnClickListener {
            startActivity(Intent(this, SkillRecommendationActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardJobs).setOnClickListener {
            startActivity(Intent(this, JobListActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardApplications).setOnClickListener {
            startActivity(Intent(this, ApplicationTrackerActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardResume).setOnClickListener {
            startActivity(Intent(this, ResumeBuilderActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardCareer).setOnClickListener {
            startActivity(Intent(this, CareerRoadmapActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardLogout).setOnClickListener {
            session.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Notification bell
        findViewById<android.widget.ImageView>(R.id.ivNotifications).setOnClickListener {
            findViewById<android.view.View>(R.id.viewNotificationBadge).visibility = android.view.View.GONE
            startActivity(Intent(this, NotificationCenterActivity::class.java))
        }

        // Refresh Button
        findViewById<android.widget.ImageView>(R.id.btnRefresh).setOnClickListener {
            val swipeRefresh = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
            swipeRefresh.isRefreshing = true
            loadDashboardData()
        }

        // Swipe to Refresh
        val swipeRefresh = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            loadDashboardData()
        }

        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val token = session.authToken

        // Load profile for stats
        ApiClient.getApi().getProfile(token).enqueue(object : Callback<StudentProfile> {
            override fun onResponse(call: Call<StudentProfile>, response: Response<StudentProfile>) {
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    val skillCount = profile.skills?.size ?: 0
                    findViewById<TextView>(R.id.tvSkillCount).text = skillCount.toString()
                }
            }

            override fun onFailure(call: Call<StudentProfile>, t: Throwable) { /* silent */ }
        })

        // Load progress
        ApiClient.getApi().getProgress(token).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val readiness = data.get("readinessPercentage")?.asInt ?: 0
                    findViewById<LinearProgressIndicator>(R.id.progressReadiness).progress = readiness
                    findViewById<TextView>(R.id.tvReadinessPercent).text = "${readiness}%"
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) { /* silent */ }
        })

        // Load application count
        ApiClient.getApi().getMyApplications(token).enqueue(object : Callback<List<JsonObject>> {
            override fun onResponse(call: Call<List<JsonObject>>, response: Response<List<JsonObject>>) {
                if (response.isSuccessful && response.body() != null) {
                    findViewById<TextView>(R.id.tvApplicationCount).text = response.body()!!.size.toString()
                }
            }

            override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) { /* silent */ }
        })

        // Load unread notification count
        ApiClient.getApi().getUnreadCount(token).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh).isRefreshing = false
                if (response.isSuccessful && response.body() != null) {
                    val count = response.body()!!.get("count")?.asInt ?: 0
                    findViewById<android.view.View>(R.id.viewNotificationBadge).visibility = 
                        if (count > 0) android.view.View.VISIBLE else android.view.View.GONE
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh).isRefreshing = false
            }
        })
    }
}

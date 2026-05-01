package com.smartcampus.app.ui.jobs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smartcampus.app.R
import com.smartcampus.app.api.ApiClient
import com.smartcampus.app.models.Job
import com.smartcampus.app.utils.SessionManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JobListActivity : AppCompatActivity() {
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_list_student)
        session = SessionManager(this)
        loadJobs()
    }

    private fun loadJobs() {
        ApiClient.getApi().getJobs(session.authToken, null, null, null).enqueue(object : Callback<List<Job>> {
            override fun onResponse(call: Call<List<Job>>, response: Response<List<Job>>) {
                if (response.isSuccessful && response.body() != null) {
                    displayJobs(response.body()!!)
                }
            }
            override fun onFailure(call: Call<List<Job>>, t: Throwable) {
                Toast.makeText(this@JobListActivity, "Error loading jobs", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayJobs(jobs: List<Job>) {
        val container = findViewById<LinearLayout>(R.id.layoutJobs)
        container.removeAllViews()
        jobs.forEach { job ->
            val card = LayoutInflater.from(this).inflate(R.layout.item_job, container, false)
            card.findViewById<TextView>(R.id.tvJobTitle).text = job.title
            card.findViewById<TextView>(R.id.tvCompany).text = job.companyName
            card.findViewById<TextView>(R.id.tvLocation).text = "📍 ${job.location}"
            card.findViewById<TextView>(R.id.tvSalary).text = "💰 ${job.salaryPackage}"
            card.findViewById<TextView>(R.id.tvJobType).text = job.jobType
            card.setOnClickListener {
                val intent = Intent(this, JobDetailActivity::class.java)
                intent.putExtra("jobId", job.id)
                startActivity(intent)
            }
            container.addView(card)
        }
    }
}

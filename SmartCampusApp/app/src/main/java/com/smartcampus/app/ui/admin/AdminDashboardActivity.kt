package com.smartcampus.app.ui.admin

import com.smartcampus.app.R

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.smartcampus.app.ui.admin.repositories.AdminRepository
import com.smartcampus.app.ui.admin.services.AdminService
import com.smartcampus.app.databinding.ActivityMainBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updateDashboardStats()
    }

    private fun updateDashboardStats() {
        val token = com.smartcampus.app.utils.SessionManager(this).authToken
        com.smartcampus.app.api.ApiClient.getApi().getSystemStats(token).enqueue(object : retrofit2.Callback<com.google.gson.JsonObject> {
            override fun onResponse(call: retrofit2.Call<com.google.gson.JsonObject>, response: retrofit2.Response<com.google.gson.JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    binding.tvTotalStudents.text = (stats.get("totalStudents")?.asInt ?: 0).toString()
                    binding.tvTotalJobs.text = (stats.get("totalJobs")?.asInt ?: 0).toString()
                    binding.tvTotalApps.text = (stats.get("totalApplications")?.asInt ?: 0).toString()
                } else {
                    Toast.makeText(this@AdminDashboardActivity, "Failed to load stats", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.google.gson.JsonObject>, t: Throwable) {
                Toast.makeText(this@AdminDashboardActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun setupListeners() {
        binding.btnManageColleges.setOnClickListener {
            startActivity(Intent(this, CollegeActivity::class.java))
        }

        binding.btnManageOfficers.setOnClickListener {
            startActivity(Intent(this, OfficerActivity::class.java))
        }

        binding.btnManageStudents.setOnClickListener {
            startActivity(Intent(this, StudentActivity::class.java))
        }

        binding.btnManageJobs.setOnClickListener {
            startActivity(Intent(this, JobActivity::class.java))
        }

        binding.btnManageCompanies.setOnClickListener {
            startActivity(Intent(this, CompanyActivity::class.java))
        }

        binding.btnManageApplications.setOnClickListener {
            startActivity(Intent(this, ApplicationActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            com.smartcampus.app.utils.SessionManager(this).logout()
            val intent = Intent(this, com.smartcampus.app.ui.auth.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}

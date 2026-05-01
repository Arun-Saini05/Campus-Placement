package com.smartcampus.app.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.smartcampus.app.ui.admin.repositories.AdminRepository
import com.smartcampus.app.ui.admin.services.AdminService
import com.smartcampus.app.databinding.ActivityMainBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adminService: AdminService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Services
        
        adminService = AdminService()
        
        // Add Sample Data
        adminService.addCollege("Sample Tech Institute", "Sample City")
        adminService.addRecruitmentOfficer("Jane Placement", "jane@tech.com", "TechCorp")
        adminService.addStudent("John Doe", "john@example.com", "Computer Science")
        adminService.addJob("Software Engineer", "TechCorp", "Bangalore", "12 LPA")
        adminService.addCompany("TechCorp", "IT/Software", "Bangalore")
        adminService.addApplication("John Doe", "Software Engineer", "Applied")

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updateDashboardStats()
    }

    private fun updateDashboardStats() {
        val totalStudents = adminService.viewAllStudents().size
        val totalJobs = adminService.viewAllJobs().size
        val totalApps = adminService.viewAllApplications().size

        binding.tvTotalStudents.text = totalStudents.toString()
        binding.tvTotalJobs.text = totalJobs.toString()
        binding.tvTotalApps.text = totalApps.toString()
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

    }
}

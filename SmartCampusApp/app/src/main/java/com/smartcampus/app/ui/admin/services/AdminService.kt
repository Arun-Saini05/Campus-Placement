package com.smartcampus.app.ui.admin.services

import com.smartcampus.app.ui.admin.models.College
import com.smartcampus.app.ui.admin.models.RecruitmentOfficer
import com.smartcampus.app.ui.admin.models.Student
import com.smartcampus.app.ui.admin.models.Job
import com.smartcampus.app.ui.admin.models.Company
import com.smartcampus.app.ui.admin.models.Application
import com.smartcampus.app.ui.admin.repositories.AdminRepository

class AdminService(private val repository: AdminRepository = AdminRepository) {

    // --- College Management ---

    fun addCollege(name: String, location: String): College {
        if (name.isBlank() || location.isBlank()) {
            throw IllegalArgumentException("College name and location cannot be blank.")
        }
        return repository.addCollege(name, location)
    }

    fun viewAllColleges(): List<College> {
        return repository.getAllColleges()
    }

    fun viewCollegeDetails(id: Int): College? {
        return repository.getCollege(id)
    }

    fun updateCollege(id: Int, newName: String, newLocation: String): Boolean {
        if (newName.isBlank() || newLocation.isBlank()) {
            throw IllegalArgumentException("College name and location cannot be blank.")
        }
        return repository.updateCollege(id, newName, newLocation)
    }

    fun deleteCollege(id: Int): Boolean {
        return repository.deleteCollege(id)
    }

    // --- Recruitment Officer Management ---

    fun addRecruitmentOfficer(name: String, email: String, company: String): RecruitmentOfficer {
        if (name.isBlank() || email.isBlank() || company.isBlank()) {
            throw IllegalArgumentException("Name, email, and company cannot be blank.")
        }
        return repository.addRecruitmentOfficer(name, email, company)
    }

    fun viewAllRecruitmentOfficers(): List<RecruitmentOfficer> {
        return repository.getAllRecruitmentOfficers()
    }

    fun viewRecruitmentOfficerDetails(id: Int): RecruitmentOfficer? {
        return repository.getRecruitmentOfficer(id)
    }

    fun updateRecruitmentOfficer(id: Int, newName: String, newEmail: String, newCompany: String): Boolean {
        if (newName.isBlank() || newEmail.isBlank() || newCompany.isBlank()) {
            throw IllegalArgumentException("Name, email, and company cannot be blank.")
        }
        return repository.updateRecruitmentOfficer(id, newName, newEmail, newCompany)
    }

    fun deleteRecruitmentOfficer(id: Int): Boolean {
        return repository.deleteRecruitmentOfficer(id)
    }

    // --- Student Management ---

    fun addStudent(name: String, email: String, branch: String): Student {
        if (name.isBlank() || email.isBlank() || branch.isBlank()) {
            throw IllegalArgumentException("Name, email, and branch cannot be blank.")
        }
        return repository.addStudent(name, email, branch)
    }

    fun viewAllStudents(): List<Student> {
        return repository.getAllStudents()
    }

    fun viewStudentDetails(id: Int): Student? {
        return repository.getStudent(id)
    }

    fun updateStudent(id: Int, newName: String, newEmail: String, newBranch: String): Boolean {
        if (newName.isBlank() || newEmail.isBlank() || newBranch.isBlank()) {
            throw IllegalArgumentException("Name, email, and branch cannot be blank.")
        }
        return repository.updateStudent(id, newName, newEmail, newBranch)
    }

    fun deleteStudent(id: Int): Boolean {
        return repository.deleteStudent(id)
    }

    // --- Job Management ---

    fun addJob(title: String, companyName: String, location: String, salaryPackage: String): Job {
        if (title.isBlank() || companyName.isBlank() || location.isBlank() || salaryPackage.isBlank()) {
            throw IllegalArgumentException("All job fields must be filled.")
        }
        return repository.addJob(title, companyName, location, salaryPackage)
    }

    fun viewAllJobs(): List<Job> {
        return repository.getAllJobs()
    }

    fun viewJobDetails(id: Int): Job? {
        return repository.getJob(id)
    }

    fun updateJob(id: Int, newTitle: String, newCompanyName: String, newLocation: String, newSalaryPackage: String): Boolean {
        if (newTitle.isBlank() || newCompanyName.isBlank() || newLocation.isBlank() || newSalaryPackage.isBlank()) {
            throw IllegalArgumentException("All job fields must be filled.")
        }
        return repository.updateJob(id, newTitle, newCompanyName, newLocation, newSalaryPackage)
    }

    fun deleteJob(id: Int): Boolean {
        return repository.deleteJob(id)
    }

    // --- Company Management ---

    fun addCompany(name: String, industry: String, location: String): Company {
        if (name.isBlank() || industry.isBlank() || location.isBlank()) {
            throw IllegalArgumentException("Company fields cannot be blank.")
        }
        return repository.addCompany(name, industry, location)
    }

    fun viewAllCompanies(): List<Company> {
        return repository.getAllCompanies()
    }

    fun viewCompanyDetails(id: Int): Company? {
        return repository.getCompany(id)
    }

    fun updateCompany(id: Int, newName: String, newIndustry: String, newLocation: String): Boolean {
        if (newName.isBlank() || newIndustry.isBlank() || newLocation.isBlank()) {
            throw IllegalArgumentException("Company fields cannot be blank.")
        }
        return repository.updateCompany(id, newName, newIndustry, newLocation)
    }

    fun deleteCompany(id: Int): Boolean {
        return repository.deleteCompany(id)
    }

    // --- Application Management ---

    fun addApplication(studentName: String, jobTitle: String, status: String): Application {
        if (studentName.isBlank() || jobTitle.isBlank() || status.isBlank()) {
            throw IllegalArgumentException("Application fields cannot be blank.")
        }
        return repository.addApplication(studentName, jobTitle, status)
    }

    fun viewAllApplications(): List<Application> {
        return repository.getAllApplications()
    }

    fun viewApplicationDetails(id: Int): Application? {
        return repository.getApplication(id)
    }

    fun updateApplication(id: Int, newStudentName: String, newJobTitle: String, newStatus: String): Boolean {
        if (newStudentName.isBlank() || newJobTitle.isBlank() || newStatus.isBlank()) {
            throw IllegalArgumentException("Application fields cannot be blank.")
        }
        return repository.updateApplication(id, newStudentName, newJobTitle, newStatus)
    }

    fun deleteApplication(id: Int): Boolean {
        return repository.deleteApplication(id)
    }
}

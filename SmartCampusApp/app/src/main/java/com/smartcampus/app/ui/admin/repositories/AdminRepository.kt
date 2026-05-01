package com.smartcampus.app.ui.admin.repositories

import com.smartcampus.app.ui.admin.models.College
import com.smartcampus.app.ui.admin.models.RecruitmentOfficer
import com.smartcampus.app.ui.admin.models.Student
import com.smartcampus.app.ui.admin.models.Job
import com.smartcampus.app.ui.admin.models.Company
import com.smartcampus.app.ui.admin.models.Application

object AdminRepository {
    private val colleges = mutableListOf<College>()
    private val officers = mutableListOf<RecruitmentOfficer>()
    private val students = mutableListOf<Student>()
    private val jobs = mutableListOf<Job>()
    private val companies = mutableListOf<Company>()
    private val applications = mutableListOf<Application>()
    
    private var nextCollegeId = 1
    private var nextOfficerId = 1
    private var nextStudentId = 1
    private var nextJobId = 1
    private var nextCompanyId = 1
    private var nextApplicationId = 1

    // --- College Operations ---
    fun addCollege(name: String, location: String): College {
        val college = College(nextCollegeId++, name, location)
        colleges.add(college)
        return college
    }

    fun getCollege(id: Int): College? {
        return colleges.find { it.id == id }
    }

    fun getAllColleges(): List<College> {
        return colleges.toList()
    }

    fun updateCollege(id: Int, newName: String, newLocation: String): Boolean {
        val college = getCollege(id)
        if (college != null) {
            college.name = newName
            college.location = newLocation
            return true
        }
        return false
    }

    fun deleteCollege(id: Int): Boolean {
        return colleges.removeIf { it.id == id }
    }

    // --- Recruitment Officer Operations ---
    fun addRecruitmentOfficer(name: String, email: String, company: String): RecruitmentOfficer {
        val officer = RecruitmentOfficer(nextOfficerId++, name, email, company)
        officers.add(officer)
        return officer
    }

    fun getRecruitmentOfficer(id: Int): RecruitmentOfficer? {
        return officers.find { it.id == id }
    }

    fun getAllRecruitmentOfficers(): List<RecruitmentOfficer> {
        return officers.toList()
    }

    fun updateRecruitmentOfficer(id: Int, newName: String, newEmail: String, newCompany: String): Boolean {
        val officer = getRecruitmentOfficer(id)
        if (officer != null) {
            officer.name = newName
            officer.email = newEmail
            officer.company = newCompany
            return true
        }
        return false
    }

    fun deleteRecruitmentOfficer(id: Int): Boolean {
        return officers.removeIf { it.id == id }
    }

    // --- Student Operations ---
    fun addStudent(name: String, email: String, branch: String): Student {
        val student = Student(nextStudentId++, name, email, branch)
        students.add(student)
        return student
    }

    fun getStudent(id: Int): Student? {
        return students.find { it.id == id }
    }

    fun getAllStudents(): List<Student> {
        return students.toList()
    }

    fun updateStudent(id: Int, newName: String, newEmail: String, newBranch: String): Boolean {
        val student = getStudent(id)
        if (student != null) {
            student.name = newName
            student.email = newEmail
            student.branch = newBranch
            return true
        }
        return false
    }

    fun deleteStudent(id: Int): Boolean {
        return students.removeIf { it.id == id }
    }

    // --- Job Operations ---
    fun addJob(title: String, companyName: String, location: String, salaryPackage: String): Job {
        val job = Job(nextJobId++, title, companyName, location, salaryPackage)
        jobs.add(job)
        return job
    }

    fun getJob(id: Int): Job? {
        return jobs.find { it.id == id }
    }

    fun getAllJobs(): List<Job> {
        return jobs.toList()
    }

    fun updateJob(id: Int, newTitle: String, newCompanyName: String, newLocation: String, newSalaryPackage: String): Boolean {
        val job = getJob(id)
        if (job != null) {
            job.title = newTitle
            job.companyName = newCompanyName
            job.location = newLocation
            job.salaryPackage = newSalaryPackage
            return true
        }
        return false
    }

    fun deleteJob(id: Int): Boolean {
        return jobs.removeIf { it.id == id }
    }

    // --- Company Operations ---
    fun addCompany(name: String, industry: String, location: String): Company {
        val company = Company(nextCompanyId++, name, industry, location)
        companies.add(company)
        return company
    }

    fun getCompany(id: Int): Company? {
        return companies.find { it.id == id }
    }

    fun getAllCompanies(): List<Company> {
        return companies.toList()
    }

    fun updateCompany(id: Int, newName: String, newIndustry: String, newLocation: String): Boolean {
        val company = getCompany(id)
        if (company != null) {
            company.name = newName
            company.industry = newIndustry
            company.location = newLocation
            return true
        }
        return false
    }

    fun deleteCompany(id: Int): Boolean {
        return companies.removeIf { it.id == id }
    }

    // --- Application Operations ---
    fun addApplication(studentName: String, jobTitle: String, status: String): Application {
        val application = Application(nextApplicationId++, studentName, jobTitle, status)
        applications.add(application)
        return application
    }

    fun getApplication(id: Int): Application? {
        return applications.find { it.id == id }
    }

    fun getAllApplications(): List<Application> {
        return applications.toList()
    }

    fun updateApplication(id: Int, newStudentName: String, newJobTitle: String, newStatus: String): Boolean {
        val application = getApplication(id)
        if (application != null) {
            application.studentName = newStudentName
            application.jobTitle = newJobTitle
            application.status = newStatus
            return true
        }
        return false
    }

    fun deleteApplication(id: Int): Boolean {
        return applications.removeIf { it.id == id }
    }
}

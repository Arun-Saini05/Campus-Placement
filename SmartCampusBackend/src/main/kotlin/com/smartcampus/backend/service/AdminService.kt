package com.smartcampus.backend.service

import com.smartcampus.backend.dto.*
import com.smartcampus.backend.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

class AdminService {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun getAllUsers(): List<UserManagementResponse> {
        return transaction {
            Users.selectAll()
                .orderBy(Users.createdAt to SortOrder.DESC)
                .map { row ->
                    UserManagementResponse(
                        id = row[Users.id],
                        name = row[Users.name],
                        email = row[Users.email],
                        role = row[Users.role],
                        isActive = row[Users.isActive],
                        createdAt = row[Users.createdAt].format(formatter)
                    )
                }
        }
    }

    fun getPendingRecruiters(): List<UserManagementResponse> {
        return transaction {
            Users.select { (Users.role eq "RECRUITER") and (Users.isActive eq false) }
                .map { row ->
                    UserManagementResponse(
                        id = row[Users.id],
                        name = row[Users.name],
                        email = row[Users.email],
                        role = row[Users.role],
                        isActive = row[Users.isActive],
                        createdAt = row[Users.createdAt].format(formatter)
                    )
                }
        }
    }

    fun approveRecruiter(recruiterId: Int): MessageResponse {
        transaction {
            Users.update({ Users.id eq recruiterId }) {
                it[isActive] = true
            }
        }
        return MessageResponse("Recruiter approved successfully")
    }

    fun toggleUserStatus(userId: Int, activate: Boolean): MessageResponse {
        transaction {
            Users.update({ Users.id eq userId }) {
                it[isActive] = activate
            }
        }
        val status = if (activate) "activated" else "deactivated"
        return MessageResponse("User $status successfully")
    }

    fun getSystemStats(): AdminStatsResponse {
        return transaction {
            val collegeWisePlacementPercentage = mutableMapOf<String, Double>()
            val collegesList = Colleges.selectAll().map { it[Colleges.id] to it[Colleges.name] }
            collegesList.forEach { (collegeId, name) ->
                val totalStuds = Users.select { (Users.role eq "STUDENT") and (Users.collegeId eq collegeId) }.count()
                if (totalStuds > 0) {
                    val studentIds = Users.select { (Users.role eq "STUDENT") and (Users.collegeId eq collegeId) }.map { it[Users.id] }
                    val placedCount = StudentProfiles.select { (StudentProfiles.userId inList studentIds) and (StudentProfiles.placementStatus eq "PLACED") }.count()
                    collegeWisePlacementPercentage[name] = Math.round((placedCount.toDouble() / totalStuds.toDouble() * 100.0) * 10.0) / 10.0
                } else {
                    collegeWisePlacementPercentage[name] = 0.0
                }
            }

            val companyWiseHiringCount = mutableMapOf<String, Int>()
            (JobApplications innerJoin Jobs)
                .select { JobApplications.status eq "SELECTED" }
                .forEach { row ->
                    val companyName = row[Jobs.companyName]
                    companyWiseHiringCount[companyName] = companyWiseHiringCount.getOrDefault(companyName, 0) + 1
                }

            val departmentWisePlacementStats = mutableMapOf<String, Int>()
            StudentProfiles.select { StudentProfiles.placementStatus eq "PLACED" }
                .forEach { row ->
                    val branch = row[StudentProfiles.branch] ?: "N/A"
                    departmentWisePlacementStats[branch] = departmentWisePlacementStats.getOrDefault(branch, 0) + 1
                }

            val monthlyPlacementTrends = mutableMapOf<String, Int>()
            JobApplications.select { JobApplications.status eq "SELECTED" }
                .forEach { row ->
                    val date = row[JobApplications.updatedAt]
                    val monthKey = "%d-%02d".format(date.year, date.monthValue)
                    monthlyPlacementTrends[monthKey] = monthlyPlacementTrends.getOrDefault(monthKey, 0) + 1
                }

            AdminStatsResponse(
                totalUsers = Users.selectAll().count(),
                activeUsers = Users.select { Users.isActive eq true }.count(),
                totalStudents = Users.select { Users.role eq "STUDENT" }.count(),
                totalRecruiters = Users.select { Users.role eq "RECRUITER" }.count(),
                totalOfficers = Users.select { Users.role eq "PLACEMENT_OFFICER" }.count(),
                totalJobs = Jobs.selectAll().count(),
                totalApplications = JobApplications.selectAll().count(),
                totalDrives = PlacementDrives.selectAll().count(),
                totalColleges = Colleges.selectAll().count(),
                totalCompanies = Companies.selectAll().count(),
                trendingSkillsCount = TrendingSkills.selectAll().count(),
                collegeWisePlacementPercentage = collegeWisePlacementPercentage,
                companyWiseHiringCount = companyWiseHiringCount,
                departmentWisePlacementStats = departmentWisePlacementStats,
                monthlyPlacementTrends = monthlyPlacementTrends
            )
        }
    }

    // --- College Management ---
    fun getAllColleges(): List<CollegeResponse> = transaction {
        Colleges.selectAll().map { row ->
            CollegeResponse(
                id = row[Colleges.id],
                name = row[Colleges.name],
                location = row[Colleges.location]
            )
        }
    }

    fun addCollege(name: String, location: String) = transaction {
        Colleges.insert {
            it[Colleges.name] = name
            it[Colleges.location] = location
            it[createdAt] = java.time.LocalDateTime.now()
        }
    }

    fun deleteCollege(id: Int) = transaction {
        Colleges.deleteWhere { Colleges.id eq id }
    }

    // --- Company Management ---
    fun getAllCompanies(): List<CompanyResponse> = transaction {
        Companies.selectAll().map { row ->
            CompanyResponse(
                id = row[Companies.id],
                name = row[Companies.name],
                industry = row[Companies.industry] ?: "",
                location = row[Companies.location] ?: ""
            )
        }
    }

    fun addCompany(name: String, industry: String, location: String) = transaction {
        Companies.insert {
            it[Companies.name] = name
            it[Companies.industry] = industry
            it[Companies.location] = location
            it[createdAt] = java.time.LocalDateTime.now()
        }
    }

    fun deleteCompany(id: Int) = transaction {
        Companies.deleteWhere { Companies.id eq id }
    }

    // --- Job & Application Management ---
    fun getAllJobs(): List<AdminJobResponse> = transaction {
        Jobs.selectAll().map { row ->
            AdminJobResponse(
                id = row[Jobs.id],
                title = row[Jobs.title],
                companyName = row[Jobs.companyName],
                location = row[Jobs.location] ?: "",
                salaryPackage = row[Jobs.salaryPackage] ?: ""
            )
        }
    }

    fun getAllApplications(): List<AdminApplicationResponse> = transaction {
        (JobApplications innerJoin Users).selectAll().map { row ->
            AdminApplicationResponse(
                id = row[JobApplications.id],
                studentName = row[Users.name],
                jobId = row[JobApplications.jobId],
                status = row[JobApplications.status]
            )
        }
    }

    fun updateCollege(id: Int, name: String, location: String) = transaction {
        Colleges.update({ Colleges.id eq id }) {
            it[Colleges.name] = name
            it[Colleges.location] = location
        }
    }

    fun updateCompany(id: Int, name: String, industry: String, location: String) = transaction {
        Companies.update({ Companies.id eq id }) {
            it[Companies.name] = name
            it[Companies.industry] = industry
            it[Companies.location] = location
        }
    }

    fun addJob(title: String, companyName: String, location: String, salaryPackage: String, description: String, requiredSkills: String, postedBy: Int) = transaction {
        Jobs.insert {
            it[Jobs.title] = title
            it[Jobs.companyName] = companyName
            it[Jobs.location] = location
            it[Jobs.description] = description
            it[Jobs.requiredSkills] = requiredSkills
            it[Jobs.salaryPackage] = salaryPackage
            it[Jobs.jobType] = "FULL_TIME"
            it[Jobs.postedBy] = postedBy
            it[isActive] = true
            it[createdAt] = java.time.LocalDateTime.now()
        }
    }

    fun updateJob(id: Int, title: String, companyName: String, location: String, salaryPackage: String) = transaction {
        Jobs.update({ Jobs.id eq id }) {
            it[Jobs.title] = title
            it[Jobs.companyName] = companyName
            it[Jobs.location] = location
            it[Jobs.salaryPackage] = salaryPackage
        }
    }

    fun deleteJob(id: Int) = transaction {
        JobApplications.deleteWhere { JobApplications.jobId eq id }
        Jobs.deleteWhere { Jobs.id eq id }
    }

    fun updateUser(userId: Int, name: String, email: String, branch: String? = null) = transaction {
        Users.update({ Users.id eq userId }) {
            it[Users.name] = name
            it[Users.email] = email
        }
        if (branch != null) {
            val profile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull()
            if (profile != null) {
                StudentProfiles.update({ StudentProfiles.userId eq userId }) {
                    it[StudentProfiles.branch] = branch
                }
            } else {
                StudentProfiles.insert {
                    it[StudentProfiles.userId] = userId
                    it[StudentProfiles.branch] = branch
                    it[StudentProfiles.semester] = 1
                    it[StudentProfiles.cgpa] = 0.0f
                    it[StudentProfiles.createdAt] = java.time.LocalDateTime.now()
                    it[StudentProfiles.updatedAt] = java.time.LocalDateTime.now()
                }
            }
        }
    }
}

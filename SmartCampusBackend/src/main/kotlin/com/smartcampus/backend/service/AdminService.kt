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

    fun getSystemStats(): Map<String, Any> {
        return transaction {
            mapOf(
                "totalUsers" to Users.selectAll().count(),
                "activeUsers" to Users.select { Users.isActive eq true }.count(),
                "totalStudents" to Users.select { Users.role eq "STUDENT" }.count(),
                "totalRecruiters" to Users.select { Users.role eq "RECRUITER" }.count(),
                "totalOfficers" to Users.select { Users.role eq "PLACEMENT_OFFICER" }.count(),
                "totalJobs" to Jobs.selectAll().count(),
                "totalApplications" to JobApplications.selectAll().count(),
                "totalDrives" to PlacementDrives.selectAll().count(),
                "totalColleges" to Colleges.selectAll().count(),
                "totalCompanies" to Companies.selectAll().count(),
                "trendingSkillsCount" to TrendingSkills.selectAll().count()
            )
        }
    }

    // --- College Management ---
    fun getAllColleges(): List<Map<String, Any>> = transaction {
        Colleges.selectAll().map { row ->
            mapOf<String, Any>(
                "id" to row[Colleges.id],
                "name" to row[Colleges.name],
                "location" to row[Colleges.location]
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
    fun getAllCompanies(): List<Map<String, Any>> = transaction {
        Companies.selectAll().map { row ->
            mapOf<String, Any>(
                "id" to row[Companies.id],
                "name" to row[Companies.name],
                "industry" to (row[Companies.industry] ?: ""),
                "location" to (row[Companies.location] ?: "")
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
    fun getAllJobs(): List<Map<String, Any>> = transaction {
        Jobs.selectAll().map { row ->
            mapOf<String, Any>(
                "id" to row[Jobs.id],
                "title" to row[Jobs.title],
                "companyName" to row[Jobs.companyName],
                "location" to (row[Jobs.location] ?: ""),
                "salaryPackage" to (row[Jobs.salaryPackage] ?: "")
            )
        }
    }

    fun getAllApplications(): List<Map<String, Any>> = transaction {
        (JobApplications innerJoin Users).selectAll().map { row ->
            mapOf<String, Any>(
                "id" to row[JobApplications.id],
                "studentName" to row[Users.name],
                "jobId" to row[JobApplications.jobId],
                "status" to row[JobApplications.status]
            )
        }
    }
}

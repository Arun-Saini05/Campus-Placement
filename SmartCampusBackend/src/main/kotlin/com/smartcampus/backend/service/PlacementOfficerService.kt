package com.smartcampus.backend.service

import com.smartcampus.backend.dto.*
import com.smartcampus.backend.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PlacementOfficerService {

    private val notificationService = NotificationService()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun createDrive(userId: Int, request: PlacementDriveRequest): MessageResponse {
        transaction {
            PlacementDrives.insert {
                it[companyName] = request.companyName
                it[description] = request.description
                it[eligibilityCriteria] = request.eligibilityCriteria
                it[minCgpa] = request.minCgpa
                it[allowedBranches] = request.allowedBranches?.joinToString(",")
                it[salaryPackage] = request.salaryPackage
                it[driveDate] = request.driveDate?.let { d -> LocalDateTime.parse(d, formatter) }
                it[postedBy] = userId
                it[createdAt] = LocalDateTime.now()
            }
        }
        
        notificationService.createNotification(
            userId = userId,
            title = "Drive Created: ${request.companyName}",
            message = "New placement drive for ${request.companyName} added successfully.",
            type = "DRIVE_CREATED"
        )

        return MessageResponse("Placement drive created successfully")
    }

    fun getAllDrives(): List<PlacementDriveResponse> {
        return transaction {
            PlacementDrives.selectAll()
                .orderBy(PlacementDrives.createdAt to SortOrder.DESC)
                .map { row ->
                    val eligibleCount = getEligibleStudentCount(
                        row[PlacementDrives.minCgpa],
                        row[PlacementDrives.allowedBranches]
                    )
                    PlacementDriveResponse(
                        id = row[PlacementDrives.id],
                        companyName = row[PlacementDrives.companyName],
                        description = row[PlacementDrives.description],
                        eligibilityCriteria = row[PlacementDrives.eligibilityCriteria],
                        minCgpa = row[PlacementDrives.minCgpa],
                        allowedBranches = row[PlacementDrives.allowedBranches]?.split(",")?.map { b -> b.trim() },
                        salaryPackage = row[PlacementDrives.salaryPackage],
                        driveDate = row[PlacementDrives.driveDate]?.format(formatter),
                        eligibleStudentCount = eligibleCount
                    )
                }
        }
    }

    fun getEligibleStudents(driveId: Int): List<CandidateResponse> {
        return transaction {
            val drive = PlacementDrives.select { PlacementDrives.id eq driveId }.singleOrNull()
                ?: throw IllegalArgumentException("Drive not found")

            val minCgpa = drive[PlacementDrives.minCgpa]
            val allowedBranches = drive[PlacementDrives.allowedBranches]?.split(",")?.map { b -> b.trim() }

            var query = (StudentProfiles innerJoin Users)
                .select { Users.role eq "STUDENT" }

            minCgpa?.let { cgpa ->
                query = query.andWhere { StudentProfiles.cgpa greaterEq cgpa }
            }

            query.map { row ->
                val profileId = row[StudentProfiles.id]
                val skills = (StudentSkills innerJoin Skills)
                    .select { StudentSkills.studentProfileId eq profileId }
                    .map { r -> r[Skills.name] }

                CandidateResponse(
                    userId = row[Users.id],
                    name = row[Users.name],
                    email = row[Users.email],
                    semester = row[StudentProfiles.semester],
                    branch = row[StudentProfiles.branch],
                    cgpa = row[StudentProfiles.cgpa],
                    skills = skills,
                    resumeUrl = row[StudentProfiles.resumeUrl]
                )
            }.filter { candidate ->
                allowedBranches == null || candidate.branch in allowedBranches
            }
        }
    }

    private fun getEligibleStudentCount(minCgpa: Float?, allowedBranches: String?): Int {
        return transaction {
            var query = StudentProfiles.selectAll()
            minCgpa?.let { cgpa ->
                query = query.andWhere { StudentProfiles.cgpa greaterEq cgpa }
            }
            allowedBranches?.let { branches ->
                val branchList = branches.split(",").map { b -> b.trim() }
                query = query.andWhere { StudentProfiles.branch inList branchList }
            }
            query.count().toInt()
        }
    }

    fun getPlacementStats(): OfficerDashboardResponse {
        return transaction {
            val totalStudents = Users.select { Users.role eq "STUDENT" }.count().toInt()
            
            // Get all successful applications with their associated job data
            val successfulApps = (JobApplications innerJoin Jobs)
                .select { JobApplications.status eq "SELECTED" }
            
            val totalOffers = successfulApps.count().toInt()
            val placedStudentIds = successfulApps.map { it[JobApplications.studentId] }.distinct()
            val placedStudentsCount = placedStudentIds.size
            val unplacedStudents = totalStudents - placedStudentsCount

            // Parse and calculate packages
            val packages = successfulApps.mapNotNull { row ->
                val pkgStr = row[Jobs.salaryPackage] ?: return@mapNotNull null
                // Extract numeric value from string like "12 LPA" or "₹ 8.5"
                pkgStr.replace(Regex("[^0-9.]"), "").toDoubleOrNull()
            }

            val highestPkgVal = packages.maxOrNull() ?: 0.0
            val averagePkgVal = if (packages.isNotEmpty()) packages.average() else 0.0
            
            val highestPackage = if (highestPkgVal > 0) "%.1f LPA".format(highestPkgVal) else "N/A"
            val averagePackage = if (averagePkgVal > 0) "%.1f LPA".format(averagePkgVal) else "N/A"
            
            val departmentPlacements = mutableMapOf<String, Int>()
            if (placedStudentIds.isNotEmpty()) {
                StudentProfiles.select { StudentProfiles.userId inList placedStudentIds }
                    .forEach { row ->
                        val branch = row[StudentProfiles.branch] ?: "Unknown"
                        departmentPlacements[branch] = departmentPlacements.getOrDefault(branch, 0) + 1
                    }
            }

            val activeDrives = PlacementDrives.selectAll()
                .orderBy(PlacementDrives.createdAt to SortOrder.DESC)
                .limit(5)
                .map { row ->
                    PlacementDriveResponse(
                        id = row[PlacementDrives.id],
                        companyName = row[PlacementDrives.companyName],
                        description = row[PlacementDrives.description],
                        eligibilityCriteria = row[PlacementDrives.eligibilityCriteria],
                        minCgpa = row[PlacementDrives.minCgpa],
                        allowedBranches = row[PlacementDrives.allowedBranches]?.split(",")?.map { b -> b.trim() },
                        salaryPackage = row[PlacementDrives.salaryPackage],
                        driveDate = row[PlacementDrives.driveDate]?.format(formatter),
                        eligibleStudentCount = getEligibleStudentCount(
                            row[PlacementDrives.minCgpa],
                            row[PlacementDrives.allowedBranches]
                        )
                    )
                }

            OfficerDashboardResponse(
                totalStudents = totalStudents,
                placedStudents = placedStudentsCount,
                unplacedStudents = unplacedStudents,
                highestPackage = highestPackage,
                averagePackage = averagePackage,
                totalOffers = totalOffers,
                activeDrives = activeDrives,
                departmentWisePlacements = departmentPlacements
            )
        }
    }

    fun searchStudents(branchFilter: String?, minCgpaFilter: Float?, statusFilter: String?): List<OfficerStudentResponse> {
        return transaction {
            // Get all placed student IDs
            val placedStudentIds = JobApplications
                .select { JobApplications.status eq "SELECTED" }
                .map { it[JobApplications.studentId] }
                .toSet()

            var query = (StudentProfiles innerJoin Users)
                .select { Users.role eq "STUDENT" }

            branchFilter?.let { b ->
                if (b.isNotBlank() && b != "All Branches") {
                    query = query.andWhere { StudentProfiles.branch eq b }
                }
            }

            minCgpaFilter?.let { cgpa ->
                if (cgpa > 0) {
                    query = query.andWhere { StudentProfiles.cgpa greaterEq cgpa }
                }
            }

            query.map { row ->
                val userId = row[Users.id]
                val profileId = row[StudentProfiles.id]
                
                val skills = (StudentSkills innerJoin Skills)
                    .select { StudentSkills.studentProfileId eq profileId }
                    .map { r -> r[Skills.name] }

                val isPlaced = placedStudentIds.contains(userId)
                val status = if (isPlaced) "PLACED" else "UNPLACED"

                OfficerStudentResponse(
                    userId = userId,
                    name = row[Users.name],
                    email = row[Users.email],
                    semester = row[StudentProfiles.semester],
                    branch = row[StudentProfiles.branch],
                    cgpa = row[StudentProfiles.cgpa],
                    skills = skills,
                    placementStatus = status
                )
            }.filter { student ->
                if (statusFilter.isNullOrBlank() || statusFilter == "ALL") {
                    true
                } else {
                    student.placementStatus.equals(statusFilter, ignoreCase = true)
                }
            }
        }
    }

    fun sendMassNotification(userId: Int, request: MassNotificationRequest): MessageResponse {
        return transaction {
            var targetUsers = Users.select { Users.role eq "STUDENT" }.map { it[Users.id] }.toSet()

            if (request.targetAudience != "ALL") {
                if (request.targetAudience == "PLACED" || request.targetAudience == "UNPLACED") {
                    val placedStudentIds = JobApplications
                        .select { JobApplications.status eq "SELECTED" }
                        .map { it[JobApplications.studentId] }
                        .toSet()
                    
                    targetUsers = if (request.targetAudience == "PLACED") {
                        targetUsers.intersect(placedStudentIds)
                    } else {
                        targetUsers.subtract(placedStudentIds)
                    }
                } else {
                    // Assume it's a specific branch name
                    val branchUsers = StudentProfiles
                        .select { StudentProfiles.branch eq request.targetAudience }
                        .map { it[StudentProfiles.userId] }
                        .toSet()
                    targetUsers = targetUsers.intersect(branchUsers)
                }
            }

            if (targetUsers.isEmpty()) {
                return@transaction MessageResponse("No students found matching the target audience.", false)
            }

            val now = LocalDateTime.now()
            
            Notifications.batchInsert(targetUsers) { targetId ->
                this[Notifications.userId] = targetId
                this[Notifications.title] = request.title
                this[Notifications.message] = request.message
                this[Notifications.type] = request.type
                this[Notifications.isRead] = false
                this[Notifications.createdAt] = now
            }

            // Create notification for officer
            notificationService.createNotification(
                userId = userId,
                title = "Mass Announcement Sent",
                message = "Broadcasted '${request.title}' to ${targetUsers.size} students.",
                type = "ANNOUNCEMENT_SENT"
            )

            MessageResponse("Notification sent successfully to ${targetUsers.size} students.", true)
        }
    }

    fun deleteDrive(driveId: Int, userId: Int): MessageResponse {
        return transaction {
            val drive = PlacementDrives.select { PlacementDrives.id eq driveId }.singleOrNull()
                ?: return@transaction MessageResponse("Drive not found", false)
            
            val company = drive[PlacementDrives.companyName]
            
            // Delete associated applications first
            JobApplications.deleteWhere { jobId eq driveId }
            
            PlacementDrives.deleteWhere { id eq driveId }
            
            notificationService.createNotification(
                userId = userId,
                title = "Drive Removed: $company",
                message = "The placement drive for $company has been cancelled and removed.",
                type = "DRIVE_DELETED"
            )
            
            MessageResponse("Drive removed successfully", true)
        }
    }
}

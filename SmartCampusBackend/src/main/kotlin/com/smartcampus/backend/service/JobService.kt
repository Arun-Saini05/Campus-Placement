package com.smartcampus.backend.service

import com.smartcampus.backend.dto.*
import com.smartcampus.backend.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JobService {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun getAllJobs(filter: JobFilterRequest? = null, requestingUserId: Int? = null, requestingRole: String? = null): List<JobResponse> {
        return transaction {
            var query = if (requestingRole == "STUDENT" && requestingUserId != null) {
                val studentCollegeId = Users.select { Users.id eq requestingUserId }.singleOrNull()?.get(Users.collegeId)
                if (studentCollegeId != null) {
                    (Jobs innerJoin Users).slice(Jobs.columns).select { (Users.collegeId eq studentCollegeId) and (Jobs.isActive eq true) }
                } else {
                    Jobs.selectAll()
                }
            } else {
                Jobs.selectAll()
            }

            filter?.let { f ->
                f.location?.let { loc ->
                    query = query.andWhere { Jobs.location like "%$loc%" }
                }
                f.jobType?.let { type ->
                    query = query.andWhere { Jobs.jobType eq type }
                }
                f.skill?.let { skill ->
                    query = query.andWhere { Jobs.requiredSkills like "%$skill%" }
                }
            }

            query.orderBy(Jobs.createdAt to SortOrder.DESC).map { row ->
                JobResponse(
                    id = row[Jobs.id],
                    title = row[Jobs.title],
                    companyName = row[Jobs.companyName],
                    location = row[Jobs.location],
                    description = row[Jobs.description],
                    requiredSkills = row[Jobs.requiredSkills].split(",").map { s -> s.trim() },
                    salaryPackage = row[Jobs.salaryPackage],
                    jobType = row[Jobs.jobType],
                    applicationDeadline = row[Jobs.applicationDeadline]?.format(formatter),
                    isActive = row[Jobs.isActive]
                )
            }
        }
    }

    fun getJobById(jobId: Int): JobResponse {
        return transaction {
            val row = Jobs.select { Jobs.id eq jobId }.singleOrNull()
                ?: throw IllegalArgumentException("Job not found")

            JobResponse(
                id = row[Jobs.id],
                title = row[Jobs.title],
                companyName = row[Jobs.companyName],
                location = row[Jobs.location],
                description = row[Jobs.description],
                requiredSkills = row[Jobs.requiredSkills].split(",").map { s -> s.trim() },
                salaryPackage = row[Jobs.salaryPackage],
                jobType = row[Jobs.jobType],
                applicationDeadline = row[Jobs.applicationDeadline]?.format(formatter),
                isActive = row[Jobs.isActive]
            )
        }
    }

    fun createJob(userId: Int, request: JobRequest): MessageResponse {
        transaction {
            Jobs.insert {
                it[title] = request.title
                it[companyName] = request.companyName
                it[location] = request.location
                it[description] = request.description
                it[requiredSkills] = request.requiredSkills.joinToString(", ")
                it[salaryPackage] = request.salaryPackage
                it[jobType] = request.jobType
                it[applicationDeadline] = request.applicationDeadline?.let { d ->
                    LocalDateTime.parse(d, formatter)
                }
                it[postedBy] = userId
                it[isActive] = true
                it[createdAt] = LocalDateTime.now()
            }
        }
        return MessageResponse("Job posted successfully")
    }

    fun applyForJob(studentId: Int, jobId: Int, resumeUrl: String?): MessageResponse {
        transaction {
            val existing = (JobApplications).select {
                (JobApplications.jobId eq jobId) and (JobApplications.studentId eq studentId)
            }.singleOrNull()

            if (existing != null) {
                throw IllegalArgumentException("You have already applied for this job")
            }

            JobApplications.insert {
                it[JobApplications.jobId] = jobId
                it[JobApplications.studentId] = studentId
                it[JobApplications.resumeUrl] = resumeUrl
                it[status] = "APPLIED"
                it[appliedAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }
        }
        return MessageResponse("Application submitted successfully")
    }

    fun getMyApplications(studentId: Int): List<ApplicationResponse> {
        return transaction {
            (JobApplications innerJoin Jobs)
                .select { JobApplications.studentId eq studentId }
                .orderBy(JobApplications.appliedAt to SortOrder.DESC)
                .map { row ->
                    ApplicationResponse(
                        id = row[JobApplications.id],
                        jobId = row[Jobs.id],
                        jobTitle = row[Jobs.title],
                        companyName = row[Jobs.companyName],
                        status = row[JobApplications.status],
                        appliedAt = row[JobApplications.appliedAt].format(formatter)
                    )
                }
        }
    }

    fun updateApplicationStatus(applicationId: Int, status: String): MessageResponse {
        val validStatuses = listOf("APPLIED", "SHORTLISTED", "INTERVIEW_SCHEDULED", "SELECTED", "REJECTED")
        if (status !in validStatuses) {
            throw IllegalArgumentException("Invalid status: $status")
        }

        transaction {
            JobApplications.update({ JobApplications.id eq applicationId }) {
                it[JobApplications.status] = status
                it[updatedAt] = LocalDateTime.now()
            }
            
            val app = JobApplications.select { JobApplications.id eq applicationId }.singleOrNull()
            val studentUserId = app?.get(JobApplications.studentId)
            val jobId = app?.get(JobApplications.jobId)
            
            if (studentUserId != null && jobId != null) {
                if (status == "SELECTED") {
                    StudentProfiles.update({ StudentProfiles.userId eq studentUserId }) {
                        it[placementStatus] = "PLACED"
                        it[updatedAt] = LocalDateTime.now()
                    }
                }
                
                val jobTitle = Jobs.select { Jobs.id eq jobId }.singleOrNull()?.get(Jobs.title) ?: "Job Position"
                val companyName = Jobs.select { Jobs.id eq jobId }.singleOrNull()?.get(Jobs.companyName) ?: "Company"
                val titleStr = when (status) {
                    "SELECTED" -> "Selected: $companyName"
                    "REJECTED" -> "Application Status: $companyName"
                    "SHORTLISTED" -> "Shortlisted: $companyName"
                    else -> "Application Update: $companyName"
                }
                val msgStr = when (status) {
                    "SELECTED" -> "Congratulations! You have been selected for the position of $jobTitle at $companyName."
                    "REJECTED" -> "We regret to inform you that your application for $jobTitle at $companyName has been rejected."
                    "SHORTLISTED" -> "Great news! You have been shortlisted for the position of $jobTitle at $companyName."
                    else -> "Your application status for the position of $jobTitle at $companyName has been updated to $status."
                }
                
                NotificationService().createNotification(
                    userId = studentUserId,
                    title = titleStr,
                    message = msgStr,
                    type = "APPLICATION_STATUS_UPDATE"
                )
            }
        }
        return MessageResponse("Application status updated to $status")
    }

    fun deleteJob(userId: Int, jobId: Int): MessageResponse {
        return transaction {
            val job = Jobs.select { (Jobs.id eq jobId) and (Jobs.postedBy eq userId) }.singleOrNull()
                ?: throw IllegalArgumentException("Job not found or unauthorized")
            
            // Delete applications first due to foreign key
            JobApplications.deleteWhere { JobApplications.jobId eq jobId }
            Jobs.deleteWhere { Jobs.id eq jobId }
            MessageResponse("Job deleted successfully")
        }
    }

    fun toggleJobStatus(userId: Int, jobId: Int): MessageResponse {
        return transaction {
            val job = Jobs.select { (Jobs.id eq jobId) and (Jobs.postedBy eq userId) }.singleOrNull()
                ?: throw IllegalArgumentException("Job not found or unauthorized")
            
            val newStatus = !job[Jobs.isActive]
            Jobs.update({ Jobs.id eq jobId }) {
                it[isActive] = newStatus
            }
            MessageResponse("Job status updated to ${if (newStatus) "Vacant" else "Not Vacant"}")
        }
    }

    fun getApplicationsForJob(jobId: Int, requestingUserId: Int? = null, requestingRole: String? = null): List<CandidateResponse> {
        return transaction {
            if (requestingRole == "RECRUITER" && requestingUserId != null) {
                val job = Jobs.select { Jobs.id eq jobId }.singleOrNull()
                if (job == null || job[Jobs.postedBy] != requestingUserId) {
                    throw IllegalArgumentException("Unauthorized to view applications for this job")
                }
            }
            (JobApplications innerJoin Users)
                .select { JobApplications.jobId eq jobId }
                .map { row ->
                    val studentUserId = row[Users.id]
                    val profile = StudentProfiles.select { StudentProfiles.userId eq studentUserId }.singleOrNull()
                    val profileId = profile?.get(StudentProfiles.id)
                    val skills = if (profileId != null) {
                        (StudentSkills innerJoin Skills)
                            .select { StudentSkills.studentProfileId eq profileId }
                            .map { r -> r[Skills.name] }
                    } else emptyList()

                    CandidateResponse(
                        userId = row[Users.id],
                        name = row[Users.name],
                        email = row[Users.email],
                        semester = profile?.get(StudentProfiles.semester),
                        branch = profile?.get(StudentProfiles.branch),
                        cgpa = profile?.get(StudentProfiles.cgpa),
                        skills = skills,
                        resumeUrl = row[JobApplications.resumeUrl]
                    )
                }
        }
    }
}


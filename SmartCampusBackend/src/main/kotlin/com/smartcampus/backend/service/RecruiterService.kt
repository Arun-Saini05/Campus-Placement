package com.smartcampus.backend.service

import com.smartcampus.backend.dto.*
import com.smartcampus.backend.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class RecruiterService {

    fun getJobsByRecruiter(recruiterId: Int): List<JobResponse> {
        return transaction {
            Jobs.select { Jobs.postedBy eq recruiterId }
                .map { row ->
                    JobResponse(
                        id = row[Jobs.id],
                        title = row[Jobs.title],
                        companyName = row[Jobs.companyName],
                        location = row[Jobs.location],
                        description = row[Jobs.description],
                        requiredSkills = row[Jobs.requiredSkills].split(",").map { it.trim() },
                        salaryPackage = row[Jobs.salaryPackage],
                        jobType = row[Jobs.jobType],
                        applicationDeadline = row[Jobs.applicationDeadline]?.toString(),
                        isActive = row[Jobs.isActive]
                    )
                }
        }
    }

    fun searchCandidates(request: CandidateSearchRequest): List<CandidateResponse> {

        return transaction {
            var query = (StudentProfiles innerJoin Users)
                .select { Users.role eq "STUDENT" }

            request.branch?.let { branch ->
                query = query.andWhere { StudentProfiles.branch eq branch }
            }
            request.minCgpa?.let { cgpa ->
                query = query.andWhere { StudentProfiles.cgpa greaterEq cgpa }
            }

            val candidates = query.map { row ->
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
            }

            request.skill?.let { skill ->
                candidates.filter { candidate ->
                    candidate.skills.any { s -> s.equals(skill, ignoreCase = true) }
                }
            } ?: candidates
        }
    }

    fun viewResume(recruiterId: Int, studentId: Int): CandidateResponse {
        return transaction {
            ResumeViews.insert {
                it[ResumeViews.studentId] = studentId
                it[ResumeViews.recruiterId] = recruiterId
                it[viewedAt] = LocalDateTime.now()
            }

            Notifications.insert {
                it[userId] = studentId
                it[title] = "Resume Viewed"
                it[message] = "A recruiter has viewed your resume"
                it[type] = "RESUME_VIEW"
                it[isRead] = false
                it[createdAt] = LocalDateTime.now()
            }

            val row = (StudentProfiles innerJoin Users)
                .select { Users.id eq studentId }.singleOrNull()
                ?: throw IllegalArgumentException("Student not found")

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
        }
    }

    fun getProfile(userId: Int): RecruiterProfileResponse? {
        return transaction {
            RecruiterProfiles.select { RecruiterProfiles.userId eq userId }
                .map { row ->
                    RecruiterProfileResponse(
                        userId = row[RecruiterProfiles.userId],
                        companyName = row[RecruiterProfiles.companyName],
                        companyLogoUrl = row[RecruiterProfiles.companyLogoUrl],
                        website = row[RecruiterProfiles.website],
                        industry = row[RecruiterProfiles.industry],
                        contactName = row[RecruiterProfiles.contactName],
                        contactDesignation = row[RecruiterProfiles.contactDesignation]
                    )
                }.singleOrNull()
        }
    }

    fun updateProfile(userId: Int, request: RecruiterProfileRequest): RecruiterProfileResponse {
        return transaction {
            val exists = RecruiterProfiles.select { RecruiterProfiles.userId eq userId }.any()
            if (exists) {
                RecruiterProfiles.update({ RecruiterProfiles.userId eq userId }) {
                    it[companyName] = request.companyName
                    it[website] = request.website
                    it[industry] = request.industry
                    it[contactName] = request.contactName
                    it[contactDesignation] = request.contactDesignation
                    it[updatedAt] = LocalDateTime.now()
                }
            } else {
                RecruiterProfiles.insert {
                    it[RecruiterProfiles.userId] = userId
                    it[companyName] = request.companyName
                    it[website] = request.website
                    it[industry] = request.industry
                    it[contactName] = request.contactName
                    it[contactDesignation] = request.contactDesignation
                    it[createdAt] = LocalDateTime.now()
                    it[updatedAt] = LocalDateTime.now()
                }
            }
            getProfile(userId)!!
        }
    }

    fun getAnalytics(recruiterId: Int): RecruiterAnalyticsResponse {
        return transaction {
            val jobIds = Jobs.select { Jobs.postedBy eq recruiterId }.map { it[Jobs.id] }
            val totalJobs = jobIds.size
            val totalApplications = JobApplications.select { JobApplications.jobId inList jobIds }.count().toInt()
            val totalInterviews = JobApplications.select { 
                (JobApplications.jobId inList jobIds) and (JobApplications.status eq "INTERVIEW_SCHEDULED") 
            }.count().toInt()
            val totalHires = JobApplications.select { 
                (JobApplications.jobId inList jobIds) and (JobApplications.status eq "SELECTED") 
            }.count().toInt()

            val appsPerJob = mutableMapOf<String, Int>()
            Jobs.select { Jobs.postedBy eq recruiterId }.forEach { row ->
                val jobId = row[Jobs.id]
                val jobTitle = row[Jobs.title]
                val count = JobApplications.select { JobApplications.jobId eq jobId }.count().toInt()
                appsPerJob[jobTitle] = count
            }

            RecruiterAnalyticsResponse(
                totalJobsPosted = totalJobs,
                totalApplicationsReceived = totalApplications,
                totalInterviewsScheduled = totalInterviews,
                totalHires = totalHires,
                applicationsPerJob = appsPerJob
            )
        }
    }

    fun scheduleInterview(request: InterviewScheduleRequest): MessageResponse {
        return transaction {
            JobApplications.update({ JobApplications.id eq request.applicationId }) {
                it[status] = "INTERVIEW_SCHEDULED"
                it[interviewDate] = LocalDateTime.parse(request.interviewDate)
                it[interviewLink] = request.interviewLink
                it[feedback] = request.feedback
                it[updatedAt] = LocalDateTime.now()
            }
            MessageResponse("Interview scheduled successfully", true)
        }
    }
}


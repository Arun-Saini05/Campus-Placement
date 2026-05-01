package com.smartcampus.backend.dto

import kotlinx.serialization.Serializable

// ==================== AUTH DTOs ====================
@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val enrollmentId: String? = null,
    val role: String = "STUDENT"
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: Int,
    val name: String,
    val email: String,
    val role: String
)

// ==================== STUDENT PROFILE DTOs ====================
@Serializable
data class StudentProfileRequest(
    val semester: Int? = null,
    val branch: String? = null,
    val cgpa: Float? = null,
    val preferredRegion: String? = null,
    val about: String? = null
)

@Serializable
data class StudentProfileResponse(
    val id: Int,
    val userId: Int,
    val name: String,
    val email: String,
    val semester: Int?,
    val branch: String?,
    val cgpa: Float?,
    val preferredRegion: String?,
    val about: String?,
    val resumeUrl: String?,
    val skills: List<SkillItem> = emptyList(),
    val certifications: List<CertificationItem> = emptyList(),
    val projects: List<ProjectItem> = emptyList(),
    val experiences: List<ExperienceItem> = emptyList()
)

// ==================== SKILL DTOs ====================
@Serializable
data class SkillItem(
    val id: Int,
    val name: String,
    val category: String? = null,
    val proficiencyLevel: String = "BEGINNER"
)

@Serializable
data class AddSkillRequest(
    val skillName: String,
    val category: String? = null,
    val proficiencyLevel: String = "BEGINNER"
)

@Serializable
data class SkillRecommendation(
    val skillName: String,
    val demandScore: Float,
    val reason: String,
    val region: String? = null
)

@Serializable
data class SkillGapReport(
    val currentSkills: List<String>,
    val demandedSkills: List<String>,
    val missingSkills: List<SkillRecommendation>,
    val readinessPercentage: Float
)

// ==================== CERTIFICATION DTOs ====================
@Serializable
data class CertificationItem(
    val id: Int,
    val name: String,
    val issuingOrganization: String? = null,
    val issueDate: String? = null,
    val credentialUrl: String? = null
)

@Serializable
data class CertificationRequest(
    val name: String,
    val issuingOrganization: String? = null,
    val issueDate: String? = null,
    val credentialUrl: String? = null
)

// ==================== PROJECT DTOs ====================
@Serializable
data class ProjectItem(
    val id: Int,
    val title: String,
    val description: String? = null,
    val techStack: String? = null,
    val projectUrl: String? = null
)

@Serializable
data class ProjectRequest(
    val title: String,
    val description: String? = null,
    val techStack: String? = null,
    val projectUrl: String? = null
)

// ==================== EXPERIENCE DTOs ====================
@Serializable
data class ExperienceItem(
    val id: Int,
    val companyName: String,
    val role: String,
    val startDate: String,
    val endDate: String? = null,
    val description: String? = null
)

@Serializable
data class ExperienceRequest(
    val companyName: String,
    val role: String,
    val startDate: String,
    val endDate: String? = null,
    val description: String? = null
)

// ==================== JOB DTOs ====================
@Serializable
data class JobResponse(
    val id: Int,
    val title: String,
    val companyName: String,
    val location: String,
    val description: String,
    val requiredSkills: List<String>,
    val salaryPackage: String? = null,
    val jobType: String,
    val applicationDeadline: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class JobRequest(
    val title: String,
    val companyName: String,
    val location: String,
    val description: String,
    val requiredSkills: List<String>,
    val salaryPackage: String? = null,
    val jobType: String,
    val applicationDeadline: String? = null
)

@Serializable
data class JobFilterRequest(
    val skill: String? = null,
    val location: String? = null,
    val minSalary: String? = null,
    val jobType: String? = null
)

@Serializable
data class ApplicationResponse(
    val id: Int,
    val jobId: Int,
    val jobTitle: String,
    val companyName: String,
    val status: String,
    val appliedAt: String
)

// ==================== RESUME DTOs ====================
@Serializable
data class ResumeDataRequest(
    val objective: String? = null,
    val education: String? = null,
    val skills: String? = null,
    val experience: String? = null,
    val projects: String? = null,
    val certifications: String? = null,
    val achievements: String? = null,
    val templateId: String = "classic"
)

@Serializable
data class AtsScoreResponse(
    val score: Int,
    val matchedKeywords: List<String>,
    val missingKeywords: List<String>,
    val suggestions: List<String>
)

// ==================== CAREER DTOs ====================
@Serializable
data class CareerRoadmap(
    val semester: Int,
    val requiredSkills: List<String>,
    val suggestedInternships: List<String>,
    val suggestedCertifications: List<String>
)

@Serializable
data class ProgressResponse(
    val completedSkills: Int,
    val totalRecommendedSkills: Int,
    val appliedInternships: Int,
    val resumeSubmissions: Int,
    val placementReadinessPercentage: Float,
    val skillGrowth: List<Float>
)

// ==================== PLACEMENT DRIVE DTOs ====================
@Serializable
data class PlacementDriveRequest(
    val companyName: String,
    val description: String? = null,
    val eligibilityCriteria: String? = null,
    val minCgpa: Float? = null,
    val allowedBranches: List<String>? = null,
    val salaryPackage: String? = null,
    val driveDate: String? = null
)

@Serializable
data class PlacementDriveResponse(
    val id: Int,
    val companyName: String,
    val description: String?,
    val eligibilityCriteria: String?,
    val minCgpa: Float?,
    val allowedBranches: List<String>?,
    val salaryPackage: String?,
    val driveDate: String?,
    val eligibleStudentCount: Int = 0
)

// ==================== NOTIFICATION DTOs ====================
@Serializable
data class OfficerStudentResponse(
    val userId: Int,
    val name: String,
    val email: String,
    val branch: String?,
    val semester: Int?,
    val cgpa: Float?,
    val skills: List<String>,
    val placementStatus: String
)

@Serializable
data class MassNotificationRequest(
    val title: String,
    val message: String,
    val targetAudience: String,
    val type: String = "ANNOUNCEMENT"
)

@Serializable
data class NotificationResponse(
    val id: Int,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: String
)

// ==================== RECRUITER DTOs ====================
@Serializable
data class RecruiterProfileRequest(
    val companyName: String,
    val website: String? = null,
    val industry: String? = null,
    val contactName: String? = null,
    val contactDesignation: String? = null
)

@Serializable
data class RecruiterProfileResponse(
    val userId: Int,
    val companyName: String,
    val companyLogoUrl: String?,
    val website: String?,
    val industry: String?,
    val contactName: String?,
    val contactDesignation: String?
)

@Serializable
data class CandidateSearchRequest(
    val skill: String? = null,
    val branch: String? = null,
    val minCgpa: Float? = null,
    val experience: String? = null
)

@Serializable
data class CandidateResponse(
    val userId: Int,
    val name: String,
    val email: String,
    val semester: Int?,
    val branch: String?,
    val cgpa: Float?,
    val skills: List<String>,
    val resumeUrl: String?
)

@Serializable
data class RecruiterAnalyticsResponse(
    val totalJobsPosted: Int,
    val totalApplicationsReceived: Int,
    val totalInterviewsScheduled: Int,
    val totalHires: Int,
    val applicationsPerJob: Map<String, Int> = emptyMap()
)

@Serializable
data class InterviewScheduleRequest(
    val applicationId: Int,
    val interviewDate: String, // ISO datetime
    val interviewLink: String? = null,
    val feedback: String? = null
)


// ==================== ADMIN DTOs ====================
@Serializable
data class UserManagementResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: String
)

// ==================== ANALYTICS DTOs ====================
@Serializable
data class DashboardAnalytics(
    val totalStudents: Int,
    val totalJobs: Int,
    val totalApplications: Int,
    val placedStudents: Int,
    val skillDemandData: Map<String, Float> = emptyMap(),
    val regionDemandData: Map<String, Float> = emptyMap(),
    val applicationSuccessRate: Float = 0f
)

@Serializable
data class OfficerDashboardResponse(
    val totalStudents: Int,
    val placedStudents: Int,
    val unplacedStudents: Int,
    val highestPackage: String,
    val averagePackage: String,
    val totalOffers: Int,
    val activeDrives: List<PlacementDriveResponse>,
    val departmentWisePlacements: Map<String, Int>
)

// ==================== COMMON ====================
@Serializable
data class MessageResponse(
    val message: String,
    val success: Boolean = true
)

// ==================== EXTERNAL JOB DTOs ====================
@Serializable
data class ExternalJobDto(
    val jobId: String,
    val title: String,
    val companyName: String,
    val companyLogo: String?,
    val location: String,
    val description: String,
    val skills: List<String>,
    val apiSource: String,
    val applyLink: String?,
    val isRemote: Boolean,
    val postedAt: String?,
    val minSalary: Double?,
    val maxSalary: Double?,
    val salaryCurrency: String?,
    val jobType: String?
)

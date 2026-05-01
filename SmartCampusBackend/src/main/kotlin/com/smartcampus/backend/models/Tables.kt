package com.smartcampus.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

// ==================== USERS ====================
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val email = varchar("email", 150).uniqueIndex()
    val password = varchar("password", 255)
    val enrollmentId = varchar("enrollment_id", 50).nullable()
    val role = varchar("role", 30) // STUDENT, RECRUITER, PLACEMENT_OFFICER, ADMIN
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

// ==================== STUDENT PROFILES ====================
object StudentProfiles : Table("student_profiles") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id).uniqueIndex()
    val semester = integer("semester").nullable()
    val branch = varchar("branch", 100).nullable()
    val cgpa = float("cgpa").nullable()
    val preferredRegion = varchar("preferred_region", 100).nullable()
    val domainInterest = varchar("domain_interest", 100).default("Computer Science")
    val about = text("about").nullable()
    val resumeUrl = varchar("resume_url", 500).nullable()
    val profileImageUrl = varchar("profile_image_url", 500).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

// ==================== SKILLS ====================
object Skills : Table("skills") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100).uniqueIndex()
    val category = varchar("category", 100).nullable() // Programming, Database, Cloud, etc.

    override val primaryKey = PrimaryKey(id)
}

object StudentSkills : Table("student_skills") {
    val id = integer("id").autoIncrement()
    val studentProfileId = integer("student_profile_id").references(StudentProfiles.id)
    val skillId = integer("skill_id").references(Skills.id)
    val proficiencyLevel = varchar("proficiency_level", 30).default("BEGINNER") // BEGINNER, INTERMEDIATE, ADVANCED

    override val primaryKey = PrimaryKey(id)
}

// ==================== CERTIFICATIONS ====================
object Certifications : Table("certifications") {
    val id = integer("id").autoIncrement()
    val studentProfileId = integer("student_profile_id").references(StudentProfiles.id)
    val name = varchar("name", 200)
    val issuingOrganization = varchar("issuing_organization", 200).nullable()
    val issueDate = varchar("issue_date", 20).nullable()
    val credentialUrl = varchar("credential_url", 500).nullable()

    override val primaryKey = PrimaryKey(id)
}

// ==================== PROJECTS ====================
object Projects : Table("projects") {
    val id = integer("id").autoIncrement()
    val studentProfileId = integer("student_profile_id").references(StudentProfiles.id)
    val title = varchar("title", 200)
    val description = text("description").nullable()
    val techStack = varchar("tech_stack", 500).nullable()
    val projectUrl = varchar("project_url", 500).nullable()

    override val primaryKey = PrimaryKey(id)
}

// ==================== WORK EXPERIENCE ====================
object WorkExperiences : Table("work_experiences") {
    val id = integer("id").autoIncrement()
    val studentProfileId = integer("student_profile_id").references(StudentProfiles.id)
    val companyName = varchar("company_name", 200)
    val role = varchar("role", 200)
    val startDate = varchar("start_date", 20)
    val endDate = varchar("end_date", 20).nullable()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}

// ==================== TRENDING SKILLS ====================
object TrendingSkills : Table("trending_skills") {
    val id = integer("id").autoIncrement()
    val skillName = varchar("skill_name", 100)
    val demandScore = float("demand_score")
    val region = varchar("region", 100).nullable()
    val semester = integer("semester").nullable()
    val growthRate = float("growth_rate").default(15.0f)
    val domainCategory = varchar("domain_category", 100).default("General")
    val branch = varchar("branch", 100).nullable()
    val dataSource = varchar("source", 200).nullable()
    val scrapedAt = datetime("scraped_at")

    override val primaryKey = PrimaryKey(id)
}

// ==================== RECRUITER PROFILES ====================
object RecruiterProfiles : Table("recruiter_profiles") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id).uniqueIndex()
    val companyName = varchar("company_name", 200)
    val companyLogoUrl = varchar("company_logo_url", 500).nullable()
    val website = varchar("website", 200).nullable()
    val industry = varchar("industry", 100).nullable()
    val contactName = varchar("contact_name", 100).nullable()
    val contactDesignation = varchar("contact_designation", 100).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

// ==================== JOBS ====================
object Jobs : Table("jobs") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 200)
    val companyName = varchar("company_name", 200)
    val location = varchar("location", 200)
    val description = text("description")
    val requiredSkills = varchar("required_skills", 1000)
    val salaryPackage = varchar("salary_package", 100).nullable()
    val jobType = varchar("job_type", 30) // INTERNSHIP, FULL_TIME, PART_TIME
    val applicationDeadline = datetime("application_deadline").nullable()
    val postedBy = integer("posted_by").references(Users.id)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

// ==================== JOB APPLICATIONS ====================
object JobApplications : Table("job_applications") {
    val id = integer("id").autoIncrement()
    val jobId = integer("job_id").references(Jobs.id)
    val studentId = integer("student_id").references(Users.id)
    val resumeUrl = varchar("resume_url", 500).nullable()
    val status = varchar("status", 30).default("APPLIED") // APPLIED, SHORTLISTED, INTERVIEW_SCHEDULED, SELECTED, REJECTED
    val interviewDate = datetime("interview_date").nullable()
    val interviewLink = varchar("interview_link", 500).nullable()
    val feedback = text("feedback").nullable()
    val appliedAt = datetime("applied_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}


// ==================== RESUME DATA ====================
object ResumeData : Table("resume_data") {
    val id = integer("id").autoIncrement()
    val studentId = integer("student_id").references(Users.id).uniqueIndex()
    val objective = text("objective").nullable()
    val education = text("education").nullable()
    val skills = text("skills").nullable()
    val experience = text("experience").nullable()
    val projects = text("projects").nullable()
    val certifications = text("certifications").nullable()
    val achievements = text("achievements").nullable()
    val templateId = varchar("template_id", 50).default("classic")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

// ==================== PLACEMENT DRIVES ====================
object PlacementDrives : Table("placement_drives") {
    val id = integer("id").autoIncrement()
    val companyName = varchar("company_name", 200)
    val description = text("description").nullable()
    val eligibilityCriteria = text("eligibility_criteria").nullable()
    val minCgpa = float("min_cgpa").nullable()
    val allowedBranches = varchar("allowed_branches", 500).nullable()
    val salaryPackage = varchar("salary_package", 100).nullable()
    val driveDate = datetime("drive_date").nullable()
    val scheduleUrl = varchar("schedule_url", 500).nullable()
    val postedBy = integer("posted_by").references(Users.id)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

// ==================== NOTIFICATIONS ====================
object Notifications : Table("notifications") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val title = varchar("title", 200)
    val message = text("message")
    val type = varchar("type", 50) // JOB_POSTING, SKILL_TREND, INTERVIEW, APPLICATION_STATUS, RESUME_VIEW
    val isRead = bool("is_read").default(false)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

// ==================== RESUME VIEWS (FR-36) ====================
object ResumeViews : Table("resume_views") {
    val id = integer("id").autoIncrement()
    val studentId = integer("student_id").references(Users.id)
    val recruiterId = integer("recruiter_id").references(Users.id)
    val viewedAt = datetime("viewed_at")

    override val primaryKey = PrimaryKey(id)
}

// ==================== ROADMAP PROGRESS ====================
object RoadmapProgress : Table("roadmap_progress") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val skillName = varchar("skill_name", 100)
    val stepName = varchar("step_name", 200)

    override val primaryKey = PrimaryKey(id)
}

package com.smartcampus.backend.service

import com.smartcampus.backend.dto.*
import com.smartcampus.backend.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class StudentService {

    fun getProfile(userId: Int): StudentProfileResponse {
        return transaction {
            val user = Users.select { Users.id eq userId }.singleOrNull()
                ?: throw IllegalArgumentException("User not found")

            val profile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull()
            val profileId = profile?.get(StudentProfiles.id) ?: 0

            val skills = if (profile != null) {
                (StudentSkills innerJoin Skills)
                    .select { StudentSkills.studentProfileId eq profileId }
                    .map { row ->
                        SkillItem(
                            id = row[Skills.id],
                            name = row[Skills.name],
                            category = row[Skills.category],
                            proficiencyLevel = row[StudentSkills.proficiencyLevel]
                        )
                    }
            } else emptyList()

            val certifications = if (profile != null) {
                Certifications.select { Certifications.studentProfileId eq profileId }
                    .map { row ->
                        CertificationItem(
                            id = row[Certifications.id],
                            name = row[Certifications.name],
                            issuingOrganization = row[Certifications.issuingOrganization],
                            issueDate = row[Certifications.issueDate],
                            credentialUrl = row[Certifications.credentialUrl]
                        )
                    }
            } else emptyList()

            val projects = if (profile != null) {
                Projects.select { Projects.studentProfileId eq profileId }
                    .map { row ->
                        ProjectItem(
                            id = row[Projects.id],
                            title = row[Projects.title],
                            description = row[Projects.description],
                            techStack = row[Projects.techStack],
                            projectUrl = row[Projects.projectUrl]
                        )
                    }
            } else emptyList()

            val experiences = if (profile != null) {
                WorkExperiences.select { WorkExperiences.studentProfileId eq profileId }
                    .map { row ->
                        ExperienceItem(
                            id = row[WorkExperiences.id],
                            companyName = row[WorkExperiences.companyName],
                            role = row[WorkExperiences.role],
                            startDate = row[WorkExperiences.startDate],
                            endDate = row[WorkExperiences.endDate],
                            description = row[WorkExperiences.description]
                        )
                    }
            } else emptyList()

            StudentProfileResponse(
                id = profileId,
                userId = userId,
                name = user[Users.name],
                email = user[Users.email],
                semester = profile?.get(StudentProfiles.semester),
                branch = profile?.get(StudentProfiles.branch),
                cgpa = profile?.get(StudentProfiles.cgpa),
                preferredRegion = profile?.get(StudentProfiles.preferredRegion),
                about = profile?.get(StudentProfiles.about),
                resumeUrl = profile?.get(StudentProfiles.resumeUrl),
                placementStatus = profile?.get(StudentProfiles.placementStatus) ?: "UNPLACED",
                skills = skills,
                certifications = certifications,
                projects = projects,
                experiences = experiences
            )
        }
    }

    fun updatePlacementStatus(userId: Int, status: String): MessageResponse {
        transaction {
            val profile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull()
            if (profile != null) {
                StudentProfiles.update({ StudentProfiles.userId eq userId }) {
                    it[placementStatus] = status
                    it[updatedAt] = LocalDateTime.now()
                }
            } else {
                // If profile doesn't exist, create it with this status
                StudentProfiles.insert {
                    it[StudentProfiles.userId] = userId
                    it[placementStatus] = status
                    it[createdAt] = LocalDateTime.now()
                    it[updatedAt] = LocalDateTime.now()
                }
            }
        }
        return MessageResponse("Placement status updated to $status")
    }

    fun updateProfile(userId: Int, request: StudentProfileRequest): MessageResponse {
        transaction {
            val existing = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull()

            if (existing != null) {
                StudentProfiles.update({ StudentProfiles.userId eq userId }) {
                    request.semester?.let { v -> it[semester] = v }
                    request.branch?.let { v -> it[branch] = v }
                    request.cgpa?.let { v -> it[cgpa] = v }
                    request.preferredRegion?.let { v -> it[preferredRegion] = v }
                    request.about?.let { v -> it[about] = v }
                    it[updatedAt] = LocalDateTime.now()
                }
            } else {
                StudentProfiles.insert {
                    it[StudentProfiles.userId] = userId
                    it[semester] = request.semester
                    it[branch] = request.branch
                    it[cgpa] = request.cgpa
                    it[preferredRegion] = request.preferredRegion
                    it[about] = request.about
                    it[createdAt] = LocalDateTime.now()
                    it[updatedAt] = LocalDateTime.now()
                }
            }
        }
        return MessageResponse("Profile updated successfully")
    }

    fun addSkill(userId: Int, request: AddSkillRequest): MessageResponse {
        transaction {
            val profile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull()
                ?: throw IllegalArgumentException("Create profile first")

            val profileId = profile[StudentProfiles.id]

            val skillRow = Skills.select { Skills.name eq request.skillName }.singleOrNull()
            val skillId: Int = skillRow?.get(Skills.id)
                ?: (Skills.insert {
                    it[name] = request.skillName
                    it[category] = request.category
                } get Skills.id)

            val existing = StudentSkills.select {
                (StudentSkills.studentProfileId eq profileId) and (StudentSkills.skillId eq skillId)
            }.singleOrNull()

            if (existing != null) {
                throw IllegalArgumentException("Skill already added")
            }

            StudentSkills.insert {
                it[studentProfileId] = profileId
                it[StudentSkills.skillId] = skillId
                it[proficiencyLevel] = request.proficiencyLevel
            }
        }
        return MessageResponse("Skill added successfully")
    }

    fun removeSkill(userId: Int, skillIdParam: Int): MessageResponse {
        transaction {
            val profile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull()
                ?: throw IllegalArgumentException("Profile not found")

            val profileId = profile[StudentProfiles.id]

            StudentSkills.deleteWhere {
                (StudentSkills.studentProfileId eq profileId) and (StudentSkills.skillId eq skillIdParam)
            }
        }
        return MessageResponse("Skill removed successfully")
    }

    fun addCertification(userId: Int, request: CertificationRequest): MessageResponse {
        transaction {
            val profile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull()
                ?: throw IllegalArgumentException("Create profile first")

            val profileId = profile[StudentProfiles.id]

            Certifications.insert {
                it[studentProfileId] = profileId
                it[name] = request.name
                it[issuingOrganization] = request.issuingOrganization
                it[issueDate] = request.issueDate
                it[credentialUrl] = request.credentialUrl
            }
        }
        return MessageResponse("Certification added successfully")
    }

    fun addProject(userId: Int, request: ProjectRequest): MessageResponse {
        transaction {
            val profile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull()
                ?: throw IllegalArgumentException("Create profile first")

            val profileId = profile[StudentProfiles.id]

            Projects.insert {
                it[studentProfileId] = profileId
                it[title] = request.title
                it[description] = request.description
                it[techStack] = request.techStack
                it[projectUrl] = request.projectUrl
            }
        }
        return MessageResponse("Project added successfully")
    }

    fun addExperience(userId: Int, request: ExperienceRequest): MessageResponse {
        transaction {
            val profile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull()
                ?: throw IllegalArgumentException("Create profile first")

            val profileId = profile[StudentProfiles.id]

            WorkExperiences.insert {
                it[studentProfileId] = profileId
                it[companyName] = request.companyName
                it[role] = request.role
                it[startDate] = request.startDate
                it[endDate] = request.endDate
                it[description] = request.description
            }
        }
        return MessageResponse("Experience added successfully")
    }
}

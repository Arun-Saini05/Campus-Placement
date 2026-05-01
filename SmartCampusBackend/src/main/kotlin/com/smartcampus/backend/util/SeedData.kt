package com.smartcampus.backend.util

import com.smartcampus.backend.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime

object SeedData {

    fun seed() {
        transaction {
            val now = LocalDateTime.now()

            // Ensure admin user exists
            if (Users.select { Users.email eq "admin@campus.edu" }.count() == 0L) {
                Users.insert {
                    it[name] = "Admin"
                    it[email] = "admin@campus.edu"
                    it[password] = BCrypt.hashpw("admin123", BCrypt.gensalt())
                    it[enrollmentId] = "ADM001"
                    it[role] = "ADMIN"
                    it[isActive] = true
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }

            // Ensure placement officer exists and is active
            val officer = Users.select { Users.email eq "officer@campus.edu" }.singleOrNull()
            if (officer == null) {
                Users.insert {
                    it[name] = "Dr. Sharma"
                    it[email] = "officer@campus.edu"
                    it[password] = BCrypt.hashpw("officer123", BCrypt.gensalt())
                    it[enrollmentId] = "OFF001"
                    it[role] = "PLACEMENT_OFFICER"
                    it[isActive] = true
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            } else {
                Users.update({ Users.email eq "officer@campus.edu" }) {
                    it[isActive] = true
                }
            }

            // Ensure sample student exists
            if (Users.select { Users.email eq "student@campus.edu" }.count() == 0L) {
                val studentId = Users.insert {
                    it[name] = "Arun Student"
                    it[email] = "student@campus.edu"
                    it[password] = BCrypt.hashpw("student123", BCrypt.gensalt())
                    it[enrollmentId] = "STU001"
                    it[role] = "STUDENT"
                    it[isActive] = true
                    it[createdAt] = now
                    it[updatedAt] = now
                } get Users.id

                // Student profile
                val profileId = StudentProfiles.insert {
                    it[userId] = studentId
                    it[semester] = 5
                    it[branch] = "Computer Science"
                    it[cgpa] = 8.5f
                    it[preferredRegion] = "Bangalore"
                    it[about] = "Passionate about software development"
                    it[createdAt] = now
                    it[updatedAt] = now
                } get StudentProfiles.id

                // Skills
                val skills = listOf(
                    "Kotlin" to "Programming", "Java" to "Programming", "Python" to "Programming",
                    "SQL" to "Database", "Android" to "Mobile", "Git" to "Tools"
                )
                skills.forEach { (name, category) ->
                    val skillId = Skills.insert {
                        it[Skills.name] = name
                        it[Skills.category] = category
                    } get Skills.id

                    StudentSkills.insert {
                        it[studentProfileId] = profileId
                        it[StudentSkills.skillId] = skillId
                        it[proficiencyLevel] = "INTERMEDIATE"
                    }
                }
            }

            // Create recruiter if not exists
            if (Users.select { Users.email eq "recruiter@techcorp.com" }.count() == 0L) {
                Users.insert {
                    it[name] = "TechCorp Recruiter"
                    it[email] = "recruiter@techcorp.com"
                    it[password] = BCrypt.hashpw("recruiter123", BCrypt.gensalt())
                    it[role] = "RECRUITER"
                    it[isActive] = true
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }

            // Trending skills
            if (TrendingSkills.selectAll().count() == 0L) {
                val trendingData = listOf(
                    Triple("React", 96f, "Bangalore"),
                    Triple("TensorFlow", 95f, "Bangalore"),
                    Triple("TypeScript", 94f, "Pune"),
                    Triple("Kotlin", 90f, "Pune")
                )
                trendingData.forEach { (skill, score, region) ->
                    TrendingSkills.insert {
                        it[skillName] = skill
                        it[demandScore] = score
                        it[TrendingSkills.region] = region
                        it[semester] = 5
                        it[branch] = "Computer Science"
                        it[dataSource] = "Job Portal Analysis"
                        it[scrapedAt] = now
                    }
                }
            }
        }
    }

    fun refreshTrendingSkills() {
        transaction {
            val now = LocalDateTime.now()
            TrendingSkills.deleteAll()
            val trendingData = listOf(Triple("React", 96f, "Bangalore"), Triple("Kotlin", 90f, "Pune"))
            trendingData.forEach { (skill, score, region) ->
                TrendingSkills.insert {
                    it[TrendingSkills.skillName] = skill
                    it[TrendingSkills.demandScore] = score
                    it[TrendingSkills.region] = region
                    it[TrendingSkills.semester] = 5
                    it[TrendingSkills.branch] = "Computer Science"
                    it[TrendingSkills.dataSource] = "Job Portal Analysis"
                    it[TrendingSkills.scrapedAt] = now
                }
            }
        }
    }
}

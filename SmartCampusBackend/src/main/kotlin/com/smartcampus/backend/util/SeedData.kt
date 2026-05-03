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
                seedTrendingSkills(now)
            }
        }
    }

    private fun seedTrendingSkills(now: LocalDateTime) {
        val trendingData = listOf(
            // Bangalore
            Triple("React", 96f, "Bangalore"),
            Triple("TensorFlow", 95f, "Bangalore"),
            Triple("Spring Boot", 92f, "Bangalore"),
            Triple("Node.js", 89f, "Bangalore"),
            // Mumbai
            Triple("Python", 94f, "Mumbai"),
            Triple("AWS", 93f, "Mumbai"),
            Triple("Java", 91f, "Mumbai"),
            Triple("Docker", 88f, "Mumbai"),
            // Pune
            Triple("TypeScript", 94f, "Pune"),
            Triple("Kotlin", 90f, "Pune"),
            Triple("Angular", 87f, "Pune"),
            Triple("Kubernetes", 85f, "Pune"),
            // Hyderabad
            Triple("GCP", 92f, "Hyderabad"),
            Triple("Next.js", 91f, "Hyderabad"),
            Triple("PostgreSQL", 89f, "Hyderabad"),
            Triple("FastAPI", 86f, "Hyderabad"),
            // Delhi NCR
            Triple("Machine Learning", 95f, "Delhi NCR"),
            Triple("React Native", 88f, "Delhi NCR"),
            Triple("C++", 87f, "Delhi NCR"),
            Triple("MongoDB", 84f, "Delhi NCR"),
            // Chennai
            Triple("Azure", 90f, "Chennai"),
            Triple("Flutter", 89f, "Chennai"),
            Triple("Swift", 86f, "Chennai"),
            Triple("Redis", 83f, "Chennai"),
            // Kolkata
            Triple("PHP", 82f, "Kolkata"),
            Triple("Laravel", 80f, "Kolkata"),
            Triple("SQL", 85f, "Kolkata"),
            // Ahmedabad
            Triple("Go", 88f, "Ahmedabad"),
            Triple("Rust", 84f, "Ahmedabad"),
            // Jaipur
            Triple("WordPress", 75f, "Jaipur"),
            Triple("UI/UX Design", 88f, "Jaipur"),
            // Kochi
            Triple("Cybersecurity", 91f, "Kochi"),
            // Noida
            Triple("DevOps", 93f, "Noida"),
            // Global
            Triple("Generative AI", 98f, "Global"),
            Triple("LLMs", 97f, "Global"),
            Triple("Blockchain", 85f, "Global")
        )
        
        trendingData.forEach { (skill, score, region) ->
            TrendingSkills.insert {
                it[skillName] = skill
                it[demandScore] = score
                it[TrendingSkills.region] = region
                it[semester] = (3..8).random()
                it[branch] = listOf("Computer Science", "Information Technology", "Electronics").random()
                it[dataSource] = "Industry Market Analysis"
                it[scrapedAt] = now
            }
        }
    }

    fun refreshTrendingSkills() {
        transaction {
            val count = TrendingSkills.selectAll().count()
            // Only refresh if data is sparse (less than 10 items)
            if (count < 10) {
                val now = LocalDateTime.now()
                seedTrendingSkills(now)
            }
        }
    }
}

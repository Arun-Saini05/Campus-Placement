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

            // 1. Reset Database in order of foreign key dependency
            Notifications.deleteAll()
            RoadmapProgress.deleteAll()
            ResumeViews.deleteAll()
            JobApplications.deleteAll()
            PlacementDrives.deleteAll()
            Jobs.deleteAll()
            ResumeData.deleteAll()
            StudentSkills.deleteAll()
            Certifications.deleteAll()
            Projects.deleteAll()
            WorkExperiences.deleteAll()
            StudentProfiles.deleteAll()
            RecruiterProfiles.deleteAll()
            Companies.deleteAll()
            Users.deleteAll()
            Colleges.deleteAll()

            // 2. Seed Colleges
            val c1Id = Colleges.insert {
                it[name] = "Alpha Institute of Technology"
                it[location] = "Bangalore"
                it[createdAt] = now
            } get Colleges.id

            val c2Id = Colleges.insert {
                it[name] = "Zenith Engineering College"
                it[location] = "Mumbai"
                it[createdAt] = now
            } get Colleges.id

            // 3. Seed Companies
            val companiesList = listOf(
                "TCS" to "IT Services" to "Chennai",
                "Infosys" to "IT Services" to "Bangalore",
                "Wipro" to "IT Services" to "Bangalore",
                "Accenture" to "Consulting" to "Mumbai",
                "Capgemini" to "Consulting" to "Pune",
                "Cognizant" to "IT Services" to "Hyderabad",
                "IBM" to "Technology" to "Bangalore",
                "Tech Mahindra" to "IT Services" to "Pune",
                "LTIMindtree" to "IT Services" to "Mumbai",
                "Deloitte" to "Consulting" to "Bangalore",
                "Oracle" to "Technology" to "Hyderabad",
                "Zoho" to "Software" to "Chennai"
            )
            val companyIds = companiesList.map { pair ->
                val info = pair.first
                val locationStr = pair.second
                val nameStr = info.first
                val industryStr = info.second
                Companies.insert {
                    it[Companies.name] = nameStr
                    it[Companies.industry] = industryStr
                    it[Companies.location] = locationStr
                    it[createdAt] = now
                } get Companies.id
            }

            // 4. Seed Default Super Admin
            Users.insert {
                it[name] = "Admin"
                it[email] = "admin@campus.edu"
                it[password] = BCrypt.hashpw("admin123", BCrypt.gensalt())
                it[enrollmentId] = "ADM001"
                it[role] = "ADMIN"
                it[isActive] = true
                it[collegeId] = null
                it[createdAt] = now
                it[updatedAt] = now
            }

            // 5. Seed Students
            val firstNames = listOf("Arun", "Yash", "Amit", "Rahul", "Pooja", "Neha", "Divya", "Siddharth", "Aman", "Rohan", "Sneha", "Karan", "Simran", "Vijay", "Anjali", "Aditya", "Priya", "Raj", "Sonia", "Deepak")
            val lastNames = listOf("Saini", "Pandey", "Sharma", "Kumar", "Verma", "Singh", "Gupta", "Joshi", "Patel", "Mehta", "Shah", "Reddy", "Nair", "Das", "Sen", "Bose", "Choudhury", "Mishra", "Trivedi", "Rao")
            val branches = listOf("Computer Science", "Information Technology", "Electronics & Communication", "Mechanical Engineering", "Civil Engineering")
            val skillPool = listOf("Java", "Python", "React", "Node.js", "SQL", "AWS", "Kotlin", "Docker", "Git", "Flutter")

            // Seed College 1 Students (100 students)
            val studentIdsC1 = mutableListOf<Int>()
            // Student 1 is the default student
            val studentIdDefault = Users.insert {
                it[name] = "Arun Student"
                it[email] = "student@campus.edu"
                it[password] = BCrypt.hashpw("student123", BCrypt.gensalt())
                it[enrollmentId] = "STU001"
                it[role] = "STUDENT"
                it[isActive] = true
                it[collegeId] = c1Id
                it[createdAt] = now
                it[updatedAt] = now
            } get Users.id
            studentIdsC1.add(studentIdDefault)
            
            val defaultStudentProfileId = StudentProfiles.insert {
                it[userId] = studentIdDefault
                it[semester] = 5
                it[branch] = "Computer Science"
                it[cgpa] = 8.5f
                it[preferredRegion] = "Bangalore"
                it[about] = "Passionate about software development"
                it[createdAt] = now
                it[updatedAt] = now
            } get StudentProfiles.id
            
            listOf("Kotlin", "Java", "Python", "SQL").forEach { skillName ->
                val sId = Skills.select { Skills.name eq skillName }.singleOrNull()?.get(Skills.id) ?: Skills.insert {
                    it[name] = skillName
                    it[category] = "Programming"
                } get Skills.id
                StudentSkills.insert {
                    it[studentProfileId] = defaultStudentProfileId
                    it[StudentSkills.skillId] = sId
                    it[proficiencyLevel] = "INTERMEDIATE"
                }
            }

            for (i in 2..100) {
                val fName = firstNames.random()
                val lName = lastNames.random()
                val emailStr = "student${i}@alpha.edu"
                val enroll = "STU_C1_${"%03d".format(i)}"
                val sId = Users.insert {
                    it[name] = "$fName $lName"
                    it[email] = emailStr
                    it[password] = BCrypt.hashpw("password123", BCrypt.gensalt())
                    it[enrollmentId] = enroll
                    it[role] = "STUDENT"
                    it[isActive] = true
                    it[collegeId] = c1Id
                    it[createdAt] = now
                    it[updatedAt] = now
                } get Users.id
                studentIdsC1.add(sId)

                val branchStr = branches.random()
                val semesterVal = (1..8).random()
                val cgpaVal = Math.round(((6.0 + Math.random() * 3.8).toFloat()) * 10f) / 10f
                
                val pId = StudentProfiles.insert {
                    it[userId] = sId
                    it[semester] = semesterVal
                    it[branch] = branchStr
                    it[cgpa] = cgpaVal
                    it[preferredRegion] = listOf("Bangalore", "Mumbai", "Pune", "Hyderabad").random()
                    it[about] = "Passionate engineering student specializing in $branchStr."
                    it[createdAt] = now
                    it[updatedAt] = now
                } get StudentProfiles.id

                skillPool.shuffled().take((3..5).random()).forEach { skillName ->
                    val skillIdVal = Skills.select { Skills.name eq skillName }.singleOrNull()?.get(Skills.id) ?: Skills.insert {
                        it[name] = skillName
                        it[category] = "Technology"
                    } get Skills.id
                    StudentSkills.insert {
                        it[studentProfileId] = pId
                        it[StudentSkills.skillId] = skillIdVal
                        it[proficiencyLevel] = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED").random()
                    }
                }
            }

            // Seed College 2 Students (100 students)
            val studentIdsC2 = mutableListOf<Int>()
            for (i in 1..100) {
                val fName = firstNames.random()
                val lName = lastNames.random()
                val emailStr = "student${i}@zenith.edu"
                val enroll = "STU_C2_${"%03d".format(i)}"
                val sId = Users.insert {
                    it[name] = "$fName $lName"
                    it[email] = emailStr
                    it[password] = BCrypt.hashpw("password123", BCrypt.gensalt())
                    it[enrollmentId] = enroll
                    it[role] = "STUDENT"
                    it[isActive] = true
                    it[collegeId] = c2Id
                    it[createdAt] = now
                    it[updatedAt] = now
                } get Users.id
                studentIdsC2.add(sId)

                val branchStr = branches.random()
                val semesterVal = (1..8).random()
                val cgpaVal = Math.round(((6.0 + Math.random() * 3.8).toFloat()) * 10f) / 10f
                
                val pId = StudentProfiles.insert {
                    it[userId] = sId
                    it[semester] = semesterVal
                    it[branch] = branchStr
                    it[cgpa] = cgpaVal
                    it[preferredRegion] = listOf("Bangalore", "Mumbai", "Pune", "Hyderabad").random()
                    it[about] = "Dedicated student pursuing B.Tech in $branchStr."
                    it[createdAt] = now
                    it[updatedAt] = now
                } get StudentProfiles.id

                skillPool.shuffled().take((3..5).random()).forEach { skillName ->
                    val skillIdVal = Skills.select { Skills.name eq skillName }.singleOrNull()?.get(Skills.id) ?: Skills.insert {
                        it[name] = skillName
                        it[category] = "Technology"
                    } get Skills.id
                    StudentSkills.insert {
                        it[studentProfileId] = pId
                        it[StudentSkills.skillId] = skillIdVal
                        it[proficiencyLevel] = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED").random()
                    }
                }
            }

            // 6. Seed Officers
            Users.insert {
                it[name] = "Dr. Sharma"
                it[email] = "officer@campus.edu"
                it[password] = BCrypt.hashpw("officer123", BCrypt.gensalt())
                it[enrollmentId] = "OFF001"
                it[role] = "PLACEMENT_OFFICER"
                it[isActive] = true
                it[collegeId] = c1Id
                it[createdAt] = now
                it[updatedAt] = now
            }

            val officerC2Id = Users.insert {
                it[name] = "Prof. Mehta"
                it[email] = "officer2@zenith.edu"
                it[password] = BCrypt.hashpw("officer123", BCrypt.gensalt())
                it[enrollmentId] = "OFF002"
                it[role] = "PLACEMENT_OFFICER"
                it[isActive] = true
                it[collegeId] = c2Id
                it[createdAt] = now
                it[updatedAt] = now
            } get Users.id

            // 7. Seed Recruiters
            val recruiterNames = listOf("Siddharth Roy", "Anjali Deshmukh", "Vikram Malhotra", "Priya Nair", "Rohan Verma", "Sneha Iyer", "Rajesh Gowda", "Meera Kulkarni")
            val c1Recruiters = mutableListOf<Int>()
            val c2Recruiters = mutableListOf<Int>()

            // Default Recruiter: recruiter@techcorp.com -> TCS
            val rIdDefault = Users.insert {
                it[name] = "TechCorp Recruiter"
                it[email] = "recruiter@techcorp.com"
                it[password] = BCrypt.hashpw("recruiter123", BCrypt.gensalt())
                it[role] = "RECRUITER"
                it[isActive] = true
                it[collegeId] = c1Id
                it[createdAt] = now
                it[updatedAt] = now
            } get Users.id
            c1Recruiters.add(rIdDefault)

            RecruiterProfiles.insert {
                it[userId] = rIdDefault
                it[companyName] = "TCS"
                it[companyId] = companyIds[0]
                it[industry] = "IT Services"
                it[contactName] = "TechCorp Recruiter"
                it[contactDesignation] = "HR Manager"
                it[createdAt] = now
                it[updatedAt] = now
            }

            // Other 4 Recruiters for College 1 -> Infosys, Wipro, Accenture, Capgemini
            val c1RecruiterDetails = listOf(
                "Infosys" to companyIds[1],
                "Wipro" to companyIds[2],
                "Accenture" to companyIds[3],
                "Capgemini" to companyIds[4]
            )
            c1RecruiterDetails.forEachIndexed { idx, pair ->
                val cName = pair.first
                val cId = pair.second
                val rName = recruiterNames[idx]
                val emailStr = "recruiter${idx + 1}@alpha.edu"
                val rId = Users.insert {
                    it[name] = rName
                    it[email] = emailStr
                    it[password] = BCrypt.hashpw("password123", BCrypt.gensalt())
                    it[role] = "RECRUITER"
                    it[isActive] = true
                    it[collegeId] = c1Id
                    it[createdAt] = now
                    it[updatedAt] = now
                } get Users.id
                c1Recruiters.add(rId)

                RecruiterProfiles.insert {
                    it[userId] = rId
                    it[companyName] = cName
                    it[companyId] = cId
                    it[industry] = if (cName == "Accenture" || cName == "Capgemini") "Consulting" else "IT Services"
                    it[contactName] = rName
                    it[contactDesignation] = "Lead HR"
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }

            // 3 Recruiters for College 2 -> IBM, Deloitte, Oracle
            val c2RecruiterDetails = listOf(
                "IBM" to companyIds[6],
                "Deloitte" to companyIds[9],
                "Oracle" to companyIds[10]
            )
            c2RecruiterDetails.forEachIndexed { idx, pair ->
                val cName = pair.first
                val cId = pair.second
                val rName = recruiterNames[idx + 4]
                val emailStr = "recruiter${idx + 1}@zenith.edu"
                val rId = Users.insert {
                    it[name] = rName
                    it[email] = emailStr
                    it[password] = BCrypt.hashpw("password123", BCrypt.gensalt())
                    it[role] = "RECRUITER"
                    it[isActive] = true
                    it[collegeId] = c2Id
                    it[createdAt] = now
                    it[updatedAt] = now
                } get Users.id
                c2Recruiters.add(rId)

                RecruiterProfiles.insert {
                    it[userId] = rId
                    it[companyName] = cName
                    it[companyId] = cId
                    it[industry] = if (cName == "Deloitte") "Consulting" else "Technology"
                    it[contactName] = rName
                    it[contactDesignation] = "Talent Acquisition"
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }

            // 8. Seed Jobs
            val c1Jobs = mutableListOf<Int>()
            val c2Jobs = mutableListOf<Int>()

            val jobTitles = listOf(
                "Software Development Engineer" to listOf("Java", "SQL", "Git") to "6.5 LPA",
                "Frontend Developer Intern" to listOf("React", "Git") to "4.0 LPA",
                "Backend Engineer" to listOf("Python", "Node.js", "SQL") to "8.0 LPA",
                "DevOps Engineer" to listOf("AWS", "Docker", "Git") to "10.0 LPA",
                "Mobile App Developer" to listOf("Kotlin", "Flutter", "Git") to "7.5 LPA",
                "Data Analyst" to listOf("Python", "SQL") to "5.5 LPA"
            )

            // College 1 Jobs
            c1Recruiters.forEachIndexed { idx, recId ->
                val companyNameStr = RecruiterProfiles.select { RecruiterProfiles.userId eq recId }.single()[RecruiterProfiles.companyName]
                
                // Job 1
                val pair1 = jobTitles[(idx * 2) % jobTitles.size]
                val jobInfo1 = pair1.first
                val salary1 = pair1.second
                val title1 = jobInfo1.first
                val skills1 = jobInfo1.second
                
                val jId1 = Jobs.insert {
                    it[title] = title1
                    it[companyName] = companyNameStr
                    it[location] = "Bangalore"
                    it[description] = "Exciting opportunity to join the $companyNameStr technical team as a $title1. Work on building highly scalable products and modern systems."
                    it[requiredSkills] = skills1.joinToString(", ")
                    it[salaryPackage] = salary1
                    it[jobType] = if (title1.contains("Intern")) "INTERNSHIP" else "FULL_TIME"
                    it[minCgpa] = 7.0f
                    it[eligibilityCriteria] = "B.Tech/MCA with no active backlogs."
                    it[applicationDeadline] = now.plusDays(30)
                    it[postedBy] = recId
                    it[isActive] = true
                    it[createdAt] = now
                } get Jobs.id
                c1Jobs.add(jId1)

                // Job 2
                val pair2 = jobTitles[(idx * 2 + 1) % jobTitles.size]
                val jobInfo2 = pair2.first
                val salary2 = pair2.second
                val title2 = jobInfo2.first
                val skills2 = jobInfo2.second

                val jId2 = Jobs.insert {
                    it[title] = title2
                    it[companyName] = companyNameStr
                    it[location] = "Pune"
                    it[description] = "We are seeking a talented $title2 for our growing office in Pune. Candidate should be a quick learner and strong team player."
                    it[requiredSkills] = skills2.joinToString(", ")
                    it[salaryPackage] = salary2
                    it[jobType] = if (title2.contains("Intern")) "INTERNSHIP" else "FULL_TIME"
                    it[minCgpa] = 6.5f
                    it[eligibilityCriteria] = "B.Tech/MCA/M.Tech with CGPA >= 6.5"
                    it[applicationDeadline] = now.plusDays(15)
                    it[postedBy] = recId
                    it[isActive] = true
                    it[createdAt] = now
                } get Jobs.id
                c1Jobs.add(jId2)
            }

            // College 2 Jobs
            c2Recruiters.forEachIndexed { idx, recId ->
                val companyNameStr = RecruiterProfiles.select { RecruiterProfiles.userId eq recId }.single()[RecruiterProfiles.companyName]
                
                // Job 1
                val pair1 = jobTitles[(idx * 2) % jobTitles.size]
                val jobInfo1 = pair1.first
                val salary1 = pair1.second
                val title1 = jobInfo1.first
                val skills1 = jobInfo1.second

                val jId1 = Jobs.insert {
                    it[title] = title1
                    it[companyName] = companyNameStr
                    it[location] = "Mumbai"
                    it[description] = "Join $companyNameStr as a $title1 in our Mumbai branch. Collaborate with design and product teams to ship code daily."
                    it[requiredSkills] = skills1.joinToString(", ")
                    it[salaryPackage] = salary1
                    it[jobType] = if (title1.contains("Intern")) "INTERNSHIP" else "FULL_TIME"
                    it[minCgpa] = 7.5f
                    it[eligibilityCriteria] = "B.Tech with strong fundamental concepts."
                    it[applicationDeadline] = now.plusDays(20)
                    it[postedBy] = recId
                    it[isActive] = true
                    it[createdAt] = now
                } get Jobs.id
                c2Jobs.add(jId1)

                // Job 2
                val pair2 = jobTitles[(idx * 2 + 1) % jobTitles.size]
                val jobInfo2 = pair2.first
                val salary2 = pair2.second
                val title2 = jobInfo2.first
                val skills2 = jobInfo2.second

                val jId2 = Jobs.insert {
                    it[title] = title2
                    it[companyName] = companyNameStr
                    it[location] = "Hyderabad"
                    it[description] = "Exciting new opening for a $title2 at $companyNameStr. Candidates must have hands-on experience working on projects with similar stack."
                    it[requiredSkills] = skills2.joinToString(", ")
                    it[salaryPackage] = salary2
                    it[jobType] = if (title2.contains("Intern")) "INTERNSHIP" else "FULL_TIME"
                    it[minCgpa] = 7.0f
                    it[eligibilityCriteria] = "B.Tech/MCA/M.Tech with CGPA >= 7.0"
                    it[applicationDeadline] = now.plusDays(25)
                    it[postedBy] = recId
                    it[isActive] = true
                    it[createdAt] = now
                } get Jobs.id
                c2Jobs.add(jId2)
            }

            // 9. Seed Student Applications & Interviews
            val statuses = listOf("APPLIED", "SHORTLISTED", "INTERVIEW_SCHEDULED", "SELECTED", "REJECTED")

            // College 1 applications
            studentIdsC1.forEach { stuId ->
                val countToApply = (0..3).random()
                c1Jobs.shuffled().take(countToApply).forEach { jId ->
                    val statusVal = statuses.random()
                    val dateUpdated = now.minusDays((1..15).random().toLong())
                    
                    val (interviewD, interviewL, feedb) = if (statusVal == "INTERVIEW_SCHEDULED") {
                        Triple(
                            now.plusDays((1..5).random().toLong()),
                            "https://meet.google.com/abc-defg-hij",
                            "Technical Interview Round 1"
                        )
                    } else Triple(null, null, null)

                    JobApplications.insert {
                        it[JobApplications.jobId] = jId
                        it[JobApplications.studentId] = stuId
                        it[JobApplications.resumeUrl] = "https://smartcampus.storage/resumes/stu_${stuId}.pdf"
                        it[status] = statusVal
                        it[interviewDate] = interviewD
                        it[interviewLink] = interviewL
                        it[feedback] = feedb
                        it[appliedAt] = now.minusDays(20)
                        it[updatedAt] = dateUpdated
                    }

                    if (statusVal == "SELECTED") {
                        StudentProfiles.update({ StudentProfiles.userId eq stuId }) {
                            it[placementStatus] = "PLACED"
                        }
                    }
                }
            }

            // College 2 applications
            studentIdsC2.forEach { stuId ->
                val countToApply = (0..3).random()
                c2Jobs.shuffled().take(countToApply).forEach { jId ->
                    val statusVal = statuses.random()
                    val dateUpdated = now.minusDays((1..15).random().toLong())
                    
                    val (interviewD, interviewL, feedb) = if (statusVal == "INTERVIEW_SCHEDULED") {
                        Triple(
                            now.plusDays((1..5).random().toLong()),
                            "https://meet.google.com/xyz-qprs-tuv",
                            "Coding Round Interview"
                        )
                    } else Triple(null, null, null)

                    JobApplications.insert {
                        it[JobApplications.jobId] = jId
                        it[JobApplications.studentId] = stuId
                        it[JobApplications.resumeUrl] = "https://smartcampus.storage/resumes/stu_${stuId}.pdf"
                        it[status] = statusVal
                        it[interviewDate] = interviewD
                        it[interviewLink] = interviewL
                        it[feedback] = feedb
                        it[appliedAt] = now.minusDays(20)
                        it[updatedAt] = dateUpdated
                    }

                    if (statusVal == "SELECTED") {
                        StudentProfiles.update({ StudentProfiles.userId eq stuId }) {
                            it[placementStatus] = "PLACED"
                        }
                    }
                }
            }

            // 10. Seed Placement Drives
            val officerC1Id = Users.select { (Users.role eq "PLACEMENT_OFFICER") and (Users.collegeId eq c1Id) }.first()[Users.id]

            PlacementDrives.insert {
                it[companyName] = "TCS"
                it[description] = "Mass hiring drive for engineering graduates."
                it[eligibilityCriteria] = "CGPA >= 6.5 and no active backlogs."
                it[minCgpa] = 6.5f
                it[allowedBranches] = "Computer Science,Information Technology"
                it[salaryPackage] = "3.6 LPA"
                it[driveDate] = now.plusDays(10)
                it[postedBy] = officerC1Id
                it[createdAt] = now
            }

            PlacementDrives.insert {
                it[companyName] = "IBM"
                it[description] = "Special drive for technical consultant roles."
                it[eligibilityCriteria] = "CGPA >= 7.0, branches: CS, IT."
                it[minCgpa] = 7.0f
                it[allowedBranches] = "Computer Science,Information Technology"
                it[salaryPackage] = "7.2 LPA"
                it[driveDate] = now.plusDays(12)
                it[postedBy] = officerC2Id
                it[createdAt] = now
            }

            // Seed Trending Skills (always ensure they are generated)
            seedTrendingSkills(now)
        }
    }

    private fun seedTrendingSkills(now: LocalDateTime) {
        val trendingData = listOf(
            Triple("React", 96f, "Bangalore"),
            Triple("TensorFlow", 95f, "Bangalore"),
            Triple("Spring Boot", 92f, "Bangalore"),
            Triple("Node.js", 89f, "Bangalore"),
            Triple("Python", 94f, "Mumbai"),
            Triple("AWS", 93f, "Mumbai"),
            Triple("Java", 91f, "Mumbai"),
            Triple("Docker", 88f, "Mumbai"),
            Triple("TypeScript", 94f, "Pune"),
            Triple("Kotlin", 90f, "Pune"),
            Triple("Angular", 87f, "Pune"),
            Triple("Kubernetes", 85f, "Pune"),
            Triple("GCP", 92f, "Hyderabad"),
            Triple("Next.js", 91f, "Hyderabad"),
            Triple("PostgreSQL", 89f, "Hyderabad"),
            Triple("FastAPI", 86f, "Hyderabad"),
            Triple("Machine Learning", 95f, "Delhi NCR"),
            Triple("React Native", 88f, "Delhi NCR"),
            Triple("C++", 87f, "Delhi NCR"),
            Triple("MongoDB", 84f, "Delhi NCR"),
            Triple("Azure", 90f, "Chennai"),
            Triple("Flutter", 89f, "Chennai"),
            Triple("Swift", 86f, "Chennai"),
            Triple("Redis", 83f, "Chennai"),
            Triple("PHP", 82f, "Kolkata"),
            Triple("Laravel", 80f, "Kolkata"),
            Triple("SQL", 85f, "Kolkata"),
            Triple("Go", 88f, "Ahmedabad"),
            Triple("Rust", 84f, "Ahmedabad"),
            Triple("WordPress", 75f, "Jaipur"),
            Triple("UI/UX Design", 88f, "Jaipur"),
            Triple("Cybersecurity", 91f, "Kochi"),
            Triple("DevOps", 93f, "Noida"),
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
            if (count < 10) {
                val now = LocalDateTime.now()
                seedTrendingSkills(now)
            }
        }
    }
}

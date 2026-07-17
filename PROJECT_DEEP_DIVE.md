# SmartCampus Project Deep Dive

## 1. Overview
SmartCampus is an intelligent placement and career acceleration platform. It bridges the gap between students, placement officers, and recruiters by providing a centralized system for career development, skill tracking, intelligent recommendations, and job applications. 

## 2. Modules and Features

### Student Module
- **Profile Management**: Tracks academic progress (CGPA, branch, semester), domain interests, and location preferences.
- **Skill Tracking**: Students can add skills and self-assess proficiency levels (Beginner, Intermediate, Advanced).
- **Career Roadmaps**: Semester-by-semester interactive roadmaps detailing essential skills, certifications, and project benchmarks.
- **Resume Builder & ATS Scoring**: Integrated resume generation tool with an ATS (Applicant Tracking System) compatibility calculator.

### Placement Officer Module
- **Placement Drives**: Officers can create, manage, and delete campus placement drives.
- **Eligibility Filtering**: Dynamically fetches eligible students based on drive criteria (e.g., minimum CGPA, specific branch).
- **Mass Notifications**: Tools to broadcast critical updates and announcements to specific student cohorts.
- **Dashboard & Stats**: High-level statistical views of overall placement percentages and student statuses.

### Recruiter Module
- **Candidate Search**: Advanced search capabilities to find students based on specific skills, GPAs, and domain matching.
- **Job Posting**: Post external job opportunities exclusively for campus students.
- **Interview Scheduling**: Direct scheduling capabilities to invite students for technical/HR rounds.
- **Analytics Dashboard**: Recruiter-specific hiring analytics to track application pipelines.

### Admin Module
- **System Management**: Global overview of all system statistics.
- **Entity Management**: CRUD operations for participating Colleges and Companies.
- **User Auditing**: Approve pending recruiters and toggle user active/inactive statuses.

---

## 3. Architecture and Interactions

The system operates on a classic Client-Server architecture:
1. **The Client (Android App)**: Built with MVVM (Model-View-ViewModel) architecture. It handles user inputs, UI states, and caches specific data locally using Room DB. It requests data via HTTP calls.
2. **The Server (Ktor Backend)**: Acts as the central nervous system. It exposes RESTful endpoints, handles JWT-based authentication, implements complex heuristic logic, and interacts with external APIs.
3. **The Database (PostgreSQL)**: Serves as the persistent storage layer. 

**Interaction Flow**: 
- The Android app communicates to the Ktor server via the Retrofit networking library.
- Requests are intercepted for JWT token attachment.
- The Ktor server routes requests to specific Service classes (`StudentService`, `RecommendationService`, etc.).
- Services execute business logic, query the PostgreSQL database using JetBrains Exposed ORM, and return serialized JSON responses.

---

## 4. Technology Stack & Advantages

### Backend: Kotlin & Ktor
- **Why chosen**: Kotlin offers null-safety, concise syntax, and native asynchronous support via Coroutines. Ktor is a lightweight framework built ground-up for Kotlin coroutines, making it incredibly fast and efficient for high-throughput, non-blocking asynchronous operations compared to heavy frameworks like Spring Boot.
- **Advantages**: Minimal boilerplate, fast startup times, highly scalable for thousands of concurrent student requests during placement drives.

### Database: PostgreSQL & JetBrains Exposed ORM
- **Why chosen**: PostgreSQL is an enterprise-grade, highly reliable relational database perfect for managing structured relational data (Students -> Skills -> Jobs). JetBrains Exposed is a lightweight, type-safe SQL framework for Kotlin.
- **Advantages**: Exposed allows writing type-safe SQL queries directly in Kotlin, preventing SQL injection and compilation-time query validation.

### Frontend: Native Android (Kotlin, XML, Material Design)
- **Why chosen**: Native Android development ensures the highest performance, deep hardware integration, and a fluid, responsive UI experience.
- **Advantages**: Utilizes Retrofit for robust networking, Room for local caching, and Hilt for Dependency Injection. Material Design components guarantee an accessible and premium aesthetic.

---

## 5. External APIs

1. **JSearch (RapidAPI)**
   - **Purpose**: Used in the `ExternalJobService` to fetch live, real-world job postings (remote, internships, full-time). 
   - **Integration**: The backend makes outbound HTTP requests to RapidAPI, parses the third-party JSON into native Data Transfer Objects (DTOs), and streams it to the Android app. Fallback mock data is configured in case of API rate limits.

2. **GitHub API (GitHubTrendingService)**
   - **Purpose**: Used to dynamically fetch the hottest and most trending developer skills and technologies globally based on repository stars and tags. 
   - **Integration**: Feeds into the `SkillService` to augment the platform's internal skill database with live industry trends.

---

## 6. Algorithms and Heuristics

The platform moves beyond basic CRUD operations by utilizing custom heuristic algorithms to provide intelligent guidance.

### Personalized Skill Recommendation Heuristic Algorithm
Located in the `RecommendationService`, this algorithm calculates a highly personalized **Final Relevance Score** for every trending skill based on the student's unique profile. It uses a weighted multi-factor heuristic approach:

**Factors & Weights**:
1. **Region Demand Score (RDS) - Weight: 2.0x** 
   - Checks if the skill is highly demanded in the student's preferred geographical region. (Score 1.0 or 0.0)
2. **Semester Relevance Score (SRS) - Weight: 2.0x**
   - Compares the student's current semester to the ideal learning semester for the skill. Perfect match = 1.0, adjacent semester = 0.8/0.5, off-target = 0.2.
3. **Domain Match Score (DMS) - Weight: 1.5x**
   - Checks if the skill matches the student's primary domain of interest (e.g., "Web Development" vs "AI"). (Score 1.0 or 0.0)
4. **Skill Gap Score (SGS) - Weight: 1.5x**
   - Analyzes the student's current proficiency. Completely unknown = 1.0 (High priority to learn), Beginner = 0.7, Intermediate/Advanced = 0.0 (Already known, skip recommendation).
5. **Industry Growth Score (IGS) - Weight: 1.0x**
   - Evaluates macro-level market growth rates. >20% growth = 1.0, >10% growth = 0.7, else 0.4.

**Processing Pipeline**:
1. `Final Score = (2.0 * RDS) + (2.0 * SRS) + (1.5 * DMS) + (1.5 * SGS) + (1.0 * IGS)`
2. Generates qualitative "Reasons" (e.g., "High demand in Bangalore • Ideal for Sem 5").
3. Sorts all skills descending by the Final Score.
4. Filters out skills the student already knows at an "Advanced" level.
5. Returns the Top 5 absolute best skill recommendations tailored to that specific student.

### Skill Gap Analysis Algorithm
- Compares the user's localized subset of skills against the global Top 30 Demanded Skills.
- Computes a mathematical **Readiness Percentage** based on set intersections (Matched Skills / Total Market Demanded Skills) * 100.
- Identifies critical missing nodes (skills the market wants but the student lacks) and pushes them to the UI as urgent learning objectives.

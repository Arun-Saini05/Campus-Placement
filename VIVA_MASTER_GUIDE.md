# 🎓 MASTER VIVA GUIDE: Campus Placement App

*This document is your ultimate survival guide. It is reverse-engineered to act as your complete brain dump for the Campus Placement project. Read this, and you will dominate your Viva Voce.*

---

## PART 1 - PROJECT OVERVIEW

**Project Objective:** To build an intelligent, centralized platform connecting students, placement officers, and recruiters to streamline the campus placement process, augment skill discovery, and manage job applications effectively.

**Problem Statement & Existing Problems:** Traditional placement cells rely on fragmented Excel sheets, WhatsApp groups, and manual notice boards. Students lack real-time visibility into market demands and suffer from a "skill gap" due to outdated college curricula. Recruiters struggle to filter candidates efficiently.

**Proposed Solution:** A unified mobile application (Android) backed by a robust asynchronous server (Ktor) and relational database (PostgreSQL). It provides real-time job tracking, AI-driven skill recommendations based on geographic and industry trends, and automated ATS-friendly resume generation.

**Key Features:**
- Role-based Access (Student, Recruiter, Officer, Admin)
- Dynamic Career Roadmaps & Skill Gap Analysis
- Intelligent Skill Recommendation Engine (Heuristic-based)
- Real-time internal campus job applications & external RapidAPI job fetching.
- Mass Notifications & Recruiter Analytics.

**Scope & Future Scope:** Currently limited to campus-level execution. Future scope includes Machine Learning integration (collaborative filtering for job prediction), video interview hosting, and cross-platform (iOS/Web) expansion.

**Project Limitations:** Heuristics are statically weighted; real ML models would require massive historical data. Relies on external APIs (GitHub, JSearch) which have rate limits.

---

## PART 2 - COMPLETE ARCHITECTURE

**High-Level Architecture:** Client-Server decoupled architecture.
**Frontend:** Android Native (MVVM Pattern) -> Views (XML) -> ViewModels (LiveData/Flow) -> Repositories -> Retrofit (Network).
**Backend:** Ktor (Kotlin Coroutines) -> Routes (Controllers) -> Services (Business Logic) -> Exposed ORM -> PostgreSQL.

**Data & User Flow:**
`User Tap -> View -> ViewModel -> Repository -> Retrofit -> [HTTP/TCP/IP] -> Ktor Route -> Service -> Exposed -> DB -> Returns JSON -> Retrofit -> ViewModel -> UI Update.`

**Why this Architecture?**
- **MVVM vs MVC/MVP:** MVVM solves the "fat activity" problem. `ViewModel` survives configuration changes (screen rotations), unlike MVC where the controller (Activity) is destroyed.
- **Ktor vs Spring Boot:** Spring Boot uses one OS thread per request (heavy). Ktor uses Coroutines (lightweight state machines). For thousands of students accessing the app during a placement drive, Ktor prevents thread exhaustion and memory overflow.
- **REST over GraphQL/gRPC:** REST is standard, highly cacheable, and easier to debug for standard CRUD operations in a placement portal.

---

## PART 3 - PROJECT STRUCTURE

### Frontend (`app/src/main/`)
- `java/com/smartcampus/app/api/ApiClient.java`: *Singleton Retrofit Builder.* Intercepts requests to inject JWT.
- `java/com/smartcampus/app/ui/`: Contains Activities/Fragments.
- `java/com/smartcampus/app/viewmodel/`: Contains ViewModels binding data to UI.
- `res/layout/`: XML UI layouts (`activity_main.xml`).

### Backend (`src/main/kotlin/com/smartcampus/backend/`)
- `Application.kt`: Bootstraps Netty engine, loads JWT configs, connects to DB.
- `routes/`: E.g., `JobRoutes.kt`. Extracts HTTP parameters, passes to services.
- `service/`: E.g., `RecommendationService.kt`. The core brain. Contains all algorithmic logic.
- `models/Tables.kt`: Exposed ORM schema. Defines DB columns.
- `dto/`: Data Transfer Objects for JSON serialization.

---

## PART 4 - FRONTEND (ANDROID DEEP DIVE)

**Internal Rendering (Measure, Layout, Draw):**
1. **Measure:** Android traverses the view tree top-down. Parents ask children, "How big do you want to be?" (`onMeasure`).
2. **Layout:** Parents tell children exactly where to position themselves (X/Y coordinates) (`onLayout`).
3. **Draw:** The hardware (GPU) via Canvas/OpenGL draws the pixels on the screen (`onDraw`).
*ConstraintLayout* is used because it flattens the view hierarchy. Deeply nested `LinearLayouts` cause multiple expensive Measure/Layout passes. ConstraintLayout solves this using a Cassowary linear equation solver algorithm.

**Lifecycle:**
- `onCreate()`: UI inflated, variables initialized.
- `onStart()`: Visible, but not interacting.
- `onResume()`: Foreground, user interacting.
- `onPause()`: Partially obscured.
- `onStop()`: Hidden.
- `onDestroy()`: Killed by OS.

**XML Inflation:** `LayoutInflater.inflate()` reads the compiled binary XML, uses Java Reflection to instantiate the actual View classes (e.g., `android.widget.TextView`), and builds the in-memory View tree.

---

## PART 5 - KOTLIN INTERNALS

**Compilation Process:** `.kt` files -> Kotlin Compiler (kotlinc) -> `.class` (Java Bytecode) -> D8/R8 compiler -> `.dex` (Dalvik Executable) -> APK -> ART (Android Runtime) compiles it ahead-of-time (AOT) to native machine code on the device.

**Memory Management & GC:** Uses JVM/ART Garbage Collection (Generational). Objects start in "Young Generation". If they survive GC cycles, they move to "Old Generation". GC uses Mark-and-Sweep.

**Coroutines under the hood:** A suspend function is compiled into a standard function taking a hidden parameter called a `Continuation`. This acts as a state machine. When the function "suspends", it returns a `COROUTINE_SUSPENDED` flag and exits the thread. When ready to resume, the Continuation calls the function again, jumping to the exact line (state) it left off via a `switch` statement in bytecode.

---

## PART 6 & 7 - BACKEND & DATABASE

**PostgreSQL Schema (`Tables.kt`)**
- **Users**: `id` (PK), `email` (Unique), `password` (Hashed).
- **StudentProfiles**: `user_id` (FK to Users, Unique -> 1:1 relation).
- **Skills**: `id` (PK), `name` (Unique).
- **StudentSkills**: `profile_id`, `skill_id`. Resolves Many-to-Many relation between Students and Skills.
- **Jobs**: Postings by recruiters.

**Why Relational (PostgreSQL) over NoSQL (MongoDB)?**
Placement data is highly structured and requires ACID compliance. If a student applies for a job, we cannot have "eventual consistency" where the application is lost. `JOIN` operations are critical (e.g., Joining User -> Profile -> Skills -> Applied Jobs). NoSQL would require massive data duplication.

**Database Optimization:** B-Tree Indexes are created automatically on Primary Keys and `uniqueIndex()` fields (like Email), making lookup O(log N) instead of O(N) full table scans.

---

## PART 8 - APIs

**Example API: POST `/jobs/{jobId}/apply`**
- **Headers:** `Authorization: Bearer <JWT>`
- **Controller Flow:** `JobRoutes` extracts `jobId`. Calls `JobService.applyForJob()`.
- **DB Flow:** JetBrains Exposed opens a `transaction {}`. Executes SQL: `INSERT INTO job_applications (job_id, student_id, status) VALUES (...)`.
- **Response:** 200 OK `{"message": "Applied successfully", "success": true}`.
- **Security:** If JWT is missing or tampered with, Ktor returns `401 Unauthorized`.

---

## PART 10 & 11 - ALGORITHMS & HEURISTICS

### The Recommendation Heuristic Algorithm
**Purpose:** Recommends the absolute best skills to a student based on their profile and global trends.
**Logic:** Multi-factor weighted scoring.
**Formula:** `Final Score = (2.0 * RDS) + (2.0 * SRS) + (1.5 * DMS) + (1.5 * SGS) + (1.0 * IGS)`
- `RDS` (Region Demand): +1 if skill is trending in student's city.
- `SRS` (Semester): +1 if ideal semester matches student's semester.
- `DMS` (Domain): +1 if domains match.
- `SGS` (Skill Gap): +1 if student doesn't know it, 0 if they do.
- `IGS` (Growth): Macro growth rate scaling.

**Example Dry Run:**
- Student: Bangalore, Sem 5, Web Dev. Doesn't know React.
- Skill 'React' in DB: Bangalore (RDS=1), Ideal Sem 5 (SRS=1), Web Dev (DMS=1), Growth 25% (IGS=1).
- Score = (2.0*1) + (2.0*1) + (1.5*1) + (1.5*1) + (1.0*1) = **8.0 / 8.0**. React is pushed to position #1.

**Skill Gap Set Theory Algorithm:**
- `Missing Skills = MarketTopSkills (Set A) - StudentKnownSkills (Set B)`.
- `Readiness % = (|A intersect B| / |A|) * 100`.

---

## PART 14 & 15 - SECURITY & PERFORMANCE

**Security:**
- **JWT (JSON Web Token):** Stateless. Server signs token with a secret key using HMAC-SHA256. If a hacker alters the payload to claim `role="ADMIN"`, the signature verification fails.
- **SQL Injection:** Exposed ORM uses prepared statements (`PreparedStatement` in JDBC). Variables are sent separately from the SQL query string, preventing injection.

**Performance:**
- **RecyclerView:** Uses the `ViewHolder` pattern. Instead of inflating 1000 XML views for 1000 jobs, it inflates only ~10 (enough to fill the screen) and re-binds new data to the old views as you scroll.
- **Lazy Loading / Flow:** DB queries return standard collections, but large data is processed in-memory using Kotlin standard functions (`map`, `filter`).

---

## PART 19 - VIVA QUESTIONS (SAMPLE FROM 250+)

### Basic
1. **What is the difference between `val` and `var` in Kotlin?** 
   *Ans: `val` is immutable (read-only reference, like Java's `final`), `var` is mutable.*
2. **What is a Primary Key?** 
   *Ans: A unique identifier for a database record. Enforces entity integrity.*

### Intermediate
3. **Why did you use Retrofit instead of `HttpURLConnection`?** 
   *Ans: Retrofit abstracts thread management and serialization (Gson). `HttpURLConnection` requires massive boilerplate, manual input stream reading, and manual JSON parsing.*
4. **How does Ktor differ from Spring Boot?** 
   *Ans: Ktor relies on Kotlin Coroutines for async non-blocking I/O. Spring Boot traditionally uses a thread-per-request model, which consumes massive RAM under high load.*

### Advanced / Architecture
5. **What happens if someone steals the JWT token?** 
   *Ans: JWTs are bearer tokens. If stolen, they can be used. To mitigate this, tokens have a short expiration time (`expirationMs`). In a production environment, we would implement refresh tokens and HTTPS to prevent MITM (Man-in-the-Middle) sniffing.*
6. **Explain your recommendation algorithm's time complexity.** 
   *Ans: It is O(N) where N is the number of top trending skills. We limit N to 20 or 40 via the DB query (`LIMIT 40`), effectively making the algorithm run in O(1) constant time from the CPU's perspective, ensuring instantaneous UI updates.*

### Code-Based
7. **Why is `ViewModel` used instead of storing data in the `Activity`?** 
   *Ans: If the user rotates the screen, the Activity is destroyed and recreated. `ViewModel` survives configuration changes, preventing data loss and unnecessary network calls.*
8. **What does the `suspend` keyword actually do?** 
   *Ans: It marks a function as a coroutine. It tells the compiler to generate a state machine using a `Continuation` object, allowing the function to pause execution without blocking the main thread.*

---

## RAPID REVISION SHEET (20-Minute Pre-Viva Glance)

- **Frontend:** Android, Kotlin, MVVM, Retrofit, Coroutines, ConstraintLayout.
- **Backend:** Kotlin, Ktor, Netty, JWT Auth, Exposed ORM.
- **Database:** PostgreSQL (Relational, ACID, 3NF Normalized).
- **Core Algorithm:** Weighted Heuristic Scoring (RDS + SRS + DMS + SGS + IGS) for skill recommendations. Runs in O(N).
- **Coroutines:** Lightweight threads, non-blocking, uses Continuations (state machines).
- **RecyclerView:** Recycles views using ViewHolder pattern to save memory.
- **JWT:** Header.Payload.Signature. Secured by HMAC-SHA256 signature.
- **Exposed ORM:** Prevents SQL injection via Prepared Statements. Provides compile-time safety.

*Remember: Be confident. If asked a question you don't know, relate it back to a core concept (e.g., "I haven't used X, but in my architecture, we solved that problem by doing Y...").*

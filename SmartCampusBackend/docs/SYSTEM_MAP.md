# SmartCampus - System Architecture Map

This document maps the project's frontend (Android) to its backend (Ktor API), detailing which file handles which feature and how they communicate.

## 1. Authentication Module
| Android Activity | Layout (XML) | Backend Route | Purpose |
| :--- | :--- | :--- | :--- |
| `LoginActivity` | `activity_login.xml` | `POST /auth/login` | User login and session initialization. |
| `RegisterActivity` | `activity_register.xml` | `POST /auth/register` | New user (Student/Recruiter) registration. |
| `SplashActivity` | `activity_splash.xml` | N/A | Session validation and role-based redirection. |

## 2. Student Module
| Android Activity | Layout (XML) | Backend Route | Purpose |
| :--- | :--- | :--- | :--- |
| `StudentDashboardActivity` | `activity_student_dashboard.xml` | `GET /student/profile`, `GET /career/progress` | Main hub showing readiness stats and navigation cards. |
| `ProfileActivity` | `activity_profile.xml` | `GET /student/profile`, `PUT /student/profile` | View and edit student details, CGPA, and branch. |
| `SkillRecommendationActivity` | `activity_skill_recommendation.xml` | `GET /skills/recommend/personalized`, `GET /skills/trending` | Shows AI-suggested skills and global tech trends. |
| `SkillMapActivity` | `activity_skill_map.xml` | `GET /skills/regions` | Interactive map showing regional tech demand. |
| `SkillRoadmapActivity` | `activity_skill_roadmap.xml` | `GET /skills/roadmap/{skill}` | Step-by-step learning guide for specific technologies. |
| `CareerRoadmapActivity` | `activity_career_roadmap.xml` | `GET /career/roadmap` | Semester-wise career path and placement analytics. |

## 3. Jobs & Placement Module
| Android Activity | Layout (XML) | Backend Route | Purpose |
| :--- | :--- | :--- | :--- |
| `JobListActivity` | `activity_job_list.xml` | `GET /jobs` | List of on-campus and internal placement drives. |
| `JobDetailActivity` | `activity_job_detail.xml` | `GET /jobs/{id}`, `POST /jobs/{id}/apply` | Detailed job description and application submission. |
| `ExternalJobsActivity` | `activity_external_jobs.xml` | `GET /jobs/external` | Aggregated job listings from external sources via API. |
| `ApplicationTrackerActivity` | `activity_application_tracker.xml` | `GET /jobs/applications/me` | Tracks the status of submitted job applications. |

## 4. Resume & Career Tools
| Android Activity | Layout (XML) | Backend Route | Purpose |
| :--- | :--- | :--- | :--- |
| `ResumeBuilderActivity` | `activity_resume_builder.xml` | `GET /resume`, `POST /resume` | Form-based resume builder with professional templates. |
| `AtsScorerActivity` | `activity_ats_scorer.xml` | `POST /resume/ats-score` | Analyzes resume text against job descriptions for ATS score. |

## 5. Administrative & Management
| Android Activity | Layout (XML) | Backend Route | Purpose |
| :--- | :--- | :--- | :--- |
| `OfficerDashboardActivity` | `activity_officer_dashboard.xml` | `GET /officer/stats` | Placement officer hub for drive management. |
| `AdminDashboardActivity` | `activity_admin_dashboard.xml` | `GET /admin/stats` | Super-admin dashboard for system health. |
| `UserManagementActivity` | `activity_user_management.xml` | `GET /admin/users` | Admin tool to enable/disable user accounts. |
| `NotificationCenterActivity` | `activity_notification_center.xml` | `GET /notifications` | Central hub for all system and mass notifications. |

## 6. Shared Components & Models
*   **API Client**: `com.smartcampus.app.api.ApiClient` (Retrofit configuration).
*   **Session Manager**: `com.smartcampus.app.utils.SessionManager` (JWT storage).
*   **Models**: Located in `com.smartcampus.app.models` (POJOs for JSON mapping).

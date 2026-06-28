# SmartCampus API Documentation

This document provides a comprehensive list of all API endpoints available in the SmartCampus project.

## Authentication (`/auth`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/auth/register` | Register a new user (Student, Recruiter, or Officer). |
| `POST` | `/auth/login` | Authenticate and receive a JWT token. |

## Student (`/student`)
*All endpoints require `auth-jwt` authentication.*
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/student/profile` | Retrieve the authenticated student's profile. |
| `PUT` | `/student/profile` | Update the student's profile information. |
| `PUT` | `/student/placement-status` | Update the student's current placement status. |
| `POST` | `/student/skills` | Add a new skill to the student profile. |
| `DELETE` | `/student/skills/{skillId}` | Remove a skill from the student profile. |
| `POST` | `/student/certifications` | Add a new certification. |
| `POST` | `/student/projects` | Add a new project. |
| `POST` | `/student/experience` | Add new work experience. |

## Admin (`/admin`)
*All endpoints require `auth-jwt` authentication and `ADMIN` role.*
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/admin/users` | List all users in the system. |
| `GET` | `/admin/pending-recruiters` | List recruiters awaiting approval. |
| `PUT` | `/admin/approve/{recruiterId}` | Approve a pending recruiter. |
| `PUT` | `/admin/users/{userId}/toggle` | Toggle a user's active/inactive status. |
| `GET` | `/admin/stats` | Retrieve system-wide statistics. |
| `GET` | `/admin/colleges` | List all registered colleges. |
| `POST` | `/admin/colleges` | Add a new college. |
| `DELETE` | `/admin/colleges/{id}` | Delete a college. |
| `GET` | `/admin/companies` | List all registered companies. |
| `POST` | `/admin/companies` | Add a new company. |
| `DELETE` | `/admin/companies/{id}` | Delete a company. |
| `GET` | `/admin/jobs` | List all internal jobs. |
| `GET` | `/admin/applications` | List all job applications. |

## Career (`/career`)
*All endpoints require `auth-jwt` authentication.*
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/career/roadmap` | Get a personalized career roadmap. |
| `GET` | `/career/progress` | Get overall career development progress. |
| `GET` | `/career/analytics` | Get career dashboard analytics. |

## Jobs (`/jobs`)
*All endpoints require `auth-jwt` authentication.*
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/jobs/external` | Search for external jobs using RapidAPI. |
| `GET` | `/jobs` | List all internal jobs (supports filters like skill, location, salary). |
| `GET` | `/jobs/{jobId}` | Get details of a specific job. |
| `GET` | `/jobs/{jobId}/applications` | Get all applications for a job (Recruiter/Officer). |
| `POST` | `/jobs` | Post a new job (Recruiter/Officer). |
| `POST` | `/jobs/{jobId}/apply` | Apply for a specific job (Student). |
| `GET` | `/jobs/applications/me` | List applications submitted by the current student. |
| `PUT` | `/jobs/applications/{applicationId}/status` | Update application status (e.g., Shortlisted, Rejected). |
| `DELETE` | `/jobs/{jobId}` | Delete a job posting. |
| `PUT` | `/jobs/{jobId}/toggle-status` | Toggle job status between Active and Closed. |

## Notifications (`/notifications`)
*All endpoints require `auth-jwt` authentication.*
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/notifications` | Retrieve all notifications for the current user. |
| `GET` | `/notifications/unread-count` | Get the count of unread notifications. |
| `PUT` | `/notifications/{notificationId}/read` | Mark a specific notification as read. |
| `PUT` | `/notifications/read-all` | Mark all notifications for the user as read. |

## Placement Officer (`/officer`)
*All endpoints require `auth-jwt` authentication.*
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/officer/drives` | Create a new placement drive. |
| `GET` | `/officer/drives` | List all placement drives. |
| `GET` | `/officer/drives/{driveId}/eligible` | Get students eligible for a specific drive. |
| `GET` | `/officer/stats` | Retrieve placement statistics. |
| `GET` | `/officer/students` | Search and filter students across the platform. |
| `PUT` | `/officer/students/{studentId}/status` | Manually update a student's placement status. |
| `DELETE` | `/officer/drives/{driveId}` | Delete a placement drive. |
| `POST` | `/officer/notifications/mass` | Send mass notifications to specific student groups. |

## Recruiter (`/recruiter`)
*All endpoints require `auth-jwt` authentication.*
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/recruiter/search` | Search for candidates based on skills, GPA, etc. |
| `GET` | `/recruiter/jobs` | List all jobs posted by the current recruiter. |
| `GET` | `/recruiter/resume/{studentId}` | View a student's resume (tracks the view). |
| `GET` | `/recruiter/profile` | Get the recruiter's profile. |
| `PUT` | `/recruiter/profile` | Update the recruiter's profile. |
| `GET` | `/recruiter/analytics` | Get recruiter-specific hiring analytics. |
| `POST` | `/recruiter/schedule-interview` | Schedule an interview with a candidate. |

## Resume (`/resume`)
*All endpoints require `auth-jwt` authentication.*
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/resume` | Save student resume data. |
| `GET` | `/resume` | Retrieve saved resume data. |
| `POST` | `/resume/ats-score` | Calculate ATS compatibility score for a job description. |

## Skills & Roadmaps (`/skills`)
*All endpoints require `auth-jwt` authentication.*
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/skills/recommend/personalized` | Get AI-driven personalized skill recommendations. |
| `GET` | `/skills/recommend` | Get basic skill recommendations. |
| `GET` | `/skills/recommend/region/{region}` | Get skill recommendations based on geographic region. |
| `GET` | `/skills/gap-analysis` | Perform a gap analysis between current and desired skills. |
| `GET` | `/skills/trending` | Get trending skills (live GitHub data + fallback). |
| `GET` | `/skills/regions` | Get a list of regions for the interactive skill map. |
| `GET` | `/skills/roadmap/{skill}` | Get a learning roadmap for a specific skill. |
| `GET` | `/skills/roadmap/{skill}/progress` | Get the user's progress for a specific skill roadmap. |
| `POST` | `/skills/roadmap/{skill}/progress` | Update the user's progress for a skill roadmap. |
| `GET` | `/skills/completed-roadmaps` | Get a list of all skills for which the roadmap is 100% complete. |

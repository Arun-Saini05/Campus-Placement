package com.smartcampus.backend.routes

import com.smartcampus.backend.dto.*
import com.smartcampus.backend.service.ExternalJobService
import com.smartcampus.backend.service.JobService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.jobRoutes() {
    val jobService = JobService()
    val externalJobService = ExternalJobService()


    authenticate("auth-jwt") {
        route("/jobs") {
            // Get external jobs (RapidAPI)
            get("/external") {
                try {
                    val query = call.request.queryParameters["query"] ?: "internship"
                    val numPages = call.request.queryParameters["num_pages"] ?: "1"
                    
                    val config = application.environment.config
                    val rapidApiKey = config.propertyOrNull("rapidapi.key")?.getString() ?: ""
                    val rapidApiHost = config.propertyOrNull("rapidapi.host")?.getString() ?: ""

                    if (rapidApiKey == "YOUR_RAPIDAPI_KEY_HERE" || rapidApiKey.isEmpty()) {
                         call.respond(HttpStatusCode.ServiceUnavailable, MessageResponse("RapidAPI Key not configured", false))
                         return@get
                    }

                    val jobs = externalJobService.searchJobs(query, numPages = numPages, host = rapidApiHost, key = rapidApiKey)
                    call.respond(HttpStatusCode.OK, jobs)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Get all jobs with optional filters
            get {
                try {
                    val filter = JobFilterRequest(
                        skill = call.request.queryParameters["skill"],
                        location = call.request.queryParameters["location"],
                        minSalary = call.request.queryParameters["minSalary"],
                        jobType = call.request.queryParameters["jobType"]
                    )
                    val jobs = jobService.getAllJobs(filter)
                    call.respond(HttpStatusCode.OK, jobs)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Get job by ID
            get("/{jobId}") {
                try {
                    val jobId = call.parameters["jobId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid job ID")
                    val job = jobService.getJobById(jobId)
                    call.respond(HttpStatusCode.OK, job)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Get applications for a specific job (Recruiter/Officer)
            get("/{jobId}/applications") {
                try {
                    val jobId = call.parameters["jobId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid job ID")
                    val applications = jobService.getApplicationsForJob(jobId)
                    call.respond(HttpStatusCode.OK, applications)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }


            // Post a new job (Recruiter/Officer)
            post {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val request = call.receive<JobRequest>()
                    val response = jobService.createJob(userId, request)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Apply for a job (Student)
            post("/{jobId}/apply") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val jobId = call.parameters["jobId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid job ID")
                    val resumeUrl = call.request.queryParameters["resumeUrl"]
                    val response = jobService.applyForJob(userId, jobId, resumeUrl)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Get my applications
            get("/applications/me") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val applications = jobService.getMyApplications(userId)
                    call.respond(HttpStatusCode.OK, applications)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Update application status
            put("/applications/{applicationId}/status") {
                try {
                    val applicationId = call.parameters["applicationId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid application ID")
                    val status = call.request.queryParameters["status"]
                        ?: throw IllegalArgumentException("Status required")
                    val response = jobService.updateApplicationStatus(applicationId, status)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Delete a job
            delete("/{jobId}") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val jobId = call.parameters["jobId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid job ID")
                    val response = jobService.deleteJob(userId, jobId)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Toggle job vacancy status
            put("/{jobId}/toggle-status") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val jobId = call.parameters["jobId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid job ID")
                    val response = jobService.toggleJobStatus(userId, jobId)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }
        }
    }
}

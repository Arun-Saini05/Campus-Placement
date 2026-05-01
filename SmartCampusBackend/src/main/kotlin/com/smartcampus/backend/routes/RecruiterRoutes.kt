package com.smartcampus.backend.routes

import com.smartcampus.backend.dto.*
import com.smartcampus.backend.service.RecruiterService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.recruiterRoutes() {
    val recruiterService = RecruiterService()

    authenticate("auth-jwt") {
        route("/recruiter") {
            // Search candidates
            post("/search") {
                try {
                    val request = call.receive<CandidateSearchRequest>()
                    val candidates = recruiterService.searchCandidates(request)
                    call.respond(HttpStatusCode.OK, candidates)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Get jobs posted by this recruiter
            get("/jobs") {
                try {
                    val recruiterId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val jobs = recruiterService.getJobsByRecruiter(recruiterId)
                    call.respond(HttpStatusCode.OK, jobs)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, MessageResponse(e.message ?: "Error loading jobs", false))
                }
            }


            // View student resume (tracks view for FR-36)
            get("/resume/{studentId}") {
                try {
                    val recruiterId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val studentId = call.parameters["studentId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid student ID")
                    val candidate = recruiterService.viewResume(recruiterId, studentId)
                    call.respond(HttpStatusCode.OK, candidate)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }
            // Profile management
            get("/profile") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                val profile = recruiterService.getProfile(userId)
                if (profile != null) call.respond(HttpStatusCode.OK, profile)
                else call.respond(HttpStatusCode.NotFound, MessageResponse("Profile not found", false))
            }

            put("/profile") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                val request = call.receive<RecruiterProfileRequest>()
                val profile = recruiterService.updateProfile(userId, request)
                call.respond(HttpStatusCode.OK, profile)
            }

            // Analytics
            get("/analytics") {
                val recruiterId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                val analytics = recruiterService.getAnalytics(recruiterId)
                call.respond(HttpStatusCode.OK, analytics)
            }

            // Interview Scheduling
            post("/schedule-interview") {
                try {
                    val request = call.receive<InterviewScheduleRequest>()
                    val response = recruiterService.scheduleInterview(request)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }
        }
    }
}


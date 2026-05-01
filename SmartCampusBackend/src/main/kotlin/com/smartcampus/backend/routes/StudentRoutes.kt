package com.smartcampus.backend.routes

import com.smartcampus.backend.dto.*
import com.smartcampus.backend.service.StudentService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.studentRoutes() {
    val studentService = StudentService()

    authenticate("auth-jwt") {
        route("/student") {
            // Get profile
            get("/profile") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val profile = studentService.getProfile(userId)
                    call.respond(HttpStatusCode.OK, profile)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Update profile
            put("/profile") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val request = call.receive<StudentProfileRequest>()
                    val response = studentService.updateProfile(userId, request)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Update placement status
            put("/placement-status") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val request = call.receive<PlacementStatusRequest>()
                    val response = studentService.updatePlacementStatus(userId, request.status)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Add skill
            post("/skills") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val request = call.receive<AddSkillRequest>()
                    val response = studentService.addSkill(userId, request)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Remove skill
            delete("/skills/{skillId}") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val skillId = call.parameters["skillId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid skill ID")
                    val response = studentService.removeSkill(userId, skillId)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Add certification
            post("/certifications") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val request = call.receive<CertificationRequest>()
                    val response = studentService.addCertification(userId, request)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Add project
            post("/projects") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val request = call.receive<ProjectRequest>()
                    val response = studentService.addProject(userId, request)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Add experience
            post("/experience") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val request = call.receive<ExperienceRequest>()
                    val response = studentService.addExperience(userId, request)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }
        }
    }
}

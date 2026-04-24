package com.smartcampus.backend.routes

import com.smartcampus.backend.dto.*
import com.smartcampus.backend.service.PlacementOfficerService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.officerRoutes() {
    val officerService = PlacementOfficerService()

    authenticate("auth-jwt") {
        route("/officer") {
            // Create placement drive
            post("/drives") {
                try {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
                    val request = call.receive<PlacementDriveRequest>()
                    val response = officerService.createDrive(userId, request)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Get all drives
            get("/drives") {
                try {
                    val drives = officerService.getAllDrives()
                    call.respond(HttpStatusCode.OK, drives)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Get eligible students for a drive
            get("/drives/{driveId}/eligible") {
                try {
                    val driveId = call.parameters["driveId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid drive ID")
                    val students = officerService.getEligibleStudents(driveId)
                    call.respond(HttpStatusCode.OK, students)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Get placement stats
            get("/stats") {
                try {
                    val stats = officerService.getPlacementStats()
                    call.respond(HttpStatusCode.OK, stats)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Search/Filter all students
            get("/students") {
                try {
                    val branch = call.request.queryParameters["branch"]
                    val minCgpa = call.request.queryParameters["minCgpa"]?.toFloatOrNull()
                    val status = call.request.queryParameters["status"]

                    val students = officerService.searchStudents(branch, minCgpa, status)
                    call.respond(HttpStatusCode.OK, students)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Mass Notification System
            post("/notifications/mass") {
                try {
                    val request = call.receive<MassNotificationRequest>()
                    val response = officerService.sendMassNotification(request)
                    call.respond(if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }
        }
    }
}

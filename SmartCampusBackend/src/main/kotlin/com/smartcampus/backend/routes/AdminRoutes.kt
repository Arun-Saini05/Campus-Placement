package com.smartcampus.backend.routes

import com.smartcampus.backend.dto.MessageResponse
import com.smartcampus.backend.service.AdminService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.adminRoutes() {
    val adminService = AdminService()

    authenticate("auth-jwt") {
        route("/admin") {
            // Get all users
            get("/users") {
                try {
                    val role = call.principal<JWTPrincipal>()!!.payload.getClaim("role").asString()
                    if (role != "ADMIN") {
                        call.respond(HttpStatusCode.Forbidden, MessageResponse("Admin access required", false))
                        return@get
                    }
                    val users = adminService.getAllUsers()
                    call.respond(HttpStatusCode.OK, users)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Get pending recruiters
            get("/pending-recruiters") {
                try {
                    val role = call.principal<JWTPrincipal>()!!.payload.getClaim("role").asString()
                    if (role != "ADMIN") {
                        call.respond(HttpStatusCode.Forbidden, MessageResponse("Admin access required", false))
                        return@get
                    }
                    val recruiters = adminService.getPendingRecruiters()
                    call.respond(HttpStatusCode.OK, recruiters)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Approve recruiter
            put("/approve/{recruiterId}") {
                try {
                    val role = call.principal<JWTPrincipal>()!!.payload.getClaim("role").asString()
                    if (role != "ADMIN") {
                        call.respond(HttpStatusCode.Forbidden, MessageResponse("Admin access required", false))
                        return@put
                    }
                    val recruiterId = call.parameters["recruiterId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid recruiter ID")
                    val response = adminService.approveRecruiter(recruiterId)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Toggle user active status
            put("/users/{userId}/toggle") {
                try {
                    val role = call.principal<JWTPrincipal>()!!.payload.getClaim("role").asString()
                    if (role != "ADMIN") {
                        call.respond(HttpStatusCode.Forbidden, MessageResponse("Admin access required", false))
                        return@put
                    }
                    val userId = call.parameters["userId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid user ID")
                    val activate = call.request.queryParameters["activate"]?.toBoolean() ?: true
                    val response = adminService.toggleUserStatus(userId, activate)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // Get system stats
            get("/stats") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    println("Admin stats requested by user with role: $role")
                    
                    if (role != "ADMIN") {
                        println("Access denied: User is not an ADMIN")
                        call.respond(HttpStatusCode.Forbidden, MessageResponse("Admin access required", false))
                        return@get
                    }
                    val stats = adminService.getSystemStats()
                    println("Returning stats: $stats")
                    call.respond(HttpStatusCode.OK, stats)
                } catch (e: Exception) {
                    println("Error fetching admin stats: ${e.message}")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Error", false))
                }
            }

            // --- College Management ---
            get("/colleges") {
                call.respond(HttpStatusCode.OK, adminService.getAllColleges())
            }

            post("/colleges") {
                val params = call.receive<Map<String, String>>()
                adminService.addCollege(params["name"]!!, params["location"]!!)
                call.respond(HttpStatusCode.Created, MessageResponse("College added"))
            }

            delete("/colleges/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                adminService.deleteCollege(id)
                call.respond(HttpStatusCode.OK, MessageResponse("College deleted"))
            }

            // --- Company Management ---
            get("/companies") {
                call.respond(HttpStatusCode.OK, adminService.getAllCompanies())
            }

            post("/companies") {
                val params = call.receive<Map<String, String>>()
                adminService.addCompany(params["name"]!!, params["industry"]!!, params["location"]!!)
                call.respond(HttpStatusCode.Created, MessageResponse("Company added"))
            }

            delete("/companies/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                adminService.deleteCompany(id)
                call.respond(HttpStatusCode.OK, MessageResponse("Company deleted"))
            }

            // --- Job & Application Management ---
            get("/jobs") {
                call.respond(HttpStatusCode.OK, adminService.getAllJobs())
            }

            get("/applications") {
                call.respond(HttpStatusCode.OK, adminService.getAllApplications())
            }
        }
    }
}

package com.smartcampus.backend.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.smartcampus.backend.dto.*
import com.smartcampus.backend.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime
import java.util.*

class AuthService(
    private val jwtSecret: String,
    private val jwtIssuer: String,
    private val jwtAudience: String,
    private val jwtExpirationMs: Long
) {
    fun register(request: RegisterRequest): AuthResponse {
        val existingUser = transaction {
            Users.select { Users.email eq request.email }.singleOrNull()
        }

        if (existingUser != null) {
            throw IllegalArgumentException("User with email ${request.email} already exists")
        }

        val validRoles = listOf("STUDENT", "RECRUITER", "PLACEMENT_OFFICER", "ADMIN")
        if (request.role !in validRoles) {
            throw IllegalArgumentException("Invalid role: ${request.role}")
        }

        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())

        val userId = transaction {
            Users.insert {
                it[name] = request.name
                it[email] = request.email
                it[password] = hashedPassword
                it[enrollmentId] = request.enrollmentId
                it[role] = request.role
                it[isActive] = request.role != "RECRUITER"
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            } get Users.id
        }

        val token = generateToken(userId, request.email, request.role)

        return AuthResponse(
            token = token,
            userId = userId,
            name = request.name,
            email = request.email,
            role = request.role
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        println("Login attempt for email: ${request.email}")
        val user = transaction {
            Users.select { Users.email eq request.email }.singleOrNull()
        } ?: run {
            println("Login failed: User not found")
            throw IllegalArgumentException("Invalid email or password")
        }

        if (!BCrypt.checkpw(request.password, user[Users.password])) {
            println("Login failed: Incorrect password")
            throw IllegalArgumentException("Invalid email or password")
        }

        if (!user[Users.isActive]) {
            println("Login failed: Account inactive")
            throw IllegalArgumentException("Account is not active. Please contact admin.")
        }

        println("Login successful for user: ${user[Users.email]} (Role: ${user[Users.role]})")

        val token = generateToken(user[Users.id], user[Users.email], user[Users.role])

        return AuthResponse(
            token = token,
            userId = user[Users.id],
            name = user[Users.name],
            email = user[Users.email],
            role = user[Users.role]
        )
    }

    private fun generateToken(userId: Int, email: String, role: String): String {
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + jwtExpirationMs))
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}

package com.smartcampus.backend.plugins

import com.smartcampus.backend.models.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val config = HikariConfig().apply {
        jdbcUrl = environment.config.property("database.url").getString()
        driverClassName = environment.config.property("database.driver").getString()
        username = environment.config.property("database.user").getString()
        password = environment.config.property("database.password").getString()
        maximumPoolSize = environment.config.property("database.maxPoolSize").getString().toInt()
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    // Create tables
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            Users,
            StudentProfiles,
            Skills,
            StudentSkills,
            Certifications,
            Projects,
            WorkExperiences,
            TrendingSkills,
            Jobs,
            JobApplications,
            ResumeData,
            PlacementDrives,
            Notifications,
            ResumeViews,
            RoadmapProgress,
            Colleges,
            Companies,
            RecruiterProfiles
        )
    }

    log.info("Database connected and tables created successfully")
}

package com.smartcampus.backend.service

import com.smartcampus.backend.dto.SkillRecommendation
import com.smartcampus.backend.models.TrendingSkills
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Serializable
data class GitHubSearchResponse(
    val total_count: Int = 0,
    val items: List<GitHubRepo> = emptyList()
)

@Serializable
data class GitHubRepo(
    val name: String = "",
    val full_name: String = "",
    val language: String? = null,
    val stargazers_count: Int = 0,
    val topics: List<String> = emptyList(),
    val description: String? = null
)

object GitHubTrendingService {

    private var lastFetchTime: LocalDateTime? = null
    private var cachedResults: List<SkillRecommendation> = emptyList()
    private const val CACHE_HOURS = 1L

    // Known tech skills to look for in GitHub topics/languages
    private val techSkillMapping = mapOf(
        // Languages
        "kotlin" to "Mobile/Backend",
        "java" to "Backend",
        "python" to "AI/ML/Backend",
        "javascript" to "Web",
        "typescript" to "Web",
        "go" to "Backend/Cloud",
        "rust" to "Systems",
        "swift" to "Mobile",
        "dart" to "Mobile",
        "ruby" to "Web",
        "php" to "Web",
        "c++" to "Systems",
        "c#" to "Backend/Game",
        "scala" to "Data",
        "r" to "Data Science",
        // Frameworks/Tools (from topics)
        "react" to "Web Frontend",
        "nextjs" to "Web Fullstack",
        "vue" to "Web Frontend",
        "angular" to "Web Frontend",
        "svelte" to "Web Frontend",
        "django" to "Web Backend",
        "flask" to "Web Backend",
        "spring-boot" to "Backend",
        "fastapi" to "Web Backend",
        "flutter" to "Mobile",
        "react-native" to "Mobile",
        "docker" to "DevOps",
        "kubernetes" to "Cloud/DevOps",
        "terraform" to "Cloud/DevOps",
        "aws" to "Cloud",
        "azure" to "Cloud",
        "gcp" to "Cloud",
        "tensorflow" to "AI/ML",
        "pytorch" to "AI/ML",
        "langchain" to "AI/LLMs",
        "openai" to "AI/LLMs",
        "machine-learning" to "AI/ML",
        "deep-learning" to "AI/ML",
        "llm" to "AI/LLMs",
        "graphql" to "Web API",
        "postgresql" to "Database",
        "mongodb" to "Database",
        "redis" to "Database/Cache",
        "kafka" to "Data Engineering",
        "spark" to "Data Engineering",
        "elasticsearch" to "Search/Data",
        "cybersecurity" to "Security",
        "blockchain" to "Web3",
        "solidity" to "Web3",
        "tailwind" to "Web Styling",
        "bootstrap" to "Web Styling",
        "numpy" to "Data Science",
        "pandas" to "Data Science",
        "scikit-learn" to "AI/ML",
        "jenkins" to "DevOps",
        "ansible" to "DevOps",
        "github-actions" to "DevOps",
        "vitest" to "Testing",
        "jest" to "Testing"
    )

    // Regional mapping for tech hubs
    private val regionAssignment = listOf(
        "Bangalore", "Mumbai", "Pune", "Hyderabad", "Delhi NCR", "Chennai", 
        "Kolkata", "Ahmedabad", "Jaipur", "Kochi", "Indore", "Noida", 
        "Coimbatore", "Thiruvananthapuram", "Chandigarh", "Global"
    )

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        engine {
            requestTimeout = 15000
        }
    }

    suspend fun fetchAndCacheTrending(): List<SkillRecommendation> {
        // Return cached if fresh
        val now = LocalDateTime.now()
        if (lastFetchTime != null && cachedResults.isNotEmpty()) {
            val hoursSinceLastFetch = ChronoUnit.HOURS.between(lastFetchTime, now)
            if (hoursSinceLastFetch < CACHE_HOURS) {
                return cachedResults
            }
        }

        return try {
            val skills = fetchFromGitHub()
            if (skills.isNotEmpty()) {
                saveToDB(skills)
                cachedResults = skills
                lastFetchTime = now
            }
            skills.ifEmpty { getFromDB() }
        } catch (e: Exception) {
            println("GitHub API fetch failed: ${e.message}, using DB fallback")
            getFromDB()
        }
    }

    private suspend fun fetchFromGitHub(): List<SkillRecommendation> {
        val skillScores = mutableMapOf<String, MutableList<Int>>()
        val skillCategories = mutableMapOf<String, String>()

        // Fetch trending repos (recently created, high stars)
        val queries = listOf(
            "stars:>5000+pushed:>2024-01-01",
            "stars:>1000+created:>2024-06-01"
        )

        for (query in queries) {
            try {
                val response: GitHubSearchResponse = client.get("https://api.github.com/search/repositories") {
                    parameter("q", query)
                    parameter("sort", "stars")
                    parameter("order", "desc")
                    parameter("per_page", 50)
                    header("Accept", "application/vnd.github.v3+json")
                    header("User-Agent", "SmartCampusApp")
                }.body()

                for (repo in response.items) {
                    // Extract language
                    repo.language?.lowercase()?.let { lang ->
                        if (lang in techSkillMapping) {
                            skillScores.getOrPut(lang) { mutableListOf() }.add(repo.stargazers_count)
                            skillCategories[lang] = techSkillMapping[lang]!!
                        }
                    }

                    // Extract topics
                    for (topic in repo.topics) {
                        val topicLower = topic.lowercase()
                        if (topicLower in techSkillMapping) {
                            skillScores.getOrPut(topicLower) { mutableListOf() }.add(repo.stargazers_count)
                            skillCategories[topicLower] = techSkillMapping[topicLower]!!
                        }
                    }
                }
            } catch (e: Exception) {
                println("GitHub query failed for '$query': ${e.message}")
            }
        }

        if (skillScores.isEmpty()) return emptyList()

        // Calculate demand scores (normalized 50-98 range)
        val maxAvg = skillScores.values.maxOfOrNull { it.average() } ?: 1.0
        var regionIndex = 0

        return skillScores.map { (skill, stars) ->
            val avgStars = stars.average()
            val normalizedScore = ((avgStars / maxAvg) * 48 + 50).toFloat().coerceIn(50f, 98f)
            val region = regionAssignment[regionIndex++ % regionAssignment.size]

            SkillRecommendation(
                skillName = formatSkillName(skill),
                demandScore = normalizedScore,
                reason = "Trending on GitHub — ${skillCategories[skill] ?: "Tech"}",
                region = region
            )
        }.sortedByDescending { it.demandScore }
    }

    private fun formatSkillName(raw: String): String {
        val nameMap = mapOf(
            "javascript" to "JavaScript", "typescript" to "TypeScript",
            "python" to "Python", "kotlin" to "Kotlin", "java" to "Java",
            "go" to "Go", "rust" to "Rust", "swift" to "Swift", "dart" to "Dart",
            "ruby" to "Ruby", "php" to "PHP", "c++" to "C++", "c#" to "C#",
            "scala" to "Scala", "r" to "R",
            "react" to "React", "nextjs" to "Next.js", "vue" to "Vue.js",
            "angular" to "Angular", "svelte" to "Svelte",
            "django" to "Django", "flask" to "Flask",
            "spring-boot" to "Spring Boot", "fastapi" to "FastAPI",
            "flutter" to "Flutter", "react-native" to "React Native",
            "docker" to "Docker", "kubernetes" to "Kubernetes",
            "terraform" to "Terraform", "aws" to "AWS", "azure" to "Azure", "gcp" to "GCP",
            "tensorflow" to "TensorFlow", "pytorch" to "PyTorch",
            "langchain" to "LangChain", "openai" to "OpenAI API",
            "machine-learning" to "Machine Learning", "deep-learning" to "Deep Learning",
            "llm" to "LLMs", "graphql" to "GraphQL",
            "postgresql" to "PostgreSQL", "mongodb" to "MongoDB",
            "redis" to "Redis", "kafka" to "Apache Kafka", "spark" to "Apache Spark",
            "elasticsearch" to "Elasticsearch",
            "cybersecurity" to "Cybersecurity", "blockchain" to "Blockchain",
            "solidity" to "Solidity"
        )
        return nameMap[raw] ?: raw.replaceFirstChar { it.uppercase() }
    }

    private fun saveToDB(skills: List<SkillRecommendation>) {
        transaction {
            // Clear old GitHub-sourced data
            TrendingSkills.deleteWhere { dataSource eq "GitHub API" }

            val now = LocalDateTime.now()
            for (skill in skills) {
                TrendingSkills.insert {
                    it[skillName] = skill.skillName
                    it[demandScore] = skill.demandScore
                    it[region] = skill.region
                    it[semester] = null
                    it[branch] = null
                    it[dataSource] = "GitHub API"
                    it[scrapedAt] = now
                }
            }
        }
    }

    private fun getFromDB(): List<SkillRecommendation> {
        return transaction {
            TrendingSkills.selectAll()
                .orderBy(TrendingSkills.demandScore to org.jetbrains.exposed.sql.SortOrder.DESC)
                .limit(40)
                .map { row ->
                    SkillRecommendation(
                        skillName = row[TrendingSkills.skillName],
                        demandScore = row[TrendingSkills.demandScore],
                        reason = when (row[TrendingSkills.dataSource]) {
                            "GitHub API" -> "Trending on GitHub"
                            else -> "Industry trending skill"
                        },
                        region = row[TrendingSkills.region]
                    )
                }
        }
    }
}

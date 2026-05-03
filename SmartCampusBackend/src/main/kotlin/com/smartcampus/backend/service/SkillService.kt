package com.smartcampus.backend.service

import com.smartcampus.backend.dto.*
import com.smartcampus.backend.models.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class RegionInfo(
    val name: String,
    val lat: Double,
    val lng: Double,
    val skillCount: Int,
    val topSkills: List<String>
)

class SkillService {

    fun getRecommendations(userId: Int): List<SkillRecommendation> {
        return transaction {
            val profile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull()

            val semester = profile?.get(StudentProfiles.semester)
            val branch = profile?.get(StudentProfiles.branch)
            val region = profile?.get(StudentProfiles.preferredRegion)

            val currentSkillIds = if (profile != null) {
                val profileId = profile[StudentProfiles.id]
                StudentSkills.select { StudentSkills.studentProfileId eq profileId }
                    .map { row -> row[StudentSkills.skillId] }
            } else emptyList()

            val currentSkillNames = if (currentSkillIds.isNotEmpty()) {
                Skills.select { Skills.id inList currentSkillIds }
                    .map { row -> row[Skills.name].lowercase() }
            } else emptyList()

            val trendingSkills = TrendingSkills.selectAll()
                .orderBy(TrendingSkills.demandScore to SortOrder.DESC)
                .limit(20)
                .map { row ->
                    val skillName = row[TrendingSkills.skillName]
                    val isAlreadyKnown = currentSkillNames.any { it.equals(skillName, ignoreCase = true) }

                    SkillRecommendation(
                        skillName = skillName,
                        demandScore = row[TrendingSkills.demandScore],
                        reason = when {
                            !isAlreadyKnown && row[TrendingSkills.region] == region ->
                                "High demand in $region region"
                            !isAlreadyKnown && row[TrendingSkills.semester] == semester ->
                                "Recommended for semester $semester"
                            !isAlreadyKnown && row[TrendingSkills.branch] == branch ->
                                "Trending in $branch branch"
                            !isAlreadyKnown ->
                                "Trending skill with high demand"
                            else -> "You already know this skill"
                        },
                        region = row[TrendingSkills.region]
                    )
                }
                .filter { rec -> !currentSkillNames.any { known -> known.equals(rec.skillName, ignoreCase = true) } }

            trendingSkills
        }
    }

    fun getRegionRecommendations(region: String): List<SkillRecommendation> {
        return transaction {
            TrendingSkills.select { TrendingSkills.region eq region }
                .orderBy(TrendingSkills.demandScore to SortOrder.DESC)
                .limit(20)
                .map { row ->
                    SkillRecommendation(
                        skillName = row[TrendingSkills.skillName],
                        demandScore = row[TrendingSkills.demandScore],
                        reason = "High demand in $region",
                        region = region
                    )
                }
        }
    }

    fun getSkillGapAnalysis(userId: Int): SkillGapReport {
        return transaction {
            val profile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull()

            val currentSkills = if (profile != null) {
                val profileId = profile[StudentProfiles.id]
                (StudentSkills innerJoin Skills)
                    .select { StudentSkills.studentProfileId eq profileId }
                    .map { row -> row[Skills.name] }
            } else emptyList()

            val demandedSkills = TrendingSkills.selectAll()
                .orderBy(TrendingSkills.demandScore to SortOrder.DESC)
                .limit(30)
                .map { row -> row[TrendingSkills.skillName] }
                .distinct()

            val missingSkills = demandedSkills
                .filter { demanded -> !currentSkills.any { cs -> cs.equals(demanded, ignoreCase = true) } }
                .map { skillName ->
                    val trend = TrendingSkills.select { TrendingSkills.skillName eq skillName }
                        .orderBy(TrendingSkills.demandScore to SortOrder.DESC)
                        .firstOrNull()

                    SkillRecommendation(
                        skillName = skillName,
                        demandScore = trend?.get(TrendingSkills.demandScore) ?: 0f,
                        reason = "Required by market, not in your skillset",
                        region = trend?.get(TrendingSkills.region)
                    )
                }

            val readiness = if (demandedSkills.isNotEmpty()) {
                val matched = currentSkills.count { current ->
                    demandedSkills.any { d -> d.equals(current, ignoreCase = true) }
                }
                (matched.toFloat() / demandedSkills.size) * 100
            } else 0f

            SkillGapReport(
                currentSkills = currentSkills,
                demandedSkills = demandedSkills,
                missingSkills = missingSkills,
                readinessPercentage = readiness
            )
        }
    }

    suspend fun getTrendingSkills(): List<SkillRecommendation> {
        // Try to fetch fresh data from GitHub API first
        val githubSkills = try {
            GitHubTrendingService.fetchAndCacheTrending()
        } catch (e: Exception) {
            emptyList()
        }

        val dbFallback = transaction {
            TrendingSkills.selectAll()
                .orderBy(TrendingSkills.demandScore to SortOrder.DESC)
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

        // Merge results: GitHub skills first, then DB fallback for missing skills
        val combined = githubSkills.toMutableList()
        if (combined.size < 15) {
            val existingNames = combined.map { it.skillName.lowercase() }.toSet()
            dbFallback.forEach { dbSkill ->
                if (dbSkill.skillName.lowercase() !in existingNames && combined.size < 30) {
                    combined.add(dbSkill)
                }
            }
        }

        return combined.sortedByDescending { it.demandScore }
    }

    fun getRegionList(): List<RegionInfo> {
        val regionCoords = mapOf(
            "Bangalore" to Pair(12.9716, 77.5946),
            "Mumbai" to Pair(19.0760, 72.8777),
            "Pune" to Pair(18.5204, 73.8567),
            "Hyderabad" to Pair(17.3850, 78.4867),
            "Delhi NCR" to Pair(28.7041, 77.1025),
            "Chennai" to Pair(13.0827, 80.2707),
            "Kolkata" to Pair(22.5726, 88.3639),
            "Ahmedabad" to Pair(23.0225, 72.5714),
            "Jaipur" to Pair(26.9124, 75.7873),
            "Kochi" to Pair(9.9312, 76.2673),
            "Indore" to Pair(22.7196, 75.8577),
            "Noida" to Pair(28.5355, 77.3910),
            "Coimbatore" to Pair(11.0168, 76.9558),
            "Thiruvananthapuram" to Pair(8.5241, 76.9366),
            "Chandigarh" to Pair(30.7333, 76.7794),
            "Global" to Pair(20.5937, 78.9629)
        )

        return transaction {
            val regions = TrendingSkills.slice(TrendingSkills.region)
                .selectAll()
                .mapNotNull { it[TrendingSkills.region] }
                .distinct()

            regions.mapNotNull { region ->
                val coords = regionCoords[region] ?: return@mapNotNull null

                val skills = TrendingSkills.select { TrendingSkills.region eq region }
                    .orderBy(TrendingSkills.demandScore to SortOrder.DESC)
                    .limit(5)
                    .map { it[TrendingSkills.skillName] }

                RegionInfo(
                    name = region,
                    lat = coords.first,
                    lng = coords.second,
                    skillCount = TrendingSkills.select { TrendingSkills.region eq region }.count().toInt(),
                    topSkills = skills
                )
            }
        }
    }
}

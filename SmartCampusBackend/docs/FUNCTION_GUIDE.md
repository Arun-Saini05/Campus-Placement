# SmartCampus Backend - Core Function Guide

This document provides a summary of the key functions within the SmartCampus backend services, specifically those responsible for trending skills, recommendations, and system initialization.

## 1. SkillService (`com.smartcampus.backend.service.SkillService`)
This service handles the core logic for skill analysis and region-based data.

| Function Name | Description |
| :--- | :--- |
| `getRecommendations(userId)` | Generates a list of recommended skills for a student based on their profile (semester, branch, preferred region) and trending data. |
| `getRegionRecommendations(region)` | Fetches the top 20 trending skills for a specific geographic region (e.g., Bangalore, Mumbai). |
| `getSkillGapAnalysis(userId)` | Compares a student's current skill set against market demands to generate a "readiness" percentage and a list of missing skills. |
| `getTrendingSkills()` | Fetches live trends from GitHub and merges them with a robust set of industry-standard fallback data. |
| `getRegionList()` | Provides a list of all supported regions with their GPS coordinates and top 5 trending skills for map visualization. |

## 2. GitHubTrendingService (`com.smartcampus.backend.service.GitHubTrendingService`)
An automated service that periodically syncs with the GitHub API to identify emerging tech trends.

| Function Name | Description |
| :--- | :--- |
| `fetchAndCacheTrending()` | Coordinates the fetching process, manages the 1-hour in-memory cache, and updates the database with fresh data. |
| `fetchFromGitHub()` | Performs live searches on GitHub (stars > 5000) to identify popular repositories and maps them to industry skills. |
| `formatSkillName(raw)` | Normalizes raw GitHub technology strings (e.g., "javascript" to "JavaScript"). |
| `saveToDB(skills)` | Clears old GitHub-sourced data and persists the newly fetched trends to the `TrendingSkills` table. |
| `getFromDB()` | Retrieves previously cached GitHub trends from the database if the API is currently unavailable. |

## 3. SeedData (`com.smartcampus.backend.util.SeedData`)
Responsible for ensuring the database has a healthy set of initial data on first run or after a reset.

| Function Name | Description |
| :--- | :--- |
| `seed()` | Entry point for system initialization. Ensures admin, officer, and sample student accounts exist. |
| `seedTrendingSkills(now)` | Populates the database with 30+ high-quality industry trends across 16 different regions to ensure the map looks rich. |
| `refreshTrendingSkills()` | Intelligently refreshes the trend list if it falls below a certain density, without wiping useful data. |

## 4. RoadmapService (`com.smartcampus.backend.service.RoadmapService`)
Manages the educational paths for specific technologies.

| Function Name | Description |
| :--- | :--- |
| `getRoadmap(skill)` | Retrieves a step-by-step learning roadmap for a specific technology (e.g., "Kotlin", "React"). |

## 5. RecommendationService (`com.smartcampus.backend.service.RecommendationService`)
Advanced logic for student-specific career advice.

| Function Name | Description |
| :--- | :--- |
| `getPersonalizedRecommendations(userId)` | Uses an advanced algorithm to suggest skills based on current market trends and the user's career progress. |

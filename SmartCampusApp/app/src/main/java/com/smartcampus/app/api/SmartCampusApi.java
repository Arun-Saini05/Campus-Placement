package com.smartcampus.app.api;

import com.smartcampus.app.models.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface SmartCampusApi {

        // ========== AUTH ==========
        @POST("auth/register")
        Call<User> register(@Body Map<String, String> body);

        @POST("auth/login")
        Call<User> login(@Body Map<String, String> body);

        // ========== STUDENT PROFILE ==========
        @GET("student/profile")
        Call<StudentProfile> getProfile(@Header("Authorization") String token);

        @PUT("student/profile")
        Call<JsonObject> updateProfile(@Header("Authorization") String token, @Body Map<String, Object> body);

        @POST("student/skills")
        Call<JsonObject> addSkill(@Header("Authorization") String token, @Body Map<String, String> body);

        @DELETE("student/skills/{skillId}")
        Call<JsonObject> removeSkill(@Header("Authorization") String token, @Path("skillId") int skillId);

        @POST("student/certifications")
        Call<JsonObject> addCertification(@Header("Authorization") String token, @Body Map<String, String> body);

        @POST("student/projects")
        Call<JsonObject> addProject(@Header("Authorization") String token, @Body Map<String, String> body);

        @POST("student/experience")
        Call<JsonObject> addExperience(@Header("Authorization") String token, @Body Map<String, String> body);

        // ========== SKILLS ==========
        @GET("skills/recommend/personalized")
        Call<List<SkillRecommendation>> getPersonalizedRecommendations(@Header("Authorization") String token);

        @GET("skills/recommend")
        Call<List<SkillRecommendation>> getSkillRecommendations(@Header("Authorization") String token);

        @GET("skills/recommend/region/{region}")
        Call<List<SkillRecommendation>> getRegionRecommendations(
                        @Header("Authorization") String token,
                        @Path("region") String region);

        @GET("skills/gap-analysis")
        Call<JsonObject> getSkillGapAnalysis(@Header("Authorization") String token);

        @GET("skills/trending")
        Call<List<SkillRecommendation>> getTrendingSkills(@Header("Authorization") String token);

        @GET("skills/regions")
        Call<JsonArray> getRegions(@Header("Authorization") String token);

        // ========== ROADMAPS ==========
        @GET("skills/roadmap/{skill}")
        Call<JsonObject> getRoadmap(@Header("Authorization") String token, @Path("skill") String skill);

        @GET("skills/roadmap/{skill}/progress")
        Call<JsonArray> getRoadmapProgress(@Header("Authorization") String token, @Path("skill") String skill);

        @POST("skills/roadmap/{skill}/progress")
        Call<JsonObject> saveRoadmapProgress(@Header("Authorization") String token, @Path("skill") String skill,
                        @Body JsonObject body);

        @GET("skills/completed-roadmaps")
        Call<List<String>> getCompletedRoadmaps(@Header("Authorization") String token);

        // ========== JOBS ==========
        @GET("jobs")
        Call<List<Job>> getJobs(
                        @Header("Authorization") String token,
                        @Query("skill") String skill,
                        @Query("location") String location,
                        @Query("jobType") String jobType);

        //
        @GET("jobs/external")
        Call<List<ExternalJob>> getExternalJobs(
                        @Header("Authorization") String token,
                        @Query("query") String query,
                        @Query("num_pages") String numPages);

        @GET("jobs/{jobId}")
        Call<Job> getJobById(@Header("Authorization") String token, @Path("jobId") int jobId);

        @POST("jobs/{jobId}/apply")
        Call<JsonObject> applyForJob(
                        @Header("Authorization") String token,
                        @Path("jobId") int jobId,
                        @Query("resumeUrl") String resumeUrl);

        @GET("jobs/applications/me")
        Call<List<JsonObject>> getMyApplications(@Header("Authorization") String token);

        // ========== RESUME ==========
        @POST("resume")
        Call<JsonObject> saveResumeData(@Header("Authorization") String token, @Body Map<String, String> body);

        @GET("resume")
        Call<JsonObject> getResumeData(@Header("Authorization") String token);

        @POST("resume/ats-score")
        Call<JsonObject> getAtsScore(@Header("Authorization") String token, @Body Map<String, String> body);

        // ========== CAREER ==========
        @GET("career/roadmap")
        Call<List<JsonObject>> getCareerRoadmap(@Header("Authorization") String token);

        @GET("career/progress")
        Call<JsonObject> getProgress(@Header("Authorization") String token);

        @GET("career/analytics")
        Call<JsonObject> getAnalytics(@Header("Authorization") String token);

        // ========== NOTIFICATIONS ==========
        @GET("notifications")
        Call<List<JsonObject>> getNotifications(@Header("Authorization") String token);

        @GET("notifications/unread-count")
        Call<JsonObject> getUnreadCount(@Header("Authorization") String token);

        @PUT("notifications/{notificationId}/read")
        Call<JsonObject> markAsRead(@Header("Authorization") String token, @Path("notificationId") int id);

        @PUT("notifications/read-all")
        Call<JsonObject> markAllAsRead(@Header("Authorization") String token);

        // ========== OFFICER ==========
        @POST("officer/drives")
        Call<JsonObject> createDrive(@Header("Authorization") String token, @Body Map<String, Object> body);

        @GET("officer/drives")
        Call<List<JsonObject>> getDrives(@Header("Authorization") String token);

        @GET("officer/drives/{driveId}/eligible")
        Call<List<JsonObject>> getEligibleStudents(@Header("Authorization") String token, @Path("driveId") int driveId);

        @GET("officer/stats")
        Call<JsonObject> getOfficerStats(@Header("Authorization") String token);

        @GET("officer/students")
        Call<List<JsonObject>> searchStudents(
                @Header("Authorization") String token,
                @Query("branch") String branch,
                @Query("minCgpa") Float minCgpa,
                @Query("status") String status
        );

        @POST("officer/notifications/mass")
        Call<JsonObject> sendMassNotification(@Header("Authorization") String token, @Body Map<String, Object> body);

        @DELETE("officer/drives/{driveId}")
        Call<JsonObject> deleteDrive(@Header("Authorization") String token, @Path("driveId") int driveId);

        @PUT("officer/students/{studentId}/status")
        Call<JsonObject> updateStudentStatus(@Header("Authorization") String token, @Path("studentId") int studentId, @Body Map<String, String> body);

        @PUT("student/placement-status")
        Call<JsonObject> updatePlacementStatus(@Header("Authorization") String token, @Body Map<String, String> body);

        // ========== RECRUITER ==========
        @POST("recruiter/search")
        Call<List<JsonObject>> searchCandidates(@Header("Authorization") String token, @Body Map<String, Object> body);

        @GET("recruiter/resume/{studentId}")
        Call<JsonObject> viewStudentResume(@Header("Authorization") String token, @Path("studentId") int studentId);

        @GET("recruiter/analytics")
        Call<JsonObject> getRecruiterAnalytics(@Header("Authorization") String token);

        @GET("recruiter/profile")
        Call<JsonObject> getRecruiterProfile(@Header("Authorization") String token);

        @PUT("recruiter/profile")
        Call<JsonObject> updateRecruiterProfile(@Header("Authorization") String token, @Body Map<String, Object> body);

        @POST("recruiter/interview")
        Call<JsonObject> scheduleInterview(@Header("Authorization") String token, @Body Map<String, Object> body);

        // ========== ADMIN ==========
        @GET("admin/users")
        Call<List<JsonObject>> getAllUsers(@Header("Authorization") String token);

        @GET("admin/pending-recruiters")
        Call<List<JsonObject>> getPendingRecruiters(@Header("Authorization") String token);

        @PUT("admin/approve/{recruiterId}")
        Call<JsonObject> approveRecruiter(@Header("Authorization") String token, @Path("recruiterId") int recruiterId);

        @PUT("admin/users/{userId}/toggle")
        Call<JsonObject> toggleUserStatus(
                        @Header("Authorization") String token,
                        @Path("userId") int userId,
                        @Query("activate") boolean activate);

        @PUT("admin/users/{userId}")
        Call<JsonObject> updateUser(
                        @Header("Authorization") String token,
                        @Path("userId") int userId,
                        @Body Map<String, String> body);

        @GET("admin/stats")
        Call<JsonObject> getSystemStats(@Header("Authorization") String token);

        @GET("admin/applications")
        Call<JsonArray> getAllApplications(@Header("Authorization") String token);

        // --- Admin Management ---
        @GET("admin/colleges")
        Call<JsonArray> getAllColleges(@Header("Authorization") String token);

        @POST("admin/colleges")
        Call<JsonObject> addCollege(@Header("Authorization") String token, @Body Map<String, String> body);

        @PUT("admin/colleges/{id}")
        Call<JsonObject> updateCollege(@Header("Authorization") String token, @Path("id") int id, @Body Map<String, String> body);

        @DELETE("admin/colleges/{id}")
        Call<JsonObject> deleteCollege(@Header("Authorization") String token, @Path("id") int id);

        @GET("admin/companies")
        Call<JsonArray> getAllCompanies(@Header("Authorization") String token);

        @POST("admin/companies")
        Call<JsonObject> addCompany(@Header("Authorization") String token, @Body Map<String, String> body);

        @PUT("admin/companies/{id}")
        Call<JsonObject> updateCompany(@Header("Authorization") String token, @Path("id") int id, @Body Map<String, String> body);

        @DELETE("admin/companies/{id}")
        Call<JsonObject> deleteCompany(@Header("Authorization") String token, @Path("id") int id);

        @POST("admin/jobs")
        Call<JsonObject> addAdminJob(@Header("Authorization") String token, @Body Map<String, String> body);

        @PUT("admin/jobs/{id}")
        Call<JsonObject> updateAdminJob(@Header("Authorization") String token, @Path("id") int id, @Body Map<String, String> body);

        @DELETE("admin/jobs/{id}")
        Call<JsonObject> deleteAdminJob(@Header("Authorization") String token, @Path("id") int id);

        @PUT("admin/applications/{id}")
        Call<JsonObject> updateAdminApplication(@Header("Authorization") String token, @Path("id") int id, @Body Map<String, String> body);

        @DELETE("admin/applications/{id}")
        Call<JsonObject> deleteAdminApplication(@Header("Authorization") String token, @Path("id") int id);

        // --- Recruiter Job Posting & Direct Interview ---
        @POST("jobs")
        Call<JsonObject> postJob(@Header("Authorization") String token, @Body Map<String, Object> body);

        @POST("recruiter/schedule-interview-direct")
        Call<JsonObject> scheduleInterviewDirect(@Header("Authorization") String token, @Body Map<String, String> body);

        @POST("recruiter/select-candidate-direct")
        Call<JsonObject> selectCandidateDirect(@Header("Authorization") String token, @Body Map<String, String> body);
}

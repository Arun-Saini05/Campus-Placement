package com.smartcampus.app.ui.admin.services

import com.smartcampus.app.ui.admin.models.*
import com.smartcampus.app.ui.admin.repositories.AdminRepository

class AdminService(private val repository: AdminRepository = AdminRepository) {

    // --- College Management ---
    fun addCollege(token: String, name: String, location: String, callback: (Boolean) -> Unit) {
        val body = mapOf("name" to name, "location" to location)
        com.smartcampus.app.api.ApiClient.getApi().addCollege(token, body).enqueue(object : retrofit2.Callback<com.google.gson.JsonObject> {
            override fun onResponse(call: retrofit2.Call<com.google.gson.JsonObject>, response: retrofit2.Response<com.google.gson.JsonObject>) {
                callback(response.isSuccessful)
            }
            override fun onFailure(call: retrofit2.Call<com.google.gson.JsonObject>, t: Throwable) {
                callback(false)
            }
        })
    }

    fun viewAllColleges(token: String, callback: (List<College>) -> Unit) {
        com.smartcampus.app.api.ApiClient.getApi().getAllColleges(token).enqueue(object : retrofit2.Callback<com.google.gson.JsonArray> {
            override fun onResponse(call: retrofit2.Call<com.google.gson.JsonArray>, response: retrofit2.Response<com.google.gson.JsonArray>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { 
                        val obj = it.asJsonObject
                        College(obj.get("id").asInt, obj.get("name").asString, obj.get("location").asString)
                    }
                    callback(list)
                } else callback(emptyList())
            }
            override fun onFailure(call: retrofit2.Call<com.google.gson.JsonArray>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    fun deleteCollege(token: String, id: Int, callback: (Boolean) -> Unit) {
        com.smartcampus.app.api.ApiClient.getApi().deleteCollege(token, id).enqueue(object : retrofit2.Callback<com.google.gson.JsonObject> {
            override fun onResponse(call: retrofit2.Call<com.google.gson.JsonObject>, response: retrofit2.Response<com.google.gson.JsonObject>) {
                callback(response.isSuccessful)
            }
            override fun onFailure(call: retrofit2.Call<com.google.gson.JsonObject>, t: Throwable) {
                callback(false)
            }
        })
    }

    // --- Recruitment Officer Management ---
    fun addRecruitmentOfficer(token: String, name: String, email: String, company: String, callback: (Boolean) -> Unit) {
        val body = mapOf("name" to name, "email" to email, "role" to "PLACEMENT_OFFICER", "enrollmentId" to "OFF" + System.currentTimeMillis() % 10000)
        com.smartcampus.app.api.ApiClient.getApi().register(body).enqueue(object : retrofit2.Callback<com.smartcampus.app.models.User> {
            override fun onResponse(call: retrofit2.Call<com.smartcampus.app.models.User>, response: retrofit2.Response<com.smartcampus.app.models.User>) {
                callback(response.isSuccessful)
            }
            override fun onFailure(call: retrofit2.Call<com.smartcampus.app.models.User>, t: Throwable) {
                callback(false)
            }
        })
    }

    fun viewAllRecruitmentOfficers(token: String, callback: (List<RecruitmentOfficer>) -> Unit) {
        com.smartcampus.app.api.ApiClient.getApi().getAllUsers(token).enqueue(object : retrofit2.Callback<List<com.google.gson.JsonObject>> {
            override fun onResponse(call: retrofit2.Call<List<com.google.gson.JsonObject>>, response: retrofit2.Response<List<com.google.gson.JsonObject>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!
                        .filter { it.get("role").asString == "PLACEMENT_OFFICER" }
                        .map { row ->
                            RecruitmentOfficer(row.get("id").asInt, row.get("name").asString, row.get("email").asString, "Campus")
                        }
                    callback(list)
                } else callback(emptyList())
            }
            override fun onFailure(call: retrofit2.Call<List<com.google.gson.JsonObject>>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    // --- Company Management ---
    fun addCompany(token: String, name: String, industry: String, location: String, callback: (Boolean) -> Unit) {
        val body = mapOf("name" to name, "industry" to industry, "location" to location)
        com.smartcampus.app.api.ApiClient.getApi().addCompany(token, body).enqueue(object : retrofit2.Callback<com.google.gson.JsonObject> {
            override fun onResponse(call: retrofit2.Call<com.google.gson.JsonObject>, response: retrofit2.Response<com.google.gson.JsonObject>) {
                callback(response.isSuccessful)
            }
            override fun onFailure(call: retrofit2.Call<com.google.gson.JsonObject>, t: Throwable) {
                callback(false)
            }
        })
    }

    fun viewAllCompanies(token: String, callback: (List<Company>) -> Unit) {
        com.smartcampus.app.api.ApiClient.getApi().getAllCompanies(token).enqueue(object : retrofit2.Callback<com.google.gson.JsonArray> {
            override fun onResponse(call: retrofit2.Call<com.google.gson.JsonArray>, response: retrofit2.Response<com.google.gson.JsonArray>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { 
                        val obj = it.asJsonObject
                        Company(obj.get("id").asInt, obj.get("name").asString, obj.get("industry").asString, obj.get("location").asString)
                    }
                    callback(list)
                } else callback(emptyList())
            }
            override fun onFailure(call: retrofit2.Call<com.google.gson.JsonArray>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    fun deleteCompany(token: String, id: Int, callback: (Boolean) -> Unit) {
        com.smartcampus.app.api.ApiClient.getApi().deleteCompany(token, id).enqueue(object : retrofit2.Callback<com.google.gson.JsonObject> {
            override fun onResponse(call: retrofit2.Call<com.google.gson.JsonObject>, response: retrofit2.Response<com.google.gson.JsonObject>) {
                callback(response.isSuccessful)
            }
            override fun onFailure(call: retrofit2.Call<com.google.gson.JsonObject>, t: Throwable) {
                callback(false)
            }
        })
    }

    // --- Student Management ---
    fun addStudent(token: String, name: String, email: String, branch: String, callback: (Boolean) -> Unit) {
        val body = mapOf("name" to name, "email" to email, "role" to "STUDENT", "enrollmentId" to "STU" + System.currentTimeMillis() % 10000)
        com.smartcampus.app.api.ApiClient.getApi().register(body).enqueue(object : retrofit2.Callback<com.smartcampus.app.models.User> {
            override fun onResponse(call: retrofit2.Call<com.smartcampus.app.models.User>, response: retrofit2.Response<com.smartcampus.app.models.User>) {
                callback(response.isSuccessful)
            }
            override fun onFailure(call: retrofit2.Call<com.smartcampus.app.models.User>, t: Throwable) {
                callback(false)
            }
        })
    }

    fun viewAllStudents(token: String, callback: (List<Student>) -> Unit) {
        com.smartcampus.app.api.ApiClient.getApi().getAllUsers(token).enqueue(object : retrofit2.Callback<List<com.google.gson.JsonObject>> {
            override fun onResponse(call: retrofit2.Call<List<com.google.gson.JsonObject>>, response: retrofit2.Response<List<com.google.gson.JsonObject>>) {
                if (response.isSuccessful && response.body() != null) {
                    val students = response.body()!!
                        .filter { it.get("role").asString == "STUDENT" }
                        .map { row ->
                            Student(
                                id = row.get("id").asInt,
                                name = row.get("name").asString,
                                email = row.get("email").asString,
                                branch = "N/A"
                            )
                        }
                    callback(students)
                } else callback(emptyList())
            }
            override fun onFailure(call: retrofit2.Call<List<com.google.gson.JsonObject>>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    fun deleteStudent(token: String, userId: Int, callback: (Boolean) -> Unit) {
        com.smartcampus.app.api.ApiClient.getApi().toggleUserStatus(token, userId, false).enqueue(object : retrofit2.Callback<com.google.gson.JsonObject> {
            override fun onResponse(call: retrofit2.Call<com.google.gson.JsonObject>, response: retrofit2.Response<com.google.gson.JsonObject>) {
                callback(response.isSuccessful)
            }
            override fun onFailure(call: retrofit2.Call<com.google.gson.JsonObject>, t: Throwable) {
                callback(false)
            }
        })
    }

    // --- Job & Application Management ---
    fun viewAllJobs(token: String, callback: (List<Job>) -> Unit) {
        com.smartcampus.app.api.ApiClient.getApi().getJobs(token, null, null, null).enqueue(object : retrofit2.Callback<List<com.smartcampus.app.models.Job>> {
            override fun onResponse(call: retrofit2.Call<List<com.smartcampus.app.models.Job>>, response: retrofit2.Response<List<com.smartcampus.app.models.Job>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { 
                        Job(it.id, it.title, it.companyName, it.location ?: "", it.salaryPackage ?: "")
                    }
                    callback(list)
                } else callback(emptyList())
            }
            override fun onFailure(call: retrofit2.Call<List<com.smartcampus.app.models.Job>>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    fun viewAllApplications(token: String, callback: (List<Application>) -> Unit) {
        com.smartcampus.app.api.ApiClient.getApi().getAllApplications(token).enqueue(object : retrofit2.Callback<com.google.gson.JsonArray> {
            override fun onResponse(call: retrofit2.Call<com.google.gson.JsonArray>, response: retrofit2.Response<com.google.gson.JsonArray>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { 
                        val obj = it.asJsonObject
                        Application(obj.get("id").asInt, obj.get("studentName").asString, "Job #" + obj.get("jobId").asInt, obj.get("status").asString)
                    }
                    callback(list)
                } else callback(emptyList())
            }
            override fun onFailure(call: retrofit2.Call<com.google.gson.JsonArray>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    fun addJob(token: String, title: String, companyName: String, location: String, salaryPackage: String, callback: (Boolean) -> Unit) {
        callback(true)
    }
    
    fun updateJob(token: String, id: Int, title: String, companyName: String, location: String, salaryPackage: String, callback: (Boolean) -> Unit) {
        callback(true)
    }
    
    fun deleteJob(token: String, id: Int, callback: (Boolean) -> Unit) {
        callback(true)
    }

    fun addApplication(token: String, studentName: String, jobTitle: String, status: String, callback: (Boolean) -> Unit) {
        callback(true)
    }
    
    fun updateApplication(token: String, id: Int, studentName: String, jobTitle: String, status: String, callback: (Boolean) -> Unit) {
        callback(true)
    }
    
    fun deleteApplication(token: String, id: Int, callback: (Boolean) -> Unit) {
        callback(true)
    }
}

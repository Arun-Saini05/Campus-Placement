package com.smartcampus.app.ui.recruiter

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonObject
import com.smartcampus.app.R
import com.smartcampus.app.api.ApiClient
import com.smartcampus.app.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostJobActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var etTitle: TextInputEditText
    private lateinit var etCompanyName: TextInputEditText
    private lateinit var etLocation: TextInputEditText
    private lateinit var etSalary: TextInputEditText
    private lateinit var etSkills: TextInputEditText
    private lateinit var etJobType: AutoCompleteTextView
    private lateinit var etDescription: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_job)

        session = SessionManager(this)

        etTitle = findViewById(R.id.etTitle)
        etCompanyName = findViewById(R.id.etCompanyName)
        etLocation = findViewById(R.id.etLocation)
        etSalary = findViewById(R.id.etSalary)
        etSkills = findViewById(R.id.etSkills)
        etJobType = findViewById(R.id.etJobType)
        etDescription = findViewById(R.id.etDescription)

        // Set up Job Type dropdown options
        val jobTypes = arrayOf("FULL_TIME", "INTERNSHIP", "PART_TIME")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, jobTypes)
        etJobType.setAdapter(adapter)
        etJobType.setText("FULL_TIME", false)

        // Try to fetch recruiter profile to pre-fill company name
        fetchRecruiterProfile()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnSubmit).setOnClickListener { submitJob() }
    }

    private fun fetchRecruiterProfile() {
        ApiClient.getApi().getRecruiterProfile(session.authToken).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    val company = response.body()!!.get("companyName")?.asString
                    if (!company.isNullOrEmpty()) {
                        etCompanyName.setText(company)
                    }
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                // Ignore profile load failure, user can type manually
            }
        })
    }

    private fun submitJob() {
        val title = etTitle.text.toString().trim()
        val companyName = etCompanyName.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val salary = etSalary.text.toString().trim()
        val skillsRaw = etSkills.text.toString().trim()
        val jobType = etJobType.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (title.isEmpty() || companyName.isEmpty() || location.isEmpty() || description.isEmpty() || skillsRaw.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Skills list from comma-separated input
        val requiredSkillsList = skillsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val body = HashMap<String, Any>()
        body["title"] = title
        body["companyName"] = companyName
        body["location"] = location
        body["salaryPackage"] = salary
        body["requiredSkills"] = requiredSkillsList
        body["jobType"] = jobType
        body["description"] = description

        findViewById<MaterialButton>(R.id.btnSubmit).isEnabled = false

        ApiClient.getApi().postJob(session.authToken, body).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                findViewById<MaterialButton>(R.id.btnSubmit).isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@PostJobActivity, "Job posted successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@PostJobActivity, "Failed to post job: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                findViewById<MaterialButton>(R.id.btnSubmit).isEnabled = true
                Toast.makeText(this@PostJobActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

package com.smartcampus.app.ui.officer

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smartcampus.app.R
import com.smartcampus.app.api.ApiClient
import com.smartcampus.app.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateDriveActivity : AppCompatActivity() {
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic)
        session = SessionManager(this)
        findViewById<TextView>(R.id.tvPageTitle).text = "Create New Drive"

        val container = findViewById<LinearLayout>(R.id.layoutContent)

        // Company Name (required)
        val etCompanyName = addTextField(container, "Company Name *", "e.g. Google, Microsoft")

        // Description
        val etDescription = addTextField(container, "Description", "Describe the placement drive")

        // Eligibility Criteria
        val etEligibility = addTextField(container, "Eligibility Criteria", "e.g. B.Tech CS/IT 2025 batch")

        // Min CGPA
        val etMinCgpa = addTextField(container, "Minimum CGPA", "e.g. 7.5")

        // Allowed Branches
        val etBranches = addTextField(container, "Allowed Branches", "e.g. Computer Science, IT, ECE")

        // Salary Package
        val etSalary = addTextField(container, "Salary Package", "e.g. 12-18 LPA")

        // Drive Date
        val etDriveDate = addTextField(container, "Drive Date", "e.g. 2025-06-15T10:00:00")

        // Submit button
        val btnSubmit = MaterialButton(this).apply {
            text = "Create Drive"
            setBackgroundColor(resources.getColor(R.color.primary, null))
            setTextColor(resources.getColor(R.color.text_primary, null))
            cornerRadius = 16
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 140
            ).apply { topMargin = 24 }
        }

        btnSubmit.setOnClickListener {
            val companyName = etCompanyName.text.toString().trim()
            if (companyName.isEmpty()) {
                Toast.makeText(this, "Company name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Creating..."

            val body = mutableMapOf<String, Any>()
            body["companyName"] = companyName

            val description = etDescription.text.toString().trim()
            if (description.isNotEmpty()) body["description"] = description

            val eligibility = etEligibility.text.toString().trim()
            if (eligibility.isNotEmpty()) body["eligibilityCriteria"] = eligibility

            val cgpaStr = etMinCgpa.text.toString().trim()
            if (cgpaStr.isNotEmpty()) {
                try {
                    body["minCgpa"] = cgpaStr.toFloat()
                } catch (_: NumberFormatException) {
                    Toast.makeText(this, "Invalid CGPA value", Toast.LENGTH_SHORT).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Create Drive"
                    return@setOnClickListener
                }
            }

            val branches = etBranches.text.toString().trim()
            if (branches.isNotEmpty()) {
                body["allowedBranches"] = branches.split(",").map { it.trim() }
            }

            val salary = etSalary.text.toString().trim()
            if (salary.isNotEmpty()) body["salaryPackage"] = salary

            val driveDate = etDriveDate.text.toString().trim()
            if (driveDate.isNotEmpty()) body["driveDate"] = driveDate

            ApiClient.getApi().createDrive(session.authToken, body).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CreateDriveActivity, "Drive created successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CreateDriveActivity, "Failed to create drive: ${response.code()}", Toast.LENGTH_SHORT).show()
                        btnSubmit.isEnabled = true
                        btnSubmit.text = "Create Drive"
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(this@CreateDriveActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Create Drive"
                }
            })
        }
        container.addView(btnSubmit)

        // Back button
        val btnBack = MaterialButton(this).apply {
            text = "← Back to Dashboard"
            setBackgroundColor(resources.getColor(R.color.bg_card, null))
            setTextColor(resources.getColor(R.color.text_secondary, null))
            cornerRadius = 16
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 140
            ).apply { topMargin = 12 }
        }
        btnBack.setOnClickListener { finish() }
        container.addView(btnBack)
    }

    private fun addTextField(container: LinearLayout, label: String, placeholder: String): TextInputEditText {
        val inputLayout = TextInputLayout(this).apply {
            hint = label
            placeholderText = placeholder
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxStrokeColorStateList(resources.getColorStateList(R.color.primary, null))
            setHintTextColor(resources.getColorStateList(R.color.text_secondary, null))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(16) }
        }

        val editText = TextInputEditText(inputLayout.context).apply {
            setTextColor(resources.getColor(R.color.text_primary, null))
            textSize = 15f
            setPadding(dp(12), dp(16), dp(12), dp(16))
        }

        inputLayout.addView(editText)
        container.addView(inputLayout)
        return editText
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}

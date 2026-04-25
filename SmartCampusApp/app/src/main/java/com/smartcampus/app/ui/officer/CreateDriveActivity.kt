package com.smartcampus.app.ui.officer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import java.text.SimpleDateFormat
import java.util.*

class CreateDriveActivity : AppCompatActivity() {
    private lateinit var session: SessionManager
    private val calendar = Calendar.getInstance()
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic)
        session = SessionManager(this)
        findViewById<TextView>(R.id.tvPageTitle).text = "Create Recruitment Drive"

        val container = findViewById<LinearLayout>(R.id.layoutContent)

        // Company Name (required)
        val etCompanyName = addTextField(container, "Company Name *", "e.g. Google, Microsoft")

        // Description
        val etDescription = addTextField(container, "Job Description", "Describe roles and responsibilities")

        // Eligibility Criteria
        val etEligibility = addTextField(container, "Eligibility Criteria", "e.g. B.Tech CS/IT 2025 batch")

        // Min CGPA
        val etMinCgpa = addTextField(container, "Minimum CGPA", "e.g. 7.5")

        // Allowed Branches
        val etBranches = addTextField(container, "Allowed Branches", "e.g. Computer Science, IT, ECE")

        // Salary Package
        val etSalary = addTextField(container, "Salary Package (LPA)", "e.g. 12-18")

        // Drive Date (Picker)
        val etDriveDate = addTextField(container, "Drive Date & Time *", "Select schedule")
        etDriveDate.isFocusable = false
        etDriveDate.isFocusableInTouchMode = false
        etDriveDate.isCursorVisible = false
        etDriveDate.setOnClickListener { showDateTimePicker(etDriveDate) }

        // Submit button
        val btnSubmit = MaterialButton(this).apply {
            text = "Broadcast Drive"
            setBackgroundColor(resources.getColor(R.color.success, null))
            setTextColor(resources.getColor(R.color.white, null))
            cornerRadius = dp(24)
            isAllCaps = false
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56)
            ).apply { topMargin = dp(32) }
        }

        btnSubmit.setOnClickListener {
            val companyName = etCompanyName.text.toString().trim()
            val dateStr = etDriveDate.tag?.toString() ?: ""

            if (companyName.isEmpty()) {
                etCompanyName.error = "Company name is required"
                return@setOnClickListener
            }
            if (dateStr.isEmpty()) {
                etDriveDate.error = "Please select a drive date"
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Broadcasting..."

            val body = mutableMapOf<String, Any>()
            body["companyName"] = companyName
            body["driveDate"] = dateStr

            val description = etDescription.text.toString().trim()
            if (description.isNotEmpty()) body["description"] = description

            val eligibility = etEligibility.text.toString().trim()
            if (eligibility.isNotEmpty()) body["eligibilityCriteria"] = eligibility

            val cgpaStr = etMinCgpa.text.toString().trim()
            if (cgpaStr.isNotEmpty()) {
                try {
                    val cgpa = cgpaStr.toFloat()
                    if (cgpa < 0 || cgpa > 10) throw NumberFormatException()
                    body["minCgpa"] = cgpa
                } catch (_: NumberFormatException) {
                    Toast.makeText(this, "Invalid CGPA (0-10)", Toast.LENGTH_SHORT).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Broadcast Drive"
                    return@setOnClickListener
                }
            }

            val branches = etBranches.text.toString().trim()
            if (branches.isNotEmpty()) {
                body["allowedBranches"] = branches.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }

            val salary = etSalary.text.toString().trim()
            if (salary.isNotEmpty()) body["salaryPackage"] = salary

            ApiClient.getApi().createDrive(session.authToken, body).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CreateDriveActivity, "Placement drive created!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CreateDriveActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        btnSubmit.isEnabled = true
                        btnSubmit.text = "Broadcast Drive"
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(this@CreateDriveActivity, "Network error", Toast.LENGTH_SHORT).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Broadcast Drive"
                }
            })
        }
        container.addView(btnSubmit)

        // Cancel button
        val btnCancel = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "Cancel"
            setTextColor(resources.getColor(R.color.text_secondary, null))
            strokeColor = android.content.res.ColorStateList.valueOf(resources.getColor(R.color.divider, null))
            cornerRadius = dp(24)
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(50)
            ).apply { topMargin = dp(16); bottomMargin = dp(40) }
        }
        btnCancel.setOnClickListener { finish() }
        container.addView(btnCancel)
    }

    private fun showDateTimePicker(editText: TextInputEditText) {
        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                editText.setText(displayFormat.format(calendar.time))
                editText.tag = isoFormat.format(calendar.time)
                editText.error = null
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun addTextField(container: LinearLayout, label: String, placeholder: String): TextInputEditText {
        val inputLayout = TextInputLayout(this).apply {
            hint = label
            placeholderText = placeholder
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxStrokeColorStateList(resources.getColorStateList(R.color.primary, null))
            setHintTextColor(resources.getColorStateList(R.color.text_secondary, null))
            setBoxCornerRadii(
                dp(12).toFloat(), dp(12).toFloat(),
                dp(12).toFloat(), dp(12).toFloat()
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(16) }
        }

        val editText = TextInputEditText(inputLayout.context).apply {
            setTextColor(resources.getColor(R.color.text_primary, null))
            textSize = 15f
            setPadding(dp(12), dp(16), dp(12), dp(16))
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        inputLayout.addView(editText)
        container.addView(inputLayout)
        return editText
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}

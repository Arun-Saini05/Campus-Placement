package com.smartcampus.app.ui.student

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smartcampus.app.R
import com.smartcampus.app.api.ApiClient
import com.smartcampus.app.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var etSemester: TextInputEditText
    private lateinit var etBranch: AutoCompleteTextView
    private lateinit var etCgpa: TextInputEditText
    private lateinit var etRegion: TextInputEditText
    private lateinit var etAbout: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        session = SessionManager(this)

        etSemester = findViewById(R.id.etSemester)
        etBranch = findViewById(R.id.etBranch)
        etCgpa = findViewById(R.id.etCgpa)
        etRegion = findViewById(R.id.etRegion)
        etAbout = findViewById(R.id.etAbout)

        // Set up Branch dropdown options
        val departments = arrayOf(
            "Computer Science",
            "Information Tech",
            "ECE",
            "Electrical",
            "Mechanical",
            "Civil"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, departments)
        etBranch.setAdapter(adapter)

        // Pre-fill from intent extras
        intent.extras?.let { extras ->
            extras.getInt("semester", 0).let { if (it > 0) etSemester.setText(it.toString()) }
            extras.getString("branch")?.let { etBranch.setText(it, false) }
            extras.getFloat("cgpa", 0f).let { if (it > 0f) etCgpa.setText(it.toString()) }
            extras.getString("region")?.let { etRegion.setText(it) }
            extras.getString("about")?.let { etAbout.setText(it) }
        }

        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener { saveProfile() }
    }

    private fun saveProfile() {
        val body = HashMap<String, Any>()

        val semesterText = etSemester.text.toString().trim()
        if (semesterText.isNotEmpty()) {
            body["semester"] = semesterText.toIntOrNull() ?: 0
        }

        val branchText = etBranch.text.toString().trim()
        if (branchText.isNotEmpty()) body["branch"] = branchText

        val cgpaText = etCgpa.text.toString().trim()
        if (cgpaText.isNotEmpty()) {
            body["cgpa"] = cgpaText.toFloatOrNull() ?: 0f
        }

        val regionText = etRegion.text.toString().trim()
        if (regionText.isNotEmpty()) body["preferredRegion"] = regionText

        val aboutText = etAbout.text.toString().trim()
        if (aboutText.isNotEmpty()) body["about"] = aboutText

        if (body.isEmpty()) {
            Toast.makeText(this, "Nothing to update", Toast.LENGTH_SHORT).show()
            return
        }

        findViewById<MaterialButton>(R.id.btnSave).isEnabled = false

        ApiClient.getApi().updateProfile(session.authToken, body)
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    findViewById<MaterialButton>(R.id.btnSave).isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    findViewById<MaterialButton>(R.id.btnSave).isEnabled = true
                    Toast.makeText(this@EditProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

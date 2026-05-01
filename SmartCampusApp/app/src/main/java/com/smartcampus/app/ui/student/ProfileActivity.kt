package com.smartcampus.app.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.smartcampus.app.R
import com.smartcampus.app.api.ApiClient
import com.smartcampus.app.models.StudentProfile
import com.smartcampus.app.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private var currentProfile: StudentProfile? = null

    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadProfile() // Refresh after edit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        session = SessionManager(this)

        // Edit Profile button
        findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            currentProfile?.let { p ->
                intent.putExtra("semester", p.semester ?: 0)
                intent.putExtra("branch", p.branch ?: "")
                intent.putExtra("cgpa", p.cgpa ?: 0f)
                intent.putExtra("region", p.preferredRegion ?: "")
                intent.putExtra("about", p.about ?: "")
            }
            editProfileLauncher.launch(intent)
        }

        // Add buttons
        findViewById<MaterialButton>(R.id.btnAddSkill).setOnClickListener { showAddSkillDialog() }
        findViewById<MaterialButton>(R.id.btnAddCert).setOnClickListener { showAddCertDialog() }
        findViewById<MaterialButton>(R.id.btnAddProject).setOnClickListener { showAddProjectDialog() }
        
        findViewById<MaterialButton>(R.id.btnToggleStatus).setOnClickListener { togglePlacementStatus() }

        loadProfile()
    }

    private fun togglePlacementStatus() {
        val currentStatus = currentProfile?.placementStatus ?: "UNPLACED"
        val newStatus = if (currentStatus == "PLACED") "UNPLACED" else "PLACED"
        
        val body = HashMap<String, String>()
        body["status"] = newStatus
        
        ApiClient.getApi().updatePlacementStatus(session.authToken, body)
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                        loadProfile()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadProfile() {
        ApiClient.getApi().getProfile(session.authToken).enqueue(object : Callback<StudentProfile> {
            override fun onResponse(call: Call<StudentProfile>, response: Response<StudentProfile>) {
                if (response.isSuccessful && response.body() != null) {
                    currentProfile = response.body()!!
                    displayProfile(currentProfile!!)
                } else {
                    Toast.makeText(this@ProfileActivity, "Could not load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StudentProfile>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayProfile(profile: StudentProfile) {
        findViewById<TextView>(R.id.tvName).text = profile.name ?: session.userName
        findViewById<TextView>(R.id.tvEmail).text = profile.email ?: session.userEmail
        findViewById<TextView>(R.id.tvBranch).text = profile.branch ?: "Not set"
        findViewById<TextView>(R.id.tvSemester).text = "Semester ${profile.semester ?: "-"}"
        findViewById<TextView>(R.id.tvCgpa).text = "CGPA: ${profile.cgpa ?: "-"}"
        findViewById<TextView>(R.id.tvRegion).text = profile.preferredRegion ?: "Not set"
        findViewById<TextView>(R.id.tvAbout).text = profile.about ?: "No about info"

        val statusTv = findViewById<TextView>(R.id.tvPlacementStatus)
        val toggleBtn = findViewById<MaterialButton>(R.id.btnToggleStatus)
        val status = profile.placementStatus ?: "UNPLACED"
        
        statusTv.text = status
        if (status == "PLACED") {
            statusTv.setTextColor(resources.getColor(R.color.accent, null))
            toggleBtn.text = "Mark as Unplaced"
        } else {
            statusTv.setTextColor(resources.getColor(R.color.error, null))
            toggleBtn.text = "Mark as Placed"
        }

        // Skills chips — closeable for deletion
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupSkills)
        chipGroup.removeAllViews()
        if (profile.skills.isNullOrEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No skills added yet"
                setTextColor(resources.getColor(R.color.text_tertiary, null))
                textSize = 13f
            }
            chipGroup.addView(emptyTv)
        } else {
            profile.skills?.forEach { skill ->
                val chip = Chip(this).apply {
                    text = "${skill.name} (${skill.proficiencyLevel})"
                    isCloseIconVisible = true
                    setChipBackgroundColorResource(R.color.chip_bg)
                    setTextColor(resources.getColor(R.color.chip_text, null))
                    setCloseIconTintResource(R.color.error)
                    setOnCloseIconClickListener {
                        confirmDeleteSkill(skill.id, skill.name)
                    }
                }
                chipGroup.addView(chip)
            }
        }

        // Certifications
        val certLayout = findViewById<LinearLayout>(R.id.layoutCertifications)
        certLayout.removeAllViews()
        if (profile.certifications.isNullOrEmpty()) {
            certLayout.addView(createEmptyText("No certifications added yet"))
        } else {
            profile.certifications?.forEach { cert ->
                val tv = TextView(this).apply {
                    text = "📜 ${cert.name} — ${cert.issuingOrganization ?: "N/A"}"
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    setPadding(0, 8, 0, 8)
                    textSize = 14f
                }
                certLayout.addView(tv)
            }
        }

        // Projects
        val projLayout = findViewById<LinearLayout>(R.id.layoutProjects)
        projLayout.removeAllViews()
        if (profile.projects.isNullOrEmpty()) {
            projLayout.addView(createEmptyText("No projects added yet"))
        } else {
            profile.projects?.forEach { proj ->
                val tv = TextView(this).apply {
                    text = "🔧 ${proj.title} — ${proj.techStack ?: "N/A"}"
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    setPadding(0, 8, 0, 8)
                    textSize = 14f
                }
                projLayout.addView(tv)
            }
        }
    }

    private fun createEmptyText(msg: String): TextView {
        return TextView(this).apply {
            text = msg
            setTextColor(resources.getColor(R.color.text_tertiary, null))
            textSize = 13f
        }
    }

    // ==================== SKILL DIALOG ====================
    private fun showAddSkillDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 32, 64, 0)
        }

        val etName = EditText(this).apply {
            hint = "Skill name (e.g. React)"
            setTextColor(resources.getColor(R.color.text_primary, null))
            setHintTextColor(resources.getColor(R.color.text_tertiary, null))
        }
        val etCategory = EditText(this).apply {
            hint = "Category (e.g. Frontend)"
            setTextColor(resources.getColor(R.color.text_primary, null))
            setHintTextColor(resources.getColor(R.color.text_tertiary, null))
        }
        val etLevel = EditText(this).apply {
            hint = "Level: BEGINNER / INTERMEDIATE / ADVANCED"
            setTextColor(resources.getColor(R.color.text_primary, null))
            setHintTextColor(resources.getColor(R.color.text_tertiary, null))
        }

        layout.addView(etName)
        layout.addView(etCategory)
        layout.addView(etLevel)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Skill")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Skill name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val body = HashMap<String, String>()
                body["skillName"] = name
                body["category"] = etCategory.text.toString().trim().ifEmpty { "General" }
                body["proficiencyLevel"] = etLevel.text.toString().trim().ifEmpty { "BEGINNER" }

                ApiClient.getApi().addSkill(session.authToken, body)
                    .enqueue(object : Callback<JsonObject> {
                        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@ProfileActivity, "Skill added!", Toast.LENGTH_SHORT).show()
                                loadProfile()
                            } else {
                                Toast.makeText(this@ProfileActivity, "Failed to add skill", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                            Toast.makeText(this@ProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteSkill(skillId: Int, skillName: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Remove Skill")
            .setMessage("Remove \"$skillName\" from your profile?")
            .setPositiveButton("Remove") { _, _ ->
                ApiClient.getApi().removeSkill(session.authToken, skillId)
                    .enqueue(object : Callback<JsonObject> {
                        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@ProfileActivity, "Skill removed", Toast.LENGTH_SHORT).show()
                                loadProfile()
                            } else {
                                Toast.makeText(this@ProfileActivity, "Failed to remove skill", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                            Toast.makeText(this@ProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ==================== CERTIFICATION DIALOG ====================
    private fun showAddCertDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 32, 64, 0)
        }

        val etName = EditText(this).apply {
            hint = "Certification name"
            setTextColor(resources.getColor(R.color.text_primary, null))
            setHintTextColor(resources.getColor(R.color.text_tertiary, null))
        }
        val etOrg = EditText(this).apply {
            hint = "Issuing organization (e.g. Google)"
            setTextColor(resources.getColor(R.color.text_primary, null))
            setHintTextColor(resources.getColor(R.color.text_tertiary, null))
        }
        val etDate = EditText(this).apply {
            hint = "Issue date (e.g. 2025-01-15)"
            setTextColor(resources.getColor(R.color.text_primary, null))
            setHintTextColor(resources.getColor(R.color.text_tertiary, null))
        }
        val etUrl = EditText(this).apply {
            hint = "Credential URL (optional)"
            setTextColor(resources.getColor(R.color.text_primary, null))
            setHintTextColor(resources.getColor(R.color.text_tertiary, null))
        }

        layout.addView(etName)
        layout.addView(etOrg)
        layout.addView(etDate)
        layout.addView(etUrl)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Certification")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Certification name required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val body = HashMap<String, String>()
                body["name"] = name
                body["issuingOrganization"] = etOrg.text.toString().trim()
                val date = etDate.text.toString().trim()
                if (date.isNotEmpty()) body["issueDate"] = date
                val url = etUrl.text.toString().trim()
                if (url.isNotEmpty()) body["credentialUrl"] = url

                ApiClient.getApi().addCertification(session.authToken, body)
                    .enqueue(object : Callback<JsonObject> {
                        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@ProfileActivity, "Certification added!", Toast.LENGTH_SHORT).show()
                                loadProfile()
                            } else {
                                Toast.makeText(this@ProfileActivity, "Failed to add cert", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                            Toast.makeText(this@ProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ==================== PROJECT DIALOG ====================
    private fun showAddProjectDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 32, 64, 0)
        }

        val etTitle = EditText(this).apply {
            hint = "Project title"
            setTextColor(resources.getColor(R.color.text_primary, null))
            setHintTextColor(resources.getColor(R.color.text_tertiary, null))
        }
        val etDesc = EditText(this).apply {
            hint = "Description"
            setTextColor(resources.getColor(R.color.text_primary, null))
            setHintTextColor(resources.getColor(R.color.text_tertiary, null))
        }
        val etTech = EditText(this).apply {
            hint = "Tech stack (e.g. Kotlin, Firebase)"
            setTextColor(resources.getColor(R.color.text_primary, null))
            setHintTextColor(resources.getColor(R.color.text_tertiary, null))
        }
        val etUrl = EditText(this).apply {
            hint = "Project URL (optional)"
            setTextColor(resources.getColor(R.color.text_primary, null))
            setHintTextColor(resources.getColor(R.color.text_tertiary, null))
        }

        layout.addView(etTitle)
        layout.addView(etDesc)
        layout.addView(etTech)
        layout.addView(etUrl)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Project")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isEmpty()) {
                    Toast.makeText(this, "Project title required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val body = HashMap<String, String>()
                body["title"] = title
                val desc = etDesc.text.toString().trim()
                if (desc.isNotEmpty()) body["description"] = desc
                val tech = etTech.text.toString().trim()
                if (tech.isNotEmpty()) body["techStack"] = tech
                val url = etUrl.text.toString().trim()
                if (url.isNotEmpty()) body["projectUrl"] = url

                ApiClient.getApi().addProject(session.authToken, body)
                    .enqueue(object : Callback<JsonObject> {
                        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@ProfileActivity, "Project added!", Toast.LENGTH_SHORT).show()
                                loadProfile()
                            } else {
                                Toast.makeText(this@ProfileActivity, "Failed to add project", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                            Toast.makeText(this@ProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

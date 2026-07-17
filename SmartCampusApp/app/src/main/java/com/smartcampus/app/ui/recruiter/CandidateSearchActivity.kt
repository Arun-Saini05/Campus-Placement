package com.smartcampus.app.ui.recruiter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.smartcampus.app.R
import com.smartcampus.app.api.ApiClient
import com.smartcampus.app.models.Job
import com.smartcampus.app.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CandidateSearchActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var etSkillFilter: TextInputEditText
    private lateinit var etBranchFilter: AutoCompleteTextView
    private lateinit var etMinCgpa: TextInputEditText
    private lateinit var btnSearch: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var progressBar: ProgressBar
    
    private var candidateList = mutableListOf<JsonObject>()
    private var recruiterJobs = mutableListOf<Job>()
    private lateinit var adapter: CandidateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candidate_search)

        session = SessionManager(this)

        etSkillFilter = findViewById(R.id.etSkillFilter)
        etBranchFilter = findViewById(R.id.etBranchFilter)
        etMinCgpa = findViewById(R.id.etMinCgpa)
        btnSearch = findViewById(R.id.btnSearch)
        recyclerView = findViewById(R.id.recyclerViewCandidates)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        progressBar = findViewById(R.id.searchProgressBar)

        // Setup Branch Dropdown
        val branches = arrayOf("All Branches", "Computer Science", "Information Tech", "ECE", "Electrical", "Mechanical", "Civil")
        val branchAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, branches)
        etBranchFilter.setAdapter(branchAdapter)
        etBranchFilter.setText("All Branches", false)

        adapter = CandidateAdapter(candidateList, { candidate ->
            showScheduleInterviewDialog(candidate)
        }, { candidate ->
            showSelectCandidateDialog(candidate)
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        btnSearch.setOnClickListener { performSearch() }

        // Fetch jobs for interview dialog dropdown
        loadJobs()
        
        // Initial load
        performSearch()
    }

    private fun loadJobs() {
        ApiClient.getApi().getJobs(session.authToken, null, null, null).enqueue(object : Callback<List<Job>> {
            override fun onResponse(call: Call<List<Job>>, response: Response<List<Job>>) {
                if (response.isSuccessful && response.body() != null) {
                    recruiterJobs.clear()
                    recruiterJobs.addAll(response.body()!!)
                }
            }
            override fun onFailure(call: Call<List<Job>>, t: Throwable) {
                // Fail silently
            }
        })
    }

    private fun performSearch() {
        val skill = etSkillFilter.text.toString().trim()
        val branch = etBranchFilter.text.toString().trim()
        val cgpaText = etMinCgpa.text.toString().trim()

        val body = HashMap<String, Any>()
        if (skill.isNotEmpty()) body["skill"] = skill
        if (branch.isNotEmpty() && branch != "All Branches") body["branch"] = branch
        if (cgpaText.isNotEmpty()) {
            body["minCgpa"] = cgpaText.toFloatOrNull() ?: 0.0f
        }

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvEmptyState.visibility = View.GONE

        ApiClient.getApi().searchCandidates(session.authToken, body).enqueue(object : Callback<List<JsonObject>> {
            override fun onResponse(call: Call<List<JsonObject>>, response: Response<List<JsonObject>>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    candidateList.clear()
                    candidateList.addAll(response.body()!!)
                    adapter.notifyDataSetChanged()

                    if (candidateList.isEmpty()) {
                        tvEmptyState.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@CandidateSearchActivity, "Search failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@CandidateSearchActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showScheduleInterviewDialog(candidate: JsonObject) {
        if (recruiterJobs.isEmpty()) {
            Toast.makeText(this, "Please post a job first before scheduling interviews", Toast.LENGTH_LONG).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_post_job, null) as ScrollView
        // We will construct a clean simple dialog instead of loading the full post job layout
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
        }

        // Job Selector Spinner
        val tvJobLabel = TextView(this).apply { text = "Select Job Opening:" }
        val jobSpinner = Spinner(this)
        val jobTitles = recruiterJobs.map { "${it.title} (${it.companyName})" }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jobTitles)
        jobSpinner.adapter = spinnerAdapter
        
        // Date & Time Picker
        val tvDateLabel = TextView(this).apply { 
            text = "Select Date & Time (Click to choose):" 
            setPadding(0, 30, 0, 10)
        }
        val etDateTime = EditText(this).apply {
            hint = "YYYY-MM-DDTHH:MM:SS"
            isFocusable = false
            setOnClickListener {
                showDateTimePicker(this)
            }
        }

        // Location/Link Input
        val tvLinkLabel = TextView(this).apply { 
            text = "Interview Link / Location:"
            setPadding(0, 30, 0, 10)
        }
        val etLink = EditText(this).apply {
            hint = "e.g. Google Meet link or Room 101"
        }

        // Note Input
        val tvNoteLabel = TextView(this).apply { 
            text = "Notes / Instructions:"
            setPadding(0, 30, 0, 10)
        }
        val etNote = EditText(this).apply {
            hint = "e.g. Bring a printed copy of your resume"
        }

        layout.addView(tvJobLabel)
        layout.addView(jobSpinner)
        layout.addView(tvDateLabel)
        layout.addView(etDateTime)
        layout.addView(tvLinkLabel)
        layout.addView(etLink)
        layout.addView(tvNoteLabel)
        layout.addView(etNote)

        val candidateName = candidate.get("name")?.asString ?: "Candidate"

        AlertDialog.Builder(this)
            .setTitle("Schedule Interview with $candidateName")
            .setView(layout)
            .setPositiveButton("Schedule") { _, _ ->
                val selectedJobIndex = jobSpinner.selectedItemPosition
                val job = recruiterJobs[selectedJobIndex]
                val dateTimeStr = etDateTime.text.toString().trim()
                val link = etLink.text.toString().trim()
                val note = etNote.text.toString().trim()

                if (dateTimeStr.isEmpty()) {
                    Toast.makeText(this, "Date and Time are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val body = HashMap<String, String>()
                body["studentId"] = candidate.get("userId").asInt.toString()
                body["jobId"] = job.id.toString()
                body["interviewDate"] = dateTimeStr
                body["interviewLink"] = link
                body["feedback"] = note

                ApiClient.getApi().scheduleInterviewDirect(session.authToken, body).enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@CandidateSearchActivity, "Interview scheduled successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@CandidateSearchActivity, "Failed to schedule interview", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Toast.makeText(this@CandidateSearchActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSelectCandidateDialog(candidate: JsonObject) {
        if (recruiterJobs.isEmpty()) {
            Toast.makeText(this, "Please post a job first before hiring", Toast.LENGTH_LONG).show()
            return
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
        }

        val tvJobLabel = TextView(this).apply { text = "Hire candidate for which job?" }
        val jobSpinner = Spinner(this)
        val jobTitles = recruiterJobs.map { "${it.title} (${it.companyName})" }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jobTitles)
        jobSpinner.adapter = spinnerAdapter
        
        layout.addView(tvJobLabel)
        layout.addView(jobSpinner)

        val candidateName = candidate.get("name")?.asString ?: "Candidate"

        AlertDialog.Builder(this)
            .setTitle("Hire $candidateName")
            .setView(layout)
            .setPositiveButton("Hire") { _, _ ->
                val selectedJobIndex = jobSpinner.selectedItemPosition
                val job = recruiterJobs[selectedJobIndex]
                val body = HashMap<String, String>()
                body["studentId"] = candidate.get("userId").asInt.toString()
                body["jobId"] = job.id.toString()

                ApiClient.getApi().selectCandidateDirect(session.authToken, body).enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@CandidateSearchActivity, "Candidate Hired!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@CandidateSearchActivity, "Failed to hire", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Toast.makeText(this@CandidateSearchActivity, "Network error", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDateTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val formattedDateTime = String.format(
                    Locale.US,
                    "%04d-%02d-%02dT%02d:%02d:00",
                    year, month + 1, dayOfMonth, hourOfDay, minute
                )
                editText.setText(formattedDateTime)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}

class CandidateAdapter(
    private val list: List<JsonObject>,
    private val onSchedule: (JsonObject) -> Unit,
    private val onSelect: (JsonObject) -> Unit
) : RecyclerView.Adapter<CandidateAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entity_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val candidate = list[position]
        holder.tvTitle.text = candidate.get("name")?.asString ?: "Unknown"
        
        val branch = candidate.get("branch")?.asString ?: "N/A"
        val cgpa = candidate.get("cgpa")?.asFloat ?: 0f
        holder.tvSubtitle.text = "Branch: $branch | CGPA: $cgpa"
        
        val skillsJson = candidate.get("skills")?.asJsonArray
        val skillsList = mutableListOf<String>()
        skillsJson?.forEach { skillsList.add(it.asString) }
        holder.tvDescription.text = "Skills: " + if (skillsList.isEmpty()) "None listed" else skillsList.joinToString(", ")
        holder.tvDescription.visibility = View.VISIBLE

        // Change edit icon to calendar icon for scheduling interview
        holder.btnEdit.setImageResource(android.R.drawable.ic_menu_my_calendar)
        holder.btnEdit.setOnClickListener { onSchedule(candidate) }
        
        // Show delete icon as hire icon
        holder.btnDelete.visibility = View.VISIBLE
        holder.btnDelete.setImageResource(android.R.drawable.ic_menu_add)
        holder.btnDelete.setOnClickListener { onSelect(candidate) }
    }

    override fun getItemCount() = list.size
}

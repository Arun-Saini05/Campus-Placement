package com.smartcampus.app.ui.admin

import com.smartcampus.app.R

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartcampus.app.databinding.ActivityJobListBinding
import com.smartcampus.app.ui.admin.models.Job
import com.smartcampus.app.ui.admin.services.AdminService

class JobActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobListBinding
    private lateinit var adminService: AdminService
    private lateinit var adapter: JobAdapter
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = com.smartcampus.app.utils.SessionManager(this)
        token = session.authToken
        adminService = AdminService()
        
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = JobAdapter(
            jobs = emptyList(),
            onEdit = { job -> showJobDialog(job) },
            onDelete = { job -> 
                adminService.deleteJob(token, job.id) { success ->
                    if (success) refreshList()
                    else android.widget.Toast.makeText(this, "Failed to delete job", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.recyclerViewJobs.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewJobs.adapter = adapter

        binding.fabAddJob.setOnClickListener {
            showJobDialog(null)
        }
        
        refreshList()
    }

    private fun refreshList() {
        adminService.viewAllJobs(token) { jobs ->
            adapter.jobs = jobs
            adapter.notifyDataSetChanged()
        }
    }

    private fun showJobDialog(job: Job?) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val titleInput = EditText(this).apply {
            hint = "Job Title"
            setText(job?.title ?: "")
        }
        val companyInput = EditText(this).apply {
            hint = "Company Name"
            setText(job?.companyName ?: "")
        }
        val locationInput = EditText(this).apply {
            hint = "Location"
            setText(job?.location ?: "")
        }
        val salaryInput = EditText(this).apply {
            hint = "Salary Package"
            setText(job?.salaryPackage ?: "")
        }
        layout.addView(titleInput)
        layout.addView(companyInput)
        layout.addView(locationInput)
        layout.addView(salaryInput)

        AlertDialog.Builder(this)
            .setTitle(if (job == null) "Add Job" else "Edit Job")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString()
                val company = companyInput.text.toString()
                val location = locationInput.text.toString()
                val salary = salaryInput.text.toString()
                
                if (title.isNotBlank() && company.isNotBlank() && location.isNotBlank() && salary.isNotBlank()) {
                    if (job == null) {
                        adminService.addJob(token, title, company, location, salary) { success ->
                            if (success) refreshList()
                            else android.widget.Toast.makeText(this, "Failed to add job", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        adminService.updateJob(token, job.id, title, company, location, salary) { success ->
                            if (success) refreshList()
                            else android.widget.Toast.makeText(this, "Failed to update job", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class JobAdapter(
    var jobs: List<Job>,
    private val onEdit: (Job) -> Unit,
    private val onDelete: (Job) -> Unit
) : RecyclerView.Adapter<JobAdapter.ViewHolder>() {

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
        if (position >= jobs.size) return
        val job = jobs[position]
        holder.tvTitle.text = job.title
        holder.tvSubtitle.text = job.companyName
        holder.tvDescription.text = "Location: ${job.location} | CTC: ${job.salaryPackage}"
        holder.tvDescription.visibility = View.VISIBLE

        holder.btnEdit.setOnClickListener { onEdit(job) }
        holder.btnDelete.setOnClickListener { onDelete(job) }
    }

    override fun getItemCount() = jobs.size
}

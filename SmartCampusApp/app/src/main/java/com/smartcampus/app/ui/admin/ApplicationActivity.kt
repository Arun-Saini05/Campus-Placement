package com.smartcampus.app.ui.admin

import com.smartcampus.app.R

import android.os.Bundle
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
import com.smartcampus.app.databinding.ActivityApplicationListBinding
import com.smartcampus.app.ui.admin.models.Application
import com.smartcampus.app.ui.admin.services.AdminService

class ApplicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplicationListBinding
    private lateinit var adminService: AdminService
    private lateinit var adapter: ApplicationAdapter
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = com.smartcampus.app.utils.SessionManager(this)
        token = session.authToken
        adminService = AdminService()
        
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = ApplicationAdapter(
            applications = emptyList(),
            onEdit = { app -> showApplicationDialog(app) },
            onDelete = { app -> 
                adminService.deleteApplication(token, app.id) { success ->
                    if (success) refreshList()
                    else android.widget.Toast.makeText(this, "Failed to delete application", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.recyclerViewApplications.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewApplications.adapter = adapter

        binding.fabAddApplication.setOnClickListener {
            showApplicationDialog(null)
        }

        refreshList()
    }

    private fun refreshList() {
        adminService.viewAllApplications(token) { list ->
            adapter.applications = list
            adapter.notifyDataSetChanged()
        }
    }

    private fun showApplicationDialog(app: Application?) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val studentNameInput = EditText(this).apply {
            hint = "Student Name"
            setText(app?.studentName ?: "")
        }
        val jobTitleInput = EditText(this).apply {
            hint = "Job Title"
            setText(app?.jobTitle ?: "")
        }
        val statusInput = EditText(this).apply {
            hint = "Status (e.g. Applied, Selected)"
            setText(app?.status ?: "")
        }
        layout.addView(studentNameInput)
        layout.addView(jobTitleInput)
        layout.addView(statusInput)

        AlertDialog.Builder(this)
            .setTitle(if (app == null) "Add Application" else "Edit Application")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val studentName = studentNameInput.text.toString()
                val jobTitle = jobTitleInput.text.toString()
                val status = statusInput.text.toString()
                if (studentName.isNotBlank() && jobTitle.isNotBlank() && status.isNotBlank()) {
                    if (app == null) {
                        adminService.addApplication(token, studentName, jobTitle, status) { success ->
                            if (success) refreshList()
                            else android.widget.Toast.makeText(this, "Failed to add application", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        adminService.updateApplication(token, app.id, studentName, jobTitle, status) { success ->
                            if (success) refreshList()
                            else android.widget.Toast.makeText(this, "Failed to update application", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class ApplicationAdapter(
    var applications: List<Application>,
    private val onEdit: (Application) -> Unit,
    private val onDelete: (Application) -> Unit
) : RecyclerView.Adapter<ApplicationAdapter.ViewHolder>() {

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
        if (position >= applications.size) return
        val app = applications[position]
        holder.tvTitle.text = app.studentName
        holder.tvSubtitle.text = app.jobTitle
        holder.tvDescription.text = "Status: ${app.status}"
        holder.tvDescription.visibility = View.VISIBLE

        holder.btnEdit.setOnClickListener { onEdit(app) }
        holder.btnDelete.setOnClickListener { onDelete(app) }
    }

    override fun getItemCount() = applications.size
}

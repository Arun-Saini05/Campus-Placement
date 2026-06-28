package com.smartcampus.app.ui.admin

import com.smartcampus.app.R

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartcampus.app.databinding.ActivityOfficerListBinding
import com.smartcampus.app.ui.admin.models.RecruitmentOfficer
import com.smartcampus.app.ui.admin.services.AdminService
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

class OfficerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOfficerListBinding
    private lateinit var adminService: AdminService
    private lateinit var adapter: OfficerAdapter
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfficerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = com.smartcampus.app.utils.SessionManager(this)
        token = session.authToken
        adminService = AdminService()
        
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = OfficerAdapter(
            officers = emptyList(),
            onEdit = { officer -> showOfficerDialog(officer) },
            onDelete = { officer -> 
                adminService.deleteStudent(token, officer.id) { success -> // Officers are also users
                    if (success) refreshList()
                }
            }
        )

        binding.recyclerViewOfficers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewOfficers.adapter = adapter

        binding.fabAddOfficer.setOnClickListener {
            showOfficerDialog(null)
        }
        
        refreshList()
    }

    private fun refreshList() {
        adminService.viewAllRecruitmentOfficers(token) { list ->
            adapter.officers = list
            adapter.notifyDataSetChanged()
        }
    }

    private fun showOfficerDialog(officer: RecruitmentOfficer?) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val nameInput = EditText(this).apply {
            hint = "Full Name"
            setText(officer?.name ?: "")
        }
        val emailInput = EditText(this).apply {
            hint = "Email"
            setText(officer?.email ?: "")
        }
        val companyInput = EditText(this).apply {
            hint = "Company"
            setText(officer?.company ?: "")
        }
        layout.addView(nameInput)
        layout.addView(emailInput)
        layout.addView(companyInput)

        AlertDialog.Builder(this)
            .setTitle(if (officer == null) "Add Placement Officer" else "Edit Placement Officer")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString()
                val email = emailInput.text.toString()
                val company = companyInput.text.toString()
                if (name.isNotBlank() && email.isNotBlank() && company.isNotBlank()) {
                    if (officer == null) {
                        adminService.addRecruitmentOfficer(token, name, email, company) { success ->
                            if (success) refreshList()
                        }
                    } else {
                        adminService.updateRecruitmentOfficer(token, officer.id, name, email, company) { success ->
                            if (success) refreshList()
                            else android.widget.Toast.makeText(this, "Failed to update officer", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class OfficerAdapter(
    var officers: List<RecruitmentOfficer>,
    private val onEdit: (RecruitmentOfficer) -> Unit,
    private val onDelete: (RecruitmentOfficer) -> Unit
) : RecyclerView.Adapter<OfficerAdapter.ViewHolder>() {

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
        val officer = officers[position]
        holder.tvTitle.text = officer.name
        holder.tvSubtitle.text = officer.email
        holder.tvDescription.text = "Company: ${officer.company}"
        holder.tvDescription.visibility = View.VISIBLE

        holder.btnEdit.setOnClickListener { onEdit(officer) }
        holder.btnDelete.setOnClickListener { onDelete(officer) }
    }

    override fun getItemCount() = officers.size
}

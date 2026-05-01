package com.smartcampus.app.ui.admin

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
import com.smartcampus.app.databinding.ActivityCompanyListBinding
import com.smartcampus.app.ui.admin.models.Company
import com.smartcampus.app.ui.admin.services.AdminService

class CompanyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyListBinding
    private lateinit var adminService: AdminService
    private lateinit var adapter: CompanyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminService = AdminService()
        
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = CompanyAdapter(
            companies = adminService.viewAllCompanies(),
            onEdit = { company -> showCompanyDialog(company) },
            onDelete = { company -> 
                adminService.deleteCompany(company.id)
                refreshList()
            }
        )

        binding.recyclerViewCompanies.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCompanies.adapter = adapter

        binding.fabAddCompany.setOnClickListener {
            showCompanyDialog(null)
        }
    }

    private fun refreshList() {
        adapter.companies = adminService.viewAllCompanies()
        adapter.notifyDataSetChanged()
    }

    private fun showCompanyDialog(company: Company?) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val nameInput = EditText(this).apply {
            hint = "Company Name"
            setText(company?.name ?: "")
        }
        val industryInput = EditText(this).apply {
            hint = "Industry"
            setText(company?.industry ?: "")
        }
        val locationInput = EditText(this).apply {
            hint = "Location"
            setText(company?.location ?: "")
        }
        layout.addView(nameInput)
        layout.addView(industryInput)
        layout.addView(locationInput)

        AlertDialog.Builder(this)
            .setTitle(if (company == null) "Add Company" else "Edit Company")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString()
                val industry = industryInput.text.toString()
                val location = locationInput.text.toString()
                if (name.isNotBlank() && industry.isNotBlank() && location.isNotBlank()) {
                    if (company == null) {
                        adminService.addCompany(name, industry, location)
                    } else {
                        adminService.updateCompany(company.id, name, industry, location)
                    }
                    refreshList()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class CompanyAdapter(
    var companies: List<Company>,
    private val onEdit: (Company) -> Unit,
    private val onDelete: (Company) -> Unit
) : RecyclerView.Adapter<CompanyAdapter.ViewHolder>() {

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
        val company = companies[position]
        holder.tvTitle.text = company.name
        holder.tvSubtitle.text = "Industry: ${company.industry}"
        holder.tvDescription.text = "Location: ${company.location}"
        holder.tvDescription.visibility = View.VISIBLE

        holder.btnEdit.setOnClickListener { onEdit(company) }
        holder.btnDelete.setOnClickListener { onDelete(company) }
    }

    override fun getItemCount() = companies.size
}

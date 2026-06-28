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
import com.smartcampus.app.databinding.ActivityCollegeListBinding
import com.smartcampus.app.ui.admin.models.College
import com.smartcampus.app.ui.admin.services.AdminService
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

class CollegeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCollegeListBinding
    private lateinit var adminService: AdminService
    private lateinit var adapter: CollegeAdapter
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollegeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = com.smartcampus.app.utils.SessionManager(this)
        token = session.authToken
        adminService = AdminService()
        
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = CollegeAdapter(
            colleges = emptyList(),
            onEdit = { college -> showCollegeDialog(college) },
            onDelete = { college -> 
                adminService.deleteCollege(token, college.id) { success ->
                    if (success) refreshList()
                }
            }
        )

        binding.recyclerViewColleges.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewColleges.adapter = adapter

        binding.fabAddCollege.setOnClickListener {
            showCollegeDialog(null)
        }
        
        refreshList()
    }

    private fun refreshList() {
        adminService.viewAllColleges(token) { list ->
            adapter.colleges = list
            adapter.notifyDataSetChanged()
        }
    }

    private fun showCollegeDialog(college: College?) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val nameInput = EditText(this).apply {
            hint = "College Name"
            setText(college?.name ?: "")
        }
        val locationInput = EditText(this).apply {
            hint = "Location"
            setText(college?.location ?: "")
        }
        layout.addView(nameInput)
        layout.addView(locationInput)

        AlertDialog.Builder(this)
            .setTitle(if (college == null) "Add College" else "Edit College")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString()
                val location = locationInput.text.toString()
                if (name.isNotBlank() && location.isNotBlank()) {
                    if (college == null) {
                        adminService.addCollege(token, name, location) { success ->
                            if (success) refreshList()
                        }
                    } else {
                        adminService.updateCollege(token, college.id, name, location) { success ->
                            if (success) refreshList()
                            else android.widget.Toast.makeText(this, "Update failed", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class CollegeAdapter(
    var colleges: List<College>,
    private val onEdit: (College) -> Unit,
    private val onDelete: (College) -> Unit
) : RecyclerView.Adapter<CollegeAdapter.ViewHolder>() {

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
        val college = colleges[position]
        holder.tvTitle.text = college.name
        holder.tvSubtitle.text = "Location: ${college.location}"
        holder.tvDescription.visibility = View.GONE

        holder.btnEdit.setOnClickListener { onEdit(college) }
        holder.btnDelete.setOnClickListener { onDelete(college) }
    }

    override fun getItemCount() = colleges.size
}

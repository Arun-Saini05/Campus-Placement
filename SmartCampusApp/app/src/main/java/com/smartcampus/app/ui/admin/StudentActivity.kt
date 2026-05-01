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
import com.smartcampus.app.databinding.ActivityStudentListBinding
import com.smartcampus.app.ui.admin.models.Student
import com.smartcampus.app.ui.admin.services.AdminService

class StudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentListBinding
    private lateinit var adminService: AdminService
    private lateinit var adapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminService = AdminService()
        
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = StudentAdapter(
            students = adminService.viewAllStudents(),
            onEdit = { student -> showStudentDialog(student) },
            onDelete = { student -> 
                adminService.deleteStudent(student.id)
                refreshList()
            }
        )

        binding.recyclerViewStudents.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewStudents.adapter = adapter

        binding.fabAddStudent.setOnClickListener {
            showStudentDialog(null)
        }
    }

    private fun refreshList() {
        adapter.students = adminService.viewAllStudents()
        adapter.notifyDataSetChanged()
    }

    private fun showStudentDialog(student: Student?) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val nameInput = EditText(this).apply {
            hint = "Full Name"
            setText(student?.name ?: "")
        }
        val emailInput = EditText(this).apply {
            hint = "Email"
            setText(student?.email ?: "")
        }
        val branchInput = EditText(this).apply {
            hint = "Branch/Course"
            setText(student?.branch ?: "")
        }
        layout.addView(nameInput)
        layout.addView(emailInput)
        layout.addView(branchInput)

        AlertDialog.Builder(this)
            .setTitle(if (student == null) "Add Student" else "Edit Student")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString()
                val email = emailInput.text.toString()
                val branch = branchInput.text.toString()
                if (name.isNotBlank() && email.isNotBlank() && branch.isNotBlank()) {
                    if (student == null) {
                        adminService.addStudent(name, email, branch)
                    } else {
                        adminService.updateStudent(student.id, name, email, branch)
                    }
                    refreshList()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class StudentAdapter(
    var students: List<Student>,
    private val onEdit: (Student) -> Unit,
    private val onDelete: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

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
        val student = students[position]
        holder.tvTitle.text = student.name
        holder.tvSubtitle.text = student.email
        holder.tvDescription.text = "Branch: ${student.branch}"
        holder.tvDescription.visibility = View.VISIBLE

        holder.btnEdit.setOnClickListener { onEdit(student) }
        holder.btnDelete.setOnClickListener { onDelete(student) }
    }

    override fun getItemCount() = students.size
}

package com.smartcampus.app.ui.officer

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.smartcampus.app.R
import com.smartcampus.app.api.ApiClient
import com.smartcampus.app.ui.auth.LoginActivity
import com.smartcampus.app.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfficerDashboardActivity : AppCompatActivity() {
    private lateinit var session: SessionManager
    private lateinit var container: LinearLayout
    private lateinit var layoutStats: LinearLayout
    private lateinit var layoutUpdates: LinearLayout
    private lateinit var layoutDrives: LinearLayout
    
    private var notifications: List<JsonObject> = emptyList()

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic)
        session = SessionManager(this)
        findViewById<TextView>(R.id.tvPageTitle).text = "Recruitment Hub"
        container = findViewById(R.id.layoutContent)
    }

    override fun onResume() {
        super.onResume()
        container.removeAllViews()
        setupSections()
        
        addHeaderSection()
        addQuickActions()
        
        loadStats()
        loadNotifications()
        loadDrives()
        
        addLogoutButton()
    }

    private fun setupSections() {
        layoutStats = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        layoutUpdates = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        layoutDrives = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        
        container.addView(layoutStats)
        container.addView(layoutUpdates)
        container.addView(layoutDrives)
    }

    // ========== HEADER ==========
    private fun addHeaderSection() {
        container.addView(TextView(this).apply {
            text = "Officer Analytics\nOverview"
            textSize = 26f; setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD); setLineSpacing(0f, 1.1f)
        }, 0)
        container.addView(TextView(this).apply {
            text = "Monitoring placement efficiency for the Class of 2025"
            textSize = 13f; setTextColor(resources.getColor(R.color.text_secondary, null))
            setPadding(0, dp(4), 0, dp(16))
        }, 1)
    }

    // ========== QUICK ACTIONS ==========
    private fun addQuickActions() {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(20) }
        }

        val btnDrive = MaterialButton(this).apply {
            text = "＋ New Drive"; setBackgroundColor(Color.parseColor("#00BCD4"))
            setTextColor(Color.WHITE); cornerRadius = dp(24); textSize = 13f; isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(8) }
        }
        btnDrive.setOnClickListener { startActivity(Intent(this, CreateDriveActivity::class.java)) }

        val btnDirectory = MaterialButton(this).apply {
            text = "🔍 Students"; setBackgroundColor(Color.parseColor("#3FB950"))
            setTextColor(Color.WHITE); cornerRadius = dp(24); textSize = 13f; isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginStart = dp(8) }
        }
        btnDirectory.setOnClickListener { startActivity(Intent(this, StudentDirectoryActivity::class.java)) }

        row.addView(btnDrive); row.addView(btnDirectory)
        container.addView(row, 2)

        val btnAnnounce = MaterialButton(this).apply {
            text = "📢 Send Mass Announcement"; setBackgroundColor(Color.parseColor("#FF9800"))
            setTextColor(Color.WHITE); cornerRadius = dp(24); textSize = 14f; isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
            ).apply { bottomMargin = dp(20) }
        }
        btnAnnounce.setOnClickListener { startActivity(Intent(this, MassNotificationActivity::class.java)) }
        container.addView(btnAnnounce, 3)
    }

    // ========== STATS SECTION ==========
    private fun loadStats() {
        ApiClient.getApi().getOfficerStats(session.authToken).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, resp: Response<JsonObject>) {
                if (resp.isSuccessful && resp.body() != null) {
                    val s = resp.body()!!
                    layoutStats.removeAllViews()
                    
                    val total = s.get("totalStudents")?.asInt ?: 0
                    val placed = s.get("placedStudents")?.asInt ?: 0
                    val unplaced = s.get("unplacedStudents")?.asInt ?: 0
                    val highest = s.get("highestPackage")?.asString ?: "N/A"
                    val avg = s.get("averagePackage")?.asString ?: "N/A"
                    val offers = s.get("totalOffers")?.asInt ?: 0
                    
                    addStatsGrid(total, placed, unplaced, highest, avg, offers)
                    addDepartmentChart(s.getAsJsonObject("departmentWisePlacements"))
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {}
        })
    }

    private fun addStatsGrid(total: Int, placed: Int, unplaced: Int, highest: String, avg: String, offers: Int) {
        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
        }
        val placedPerc = if(total > 0) (placed * 100 / total) else 0
        row1.addView(buildStatCard("STUDENTS", "$total", "$unplaced unplaced", Color.parseColor("#00E5FF")))
        row1.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(12), 0) })
        row1.addView(buildStatCard("PLACED", "$placed", "$placedPerc% placement", Color.parseColor("#3FB950")))
        layoutStats.addView(row1)

        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(24) }
        }
        row2.addView(buildStatCard("HIGHEST PKG", highest, "$offers total offers", Color.parseColor("#F0883E")))
        row2.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(12), 0) })
        row2.addView(buildStatCard("AVERAGE PKG", avg, "Across all branches", Color.parseColor("#58A6FF")))
        layoutStats.addView(row2)
    }

    private fun addDepartmentChart(deptStats: JsonObject?) {
        if (deptStats == null || deptStats.size() == 0) return

        layoutStats.addView(TextView(this).apply {
            text = "Department-wise Placements"; textSize = 16f; setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
        })

        val chartCard = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22")); radius = dp(16).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(24) }
        }

        val chartLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        var maxCount = 1
        for (key in deptStats.keySet()) {
            val count = deptStats.get(key).asInt
            if (count > maxCount) maxCount = count
        }

        val colors = listOf("#6C63FF", "#00BCD4", "#F0883E", "#3FB950")
        var colorIdx = 0
        for (key in deptStats.keySet()) {
            val count = deptStats.get(key).asInt
            val percentage = (count.toFloat() / maxCount) * 100
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12) }
            }
            val labelRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            labelRow.addView(TextView(this).apply {
                text = key; textSize = 12f; setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            labelRow.addView(TextView(this).apply {
                text = "$count"; textSize = 12f; setTextColor(Color.parseColor("#8B949E"))
                setTypeface(typeface, Typeface.BOLD)
            })
            itemLayout.addView(labelRow)
            val barBg = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(8)
                ).apply { topMargin = dp(4) }
                background = GradientDrawable().apply { setColor(Color.parseColor("#21262D")); cornerRadius = dp(4).toFloat() }
            }
            val barFill = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(0, dp(8))
                background = GradientDrawable().apply { setColor(Color.parseColor(colors[colorIdx % colors.size])); cornerRadius = dp(4).toFloat() }
            }
            barBg.addView(barFill)
            barFill.post {
                val fullWidth = barBg.width
                barFill.layoutParams = FrameLayout.LayoutParams((fullWidth * percentage / 100).toInt(), dp(8))
                barFill.requestLayout()
            }
            itemLayout.addView(barBg); chartLayout.addView(itemLayout); colorIdx++
        }
        chartCard.addView(chartLayout); layoutStats.addView(chartCard)
    }

    private fun buildStatCard(label: String, value: String, sub: String, color: Int): MaterialCardView {
        val card = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22")); radius = dp(16).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        inner.addView(TextView(this).apply { text = label; textSize = 10f; setTextColor(Color.parseColor("#8B949E")); setTypeface(typeface, Typeface.BOLD); letterSpacing = 0.1f })
        inner.addView(TextView(this).apply { text = value; textSize = 24f; setTextColor(color); setTypeface(typeface, Typeface.BOLD); setPadding(0, dp(4), 0, dp(4)) })
        inner.addView(TextView(this).apply { text = sub; textSize = 10f; setTextColor(Color.parseColor("#8B949E")) })
        card.addView(inner); return card
    }

    // ========== RECENT UPDATES ==========
    private fun loadNotifications() {
        ApiClient.getApi().getNotifications(session.authToken).enqueue(object : Callback<List<JsonObject>> {
            override fun onResponse(call: Call<List<JsonObject>>, resp: Response<List<JsonObject>>) {
                if (resp.isSuccessful && resp.body() != null) {
                    notifications = resp.body()!!
                    addRecentUpdatesSection()
                }
            }
            override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) {}
        })
    }

    private fun addRecentUpdatesSection() {
        layoutUpdates.removeAllViews()

        val card = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22")); radius = dp(16).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16); bottomMargin = dp(16) }
        }
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        inner.addView(TextView(this).apply { 
            text = "📌  Recent Updates"
            textSize = 16f; setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, dp(12)) 
        })

        if (notifications.isEmpty()) {
            inner.addView(TextView(this).apply {
                text = "No recent activities logged yet."
                textSize = 12f; setTextColor(Color.parseColor("#8B949E"))
                setPadding(dp(4), 0, 0, dp(8))
            })
        } else {
            // Filter to only show the latest DRIVE_DELETED if multiple exist consecutively or in the list
            var hasShownDeletion = false
            val filteredNotifications = notifications.filter { n ->
                val type = n.get("type")?.asString ?: ""
                if (type == "DRIVE_DELETED") {
                    if (hasShownDeletion) false else { hasShownDeletion = true; true }
                } else true
            }

            filteredNotifications.take(5).forEach { n ->
                val type = n.get("type")?.asString ?: ""
                val icon = when(type) { 
                    "DRIVE_CREATED" -> "🎉"
                    "ANNOUNCEMENT_SENT" -> "📢"
                    "STUDENT_PLACED" -> "🎯"
                    "STATS_UPDATED" -> "📊"
                    "DRIVE_DELETED" -> "🗑️"
                    else -> "📌" 
                }
                val row = LinearLayout(this).apply { 
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, dp(8), 0, dp(8)) 
                }
                row.addView(TextView(this).apply { 
                    text = icon; textSize = 18f
                    layoutParams = LinearLayout.LayoutParams(dp(32), dp(32))
                    gravity = Gravity.CENTER 
                })
                val col = LinearLayout(this).apply { 
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) } 
                }
                col.addView(TextView(this).apply { 
                    text = n.get("title")?.asString ?: "Update"
                    textSize = 13f; setTextColor(Color.WHITE); setTypeface(typeface, Typeface.BOLD) 
                })
                col.addView(TextView(this).apply { 
                    text = n.get("message")?.asString ?: ""
                    textSize = 11f; setTextColor(Color.parseColor("#8B949E")) 
                })
                row.addView(col); inner.addView(row)
            }
        }
        card.addView(inner); layoutUpdates.addView(card)
    }

    // ========== DRIVES SECTION ==========
    private fun loadDrives() {
        ApiClient.getApi().getDrives(session.authToken).enqueue(object : Callback<List<JsonObject>> {
            override fun onResponse(call: Call<List<JsonObject>>, resp: Response<List<JsonObject>>) {
                if (resp.isSuccessful && resp.body() != null) {
                    val drives = resp.body()!!
                    layoutDrives.removeAllViews()
                    addDrivesHeader()
                    if (drives.isEmpty()) {
                        layoutDrives.addView(TextView(this@OfficerDashboardActivity).apply { text = "No drives yet. Create one above!"; textSize = 14f; setTextColor(Color.parseColor("#8B949E")); setPadding(0, 0, 0, dp(16)) })
                    } else {
                        drives.forEach { addDriveCard(it) }
                    }
                }
            }
            override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) {}
        })
    }

    private fun addDrivesHeader() {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(12) }
        }
        header.addView(TextView(this).apply { text = "Featured Recruitment Drives"; textSize = 16f; setTextColor(Color.WHITE); setTypeface(typeface, Typeface.BOLD); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) })
        header.addView(TextView(this).apply { text = "View All →"; textSize = 12f; setTextColor(Color.parseColor("#00E5FF")) })
        layoutDrives.addView(header)
    }

    private fun addDriveCard(drive: JsonObject) {
        val id = drive.get("id")?.asInt ?: 0
        val company = drive.get("companyName")?.asString ?: "Company"
        val desc = drive.get("description")?.asString ?: ""
        val date = drive.get("driveDate")?.asString?.take(10) ?: "TBD"
        val card = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22")); radius = dp(16).toFloat(); setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(12) }
        }
        val outer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        
        // Add Close/Remove icon in the top right using a FrameLayout wrapper for the top row
        val headerWrapper = FrameLayout(this)
        val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val circleColor = listOf("#6C63FF", "#00BCD4", "#F0883E", "#3FB950", "#58A6FF")[id % 5]
        topRow.addView(TextView(this).apply { text = company.first().uppercase(); textSize = 16f; setTextColor(Color.WHITE); setTypeface(typeface, Typeface.BOLD); gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply { marginEnd = dp(12) }; background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.parseColor(circleColor)) } })
        val titleCol = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        titleCol.addView(TextView(this).apply { text = if (desc.isNotEmpty()) desc else company; textSize = 15f; setTextColor(Color.WHITE); setTypeface(typeface, Typeface.BOLD); maxLines = 2 })
        titleCol.addView(TextView(this).apply { text = company; textSize = 12f; setTextColor(Color.parseColor("#8B949E")) })
        topRow.addView(titleCol)
        
        headerWrapper.addView(topRow)
        
        // The Cross Icon
        val ivRemove = TextView(this).apply {
            text = "✕"; textSize = 14f; setTextColor(Color.parseColor("#8B949E"))
            gravity = Gravity.CENTER; setTypeface(typeface, Typeface.BOLD)
            layoutParams = FrameLayout.LayoutParams(dp(28), dp(28)).apply { gravity = Gravity.TOP or Gravity.END }
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.parseColor("#21262D")) }
        }
        ivRemove.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Remove Drive")
                .setMessage("Are you sure you want to remove the placement drive for $company?")
                .setPositiveButton("Remove") { _, _ ->
                    ApiClient.getApi().deleteDrive(session.authToken, id).enqueue(object : Callback<JsonObject> {
                        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@OfficerDashboardActivity, "Drive removed", Toast.LENGTH_SHORT).show()
                                onResume()
                            }
                        }
                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                            Toast.makeText(this@OfficerDashboardActivity, "Error removing drive", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        headerWrapper.addView(ivRemove)
        outer.addView(headerWrapper)
        
        val details = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(12), 0, dp(8)) }
        details.addView(TextView(this).apply { text = "💰 ₹${drive.get("salaryPackage")?.asString ?: ""}"; textSize = 12f; setTextColor(Color.parseColor("#00E5FF")); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(16) } })
        details.addView(TextView(this).apply { text = "📅 $date"; textSize = 12f; setTextColor(Color.parseColor("#8B949E")) })
        outer.addView(details)
        
        val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(8), 0, 0) }
        val btnTrack = MaterialButton(this).apply {
            text = "Track Drive Details"; textSize = 11f; isAllCaps = false; cornerRadius = dp(16)
            setBackgroundColor(Color.parseColor("#3FB950")); setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38))
        }
        btnTrack.setOnClickListener {
            val intent = Intent(this, EligibleStudentsActivity::class.java)
            intent.putExtra("driveId", id); intent.putExtra("companyName", company)
            startActivity(intent)
        }

        btnRow.addView(btnTrack)
        outer.addView(btnRow); card.addView(outer); layoutDrives.addView(card)
    }

    private fun addLogoutButton() {
        container.addView(MaterialButton(this).apply {
            text = "Logout"; setBackgroundColor(resources.getColor(R.color.error, null)); cornerRadius = dp(12)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)).apply { topMargin = dp(8); bottomMargin = dp(32) }
            setOnClickListener { session.logout(); startActivity(Intent(this@OfficerDashboardActivity, LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }) }
        })
    }
}

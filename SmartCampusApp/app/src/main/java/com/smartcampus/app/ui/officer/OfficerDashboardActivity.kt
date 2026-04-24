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
    private var hasLoaded = false

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
        addHeaderSection()
        addCreateDriveButton()
        loadStats()
        loadDrives()
    }

    // ========== HEADER ==========
    private fun addHeaderSection() {
        container.addView(TextView(this).apply {
            text = "Officer Analytics\nOverview"
            textSize = 26f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setLineSpacing(0f, 1.1f)
        })
        container.addView(TextView(this).apply {
            text = "Monitoring placement efficiency for the Class of 2025"
            textSize = 13f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            setPadding(0, dp(4), 0, dp(16))
        })
    }

    // ========== CREATE DRIVE BUTTON ==========
    private fun addCreateDriveButton() {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(20) }
        }

        val btnDrive = MaterialButton(this).apply {
            text = "＋ New Drive"
            setBackgroundColor(Color.parseColor("#00BCD4"))
            setTextColor(Color.WHITE)
            cornerRadius = dp(24)
            textSize = 13f
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(8) }
        }
        btnDrive.setOnClickListener {
            startActivity(Intent(this, CreateDriveActivity::class.java))
        }

        val btnDirectory = MaterialButton(this).apply {
            text = "🔍 Students"
            setBackgroundColor(Color.parseColor("#3FB950"))
            setTextColor(Color.WHITE)
            cornerRadius = dp(24)
            textSize = 13f
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginStart = dp(8) }
        }
        btnDirectory.setOnClickListener {
            startActivity(Intent(this, StudentDirectoryActivity::class.java))
        }

        row.addView(btnDrive)
        row.addView(btnDirectory)
        container.addView(row)
    }

    // ========== STATS SECTION ==========
    private fun loadStats() {
        ApiClient.getApi().getOfficerStats(session.authToken).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, resp: Response<JsonObject>) {
                if (resp.isSuccessful && resp.body() != null) {
                    val s = resp.body()!!
                    
                    val total = s.get("totalStudents")?.asInt ?: 0
                    val placed = s.get("placedStudents")?.asInt ?: 0
                    val unplaced = s.get("unplacedStudents")?.asInt ?: 0
                    val highest = s.get("highestPackage")?.asString ?: "N/A"
                    val avg = s.get("averagePackage")?.asString ?: "N/A"
                    val offers = s.get("totalOffers")?.asInt ?: 0
                    
                    val deptStats = s.getAsJsonObject("departmentWisePlacements")

                    addStatsGrid(total, placed, unplaced, highest, avg, offers)
                    addDepartmentChart(deptStats)
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {}
        })
    }

    private fun addStatsGrid(total: Int, placed: Int, unplaced: Int, highest: String, avg: String, offers: Int) {
        // Row 1: Students & Placed
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
        container.addView(row1)

        // Row 2: Packages
        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(24) }
        }
        row2.addView(buildStatCard("HIGHEST PKG", highest, "$offers total offers", Color.parseColor("#F0883E")))
        row2.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(12), 0) })
        row2.addView(buildStatCard("AVERAGE PKG", avg, "Across all branches", Color.parseColor("#58A6FF")))
        container.addView(row2)
    }

    private fun addDepartmentChart(deptStats: JsonObject?) {
        if (deptStats == null || deptStats.size() == 0) return

        container.addView(TextView(this).apply {
            text = "Department-wise Placements"
            textSize = 16f; setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
        })

        val chartCard = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = dp(16).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(24) }
        }

        val chartLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        
        // Find max value to scale bars
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

            // Label
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

            // Bar background
            val barBg = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(8)
                ).apply { topMargin = dp(4) }
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#21262D"))
                    cornerRadius = dp(4).toFloat()
                }
            }

            // Colored Bar
            val barFill = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    0, dp(8)
                )
                background = GradientDrawable().apply {
                    setColor(Color.parseColor(colors[colorIdx % colors.size]))
                    cornerRadius = dp(4).toFloat()
                }
            }
            barBg.addView(barFill)
            
            // Animate bar width (simulated with post)
            barFill.post {
                val fullWidth = barBg.width
                barFill.layoutParams = FrameLayout.LayoutParams((fullWidth * percentage / 100).toInt(), dp(8))
                barFill.requestLayout()
            }

            itemLayout.addView(barBg)
            chartLayout.addView(itemLayout)
            colorIdx++
        }

        chartCard.addView(chartLayout)
        container.addView(chartCard)
    }

    private fun buildStatCard(label: String, value: String, sub: String, color: Int): MaterialCardView {
        val card = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = dp(16).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        inner.addView(TextView(this).apply {
            text = label; textSize = 10f
            setTextColor(Color.parseColor("#8B949E"))
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.1f
        })
        inner.addView(TextView(this).apply {
            text = value; textSize = 24f // slightly smaller to fit
            setTextColor(color)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(4))
        })
        inner.addView(TextView(this).apply {
            text = sub; textSize = 10f
            setTextColor(Color.parseColor("#8B949E")) // Make subtext muted gray
        })
        card.addView(inner)
        return card
    }

    // ========== DRIVES SECTION ==========
    private fun loadDrives() {
        ApiClient.getApi().getDrives(session.authToken).enqueue(object : Callback<List<JsonObject>> {
            override fun onResponse(call: Call<List<JsonObject>>, resp: Response<List<JsonObject>>) {
                if (resp.isSuccessful && resp.body() != null) {
                    val drives = resp.body()!!
                    addDrivesHeader()

                    if (drives.isEmpty()) {
                        container.addView(TextView(this@OfficerDashboardActivity).apply {
                            text = "No drives yet. Create one above!"
                            textSize = 14f
                            setTextColor(Color.parseColor("#8B949E"))
                            setPadding(0, 0, 0, dp(16))
                        })
                    } else {
                        drives.forEach { drive -> addDriveCard(drive) }
                    }
                }
                addRecentUpdatesSection()
                addLogoutButton()
            }
            override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) {
                Toast.makeText(this@OfficerDashboardActivity, "Failed to load drives", Toast.LENGTH_SHORT).show()
                addRecentUpdatesSection()
                addLogoutButton()
            }
        })
    }

    private fun addDrivesHeader() {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
        }
        header.addView(TextView(this).apply {
            text = "Featured Recruitment Drives"
            textSize = 16f; setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        header.addView(TextView(this).apply {
            text = "View All →"; textSize = 12f
            setTextColor(Color.parseColor("#00E5FF"))
        })
        container.addView(header)
    }

    private fun addDriveCard(drive: JsonObject) {
        val driveId = drive.get("id")?.asInt ?: 0
        val company = drive.get("companyName")?.asString ?: "Company"
        val desc = drive.get("description")?.asString ?: ""
        val salary = drive.get("salaryPackage")?.asString ?: ""
        val date = drive.get("driveDate")?.asString?.take(10) ?: "TBD"
        val eligible = drive.get("eligibleStudentCount")?.asInt ?: 0

        val card = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = dp(16).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            isClickable = true; isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
        }

        val outer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // Row 1: Logo + Title + Badge
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        }
        val colors = listOf("#6C63FF", "#00BCD4", "#F0883E", "#3FB950", "#58A6FF")
        val circleColor = colors[driveId % colors.size]
        topRow.addView(TextView(this).apply {
            text = company.first().uppercase(); textSize = 16f
            setTextColor(Color.WHITE); setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            val size = dp(40)
            layoutParams = LinearLayout.LayoutParams(size, size).apply { marginEnd = dp(12) }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL; setColor(Color.parseColor(circleColor))
            }
        })

        val titleCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        titleCol.addView(TextView(this).apply {
            text = if (desc.isNotEmpty()) desc else company
            textSize = 15f; setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD); maxLines = 2
        })
        titleCol.addView(TextView(this).apply {
            text = company; textSize = 12f
            setTextColor(Color.parseColor("#8B949E"))
        })
        topRow.addView(titleCol)

        // Badge
        val isEligible = eligible > 0
        topRow.addView(TextView(this).apply {
            text = if (isEligible) "ELIGIBLE" else "PENDING"
            textSize = 9f
            setTextColor(if (isEligible) Color.parseColor("#3FB950") else Color.parseColor("#F0883E"))
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(8), dp(4), dp(8), dp(4))
            background = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(if (isEligible) Color.parseColor("#1A3FB950") else Color.parseColor("#1AF0883E"))
            }
        })
        outer.addView(topRow)

        // Row 2: Salary + Date
        val detailRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(12), 0, dp(8))
        }
        if (salary.isNotEmpty()) {
            detailRow.addView(TextView(this).apply {
                text = "💰 ₹$salary"; textSize = 12f
                setTextColor(Color.parseColor("#00E5FF"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(16) }
            })
        }
        detailRow.addView(TextView(this).apply {
            text = "📅 $date"; textSize = 12f
            setTextColor(Color.parseColor("#8B949E"))
        })
        outer.addView(detailRow)

        // Row 3: Eligible count
        outer.addView(TextView(this).apply {
            text = "👥 $eligible Eligible Students"; textSize = 12f
            setTextColor(Color.parseColor("#8B949E"))
            setPadding(0, 0, 0, dp(12))
        })

        // Row 4: Action Buttons
        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.START
        }
        val btnDetails = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "Details"; textSize = 11f; isAllCaps = false; cornerRadius = dp(16)
            setTextColor(Color.parseColor("#8B949E"))
            strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#30363D"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(36)
            ).apply { marginEnd = dp(8) }
        }
        btnDetails.setOnClickListener {
            val intent = Intent(this, EligibleStudentsActivity::class.java)
            intent.putExtra("driveId", driveId)
            intent.putExtra("companyName", company)
            startActivity(intent)
        }

        val btnTrack = MaterialButton(this).apply {
            text = "Track"; textSize = 11f; isAllCaps = false; cornerRadius = dp(16)
            setBackgroundColor(Color.parseColor("#00BCD4")); setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(36)
            )
        }
        btnTrack.setOnClickListener {
            val intent = Intent(this, EligibleStudentsActivity::class.java)
            intent.putExtra("driveId", driveId)
            intent.putExtra("companyName", company)
            startActivity(intent)
        }

        btnRow.addView(btnDetails)
        btnRow.addView(btnTrack)
        outer.addView(btnRow)

        card.addView(outer)
        container.addView(card)
    }

    // ========== RECENT UPDATES ==========
    private fun addRecentUpdatesSection() {
        val card = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = dp(16).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16); bottomMargin = dp(16) }
        }
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        inner.addView(TextView(this).apply {
            text = "📌  Recent Updates"; textSize = 16f
            setTextColor(Color.WHITE); setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, dp(12))
        })
        val updates = listOf(
            Triple("🎉", "Drive created successfully", "New placement drive added"),
            Triple("📢", "New Drive: Check dashboard", "View eligible students for each drive"),
            Triple("📊", "Stats updated in real-time", "Track placement progress live")
        )
        updates.forEach { (icon, title, sub) ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8), 0, dp(8))
            }
            row.addView(TextView(this).apply {
                text = icon; textSize = 18f; gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(32), dp(32))
            })
            val col = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) }
            }
            col.addView(TextView(this).apply {
                text = title; textSize = 13f; setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
            })
            col.addView(TextView(this).apply {
                text = sub; textSize = 11f; setTextColor(Color.parseColor("#8B949E"))
            })
            row.addView(col)
            inner.addView(row)
        }
        card.addView(inner)
        container.addView(card)
    }

    // ========== LOGOUT ==========
    private fun addLogoutButton() {
        val btn = MaterialButton(this).apply {
            text = "Logout"
            setBackgroundColor(resources.getColor(R.color.error, null))
            cornerRadius = dp(12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
            ).apply { topMargin = dp(8); bottomMargin = dp(32) }
        }
        btn.setOnClickListener {
            session.logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
        container.addView(btn)
    }
}

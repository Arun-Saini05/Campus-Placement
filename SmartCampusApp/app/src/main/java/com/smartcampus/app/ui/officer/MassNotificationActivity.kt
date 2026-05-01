package com.smartcampus.app.ui.officer

import android.content.res.ColorStateList
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
import com.smartcampus.app.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MassNotificationActivity : AppCompatActivity() {
    private lateinit var session: SessionManager
    private lateinit var container: LinearLayout
    private var selectedAudience: String = "ALL"

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic)
        session = SessionManager(this)
        
        findViewById<TextView>(R.id.tvPageTitle).text = "Global Broadcast"
        container = findViewById(R.id.layoutContent)
        
        setupProfessionalForm()
    }

    private fun setupProfessionalForm() {
        // Info Card
        val infoCard = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#122529"))
            radius = dp(12).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(20) }
        }
        val infoInner = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        infoInner.addView(TextView(this).apply { text = "📢"; textSize = 24f })
        val infoTextCol = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(12) }
        }
        infoTextCol.addView(TextView(this).apply { text = "Important Notification"; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD); textSize = 14f })
        infoTextCol.addView(TextView(this).apply { text = "Broadcast updates, schedules, or alerts to students instantly."; setTextColor(Color.parseColor("#8B949E")); textSize = 11f })
        infoInner.addView(infoTextCol)
        infoCard.addView(infoInner)
        container.addView(infoCard)

        // Audience Section
        container.addView(TextView(this).apply { 
            text = "TARGET AUDIENCE"; textSize = 11f; setTextColor(Color.parseColor("#8B949E"))
            setTypeface(null, Typeface.BOLD); setPadding(dp(4), 0, 0, dp(12))
        })
        
        val horizontalScroll = HorizontalScrollView(this).apply { 
            isFillViewport = true
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(24) }
        }
        val chipGroup = ChipGroup(this).apply { 
            isSingleSelection = true
            isSelectionRequired = true
        }
        val audiences = arrayOf("ALL", "PLACED", "UNPLACED", "Computer Science", "Information Tech", "ECE", "Mechanical")
        audiences.forEach { audience ->
            val chip = Chip(this).apply {
                text = audience
                isCheckable = true
                isChecked = audience == "ALL"
                setTextColor(Color.WHITE)
                chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#161B22"))
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#30363D"))
                chipStrokeWidth = dp(1).toFloat()
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedAudience = audience
                        chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#6C63FF"))
                    } else {
                        chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#161B22"))
                    }
                }
            }
            chipGroup.addView(chip)
        }
        horizontalScroll.addView(chipGroup)
        container.addView(horizontalScroll)

        // Form Section
        val formCard = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = dp(20).toFloat()
            setContentPadding(dp(20), dp(24), dp(20), dp(24))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val formInner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val tilTitle = createInputLayout("Announcement Subject")
        val etTitle = TextInputEditText(tilTitle.context).apply {
            setTextColor(Color.WHITE)
            textSize = 15f
        }
        tilTitle.placeholderText = "e.g. New Placement Drive Alert"
        tilTitle.addView(etTitle)
        formInner.addView(tilTitle)

        val tilMessage = createInputLayout("Detailed Message").apply { 
            layoutParams = (layoutParams as LinearLayout.LayoutParams).apply { topMargin = dp(16) }
        }
        val etMessage = TextInputEditText(tilMessage.context).apply {
            setTextColor(Color.WHITE)
            textSize = 14f
            minLines = 5
            gravity = Gravity.TOP
        }
        tilMessage.placeholderText = "Type the details of your announcement here..."
        tilMessage.addView(etMessage)
        formInner.addView(tilMessage)

        val btnSend = MaterialButton(this).apply {
            text = "Broadcast Now"; textSize = 15f; isAllCaps = false
            cornerRadius = dp(24); setBackgroundColor(Color.parseColor("#F0883E"))
            setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56)
            ).apply { topMargin = dp(32) }
        }
        btnSend.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val message = etMessage.text.toString().trim()
            if (title.isBlank() || message.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnSend.isEnabled = false
            btnSend.text = "Broadcasting..."
            sendNotification(title, message, btnSend)
        }
        formInner.addView(btnSend)

        formCard.addView(formInner)
        container.addView(formCard)

        // Cancel Button
        val btnCancel = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "Cancel Broadcast"; textSize = 13f; isAllCaps = false
            cornerRadius = dp(24); setTextColor(Color.parseColor("#8B949E"))
            strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#30363D"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
            ).apply { topMargin = dp(20); bottomMargin = dp(40) }
            setOnClickListener { finish() }
        }
        container.addView(btnCancel)
    }

    private fun createInputLayout(label: String): TextInputLayout {
        return TextInputLayout(this).apply {
            hint = label
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxStrokeColorStateList(android.content.res.ColorStateList.valueOf(Color.parseColor("#6C63FF")))
            setHintTextColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#8B949E")))
            setBoxCornerRadii(dp(12).toFloat(), dp(12).toFloat(), dp(12).toFloat(), dp(12).toFloat())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun sendNotification(title: String, message: String, button: MaterialButton) {
        val body = mapOf(
            "title" to title,
            "message" to message,
            "targetAudience" to selectedAudience,
            "type" to "ANNOUNCEMENT"
        )

        ApiClient.getApi().sendMassNotification(session.authToken, body)
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MassNotificationActivity, "Broadcast Successful!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        button.isEnabled = true; button.text = "Broadcast Now"
                        Toast.makeText(this@MassNotificationActivity, "Broadcast Failed", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    button.isEnabled = true; button.text = "Broadcast Now"
                    Toast.makeText(this@MassNotificationActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

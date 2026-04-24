package com.smartcampus.app.ui.officer

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.smartcampus.app.R
import com.smartcampus.app.api.ApiClient
import com.smartcampus.app.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
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
        
        findViewById<TextView>(R.id.tvPageTitle).text = "Mass Announcement"
        container = findViewById(R.id.layoutContent)
        
        setupForm()
    }

    private fun setupForm() {
        val card = MaterialCardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = dp(16).toFloat()
            setContentPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // Title Input
        inner.addView(TextView(this).apply { text = "Announcement Title"; setTextColor(Color.parseColor("#8B949E")); textSize = 12f })
        val etTitle = EditText(this).apply {
            hint = "e.g. TCS Interview Schedule"
            setHintTextColor(Color.parseColor("#484F58"))
            setTextColor(Color.WHITE)
            background = null
            setPadding(0, dp(8), 0, dp(16))
        }
        inner.addView(etTitle)

        // Message Input
        inner.addView(TextView(this).apply { text = "Detailed Message"; setTextColor(Color.parseColor("#8B949E")); textSize = 12f })
        val etMessage = EditText(this).apply {
            hint = "Provide all necessary details here..."
            setHintTextColor(Color.parseColor("#484F58"))
            setTextColor(Color.WHITE)
            minLines = 4
            gravity = android.view.Gravity.TOP
            background = null
            setPadding(0, dp(8), 0, dp(16))
        }
        inner.addView(etMessage)

        // Audience Selector
        inner.addView(TextView(this).apply { text = "Target Audience"; setTextColor(Color.parseColor("#8B949E")); textSize = 12f })
        val audiences = arrayOf("ALL", "PLACED", "UNPLACED", "Computer Science", "Electrical Engineering", "Mechanical Engineering")
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MassNotificationActivity, android.R.layout.simple_spinner_dropdown_item, audiences)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(24) }
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedAudience = audiences[position]
                (view as? TextView)?.setTextColor(Color.WHITE)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        inner.addView(spinner)

        // Send Button
        val btnSend = MaterialButton(this).apply {
            text = "Broadcast Announcement"
            setBackgroundColor(Color.parseColor("#FF9800"))
            setTextColor(Color.WHITE)
            cornerRadius = dp(24)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48))
        }
        btnSend.setOnClickListener {
            val title = etTitle.text.toString()
            val message = etMessage.text.toString()
            
            if (title.isBlank() || message.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            sendNotification(title, message)
        }
        inner.addView(btnSend)

        card.addView(inner)
        container.addView(card)
    }

    private fun sendNotification(title: String, message: String) {
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
                        Toast.makeText(this@MassNotificationActivity, "Announcement Broadcasted!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@MassNotificationActivity, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(this@MassNotificationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

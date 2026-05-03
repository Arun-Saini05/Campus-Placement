package com.smartcampus.app.ui.notifications

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

class NotificationCenterActivity : AppCompatActivity() {
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic)
        session = SessionManager(this)
        findViewById<TextView>(R.id.tvPageTitle).text = "Notifications"

        val container = findViewById<LinearLayout>(R.id.layoutContent)

        // Mark all as read button
        val btnMarkAll = MaterialButton(this).apply {
            text = "Mark All as Read"
            setBackgroundColor(resources.getColor(R.color.primary, null))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        btnMarkAll.setOnClickListener {
            ApiClient.getApi().markAllAsRead(session.authToken).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Toast.makeText(this@NotificationCenterActivity, "All marked as read", Toast.LENGTH_SHORT).show()
                    loadNotifications(container)
                }
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {}
            })
        }
        container.addView(btnMarkAll)

        loadNotifications(container)
    }

    private fun loadNotifications(container: LinearLayout) {
        ApiClient.getApi().getNotifications(session.authToken).enqueue(object : Callback<List<JsonObject>> {
            override fun onResponse(call: Call<List<JsonObject>>, response: Response<List<JsonObject>>) {
                if (response.isSuccessful && response.body() != null) {
                    // Remove old notifications but keep the button
                    while (container.childCount > 1) container.removeViewAt(1)

                    if (response.body()!!.isEmpty()) {
                        container.addView(TextView(this@NotificationCenterActivity).apply {
                            text = "No notifications"
                            textSize = 16f
                            setTextColor(resources.getColor(R.color.text_secondary, null))
                        })
                    }

                    response.body()!!.forEach { notif ->
                        val id = notif.get("id")?.asInt ?: 0
                        val title = notif.get("title")?.asString ?: "Notification"
                        val message = notif.get("message")?.asString ?: ""
                        val isRead = notif.get("isRead")?.asBoolean ?: false
                        
                        val card = MaterialCardView(this@NotificationCenterActivity).apply {
                            setCardBackgroundColor(resources.getColor(
                                if (isRead) R.color.bg_card else R.color.bg_card_light, null))
                            radius = 12f
                            strokeWidth = if (isRead) 0 else 2
                            strokeColor = resources.getColor(R.color.primary, null)
                            setContentPadding(24, 20, 24, 20)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { bottomMargin = 8 }
                            isClickable = true
                            isFocusable = true
                        }
                        
                        card.setOnClickListener {
                            // Show Detail Dialog
                            com.google.android.material.dialog.MaterialAlertDialogBuilder(this@NotificationCenterActivity)
                                .setTitle(title)
                                .setMessage(message)
                                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                                .show()

                            // Mark as read in backend
                            if (!isRead) {
                                ApiClient.getApi().markAsRead(session.authToken, id).enqueue(object : Callback<JsonObject> {
                                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                                        loadNotifications(container) // Refresh list
                                    }
                                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {}
                                })
                            }
                        }

                        val inner = LinearLayout(this@NotificationCenterActivity).apply { orientation = LinearLayout.VERTICAL }
                        inner.addView(TextView(this@NotificationCenterActivity).apply {
                            text = title
                            textSize = 15f
                            setTextColor(resources.getColor(R.color.text_primary, null))
                            setTypeface(typeface, if (isRead) android.graphics.Typeface.NORMAL else android.graphics.Typeface.BOLD)
                        })
                        inner.addView(TextView(this@NotificationCenterActivity).apply {
                            text = message
                            textSize = 13f
                            maxLines = 1
                            ellipsize = android.text.TextUtils.TruncateAt.END
                            setTextColor(resources.getColor(R.color.text_secondary, null))
                            setPadding(0, 4, 0, 0)
                        })
                        card.addView(inner)
                        container.addView(card)
                    }
                }
            }
            override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) {
                Toast.makeText(this@NotificationCenterActivity, "Error loading notifications", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

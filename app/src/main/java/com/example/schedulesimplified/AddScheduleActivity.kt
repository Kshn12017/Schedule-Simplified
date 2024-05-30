package com.example.schedulesimplified

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class AddScheduleActivity : AppCompatActivity() {

    private lateinit var timeEditText: TextInputEditText
    private lateinit var activityEditText: TextInputEditText
    private lateinit var sharedPreferences: SharedPreferences
    private val scheduleKeyPrefix = "schedule_key_"
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_schedule)

        timeEditText = findViewById(R.id.timeEditText)
        activityEditText = findViewById(R.id.activityEditText)
        val saveButton: MaterialButton = findViewById(R.id.saveButton)

        sharedPreferences = getSharedPreferences("SchedulePrefs", Context.MODE_PRIVATE)

        selectedDate = intent.getStringExtra("SELECTED_DATE").toString()

        saveButton.setOnClickListener {
            val time = timeEditText.text.toString()
            val activity = activityEditText.text.toString()
            if (time.isNotEmpty() && activity.isNotEmpty()) {
                if (isValidTimeFormat(time)) {
                    val scheduleItem = "$time - $activity"
                    saveSchedule(scheduleItem)
                    // Close this activity and return to ScheduleActivity
                    finish()
                } else {
                    Toast.makeText(this, "Invalid time format. Use HH:mm", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isValidTimeFormat(time: String): Boolean {
        return try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeFormat.parse(time)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun saveSchedule(scheduleItem: String) {
        val editor = sharedPreferences.edit()
        val set = sharedPreferences.getStringSet(scheduleKeyPrefix + selectedDate, HashSet()) ?: HashSet()
        set.add(scheduleItem)
        editor.putStringSet(scheduleKeyPrefix + selectedDate, set)
        editor.apply()
    }
}
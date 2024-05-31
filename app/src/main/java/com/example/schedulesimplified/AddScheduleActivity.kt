package com.example.schedulesimplified

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AddScheduleActivity : AppCompatActivity() {

    private lateinit var timeEditText: TextInputEditText
    private lateinit var activityEditText: TextInputEditText
    private lateinit var sharedPreferences: SharedPreferences
    private val scheduleKeyPrefix = "schedule_key_"
    private var selectedDate: String = ""

    private lateinit var requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_schedule)

        timeEditText = findViewById(R.id.timeEditText)
        activityEditText = findViewById(R.id.activityEditText)
        val saveButton: MaterialButton = findViewById(R.id.saveButton)

        sharedPreferences = getSharedPreferences("SchedulePrefs", Context.MODE_PRIVATE)

        selectedDate = intent.getStringExtra("SELECTED_DATE").toString()

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                val time = timeEditText.text.toString()
                val activity = activityEditText.text.toString()
                if (canScheduleExactAlarms()) {
                    setAlarm(time, activity)
                    finish()
                } else {
                    requestExactAlarmPermission()
                }
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        saveButton.setOnClickListener {
            val time = timeEditText.text.toString()
            val activity = activityEditText.text.toString()
            if (time.isNotEmpty() && activity.isNotEmpty()) {
                if (isValidTimeFormat(time)) {
                    val scheduleItem = "$time - $activity"
                    saveSchedule(scheduleItem)
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestNotificationPermission(time, activity)
                    } else {
                        if (canScheduleExactAlarms()) {
                            setAlarm(time, activity)
                            finish()
                        } else {
                            requestExactAlarmPermission()
                        }
                    }
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

    private fun setAlarm(time: String, activity: String) {
        try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val calendar = Calendar.getInstance().apply {
                val dateParts = selectedDate.split("-")
                set(Calendar.YEAR, dateParts[0].toInt())
                set(Calendar.MONTH, dateParts[1].toInt() - 1) // Month is 0-based
                set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
                val parsedTime = timeFormat.parse(time)
                set(Calendar.HOUR_OF_DAY, parsedTime.hours)
                set(Calendar.MINUTE, parsedTime.minutes)
                set(Calendar.SECOND, 0)
            }

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("NOTIFICATION_ID", System.currentTimeMillis().toInt())
                putExtra("TITLE", "Schedule Reminder")
                putExtra("MESSAGE", "Time for your activity: $activity")
            }

            val pendingIntent = PendingIntent.getBroadcast(this, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission required to set exact alarms.", Toast.LENGTH_SHORT).show()
            requestExactAlarmPermission()
        }
    }

    private fun requestNotificationPermission(time: String, activity: String) {
        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun canScheduleExactAlarms(): Boolean {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }
    }
}
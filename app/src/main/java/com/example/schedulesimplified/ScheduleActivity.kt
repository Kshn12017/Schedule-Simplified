package com.example.schedulesimplified

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.CalendarView
import android.widget.ListView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class ScheduleActivity : AppCompatActivity() {

    private lateinit var scheduleListView: ListView
    private lateinit var adapter: ScheduleAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val scheduleKeyPrefix = "schedule_key_"
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        sharedPreferences = getSharedPreferences("SchedulePrefs", Context.MODE_PRIVATE)
        scheduleListView = findViewById(R.id.scheduleListView)
        val addButton: FloatingActionButton = findViewById(R.id.addButton)
        val calendarView: CalendarView = findViewById(R.id.calendarView)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
            loadSchedule()
        }

        adapter = ScheduleAdapter(this, mutableListOf())
        scheduleListView.adapter = adapter

        addButton.setOnClickListener {
            val intent = Intent(this, AddScheduleActivity::class.java)
            intent.putExtra("SELECTED_DATE", selectedDate)
            startActivity(intent)
        }
    }

    private fun saveSchedule(schedule: List<String>) {
        val editor = sharedPreferences.edit()
        val set: Set<String> = HashSet(schedule)
        editor.putStringSet(scheduleKeyPrefix + selectedDate, set)
        editor.apply()
    }

    private fun loadSchedule() {
        val set = sharedPreferences.getStringSet(scheduleKeyPrefix + selectedDate, HashSet()) ?: HashSet()
        val scheduleList = set.toMutableList()
        scheduleList.sortWith(Comparator { o1, o2 ->
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            try {
                val time1 = timeFormat.parse(o1.split(" - ")[0])
                val time2 = timeFormat.parse(o2.split(" - ")[0])
                time1.compareTo(time2)
            } catch (e: Exception) {
                0
            }
        })
        adapter.updateSchedule(scheduleList)
    }

    override fun onResume() {
        super.onResume()
        if (selectedDate.isNotEmpty()) {
            loadSchedule()
        }
    }
}
package com.example.schedulesimplified

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ScheduleAdapter(private val context: Context, private var scheduleList: List<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return scheduleList.size
    }

    override fun getItem(position: Int): Any {
        return scheduleList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.schedule_item, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val scheduleItem = scheduleList[position]
        val parts = scheduleItem.split(" - ")
        holder.timeTextView.text = parts[0]
        holder.activityTextView.text = parts[1]

        return view
    }

    fun updateSchedule(newScheduleList: List<String>) {
        scheduleList = newScheduleList
        notifyDataSetChanged()
    }

    private class ViewHolder(view: View) {
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val activityTextView: TextView = view.findViewById(R.id.activityTextView)
    }
}

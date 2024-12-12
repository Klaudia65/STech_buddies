package com.example.stech_buddies

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class StatsFragment : Fragment() {

    private lateinit var currentMonthProgressBar: ProgressBar
    private lateinit var currentMonthPercentage: TextView
    private lateinit var currentMonthMinutes: TextView
    private lateinit var motivationalText: TextView
    private lateinit var monthlyGoalInput: EditText
    private lateinit var setGoalButton: Button
    private lateinit var pastMonthsTitle: TextView
    private lateinit var month1Label: TextView
    private lateinit var month2Label: TextView
    private lateinit var month3Label: TextView
    private lateinit var month1Progress: ProgressBar
    private lateinit var month2Progress: ProgressBar
    private lateinit var month3Progress: ProgressBar

    private var monthlyGoal = 3 * 60 * 60 * 1000L // Default 3 hours in milliseconds

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_stats, container, false)

        // Initialize Views
        currentMonthProgressBar = rootView.findViewById(R.id.current_month_progress_bar)
        currentMonthPercentage = rootView.findViewById(R.id.current_month_percentage)
        currentMonthMinutes = rootView.findViewById(R.id.current_month_minutes)
        motivationalText = rootView.findViewById(R.id.motivational_text)
        monthlyGoalInput = rootView.findViewById(R.id.monthly_goal_input)
        setGoalButton = rootView.findViewById(R.id.set_goal_button)
        pastMonthsTitle = rootView.findViewById(R.id.past_months_title)
        month1Label = rootView.findViewById(R.id.month1_label)
        month2Label = rootView.findViewById(R.id.month2_label)
        month3Label = rootView.findViewById(R.id.month3_label)
        month1Progress = rootView.findViewById(R.id.month1_progress)
        month2Progress = rootView.findViewById(R.id.month2_progress)
        month3Progress = rootView.findViewById(R.id.month3_progress)

        // Set Goal Button
        setGoalButton.setOnClickListener {
            val goalInput = monthlyGoalInput.text.toString()
            if (goalInput.isNotEmpty()) {
                monthlyGoal = goalInput.toLong() * 60 * 60 * 1000L
                updateStats()
            }
        }

        updateStats()

        return rootView
    }

    private fun updateStats() {
        val sharedPreferences = requireContext().getSharedPreferences("Stats", Context.MODE_PRIVATE)
        val totalStudyTime = sharedPreferences.getLong("studyTime", 0L)

        // Update Current Month Progress
        val currentProgress = ((totalStudyTime.toDouble() / monthlyGoal) * 100).toInt()
        currentMonthProgressBar.progress = currentProgress.coerceAtMost(100)
        currentMonthPercentage.text = if (currentProgress > 100) {
            "$currentProgress% (Over Goal)"
        } else {
            "$currentProgress%"
        }

        val totalMinutes = (totalStudyTime / (1000 * 60)).toInt()
        currentMonthMinutes.text = "$totalMinutes minutes"

        // Update Motivational Text
        motivationalText.text = when {
            currentProgress < 20 -> "You're slacking, study more!"
            currentProgress in 20..49 -> "Not quite there, keep going buddy!"
            currentProgress in 50..79 -> "Halfway there, keep going!"
            currentProgress in 80..99 -> "You're almost there, keep going for a bit!!"
            currentProgress >= 100 -> "You have completed your goal! \nYou can change the goal or stop studying for this month!!!"
            else -> ""
        }

        // Fake Data for Past Months
        val fakeData = listOf(
            Pair(1 * 60 * 60 * 1000L, 33), // 33% completion
            Pair(2 * 60 * 60 * 1000L, 67), // 67% completion
            Pair(3 * 60 * 60 * 1000L, 100) // 100% completion
        )

        val calendar = Calendar.getInstance()
        month1Label.text = "${getMonthName(calendar, -1)} (${fakeData[0].second}%)"
        updateMonthProgress(month1Progress, fakeData[0].first)

        month2Label.text = "${getMonthName(calendar, -2)} (${fakeData[1].second}%)"
        updateMonthProgress(month2Progress, fakeData[1].first)

        month3Label.text = "${getMonthName(calendar, -3)} (${fakeData[2].second}%)"
        updateMonthProgress(month3Progress, fakeData[2].first)
    }

    private fun updateMonthProgress(progressBar: ProgressBar, studyTime: Long) {
        val progress = ((studyTime.toDouble() / monthlyGoal) * 100).toInt()
        progressBar.progress = progress.coerceAtMost(100) // Cap at 100%
    }

    private fun getMonthName(calendar: Calendar, offset: Int): String {
        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.add(Calendar.MONTH, offset)
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(tempCalendar.time)
    }
}

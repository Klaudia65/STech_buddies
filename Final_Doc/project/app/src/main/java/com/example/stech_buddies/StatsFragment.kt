package com.example.stech_buddies

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

    // UI elements for displaying and managing statistics
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

    private var monthlyGoal = 3 * 60 * 60 * 1000L // Default goal: 3 hours in milliseconds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_stats, container, false)

        // Bind UI elements
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

        // Handle goal setting
        setGoalButton.setOnClickListener {
            val goalInput = monthlyGoalInput.text.toString()
            if (goalInput.isNotEmpty()) {
                monthlyGoal = goalInput.toLong() * 60 * 60 * 1000L // Convert hours to milliseconds
                updateStats() // Refresh stats after updating the goal
            }
        }

        updateStats() // Initial update of statistics
        return rootView
    }

    private fun updateStats() {
        val sharedPreferences = requireContext().getSharedPreferences("Stats", Context.MODE_PRIVATE)
        val totalStudyTime = sharedPreferences.getLong("studyTime", 0L) // Retrieve saved study time

        // Calculate and display current month's progress
        val currentProgress = ((totalStudyTime.toDouble() / monthlyGoal) * 100).toInt()
        currentMonthProgressBar.progress = currentProgress.coerceAtMost(100) // Limit progress to 100%
        currentMonthPercentage.text = if (currentProgress > 100) {
            "$currentProgress% (Over Goal)"
        } else {
            "$currentProgress%"
        }

        val totalMinutes = (totalStudyTime / (1000 * 60)).toInt() // Convert milliseconds to minutes
        currentMonthMinutes.text = "$totalMinutes minutes"

        // Set motivational message based on progress
        motivationalText.text = when {
            currentProgress < 20 -> "You're slacking, study more!"
            currentProgress in 20..49 -> "Not quite there, keep going buddy!"
            currentProgress in 50..79 -> "Halfway there, keep going!"
            currentProgress in 80..99 -> "You're almost there, keep going for a bit!!"
            currentProgress >= 100 -> "You have completed your goal! \nYou can change the goal or stop studying for this month!!!"
            else -> ""
        }

        // Mock data for past months' progress
        val fakeData = listOf(
            Pair(1 * 60 * 60 * 1000L, 33), // Example: 33% of a 3-hour goal
            Pair(2 * 60 * 60 * 1000L, 67), // Example: 67% of a 3-hour goal
            Pair(3 * 60 * 60 * 1000L, 100) // Example: Fully completed
        )

        // Update progress for the last three months
        val calendar = Calendar.getInstance()
        month1Label.text = "${getMonthName(calendar, -1)} (${fakeData[0].second}%)"
        updateMonthProgress(month1Progress, fakeData[0].first)

        month2Label.text = "${getMonthName(calendar, -2)} (${fakeData[1].second}%)"
        updateMonthProgress(month2Progress, fakeData[1].first)

        month3Label.text = "${getMonthName(calendar, -3)} (${fakeData[2].second}%)"
        updateMonthProgress(month3Progress, fakeData[2].first)
    }

    // Updates progress bar for a given month's progress
    private fun updateMonthProgress(progressBar: ProgressBar, studyTime: Long) {
        val progress = ((studyTime.toDouble() / monthlyGoal) * 100).toInt()
        progressBar.progress = progress.coerceAtMost(100) // Cap progress at 100%
    }

    // Returns the name of month based on an offset from current month
    private fun getMonthName(calendar: Calendar, offset: Int): String {
        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.add(Calendar.MONTH, offset)
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(tempCalendar.time)
    }
}

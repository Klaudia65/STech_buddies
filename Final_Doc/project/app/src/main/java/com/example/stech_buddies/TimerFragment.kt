package com.example.stech_buddies

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class TimerFragment : Fragment() {

    // Timer controls and settings
    private lateinit var timerText: TextView
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var resetButton: Button
    private lateinit var presetStandardButton: Button
    private lateinit var presetShortBreakButton: Button
    private lateinit var presetCustomButton: Button
    private lateinit var soundToggle: CheckBox
    private lateinit var vibrationToggle: CheckBox
    private lateinit var studyTimeInput: EditText
    private lateinit var breakTimeInput: EditText
    private lateinit var progressCircular: ProgressBar

    // Timer state and configuration
    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var isStudyTime = true
    private var studyTimeInMillis = 25 * 60 * 1000L // Default study time: 25 minutes
    private var breakTimeInMillis = 5 * 60 * 1000L // Default break time: 5 minutes
    private var timeLeftInMillis = studyTimeInMillis // Tracks remaining time for the current session
    private var studyTimeSpent = 0L // Total accumulated study time in milliseconds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_timer, container, false)

        // Bind UI elements
        timerText = rootView.findViewById(R.id.timer_text)
        startButton = rootView.findViewById(R.id.start_button)
        pauseButton = rootView.findViewById(R.id.pause_button)
        resetButton = rootView.findViewById(R.id.reset_button)
        presetStandardButton = rootView.findViewById(R.id.preset_standard)
        presetShortBreakButton = rootView.findViewById(R.id.preset_short_break)
        presetCustomButton = rootView.findViewById(R.id.preset_custom)
        soundToggle = rootView.findViewById(R.id.sound_toggle)
        vibrationToggle = rootView.findViewById(R.id.vibration_toggle)
        studyTimeInput = rootView.findViewById(R.id.study_time_input)
        breakTimeInput = rootView.findViewById(R.id.break_time_input)
        progressCircular = rootView.findViewById(R.id.progress_circular)

        // Set up button actions
        startButton.setOnClickListener { startTimer() }
        pauseButton.setOnClickListener { pauseTimer() }
        resetButton.setOnClickListener { resetTimer() }
        presetStandardButton.setOnClickListener { applyPreset(25, 5) } // Default study and break times
        presetShortBreakButton.setOnClickListener { applyPreset(0, 5) } // Only a break
        presetCustomButton.setOnClickListener { applyCustomPreset() } // Custom times

        restoreTimerState()
        updateTimerText()

        return rootView
    }

    override fun onPause() {
        super.onPause()
        saveTimerState() // Save timer state when fragment is paused
    }

    override fun onResume() {
        super.onResume()
        restoreTimerState() // Restore timer state when fragment is resumed
        updateTimerText()
    }

    // Starts countdown timer
    private fun startTimer() {
        if (!isRunning) {
            progressCircular.max = (timeLeftInMillis / 1000).toInt()
            countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftInMillis = millisUntilFinished
                    updateTimerText()
                    progressCircular.progress = (timeLeftInMillis / 1000).toInt()
                }

                override fun onFinish() {
                    isRunning = false
                    if (isStudyTime) {
                        studyTimeSpent += studyTimeInMillis
                        saveStudyTime() // Save study time upon session completion
                    }
                    notifyTimerEnd() // Notify user that timer has ended
                    isStudyTime = !isStudyTime // Switch between study and break times
                    timeLeftInMillis = if (isStudyTime) studyTimeInMillis else breakTimeInMillis
                    updateTimerText()
                    startTimer() // Automatically start next session
                }
            }.start()
            isRunning = true
        }
    }

    // Pauses timer
    private fun pauseTimer() {
        if (isRunning) {
            countDownTimer?.cancel()
            isRunning = false
        }
    }

    // Resets timer to the default or current session's initial state
    private fun resetTimer() {
        countDownTimer?.cancel()
        isRunning = false
        timeLeftInMillis = if (isStudyTime) studyTimeInMillis else breakTimeInMillis
        updateTimerText()
        progressCircular.progress = (timeLeftInMillis / 1000).toInt()
    }

    // Applies preset times for study and break sessions
    private fun applyPreset(studyTime: Int, breakTime: Int) {
        studyTimeInMillis = studyTime * 60 * 1000L
        breakTimeInMillis = breakTime * 60 * 1000L
        timeLeftInMillis = studyTimeInMillis
        resetTimer()
    }

    // Allows user to set custom times
    private fun applyCustomPreset() {
        studyTimeInMillis = getInputTime(studyTimeInput, 25) * 60 * 1000L
        breakTimeInMillis = getInputTime(breakTimeInput, 5) * 60 * 1000L
        timeLeftInMillis = if (isStudyTime) studyTimeInMillis else breakTimeInMillis
        resetTimer()
    }

    // Updates timer text display
    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    // Provides notification when timer ends
    private fun notifyTimerEnd() {
        if (vibrationToggle.isChecked) {
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(500) // Vibrates for 500ms
        }
        if (soundToggle.isChecked) {
            val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.timer_end_sound)
            mediaPlayer.start() // Plays end sound
        }
    }

    // Saves accumulated study time in shared preferences
    private fun saveStudyTime() {
        val sharedPreferences = requireContext().getSharedPreferences("Stats", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val previousTime = sharedPreferences.getLong("studyTime", 0L)
        editor.putLong("studyTime", previousTime + studyTimeSpent)
        editor.apply()
    }

    // Retrieves input time from EditText fields, with a default fallback
    private fun getInputTime(inputField: EditText, defaultTime: Int): Int {
        val inputText = inputField.text.toString()
        return if (inputText.isNotEmpty()) inputText.toInt() else defaultTime
    }

    // Saves current timer state to shared preferences
    private fun saveTimerState() {
        val sharedPreferences = requireContext().getSharedPreferences("TimerState", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong("timeLeft", timeLeftInMillis)
        editor.putBoolean("isStudyTime", isStudyTime)
        editor.putBoolean("isRunning", isRunning)
        editor.apply()
    }

    // Restores timer state from shared preferences
    private fun restoreTimerState() {
        val sharedPreferences = requireContext().getSharedPreferences("TimerState", Context.MODE_PRIVATE)
        timeLeftInMillis = sharedPreferences.getLong("timeLeft", studyTimeInMillis)
        isStudyTime = sharedPreferences.getBoolean("isStudyTime", true)
        isRunning = sharedPreferences.getBoolean("isRunning", false)

        if (isRunning) {
            startTimer() // Resume timer if it was running previously
        }
    }
}

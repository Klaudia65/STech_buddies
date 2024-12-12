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

    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var isStudyTime = true
    private var studyTimeInMillis = 25 * 60 * 1000L // Default 25 minutes
    private var breakTimeInMillis = 5 * 60 * 1000L // Default 5 minutes
    private var timeLeftInMillis = studyTimeInMillis // Time left for the current session

    private var studyTimeSpent = 0L // Total study time spent in milliseconds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_timer, container, false)

        // Initialize Views
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

        // Button Click Listeners
        startButton.setOnClickListener { startTimer() }
        pauseButton.setOnClickListener { pauseTimer() }
        resetButton.setOnClickListener { resetTimer() }
        presetStandardButton.setOnClickListener { applyPreset(25, 5) }
        presetShortBreakButton.setOnClickListener { applyPreset(0, 5) }
        presetCustomButton.setOnClickListener { applyCustomPreset() }

        restoreTimerState()
        updateTimerText()

        return rootView
    }

    override fun onPause() {
        super.onPause()
        saveTimerState()
    }

    override fun onResume() {
        super.onResume()
        restoreTimerState()
        updateTimerText()
    }

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
                        saveStudyTime()
                    }
                    notifyTimerEnd()
                    isStudyTime = !isStudyTime
                    timeLeftInMillis = if (isStudyTime) studyTimeInMillis else breakTimeInMillis
                    updateTimerText()
                    startTimer()
                }
            }.start()
            isRunning = true
        }
    }

    private fun pauseTimer() {
        if (isRunning) {
            countDownTimer?.cancel()
            isRunning = false
        }
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isRunning = false
        timeLeftInMillis = if (isStudyTime) studyTimeInMillis else breakTimeInMillis
        updateTimerText()
        progressCircular.progress = (timeLeftInMillis / 1000).toInt()
    }

    private fun applyPreset(studyTime: Int, breakTime: Int) {
        studyTimeInMillis = studyTime * 60 * 1000L
        breakTimeInMillis = breakTime * 60 * 1000L
        timeLeftInMillis = studyTimeInMillis
        resetTimer()
    }

    private fun applyCustomPreset() {
        studyTimeInMillis = getInputTime(studyTimeInput, 25) * 60 * 1000L
        breakTimeInMillis = getInputTime(breakTimeInput, 5) * 60 * 1000L
        timeLeftInMillis = if (isStudyTime) studyTimeInMillis else breakTimeInMillis
        resetTimer()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun notifyTimerEnd() {
        if (vibrationToggle.isChecked) {
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(500)
        }
        if (soundToggle.isChecked) {
            val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.timer_end_sound)
            mediaPlayer.start()
        }
    }

    private fun saveStudyTime() {
        val sharedPreferences = requireContext().getSharedPreferences("Stats", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val previousTime = sharedPreferences.getLong("studyTime", 0L)
        editor.putLong("studyTime", previousTime + studyTimeSpent)
        editor.apply()
    }

    private fun getInputTime(inputField: EditText, defaultTime: Int): Int {
        val inputText = inputField.text.toString()
        return if (inputText.isNotEmpty()) inputText.toInt() else defaultTime
    }

    private fun saveTimerState() {
        val sharedPreferences = requireContext().getSharedPreferences("TimerState", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong("timeLeft", timeLeftInMillis)
        editor.putBoolean("isStudyTime", isStudyTime)
        editor.putBoolean("isRunning", isRunning)
        editor.apply()
    }

    private fun restoreTimerState() {
        val sharedPreferences = requireContext().getSharedPreferences("TimerState", Context.MODE_PRIVATE)
        timeLeftInMillis = sharedPreferences.getLong("timeLeft", studyTimeInMillis)
        isStudyTime = sharedPreferences.getBoolean("isStudyTime", true)
        isRunning = sharedPreferences.getBoolean("isRunning", false)

        if (isRunning) {
            startTimer() // Resume the timer if it was running
        }
    }
}

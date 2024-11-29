package com.example.stech_buddies

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

class TimerFragment : Fragment() {

    private lateinit var timerText: TextView
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var resetButton: Button
    private lateinit var studyTimeInput: EditText
    private lateinit var breakTimeInput: EditText

    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var isStudyTime = true
    private var studyTimeInMillis = 25 * 60 * 1000L // Default 25 minutes
    private var breakTimeInMillis = 5 * 60 * 1000L // Default 5 minutes
    private var timeLeftInMillis = studyTimeInMillis // Time left for the current session

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_timer, container, false)

        timerText = rootView.findViewById(R.id.timer_text)
        startButton = rootView.findViewById(R.id.start_button)
        pauseButton = rootView.findViewById(R.id.pause_button)
        resetButton = rootView.findViewById(R.id.reset_button)
        studyTimeInput = rootView.findViewById(R.id.study_time_input)
        breakTimeInput = rootView.findViewById(R.id.break_time_input)

        startButton.setOnClickListener { startTimer() }
        pauseButton.setOnClickListener { pauseTimer() }
        resetButton.setOnClickListener { resetTimer() }

        return rootView
    }

    private fun startTimer() {
        if (!isRunning) {
            countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftInMillis = millisUntilFinished
                    updateTimerText()
                }

                override fun onFinish() {
                    isRunning = false
                    isStudyTime = !isStudyTime // Toggle between study and break
                    timeLeftInMillis = if (isStudyTime) studyTimeInMillis else breakTimeInMillis
                    updateTimerText()
                    startTimer() // Automatically start the next session
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

        // Update time based on input
        studyTimeInMillis = getInputTime(studyTimeInput, 25) * 60 * 1000L
        breakTimeInMillis = getInputTime(breakTimeInput, 5) * 60 * 1000L
        timeLeftInMillis = if (isStudyTime) studyTimeInMillis else breakTimeInMillis

        updateTimerText()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun getInputTime(inputField: EditText, defaultTime: Int): Int {
        val inputText = inputField.text.toString()
        return if (inputText.isNotEmpty()) inputText.toInt() else defaultTime
    }
}

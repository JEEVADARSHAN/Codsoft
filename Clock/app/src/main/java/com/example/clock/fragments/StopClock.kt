package com.example.clock.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clock.R
import java.util.concurrent.TimeUnit

class StopwatchFragment : Fragment() {

    private lateinit var chronometer: TextView
    private lateinit var startButton: ImageView
    private lateinit var stopButton: ImageView
    private lateinit var resetButton: ImageView
    private lateinit var flagButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimeAdapter

    private var isRunning = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val flaggedTimes = mutableListOf<Long>()

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTimeRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.stop_clock_scrn, container, false)

        chronometer = view.findViewById(R.id.chronometer)
        startButton = view.findViewById(R.id.start_button)
        stopButton = view.findViewById(R.id.stop_button)
        resetButton = view.findViewById(R.id.reset_button)
        flagButton = view.findViewById(R.id.flag_button)
        recyclerView = view.findViewById(R.id.recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TimeAdapter(flaggedTimes)
        recyclerView.adapter = adapter

        // Initially disable stopButton and resetButton
        disableButton(stopButton)
        disableButton(resetButton)
        flagButton.isEnabled = false
        flagButton.alpha = 0.0f

        startButton.setOnClickListener { startStopwatch() }
        stopButton.setOnClickListener { stopStopwatch() }
        resetButton.setOnClickListener { resetStopwatch() }
        flagButton.setOnClickListener { flagTime() }

        // Set initial button states
        toggleButtonEnabled(startButton, true)
        toggleButtonEnabled(stopButton, false)
        toggleButtonEnabled(resetButton, false)

        updateTimeRunnable = object : Runnable {
            override fun run() {
                updateChronometer()
                handler.postDelayed(this, INTERVAL_MILLISECONDS.toLong())
            }
        }

        return view
    }

    private fun startStopwatch() {
        if (!isRunning) {
            if (elapsedTime == 0L) {
                startTime = SystemClock.elapsedRealtime()
            } else {
                startTime = SystemClock.elapsedRealtime() - elapsedTime
            }

            handler.removeCallbacks(updateTimeRunnable)
            handler.postDelayed(updateTimeRunnable, INTERVAL_MILLISECONDS.toLong())
            isRunning = true
            toggleButtonEnabled(startButton, false)
            toggleButtonEnabled(stopButton, true)
            toggleButtonEnabled(resetButton, true)
            flagButton.isEnabled = true
            flagButton.alpha = 1.0f
        }
    }

    private fun stopStopwatch() {
        if (isRunning) {
            handler.removeCallbacks(updateTimeRunnable)
            elapsedTime = SystemClock.elapsedRealtime() - startTime
            isRunning = false
            toggleButtonEnabled(startButton, true)
            toggleButtonEnabled(stopButton, false)
            toggleButtonEnabled(resetButton, true)
            flagButton.isEnabled = true
            flagButton.alpha = 1.0f
        }
    }


    private fun resetStopwatch() {
        handler.removeCallbacks(updateTimeRunnable)
        elapsedTime = 0
        isRunning = false
        updateChronometer()
        toggleButtonEnabled(startButton, true)
        toggleButtonEnabled(stopButton, false)
        toggleButtonEnabled(resetButton, false)
        toggleButtonEnabled(flagButton, false)
        flagButton.isEnabled = false
        flagButton.alpha = 0.0f

        flaggedTimes.clear()
        adapter.notifyDataSetChanged()
    }

    private fun flagTime() {
        val currentTime = if (isRunning) {
            SystemClock.elapsedRealtime() - startTime
        } else {
            elapsedTime
        }
        flaggedTimes.add(currentTime)
        adapter.notifyDataSetChanged()
    }


    private fun updateChronometer() {
        val currentTime = if (isRunning) {
            SystemClock.elapsedRealtime() - startTime
        } else {
            elapsedTime
        }
        chronometer.text = formatElapsedTime(currentTime)
    }

    private fun formatElapsedTime(elapsedTime: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
        val milliseconds = elapsedTime % 1000 / 10

        // Format the time as MM:SS.SSS
        return String.format("%02d:%02d:%02d", minutes, seconds, milliseconds)
    }

    private fun toggleButtonEnabled(button: ImageView, isEnabled: Boolean) {
        if (isEnabled) {
            // Button is enabled
            button.isEnabled = true
            button.alpha = 1.0f // Full opacity (normal color)
        } else {
            // Button is disabled
            button.isEnabled = false
            button.alpha = 0.5f // Half opacity (grayed out)
        }
    }

    private fun disableButton(button: ImageView) {
        button.isEnabled = false
        button.alpha = 0.5f // Half opacity (grayed out)
    }

    inner class TimeAdapter(private val times: MutableList<Long>) :
        RecyclerView.Adapter<TimeAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.stop_time_view, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val timeInMillis = times[position]
            val formattedTime = formatElapsedTime(timeInMillis)
            holder.timeTextView.text = formattedTime

            holder.closeButton.setOnClickListener {
                times.removeAt(position)
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int {
            return times.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val timeTextView: TextView = itemView.findViewById(R.id.TimeText)
            val closeButton: ImageView = itemView.findViewById(R.id.closeButton)
        }
    }

    companion object {
        private const val INTERVAL_MILLISECONDS = 100 // Update every 100 milliseconds
    }
}

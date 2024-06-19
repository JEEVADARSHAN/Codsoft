package com.example.clock.fragments
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.clock.CircularSliderView
import com.example.clock.MainActivity
import com.example.clock.R

class TimerFragment : Fragment() {

    private lateinit var circularSliderView: CircularSliderView
    private lateinit var startButton: ImageView
    private lateinit var stopButton: ImageView
    private lateinit var resetButton: ImageView
    private lateinit var timerTextView: TextView

    private lateinit var countDownTimer: CountDownTimer
    private var isRunning = false
    private var timeLeftInMillis: Long = 0
    private var initialTimeInMillis: Long = 0
    private var notificationManager: NotificationManager? = null
    private val NOTIFICATION_ID = 0
    private val CHANNEL_ID = "TimerChannel"
    private lateinit var scaleUpAnimation: Animation
    private lateinit var scaleDownAnimation: Animation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.timer_scrn, container, false)

        scaleUpAnimation = AnimationUtils.loadAnimation(requireContext(),R.anim.scaleup)
        scaleDownAnimation = AnimationUtils.loadAnimation(requireContext(),R.anim.scaledown)

        circularSliderView = view.findViewById(R.id.circularSliderView)
        startButton = view.findViewById(R.id.startButton)
        stopButton = view.findViewById(R.id.stopButton)
        resetButton = view.findViewById(R.id.resetButton)
        timerTextView = view.findViewById(R.id.timerTextView)

        disableButton(stopButton)
        disableButton(resetButton)

        toggleButtonEnabled(startButton, true)
        toggleButtonEnabled(stopButton, false)
        toggleButtonEnabled(resetButton, false)

        circularSliderView.onSliderChangeListener = { value ->
            timeLeftInMillis = (value * 60 * 1000).toLong() // Convert minutes to milliseconds
            updateTimerText()
        }

        startButton.setOnClickListener {
            startTimer()
        }

        stopButton.setOnClickListener {
            stopTimer()
        }

        resetButton.setOnClickListener {
            resetTimer()
        }

        notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        createNotificationChannel()

        circularSliderView.onSliderChangeListener = { value ->
            initialTimeInMillis = (value * 60 * 1000).toLong() // Convert minutes to milliseconds
            timeLeftInMillis = initialTimeInMillis
            updateTimerText()
        }

        return view
    }

    private fun startTimer() {
        if (!isRunning && timeLeftInMillis > 0) {
            startAnimation()
            circularSliderView.isTimerRunning = true
            countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftInMillis = millisUntilFinished
                    updateTimerText()

                    val totalTimeInMillis = timeLeftInMillis
                    val progressPercentage = ((totalTimeInMillis.toFloat() / initialTimeInMillis) * 100).toInt()

                    // Update slider position based on progress
                    val angle = circularSliderView.arcStartAngle + circularSliderView.arcSweepAngle * progressPercentage / 100
                    circularSliderView.setValue(angle)
                }

                override fun onFinish() {
                    isRunning = false
                    updateTimerText()
                    showNotification()
                }
            }.start()

            isRunning = true
            toggleButtonEnabled(startButton, false)
            toggleButtonEnabled(stopButton, true)
            toggleButtonEnabled(resetButton, true)
        }
    }

    private fun stopTimer() {
        if (isRunning) {
            circularSliderView.isTimerRunning = false
            countDownTimer.cancel()
            isRunning = false
            circularSliderView.isTimerRunning = false
            toggleButtonEnabled(startButton, true)
            toggleButtonEnabled(stopButton, false)
            toggleButtonEnabled(resetButton, true)
        }
    }

    private fun resetTimer() {
        stopTimer()
        circularSliderView.isTimerRunning = false
        timeLeftInMillis = initialTimeInMillis
        timeLeftInMillis = 0
        updateTimerText()
        toggleButtonEnabled(startButton, true)
        toggleButtonEnabled(stopButton, false)
        toggleButtonEnabled(resetButton, false)

        circularSliderView.resetSlider()
    }

    private fun updateTimerText() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60

        val timeLeftFormatted = String.format("%02d:%02d:%02d",hours , minutes, seconds)
        timerTextView.text = timeLeftFormatted
    }

    private fun showNotification() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setContentTitle("Timer")
            .setContentText("Timer is up!")
            .setSmallIcon(R.drawable.stopclock)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setColor(Color.RED)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channelName = "Timer Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, channelName, importance)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        notificationManager?.createNotificationChannel(channel)
    }

    private fun disableButton(button: ImageView) {
        button.isEnabled = false
        button.alpha = 0.5f // Half opacity (grayed out)
    }

    private fun toggleButtonEnabled(button: ImageView, isEnabled: Boolean) {
        if (isEnabled) {
            button.isEnabled = true
            button.alpha = 1.0f
        } else {
            button.isEnabled = false
            button.alpha = 0.5f
        }
    }

    private fun startAnimation(){
        scaleDownAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                circularSliderView.startAnimation(scaleUpAnimation)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        circularSliderView.startAnimation(scaleDownAnimation)
    }
}

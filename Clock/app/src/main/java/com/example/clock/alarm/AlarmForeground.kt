package com.example.clock.alarm

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import com.example.clock.AlarmRing

class AlarmForeground : Service() {

    companion object {
        const val ACTION_SHOW_FULLSCREEN = "com.example.clock.SHOW_FULLSCREEN"
        const val NOTIFICATION_ID = 1234
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isInteractive) {
                val fullScreenIntent = Intent(context, AlarmRing::class.java)
                fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(fullScreenIntent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_SHOW_FULLSCREEN -> {
                    // Show full-screen activity or UI component
                    // Example: Replace with your full-screen activity intent
                    val fullScreenIntent = Intent(this, AlarmRing::class.java)
                    fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(fullScreenIntent)
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenOffReceiver)
        releaseWakeLock()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "AlarmForeground::Wakelock"
        )
        wakeLock?.acquire()
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }
}
package com.example.clock.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.clock.R

class AlarmReceiver : BroadcastReceiver() {

    var NOTIFICATION_ID :Int = 123

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("AlarmReceiver", "Received alarm broadcast")
        when (action) {
            ACTION_ALARM_TRIGGERED -> {
                Log.d("AlarmReceiver", "Alarm triggered action detected")
                createNotificationChannel(context)
                showNotification(context)
                Log.d("success","came out")
                playAlarm(context)
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!powerManager.isInteractive){
                    startFullScreenActivity(context)
                }
            }
            ACTION_DISMISS -> {
                stopAlarm(context)
                cancelNotification(context)
            }
            ACTION_SNOOZE -> {
                handleSnooze(context, intent)
                cancelNotification(context)
            }
            else -> {}
        }
    }

    private fun playAlarm(context: Context) {
        try {
                val ringtoneUriString = SharedPreferencesHelper.getRingtoneUri(context)
                val ringtoneUri =
                    if (!ringtoneUriString.isNullOrEmpty()) Uri.parse(ringtoneUriString)
                    else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

                val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
                ringtone.play()
        } catch (e: SecurityException) {
            val defaultRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val defaultRingtonePlayer = RingtoneManager.getRingtone(context, defaultRingtone)
            defaultRingtonePlayer.play()
        }
    }

    private fun showNotification(context: Context) {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            createNotificationChannel(context)

            val notificationId = System.currentTimeMillis().toInt() // Unique ID based on current time
            NOTIFICATION_ID = notificationId

            val dismissIntent = Intent(context, AlarmReceiver::class.java)
                .apply { action = ACTION_DISMISS }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId, // Use unique notification ID here
                dismissIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val snoozeIntent = Intent(context, AlarmReceiver::class.java)
                .apply { action = ACTION_SNOOZE }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId, // Use different ID for snooze PendingIntent
                snoozeIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.alarm)
                .setContentTitle("Alarm")
                .setContentText("Time to get ready!")
                .addAction(R.drawable.close, "Dismiss", dismissPendingIntent)
                .addAction(R.drawable.close, "Snooze", snoozePendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build()

            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }


    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm notifications"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

    }

    private fun cancelNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        stopAlarm(context)
    }

    private fun startFullScreenActivity(context: Context) {
        val fullScreenIntent = Intent(context, AlarmForeground::class.java)
        fullScreenIntent.action = "com.example.clock.SHOW_FULLSCREEN"
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startService(fullScreenIntent)
    }

    private fun handleSnooze(context: Context, intent: Intent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGERED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ALARM,
            snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntervalMillis = 5 * 60 * 1000L  // 5 minutes snooze interval
        val currentTimeMillis = System.currentTimeMillis()
        val snoozeTimeMillis = currentTimeMillis + snoozeIntervalMillis

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            snoozeTimeMillis,
            pendingIntent
        )

        stopAlarm(context)
    }

    private fun stopAlarm(context: Context) {
        Log.d("stop","works")
        try {
            if (ringtone != null && ringtone!!.isPlaying) {
                ringtone!!.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val CHANNEL_ID = "AlarmChannel"
        const val ACTION_DISMISS = "com.example.clock.DISMISS_ALARM"
        const val ACTION_SNOOZE = "com.example.clock.SNOOZE_ALARM"
        const val ACTION_ALARM_TRIGGERED = "com.example.clock.ALARM_TRIGGERED"
        const val REQUEST_CODE_ALARM = 2
        private var ringtone: Ringtone? = null
    }
}

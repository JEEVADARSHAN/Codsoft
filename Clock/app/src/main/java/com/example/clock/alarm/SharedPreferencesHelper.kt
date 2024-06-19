package com.example.clock.alarm

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesHelper {

    private const val SHARED_PREFS_NAME = "AlarmSettings"
    private const val KEY_RINGTONE_URI = "ringtone_uri"

    fun getRingtoneUri(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_RINGTONE_URI, null)
    }

    fun setRingtoneUri(context: Context, uri: String?) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(KEY_RINGTONE_URI, uri).apply()
    }

}
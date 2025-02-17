package com.example.pineappleexpense.model

import android.content.Context
import android.content.SharedPreferences

class SharedPrefs(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "DATA"
    }
        fun setStr(context: Context, key: String, value: String) {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString(key, value)
            editor.apply()
        }

        fun getStr(context: Context, key: String): String? {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(key, String.toString())
        }

        fun clearInfo(context: Context) {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear().apply()
        }
}


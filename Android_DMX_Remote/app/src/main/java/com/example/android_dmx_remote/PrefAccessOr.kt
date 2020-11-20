package com.example.android_dmx_remote


import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

object PrefAccessOr {

    private val PREF_FILE = BuildConfig.APPLICATION_ID.replace(".", "_")
    private var sharedPreferences: SharedPreferences? = null

    private fun openPref(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_FILE, AppCompatActivity.MODE_PRIVATE)
    }

    //For string value
    open fun getValue(context: Context, key: String, defaultValue: String) : String {
        openPref(context)
        val result = sharedPreferences!!.getString(key, defaultValue)
        sharedPreferences = null
        return result.toString()
    }

    fun setValue(context: Context, key: String, value: String) {
        openPref(context);
        val prefsPrivateEditor = sharedPreferences!!.edit()
        prefsPrivateEditor.putString(key, value)
        prefsPrivateEditor.apply()
        sharedPreferences = null
    }

}
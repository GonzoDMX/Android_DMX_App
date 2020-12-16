package com.example.android_dmx_remote


import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


object ModelAccessSave {

    private val PREF_FILE = BuildConfig.APPLICATION_ID.replace(".", "_")
    private var sharedPreferences: SharedPreferences? = null

    private val mapKey = "CUELIST"
    private val countKey = "CUECOUNT"

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
        val editor = sharedPreferences!!.edit()
        editor.putString(key, value)
        editor.apply()
        sharedPreferences = null
    }

    fun saveCueListMap(context: Context, cueMap: MutableMap<Int, ModelCueClass>, cueCount: Int){
        openPref(context)
        val jsonObject = Gson().toJson(cueMap as Map<Int, Any>)
        //val jsonString = jsonObject.toString()
        val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
        editor.remove(mapKey).apply()
        editor.remove(countKey).apply()
        editor.putString(mapKey, jsonObject)
        editor.putInt(countKey, cueCount)
        editor.commit()
    }

    fun recoverCueListMap(context: Context) {
        openPref(context)
        val mapString = sharedPreferences!!.getString(mapKey, "")
        Log.d("PRINT", mapString.toString())
        if (mapString != "") {
            val count = sharedPreferences!!.getInt(countKey, 0)
            Log.d("COUNT", count.toString())
            var map: MutableMap<Int, ModelCueClass> = HashMap()
            //TODO: Problem here does not properly reconstruct Map Data

            val typeOfHashMap: Type = object : TypeToken<Map<Int, ModelCueClass>?>() {}.type
            map = Gson().fromJson(mapString, typeOfHashMap)
            Log.d("MAPMAP", map.toString())
            ModelCueListMap.setMap(count, map)
        } else {
            Log.d("LOAD", "No Cue List Map Found")
        }
        sharedPreferences = null
    }

}


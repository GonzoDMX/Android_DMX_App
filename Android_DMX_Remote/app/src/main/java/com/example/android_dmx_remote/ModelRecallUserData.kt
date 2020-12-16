package com.example.android_dmx_remote

import android.app.Application
import android.util.Log

class ModelRecallUserData: Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("TESTSTART", "Application started!")
        ModelAccessSave.recoverCueListMap(this)
    }
}
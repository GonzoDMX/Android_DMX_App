package com.example.android_dmx_remote

//Data class for returning parsed cue information
data class RawCue(val validCue: Boolean, val chanSelect: ArrayList<Int>?, val intensityVal: Int?, val fadeTime: Int? )
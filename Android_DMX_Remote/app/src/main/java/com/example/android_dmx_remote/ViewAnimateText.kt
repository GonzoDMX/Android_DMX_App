package com.example.android_dmx_remote

import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation

//Object for Controlling text and UI animations
object ViewAnimateText {

    //Text Shake Animation
    fun shakeError(): TranslateAnimation? {
        val shake = TranslateAnimation(0F, 10F, 0F, 0F)
        shake.duration = 100
        shake.interpolator = CycleInterpolator(7F)
        return shake
    }

    //Text Slide out to right animation
    fun slideOut(): TranslateAnimation? {
        val slide = TranslateAnimation(0F, 2000F, 0F, 0F)
        slide.duration = 250
        //slide.interpolator = CycleInterpolator(7F)
        return slide
    }
}
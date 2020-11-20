package com.example.android_dmx_remote

object MathAPO {

    //Maps an input value from one range of numbers to another range of numbers
    fun mapRange(num: Int, inMin: Int, inMax: Int, outMin: Int, outMax: Int): Int {
        return ((num - inMin) * (outMax - outMin) / (inMax - inMin) + outMin)
    }

}
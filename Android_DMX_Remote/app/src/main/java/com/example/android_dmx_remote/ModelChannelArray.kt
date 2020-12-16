package com.example.android_dmx_remote

object ModelChannelArray {
    var levels = ArrayList<Int>()

    init {
        repeat(512) { levels.add(0) }
    }

}

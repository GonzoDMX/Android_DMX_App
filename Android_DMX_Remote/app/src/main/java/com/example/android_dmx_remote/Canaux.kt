package com.example.android_dmx_remote

object Canaux {
    var levels = ArrayList<Int>()

    init {
        repeat(512) { levels.add(0) }
    }

}

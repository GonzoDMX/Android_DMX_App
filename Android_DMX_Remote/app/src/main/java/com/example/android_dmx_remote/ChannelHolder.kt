package com.example.android_dmx_remote

object ChannelHolder {
    var levels = ArrayList<Int>()

    init {
        repeat(512) { levels.add(0) }
    }

}

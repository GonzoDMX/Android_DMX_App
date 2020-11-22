package com.example.android_dmx_remote

class OutputManager {

    private var tempLevels = ArrayList<Int>()
    private var cancel = false

    private var master = 100.0f
    private var blackout = false



    init {
        repeat(512) { tempLevels.add(0) }
    }


    fun setLevel(cue: CueClass) {
        Canaux.levels = cue.levels
        for(i in 0 until 512){
            tempLevels[i] = ((Canaux.levels[i].toFloat() * (this.master / 100f)).toInt())
        }
        if (!blackout) {
            if (RemoteDevice.getConnectionStatus()) {
                Thread(
                        ClientChannelsUDP(
                                RemoteDevice.getRemoteIP(),
                                RemoteDevice.getRemotePort(),
                                tempLevels
                        )
                ).start()
            }
        }
    }


    fun fadeLevels(cue: CueClass) {
        while(!cancel) {

        }

    }

    fun startBlackOut(){
        repeat(512) { tempLevels.add(0) }
        if(RemoteDevice.getConnectionStatus()) {
            Thread(
                    ClientChannelsUDP(
                            RemoteDevice.getRemoteIP(),
                            RemoteDevice.getRemotePort(),
                            tempLevels
                    )
            ).start()
        }
        blackout = true
    }


    fun endBlackOut() {
        for(i in 0 until 512){
            tempLevels[i] = ((Canaux.levels[i].toFloat() * (this.master / 100f)).toInt())
        }
        if (RemoteDevice.getConnectionStatus()) {
            Thread(
                    ClientChannelsUDP(
                            RemoteDevice.getRemoteIP(),
                            RemoteDevice.getRemotePort(),
                            tempLevels
                    )
            ).start()
        }
        blackout = false
    }


    fun setMaster(master: Float){
        this.master = master
        for(i in 0 until 512){
            tempLevels[i] = ((Canaux.levels[i].toFloat() * (this.master / 100f)).toInt())
        }
        if (!blackout) {
            if (RemoteDevice.getConnectionStatus()) {
                Thread(
                        ClientChannelsUDP(
                                RemoteDevice.getRemoteIP(),
                                RemoteDevice.getRemotePort(),
                                tempLevels
                        )
                ).start()
            }
        }
    }

    fun cancel() {
        cancel = true
    }
}
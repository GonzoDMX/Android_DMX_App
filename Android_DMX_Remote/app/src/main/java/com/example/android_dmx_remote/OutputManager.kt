package com.example.android_dmx_remote

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class OutputManager {

    private var tempLevels = ArrayList<Int>()
    private var cancel = false

    private var master = 100.0f
    private var blackout = false

    private var job: CoroutineContext? = null

    init {
        repeat(512) { tempLevels.add(0) }
    }

    fun goCue(cue: CueClass) {
        if(cue.fade == 0) {
            setLevels(cue)
        } else {
            fadeLevels(cue)
        }
    }

    private fun setLevels(cue: CueClass) {
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


    private fun fadeLevels(cue: CueClass) {
        //Check if coroutine fade is running, if so kill it
        if(job != null) {
            if(job!!.isActive) {
                job!!.cancel()
            }
        }
        job = GlobalScope.launch(context = Dispatchers.Main) {
            /* TODO: Run fade here! */
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
}
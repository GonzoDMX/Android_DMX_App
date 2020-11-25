package com.example.android_dmx_remote

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.cancel
import kotlin.coroutines.CoroutineContext

class OutputManager {

    private var cueLevels = ArrayList<Int>()
    private var masterLevels = ArrayList<Int>()

    private var master = 100.0f
    private var blackout = false

    private var job: CoroutineContext? = null

    init {
        repeat(512) { cueLevels.add(0) }
        repeat(512) { masterLevels.add(0) }
    }

    fun goCue(cue: CueClass) {
        cueLevels = cue.levels
        var modLevels = ArrayList<Int>()
        for(i in 0 until 512){
            modLevels.add((cue.levels[i].toFloat() * (this.master / 100f)).toInt())
        }
        if(cue.fade == 0) {
            Log.d("GO_CUE", "Set Levels")
            setLevels(cueLevels, modLevels)
        } else {
            Log.d("GO_CUE", "Fade Levels")
            fadeLevels(cueLevels, modLevels, cue.fade)
        }
    }

    private fun setLevels(cueLevels: ArrayList<Int>, modLevels: ArrayList<Int>) {
        Canaux.levels = cueLevels
        if (!blackout) {
            transmit(modLevels)
        }
    }


    private fun fadeLevels(cueLevels: ArrayList<Int>, modLevels: ArrayList<Int>, cueFade: Int) {
        //Check if coroutine fade is running, if so kill it
        if(job != null) {
            //Block here to wait until running job is cancelled
            while(job!!.isActive) {
                Log.d("JOB", "Cancelling")
                job!!.cancel()
            }
        }
        job = GlobalScope.launch(context = Dispatchers.Main) {
            var currentMaster = ArrayList<Int>()

            var current = if(master < 100.0f) { masterLevels } else {Canaux.levels}
            var target = modLevels
            var fade = cueFade
            //While isActive needed to make to coroutine cancellable
            while(isActive) {
                while(fade >= 0) {
                    val loopStart = System.currentTimeMillis()
                    for (i in 0 until 512) {
                        if (target[i] != current[i]) {
                            //if fading down
                            current[i] = if(target[i] > Canaux.levels[i]) {
                                val count = (cueFade - (cueFade - fade))
                                MathAPO.mapRange(count, 0, cueFade, target[i], Canaux.levels[i])
                            }
                            //Else fade up
                            else {
                                val count = (cueFade - fade)
                                MathAPO.mapRange(count, 0, cueFade, Canaux.levels[i], target[i])
                            }
                        }
                    }
                    transmit(current)
                    val loopEnd = System.currentTimeMillis()
                    fade -= (30 + (loopEnd - loopStart).toInt())
                    delay(30)
                }
                Canaux.levels = cueLevels
                transmit(target)
                job!!.cancel()
            }
            Log.d("JOB", "Finished")
        }
    }

    private fun transmit(levels: ArrayList<Int>) {
        if (RemoteDevice.getConnectionStatus()) {
            Thread(
                    ClientChannelsUDP(
                            RemoteDevice.getRemoteIP(),
                            RemoteDevice.getRemotePort(),
                            levels
                    )
            ).start()
        }
    }

    fun startBlackOut(){
        var blackLevels = ArrayList<Int>()
        repeat(512) { blackLevels.add(0) }
        transmit(blackLevels)
        blackout = true
    }


    fun endBlackOut() {
        for(i in 0 until 512){
            masterLevels[i] = ((Canaux.levels[i].toFloat() * (this.master / 100f)).toInt())
        }
        transmit(masterLevels)
        blackout = false
    }


    fun setMaster(master: Float){
        this.master = master
        for(i in 0 until 512){
            masterLevels[i] = ((Canaux.levels[i].toFloat() * (this.master / 100f)).toInt())
        }
        if (!blackout) {
            transmit(masterLevels)
        }
    }
}
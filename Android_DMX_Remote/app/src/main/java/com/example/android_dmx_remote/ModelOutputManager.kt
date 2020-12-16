package com.example.android_dmx_remote

import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ModelOutputManager {

    private var cueLevels = ArrayList<Int>()
    private var masterLevels = ArrayList<Int>()

    private var master = 1.0f
    private var blackout = false

    private var job: CoroutineContext? = null

    init {
        repeat(512) { cueLevels.add(0) }
        repeat(512) { masterLevels.add(0) }
    }

    fun goCue(cue: ModelCueClass) {
        cueLevels = cue.levels
        var modLevels = ArrayList<Int>()
        for(i in 0 until 512){
            modLevels.add((cue.levels[i].toFloat() * master).toInt())
        }
        if(cue.fade == 0) {
            setLevels(cueLevels, modLevels)
        } else {
            fadeLevels(cueLevels, modLevels, cue.fade)
        }
    }

    private fun setLevels(cueLevels: ArrayList<Int>, modLevels: ArrayList<Int>) {
        //Check if coroutine fade is running, if so kill it
        if(job != null) {
            //Block here to wait until running job is cancelled
            while (job!!.isActive) {
                Log.d("JOB", "Cancelling")
                job!!.cancel()
            }
        }
        ModelChannelArray.levels = cueLevels
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
            //Holds initial values
            val init = ArrayList<Int>()
            //Holds current state values
            var current = ArrayList<Int>()
            for (level in ModelChannelArray.levels) {
                init.add((level * master).toInt())
                current.add((level * master).toInt())
            }
            //Holds fade target values
            val targ = modLevels

            //Start fade count at 0
            var count = 0
            //While isActive needed to make to coroutine cancellable
            while(isActive) {
                while(count <= cueFade) {
                    val loopStart = System.currentTimeMillis()
                    for (i in 0 until 512) {
                        if (init[i] != targ[i]) {
                            //if fading down
                            if(init[i] > targ[i]) {
                                val deCount = cueFade - count
                                current[i] = ModelMapRange.mapRange(deCount, 0, cueFade, targ[i], init[i])
                            }
                            //Else fade up
                            else {
                                current[i] = ModelMapRange.mapRange(count, 0, cueFade, init[i], targ[i])
                            }
                        }
                    }
                    transmit(current)
                    ModelChannelArray.levels = current
                    val loopEnd = System.currentTimeMillis()
                    count += (30 + (loopEnd - loopStart).toInt())
                    delay(30)
                }
                ModelChannelArray.levels = cueLevels
                transmit(targ)
                job!!.cancel()
            }
            Log.d("JOB", "Finished")
        }
    }

    private fun transmit(levels: ArrayList<Int>) {
        if (ModelRemoteInfo.getConnectionStatus()) {
            Thread(
                    ModelChannelOutput(
                            ModelRemoteInfo.getRemoteIP(),
                            ModelRemoteInfo.getRemotePort(),
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
            masterLevels[i] = ((ModelChannelArray.levels[i].toFloat() * master).toInt())
        }
        transmit(masterLevels)
        blackout = false
    }


    fun setMaster(value: Float){
        this.master = value / 100
        for(i in 0 until 512){
            masterLevels[i] = ((ModelChannelArray.levels[i].toFloat() * master).toInt())
        }
        if (!blackout) {
            transmit(masterLevels)
        }
    }

    fun cancel() {
        if(job != null) {
            job!!.cancel()
        }
    }
}
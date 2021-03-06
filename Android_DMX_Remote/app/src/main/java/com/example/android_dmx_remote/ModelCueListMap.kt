package com.example.android_dmx_remote

import android.content.Context
import android.util.Log
import com.example.android_dmx_remote.ModelAccessSave.saveCueListMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object ModelCueListMap {

    private var cues =  mutableMapOf<Int, ModelCueClass>()

    private var cueCount = 0

    fun setMap(count: Int, map: MutableMap<Int, ModelCueClass>) {
        cueCount = count
        cues = map
    }

    fun addCue(context: Context, cue: ModelCueClass) {
        cues[cueCount] = cue
        cueCount += 1
        saveCueMap(context, cues, cueCount)
    }

    fun editCue(context: Context, cue: ModelCueClass, pos: Int) {
        cues[pos]!!.name = cue.name
        cues[pos]!!.fade = cue.fade
        saveCueMap(context, cues, cueCount)
    }

    fun deleteCue(context: Context, cue: Int) {
        var keyArray = ArrayList<Int>()
        cues.remove(cue)
        cueCount -= 1
        for((key) in cues) {
            keyArray.add(key)
        }
        //Reorder hash map after a deletion
        keyArray.sort()
        for (i in 0 until cueCount) {
            this.cues[i] = this.cues[keyArray[i]]!!
        }
        saveCueMap(context, cues, cueCount)
    }

    fun moveCue(context: Context, from: Int, to: Int) {
        if (from != to) {
            var keyArray = ArrayList<Int>()
            val tempCue = this.cues[from]!!
            this.cues.remove(from)
            //Build array of remaining keys
            for((key) in cues) {
                keyArray.add(key)
            }
            //Put keys in numerical order
            keyArray.sort()
            Log.d("KEYS", keyArray.toString())
            //Copy cue map
            val tempMap = mutableMapOf<Int, ModelCueClass>()
            var bump = 0
            for (i in 0 until cueCount) {
                if (i == to) {
                    tempMap[i] = tempCue
                    bump += 1
                } else {
                    tempMap[i] = this.cues[keyArray[i - bump]]!!
                }
            }
            this.cues = tempMap
        }
        saveCueMap(context, cues, cueCount)
    }


    fun getCue(cue: Int): ModelCueClass {
        return this.cues[cue]!!
    }

    fun getNames(): ArrayList<String>{
        var names = ArrayList<String>()
        for (i in 0 until cueCount) {
            names.add(cues[i]!!.name)
        }
        return names
    }

    fun getTimes(): ArrayList<String>{
        var times = ArrayList<String>()
        for(i in 0 until cueCount){
            times.add("%.1f".format(cues[i]!!.fade.toFloat() / 1000f) + " Sec")
        }
        return times
    }

    fun getCueCount(): Int{
        return cueCount
    }


    fun nameFree(name: String): Boolean {
        if(cueCount > 0) {
            for (i in 0 until cueCount) {
                if (cues[i]!!.name == name) {
                    return false
                }
            }
        }
        return true
    }

    fun nameEdit(name: String, pos: Int): Boolean {
        if(cueCount > 0) {
            for (i in 0 until cueCount) {
                if (cues[i]!!.name == name && i != pos) {
                    return false
                }
            }
        }
        return true
    }

    private fun saveCueMap(context: Context, saveCues: MutableMap<Int, ModelCueClass>, saveCount: Int){
        GlobalScope.launch(context = Dispatchers.Main) {
            saveCueListMap(context, saveCues, saveCount)
        }
    }
}
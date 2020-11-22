package com.example.android_dmx_remote

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import com.example.android_dmx_remote.ChannelHolder.levels
import kotlinx.android.synthetic.main.create_cue_dialog.*


class CreateCueDialog
(var c: Activity) : Dialog(c), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.create_cue_dialog)
        button_create.setOnClickListener(this)
        button_cancel.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_create -> {
                val name = edit_CueName.text.toString()
                val fade = edit_FadeTime.text.toString().toInt()
                var cue = CueClass(name, levels, fade)
                CueListMap.addCue(cue)
                Log.d("CUELIST", "CUE CREATED!")
            }
            R.id.button_cancel -> dismiss()
        }
        dismiss()
    }
}
package com.example.android_dmx_remote

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import com.example.android_dmx_remote.Canaux.levels
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.edit_cue_dialog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class EditCueDialog
(var c: Activity, pos: Int, cue: CueClass) : Dialog(c), View.OnClickListener {

    private val position = pos
    private val preName: String = cue.name
    private val preFade: String = "%.1f".format(cue.fade.toFloat() / 1000f)

    private val fadeCheck = Regex("^([0-9]|[1-9][0-9])(|[,.][0-9])$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.edit_cue_dialog)
        button_commit_edit.setOnClickListener(this)
        button_delete_edit.setOnClickListener(this)
        button_cancel_edit.setOnClickListener(this)
        edit_name_edit.setText(this.preName)
        edit_fade_edit.setText(this.preFade)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_commit_edit -> {

                if(fadeCheck.matches(edit_fade_edit.text.toString())) {
                    val name = edit_name_edit.text.toString()
                    val fade = (edit_fade_edit.text.toString().toFloat() * 1000).toInt()
                    if(CueListMap.nameEdit(name, position)) {
                        var cue = CueClass(name, levels, fade)
                        CueListMap.editCue(cue, position)
                        Log.d("CUELIST", "CUE CREATED!")
                        dismiss()
                    } else {
                        errorCueName()
                    }
                } else {
                    errorFadeTime()
                }
            }
            R.id.button_delete_edit -> {
                Log.d("TEST", "Delete: $position")
                CueListMap.deleteCue(position)
                dismiss()
            }
            R.id.button_cancel_edit -> dismiss()
        }
    }

    private fun errorCueName() {
        GlobalScope.launch(context = Dispatchers.Main) {
            edit_name_edit.setTextColor(Color.RED)
            edit_name_edit.startAnimation(AnimAPO.shakeError())
            delay(250)
            edit_name_edit.setTextColor(Color.WHITE)
        }
    }

    private fun errorFadeTime() {
        GlobalScope.launch(context = Dispatchers.Main) {
            edit_fade_edit.setTextColor(Color.RED)
            edit_fade_edit.startAnimation(AnimAPO.shakeError())
            delay(250)
            edit_fade_edit.setTextColor(Color.WHITE)
        }
    }
}
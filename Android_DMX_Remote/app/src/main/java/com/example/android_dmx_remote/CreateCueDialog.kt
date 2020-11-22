package com.example.android_dmx_remote

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import androidx.core.widget.addTextChangedListener
import com.example.android_dmx_remote.Canaux.levels
import kotlinx.android.synthetic.main.create_cue_dialog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CreateCueDialog
(var c: Activity) : Dialog(c), View.OnClickListener {

    private val fadeCheck = Regex("^([0-9]|[1-9][0-9])(|[,.][0-9])$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.create_cue_dialog)
        button_create.setOnClickListener(this)
        button_cancel.setOnClickListener(this)
        edit_FadeTime.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0) {
                    if(fadeCheck.matches(s.toString())){
                        Log.d("TEXTCHANGE", "Match")
                    } else {
                        Log.d("TEXTCHANGE", "NOT MATCH")
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_create -> {

                if(fadeCheck.matches(edit_FadeTime.text.toString())) {
                    val name = edit_CueName.text.toString()
                    val fade = (edit_FadeTime.text.toString().toFloat() * 1000).toInt()
                    if(CueListMap.nameFree(name)) {
                        var cue = CueClass(name, levels, fade)
                        CueListMap.addCue(cue)
                        Log.d("CUELIST", "CUE CREATED!")
                        dismiss()
                    } else {
                        errorCueName()
                    }
                } else {
                    errorFadeTime()
                }
            }
            R.id.button_cancel -> dismiss()
        }
    }

    private fun errorCueName() {
        GlobalScope.launch(context = Dispatchers.Main) {
            edit_CueName.setTextColor(Color.RED)
            edit_CueName.startAnimation(AnimAPO.shakeError())
            delay(250)
            edit_CueName.setTextColor(Color.WHITE)
        }
    }

    private fun errorFadeTime() {
        GlobalScope.launch(context = Dispatchers.Main) {
            edit_FadeTime.setTextColor(Color.RED)
            edit_FadeTime.startAnimation(AnimAPO.shakeError())
            delay(250)
            edit_FadeTime.setTextColor(Color.WHITE)
        }
    }
}
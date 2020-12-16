package com.example.android_dmx_remote

import kotlinx.android.synthetic.main.settings_dialog.*
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate


class ControllerSettingsDialog
(var c: Activity) : Dialog(c), View.OnClickListener, AdapterView.OnItemSelectedListener {

    private val spinner_list = arrayOf("SYSTEM DEFAULT", "LIGHT THEME", "DARK THEME")

    private var select_theme: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.settings_dialog)

        //Setup Spinner
        spinner_theme!!.onItemSelectedListener = this
        val sadapter = this.run { ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinner_list) }
        sadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_theme!!.adapter = sadapter

        //Set current theme state
        when(ModelSystemValues.theme) {
            "SYSTEM DEFAULT" ->{ spinner_theme.setSelection(0) }
            "LIGHT THEME" -> { spinner_theme.setSelection(1) }
            "DARK THEME" -> { spinner_theme.setSelection(2) }
        }
        //Setup buttons
        button_set.setOnClickListener(this)
        button_cancel.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_set -> {
                ModelAccessSave.setValue(context, "Theme", select_theme)
                ModelSystemValues.theme = select_theme
                when(select_theme){
                    "SYSTEM DEFAULT" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    "LIGHT THEME" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "DARK THEME" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                }
            R.id.button_cancel -> dismiss()
            else -> {
            }
        }
        dismiss()
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d("SPINNER", spinner_list[position].toString())
        select_theme = spinner_list[position]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}


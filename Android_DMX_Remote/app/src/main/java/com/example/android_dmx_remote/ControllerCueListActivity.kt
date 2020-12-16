package com.example.android_dmx_remote

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.activity_cue_list.*

open class ControllerCueListActivity : AppCompatActivity() {

    private var layoutManager:RecyclerView.LayoutManager? = null
    private var adapter:RecyclerView.Adapter<ModelRecyclerCueAdapter.ViewHolder>? = null

    private val output = ModelOutputManager()

    private var editMode = false
    private var blackOut = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cue_list)
        setSupportActionBar(findViewById(R.id.toolbar_list))

        if (ModelRemoteInfo.getConnectionStatus()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
            }

        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
            }
        }

        //Set EditText Color Based on Theme
        when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
            }
            Configuration.UI_MODE_NIGHT_NO -> {
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        layoutManager = GridLayoutManager(this, 2)
        recycle_cuelist.layoutManager = layoutManager

        adapter = ModelRecyclerCueAdapter(this)
        recycle_cuelist.adapter = adapter

        itemTouchHelper.attachToRecyclerView(recycle_cuelist)

        slider_master.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped
            }
        })

        slider_master.addOnChangeListener { slider, value, fromUser ->
            // Responds to when slider's value is changed
            Log.d("SLIDER", value.toString())
            output.setMaster(value)
            val textVal = value.toString().dropLast(2) + "%"
            text_slider.text = textVal
        }


        button_remote.setOnClickListener {
            Log.d("CLICK", "DMX REMOTE")
            output.cancel()
            goToDirect()
        }

        //Toggle Edit Mode
        button_edit.setOnClickListener {
            if(editMode) {
                button_edit.compoundDrawableTintList = ColorStateList.valueOf(Color.WHITE)
                editMode = false
                (adapter as ModelRecyclerCueAdapter).editModeEnable(editMode)

            } else {
                button_edit.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
                editMode = true
                (adapter as ModelRecyclerCueAdapter).editModeEnable(editMode)
            }
            if(ModelCueListMap.getCueCount() > 0) {
                for (i in 0 until ModelCueListMap.getCueCount()) {
                    (adapter as ModelRecyclerCueAdapter).notifyItemChanged(i)
                }
            }
        }

        button_blackout.setOnClickListener {
            if(blackOut) {
                button_blackout.setTextColor(Color.WHITE)
                blackOut = false
                output.endBlackOut()
            } else {
                button_blackout.setTextColor(Color.RED)
                blackOut = true
                output.startBlackOut()
            }
        }

    }

    fun toggleEdit(edit: Boolean) {
        button_edit.isEnabled = edit
    }

    //Binds gesture control to the CardViews in the Cue List RecyclerView
    private val itemTouchHelper by lazy {
        //UP, DOWN, START and END determine direction of gesture detected
        val simpleItemTouchCallback =
                object : ItemTouchHelper.SimpleCallback(UP or
                        DOWN or
                        START or
                        END, 0) {

                    override fun onMove(recyclerView: RecyclerView,
                                        viewHolder: RecyclerView.ViewHolder,
                                        target: RecyclerView.ViewHolder): Boolean {

                        val adapter = recyclerView.adapter as ModelRecyclerCueAdapter
                        val from = viewHolder.adapterPosition
                        val to = target.adapterPosition
                        //Updates the RecyclerView Model
                        adapter.moveItem(from, to)
                        //Tell adapter to render the change
                        adapter.notifyItemMoved(from, to)

                        return true
                    }
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                          direction: Int) {
                        //Place code here to implement horizontal swipe gesture
                    }
                }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun goToDirect() {
        if(editMode){
            button_edit.compoundDrawableTintList = ColorStateList.valueOf(Color.WHITE)
            editMode = false
            (adapter as ModelRecyclerCueAdapter).editModeEnable(editMode)
        }
        if(blackOut){
            button_blackout.setTextColor(Color.WHITE)
            blackOut = false
            output.endBlackOut()
        }
        (adapter as ModelRecyclerCueAdapter).resetIndex()
        val intent = Intent(this, ControllerDirectActivity::class.java).apply {}
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


    fun cuePlay(new: Int, old: Int) {
        if(new != old) {
            //If no fade is required
            output.goCue(ModelCueListMap.getCue(new))
            (adapter as ModelRecyclerCueAdapter).notifyItemChanged(new)
            if (old >= 0) {
                (adapter as ModelRecyclerCueAdapter).notifyItemChanged(old)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        (adapter as ModelRecyclerCueAdapter).notifyDataSetChanged()
    }


    //Catches event when user presses back button
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    //Slide Out slide in animation for going back to main activity
    override fun finish() {
        super.finish()
        output.cancel()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
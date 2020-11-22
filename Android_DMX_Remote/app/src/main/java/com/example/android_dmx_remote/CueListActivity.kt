package com.example.android_dmx_remote

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.activity_cue_list.*

open class CueListActivity : AppCompatActivity() {

    private var layoutManager:RecyclerView.LayoutManager? = null
    private var adapter:RecyclerView.Adapter<RecyclerCueAdapter.ViewHolder>? = null

    private var editMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cue_list)
        setSupportActionBar(findViewById(R.id.toolbar_list))

        if (RemoteDevice.getConnectionStatus()) {
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

        adapter = RecyclerCueAdapter()
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
            val textVal = value.toString().dropLast(2) + "%"
            text_slider.text = textVal
        }


        //Toggle Edit Mode
        button_edit.setOnClickListener {
            if(editMode) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    button_edit.compoundDrawableTintList = ColorStateList.valueOf(Color.WHITE)
                    editMode = false
                    (adapter as RecyclerCueAdapter).editModeEnable(editMode)
                }
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    button_edit.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
                    editMode = true
                    (adapter as RecyclerCueAdapter).editModeEnable(editMode)
                }
            }
            (adapter as RecyclerCueAdapter).notifyDataSetChanged()
        }

    }


    private val itemTouchHelper by lazy {
        // 1. Note that I am specifying all 4 directions.
        //    Specifying START and END also allows
        //    more organic dragging than just specifying UP and DOWN.
        val simpleItemTouchCallback =
                object : ItemTouchHelper.SimpleCallback(UP or
                        DOWN or
                        START or
                        END, 0) {

                    override fun onMove(recyclerView: RecyclerView,
                                        viewHolder: RecyclerView.ViewHolder,
                                        target: RecyclerView.ViewHolder): Boolean {

                        val adapter = recyclerView.adapter as RecyclerCueAdapter
                        val from = viewHolder.adapterPosition
                        val to = target.adapterPosition
                        // 2. Update the backing model. Custom implementation in
                        //    MainRecyclerViewAdapter. You need to implement
                        //    reordering of the backing model inside the method.
                        adapter.moveItem(from, to)
                        // 3. Tell adapter to render the model update.
                        adapter.notifyItemMoved(from, to)

                        return true
                    }
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                          direction: Int) {
                        // 4. Code block for horizontal swipe.
                        //    ItemTouchHelper handles horizontal swipe as well, but
                        //    it is not relevant with reordering. Ignoring here.
                    }
                }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    //Catches event when user presses back button
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    //Slide Out slide in animation for going back to main activity
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
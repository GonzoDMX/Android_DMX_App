package com.example.android_dmx_remote

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible

class RecyclerCueAdapter: RecyclerView.Adapter<RecyclerCueAdapter.ViewHolder>() {

    var visible = false

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var cueName: TextView = itemView.findViewById(R.id.text_cuename)
        var fadeTime: TextView = itemView.findViewById(R.id.text_fadetime)
        var editImage: ImageView = itemView.findViewById(R.id.image_card)

        init {
            itemView.setOnClickListener { v: View ->
                var position:Int = adapterPosition
                if(visible){
                    //Trigger Edit Cue Action Here
                    Log.d("CLICK", "Edit Cue: $position")
                } else {
                    //Trigger Go Cue Action here
                    Log.d("CLICK", "Go Cue: $position")
                }
            }
        }
    }

    fun moveItem(from: Int, to: Int) {
        CueListMap.moveCue(from, to)
    }

    fun editModeEnable(edit: Boolean) {
        visible = edit
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_cue, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cueName.text = CueListMap.getNames()[position]
        holder.fadeTime.text = CueListMap.getTimes()[position]

        if(visible) {
            holder.editImage.isVisible = visible
        } else {
            holder.editImage.isVisible = visible
        }
    }

    override fun getItemCount(): Int {
        Log.d("RECYCLE", CueListMap.getCueCount().toString())
        return CueListMap.getCueCount()
    }
}
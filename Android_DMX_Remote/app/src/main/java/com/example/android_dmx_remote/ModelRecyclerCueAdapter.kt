package com.example.android_dmx_remote

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ModelRecyclerCueAdapter(cueListActivity: ControllerCueListActivity) : RecyclerView.Adapter<ModelRecyclerCueAdapter.ViewHolder>() {

    var visible = false
    var oldPlayPosition = -1
    var playPosition = -1
    var playTime = 0
    private val activity = cueListActivity


    inner class ViewHolder(itemView: View, activity: ControllerCueListActivity): RecyclerView.ViewHolder(itemView) {

        var cueName: TextView = itemView.findViewById(R.id.text_cuename)
        var fadeTime: TextView = itemView.findViewById(R.id.text_fadetime)
        var editImage: ImageView = itemView.findViewById(R.id.image_editcard)
        var playImage: ImageView = itemView.findViewById(R.id.image_gocard)
        var playProgress: ProgressBar = itemView.findViewById(R.id.progress_fade)

        init {
            itemView.setOnClickListener { v: View ->
                var position:Int = adapterPosition
                if(visible){
                    //Trigger Edit Cue Action Here
                    Log.d("CLICK", "Edit Cue: $position")
                    val editcuedialog = ControllerEditCueDialog(activity, position, ModelCueListMap.getCue(position))
                    editcuedialog.show()
                } else {
                    playPosition = position
                    playTime = ModelCueListMap.getCue(position).fade
                    activity.cuePlay(position, oldPlayPosition)
                    oldPlayPosition = position
                }
            }
        }
    }

    fun moveItem(from: Int, to: Int) {
        if(oldPlayPosition == from) {
            oldPlayPosition = to
        } else {
            if (from < to) {
                if (oldPlayPosition in from..to) {
                    oldPlayPosition -= 1
                }
            } else if (from > to) {
                if (oldPlayPosition in to..from) {
                    oldPlayPosition += 1
                }
            }
        }
        ModelCueListMap.moveCue(activity, from, to)
    }

    fun editModeEnable(edit: Boolean) {
        visible = edit
    }

    fun resetIndex(){
        oldPlayPosition = -1
        playPosition = -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_cue, parent, false)
        return ViewHolder(v, activity)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cueName.text = ModelCueListMap.getNames()[position]
        holder.fadeTime.text = ModelCueListMap.getTimes()[position]

        if(visible) {
            holder.editImage.visibility = VISIBLE
        } else {
            holder.editImage.visibility = INVISIBLE
        }

        //Set flag for currently active cue
        if(position == playPosition) {
            holder.playImage.visibility = VISIBLE
            if (playTime > 0) {
                GlobalScope.launch(context = Dispatchers.Main) {
                    activity.toggleEdit(false)
                    var prog = 0
                    while (prog < playTime) {
                        holder.playProgress.progress = ((prog.toFloat() * 100f) / playTime.toFloat()).toInt()
                        delay(40)
                        prog += 40
                    }
                    holder.playProgress.progress = 100
                    playTime = 0
                    activity.toggleEdit(true)
                }
            } else {
                holder.playProgress.progress = 100
            }
        } else {
            holder.playImage.visibility = INVISIBLE
            holder.playProgress.progress = 0
        }
    }


    override fun getItemCount(): Int {
        Log.d("RECYCLE", ModelCueListMap.getCueCount().toString())
        return ModelCueListMap.getCueCount()
    }
}


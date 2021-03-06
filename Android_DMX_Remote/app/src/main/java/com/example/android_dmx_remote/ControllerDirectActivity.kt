package com.example.android_dmx_remote

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.android_dmx_remote.ViewAnimateText.shakeError
import com.example.android_dmx_remote.ViewAnimateText.slideOut
import com.example.android_dmx_remote.ModelMapRange.mapRange
import com.example.android_dmx_remote.ModelCommandRegex.cmdLineCheck
import com.example.android_dmx_remote.ModelCommandRegex.splitChannels
import kotlinx.android.synthetic.main.activity_direct_remote.*
import kotlinx.android.synthetic.main.activity_direct_remote.button_online
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
open class ControllerDirectActivity : AppCompatActivity() {

    private val output = ModelOutputManager()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direct_remote)
        setSupportActionBar(findViewById(R.id.toolbar_direct))

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        if (ModelRemoteInfo.getConnectionStatus()) {
            button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)

        } else {
            button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
        }

        fillTableHeader()
        initializeChannels()

        // Checks the orientation of the screen
        val orientation = resources.configuration.orientation

        button_addcue.setOnClickListener {
            Log.d("CLICK", "Add Cue")
            val cuedialog = ControllerCreateCueDialog(this, ModelChannelArray.levels)
            cuedialog.show()
        }

        //Prevents a crash when going into landscape mode
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            //Set EditText Color Based on Theme
            when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    text_cmdline.setTextColor(Color.WHITE)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    text_cmdline.setTextColor(Color.BLACK)
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                }
            }


            text_cmdline.isEnabled = false

            //Sets flag to toggle Time button input function between "t" and "."
            var timeSet = false

            //Preset color of progress bar
            progress_time.progressTintList = ColorStateList.valueOf(Color.RED)


            button_zero.setOnClickListener {
                text_cmdline.append("0")
                checkUserInput()
            }

            button_one.setOnClickListener {
                text_cmdline.append("1")
                checkUserInput()
            }

            button_two.setOnClickListener {
                text_cmdline.append("2")
                checkUserInput()
            }

            button_three.setOnClickListener {
                text_cmdline.append("3")
                checkUserInput()
            }

            button_four.setOnClickListener {
                text_cmdline.append("4")
                checkUserInput()
            }

            button_five.setOnClickListener {
                text_cmdline.append("5")
                checkUserInput()
            }

            button_six.setOnClickListener {
                text_cmdline.append("6")
                checkUserInput()
            }

            button_seven.setOnClickListener {
                text_cmdline.append("7")
                checkUserInput()
            }

            button_eight.setOnClickListener {
                text_cmdline.append("8")
                checkUserInput()
            }

            button_nine.setOnClickListener {
                text_cmdline.append("9")
                checkUserInput()
            }

            button_at.setOnClickListener {
                text_cmdline.append("@")
                checkUserInput()
            }

            button_full.setOnClickListener {
                val cmdLineText = text_cmdline.text.toString().takeLast(1)
                if (cmdLineText == "@") {
                    text_cmdline.append("255")
                } else {
                    text_cmdline.append("@255")
                }
                checkUserInput()
            }

            button_thru.setOnClickListener {
                text_cmdline.append("<")
                checkUserInput()
            }

            button_time.setOnClickListener {
                if (timeSet) {
                    text_cmdline.append(".")
                } else {
                    text_cmdline.append("t")
                    timeSet = true
                    button_time.text = getString(R.string.dot_time)
                }
                checkUserInput()
            }

            button_all.setOnClickListener {
                val cmdLineText = text_cmdline.text.toString()
                val lastCheck = cmdLineText.takeLast(1)
                if (lastCheck == "<") {
                    text_cmdline.append("512")
                } else {
                    text_cmdline.append("ALL")
                }
                checkUserInput()
            }

            button_plus.setOnClickListener {
                text_cmdline.append("+")
                checkUserInput()
            }

            button_minus.setOnClickListener {
                text_cmdline.append("-")
                checkUserInput()
            }

            button_del.setOnClickListener {
                val cmdLineText = text_cmdline.text.toString()
                var deleteNum = 1
                //Gets last char of string (the one about to be deleted)
                val lastCheck = cmdLineText.takeLast(1)
                //If the character 't' is about to be deleted reset Time button
                if (lastCheck == "t") {
                    timeSet = false
                    button_time.text = getString(R.string.time)
                }
                if (lastCheck == "L") {
                    deleteNum += 2
                }
                //Delete last character
                text_cmdline.setText(cmdLineText.dropLast(deleteNum))
                if (text_cmdline.text.toString().count() >= 1) {
                    checkUserInput()
                } else if (text_cmdline.text.toString().count() == 0) {
                    progress_time.progress = 0
                }
            }

            button_del.setOnLongClickListener {
                text_cmdline.setText("")
                checkUserInput()
                if(timeSet) {
                    timeSet = false
                    button_time.text = getString(R.string.time)
                }
                true
            }


            button_enter.setOnClickListener {
                if (text_cmdline.text.toString() == "") {
                    flashError()
                } else {
                    val cmdLineText = text_cmdline.text.toString()
                    //Check that command line message is well-formed
                    if (checkUserInput()) {
                        Log.d("OPERATION", "Transmit Command!")
                        val cueData = parseUserInput(cmdLineText)
                        //Check that cue is valid
                        if (cueData != null) {
                            //If cue is valid reset time button
                            timeSet = false
                            button_time.text = getString(R.string.time)
                            //Animate Clear Text
                            textFlyOut()
                            //Check if fade and set progress bar accordingly
                            if (cueData.fade > 0) {
                                progressCount(cueData.fade)
                            } else {
                                flashGreen()
                            }
                            output.goCue(cueData)
                            setChannelValues(cueData)
                        }
                        //Else if cue is invalid signal the error
                        else {
                            flashError()
                        }
                    }
                    //If command line message is not well-formed
                    else {
                        text_cmdline.startAnimation(shakeError())
                    }
                }
            }
        }
    }



    //Clear text from command line
    private fun textFlyOut() {
        GlobalScope.launch(context = Dispatchers.Main) {
            //Shake command line text
            text_cmdline.startAnimation(slideOut())
            delay(250)
            text_cmdline.setText("")
        }
    }

    private fun progressCount(time: Int) {
        GlobalScope.launch(context = Dispatchers.Main) {
            //Shake command line text
            progress_time.progressTintList = ColorStateList.valueOf(Color.GREEN)
            progress_time.progress = 0
            var x = 0
            while (x < time) {
                progress_time.progress = ((x.toFloat() * 100f) / time.toFloat()).toInt()
                delay(30)
                x += 30
            }
            progress_time.progress = 100
            delay(250)
            progress_time.progress = 0
            progress_time.progressTintList = ColorStateList.valueOf(Color.RED)
        }
    }

    private fun setChannelValues(cue: ModelCueClass){
        //Non-Blocking Coroutine for updating channel values
        GlobalScope.launch(context = Dispatchers.Main) {

            if (cue.fade == 0) {
                //updateChildDMX(cue.chanSelect!!, cue.intensityVal!!, false)
                updateTableDMX(cue.levels, cue.fade, ModelChannelArray.levels)
            }
            else {
                button_enter.isEnabled = false

                //Holds initial values
                val init = ArrayList<Int>()
                //Holds current state values
                val current = ArrayList<Int>()
                //Disassociate init and current from Canaux
                for (level in ModelChannelArray.levels){
                    init.add(level)
                    current.add(level)
                }
                //Holds fade target values
                val targ = ArrayList<Int>()
                for (level in cue.levels) {
                    targ.add(level)
                }
                //Start fade count at 0
                var count = 0
                //Holds fade value Int in millis
                val fade = cue.fade
                while(count <= fade) {
                    val loopStart = System.currentTimeMillis()
                    for(i in 0 until 512) {
                        if (init[i] != targ[i]) {
                            //Set direction of fade ie: if Init > Targ -> fade down
                            if (init[i] > targ[i]) {
                                val deCount = fade - count
                                current[i] = mapRange(deCount, 0, fade, targ[i], init[i])
                            }
                            //Else fade up
                            else {
                                current[i] = mapRange(count, 0, fade, init[i], targ[i])
                            }
                        }
                    }
                    updateTableDMX(current, fade - count, targ)
                    val loopEnd = System.currentTimeMillis()
                    count += (30 + (loopEnd - loopStart).toInt())
                    delay(30)
                }
                //Ensure values arrive at target
                updateTableDMX(targ, 0, targ)
                button_enter.isEnabled = true
            }
        }
    }


    private fun flashError() {
        GlobalScope.launch(context = Dispatchers.Main) {
            //Set Progress Bar Red
            progress_time.progress = 100
            //Shake command line text
            text_cmdline.startAnimation(shakeError())
            delay(200)
            progress_time.progress = 0
        }
    }

    private fun flashGreen() {
        GlobalScope.launch(context = Dispatchers.Main) {
            //Set Progress Bar Green
            progress_time.progressTintList = ColorStateList.valueOf(Color.GREEN)
            progress_time.progress = 100
            delay(200)
            progress_time.progress = 0
            progress_time.progressTintList = ColorStateList.valueOf(Color.RED)
        }
    }


    //Processes cmd line input through Regex to validate input
    private fun checkUserInput(): Boolean {
        val strText = text_cmdline.text.toString()
        return if (cmdLineCheck(strText) || strText == ""){
            progress_time.progress = 0
            true
        }
        else {
            progress_time.progress = 100
            false
        }
    }


    //Splits cmd line data and Parses out Intensity Value and Fade Time Value
    private fun parseUserInput(cmdInput: String): ModelCueClass? {

        if (cmdInput.contains("@") && cmdInput.takeLast(1) != "@") {
            val mainParser = cmdInput.split("@")
            Log.d("PARSE", "Channels")
            val channels = parseTargetChannels(mainParser[0])
            val subParser = mainParser[1].split("t")
            Log.d("PARSE", "Intensity")
            val intensity = subParser[0].toInt()
            Log.d("PARSE", "Fade Time")
            val fadetime: Int
            //Convert fade time string to (float * 1000), then to int for time in milliseconds
            fadetime = if (subParser.count() > 1) {
                (subParser[1].toFloat() * 1000).toInt()
            }
            else { 0 }

            var levelSet = ArrayList<Int>()
            for (i in 0 until 512) {
                if (channels!!.contains(i + 1)) {
                    levelSet.add(intensity!!)
                } else {
                    levelSet.add(ModelChannelArray.levels[i])
                }
            }
            return ModelCueClass("REMOTE", levelSet, fadetime)
        }
        else {
            return null
        }
    }


    //Parses out all targeted channels from command line input
    private fun parseTargetChannels(chanInput: String): ArrayList<Int> {
        val channels = ArrayList<Int>()
        val channelList = splitChannels(chanInput)
        val chanCount = channelList.count()
        var index = 0
        if (chanCount == 1) {
            Log.d("CHAN", "Single Item")
            if (channelList[0] == "ALL") {
                for (x in IntRange(1, 512)) {
                    channels.add(x)
                }
            }
            else {
                channels.add(channelList[0].toInt())
            }
        }
        else {
            while (index < chanCount) {
                if (channelList[index] == "ALL") {
                    Log.d("CHAN", "Parse All")
                    for (x in IntRange(1, 512)) {
                        channels.add(x)
                    }
                    index += 1
                } else if (channelList[index] == "+") {
                    if (channelList.getOrNull(index + 2) == "<") {
                        var low = channelList[index + 1].toInt()
                        var high = channelList[index + 3].toInt()

                        //Check if thru is from high value to low value
                        if (low > high) {
                            val x = low
                            low = high
                            high = x
                        }

                        for (x in IntRange(low, high)) {
                            if (!channels.contains(x)) {
                                channels.add(x)
                            }
                        }
                        index += 3
                    } else {
                        val x = channelList[index + 1].toInt()
                        if (!channels.contains(x)) {
                            channels.add(x)
                        }
                        index += 2
                    }
                } else if (channelList[index] == "-") {
                    if (channelList.getOrNull(index + 2) == "<") {
                        var low = channelList[index + 1].toInt()
                        var high = channelList[index + 3].toInt()

                        //Check if thru is from high value to low value
                        if (low > high) {
                            val x = low
                            low = high
                            high = x
                        }

                        for (x in IntRange(low, high)) {
                            if (channels.contains(x)) {
                                channels.remove(x)
                            }
                        }
                        index += 3
                    } else {
                        val x = channelList[index + 1].toInt()
                        if (channels.contains(x)) {
                            channels.remove(x)
                        }
                        index += 2
                    }
                } else {
                    if (channelList.getOrNull(index + 1) == "<") {
                        var low = channelList[index].toInt()
                        var high = channelList[index + 2].toInt()

                        //Check if thru is from high value to low value
                        if (low > high) {
                            val x = low
                            low = high
                            high = x
                        }

                        for (x in IntRange(low, high)) {
                            if (!channels.contains(x)) {
                                channels.add(x)
                            }
                            Log.d("CHAN", "Finished channels")
                        }
                        index += 2
                        Log.d("CHAN", "i is incr")
                    } else {
                        val x = channelList[index].toInt()
                        if (!channels.contains(x)) {
                            channels.add(x)
                        }
                        index += 1
                    }
                }
            }
        }
        Log.d("CHANNEL", channels.toString())
        return channels
    }

    //Fill Top Table Header
    private fun fillTableHeader() {
        Log.d("CLASS", "Fill header")
        var headCount = 0
        for (i in IntRange(0, 51)) {
            val textView = TextView(this)
            val textSample: String = when {
                headCount == 0 -> { "  0" }
                headCount < 100 -> { " $headCount" }
                else -> { headCount.toString() }
            }
            textView.text = textSample
            textView.setPadding(20, 0, 20, 0)
            textView.gravity = Gravity.CENTER

            row_top_header.addView(textView)
            headCount += 10
        }
        Log.d("CLASS", "Finish header")
    }

    //Initialize channel values in table
    private fun initializeChannels() {
        for (index in IntRange(1, 10)){
            for (x in IntRange(0, 51)) {
                val textView = TextView(this)
                val arrIndex = ((x * 10) + index) -1
                if (arrIndex < 512) {
                    textView.text = ModelChannelArray.levels[arrIndex].toString()
                    if (ModelChannelArray.levels[arrIndex] > 0) {
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.android_green))
                    }
                    else {
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.stop_blue))
                    }
                }
                else{
                    textView.text = "--"
                    textView.setTextColor(ContextCompat.getColor(textView.context, R.color.null_grey))
                }
                textView.setPadding(20, 0, 20, 0)
                textView.gravity = Gravity.CENTER
                when (index) {
                    1 -> row_1.addView(textView)
                    2 -> row_2.addView(textView)
                    3 -> row_3.addView(textView)
                    4 -> row_4.addView(textView)
                    5 -> row_5.addView(textView)
                    6 -> row_6.addView(textView)
                    7 -> row_7.addView(textView)
                    8 -> row_8.addView(textView)
                    9 -> row_9.addView(textView)
                    10 -> row_10.addView(textView)
                }
            }
        }
    }


    //Updates channel value on the channel matrix display
    private fun updateTableDMX(channels: ArrayList<Int>, fading: Int, target: ArrayList<Int>) {
        GlobalScope.launch(context = Dispatchers.Main) {
            //Loop through all channel values and find position in grid table
            for (i in 0 until channels.count()) {
                val chan = i + 1
                val intensity = channels[i]
                val rowVal = chan % 10
                var col: Int
                if (chan > 10) {
                    col = ((chan - rowVal) / 10)
                    if (rowVal == 0) {
                        col -= 1
                    }
                } else {
                    col = 0
                }
                //Get the TableLayout TextView child that corresponds with channel value
                var textView = row_1.getChildAt(col) as TextView
                when (rowVal) {
                    2 -> textView = row_2.getChildAt(col) as TextView
                    3 -> textView = row_3.getChildAt(col) as TextView
                    4 -> textView = row_4.getChildAt(col) as TextView
                    5 -> textView = row_5.getChildAt(col) as TextView
                    6 -> textView = row_6.getChildAt(col) as TextView
                    7 -> textView = row_7.getChildAt(col) as TextView
                    8 -> textView = row_8.getChildAt(col) as TextView
                    9 -> textView = row_9.getChildAt(col) as TextView
                    0 -> textView = row_10.getChildAt(col) as TextView
                }
                textView.text = intensity.toString()
                //Set color of text blue = 0, Red when fading, Green when arrived at value > 0
                when {
                    intensity == 0 -> {
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.stop_blue))
                    }
                    fading > 0 && intensity != target[i] -> {
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.moving_red))
                    }
                    else -> {
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.dark_android_green))
                    }
                }
            }
        }
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




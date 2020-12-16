package com.example.android_dmx_remote

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ControllerMainActivity : AppCompatActivity() {


    private var checkConnection: Boolean = false

    private var searchDialog: AlertDialog? = null

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val constLayout = this.main_layout

        //Animates the Main Activity's background
        val animDrawable: AnimationDrawable = constLayout.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(0)
        animDrawable.setExitFadeDuration(4000)
        animDrawable.isOneShot = false
        animDrawable.start()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))


        //Get user saved user theme settings
        val theme = (ModelAccessSave.getValue(
                this,
                "Theme",
                "SYSTEM DEFAULT"
        ))
        Log.d("THEME", theme)
        //Set the App theme
        when(theme) {
            //Set Default Theme
            "SYSTEM DEFAULT" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                ModelSystemValues.theme = "SYSTEM DEFAULT"
            }
            //Set Light Mode
            "LIGHT THEME" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                ModelSystemValues.theme = "LIGHT THEME"
            }
            //Set Dark Mode
            "DARK THEME" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                ModelSystemValues.theme = "DARK THEME"
            }
        }

        //Set EditText Color Based on Theme
        when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                ModelSystemValues.mode = 2
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                ModelSystemValues.mode = 1
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                ModelSystemValues.mode = 0
            }
        }


        if (!ModelSystemValues.status) {
            Thread(ModelClientInput()).start()
            ModelSystemValues.status = true
        }

        if(!ModelRemoteInfo.getAlertStatus()) {
            setConnection()
        }

        //Blocks search dialog until connection returns
        //Coroutine is to prevent App from hanging
        GlobalScope.launch(context = Dispatchers.Main) {
            searchDialog()
        }
        button_online.setOnClickListener {
            if (!checkConnection) {
                checkConnection = true
                setConnection()
            }
        }

        button_cuelist.setOnClickListener {
            Log.d("CLICK", "SETTINGS")
            goToCueList()
        }


        button_settings.setOnClickListener {
            Log.d("CLICK", "SETTINGS")
            val settingsDialog = ControllerSettingsDialog(this)
            settingsDialog.show()
        }

    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        //setConnection()
    }


    private fun goToCueList() {
        val intent = Intent(this, ControllerCueListActivity::class.java).apply {}
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun setConnection() {
        //Declare Wifi manager
        val wifi: WifiManager
        wifi = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        //Get Wifi Address
        val remoteAddress: String? = ModelGetRemote.getIPAddress(wifi)
        if(wifi.isWifiEnabled) {
            //If not null print wifi gateway address to log
            if (remoteAddress != null) {
                //Start a UDP Client Sender Thread
                ModelRemoteInfo.setAddress(remoteAddress)
                Thread(ModelClientOutput(ModelRemoteInfo.getRemoteIP(),
                        ModelRemoteInfo.getRemotePort(),
                        ModelMessages.connect)).start()
                verifyConnect()
                Log.d("IP_CHECK", remoteAddress)
            } else {
                ModelRemoteInfo.setConnectionStatus(false)
                button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
                Log.d("IP_CHECK", "NULL")
            }
        } else {
            ModelSystemValues.message = "No DMX device found!"
            ModelRemoteInfo.setConnectionStatus(false)
            if (!ModelRemoteInfo.getAlertStatus()) {
                ModelRemoteInfo.setAlertStatus(true)
            }
            button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun verifyConnect() {
        //Validate the connection with ESP32 and signal result to user
        GlobalScope.launch(context = Dispatchers.Main) {
            var timeout = false
            var countdown = 30
            while (!ModelThreadReturn.available) {
                delay(100)
                countdown -= 1
                if (countdown == 0) {
                    timeout = true
                    break
                }
            }
            if (!timeout) {
                //Set dialog and Online indicator based on return message from ESP32
                if (ModelThreadReturn.message == "</INITIAL>" ||
                        ModelThreadReturn.message == "</DEJA_VU>" ||
                        ModelThreadReturn.message == "</512>") {
                    Log.d("THREADMAIN", "RETURN CONNECTED")
                    ModelRemoteInfo.setConnectionStatus(true)
                    ModelRemoteInfo.setAlertStatus(true)
                    button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
                } else if (ModelThreadReturn.message == "</WAKE_UP>") {
                    Log.d("THREADMAIN", "WAKE UP")
                    ModelRemoteInfo.setConnectionStatus(true)
                    ModelRemoteInfo.setAlertStatus(true)
                    button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
                } else if (ModelThreadReturn.message == "</BRAKMOD>") {
                    Log.d("THREADMAIN", "Break Mode")
                    ModelRemoteInfo.setConnectionStatus(true)
                    ModelRemoteInfo.setAlertStatus(true)
                    button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
                } else if (ModelThreadReturn.message == "</STANDBY>") {
                    ModelRemoteInfo.setConnectionStatus(true)
                    ModelRemoteInfo.setAlertStatus(true)
                    wakeDialog()
                } else {
                    Log.d("THREADMAIN", "INVALID RETURN")
                    Log.d("RETURN", ModelThreadReturn.message)
                    if (!ModelRemoteInfo.getAlertStatus()) {
                        ModelSystemValues.message = "Invalid response from device: " + ModelThreadReturn.message
                        ModelRemoteInfo.setConnectionStatus(false)
                        ModelRemoteInfo.setAlertStatus(true)
                        button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
                    } else {
                        errorDialog("Invalid response from device: " + ModelThreadReturn.message)
                        ModelRemoteInfo.setConnectionStatus(false)
                        ModelRemoteInfo.setAlertStatus(true)
                        button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
                    }
                }
            } else {
                Log.d("THREADMAIN", "RETURN TIMEOUT")
                ModelSystemValues.message = "No DMX device found!"
                ModelRemoteInfo.setConnectionStatus(false)
                if (!ModelRemoteInfo.getAlertStatus()) {
                    ModelRemoteInfo.setAlertStatus(true)
                }
                button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
            }
            //Reset Thread Return
            ModelThreadReturn.available = false
            delay(1000)
            checkConnection = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun wakeDevice() {
        Log.d("DEVICE", "Wake up")
        Thread(ModelClientOutput(ModelRemoteInfo.getRemoteIP(),
                ModelRemoteInfo.getRemotePort(),
                ModelMessages.wakeup)).start()
        ModelRemoteInfo.setConnectionStatus(true)
        button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun resetDevice() {
        Log.d("DEVICE", "Reset")
        Thread(ModelClientOutput(ModelRemoteInfo.getRemoteIP(),
                ModelRemoteInfo.getRemotePort(),
                ModelMessages.breakmode)).start()
        ModelRemoteInfo.setConnectionStatus(true)
        button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun wakeDialog() {
        val alertDialog = AlertDialog.Builder(this@ControllerMainActivity).create()
        alertDialog.setCancelable(false)
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("ESP32 on Standby, press Wake to restore channel" +
                "levels from ESP32 or press Reset to set all levels to zero")
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "WAKE"
        ) { dialog, _ ->
            dialog.dismiss()
            wakeDevice()
        }
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "RESET"
        ) { dialog, _ ->
            dialog.dismiss()
            resetDevice()
        }
        alertDialog.show()
    }

    private fun errorDialog(message: String) {
        val alertDialog = AlertDialog.Builder(this@ControllerMainActivity).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "DISMISS"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }


    private suspend fun searchDialog() {
        //Creates Blocking dialog while app is searching network for ESP32
        Log.d("DIALOG", "Search is called")
        searchDialog = AlertDialog.Builder(this@ControllerMainActivity).create()
        searchDialog!!.setTitle("Hold on a minute")
        searchDialog!!.setMessage("Searching for DMX device")
        searchDialog!!.setCancelable(false)
        searchDialog!!.show()
        delay(1000)
        while (!ModelRemoteInfo.getAlertStatus()) {
            searchDialog!!.setMessage("Searching for DMX device")
            delay(250)
            searchDialog!!.setMessage("Searching for DMX device.")
            delay(250)
            searchDialog!!.setMessage("Searching for DMX device..")
            delay(250)
            searchDialog!!.setMessage("Searching for DMX device...")
            delay(250)
        }
        if (!ModelRemoteInfo.getConnectionStatus()) {
            searchDialog!!.setTitle("Alert")
            searchDialog!!.setMessage(ModelSystemValues.message)
            delay(2000)
        }
        searchDialog!!.dismiss()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDestroy() {
        if(searchDialog != null && searchDialog!!.isShowing){
            searchDialog!!.dismiss()
        }
        super.onDestroy()
        //When App is closed put the ESP32 into standby mode
        Thread(ModelClientOutput(ModelRemoteInfo.getRemoteIP(),
                ModelRemoteInfo.getRemotePort(),
                ModelMessages.standby)).start()
    }

}
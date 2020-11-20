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


class MainActivity : AppCompatActivity() {


    private var checkConnection: Boolean = false

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val constLayout = this.main_layout
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


        val theme = (PrefAccessOr.getValue(
                this,
                "Theme",
                "SYSTEM DEFAULT"
        ))
        Log.d("THEME", theme)
        when(theme) {
            //Set Default Theme
            "SYSTEM DEFAULT" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                ListenerDaemon.theme = "SYSTEM DEFAULT"
            }
            //Set Light Mode
            "LIGHT THEME" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                ListenerDaemon.theme = "LIGHT THEME"
            }
            //Set Dark Mode
            "DARK THEME" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                ListenerDaemon.theme = "DARK THEME"
            }
        }

        //Set EditText Color Based on Theme
        when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                ListenerDaemon.mode = 2
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                ListenerDaemon.mode = 1
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                ListenerDaemon.mode = 0
            }
        }


        if (!ListenerDaemon.status) {
            Thread(ClientListenUDP()).start()
            ListenerDaemon.status = true
        }

        setConnection()
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

        button_dcontrol.setOnClickListener {
            Log.d("CLICK", "DMX REMOTE")
            goToDirect()
        }

        button_cuelist.setOnClickListener {
            Log.d("CLICK", "SETTINGS")
            goToCueList()
        }


        button_settings.setOnClickListener {
            Log.d("CLICK", "SETTINGS")
            val set_dialog = SettingsDialog(this)
            set_dialog.show()
        }

    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        setConnection()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDestroy() {
        super.onDestroy()
        Thread(ClientSendUDP(RemoteDevice.getRemoteIP(),
                RemoteDevice.getRemotePort(),
                Message.standby)).start()
    }


    private fun goToDirect() {
        val intent = Intent(this, DirectActivity::class.java).apply {}
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


    private fun goToCueList() {
        val intent = Intent(this, CueListActivity::class.java).apply {}
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun setConnection() {
        //Get Wifi Gateway Address
        val wifi: WifiManager
        wifi = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val remoteAddress: String? = RemoteConnection.getIPAddress(wifi)

        //If not null print wifi gateway address to log
        if (remoteAddress != null) {
            //Start a UDP Client Sender Thread
            RemoteDevice.setAddress(remoteAddress)
            Thread(ClientSendUDP(RemoteDevice.getRemoteIP(),
                    RemoteDevice.getRemotePort(),
                    Message.connect)).start()
            verifyConnect()
            Log.d("IP_CHECK", remoteAddress)
        } else {
            RemoteDevice.setConnectionStatus(false)
            button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
            Log.d("IP_CHECK", "NULL")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun verifyConnect() {
        GlobalScope.launch(context = Dispatchers.Main) {
            var timeout = false
            var countdown = 30
            while (!ThreadReturn.available) {
                delay(100)
                countdown -= 1
                if (countdown == 0) {
                    timeout = true
                    break
                }
            }
            if (!timeout) {
                if (ThreadReturn.message == "</INITIAL>" ||
                        ThreadReturn.message == "</DEJA_VU>") {
                    Log.d("THREADMAIN", "RETURN CONNECTED")
                    RemoteDevice.setConnectionStatus(true)
                    button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
                } else if (ThreadReturn.message == "</WAKE_UP>") {
                    Log.d("THREADMAIN", "WAKE UP")
                    RemoteDevice.setConnectionStatus(true)
                    button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
                } else if (ThreadReturn.message == "</BRAKMOD>") {
                    Log.d("THREADMAIN", "Break Mode")
                    RemoteDevice.setConnectionStatus(true)
                    button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
                } else if (ThreadReturn.message == "</STANDBY>") {
                    wakeDialog()
                } else {
                    Log.d("THREADMAIN", "INVALID RETURN")
                    Log.d("RETURN", ThreadReturn.message)
                    if (!RemoteDevice.getAlertStatus()) {
                        ListenerDaemon.message = "Invalid response from device: " + ThreadReturn.message
                        RemoteDevice.setConnectionStatus(false)
                        RemoteDevice.setAlertStatus(true)
                    } else {
                        errorDialog("Invalid response from device: " + ThreadReturn.message)
                        RemoteDevice.setConnectionStatus(false)
                    }
                    button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
                }
            } else {
                Log.d("THREADMAIN", "RETURN TIMEOUT")
                ListenerDaemon.message = "No DMX device found!"
                RemoteDevice.setConnectionStatus(false)
                if (!RemoteDevice.getAlertStatus()) {
                    RemoteDevice.setAlertStatus(true)
                }
                button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
            }
            //Reset Thread Return
            ThreadReturn.available = false
            delay(1000)
            checkConnection = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun wakeDevice() {
        Log.d("DEVICE", "Wake up")
        Thread(ClientSendUDP(RemoteDevice.getRemoteIP(),
                RemoteDevice.getRemotePort(),
                Message.wakeup)).start()
        RemoteDevice.setConnectionStatus(true)
        button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun resetDevice() {
        Log.d("DEVICE", "Reset")
        Thread(ClientSendUDP(RemoteDevice.getRemoteIP(),
                RemoteDevice.getRemotePort(),
                Message.breakmode)).start()
        RemoteDevice.setConnectionStatus(true)
        button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun wakeDialog() {
        val alertDialog = AlertDialog.Builder(this@MainActivity).create()
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
        val alertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "DISMISS"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }


    private suspend fun searchDialog() {
        Log.d("DIALOG", "Search is called")
        if (ListenerDaemon.search) {
        } else {
            val searchDialog = AlertDialog.Builder(this@MainActivity).create()
            searchDialog.setTitle("Hold on a minute")
            searchDialog.setMessage("Searching for DMX device")
            searchDialog.setCancelable(false)
            searchDialog.show()
            delay(1000)
            while (!RemoteDevice.getAlertStatus()) {
                searchDialog.setMessage("Searching for DMX device")
                delay(250)
                searchDialog.setMessage("Searching for DMX device.")
                delay(250)
                searchDialog.setMessage("Searching for DMX device..")
                delay(250)
                searchDialog.setMessage("Searching for DMX device...")
                delay(250)
            }
            if (!RemoteDevice.getConnectionStatus()) {
                searchDialog.setTitle("Alert")
                searchDialog.setMessage(ListenerDaemon.message)
                delay(2000)
            }
            searchDialog.dismiss()
        }
    }

}
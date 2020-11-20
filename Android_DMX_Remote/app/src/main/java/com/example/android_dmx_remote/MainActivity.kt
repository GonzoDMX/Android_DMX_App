package com.example.android_dmx_remote

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)



        if (!ListenerDaemon.status) {
            Thread(ClientListenUDP()).start()
            ListenerDaemon.status = true
        }

        setConnection()

        button_online.setOnClickListener{
            if (!checkConnection) {
                checkConnection = true
                setConnection()
            }
        }

        button_dcontrol.setOnClickListener {
            Log.d("GOTO", "Direct Control")
            goToDirect()
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
                }
                else if (ThreadReturn.message == "</WAKE_UP>") {
                    Log.d("THREADMAIN", "WAKE UP")
                    RemoteDevice.setConnectionStatus(true)
                    button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
                }
                else if (ThreadReturn.message == "</BRAKMOD>") {
                    Log.d("THREADMAIN", "Break Mode")
                    RemoteDevice.setConnectionStatus(true)
                    button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.GREEN)
                }
                else if (ThreadReturn.message == "</STANDBY>") {
                    wakeDialog()
                } else {
                    Log.d("THREADMAIN", "INVALID RETURN")
                    Log.d("RETURN", ThreadReturn.message)
                    errorDialog("Invalid response from device: " + ThreadReturn.message)
                    RemoteDevice.setConnectionStatus(false)
                    button_online.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
                }
            } else {
                Log.d("THREADMAIN", "RETURN TIMEOUT")
                errorDialog("No device detected!")
                RemoteDevice.setConnectionStatus(false)
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
        ) { dialog, which -> dialog.dismiss()
            wakeDevice() }
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "RESET"
        ) { dialog, which -> dialog.dismiss()
            resetDevice() }
        alertDialog.show()
    }

    private fun errorDialog(message: String) {
        val alertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "DISMISS"
        ) { dialog, which -> dialog.dismiss() }
        alertDialog.show()
    }

}

package com.example.android_dmx_remote

import android.location.Address

object RemoteDevice {
    private var ipAddress = "192.168.4.1"
    private var port = 4210
    private var status = false

    fun setAddress(address: String) {
        ipAddress = address
    }

    fun setPort(remote: Int) {
        port = remote
    }

    fun setConnectionStatus(flag: Boolean) {
        status = flag
    }

    fun getRemoteIP(): String {
        return ipAddress
    }

    fun getRemotePort(): Int {
        return port
    }

    fun getConnectionStatus(): Boolean{
        return status
    }
}
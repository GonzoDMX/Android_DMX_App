package com.example.android_dmx_remote

import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder

object RemoteConnection {

    fun getIPAddress(wifiManager: WifiManager): String? {

        var ipAddress = wifiManager.dhcpInfo.gateway
        //var ipAddress = dhcpInfo.gateway
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            ipAddress = Integer.reverseBytes(ipAddress)
        }
        val ipByteArray: ByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
        return try {
            (InetAddress.getByAddress(ipByteArray).hostAddress).toString()
        }
        catch (ex: UnknownHostException) {
            null
        }
    }

}
package com.example.android_dmx_remote

import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

class ModelChannelOutput(ip: String?, port: Int, target: ArrayList<Int>) : Runnable {

    private var ip = ip
    private var port = port
    private var target = target

    override fun run() {
        Log.d("INCOMING", target.toString())
        val udpSocket = DatagramSocket(port)
        val serverAddr = InetAddress.getByName(ip)
        try {
            val message = getByteString(target)
            val packet = DatagramPacket(message, message.size, serverAddr, port)
            udpSocket.send(packet)
            udpSocket.close()
        } catch (e: SocketException) {
            Log.e("Udp:", "Socket Error:", e)
            udpSocket.close()
        } catch (e: IOException) {
            Log.e("Udp Send:", "IO Error:", e)
            udpSocket.close()
        }
    }

    private fun getByteString(target: ArrayList<Int>): ByteArray {
        val maxSize = target.size
        val maxchannel : String = when {
            maxSize > 99 -> { maxSize.toString() }
            maxSize > 9 -> { "0$maxSize" }
            else -> { "00$maxSize" }
        }
        val head = "<CHANNEL>$maxchannel"
        var header = head.toByteArray()
        for (i in 0 until maxSize) {
            header += target[i].toByte()
        }
        //header = header.plus("\r".toByte())
        return header
    }

}
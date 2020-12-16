package com.example.android_dmx_remote

import android.os.StrictMode
import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

class ModelClientOutput(ip: String?, port: Int, message: ByteArray) : Runnable {

    private var ip = ip
    private var port = port
    private var message = message

    override fun run() {
        val udpSocket = DatagramSocket(port)
        val serverAddr = InetAddress.getByName(ip)
        try {
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
}
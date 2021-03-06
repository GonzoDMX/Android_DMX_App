package com.example.android_dmx_remote

import android.util.Log
import java.io.IOException
import java.net.*


class ModelClientOutIn(ip: String?, port: Int, message: ByteArray) : Runnable {

    private var ip = ip
    private var port = port
    private var message = message

    override fun run() {
        var run = true
        try {
            val udpSocket = DatagramSocket(9001)
            val serverAddr: InetAddress = InetAddress.getByName(ip)
            val packet = DatagramPacket(message, message.size, serverAddr, port)
            udpSocket.send(packet)
            while (run) {
                try {
                    ModelThreadReturn.message = ""
                    val message = ByteArray(560)
                    val recPacket = DatagramPacket(message, message.size)
                    Log.i("UDP client: ", "about to wait to receive")
                    udpSocket.soTimeout = 2000
                    udpSocket.receive(recPacket)
                    val text = String(message, 0, recPacket.length)
                    ModelThreadReturn.message = text
                    ModelThreadReturn.available = true
                    Log.d("Received text", text)
                    //udpSocket.close()
                } catch (e: IOException) {
                    Log.e("IOException", "error: ", e)
                    run = false
                    udpSocket.close()
                } catch (e: SocketTimeoutException) {
                    Log.e("Timeout Exception", "UDP Connection:", e)
                    run = false
                    udpSocket.close()
                }
            }
        } catch (e: SocketException) {
            Log.e("Socket Open:", "Error:", e)
        }
    }
}
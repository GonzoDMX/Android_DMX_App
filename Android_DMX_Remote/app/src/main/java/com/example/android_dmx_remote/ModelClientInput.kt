package com.example.android_dmx_remote

import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket

class ModelClientInput() : Runnable {

    private var port = 9001

    @ExperimentalUnsignedTypes
    override fun run() {
        var run = true
        val udpSocket = DatagramSocket(port)
        val message = ByteArray(600)
        val packet = DatagramPacket(message, message.size)
        while (run) {
            try {
                Log.i("UDP client: ", "about to wait to receive")
                udpSocket.receive(packet)
                val text = String(message, 0, packet.length)
                ModelThreadReturn.message = text
                ModelThreadReturn.available = true
                if (text == "</WAKE_UP>") {
                    Log.d("WAKE", "Wake Confirmed")
                    udpSocket.receive(packet)
                    val channels = packet.data
                    for (i in 0 until packet.length) {
                        ModelChannelArray.levels[i] = channels[i].toUByte().toInt()
                    }
                }
                Log.d("Received data", text)
            } catch (e: IOException) {
                Log.e("IOException", "error: ", e)
                run = false
            }
        }
    }
}
package com.example.android_dmx_remote

object ModelMessages {

    //Connect to ESP32
    public val connect = "<CONNECT>\r".toByteArray()

    //Override device connection
    public val overide = "<OVERIDE>\r".toByteArray()

    //Set ESP32 to Standby Mode
    public val standby = "<STANDBY>\r".toByteArray()

    //Wake from Standby but reset all channel values
    public val breakmode = "<BRAKMOD>\r".toByteArray()

    //Recover from Standby, ESP32 Returns current channel values
    public val wakeup = "<WAKE_UP>\r".toByteArray()

    //Reset ESP32
    public val reset = "<!RESET!>\r".toByteArray()

    var udpFlag: String
        get() {
            return udpFlag
            return udpFlag
        }
        set(value) {
        }

}
# Android_DMX_App
An Android-based DMX Lighting Controller

## <b>ESP32 Wifi Access Point to DMX Widget:</b>
In order to actually control DMX based lights the android application communicates with an ESP32 Dev board.
The schematic used can be viewed with KiCad and the code was written with Arduino IDE using a slightly modified ESP32 core and the LXESP32DMX Library available here: https://github.com/claudeheintz/LXESP32DMX

The android app sends UDP messages over the network to control the functionality of the ESP32. If you want to use this ESP32-based setup with your own application the protocol is pretty basic:
* When connecting first send message \<CONNECT\> to the ESP32
  1. If it is the first connection of the session the ESP32 responds with \</INITIAL\>
  2. If the ESP32 is in standby mode it will respond with \</RESUME?\>
* Send \<STANDBY\> to put ESP32 in Standby mode it will confirm with a reply of \</STANDBY\>
  1. Send \<WAKE_UP\> to resume from standby mode, ESP32 will reply with \</WAKE_UP\> followed by 512 bytes giving the current value of each DMX channel in the universe, ESP32 then returns to active mode.
  2. Send \<BRAKMOD\> to reset ESP32 from standby mode, All DMX Channels will be zeroed ESP32 then returns to active mode.
* When in active mode send ESP32 \<CHANNEL\> a three digit number in Ascii followed by a byte string to set channel levels
  1. Example: '\<CONNECT\>003/0xFF/0x00/0xA0' will set channel 1 to 255, channel 2 to 0 and channel 3 to 160
  2. '003' is the number of channels you wish to set (starting from 1, max 512)
  3. ESP32 will reply with \</\>

Note: ESP32 will reply with \</INVALID\> to all erroneous or poorly formed messages

#include <WiFi.h>
#include <WiFiUdp.h>
#include <LXESP32DMX.h>
#include "esp_task_wdt.h"

//WIFI Variables------------------------------
const char* ssid = "ESP32_of_doom";
const char* password = "thiswillbeyourend";

WiFiUDP Udp;
unsigned int localUdpPort = 4210;  // local port to listen on
char incomingPacket[548];  // buffer for incoming packets
char  replyPacket[] = "Hi there! Got the message :-)";  // a reply string to send back
char errorReply[] = "INVALID MESSAGE!\n";
//WIFI Variables------------------------------END//



//DMX Variables-------------------------------
#define DMX_DIRECTION_PIN 21
#define DMX_SERIAL_OUTPUT_PIN 17

uint8_t level;
uint8_t dmxbuffer[DMX_MAX_FRAME];

char head[9];
char chanCount[3];

void copyDMXToOutput(void) {
  xSemaphoreTake( ESP32DMX.lxDataLock, portMAX_DELAY );
  for (int i = 1; i < DMX_MAX_FRAME; i++) {
    ESP32DMX.setSlot(i , dmxbuffer[i]);
  }
  xSemaphoreGive( ESP32DMX.lxDataLock );
}
//DMX Variables-------------------------------END//


bool standbyMode = false;


void setup() {
  Serial.begin(115200);

  pinMode(DMX_DIRECTION_PIN, OUTPUT);
  digitalWrite(DMX_DIRECTION_PIN, HIGH);

  pinMode(DMX_SERIAL_OUTPUT_PIN, OUTPUT);
  ESP32DMX.startOutput(DMX_SERIAL_OUTPUT_PIN);

  WiFi.softAP(ssid, password);

  Udp.begin(WiFi.softAPIP(), localUdpPort);
  Serial.printf("Now listening at IP %s, UDP port %d\n", WiFi.softAPIP().toString().c_str(), localUdpPort);

  for (int i = 0; i < 512; i++){
    dmxbuffer[i + 1] = 0;
  }
  copyDMXToOutput();
}


void loop() {
 
  int packetSize = Udp.parsePacket();
  if (packetSize){
    // receive incoming UDP packets
    Serial.printf("Received %d bytes from %s, port %d\n", packetSize, Udp.remoteIP().toString().c_str(), Udp.remotePort());
    int len = Udp.read(incomingPacket, 548);
    if (len > 9) {
      int index = 0;
      for (index; index < 9; index++){
        head[index] = incomingPacket[index];
        }

      
      /*-------PARSE INCOMING CHANNEL DATA-------*/
      /*-----------------------------------------*/
      if (strcmp(head, "<CHANNEL>") == 0){
        Serial.print("Set Channels");
        int chan = 0;
        for (index; index < 12; index++){
          chanCount[chan] = incomingPacket[index];
          chan++;
        }
        chan = 1;
        int channels = atoi(chanCount);
        Serial.printf("Number of Channels: %d\n", channels);
        for (index; index < channels + 12; index++) {
          dmxbuffer[chan] = incomingPacket[index];
          chan++;
          if (chan == 513 || chan > channels){
            break;
          }
        }
        copyDMXToOutput();
        char msg[] = {'<', '/', '>'};
        Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
        for (int i = 0; i < 3; i++){
          Udp.write(msg[i]);
        }
        Udp.endPacket();
      }


      /*--------------NEW DEVICE IS CONNECTING-------------*/
      /*-----(If device in standby mode ask to resume)-----*/
      else if (strcmp(head, "<CONNECT>")) {
        Serial.print("Connect");
        if(standbyMode == true){
          char msg[] = {'<', '/', 'R', 'E', 'S', 'U', 'M', 'E', '?', '>'};
          Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
          for (int i = 0; i < 10; i++){
            Udp.write(msg[i]);
          }
          Udp.endPacket();
        }
        else {
          char msg[] = {'<', '/', 'I', 'N', 'I', 'T', 'I', 'A', 'L', '>'};
          Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
          for (int i = 0; i < 10; i++){
            Udp.write(msg[i]);
          }
          Udp.endPacket();
        }
      }


      /*-------SET DEVICE TO STANDBY MODE-------*/
      /*----------------------------------------*/
      else if (strcmp(head, "<STANDBY>") == 0) {
        Serial.print("Standby");
        char msg[] = {'<', '/', 'S', 'T', 'A', 'N', 'D', 'B', 'Y', '>'};
        Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
        for (int i = 0; i < 10; i++){
          Udp.write(msg[i]);
        }
        Udp.endPacket();
        standbyMode = true;
      }
      

      /*------------------BREAK MODE---------------------*/
      /*--(If resume is cancelled by connecting device)--*/
      else if (strcmp(head, "<BRAKMOD>") == 0) {
        Serial.print("Standby");
        char msg[] = {'<', '/', 'B', 'R', 'A', 'K', 'M', 'O', 'D', '>'};
        Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
        for (int i = 0; i < 10; i++){
          Udp.write(msg[i]);
        }
        Udp.endPacket();
        for (int i = 0; i < 512; i++){
          dmxbuffer[i + 1] = 0;
        }
        copyDMXToOutput();
        standbyMode = false;
      }


      /*---------WAKE UP DEVICE FROM STANDBY--------*/
      /*------ (RETURNS CURRENT CHANNEL STATES)-----*/
      else if (strcmp(head, "<WAKE_UP>") == 0) {
        Serial.print('Wake Up');
        char msg[] = {'<', '/', 'W', 'A', 'K', 'E', '_', 'U', 'P', '>'};
        Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
        for (int i = 0; i < 10; i++){
          Udp.write(msg[i]);
        }
        for (int i = 0; i < 512; i++) {
          Udp.write(dmxbuffer[i + 1]);
        }
        Udp.endPacket();
        standbyMode = false;
      }


      /*-----INVALID MESSAGE RECEIVED-----*/
      /*----------------------------------*/
      else{
        char msg[] = {'<', '/', 'I', 'N', 'V', 'A', 'L', 'I', 'D', '>'};
        Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
        for (int i = 0; i < 10; i++){
          Udp.write(msg[i]);
        }
        Udp.endPacket();
      }
    Serial.printf("UDP packet contents: %s\n", incomingPacket);
    }
  esp_task_wdt_feed();
  }
}

//END

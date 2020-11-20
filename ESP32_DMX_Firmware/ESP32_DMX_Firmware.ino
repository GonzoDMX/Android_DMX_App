/*
 *  Created by: Andrew O'Shei
 *  Date: October 29, 2020
 *  Description: Code for ESP32, sets ESP32 in Access Point Mode
 *  User can connect and control DMX output via UDP message strings
 * 
 */

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

const int remPort = 9001;
//WIFI Variables------------------------------END//


#define LED_RED 26
#define LED_BLUE 25

const long ledDelay = 250;

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

IPAddress currentIP;

bool initConnect = true;


void setup() {
  Serial.begin(115200);

  pinMode(LED_RED, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);

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

  digitalWrite(LED_BLUE, HIGH);
  
}



void returnMsg(int index) {
  Udp.beginPacket(Udp.remoteIP(), remPort);
  switch (index) {
    case 1:
    {
      char msg[] = {'<', '/', 'I', 'N', 'I', 'T', 'I', 'A', 'L', '>'};
      for (int i = 0; i < 10; i++){
        Udp.write(msg[i]);
      }
      break;
    }
    case 2:
    {
      char msg[] = {'<', '/', 'S', 'T', 'A', 'N', 'D', 'B', 'Y', '>'};
      for (int i = 0; i < 10; i++){
        Udp.write(msg[i]);
      }
      break;
    }
    case 3:
    {
      char msg[] = {'<', '/', 'O', 'V', 'E', 'R', 'I', 'D', 'E', '>'};
      for (int i = 0; i < 10; i++){
        Udp.write(msg[i]);
      }
      break;
    }
    case 4:
    {
      char msg[] = {'<', '/', 'B', 'R', 'A', 'K', 'M', 'O', 'D', '>'};
      for (int i = 0; i < 10; i++){
        Udp.write(msg[i]);
      }
      break;
    }
    case 5:
    {
      char msg[] = {'<', '/', 'D', 'E', 'J', 'A', '_', 'V', 'U', '>'};
      for (int i = 0; i < 10; i++){
        Udp.write(msg[i]);
      }
      break;
    }
    case 6:
    {
      char msg[] = {'<', '/', 'I', 'N', 'V', 'A', 'L', 'I', 'D', '>'};
      for (int i = 0; i < 10; i++){
        Udp.write(msg[i]);
      }
      break;
    }
    case 7:
    {
      char msg[] = {'<', '/', 'N', 'O', 'N', 'C', 'O', 'N', 'N', '>'};
      for (int i = 0; i < 10; i++){
        Udp.write(msg[i]);
      }
      break;
    }
    default:
    {
      break;
    }
  }
  Udp.endPacket(); 
}
  

void loop() {
 
  int packetSize = Udp.parsePacket();
  if (packetSize){
    // receive incoming UDP packets
    Serial.printf("Received %d bytes from %s, port %d\n", packetSize, Udp.remoteIP().toString().c_str(), Udp.remotePort());
    int len = Udp.read(incomingPacket, 548);
    //Check incoming packet size
    Serial.printf(incomingPacket);
    if (len > 9) {
      int index = 0;
      for (index; index < 9; index++){
        head[index] = incomingPacket[index];
        }
      
      /*-------PARSE INCOMING CHANNEL DATA-------*/
      /*-----------------------------------------*/
      if (strcmp(head, "<CHANNEL>") == 0) {
        if (initConnect == false) {
          if (standbyMode == false) {
            //Check remote credentials
            if (Udp.remoteIP() == currentIP){
              Serial.print("Set Channels");
              char msg[] = {'<', '/', ' ', ' ', ' ', '>'};
              int chan = 0;
              //Get number of channels/byte string length
              for (index; index < 12; index++){
                chanCount[chan] = incomingPacket[index];
                msg[chan + 2] = incomingPacket[index];
                chan++;
              }
              chan = 1;
              //Set new channel values to DMX buffer and output
              int channels = atoi(chanCount);
              for (index; index < channels + 12; index++) {
                dmxbuffer[chan] = incomingPacket[index];
                chan++;
                if (chan == 513 || chan > channels){
                  break;
                }
              }
              copyDMXToOutput();
              //Send reply message </'# of Channels Set'>
              Udp.beginPacket(Udp.remoteIP(), remPort);
              for (int i = 0; i < 6; i++){
                Udp.write(msg[i]);
              }
              Udp.endPacket();
            }
          else{
            //Send override message, remote is not the master
            returnMsg(3);
            }
          }
          else{
            //Send standby message, device is in standby
            returnMsg(2);
            }
          }
          else {
            //Send No Connection message, remote is not connected
            returnMsg(7);
          }
        }


      /*--------------NEW DEVICE IS CONNECTING-------------*/
      /*-----(If device in standby mode ask to resume)-----*/
      else if (strcmp(head, "<CONNECT>") == 0) {
        Serial.print("Connect");
        //Check remote credentials
        if (currentIP != Udp.remoteIP()){
          //If first connection in this session
          if (initConnect == true) {
            //Set remote credentials
            currentIP = Udp.remoteIP();
            //Reply with Initial message, device connection initialized
            returnMsg(1);
            //Signal Active mode engaged
            digitalWrite(LED_RED, HIGH);
            initConnect = false;
            }
          else{
            //Send override message, remote is not the master
            returnMsg(3);
          }
        }
        else {
          if(standbyMode == true){
            //Send Standby message, device is in standby mode
            returnMsg(2);
          }
          else{
            //Send deja vu message, remote is already connected
            returnMsg(5);
            }
          }
      }


      /*-------Device Override------------------*/
      /*----------------------------------------*/
      else if (strcmp(head, "<OVERIDE>") == 0) {
        Serial.print("Override");
        if (initConnect == false) {
          //Check remote credentials
          if (currentIP != Udp.remoteIP()){
            //Set new remote credentials
            currentIP = Udp.remoteIP();
            //Send Initial message, new connection has been initialized
            returnMsg(1);
            //Set all DMX channels to Zero
            for (int i = 0; i < 512; i++){
              dmxbuffer[i + 1] = 0;
            }
            copyDMXToOutput();
            //If in standby mode set to active
            standbyMode = false;
            //Signal valid connection
            digitalWrite(LED_RED, HIGH);
            }
          else{
            //Send deja vu message, remote is already connected
            returnMsg(5);
          }
        }
        else {
          //Send No Connection message, remote is not connected
          returnMsg(7);
        }
      }
      

      /*-------SET DEVICE TO STANDBY MODE-------*/
      /*----------------------------------------*/
      else if (strcmp(head, "<STANDBY>") == 0) {
        Serial.print("Standby");
        if (initConnect == false) {
          //Check remote Credentials
          if (Udp.remoteIP() == currentIP){
            //Send Standby message, device is in standby mode
            returnMsg(2);
            //Set device to active mode
            standbyMode = true;
            digitalWrite(LED_RED, LOW);
            }
          else{
            //Send override message, remote is not the master
            returnMsg(3);
          }
        }
        else {
          //Send No Connection message, remote is not connected
          returnMsg(7);
        }
      }
      

      /*------------------BREAK MODE---------------------*/
      /*--(If resume is cancelled by connecting device)--*/
      else if (strcmp(head, "<BRAKMOD>") == 0) {
        Serial.print("Standby");
        if (initConnect == false) {
          //Checl remote credentials
          if (Udp.remoteIP() == currentIP){
            //Send Break Mode confirmation message
            returnMsg(4);
            //Reset all DMX channels to Zero
            for (int i = 0; i < 512; i++){
              dmxbuffer[i + 1] = 0;
            }
            copyDMXToOutput();
            //Set device to active mode
            standbyMode = false;
            digitalWrite(LED_RED, HIGH);
            }
          else{
            //Send override message, remote is not the master
            returnMsg(3);
          }
        }
        else {
          //Send No Connection message, remote is not connected
          returnMsg(7);
        }
      }


      /*---------WAKE UP DEVICE FROM STANDBY--------*/
      /*------ (RETURNS CURRENT CHANNEL STATES)-----*/
      else if (strcmp(head, "<WAKE_UP>") == 0) {
        Serial.print('Wake Up');
        if (initConnect == false){
          //Check remote credentials
          if (Udp.remoteIP() == currentIP){
            char msg[] = {'<', '/', 'W', 'A', 'K', 'E', '_', 'U', 'P', '>'};
            Udp.beginPacket(Udp.remoteIP(), remPort);
            for (int i = 0; i < 10; i++){
              Udp.write(msg[i]);
            }
            Udp.endPacket();
            Udp.beginPacket(Udp.remoteIP(), remPort);
            for (int i = 0; i < 512; i++) {
              Udp.write(dmxbuffer[i + 1]);
            }
            Udp.endPacket();
            standbyMode = false;
            digitalWrite(LED_RED, HIGH);
          }
          else{
            //Send override message, remote is not the master
            returnMsg(3);
          }
      }
      else {
        //Send No Connection message, remote is not connected
        returnMsg(7);
        }
      }


      /*-------SEND REMOTE RESET COMMAND-------*/
      /*---------------------------------------*/
      else if (strcmp(head, "<!RESET!>") == 0) {
        digitalWrite(LED_RED, LOW);
        while (1) {
          digitalWrite(LED_BLUE, HIGH);
          delay(250);
          digitalWrite(LED_BLUE, LOW);
          delay(250);
          }
      }


      /*-----INVALID MESSAGE RECEIVED-----*/
      /*----------------------------------*/
      else{
        //Packet not properly formatted therefore signal invalid message
        returnMsg(6);
      }
    }
    else{
      //Packet to small therefore signal invalid message
      returnMsg(6);
    }
  }
  //Reset Watch Dog Timer, else device will auto reset after a second
  esp_task_wdt_feed();
}

//END

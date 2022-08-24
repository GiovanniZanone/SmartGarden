   # SmartGarden
<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/splashscreen_foreground.png" alt="Logo" width="80" height="80">
     <h3 align="center">An IOT project to automate your garden.</h3>
  </div>

<!-- GETTING STARTED -->
## Getting Started
To recreate this project you will need Arduino hardware components and an active RealTime Database on Firebase.

### Prerequisites
Here's the Arduino components needed for the project:
* Arduino MKR 1010 Wifi
* Arduino MKR IoT carrier
* Moisture sensor connected to A5 pin
* Firebase_Arduino_WiFiNINA library installed on the machine

Then you'll need a functioning Realtime Database on Firebase, to set up correctly the Database use this guide:
https://firebase.google.com/docs/database?authuser=0 

<!-- USAGE EXAMPLES -->
## Usage
Put the device in your garden with the moisture sensor inside the soil, you can check values like temperature, humidity, pressure, light and soil moisture from the screen using the buttons or using the app. The app also allows you to remotely control the water pump, the lights and a fan. It also gives you a weather forecast for the next twelve hours thanks to the implementation of the Zambretti algorithm and it is possible to observe the trend of the sensor values in different time frames through the graphs page.

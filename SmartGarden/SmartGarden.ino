#include "Firebase_Arduino_WiFiNINA.h"
#include <Arduino_MKRIoTCarrier.h>
#include <ArduinoJson.h>

/* fill with your credentials before use */
#define FIREBASE_HOST ""
#define FIREBASE_AUTH ""
#define WIFI_SSID ""
#define WIFI_PASSWORD ""

//Variables declaration
MKRIoTCarrier carrier;

int moistPin = A5;

String waterPumpState = "PUMP: OFF";
String coolingFanState = "FAN: OFF";
String lightState = "LIGHTS: OFF";

uint32_t lightsOn = carrier.leds.Color(82, 118, 115);
uint32_t lightsOff = carrier.leds.Color(0, 0, 0);

float humidity;
float temperature;
float pressure;
int light;
int moisture;
bool artificial_light = false;
bool cooling_fan = false;
;
bool waterpump = false;
;

// Variable to save current epoch time
unsigned long epochTime;

String path = "/SmartGarden";  //Base path to Firebase Realtime Database
String jsonStr;

//Define Firebase data object
FirebaseData firebaseData;

//Functions declaration
void checkChanges() {
  Serial.println("Getting devices status");
  if (Firebase.getJSON(firebaseData, path + "/Devices")) {
    Serial.println("Read result");
    Serial.println("PATH: " + firebaseData.dataPath());
    Serial.println("TYPE: " + firebaseData.dataType());
    Serial.print("VALUE: ");
    if (firebaseData.dataType() == "json") {
      Serial.println(firebaseData.jsonData());
      DynamicJsonDocument doc(1024);
      deserializeJson(doc, firebaseData.jsonData());
      artificial_light = doc["light"];
      cooling_fan = doc["fan"];
      waterpump = doc["pump"];
    }
  } else {
    Serial.println("ERROR: " + firebaseData.errorReason());
    Serial.println();
  }
}

void onWaterpumpChange() {
  if (waterpump == true) {
    carrier.Relay2.open();
    waterPumpState = "PUMP: ON";
  } else {
    carrier.Relay2.close();
    waterPumpState = "PUMP: OFF";
  }
}

void onCoolingFanChange() {
  if (cooling_fan == true) {
    carrier.Relay1.open();
    coolingFanState = "FAN: ON";
  } else {
    carrier.Relay1.close();
    coolingFanState = "FAN: OFF";
  }
}

void onArtificialLightChange() {
  if (artificial_light == true) {
    carrier.leds.fill(lightsOn, 0, 5);
    carrier.leds.show();
    lightState = "LIGHTS: ON";
  } else {
    carrier.leds.fill(lightsOff, 0, 5);
    carrier.leds.show();
    lightState = "LIGHTS: OFF";
  }
}

//Update displayed Info
void updateScreen() {
  carrier.display.fillScreen(ST77XX_BLACK);
  carrier.display.setTextColor(ST77XX_WHITE);
  carrier.display.setTextSize(3);

  carrier.display.setCursor(40, 50);
  carrier.display.print(waterPumpState);
  carrier.display.setCursor(40, 90);
  carrier.display.print(coolingFanState);
  carrier.display.setCursor(40, 130);
  carrier.display.print(lightState);
}

//initial setup
void setup() {
  Serial.begin(115200);
  delay(1500);
  CARRIER_CASE = true;
  carrier.begin();

  testroundrects();
  delay(2000);

  testtriangles();

  Serial.print("Connecting Wi-Fi");

  int status = WL_IDLE_STATUS;

  while (status != WL_CONNECTED) {
    status = WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected to this IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();
  Serial.println("------------------------------------------------------------------------------------");

  //--------------------------------------------------------------------------

  //Connecting to Firebase

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH, WIFI_SSID, WIFI_PASSWORD);
}

void loop() {
  //listen to buttons
  carrier.Buttons.update();

  //read temperature,humidity and pressure
  temperature = carrier.Env.readTemperature();
  humidity = carrier.Env.readHumidity();
  pressure = carrier.Pressure.readPressure();

  //read raw moisture value
  int raw_moisture = analogRead(moistPin);
  //map raw moisture to a scale of 0 - 100
  moisture = map(raw_moisture, 0, 1023, 100, 0);
  //read ambient light
  while (!carrier.Light.colorAvailable()) {
    delay(5);
  }
  int none;  //We dont need RGB colors
  carrier.Light.readColor(none, none, none, light);

  delay(100);
  //button inputs
  if (carrier.Buttons.getTouch(TOUCH0)) {
    carrier.display.fillScreen(ST77XX_WHITE);
    carrier.display.setTextColor(ST77XX_RED);
    carrier.display.setTextSize(2);

    carrier.display.setCursor(30, 110);
    carrier.display.print("Temp: ");
    carrier.display.print(temperature);
    carrier.display.print(" C");
  }

  if (carrier.Buttons.getTouch(TOUCH1)) {
    carrier.display.fillScreen(ST77XX_WHITE);
    carrier.display.setTextColor(ST77XX_RED);
    carrier.display.setTextSize(2);

    carrier.display.setCursor(30, 110);
    carrier.display.print("Humi: ");
    carrier.display.print(humidity);
    carrier.display.print(" %");
  }

  if (carrier.Buttons.getTouch(TOUCH2)) {
    carrier.display.fillScreen(ST77XX_WHITE);
    carrier.display.setTextColor(ST77XX_RED);
    carrier.display.setTextSize(2);

    carrier.display.setCursor(30, 110);
    carrier.display.print("Light: ");
    carrier.display.print(light);
    carrier.display.print(" Lux");

  }

  if (carrier.Buttons.getTouch(TOUCH3)) {
    carrier.display.fillScreen(ST77XX_WHITE);
    carrier.display.setTextColor(ST77XX_RED);
    carrier.display.setTextSize(2);

    carrier.display.setCursor(10, 110);
    carrier.display.print("Pressure: ");
    carrier.display.print(pressure);
    carrier.display.print("kPa");

  }

  if (carrier.Buttons.getTouch(TOUCH4)) {
    carrier.display.fillScreen(ST77XX_WHITE);
    carrier.display.setTextColor(ST77XX_RED);
    carrier.display.setTextSize(2);

    carrier.display.setCursor(30, 110);
    carrier.display.print("Moisture: ");
    carrier.display.print(moisture);
    carrier.display.print(" %");
  }
  delay(3000);

  //getting current time
  epochTime = WiFi.getTime();
  delay(1000);
  Serial.println("------------------------------------------------------------------------------------");
  //--------------------------  Sending data to Database -----------------------------


  Serial.println("Sending current values to database");

  // Start a new line
  Serial.println();
  String json = "{\"pressure\":\"" + String(pressure) + "\",\"humidity\":\"" + String(humidity) + "\",\"temp\":\"" + String(temperature) + "\",\"light\":\"" + String(light) + "\",\"moisture\":\"" + String(moisture) + "\"}";
  Serial.println(json);

  if (Firebase.setJSON(firebaseData, path + "/SensorsData/" + String(epochTime), json)) {
    Serial.println("Inserted");
    Serial.println("PATH: " + firebaseData.dataPath());
    Serial.println("TYPE: " + firebaseData.dataType());
    Serial.print("VALUE: ");
    if (firebaseData.dataType() == "json")
      Serial.println(firebaseData.jsonData());
  } else {
    Serial.println("ERROR : " + firebaseData.errorReason());
    Serial.println();
  }
  checkChanges();
  onWaterpumpChange();
  onCoolingFanChange();
  onArtificialLightChange();
  updateScreen();
}

//loading animations
void setText() {
  carrier.display.setRotation(0);
  carrier.display.setTextColor(ST77XX_WHITE);
  carrier.display.setTextSize(3);
  carrier.display.setCursor(75, 90);
  carrier.display.print("Smart");
  carrier.display.setCursor(65, 130);
  carrier.display.print("Garden");
}
void testtriangles() {
  carrier.display.fillScreen(ST77XX_BLACK);
  uint16_t color = 0xF800;
  int t;
  int w = carrier.display.width() / 2;
  int x = carrier.display.height() - 1;
  int y = 0;
  int z = carrier.display.width();
  for (t = 0; t <= 15; t++) {
    carrier.display.drawTriangle(w, y, y, x, z, x, color);
    x -= 4;
    y += 4;
    z -= 4;
    color += 100;
  }
  setText();
}

void testroundrects() {
  carrier.display.fillScreen(ST77XX_BLACK);
  uint16_t color = 100;
  int i;
  int t;
  for (t = 0; t <= 4; t += 1) {
    int x = 0;
    int y = 0;
    int w = carrier.display.width() - 2;
    int h = carrier.display.height() - 2;
    for (i = 0; i <= 16; i += 1) {
      carrier.display.drawRoundRect(x, y, w, h, 5, color);
      x += 2;
      y += 3;
      w -= 4;
      h -= 6;
      color += 1100;
    }
    color += 100;
  }
  setText();
}
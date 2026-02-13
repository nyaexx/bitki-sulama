<div align="center">
<p>
<img src="https://github.com/nyaexx/bitki-sulama/blob/main/.github/biktisulamayenilogo.png" width="150px">
</p>
<h1>Smart Irrigation</h1>
<p>
🇬🇧 English |
<a href="README-tr.md"> 🇹🇷 Türkçe</a>
</p>
<p>
<a href="https://github.com/nyaexx/bitki-sulama/releases/latest"><img alt="GitHub Release" src="https://img.shields.io/github/v/release/nyaexx/bitki-sulama?label=Smart%20Irrigation" /></a>
<a href="https://github.com/nyaexx/bitki-sulama/releases"><img src="https://img.shields.io/github/downloads/nyaexx/bitki-sulama/total?label=Downloads" alt="Downloads" /></a>
<a href="https://github.com/nyaexx/bitki-sulama/commits/main/"><img alt="GitHub commits since latest release" src="https://img.shields.io/github/commits-since/nyaexx/bitki-sulama/latest?label=Commits%20Since%20Last%20Release" /></a>
<a href="https://github.com/nyaexx/bitki-sulama/blob/master/LICENSE"><img src="https://img.shields.io/github/license/nyaexx/bitki-sulama?label=License" alt="License" /></a>
</p>
</div>

This project enables automatic irrigation using an Arduino circuit by measuring the soil moisture and air values of a specific plant. Additionally, there is an Android application operating via Bluetooth where you can monitor data and perform manual irrigation.

---

### General Features of the Android Application:

- Add multiple devices to the "My Devices" tab.
- Delete and edit devices.
- Connect to added devices from the "My Devices" page to access the management panel.
- Read sensor data from your device via the management panel.
- Manually trigger the water pump (motor) via the management panel.
- Customize the application theme in the settings section.

---

### Circuit Operation Features:

- Workflow:

  - Measurement: Sensors measure soil moisture, air temperature, and air humidity.
  - Decision: When the soil is dry, the system detects this, lights up the red LED, and automatically activates the water pump (relay).
      - It is recommended to adjust the nem_orani (moisture rate) threshold values within the [**void loop()**](https://github.com/nyaexx/bitki-sulama/blob/092b0fe9b20702ee34cc0e6b0391c396f2d98dd1/Arduino%20Kodlar%C4%B1/arduinokodlar%C4%B1.ino#L53) in the Arduino code according to the water requirements of your specific plant type.
  - Information: Soil status (Dry, Normal, Wet) is displayed via LEDs, and all data is sent to the phone via Bluetooth.
  - Remote Control: Manual irrigation can be started at any time with a single command through the app via Bluetooth connection.

- Key Features:

  - Fully Automatic Irrigation: Waters plants on its own when no one is around.
  - Bluetooth Tracking: Provides real-time temperature and humidity monitoring via phone.
  - Status Indicators: Instantly shows whether the soil needs water thanks to the LEDs.

---

### Android App Development:

**Requirements:**

    Android Studio (Arctic Fox or higher)
    
    Kotlin
    
    Gradle (Comes with Android Studio)
    
    Min SDK: 24, Target SDK: 36 (Target SDK is 35 for Release n1.6 and earlier)
    
    Physical Android device (Required for Bluetooth features.)

**How to Run?**

- Clone the Repository:
   -  git clone [https://github.com/nyaexx/bitki-sulama/](https://github.com/nyaexx/bitki-sulama/)
- Open with Android Studio.

- Once Gradle sync is complete, build and install it on your physical device.
- After installation, first pair your device from the Android Bluetooth settings, then select the device within the app to proceed to the management page. (If you encounter connection issues, ensure that "Bluetooth nearby devices" and "Location" permissions are granted under Settings > Apps > Bitki Sulama > Permissions.)

---

### Circuit Setup and Usage:

Set up the circuit according to the connection diagram below:

<p align="center">
<img src="https://github.com/nyaexx/bitki-sulama/blob/main/Arduino%20Kodlar%C4%B1/ba%C4%9Flanti%C5%9Femas%C4%B1.png" width="">
</p>

>[!NOTE]
>For usage, you can upload the Arduino codes and the libraries located in [this](https://github.com/nyaexx/bitki-sulama/tree/main/Arduino%20Kodlar%C4%B1) directory to your device.
>
>Remember, if you are not using an original Arduino Uno, you must install the CH340 driver found in the libraries folder within the [Arduino Codes](https://github.com/nyaexx/bitki-sulama/tree/main/Arduino%20Kodlar%C4%B1) directory. Otherwise, installation is not necessary.

>[!IMPORTANT]
>``Requirements for Bluetooth Connection:``
>For the first connection, open the phone's Bluetooth menu, select "HC-06", and enter the password "1234".
>If a connection problem occurs within the app, try pairing immediately after pressing the red reset button on the Uno board.
>The red LED on the Bluetooth module indicates "searching for device" when flashing and "connection established" when solid.
>If the device cannot be paired while Bluetooth appears connected, disconnect the power from the circuit, wait until the lights turn off, and reconnect the power; this should resolve the issue.

>[!NOTE]
>For help or any questions, contact on Discord: nyaex
>
>If you would like to add something to the repo or fix a bug, feel free to submit a pull request.

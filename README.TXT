   Building the Ezairo(R) Sound Designer Android(TM) Demo Application
               for Ezairo(R) 7150 SL in Android Studio
   ==================================================================

Perform the following steps to run the Ezairo Sound Designer Android Demo
Application on an Android phone after opening the project in Android Studio:

1) The project cannot be built on an emulator; instead, you need an Android
   phone to be able to use Bluetooth low energy technology and other features
   to connect to the device.

2) Import the project into Android Studio using (import project) and 
   selecting the "E7150SL_SDK_Demo" folder.

3) Wait for Android Studio and Gradle to import and analyze, and then build
   the project. Check for error messages. Android Studio detects whether you
   are missing libraries and prompts you to download and install them.

4) When the build is error-free, attach your phone to the computer. Run
   the app by selecting "app" in the "Select Run/Debug Configuration" dropdown
   and clicking the Run button beside it. To run the automated tests instead,
   see Step 7.

5) After the app is installed on the phone, you can analyze and review the
   events through the Logcat viewer in Android Studio.

6) To connect to the device, enter Pairing Mode by holding down SW1 for more
   than 10 seconds on the Ezairo 7150 SL Hybrid Demonstrator Board.

7) To run the TwoDeviceTests suite, in the Project sidebar, navigate to and open
   app > java > com.onsemi.e7150sl_sdk_demo (androidTest) > TwoDeviceTests.java.
   Click the green triangles next to "public class TwoDeviceTests" and click
   "Run TwoDeviceTests".


===============================================================================
Copyright (c) 2019 Semiconductor Components Industries, LLC 

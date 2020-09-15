package com.onsemi.e7150sl_sdk_demo;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.core.deps.guava.base.Strings;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.permission.PermissionRequester;
import android.util.Log;

import com.ark.ArkException;
import com.ark.AsyncResult;
import com.ark.CommunicationAdaptor;
import com.ark.CommunicationPort;
import com.ark.Event;
import com.ark.EventHandler;
import com.ark.EventType;
import com.ark.Library;
import com.ark.Parameter;
import com.ark.ParameterList;
import com.ark.ParameterSpace;
import com.ark.Product;
import com.ark.ProductDefinition;
import com.ark.ProductDefinitionList;
import com.ark.ProductManager;
import com.ark.WirelessCommunicationAdaptorStateType;
import com.ark.WirelessProgrammerType;
import com.onsemi.androidble.BleUtil;
import com.jhearing.e7160sl.Utils.SDKSUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

enum Ear {
    Right,
    Left,
    Both
}

@RunWith(AndroidJUnit4.class)
public class TwoDeviceTests {

    // Before starting the tests set the names of the right and left devices
    // Ear can be set to Both, Right, Left. Tests are designed to run on a single or two devices.
    // Both devices must be in pairing mode before starting the tests.

    private String deviceNameLeft = "TestLeft";
    private String deviceNameRight = "TestRight";
    private Ear ear = Ear.Both; //Ear.Right; //Ear.Left;

    private ProductManager productManager;
    private EventHandler eventHandler;
    private CommunicationAdaptor commAdaptorLeft;
    private CommunicationAdaptor commAdaptorRight;
    private Library library;
    private ProductDefinitionList productList;
    private ProductDefinition productDefinition;
    private Product productLeft;
    private Product productRight;

    private String deviceAddressLeft = "";
    private String deviceAddressRight = "";
    private String TAG = "TwoDeviceTests";
    private Semaphore connectedLeft = new Semaphore(0);
    private Semaphore connectedRight = new Semaphore(0);
    private Semaphore disconnectedLeft = new Semaphore(0);
    private Semaphore disconnectedRight = new Semaphore(0);
    private Semaphore deviceFoundLeft = new Semaphore(0);
    private Semaphore deviceFoundRight = new Semaphore(0);

    private Semaphore testComplete;

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);

    @Before
    public void SetUp() throws Exception {

        boolean noDeviceFound = false;

        Log.i(TAG, "In Setup " + Thread.currentThread().getId());
        Context appContext = InstrumentationRegistry.getTargetContext();

        // Grant background location access when running tests on Android 10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            GrantPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }

        // For the Android SDK we need to pass in the application context
        // via the following API call.  This should always be done first.
        BleUtil.BleManager_Init(appContext);

        // Get an instance of the Product Manager
        // This is where all interaction with the SDK starts
        productManager = ProductManager.getInstance();

        // Get an instance of the Event Handler
        // The Event Handler will provide asynchronous events pushed from the device
        eventHandler = productManager.getEventHandler();

        // Check the Event Handler for events for our tests
        MonitorEventHandler();

        // Load the library file which has been stored by the app developer in the assets folder
        String libraryPath = SDKSUtils.extractAsset(appContext, "E7160SL.library");
        library = productManager.loadLibraryFromFile(libraryPath);

        // Get a list of all products within the library
        productList = library.getProducts();

        // For the purposes of the sample code here just get the first product in the list
        // There is only one product defined in the demo library used here
        productDefinition = productList.getItem(0);

        // Create an instance of the left and right products
        productLeft = productDefinition.createProduct();
        productRight = productDefinition.createProduct();

        // Start scanning for wireless devices
        AsyncResult asyncScan = productManager.beginScanForWirelessDevices(WirelessProgrammerType.kPlatformDefault, "", CommunicationPort.kLeft, "", false );
        WaitUntilFinished(asyncScan);

        // Wait until a Scan Event is returned with the devices we are looking for
        // The MonitorEventHandler method will release the semaphores below when this happens.
        if (ear == Ear.Both || ear == Ear.Left)
            if (!deviceFoundLeft.tryAcquire(30L, TimeUnit.SECONDS)) {
                noDeviceFound = true;
            }
        if (ear == Ear.Both || ear == Ear.Right)
            if (!deviceFoundRight.tryAcquire(30L, TimeUnit.SECONDS)) {
                noDeviceFound = true;
            }

        // We have found the devices we need so end the scan
        productManager.endScanForWirelessDevices(asyncScan);

        if (noDeviceFound) {
            throw new RuntimeException();
        }

        // Create communication adaptors
        if (ear == Ear.Both || ear == Ear.Left)
            commAdaptorLeft = CreateAndSetupCommunicationAdaptor(deviceAddressLeft);
        if (ear == Ear.Both || ear == Ear.Right)
            commAdaptorRight = CreateAndSetupCommunicationAdaptor(deviceAddressRight);

        // Attempt to connect to the devices
        // After making this call we need to wait for a Connection Event to return a
        // Connected state. The MonitorEventHandler method will release the semaphores
        // below when this happens.
        if (ear == Ear.Both || ear == Ear.Left)
            commAdaptorLeft.connect();
        if (ear == Ear.Both || ear == Ear.Right)
            commAdaptorRight.connect();

        if (ear == Ear.Both || ear == Ear.Left)
            if (!connectedLeft.tryAcquire(60L, TimeUnit.SECONDS)) {
                throw new RuntimeException();
            }
        if (ear == Ear.Both || ear == Ear.Right)
            if (!connectedRight.tryAcquire(60L, TimeUnit.SECONDS)) {
                throw new RuntimeException();
            }

        disconnectedLeft.drainPermits();
        disconnectedRight.drainPermits();

        if (ear == Ear.Both || ear == Ear.Left)
            EnableWirelessSettings(productLeft, deviceNameLeft, CommunicationPort.kLeft);
        if (ear == Ear.Both || ear == Ear.Right)
            EnableWirelessSettings(productRight, deviceNameRight, CommunicationPort.kRight);
    }

    @After
    public void TearDown() throws Exception {

        // We are done running the test so disconnect the Communication Adaptor
        // and wait for the connection state to change to Disconnected

        Log.i(TAG, "In TearDown " + Thread.currentThread().getId());
        if (ear == Ear.Both || ear == Ear.Left)
            commAdaptorLeft.disconnect();
        if (ear == Ear.Both || ear == Ear.Right)
            commAdaptorRight.disconnect();

        if (ear == Ear.Both || ear == Ear.Left)
            disconnectedLeft.tryAcquire(10L, TimeUnit.SECONDS);
        if (ear == Ear.Both || ear == Ear.Right)
            disconnectedRight.tryAcquire(10L, TimeUnit.SECONDS);

        if (ear == Ear.Both || ear == Ear.Left)
            commAdaptorLeft.closeDevice();
        if (ear == Ear.Both || ear == Ear.Right)
            commAdaptorRight.closeDevice();

        Log.i(TAG, "Disconnected");
        BleUtil.BleManager_DeInit();
        Thread.sleep(3000);
    }


    @Test
    public void ReadAndWriteManufacturerDataTest() throws Exception {

        AsyncResult asyncLeft = null;
        AsyncResult asyncRight = null;

        // Detect a device
        if (ear == Ear.Both || ear == Ear.Left)
            asyncLeft = commAdaptorLeft.beginDetectDevice();
        if (ear == Ear.Both || ear == Ear.Right)
            asyncRight = commAdaptorRight.beginDetectDevice();

        WaitUntilFinished(asyncRight, asyncLeft);

        // Initialize the devices
        if (ear == Ear.Both || ear == Ear.Left)
            asyncLeft = productLeft.beginInitializeDevice(commAdaptorLeft);
        if (ear == Ear.Both || ear == Ear.Right)
            asyncRight = productRight.beginInitializeDevice(commAdaptorRight);

        WaitUntilFinished(asyncRight, asyncLeft);

        // Get the size of the scratch area in bytes
        int manDataAreaSizeInBytes = productDefinition.getManufacturerDataAreaLength();

        // Create two byte arrays
        // Fill the byte array with random data to write
        byte[] writeBytes = new byte[manDataAreaSizeInBytes];
        byte[] readBytesLeft = new byte[manDataAreaSizeInBytes];
        byte[] readBytesRight = new byte[manDataAreaSizeInBytes];
        Random r = new Random();
        r.nextBytes(writeBytes);

        // Write the random bytes to the device
        if (ear == Ear.Both || ear == Ear.Left)
            asyncLeft = productLeft.beginWriteManufacturerData(0, manDataAreaSizeInBytes, writeBytes);
        if (ear == Ear.Both || ear == Ear.Right)
            asyncRight = productRight.beginWriteManufacturerData(0, manDataAreaSizeInBytes, writeBytes);

        WaitUntilFinished(asyncRight, asyncLeft);

        // Read the same bytes back out from the devices
        if (ear == Ear.Both || ear == Ear.Left)
            asyncLeft = productLeft.beginReadManufacturerData(0, manDataAreaSizeInBytes);
        if (ear == Ear.Both || ear == Ear.Right)
            asyncRight = productRight.beginReadManufacturerData(0, manDataAreaSizeInBytes);

        WaitUntilFinished(asyncRight, asyncLeft);

        if (ear == Ear.Both || ear == Ear.Left)
            productLeft.endReadManufacturerData(asyncLeft, manDataAreaSizeInBytes, readBytesLeft);
        if (ear == Ear.Both || ear == Ear.Right)
            productRight.endReadManufacturerData(asyncRight, manDataAreaSizeInBytes, readBytesRight);

        // Confirm that the random bytes we wrote match the bytes we read back
        if (ear == Ear.Both || ear == Ear.Left)
            assertArrayEquals(writeBytes, readBytesLeft);
        if (ear == Ear.Both || ear == Ear.Right)
            assertArrayEquals(writeBytes, readBytesRight);
    }


    @Test
    public void ConfigureDeviceTest() throws Exception {

        AsyncResult asyncLeft = null;
        AsyncResult asyncRight = null;

        // Detect a device
        if (ear == Ear.Both || ear == Ear.Left)
            asyncLeft = commAdaptorLeft.beginDetectDevice();
        if (ear == Ear.Both || ear == Ear.Right)
            asyncRight = commAdaptorRight.beginDetectDevice();

        WaitUntilFinished(asyncRight, asyncLeft);

        // Initialize the devices
        if (ear == Ear.Both || ear == Ear.Left)
            asyncLeft = productLeft.beginInitializeDevice(commAdaptorLeft);
        if (ear == Ear.Both || ear == Ear.Right)
            asyncRight = productRight.beginInitializeDevice(commAdaptorRight);

        WaitUntilFinished(asyncRight, asyncLeft);

        // Configure the devices
        if (ear == Ear.Both || ear == Ear.Left)
            asyncLeft = productLeft.beginConfigureDevice();
        if (ear == Ear.Both || ear == Ear.Right)
            asyncRight = productRight.beginConfigureDevice();

        WaitUntilFinished(asyncRight, asyncLeft);
    }


    @Test
    public void ReadAllTest() throws Exception {

        AsyncResult asyncLeft = null;
        AsyncResult asyncRight = null;

        // Detect a device
        if (ear == Ear.Both || ear == Ear.Left)
            asyncLeft = commAdaptorLeft.beginDetectDevice();
        if (ear == Ear.Both || ear == Ear.Right)
            asyncRight = commAdaptorRight.beginDetectDevice();

        WaitUntilFinished(asyncRight, asyncLeft);

        // Initialize the devices
        if (ear == Ear.Both || ear == Ear.Left)
            asyncLeft = productLeft.beginInitializeDevice(commAdaptorLeft);
        if (ear == Ear.Both || ear == Ear.Right)
            asyncRight = productRight.beginInitializeDevice(commAdaptorRight);

        WaitUntilFinished(asyncRight, asyncLeft);

        // Read System memory from the devices
        if (ear == Ear.Both || ear == Ear.Left)
            asyncLeft = productLeft.beginReadParameters(ParameterSpace.kSystemNvmMemory);
        if (ear == Ear.Both || ear == Ear.Right)
            asyncRight = productRight.beginReadParameters(ParameterSpace.kSystemNvmMemory);

        WaitUntilFinished(asyncRight, asyncLeft);

        // Read all other memories from the devices
        int numberOfMemories = productDefinition.getNumberOfMemories();
        ParameterSpace[] paramSpaceValues = ParameterSpace.values();
        for (int i=4; i < numberOfMemories+4; i++) {

            if (ear == Ear.Both || ear == Ear.Left)
                asyncLeft = productLeft.beginReadParameters(paramSpaceValues[i]);
            if (ear == Ear.Both || ear == Ear.Right)
                asyncRight = productRight.beginReadParameters(paramSpaceValues[i]);

            WaitUntilFinished(asyncRight, asyncLeft);
        }
    }



    //---------------------------------------------------------------------------------------------
    // Methods below are helper functions used by the sample code above

    private CommunicationAdaptor CreateAndSetupCommunicationAdaptor(String deviceAddress) throws Exception {

        // Get an instance of a Communication Adaptor with the scanned device addresses
        CommunicationAdaptor commAdaptor = productManager.createWirelessCommunicationInterface(deviceAddress);
        commAdaptor.setVerifyNvmWrites(false);

        // Pass the Event Handler to the Communication Adaptor
        commAdaptor.setEventHandler(eventHandler);

        return commAdaptor;
    }

    private void MonitorEventHandler() {
        // Events from the EventHandler instance obtained from the SDK must be checked for on a separate
        // thread in a loop.  EventHandler.getEvent() will return an event if present, otherwise it
        // will be a blocking call.  For this reason it is important to ensure that the work done here
        // be on its own thread.
        // Based on the event type appropriate action can be taken in your application.

        final int DEVICE_NAME = 0;
        final int OBJTWO = 1;

        int permits = (ear == Ear.Both) ? -1: 0;
        testComplete = new Semaphore(permits);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "Starting Event Handler " + Thread.currentThread().getId());
                    while (!testComplete.tryAcquire()) {
                        Event event = eventHandler.getEvent();
                        EventType type = event.getType();

                        JSONObject jsonObject = new JSONObject(event.getData());
                        JSONArray jsonEvent = jsonObject.getJSONArray("Event");
                        JSONObject deviceName;
                        JSONObject deviceAddress;

                        switch (type) {

                            case kActiveEvent:
                                Log.i(TAG, type.name());
                                Log.i(TAG, event.getData());
                                break;
                            case kScanEvent:
                                Log.i(TAG, type.name());
                                Log.i(TAG, event.getData());

                                deviceName = jsonEvent.getJSONObject(DEVICE_NAME);
                                deviceAddress = jsonEvent.getJSONObject(1);

                                if (deviceName.getString("DeviceName").compareTo(deviceNameLeft) == 0) {
                                    Log.i(TAG, "Device Found - Left " + deviceAddress.getString("DeviceID"));
                                    deviceAddressLeft = deviceAddress.getString("DeviceID");
                                    deviceFoundLeft.release();
                                }

                                if (deviceName.getString("DeviceName").compareTo(deviceNameRight) == 0) {
                                    Log.i(TAG, "Device Found - Right " + deviceAddress.getString("DeviceID"));
                                    deviceAddressRight = deviceAddress.getString("DeviceID");
                                    deviceFoundRight.release();
                                }

                                break;
                            case kConnectionEvent:
                                Log.i(TAG, type.name());
                                Log.i(TAG, event.getData());
                                JSONObject deviceConnStat = jsonEvent.getJSONObject(OBJTWO);
                                deviceAddress = jsonEvent.getJSONObject(0);
                                int state = deviceConnStat.getInt("ConnectionState");

                                if (state == WirelessCommunicationAdaptorStateType.kConnected.ordinal()) {
                                    if (deviceAddress.getString("DeviceID").compareTo(deviceAddressLeft) == 0) {
                                        Log.i(TAG, "Received Connect - Left " + deviceAddress.getString("DeviceID"));
                                        connectedLeft.release();
                                    }

                                    if (deviceAddress.getString("DeviceID").compareTo(deviceAddressRight) == 0) {
                                        Log.i(TAG, "Received Connect - Right " + deviceAddress.getString("DeviceID"));
                                        connectedRight.release();
                                    }
                                }

                                if (state == WirelessCommunicationAdaptorStateType.kDisconnected.ordinal()) {
                                    if (deviceAddress.getString("DeviceID").compareTo(deviceAddressLeft) == 0) {
                                        Log.i(TAG, "Received Disconnect - Left " + deviceAddress.getString("DeviceID"));
                                        disconnectedLeft.release();
                                        testComplete.release();
                                    }

                                    if (deviceAddress.getString("DeviceID").compareTo(deviceAddressRight) == 0) {
                                        Log.i(TAG, "Received Disconnect - Right " + deviceAddress.getString("DeviceID"));
                                        disconnectedRight.release();
                                        testComplete.release();
                                    }
                                }
                                break;
                            case kBatteryEvent:
                                Log.i(TAG, type.name());
                                Log.i(TAG, event.getData());
                                break;
                            case kVolumeEvent:
                                Log.i(TAG, type.name());
                                Log.i(TAG, event.getData());
                                break;
                            case kMemoryEvent:
                                Log.i(TAG, type.name());
                                Log.i(TAG, event.getData());
                                break;
                        }
                    }

                    Log.i(TAG, "Exit Event Handler " + Thread.currentThread().getId());
                } catch (ArkException | JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        };

        AsyncTask.execute(runnable);
    }


    private void EnableWirelessSettings(Product product, String deviceName, CommunicationPort port) throws Exception {
        // The following helper function will find the wireless enable parameter and ensure
        // that it is enabled.  By default wireless functionality is disabled on the device.

        // Some sample code will be writing to the device or performing a configure which will
        // burn all parameters in every memory to the device.  When this happens we want to ensure
        // that the sample code does not inadvertently disable wireless functionality on the device.

        ParameterList params =  product.getSystemMemory().getParameters();
        Parameter wirelessEnable = params.getById("X_RF_Enable");
        wirelessEnable.setValue(1);

        Parameter earParam = params.getById("X_FWK_Ear");
        earParam.setValue( (port == CommunicationPort.kLeft) ? 0 : 1 );

        String paddedName = Strings.padEnd(deviceName,15,'\0');

        for(int i = 0; i < 5; i++){
            Parameter nameParam = params.getById("X_RF_DeviceName"+i);
            nameParam.setValue(((((int) paddedName.charAt(i * 3)) << 16) & 0xFF0000) |
                                ((((int) paddedName.charAt(i * 3 + 1)) << 8) & 0xFF00) |
                                (((int) paddedName.charAt(i * 3 + 2)) & 0xFF));
        }
    }

    private void WaitUntilFinished(AsyncResult asyncResult) throws Exception {
        // This helper function will monitor the AsyncResult instance returned by all
        // SDK asynchronous API calls (ones that are prefixed with "begin"). It will wait
        // until the AsyncResult indicates that the API call has completed and that the
        // progress value increments.

        int timeOut;
        int sleep = 50; //ms
        boolean isFinished = false;
        int progress;
        int previous_progress = 0;
        int print_progress = 0;
        int initial_timeout = 60000; //ms -> 60 seconds

        timeOut = initial_timeout;

        Log.i(TAG, "Async call started.  Waiting to finished...");
        while (!isFinished && (timeOut > 0)) {
            isFinished = asyncResult.isIsFinished();
            progress = asyncResult.getProgressValue();
            Thread.sleep(sleep);

            if (progress >= print_progress + 5) {
                Log.i(TAG, "Progress: " + progress);
                print_progress = progress;
            }

            // Decrement the timeout only if we are not making progress.
            // If the progress bar is moving then reset the timeout.
            timeOut = (progress > previous_progress) ? initial_timeout : timeOut - sleep;
            previous_progress = progress;
        }

        // Once an asynchronous call has completed its progress value will always be 100
        asyncResult.getResult();
        progress = asyncResult.getProgressValue();
        assertEquals(100, progress);

        Log.i(TAG, "Async call finished");
        assertEquals(true, isFinished);
    }

    private void WaitUntilFinished(AsyncResult asyncRight, AsyncResult asyncLeft) throws Exception {
        if (ear == Ear.Both || ear == Ear.Left)
            WaitUntilFinished(asyncLeft);
        if (ear == Ear.Both || ear == Ear.Right)
            WaitUntilFinished(asyncRight);
    }

    private void GrantPermissions(String permission) {
        PermissionRequester requester = new PermissionRequester();
        requester.addPermissions(permission);
        requester.requestPermissions();
    }
}

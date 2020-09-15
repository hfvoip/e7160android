package com.onsemi.e7150sl_sdk_demo;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.core.deps.guava.base.Strings;
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
import com.ark.ParameterList;
import com.ark.ParameterMemory;
import com.ark.ParameterMemoryList;
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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.bluetooth.BluetoothAdapter;

//import com.onsemi.fg7cpt.e7150sl_sdk_demo.Utils.SDKSUtils.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ConnectionTests {

    private String pairedDeviceAddress = "60:C0:BF:00:9D:9F";       // a device that is paired with phone or in pairing mode
    private String unpairedDeviceAddress = "60:C0:BF:13:DB:21";     // a device that is paired with another phone, but not in pairing mode
    private String offlineDeviceAddress = "60:C0:BF:00:00:00";     // a device that is off
    private String pairedDeviceName = "TestRight";
    private String unpairedDeviceName = "TestLeft";
    private String deviceAddress;
    private String deviceName;

    private static ReentrantLock eventHandlerLock = new ReentrantLock();
    private ProductManager productManager;
    private EventHandler eventHandler;
    private CommunicationAdaptor commAdaptor;
    private Library library;
    private ProductDefinitionList productList;
    private ProductDefinition productDefinition;
    private Product product;
    private ParameterMemoryList memories;
    private ParameterMemory memoryA;
    private ParameterMemory systemMemory;
    private ParameterList parameters;
    private ParameterList systemParameters;
    private BluetoothAdapter bluetoothAdapter;


    private String TAG = "SDKSamples";
    private Semaphore connected = new Semaphore(0);
    private Semaphore disconnected = new Semaphore(0);
    private Semaphore deviceFound = new Semaphore(0);
    private Semaphore testComplete = new Semaphore(0);
    private Semaphore bluetoothOff = new Semaphore(0);
    private Semaphore bluetoothOn = new Semaphore(0);
    private boolean done = false;

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);

    @Rule
    public GrantPermissionRule mBluetoothPermissionRule = GrantPermissionRule.grant(Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH);

    @Before
    public void SetUp() throws Exception {

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

        // Load the library file which has been stored by the app developer in the assets
        // folder
        //String libraryPath = SDKSUtils.extractAsset(appContext, "E7150SL.library");
        String libraryPath = SDKSUtils.extractAsset(appContext, "E7160SL.library");
        library = productManager.loadLibraryFromFile(libraryPath);

        // Get a list of all products within the library
        productList = library.getProducts();

        // For the purposes of the sample code here just get the first product in the list
        // There is only one product defined in the demo libary used here
        productDefinition = productList.getItem(0);

        // Create an instance of this product from its definition
        product = productDefinition.createProduct();

        // From the product get the parameter memory list
        memories = product.getMemories();

        // For the purposes of the sample code here just get the first memory in the list
        memoryA = memories.getItem(0);

        // From the product get the system memory
        systemMemory = product.getSystemMemory();

        // Get the parameter list from the first memory
        parameters = memoryA.getParameters();

        // Get the parameter list from system memory
        systemParameters = systemMemory.getParameters();

        // Get Bluetooth Adapter so we can turn Bluetooth off and on in tests
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    @After
    public void TearDown() throws Exception {
        Log.i(TAG, "In TearDown " + Thread.currentThread().getId());

        // Just incase a test fails without re-enabling bluetooth, do so here
        int state = bluetoothAdapter.getState();
        if (state == BluetoothAdapter.STATE_OFF) {
            bluetoothAdapter.enable();
        }

        BleUtil.BleManager_DeInit();
        Thread.sleep(3000);
    }

    //@Test // Test disabled for automation purposes. Uncomment to run.
    public void ConnectUnpaired() throws Exception {

        commAdaptor = productManager.createWirelessCommunicationInterface(unpairedDeviceAddress);
        commAdaptor.setVerifyNvmWrites(false);
        commAdaptor.setEventHandler((eventHandler));

        // Attempt to connect
        commAdaptor.connect();

        // We get the expected disconnected event as the device is unpaired and not in pairing mode
        if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        Thread.sleep(1000);

        // Try again
        done = true;
        commAdaptor.connect();

        // Same expected disconnected event
        if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        commAdaptor.closeDevice();
    }

    //@Test // Test disabled for automation purposes. Uncomment to run.
    public void ScanAndConnectUnpairedAndScan() throws Exception {

        boolean noDeviceFound = false;
        // Name of the device which is advertising but not in pairing mode
        // Previously paired with another phone
        deviceName = unpairedDeviceName;
        AsyncResult asyncScan = productManager.beginScanForWirelessDevices(WirelessProgrammerType.kPlatformDefault, "", CommunicationPort.kLeft, "", false );
        WaitUntilFinished(asyncScan);

        // Look for the device in the scan events
        if (!deviceFound.tryAcquire(30L, TimeUnit.SECONDS)) {
            noDeviceFound = true;
        }

        // We have found the devices we need so end the scan
        productManager.endScanForWirelessDevices(asyncScan);

        if (noDeviceFound) {
            throw new RuntimeException();
        }

        commAdaptor = productManager.createWirelessCommunicationInterface(deviceAddress);
        commAdaptor.setVerifyNvmWrites(false);

        // Pass the Event Handler to the Communication Adaptor
        commAdaptor.setEventHandler(eventHandler);

        // Attempt to connect
        commAdaptor.connect();

        // Get the expected disconnected event
        if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        Thread.sleep(1000);

        // Confirm that scanning for devices still works
        done = true;
        asyncScan = productManager.beginScanForWirelessDevices(WirelessProgrammerType.kPlatformDefault, "", CommunicationPort.kLeft, "", false );
        WaitUntilFinished(asyncScan);

        if (!deviceFound.tryAcquire(30L, TimeUnit.SECONDS)) {
            noDeviceFound = true;
        }

        productManager.endScanForWirelessDevices(asyncScan);

        if (noDeviceFound) {
            throw new RuntimeException();
        }

        commAdaptor.closeDevice();
    }


    @Test
    public void ConnectAndReconnect() throws Exception {

        commAdaptor = productManager.createWirelessCommunicationInterface(pairedDeviceAddress);
        commAdaptor.setVerifyNvmWrites(false);
        commAdaptor.setEventHandler((eventHandler));
        int numberOfLoops = 3;
        boolean failed = false;

        for (int i=0; i< numberOfLoops; i++) {

            commAdaptor.connect();
            if (!connected.tryAcquire(30L, TimeUnit.SECONDS)) {
                Log.i(TAG, "Connect "+i+" Failed: " + Thread.currentThread().getId());
                failed = true;
            }
            Thread.sleep(2000);

            if (i== numberOfLoops-1){
                done = true;
            }

            commAdaptor.disconnect();
            if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
                Log.i(TAG, "Disconnect "+i+" Failed: " + Thread.currentThread().getId());
                failed = true;
            }
            Thread.sleep(2000);
        }

        commAdaptor.closeDevice();

        if(failed)
            fail();
    }

    @Test
    public void ConnectTime() throws Exception {
        long t1, t2, delta;
        commAdaptor = productManager.createWirelessCommunicationInterface(pairedDeviceAddress);
        commAdaptor.setVerifyNvmWrites(false);
        commAdaptor.setEventHandler((eventHandler));

        t1 = System.currentTimeMillis();
        commAdaptor.connect();
        if (!connected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }
        t2 = System.currentTimeMillis();
        delta = t2 - t1;
        Log.i(TAG, "Connect Time: " + delta + " ms");
        done = true;

        Thread.sleep(2000);
        commAdaptor.disconnect();
        if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        commAdaptor.closeDevice();
    }

    @Test
    public void ConnectWhileConnecting() throws Exception {
        commAdaptor = productManager.createWirelessCommunicationInterface(pairedDeviceAddress);
        commAdaptor.setVerifyNvmWrites(false);
        commAdaptor.setEventHandler((eventHandler));

        commAdaptor.connect();

        Thread.sleep(3000);

        // Connect again before receiving the "Connected" event
        try {
            commAdaptor.connect();
        }
        catch (ArkException e) {
            // The expected error
            assertEquals("E_STILL_CONNECTING",e.getMessage());
        }

        if (!connected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        done = true;

        Thread.sleep(2000);
        commAdaptor.disconnect();
        if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        commAdaptor.closeDevice();
    }

    @Test
    public void DisconnectWhileConnecting() throws Exception {
        commAdaptor = productManager.createWirelessCommunicationInterface(pairedDeviceAddress);
        commAdaptor.setVerifyNvmWrites(false);
        commAdaptor.setEventHandler((eventHandler));

        commAdaptor.connect();

        Thread.sleep(3000);

        commAdaptor.disconnect();
        if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        Thread.sleep(1000);
        commAdaptor.connect();

        if (!connected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        Thread.sleep(2000);
        done = true;
        commAdaptor.disconnect();
        if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        commAdaptor.closeDevice();
    }

    @Test
    public void ConnectToDeviceThatIsOff() throws Exception {
        long t1, t2, delta;
        commAdaptor = productManager.createWirelessCommunicationInterface(offlineDeviceAddress);
        commAdaptor.setVerifyNvmWrites(false);
        commAdaptor.setEventHandler((eventHandler));

        t1 = System.currentTimeMillis();
        commAdaptor.connect();

        Thread.sleep(3000);

        if (!disconnected.tryAcquire(100L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        t2 = System.currentTimeMillis();
        delta = t2 - t1;
        Log.i(TAG, "Time to disconnected: " + delta + " ms");

        // Try to connect again
        commAdaptor.connect();

        Thread.sleep(5000);

        // Don't wait until we receive the disconnect event. Just call disconnect to force it
        commAdaptor.disconnect();

        done = true;
        if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        commAdaptor.closeDevice();
    }

    @Test
    public void ScanConnectReadDisconnectScanTest() throws Exception {

        boolean noDeviceFound = false;
        
        // Scan for device
        deviceName = pairedDeviceName;
        AsyncResult asyncScan = productManager.beginScanForWirelessDevices(WirelessProgrammerType.kPlatformDefault, "", CommunicationPort.kLeft, "", false );
        WaitUntilFinished(asyncScan);

        if (!deviceFound.tryAcquire(30L, TimeUnit.SECONDS)) {
            noDeviceFound = true;
        }
        productManager.endScanForWirelessDevices(asyncScan);

        if (noDeviceFound) {
            throw new RuntimeException();
        }

        // Connect to device
        commAdaptor = productManager.createWirelessCommunicationInterface(deviceAddress);
        commAdaptor.setVerifyNvmWrites(false);
        commAdaptor.setEventHandler(eventHandler);

        commAdaptor.connect();
        if (!connected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        // Read from device
        commAdaptor.detectDevice();
        product.initializeDevice(commAdaptor);
        product.readParameters(ParameterSpace.kNvmMemory0);

        Thread.sleep(500);

        // Disconnect from device
        commAdaptor.disconnect();
        if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        commAdaptor.closeDevice();

        Thread.sleep(1000);

        done = true;
        // Scan again for device
        asyncScan = productManager.beginScanForWirelessDevices(WirelessProgrammerType.kPlatformDefault, "", CommunicationPort.kLeft, "", false );
        WaitUntilFinished(asyncScan);

        if (!deviceFound.tryAcquire(30L, TimeUnit.SECONDS)) {
            noDeviceFound = true;
        }
        productManager.endScanForWirelessDevices(asyncScan);

        if (noDeviceFound) {
            throw new RuntimeException();
        }
    }

    @Test
    public void TurnBluetoothOffAndScan() throws Exception {

        // Turn off bluetooth for the purposes of this test
        bluetoothAdapter.disable();

        // Wait for the SDK to return a Bluetooth is off event
        if (!bluetoothOff.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        // Start a device scan
        deviceName = pairedDeviceName;

        // Ensure we get the expected error
        try {
            AsyncResult asyncScan = productManager.beginScanForWirelessDevices(WirelessProgrammerType.kPlatformDefault, "", CommunicationPort.kLeft, "", false );
        }
        catch (ArkException e) {
            assertEquals("E_ENABLE_BLUETOOTH",e.getMessage());
        }

        // Re-enable Bluetooth before end of test
        done = true;
        bluetoothAdapter.enable();

        // Wait for the SDK to return a Bluetooth is on event
        if (!bluetoothOn.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }
    }

    @Test
    public void TurnBluetoothOffAndConnect() throws Exception {

        commAdaptor = productManager.createWirelessCommunicationInterface(pairedDeviceAddress);
        commAdaptor.setVerifyNvmWrites(false);
        commAdaptor.setEventHandler((eventHandler));


        // Turn off bluetooth for the purposes of this test
        bluetoothAdapter.disable();

        // Wait for the SDK to return a Bluetooth is off event
        if (!bluetoothOff.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        // Try and connect. We should get an error
        try {
            commAdaptor.connect();
        }
        catch (ArkException e) {
            assertEquals("E_ENABLE_BLUETOOTH",e.getMessage());
        }

        // Re-enable Bluetooth before end of test
        done = true;
        bluetoothAdapter.enable();

        // Wait for the SDK to return a Bluetooth is on event
        if (!bluetoothOn.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }
    }

    @Test
    public void ConnectToggleBluetoothOffOnAndConnect() throws Exception {

        // Get comm adaptor
        commAdaptor = productManager.createWirelessCommunicationInterface(pairedDeviceAddress);
        commAdaptor.setVerifyNvmWrites(false);
        commAdaptor.setEventHandler((eventHandler));

        commAdaptor.connect();

        // Wait for connected event
        if (!connected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        // Turn off bluetooth for the purposes of this test
        bluetoothAdapter.disable();

        // Wait for the SDK to return a Bluetooth is off event
        if (!bluetoothOff.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        // Confirm that when bluetooth is disabled that the comm adaptor is also disconnected
        if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        // Confirm we are in a not connected state
        try {
            commAdaptor.detectDevice();
        } catch (ArkException e) {
            assertEquals("E_NOT_CONNECTED",e.getMessage());
        }

        Thread.sleep(3000);

        // Re-enable Bluetooth before end of test
        bluetoothAdapter.enable();

        // Wait for the SDK to return a Bluetooth is on event
        if (!bluetoothOn.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        Thread.sleep(3000);

        // Reconnect
        commAdaptor.connect();
        if (!connected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        // Perform a detect device to confirm communication works
        commAdaptor.detectDevice();

        // Disconnect
        done = true;
        commAdaptor.disconnect();
        if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        commAdaptor.closeDevice();
    }

    @Test
    public void ConnectReadBluetoothOff() throws Exception {

        // Get comm adaptor
        commAdaptor = productManager.createWirelessCommunicationInterface(pairedDeviceAddress);
        commAdaptor.setVerifyNvmWrites(false);
        commAdaptor.setEventHandler((eventHandler));

        commAdaptor.connect();

        // Wait for connected event
        if (!connected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        commAdaptor.detectDevice();
        product.initializeDevice(commAdaptor);

        // Turn off bluetooth for the purposes of this test
        bluetoothAdapter.disable();

        // Wait for the SDK to return a Bluetooth is off event
        if (!bluetoothOff.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        // Confirm that when bluetooth is disabled that the comm adaptor is also disconnected
        if (!disconnected.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        // Ensure we get the expected error
        try {
            product.readParameters(ParameterSpace.kNvmMemory0);
        }
        catch (ArkException e) {
            assertEquals("E_NOT_CONNECTED",e.getMessage());
        }

        // Re-enable Bluetooth before end of test
        bluetoothAdapter.enable();

        // Wait for the SDK to return a Bluetooth is on event
        done = true;
        if (!bluetoothOn.tryAcquire(30L, TimeUnit.SECONDS)) {
            throw new RuntimeException();
        }

        commAdaptor.closeDevice();
    }



    private void MonitorEventHandler() {
        // Events from the EventHandler instance obtained from the SDK must be checked for on a separate
        // thread in a loop.  EventHandler.getEvent() will return an event if present, otherwise it
        // will be a blocking call.  For this reason it is important to ensure that the work done here
        // be on its own thread.
        // Based on the event type appropriate action can be taken in your application.

        final int DEVICE_NAME = 0;
        final int OBJTWO = 1;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {

                    eventHandlerLock.lock();
                    Log.i(TAG, "Starting Event Handler " + Thread.currentThread().getId());
                    while (!testComplete.tryAcquire()) {
                        Event event = eventHandler.getEvent();
                        EventType type = event.getType();

                        JSONObject jsonObject = new JSONObject(event.getData());
                        JSONArray jsonEvent = jsonObject.getJSONArray("Event");

                        JSONObject device;
                        JSONObject DeviceID;
                        switch (type) {

                            case kActiveEvent:
                                Log.i(TAG, type.name());
                                Log.i(TAG, event.getData());

                                JSONObject active = jsonEvent.getJSONObject(0);
                                Log.i(TAG, active.getString("IsActive"));

                                if (active.getInt("IsActive") == 0) {
                                    bluetoothOff.release();
                                    bluetoothOn.drainPermits();
                                }
                                if (active.getInt("IsActive") == 1) {
                                    bluetoothOn.release();
                                }
                                if (done)
                                    testComplete.release();
                                break;
                            case kScanEvent:
                                Log.i(TAG, type.name());
                                Log.i(TAG, event.getData());

                                device = jsonEvent.getJSONObject(DEVICE_NAME);
                                DeviceID = jsonEvent.getJSONObject(1);

                                if (device.getString("DeviceName").compareTo(deviceName) == 0) {
                                    Log.i(TAG, "Device Found " + DeviceID.getString("DeviceID"));
                                    deviceAddress = DeviceID.getString("DeviceID");
                                    deviceFound.release();
                                    if (done)
                                        testComplete.release();
                                }
                                break;
                            case kConnectionEvent:
                                Log.i(TAG, type.name());
                                Log.i(TAG, event.getData());
                                JSONObject deviceConnStat = jsonEvent.getJSONObject(OBJTWO);
                                int state = deviceConnStat.getInt("ConnectionState");

                                if (state == WirelessCommunicationAdaptorStateType.kConnected.ordinal()) {
                                    connected.release();
                                    Log.i(TAG, "Received Connect " + Thread.currentThread().getId());
                                }
                                if (state == WirelessCommunicationAdaptorStateType.kDisconnected.ordinal()) {
                                    disconnected.release();
                                    if (done)
                                        testComplete.release();
                                    Log.i(TAG, "Received Disconnect " + Thread.currentThread().getId());
                                }
                                break;
                            case kBatteryEvent:
                                Log.i(TAG, type.name());
                                Log.i(TAG, event.getData());
                                break;
                            case kVolumeEvent:
                                Log.i(TAG, type.name());
                                Log.i(TAG, event.getData());
                                JSONObject volume = jsonEvent.getJSONObject(OBJTWO);
                                break;
                            case kMemoryEvent:
                                Log.i(TAG, type.name());
                                Log.i(TAG, event.getData());
                                JSONObject memory = jsonEvent.getJSONObject(OBJTWO);
                                break;
                        }

                    }

                    eventHandlerLock.unlock();
                    Log.i(TAG, "Exit Event Handler " + Thread.currentThread().getId());
                } catch (ArkException e) {
                    Log.e(TAG, e.getMessage());
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        };

        AsyncTask.execute(runnable);
    }

    private boolean WaitUntilFinished(AsyncResult asyncResult) throws Exception {
        // This helper function will monitor the AsyncResult instance returned by all
        // SDK asynchronous API calls (ones that are prefixed with "begin"). It will wait
        // until the AsyncResult indicates that the API call has completed and that the
        // progress value increments.

        int timeOut;
        int sleep = 50; //ms
        boolean isFinished = false;
        int progress = 0;
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
            // We need the way we handle the timeout to be a bit dynamic to account
            // for some programmers that are much slower than others
            timeOut = (progress > previous_progress) ? initial_timeout : timeOut - sleep;
            previous_progress = progress;
        }

        // Once an asynchronous call has completed its progress value will always be 100
        try {
            asyncResult.getResult();
        }
        catch  ( ArkException e) {
            Log.i(TAG, e.getMessage());
            throw e;
        }
        progress = asyncResult.getProgressValue();
        assertEquals(100, progress);


        Log.i(TAG, "Async call finished");
        return isFinished;
    }

    private void GrantPermissions(String permission) {
        PermissionRequester requester = new PermissionRequester();
        requester.addPermissions(permission);
        requester.requestPermissions();
    }
}
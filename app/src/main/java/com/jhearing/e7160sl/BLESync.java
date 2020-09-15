package com.jhearing.e7160sl;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.ark.ArkException;
import com.ark.ParameterSpace;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.Events.ConnectionEvents.ConnectionStateChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;


import java.io.FileDescriptor;
import java.io.PrintWriter;


public class BLESync extends Service {

    private static final String TAG = BLESync.class.getName();
    private static final int INITIAL_REQUEST = 1337;
    NotificationManager mNotificationManager = null;
    NotificationCompat.Builder builder = null;
    private int NOTIFICATION = 1123;
    private static final String CHANNEL_ID = "EZ7160SL";


    private EventReceiver<ConnectionStateChangedEvent> ConnectionStateChangedHandler
            = new EventReceiver<ConnectionStateChangedEvent>() {
        @Override
        public void onEvent(String name, ConnectionStateChangedEvent data) {

        }
    };
    private EventReceiver<ConfigurationChangedEvent> configurationChangedEventHandler = new EventReceiver<ConfigurationChangedEvent>() {
        @Override
        public void onEvent(String name, ConfigurationChangedEvent data) {
            checkConfiguration(HearingAidModel.Side.Right, data.address);
            checkConfiguration(HearingAidModel.Side.Left, data.address);

        }
    };

    public BLESync() {
    }


    private HearingAidModel getHearingAid(HearingAidModel.Side side) {

        return Configuration.instance().getDescriptor(side);
    }

    private void checkConfiguration(HearingAidModel.Side side, String address) {
        if (Configuration.instance().isHANotNull(side)) {

            if (address.equals(getHearingAid(side).address)) {
                HearingAidModel.Side oppositeSide = (side == HearingAidModel.Side.Left) ? HearingAidModel.Side.Right : HearingAidModel.Side.Left;
                if (Configuration.instance().isHANotNull(oppositeSide)) {
                    //check volumes are in sync
                    if (getHearingAid(side).volume != getHearingAid(oppositeSide).volume) {
                        updateVolumeSecondHaValue(oppositeSide, getHearingAid(side).volume);
                    }

                    //check memories are in sync
                    if (getHearingAid(side).currentMemory != getHearingAid(oppositeSide).currentMemory) {
                        updateMemoryForOtherHaValue(oppositeSide, getHearingAid(side).currentMemory);
                    }
                }


            }
        }
    }

    private void updateMemoryForOtherHaValue(HearingAidModel.Side oppositeSide, int value) {
        if (Configuration.instance().isConfigFull()) {
            if (Configuration.instance().isHAAvailable(oppositeSide)) {
                writeMemoryToHA(oppositeSide, value);
            }
        }
    }

    private void updateVolumeSecondHaValue(HearingAidModel.Side oppositeSide, int value) {
        if (Configuration.instance().isConfigFull()) {
            if (Configuration.instance().isHAAvailable(oppositeSide)) {
                writeVolumeToHA(oppositeSide, value);
            }
        }
    }

    private synchronized void writeMemoryToHA(HearingAidModel.Side side, int newMemory) {
        if (Configuration.instance().isHAAvailable(side)) {
            try {
                Log.i(TAG, "sdk write to volume from Service" + side + "value : " + ParameterSpace.values()[newMemory + 4]);
                getHearingAid(side).wirelessControl.setCurrentMemory(ParameterSpace.values()[newMemory + 4]);
                getHearingAid(side).currentMemory = newMemory;
            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private synchronized void writeVolumeToHA(HearingAidModel.Side side, int newVolume) {
        if (Configuration.instance().isHAAvailable(side)) {
            try {
                Log.i(TAG, "sdk write to volume from Service" + side + "value : " + newVolume);
                getHearingAid(side).wirelessControl.setVolume(newVolume);
                getHearingAid(side).volume = newVolume;
            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        showNotification();
        EventBus.registerReceiver(configurationChangedEventHandler, ConfigurationChangedEvent.class.getName());
        EventBus.registerReceiver(ConnectionStateChangedHandler, ConnectionStateChangedEvent.class.getName());
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);

        if (null == intent || null == intent.getAction ()) {
            String source = null == intent ? "intent" : "action";
            Log.e (TAG, source + " was null, flags=" + flags + " bits=" + Integer.toBinaryString (flags));
            return START_STICKY;
        }

        if (intent.getFlags() == PendingIntent.FLAG_CANCEL_CURRENT) {
            stopSelf();
        }
        if (intent.getFlags() == INITIAL_REQUEST || (intent.getFlags() == Intent.FLAG_ACTIVITY_CLEAR_TOP)) {

            if (builder.mActions.size() != 0) {
                builder.mActions.clear();
                mNotificationManager.notify(NOTIFICATION, builder.build());
            }
        }
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.unregisterReceiver(configurationChangedEventHandler);
        EventBus.unregisterReceiver(ConnectionStateChangedHandler);
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        super.onTaskRemoved(rootIntent);


        //         The PendingIntent to launch our activity if the user selects this notification
        Intent relaunchActivity = new Intent(this, MainActivity.class);
        relaunchActivity.setAction(Intent.ACTION_APPLICATION_PREFERENCES);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, relaunchActivity, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);


        Intent serviceAction = new Intent(this, BLESync.class);
        PendingIntent stopService = PendingIntent.getService(this, 12, serviceAction, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(R.mipmap.ic_clear_remove, "Exit", stopService);

        mNotificationManager.notify(NOTIFICATION, builder.build());
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(fd, writer, args);
    }

    /**
     * Show a notification while this service is running.
     */
    @SuppressLint("NewApi")
    private void showNotification() {
        CharSequence text = "";

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        if(builder == null){
            builder =   new NotificationCompat.Builder(this, CHANNEL_ID);
        }

        // Set the info for the views that show in the notification panel.
        builder.setSmallIcon(R.mipmap.ic_on_icon)  // the status icon
                .setTicker("Service has started")  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("JH E7160SL")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .build();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        startForeground(NOTIFICATION, builder.build());

    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}

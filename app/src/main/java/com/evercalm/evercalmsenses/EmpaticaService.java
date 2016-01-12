package com.evercalm.evercalmsenses;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import java.util.Observable;
import java.util.Observer;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by mattias on 2016-01-12.
 */
public class EmpaticaService extends Service implements EmpaDataDelegate, EmpaStatusDelegate {

    public static final String TAG = "MyServiceTag";

    static final public String EMPATICA_RESULT = "com.evercalm.evercalmsenses.EmpaticaService.REQUEST_PROCESSED";
    static final public String EMPATICA_MESSAGE = "com.evercalm.evercalmsenses.EmpaticaService.EMPATICA_MSG";
    static final public String EMPATICA_RESULT_DATA = "com.evercalm.evercalmsenses.EmpaticaService.EMPATICA_DATA";

    private static final String EMPATICA_API_KEY = "8a2036983aa448a9ac624064d72248c8";
    private static final int REQUEST_ENABLE_BT = 1;
    private LocalBroadcastManager broadcaster;
    private EmpaDeviceManager deviceManager;

    private int isrunning = 0;
    private boolean connected = false;
    private Context context;

    public EmpaticaService() {
    }

    public void sendResult(String message) {
        Intent intent = new Intent(EMPATICA_RESULT);
        if(message != null)
            intent.putExtra(EMPATICA_RESULT, message);
        broadcaster.sendBroadcast(intent);
    }

    public void sendData(int message) {
        Intent intent = new Intent(EMPATICA_RESULT_DATA);
        intent.putExtra(EMPATICA_MESSAGE, message);
        broadcaster.sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // This method is invoked when the service is called.
        Toast.makeText(this, "Service was Created", Toast.LENGTH_LONG).show();
        isrunning = 1;
        broadcaster = LocalBroadcastManager.getInstance(this);
        context = this.getApplicationContext();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //super.onCreate();
        throw new UnsupportedOperationException("Not yet implemented");
        //return null;
    }

    public IBinder onBind() {
        super.onCreate();
        // A client or activity is binded to service
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Toast.makeText(this, "Starting..", Toast.LENGTH_SHORT).show();
        try {
            boolean shouldConnect = intent.getBooleanExtra(EMPATICA_MESSAGE, false);
            if (shouldConnect) {
                sendResult("connecting");
                deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this); //getApplicationContext()
                // Initialize the Device Manager using your API key. You need to have Internet access at this point.
                deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothDevice activeDevice = deviceManager.getActiveDevice();
                        if (!connected) {
                            deviceManager.cleanUp();
                            sendResult("connectingTimeout");
                        }
                    }
                },5000);
            }
            boolean shouldSendData = intent.getBooleanExtra("sendData", false);
            if (shouldSendData) {
                SystemClock.sleep(3000); // 3 seconds
                sendData(1000);
                SystemClock.sleep(3000);
                sendData(1200);
            }
        } catch (Exception e) {

        }

        return START_STICKY; // or whatever your flag
    }

    @Override
    public void didReceiveGSR(float v, double v1) {

    }

    @Override
    public void didReceiveBVP(float v, double v1) {

    }

    @Override
    public void didReceiveIBI(float v, double v1) {

    }

    @Override
    public void didReceiveTemperature(float v, double v1) {

    }

    @Override
    public void didReceiveAcceleration(int i, int i1, int i2, double v) {

    }

    @Override
    public void didReceiveBatteryLevel(float v, double v1) {

    }

    @Override
    public void didUpdateStatus(EmpaStatus empaStatus) {

    }

    @Override
    public void didUpdateSensorStatus(EmpaSensorStatus empaSensorStatus, EmpaSensorType empaSensorType) {

    }

    @Override
    public void didDiscoverDevice(BluetoothDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);
                connected = true;
                sendResult("connected");
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                sendResult("connectedFailed");
            }
        }
    }

    @Override
    public void didRequestEnableBluetooth() {
        Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        btIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(btIntent);
        /*
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        */
    }
}
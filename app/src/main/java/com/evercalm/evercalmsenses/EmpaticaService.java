package com.evercalm.evercalmsenses;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import com.evercalm.evercalmsenses.API.StatisticsHttpWorker;
import com.evercalm.evercalmsenses.API.StatisticsModel;
import com.evercalm.evercalmsenses.API.WorkerEventListener;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by mattias on 2016-01-12.
 */
public class EmpaticaService extends Service implements EmpaDataDelegate, EmpaStatusDelegate {

    public static final String TAG = "EmpaticaServiceTag";
    private static final String API_URL = "https://mattias-innovativa-lab1.herokuapp.com"; //TODO: add url

    static final public String EMPATICA_RESULT_URL = "com.evercalm.evercalmsenses.EmpaticaService.EMPATICA_RESULT";
    static final public String EMPATICA_RESULT_DATA_URL = "com.evercalm.evercalmsenses.EmpaticaService.EMPATICA_RESULT";
    static final public String EMPATICA_MESSAGE_URL = "com.evercalm.evercalmsenses.EmpaticaService.EMPATICA_MESSAGE";
    private static final long LOGGING_INTERVAL_MINUTES = 1;

    public interface RESULTS {
        int CONNECTED = 1;
        int CONNECTION_PENDING = 2;
        int NOT_CONNECTED = 3;
        int CONNECTION_FAILED = 4;
        int CONNECTION_TIMEOUT = 5;
        int IS_LOGGING = 6;
        int NOT_LOGGING = 7;
    }

    public interface MESSAGES {
        int CONNECT_TO_DEVICE = 1;
        int START_LOGGING = 2;
        int END_LOGGING = 3;
        int RETRIEVE_DATA = 4;
        int RETRIEVE_LOGGING_STATUS = 5;
    }

    private static final String EMPATICA_API_KEY = "8a2036983aa448a9ac624064d72248c8";
    private static final int REQUEST_ENABLE_BT = 1;
    private LocalBroadcastManager broadcaster;
    private EmpaDeviceManager deviceManager;

    private int isrunning = 0;
    private boolean connected = false;
    private Context context;

    ScheduledFuture<?> loggingScheduler;

    //DATA
    ValueBundle currentStress;
    ArrayList<ValueBundle> ibis_raw;
    private boolean isLogging;

    public EmpaticaService() {
        ibis_raw = new ArrayList<>(100);
    }

    public void sendResult(int message) {
        Intent intent = new Intent(EMPATICA_RESULT_URL);
        intent.putExtra(EMPATICA_RESULT_URL, message);
        broadcaster.sendBroadcast(intent);
    }

    public void sendData(ValueBundle val) {
        Intent intent = new Intent(EMPATICA_RESULT_DATA_URL);
        intent.putExtra(EMPATICA_RESULT_DATA_URL, val.value);
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

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        EmpaticaService getService() {
            // Return this instance of LocalService so clients can call public methods
            return EmpaticaService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        handleIntent(intent);
        return mBinder;
    }

    private void tryConnect(int timeout) {
        deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);

        // Initialize the Device Manager using your API key. You need to have Internet access at this point.
        deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!connected) {
                    deviceManager.cleanUp();
                    sendResult(RESULTS.CONNECTION_TIMEOUT);
                }
            }
        }, timeout);
    }

    private void setLogging(boolean shouldLogg) {
        if (shouldLogg) {
            if (!isLogging) {
                isLogging = true;
                Runnable loggingTask = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            updateStress(getStress());
                        } catch (NoDataCollectedException e) {
                            //
                        }
                    }
                };

                ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                loggingScheduler = executor.scheduleAtFixedRate(loggingTask, 1, LOGGING_INTERVAL_MINUTES, TimeUnit.MINUTES);
            }
        } else {
            isLogging = false;
            if (loggingScheduler != null) {
                loggingScheduler.cancel(false);
            }
        }
    }

    private void setStress(ValueBundle v) {
        currentStress = v;
        //TODO: notify application of new value.
    }

    private void updateStress(ValueBundle stress) {
        if (currentStress != null) {
            if (checkIfSupportedInterval(stress)) {
                //TODO: fix this part, get id from user (use username as id etc)
                /*
                StatisticsModel model = new StatisticsModel(id, stress.value, stress.timestamp, 0);

                WorkerEventListener ev = new WorkerEventListener() {
                    @Override
                    public void dataRecieved(StatisticsModel model) {
                        ValueBundle stress = new ValueBundle(model.getValue(), model.getTimestamp());
                        setStress(stress);
                    }
                }

                StatisticsHttpWorker task = new StatisticsHttpWorker(API_URL, );
                task.execute();
                */
            }
            currentStress = stress;

        } else {
            //TODO: retrieve from server and set currentStress
            throw new UnsupportedOperationException();
        }
    }

    private boolean checkIfSupportedInterval(final ValueBundle stress) {
        //TODO: check if last stress value is not from yesterday
        return true;
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            switch (intent.getIntExtra(EMPATICA_MESSAGE_URL, -1)) {
                case MESSAGES.CONNECT_TO_DEVICE:
                    sendResult(RESULTS.CONNECTION_PENDING);
                    tryConnect(5000);
                    break;
                case MESSAGES.RETRIEVE_DATA:
                    sendData(currentStress);
                    //sendData(stress.get(stress.size()-1));
                    break;
                case MESSAGES.START_LOGGING:
                    setLogging(true);
                    break;
                case MESSAGES.END_LOGGING:
                    setLogging(false);
                    break;
                case MESSAGES.RETRIEVE_LOGGING_STATUS:
                    if (isLogging) {
                        sendResult(RESULTS.IS_LOGGING);
                    } else {
                        sendResult(RESULTS.NOT_LOGGING);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        handleIntent(intent);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_small)
                        .setContentTitle("Empatica Senses")
                        .setContentText("Service is running");


        Intent resultIntent = new Intent(this, MainTabbedActivity.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        startForeground(mNotificationId, mBuilder.build());

        return START_STICKY; // or whatever your flag
    }

    @Override
    public void didReceiveGSR(float v, double v1) {

    }

    @Override
    public void didReceiveBVP(float v, double v1) {

    }



    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        if (isLogging) {
            logIBI(ibi, timestamp);
        }
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
                sendResult(RESULTS.CONNECTED);
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                sendResult(RESULTS.CONNECTION_FAILED);
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

    public synchronized void logIBI(float value, double timestamp) {
        ibis_raw.add(new ValueBundle(value, timestamp));
    }

    private synchronized ArrayList<ValueBundle> popIbis() {
        ArrayList<ValueBundle> clone = cloneList(ibis_raw);
        ibis_raw.clear();
        return clone;
    }

    private ArrayList<ValueBundle> cloneList(ArrayList<ValueBundle> list) {
        ArrayList<ValueBundle> clone = new ArrayList<ValueBundle>(list.size());
        for(ValueBundle item: list) clone.add(item.clone());
        return clone;
    }

    private ValueBundle getStress() throws NoDataCollectedException {
        ArrayList<ValueBundle> selected_ibis = popIbis();

        //TODO: check the timestamp interval, only use data from specified time

        if(selected_ibis.size() <= 1) {
            throw new NoDataCollectedException();
        }

        double mean_ibi;
        double sum_ibi = 0;
        double sum_sqr_diff = 0;
        double rmssd_ibi;

        double last_ibi = -1;

        for (ValueBundle val : selected_ibis) {
            sum_ibi += val.value;
            if (last_ibi > 0) {
                sum_sqr_diff += (val.value - last_ibi)*(val.value - last_ibi);
            }
            last_ibi = val.value;
        }

        mean_ibi = sum_ibi /  (double) selected_ibis.size();
        rmssd_ibi = Math.sqrt(sum_sqr_diff / ((double) selected_ibis.size() - 1.0));

        double std_ibi;
        double sum_sqr_dev = 0;

        for (ValueBundle val : selected_ibis) {
            sum_sqr_dev += (val.value - mean_ibi)*(val.value - mean_ibi);
        }

        std_ibi = Math.sqrt(sum_sqr_dev / ((double)selected_ibis.size() - 1.0));

        return new ValueBundle(std_ibi / rmssd_ibi, selected_ibis.get(selected_ibis.size()-1).timestamp);
    }



    private class NoDataCollectedException extends Exception {
    }
}
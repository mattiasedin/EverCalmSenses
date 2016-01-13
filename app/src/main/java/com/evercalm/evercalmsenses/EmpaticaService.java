package com.evercalm.evercalmsenses;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import com.evercalm.evercalmsenses.API.EverCalmStatisticsEndpoint;
import com.evercalm.evercalmsenses.API.StatisticsHttpWorker;
import com.evercalm.evercalmsenses.API.StatisticsModel;
import com.evercalm.evercalmsenses.API.WorkerEventListener;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EmpaticaService extends Service implements EmpaDataDelegate, EmpaStatusDelegate {

    private String logginID;
    private boolean isRunning = false;

    private static final String EMPATICA_API_KEY = "74da5531eacb41bb819a7643cfe88d06";
    private EmpaDeviceManager deviceManager;

    private boolean connected = false;

    ScheduledFuture<?> loggingScheduler;

    //DATA
    ValueBundle currentStress;
    ArrayList<ValueBundle> ibis_raw;
    private boolean isLogging;


    static final public String EMPATICA_RESULT_URL = "com.evercalm.evercalmsenses.EmpaticaService.EMPATICA_RESULT";
    static final public String EMPATICA_RESULT_DATA_URL = "com.evercalm.evercalmsenses.EmpaticaService.EMPATICA_RESULT";
    static final public String EMPATICA_MESSAGE_URL = "com.evercalm.evercalmsenses.EmpaticaService.EMPATICA_MESSAGE";
    private static final long LOGGING_INTERVAL_MINUTES = 1;
    private Context context;

    public interface RESULTS {
        int CONNECTED = 1;
        int CONNECTION_PENDING = 2;
        int NOT_CONNECTED = 3;
        int CONNECTION_FAILED = 4;
        int CONNECTION_TIMEOUT = 5;
        int IS_LOGGING = 6;
        int NOT_LOGGING = 7;
        int NOT_AUTHENTICATED = 8;
        int AUTHENTICATED = 9;
        int STRESS_DATA = 10;
    }

    public interface MESSAGES {
        int CONNECT_TO_DEVICE = 1;
        int START_LOGGING = 2;
        int END_LOGGING = 3;
        int RETRIEVE_DATA = 4;
        int RETRIEVE_LOGGING_STATUS = 5;
        int RETRIEVE_AUTH_STATUS = 6;

        int REGISTER_CLIENT = 8;
        int UNREGISTER_CLIENT = 9;

        int AUTHENTICATE_KEY = 11;
    }

    /** For showing and hiding our notification. */
    NotificationManager mNM;
    /** Keeps track of all current registered clients. */
    ArrayList<Messenger> mClients = new ArrayList<>();


    public EmpaticaService() {
        ibis_raw = new ArrayList<>(100);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGES.CONNECT_TO_DEVICE:
                    if (!connected) {
                        sendMsg(RESULTS.CONNECTION_PENDING);
                        tryConnect(600000);
                    } else {
                        sendMsg(RESULTS.CONNECTED);
                        if (!isAuthenticated()) {
                            sendMsg(RESULTS.NOT_AUTHENTICATED);
                        }
                    }
                    break;
                case MESSAGES.RETRIEVE_DATA:
                    if (doAuthenticationConditionalSend()) {
                        sendMsg(RESULTS.STRESS_DATA, currentStress.value);
                    }
                    break;
                case MESSAGES.START_LOGGING:
                    if (doAuthenticationConditionalSend()) {
                        setLogging(true);
                    }
                    break;
                case MESSAGES.END_LOGGING:
                    if (doAuthenticationConditionalSend()) {
                        setLogging(false);
                    }
                    break;
                case MESSAGES.RETRIEVE_LOGGING_STATUS:
                    if (doAuthenticationConditionalSend()) {
                        if (isLogging) {
                            sendMsg(RESULTS.IS_LOGGING);
                        } else {
                            sendMsg(RESULTS.NOT_LOGGING);
                        }
                    }
                    break;
                case MESSAGES.RETRIEVE_AUTH_STATUS:
                    if (doAuthenticationConditionalSend()) {
                        sendMsg(RESULTS.AUTHENTICATED);
                    }
                    break;
                case MESSAGES.REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MESSAGES.UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MESSAGES.AUTHENTICATE_KEY:
                    String id = (String) msg.obj;
                    logginID = id;
                    sendMsg(RESULTS.AUTHENTICATED);
                    break;
                case -1:
                    throw new UnsupportedOperationException();
                default:
                    super.handleMessage(msg);
                    throw new UnsupportedOperationException();
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        // This method is invoked when the service is called.
        Toast.makeText(this, "Service was Created", Toast.LENGTH_LONG).show();
        //broadcaster = LocalBroadcastManager.getInstance(this);
        //context = this.getApplicationContext();

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.
        startForeground(001, getNotification());
    }

    public boolean isRunning() {
        return isRunning;
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
        return mMessenger.getBinder();
    }


    public void sendMsg(int message, double data) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                mClients.get(i).send(Message.obtain(null,
                        message, data));
            } catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

    public void sendMsg(int message) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                mClients.get(i).send(Message.obtain(null,
                        message));
            } catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

    private boolean doConnectionConditionalSend() {
        if (connected) {
            return true;
        }
        sendMsg(RESULTS.NOT_CONNECTED);
        return false;
    }

    private boolean doAuthenticationConditionalSend() {
        if (doConnectionConditionalSend()) {
            if (isAuthenticated()) {
                return true;
            } else {
                sendMsg(RESULTS.NOT_AUTHENTICATED);
            }
        }
        return false;
    }

    private void tryConnect(int timeout) {
        if (!connected) {
            deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);

            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!connected) {
                        deviceManager.cleanUp();
                        sendMsg(RESULTS.CONNECTION_TIMEOUT);
                    }
                }
            }, timeout);
        }
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

    private boolean isAuthenticated() {
        if (logginID != null) {
            return true;
        }
        return false;
    }

    private void setStress(ValueBundle v) {
        currentStress = v;
        //TODO: notify application of new value.
    }

    private void updateStress(ValueBundle stress) {
        if (isAuthenticated()) {
            if (currentStress != null) {
                if (checkIfSupportedInterval(stress)) {
                    StatisticsModel model = new StatisticsModel(logginID, stress.value, stress.timestamp);

                    WorkerEventListener ev = new WorkerEventListener() {
                        @Override
                        public void dataRecieved(StatisticsModel model) {
                            System.out.println(model);
                        }
                    };

                    StatisticsHttpWorker task = new StatisticsHttpWorker(EverCalmStatisticsEndpoint.API_URL, ev, model, StatisticsHttpWorker.SETTINGS.POST);
                    task.execute();
                }
            }
            currentStress = stress;
        }
    }

    private boolean checkIfSupportedInterval(final ValueBundle stress) {
        //TODO: check if last stress value is not from yesterday

        return true;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY; // or whatever your flag
    }

    private Notification getNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Service is running";

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_small)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Empatica Senses")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .build();

        // Send the notification.
        //mNM.notify(NOTIFICATION, notification);
        return notification;
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
    public void didUpdateStatus(EmpaStatus status) {
        if (status == EmpaStatus.READY) {
            // Start scanning
            deviceManager.startScanning();
        }

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
                sendMsg(RESULTS.CONNECTED);
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                sendMsg(RESULTS.CONNECTION_FAILED);
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
    private class UserNotLoggedInException extends Exception {
    }
}
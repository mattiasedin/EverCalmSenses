package com.evercalm.evercalmsenses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity  {
    /*
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long STREAMING_TIME = 10000; // Stops streaming 10 seconds after connection
    private static final String EMPATICA_API_KEY = "8a2036983aa448a9ac624064d72248c8";

    Intent mServiceIntent;
    private BroadcastReceiver receiver;
    private ProgressDialog dialog;
    private ServiceConnection connection;

    EmpaticaService mService;
    boolean mBound = false;

    boolean isConnected = false;
    private LocalBroadcastManager broadcaster;
    */

    boolean isConnected = false;

    /** Messenger for communicating with service. */
    Messenger mService = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;
    private ProgressDialog dialog;
    /** Some text view we are using to show state information. */

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EmpaticaService.RESULTS.NOT_CONNECTED:
                    throw new UnsupportedOperationException();
                    //broadcastToEmpatica(EmpaticaService.MESSAGES.CONNECT_TO_DEVICE);
                    //break;
                case EmpaticaService.RESULTS.CONNECTION_PENDING:
                    dialog.setMessage("Checking for device");
                    break; //throw new UnsupportedOperationException();
                case EmpaticaService.RESULTS.CONNECTED:
                    isConnected = true;
                    dialog.setMessage("Checking authentication status");
                    try {
                        sendMessageToService(EmpaticaService.MESSAGES.RETRIEVE_AUTH_STATUS);
                    } catch (RemoteException e) {

                    }
                    break;
                case EmpaticaService.RESULTS.CONNECTION_TIMEOUT:
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Connection timed out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, MainTabbedActivity.class));
                    break;
                case EmpaticaService.RESULTS.NOT_AUTHENTICATED:
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    break;
                case EmpaticaService.RESULTS.AUTHENTICATED:
                    startActivity(new Intent(MainActivity.this, MainTabbedActivity.class));
                    break;
                default:
                    super.handleMessage(msg);
                    throw new UnsupportedOperationException();
                    /*
                case EmpaticaService.RESULTS.CONNECTED:
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "connected",
                            Toast.LENGTH_SHORT).show();
                default:
                    super.handleMessage(msg);
                    */
            }
        }
    }


    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());


    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mService = new Messenger(service);
            try {

                sendMessageToService(EmpaticaService.MESSAGES.REGISTER_CLIENT);
            } catch (RemoteException e) {
            }

            Toast.makeText(getApplicationContext(), "Connected",
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;

            // As part of the sample, tell the user what happened.
            Toast.makeText(getApplicationContext(), "Disconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doBindService();
    }

    public void connectClick(View view)
    {

        dialog = ProgressDialog.show(MainActivity.this, "",
                "Loading. Please wait...", true);
        startService(new Intent(this, EmpaticaService.class));
        try {
            sendMessageToService(EmpaticaService.MESSAGES.CONNECT_TO_DEVICE);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(this,
                EmpaticaService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void sendMessageToService(int message, int value) throws RemoteException {
        Message msg = Message.obtain(null, message, this.hashCode(), value);
        msg.replyTo = mMessenger;
        mService.send(msg);
    }

    private void sendMessageToService(int message) throws RemoteException {
        Message msg = Message.obtain(null, message);
        msg.replyTo = mMessenger;
        mService.send(msg);
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    sendMessageToService(EmpaticaService.MESSAGES.UNREGISTER_CLIENT);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }



    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doBindService();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int resultMessage = intent.getIntExtra(EmpaticaService.EMPATICA_RESULT_URL, -1);
                switch (resultMessage) {
                    case EmpaticaService.RESULTS.NOT_CONNECTED:
                        broadcastToEmpatica(EmpaticaService.MESSAGES.CONNECT_TO_DEVICE);
                        break;
                    case EmpaticaService.RESULTS.CONNECTION_PENDING:
                        break;
                    case EmpaticaService.RESULTS.CONNECTED:
                        isConnected = true;
                        dialog.setMessage("Checking authentication status");
                        Intent i = new Intent(getApplicationContext(), EmpaticaService.class);
                        i.putExtra(EmpaticaService.EMPATICA_MESSAGE_URL, EmpaticaService.MESSAGES.RETRIEVE_LOGGING_STATUS);
                        mService.handleIntent(i);
                        //broadcastToEmpatica(EmpaticaService.MESSAGES.RETRIEVE_AUTH_STATUS);


                        break;
                    case EmpaticaService.RESULTS.CONNECTION_TIMEOUT:
                        dialog.dismiss();
                        Toast.makeText(context, "Connection timed out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, MainTabbedActivity.class));
                        break;
                    case EmpaticaService.RESULTS.NOT_AUTHENTICATED:
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        break;
                    case EmpaticaService.RESULTS.AUTHENTICATED:
                        startActivity(new Intent(MainActivity.this, MainTabbedActivity.class));
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        };


        IntentFilter filter = new IntentFilter();
        filter.addAction(EmpaticaService.EMPATICA_RESULT_URL);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);

        broadcaster = LocalBroadcastManager.getInstance(this);


    }

    private void broadcastToEmpatica(int message) {
        Intent intent = new Intent(getApplicationContext(), EmpaticaService.class);
        intent.putExtra(EmpaticaService.EMPATICA_MESSAGE_URL, message);
        broadcaster.sendBroadcast(intent);
    }

    private void processStartService(final String tag) {
        //Intent intent = new Intent(this, EmpaticaService.class);
        //intent.putExtra(EmpaticaService.EMPATICA_MESSAGE_URL, EmpaticaService.MESSAGES.CONNECT_TO_DEVICE);
        //startService(new Intent(this, EmpaticaService.class));
        startService(new Intent(this, EmpaticaService.class));
        bindService(new Intent(this, EmpaticaService.class), myConnection, BIND_AUTO_CREATE);

    }

    private IBinder myService;

    public ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            EmpaticaService.LocalBinder lbinder = (EmpaticaService.LocalBinder) binder;
            mService = lbinder.getService();
            mBound = true;
        }
        //binder comes from server to communicate with method's of

        public void onServiceDisconnected(ComponentName className) {
            mBound = false;
        }
    };


    public void connectClick(View view)
    {

        dialog = ProgressDialog.show(MainActivity.this, "",
                "Loading. Please wait...", true);

        processStartService(EmpaticaService.TAG);

    }


    @Override
    protected void onResume() {
        super.onResume();

        //LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
        //        new IntentFilter(EmpaticaService.EMPATICA_RESULT_URL)
        //);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) {
            //unbindService(myConnection);
            mBound = false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
    */
}

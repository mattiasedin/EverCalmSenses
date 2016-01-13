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
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity  {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long STREAMING_TIME = 10000; // Stops streaming 10 seconds after connection
    private static final String EMPATICA_API_KEY = "8a2036983aa448a9ac624064d72248c8";

    Intent mServiceIntent;
    private BroadcastReceiver receiver;
    private ProgressDialog dialog;
    private ServiceConnection connection;

    EmpaticaService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int resultMessage = intent.getIntExtra(EmpaticaService.EMPATICA_RESULT_URL, -1);
                switch (resultMessage) {
                    case EmpaticaService.RESULTS.CONNECTION_PENDING:
                        /*
                        dialog = ProgressDialog.show(MainActivity.this, "",
                                "Connecting to Empatica. Please wait...", true);
                                */
                        //dialog.setMessage("Connecting to Empatica");
                        /*dialog = ProgressDialog.show(MainActivity.this, "",
                                "Connecting to Empatica", true);*/
                        break;
                    case EmpaticaService.RESULTS.CONNECTED:
                        dialog.dismiss();
                        Toast.makeText(context, "Connected to Empatica!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, LoginSimpleActivity.class));
                        break;
                    case EmpaticaService.RESULTS.CONNECTION_TIMEOUT:
                        dialog.dismiss();
                        Toast.makeText(context, "Connection timed out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, MainTabbedActivity.class));
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        };

    }

    private void broadcastToEmpatica(int message) {
        Intent intent = new Intent(getApplicationContext(), EmpaticaService.class);
        intent.putExtra(EmpaticaService.EMPATICA_MESSAGE_URL, message);
    }

    private void processStartService(final String tag) {
        Intent intent = new Intent(getApplicationContext(), EmpaticaService.class);
        intent.putExtra(EmpaticaService.EMPATICA_MESSAGE_URL, EmpaticaService.MESSAGES.CONNECT_TO_DEVICE);
        intent.addCategory(tag);
        //bindService(intent, myConnection, BIND_AUTO_CREATE);
        startService(intent);

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
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(EmpaticaService.EMPATICA_RESULT_URL)
        );

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

}

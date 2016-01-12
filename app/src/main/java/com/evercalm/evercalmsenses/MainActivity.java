package com.evercalm.evercalmsenses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(EmpaticaService.EMPATICA_RESULT);
                switch (s) {
                    case "connecting":
                        /*
                        dialog = ProgressDialog.show(MainActivity.this, "",
                                "Connecting to Empatica. Please wait...", true);
                                */
                        //dialog.setMessage("Connecting to Empatica");
                        /*dialog = ProgressDialog.show(MainActivity.this, "",
                                "Connecting to Empatica", true);*/
                        break;
                    case "connected":
                        dialog.dismiss();
                        Toast.makeText(context, "Connected to Empatica!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, LoginSimpleActivity.class));
                        break;
                    case "connectingTimeout":
                        dialog.dismiss();
                        Toast.makeText(context, "Connection timed out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, MainTabbedActivity.class));
                        break;
                }
            }
        };

    }

    private void processStartService(final String tag) {
        Intent intent = new Intent(getApplicationContext(), EmpaticaService.class);
        intent.putExtra(EmpaticaService.EMPATICA_MESSAGE, true);
        intent.addCategory(tag);
        startService(intent);
    }


    public void connectClick(View view)
    {

        dialog = ProgressDialog.show(MainActivity.this, "",
                "Loading. Please wait...", true);

        processStartService(EmpaticaService.TAG);

    }


    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(EmpaticaService.EMPATICA_RESULT)
        );

    }

    @Override
    protected void onStop() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }


}

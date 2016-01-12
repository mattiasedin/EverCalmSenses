package com.evercalm.evercalmsenses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class LoginSimpleActivity extends AppCompatActivity {

    private BroadcastReceiver receiver;
    private TextView textView;
    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_simple);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        textView = (TextView) findViewById(R.id.section_label);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int data = intent.getIntExtra(EmpaticaService.EMPATICA_MESSAGE, -1);
                if (data != -1) {
                    textView.append("data: " + data + "\n");
                }

            }
        };

    }

    private void processStopService(final String tag) {
        Intent intent = new Intent(getApplicationContext(), EmpaticaService.class);
        intent.addCategory(tag);
        stopService(intent);
    }

    public void sendDataClick(View view) {
        Intent intent = new Intent(getApplicationContext(), EmpaticaService.class);
        intent.putExtra("sendData", true);
        intent.addCategory(EmpaticaService.TAG);
        startService(intent);
    }

    public void stopServiceClick(View view) {
        processStopService(EmpaticaService.TAG);
    }

    public void launchTestService() {
        // Construct our Intent specifying the Service
        mServiceIntent = new Intent(this, EmpaticaService.class);
        // Add extras to the bundle
        mServiceIntent.putExtra("sendData", true);
        // Start the service
        startService(mServiceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(EmpaticaService.EMPATICA_RESULT_DATA)
        );

    }

    @Override
    protected void onStop() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

}

package com.evercalm.evercalmsenses;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends ConnectedActivity  {

    private ProgressDialog dialog;

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
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
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
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHandler(new IncomingHandler());
        setContext(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}

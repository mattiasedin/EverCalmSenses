package com.evercalm.evercalmsenses;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import java.util.EventListener;

/**
 * Created by mattias on 2016-01-13.
 */
public abstract class ConnectedActivity extends Activity {
    private Context context;
    boolean isConnected = false;

    /** Messenger for communicating with service. */
    Messenger mService = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;

    private Messenger mMessenger;
    private ConnectionActivityCallbackListener ev;


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mService = new Messenger(service);
            try {
                sendMessageToService(EmpaticaService.MESSAGES.REGISTER_CLIENT);
            } catch (RemoteException e) {
            }
            if (ev != null) {
                ev.callbackPerformed();
            }
            Toast.makeText(context, "Connected",
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;

            // As part of the sample, tell the user what happened.
            Toast.makeText(context, "Disconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };

    public void setContext(Context c) {
        context = c;
    }

    public void setHandler(Handler h) {
        mMessenger = new Messenger(h);
    }

    public void setConnectionCallback(ConnectionActivityCallbackListener ev) {
        this.ev = ev;
    }

    public void sendMessageToService(int message, String value) throws RemoteException {
        Message msg = Message.obtain(null, message, value);
        msg.replyTo = mMessenger;
        mService.send(msg);
    }

    public void sendMessageToService(int message, int value) throws RemoteException {
        Message msg = Message.obtain(null, message, value);
        msg.replyTo = mMessenger;
        mService.send(msg);
    }

    public void sendMessageToService(int message) throws RemoteException {
        Message msg = Message.obtain(null, message);
        msg.replyTo = mMessenger;
        mService.send(msg);
    }

    public void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.

        context.bindService(new Intent(context,
                EmpaticaService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    public void doUnbindService() {
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
            context.unbindService(mConnection);
            mIsBound = false;
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        doBindService();
    }

    @Override
    protected void onPause() {
        doUnbindService();
        super.onPause();
    }
}

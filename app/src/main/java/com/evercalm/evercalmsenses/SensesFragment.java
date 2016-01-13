package com.evercalm.evercalmsenses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;


public class SensesFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private Switch mySwitch;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static Fragment newInstance() {
        SensesFragment fragment = new SensesFragment();
        /*
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        */
        return fragment;
    }

    private class ConnectActivity extends ConnectedActivity {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EmpaticaService.RESULTS.IS_LOGGING:
                    mySwitch.setChecked(true);
                    mySwitch.setClickable(true);
                    break;
                case EmpaticaService.RESULTS.NOT_LOGGING:
                    mySwitch.setChecked(false);
                    mySwitch.setClickable(true);
                    break;
                default:
                    super.handleMessage(msg);
                    throw new UnsupportedOperationException();
            }
        }
    }

    ConnectActivity connectActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //View rootView = inflater.inflate(R.layout.fragment_main_tabbed_tips, container, false);
        final View rootView = inflater.inflate(R.layout.fragment_main_tabbed_senses, container, false);

        mySwitch = (Switch) rootView.findViewById(R.id.loggingSwitch);

        //set the switch to ON
        mySwitch.setChecked(true);
        mySwitch.setClickable(false);
        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    try {
                        connectActivity.sendMessageToService(EmpaticaService.MESSAGES.START_LOGGING);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        connectActivity.sendMessageToService(EmpaticaService.MESSAGES.END_LOGGING);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        connectActivity = new ConnectActivity();
        connectActivity.setHandler(new IncomingHandler());
        connectActivity.setContext(rootView.getContext());
        connectActivity.setConnectionCallback(new ConnectionActivityCallbackListener() {
            @Override
            public void callbackPerformed() {
                try {
                    connectActivity.sendMessageToService(EmpaticaService.MESSAGES.RETRIEVE_LOGGING_STATUS);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        connectActivity.doBindService();
        /*
        try {
            connectActivity.sendMessageToService(EmpaticaService.MESSAGES.RETRIEVE_LOGGING_STATUS);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        */
    }

    @Override
    public void onPause() {
        connectActivity.doUnbindService();
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}

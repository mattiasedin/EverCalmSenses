package com.evercalm.evercalmsenses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Vector;


public class SensesFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final float MAX_VALUE = 0;
    private final float MIN_VALUE = -900;
    private final float MID_THRESHOLD = -600;
    private final float HIGH_THRESHOLD = -300;
    private final int MULTIPLIER = 300;

    private Switch mySwitch;
    private View rootView;
    private float currentValue;

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
                case EmpaticaService.RESULTS.NOT_AUTHENTICATED:
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    break;
                case EmpaticaService.RESULTS.NOT_CONNECTED:
                    startActivity(new Intent(getContext(), MainActivity.class));
                    break;
                case EmpaticaService.RESULTS.STRESS_DATA:
                    Double stressData = (double) msg.obj;
                    updateStressLevel(stressData.floatValue());
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
        rootView = inflater.inflate(R.layout.fragment_main_tabbed_senses, container, false);
        //final View rootView = inflater.inflate(R.layout.fragment_main_tabbed_senses, container, false);

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
                    connectActivity.sendMessageToService(EmpaticaService.MESSAGES.RETRIEVE_DATA);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        initStressLevel();

        return rootView;
    }


    /**
     * Initialize the visual stress-meter
     */
    public void initStressLevel(){
        ImageView vectorImage = (ImageView) rootView.findViewById(R.id.vector_image_content);
        ViewPropertyAnimator animator = vectorImage.animate().setDuration(1000).translationY(-MIN_VALUE);

        // Zero-set currentValue
        currentValue = MIN_VALUE;
    }

    /**
     * Update visual stress level
     * @param change the change in stress (+/-)
     */
    public void setStressLevel(float value){
        float scaledValue = value * MULTIPLIER;
        float newValue = currentValue + scaledValue;

        // Check value not out of bounds
        if((newValue <=  MAX_VALUE) && (newValue >= MIN_VALUE)){
            ImageView vectorImage = (ImageView) rootView.findViewById(R.id.vector_image_content);
            ViewPropertyAnimator animator = vectorImage.animate().translationYBy(-scaledValue);

            currentValue = newValue;

            if(currentValue > HIGH_THRESHOLD){
                vectorImage.setBackgroundColor(Color.RED);
            }else if(currentValue > MID_THRESHOLD){
                vectorImage.setBackgroundColor(Color.YELLOW);
            }else {
                vectorImage.setBackgroundColor(Color.GREEN);
            }
        }
    }
    /**
     * Update visual stress level
     * @param change the change in stress (+/-)
     */
    public void updateStressLevel(float change){
        float scaledValue = change * MULTIPLIER;
        float newValue = currentValue + scaledValue;

        // Check value not out of bounds
        if((newValue <=  MAX_VALUE) && (newValue >= MIN_VALUE)){
            ImageView vectorImage = (ImageView) rootView.findViewById(R.id.vector_image_content);
            ViewPropertyAnimator animator = vectorImage.animate().translationYBy(-scaledValue);

            currentValue = newValue;

            if(currentValue > HIGH_THRESHOLD){
                vectorImage.setBackgroundColor(Color.RED);
            }else if(currentValue > MID_THRESHOLD){
                vectorImage.setBackgroundColor(Color.YELLOW);
            }else {
                vectorImage.setBackgroundColor(Color.GREEN);
            }
        }
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

}

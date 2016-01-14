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

    public SensesFragment() {
    }


    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultMessage = intent.getIntExtra(EmpaticaService.EMPATICA_RESULT_URL, -1);
            switch (resultMessage) {
                case EmpaticaService.RESULTS.IS_LOGGING:
                    mySwitch.setChecked(true);
                    mySwitch.setClickable(true);
                    break;
                case EmpaticaService.RESULTS.NOT_LOGGING:
                    mySwitch.setChecked(false);
                    mySwitch.setClickable(true);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main_tabbed_senses, container, false);
        //final View rootView = inflater.inflate(R.layout.fragment_main_tabbed_senses, container, false);

        mySwitch = (Switch) rootView.findViewById(R.id.loggingSwitch);

        //set the switch to ON
        mySwitch.setChecked(true);
        mySwitch.setClickable(true);
        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(rootView.getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                    updateStressLevel((float)0.2);
                } else {
                    Toast.makeText(rootView.getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                    updateStressLevel((float)0.3);
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

    }

    @Override
    public void onPause() {
        super.onStop();
    }

}

package com.evercalm.evercalmsenses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
                    Toast.makeText(rootView.getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(rootView.getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return rootView;
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

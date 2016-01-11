package com.evercalm.evercalmsenses;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startBtnClick(View view)
    {
        Intent intent = new Intent(MainActivity.this, MainTabbedActivity.class);
        startActivity(intent);
    }
}

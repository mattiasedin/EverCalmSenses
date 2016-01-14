package com.evercalm.evercalmsenses.API;

import android.os.AsyncTask;

import java.io.IOException;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by mattias on 2016-01-12.
 */
public class StatisticsHttpWorker extends AsyncTask<Void, Void,
        StatisticsModel> {

    public enum SETTINGS { POST, UPDATE, GET, DELETE };
    SETTINGS choosenSetting;

    Retrofit retrofit;

    StatisticsModel model;
    String API_URL;
    String id;
    WorkerEventListener callback;

    public StatisticsHttpWorker(final String API_URL, WorkerEventListener callback, String id) {
        this(API_URL, callback, null, SETTINGS.GET);
        this.id = id;
    }

    public StatisticsHttpWorker(final String API_URL, WorkerEventListener callback, StatisticsModel model, SETTINGS s) {
        this.model = model;
        this.choosenSetting = s;
        this.API_URL = API_URL;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Override
    protected StatisticsModel doInBackground(Void... params) {
        EverCalmStatisticsEndpoint endpoint = retrofit.create(EverCalmStatisticsEndpoint.class);

        Call<StatisticsModel> call;

        switch (choosenSetting) {
            case GET:
                call = endpoint.getData(id);
                break;
            case UPDATE:
                throw new UnsupportedOperationException();
                //call = endpoint.updateData(id, new StatisticsModel(model.getId(), model.getData(), 0));
            case POST:
                call = endpoint.createDataPost(new StatisticsModel(model.getUserId(), model.getData(), model.getTimestamp()));
                break;
            case DELETE:
                throw new UnsupportedOperationException();
            default:
                call = null;
                break;
        }
        try {
            try {
                Response<StatisticsModel> response = call.execute();
                StatisticsModel body = response.body();
                return body;
            } catch (IOException e) {
                int a = 3;
            }
        } catch (Exception e) {
            int b = 2;
        }
        return null;
    }

    @Override
    protected void onPostExecute(StatisticsModel data) {
        callback.dataRecieved(data);
    }

}

package com.evercalm.evercalmsenses.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by mattias on 2016-01-12.
 */
public class StatisticsModel {

    @SerializedName("data")
    @Expose
    private double data;

    @SerializedName("userId")
    @Expose
    private String userId;

    @SerializedName("timestamp")
    @Expose
    private double timestamp;

    public StatisticsModel(String userId, double data, double timestamp) {
        this.userId = userId;
        this.data = data;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "userID: "+userId+" data: "+data+" timestamp: "+timestamp;
    }

    public String getUserId() {
        return userId;
    }
    public double getData() {
        return data;
    }
    public  double getTimestamp() {
        return timestamp;
    }
}

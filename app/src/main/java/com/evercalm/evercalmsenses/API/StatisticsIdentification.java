package com.evercalm.evercalmsenses.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by mattias on 2016-01-13.
 */
public class StatisticsIdentification {

    @SerializedName("id")
    @Expose
    private String id;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}

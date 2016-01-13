package com.evercalm.evercalmsenses.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by mattias on 2016-01-13.
 */
public class StatisticsUser {

    @SerializedName("email")
    @Expose
    private String email;

    public StatisticsUser(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

}

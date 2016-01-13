package com.evercalm.evercalmsenses.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by mattias on 2016-01-12.
 */
public class StatisticsModel {

    @SerializedName("_id")
    @Expose
    private String Id;

    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("timestamp")
    @Expose
    private String timestamp;

    @SerializedName("__v")
    @Expose
    private Integer V;

    public StatisticsModel(String id, String value, String timestamp, Integer v ) {
        this.Id = id;
        this.value = value;
        this.timestamp = timestamp;
        this.V = v;
    }


    /**
     *
     * @return
     * The Id
     */
    public String getId() {
        return Id;
    }

    /**
     *
     * @param Id
     * The _id
     */
    public void setId(String Id) {
        this.Id = Id;
    }

    /**
     *
     * @return
     * The value
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @param data
     * The value
     */
    public void setValue(String data) {
        this.value = data;
    }

    /**
     *
     * @return
     * The timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     *
     * @param timestamp
     * The timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     *
     * @return
     * The V
     */
    public Integer getV() {
        return V;
    }

    /**
     *
     * @param V
     * The __v
     */
    public void setV(Integer V) {
        this.V = V;
    }
}

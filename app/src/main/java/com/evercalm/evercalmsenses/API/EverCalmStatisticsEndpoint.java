package com.evercalm.evercalmsenses.API;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by mattias on 2016-01-12.
 */
public interface EverCalmStatisticsEndpoint {

    String API_URL = "http://130.239.239.0:9000"; //"http://evercalm-statistics.herokuapp.com";

    @GET("/api/sensedata/{id}")
    Call<StatisticsModel> getData(@Path("id") String id);

    /*
    @PUT("/api/empatica_datas/{id}")
    Call<StatisticsModel> updateData(@Path("id") String id, @Body StatisticsModel data);
    */

    @POST("/api/sensedata")
    Call<StatisticsModel> createDataPost(@Body StatisticsModel data);

    @POST("/api/login")
    Call<StatisticsUser> login(@Body StatisticsUser data);

    @POST("/api/login")
    Call<StatisticsIdentification> getLoginIdentification(@Body StatisticsUser data);

}

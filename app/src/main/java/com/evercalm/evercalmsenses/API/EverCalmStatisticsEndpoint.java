package com.evercalm.evercalmsenses.API;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by mattias on 2016-01-12.
 */
public interface EverCalmStatisticsEndpoint {
    @GET("/api/empatica_datas/{id}")
    Call<StatisticsModel> getData(@Path("id") String id);

    /*
    @PUT("/api/empatica_datas/{id}")
    Call<StatisticsModel> updateData(@Path("id") String id, @Body StatisticsModel data);
    */

    @POST("/api/empatica_datas")
    Call<StatisticsModel> createDataPost(@Body StatisticsModel data);
}

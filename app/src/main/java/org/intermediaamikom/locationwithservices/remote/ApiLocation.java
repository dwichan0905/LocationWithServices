package org.intermediaamikom.locationwithservices.remote;

import org.intermediaamikom.locationwithservices.remote.entity.LocationEntity;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiLocation {

    @GET("lokasi")
    Call<LocationEntity> sendLocation(
            @Query("lat") String latitude,
            @Query("lng") String longitude
    );
}

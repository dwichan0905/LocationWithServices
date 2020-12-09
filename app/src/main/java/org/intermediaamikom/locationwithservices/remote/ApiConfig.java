package org.intermediaamikom.locationwithservices.remote;

import android.content.Context;

import com.chuckerteam.chucker.api.ChuckerInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiConfig {
    public static ApiLocation getApiService(Context context) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new ChuckerInterceptor(context)) // Interceptor buat debugging retrofit
                .build();
        Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl("http://192.227.75.88:1337/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit.create(ApiLocation.class);
    }
}

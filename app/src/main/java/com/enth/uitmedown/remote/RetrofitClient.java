package com.enth.uitmedown.remote;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * initialize Retrofit
 */
public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static final String BASE_URL = "https://aptitude.my/2024699546/api/";

    public static Retrofit getClient() {

        // first API call, no retrofit instance yet?
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(60, TimeUnit.SECONDS) // Wait 60s for connection
                    .readTimeout(60, TimeUnit.SECONDS)    // Wait 60s for data to be read
                    .writeTimeout(60, TimeUnit.SECONDS)   // Wait 60s to send data
                    .build();
            // initialize retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        // return instance of retrofit
        return retrofit;
    }

}
package com.enth.uitmedown.remote;

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
            // initialize retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        // return instance of retrofit
        return retrofit;
    }

}
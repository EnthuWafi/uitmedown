package com.enth.uitmedown.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import com.enth.uitmedown.model.Item;

import java.util.List;

public interface ItemService {

    @GET("items")
    Call<List<Item>> getAllItems(@Query("api_key") String apiKey);

}
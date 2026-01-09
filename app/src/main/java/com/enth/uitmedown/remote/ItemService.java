package com.enth.uitmedown.remote;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

import com.enth.uitmedown.model.DeleteResponse;
import com.enth.uitmedown.model.Item;

import java.util.List;

public interface ItemService {

    @GET("items")
    Call<List<Item>> getAllItems(@Header("api_key") String apiKey);

    @GET("items/{id}")
    Call<Item> getItemById(@Header("api_key") String apiKey, @Path("id") int id);

    @Multipart
    @POST("files")
    Call<ResponseBody> uploadFile(@Header("api_key") String apiKey, @Part MultipartBody.Part file);
    @POST("items")
    Call<Item> createItem(@Header("api_key") String apiKey, @Body Item item);

    @PUT("items/{id}")
    Call<Item> updateItem(@Header("api_key") String apiKey, @Path("id") int id, @Body Item item);

    @DELETE("items/{id}")
    Call<DeleteResponse> deleteItem(@Header("api_key") String apiKey, @Path("id") int id);



}
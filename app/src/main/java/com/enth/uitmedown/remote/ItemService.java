package com.enth.uitmedown.remote;

import okhttp3.MultipartBody;
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
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

import com.enth.uitmedown.model.DeleteResponse;
import com.enth.uitmedown.model.FileModel;
import com.enth.uitmedown.model.Item;

import java.util.List;
import java.util.Map;

public interface ItemService {

    @GET("items")
    Call<List<Item>> getAllItems(@Header("api_key") String apiKey);

    @GET("items/{id}")
    Call<Item> getItemById(@Header("api_key") String apiKey, @Path("id") int id);

    @GET("items")
    Call<List<Item>> getItems(@Header("api_key") String apiKey, @QueryMap Map<String, String> options);

    @GET("items")
    Call<List<Item>> getItemsBySellerId(@Header("api_key") String apiKey, @Query("seller_id[in]") int sellerId);


    @Multipart
    @POST("files")
    Call<FileModel> uploadFile(@Header("api_key") String apiKey, @Part MultipartBody.Part file);
    @POST("items")
    Call<Item> createItem(@Header("api_key") String apiKey, @Body Item item);

    @PUT("items/{id}")
    Call<Item> updateItem(@Header("api_key") String apiKey, @Path("id") int id, @Body Item item);

    @DELETE("items/{id}")
    Call<DeleteResponse> deleteItem(@Header("api_key") String apiKey, @Path("id") int id);



}
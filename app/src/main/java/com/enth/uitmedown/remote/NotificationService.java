package com.enth.uitmedown.remote;

import com.enth.uitmedown.model.DeleteResponse;
import com.enth.uitmedown.model.Notification;
import com.enth.uitmedown.model.Transaction;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotificationService {

    @GET("notifications")
    Call<List<Notification>> getAllTransactions(@Header("api_key") String apiKey);

    @GET("notifications/{id}")
    Call<Notification> getTransactionById(@Header("api_key") String apiKey, @Path("id") int id);

    @POST("notifications")
    Call<Notification> createTransaction(@Header("api_key") String apiKey, @Body Notification notification);

    @PUT("notifications/{id}")
    Call<Notification> updateTransaction(@Header("api_key") String apiKey, @Path("id") int id, @Body Notification notification);

    @DELETE("notifications/{id}")
    Call<DeleteResponse> deleteTransaction(@Header("api_key") String apiKey, @Path("id") int id);


}

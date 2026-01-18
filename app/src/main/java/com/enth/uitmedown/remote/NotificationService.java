package com.enth.uitmedown.remote;

import com.enth.uitmedown.model.DeleteResponse;
import com.enth.uitmedown.model.Notification;
import com.enth.uitmedown.model.MyNotificationResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface NotificationService {

    @GET("notifications")
    Call<List<Notification>> getAllNotification(@Header("api_key") String apiKey);

    @GET("notifications/{id}")
    Call<Notification> getNotificationById(@Header("api_key") String apiKey, @Path("id") int id);

    @GET("notifications")
    Call<List<Notification>> getNotifications(@Header("api_key") String apiKey, @QueryMap Map<String, String> options);

    @GET("notifications/expanded")
    Call<List<MyNotificationResponse>> getNotificationsByReceiverId(
            @Header("api_key") String token,
            @Query("receiver_id") int receiverId
    );
    @POST("notifications")
    Call<Notification> createNotification(@Header("api_key") String apiKey, @Body Notification notification);

    @PUT("notifications/{id}")
    Call<Notification> updateNotification(@Header("api_key") String apiKey, @Path("id") int id, @Body Notification notification);

    @FormUrlEncoded
    @PUT("notifications/{id}")
    Call<Notification> updateNotificationStatus(
            @Header("api_key") String token,
            @Path("id") int id,
            @Field("is_read") int isRead
    );

    @DELETE("notifications/{id}")
    Call<DeleteResponse> deleteNotification(@Header("api_key") String apiKey, @Path("id") int id);

}

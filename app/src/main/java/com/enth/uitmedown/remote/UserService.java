package com.enth.uitmedown.remote;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import com.enth.uitmedown.model.FileModel;
import com.enth.uitmedown.model.User;

import java.util.List;
import java.util.Map;

public interface UserService {

    @GET("users")
    Call<List<User>> getAllUsers(@Header("api_key") String token);

    @GET("users")
    Call<List<User>> getUsers(@Header("api_key") String token, @QueryMap Map<String, String> options);


    @FormUrlEncoded
    @POST("users/login")
    Call<User> login(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("users/login")
    Call<User> loginEmail(@Field("email") String email, @Field("password") String password);

    @GET("users/{id}")
    Call<User> getUser(@Header("api_key") String token, @Path("id") int id);

    @PUT("users/{id}")
    Call<User> updateUser(
            @Header("api_key") String token,
            @Path("id") int id,
            @Body User user
    );

    @PUT("users/{id}")
    Call<User> updateUser(
            @Header("api_key") String token,
            @Path("id") int id,
            @QueryMap Map<String, String> options);
    @Multipart
    @POST("files")
    Call<FileModel> uploadProfilePicture(@Header("api_key") String token, @Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("users/register")
    Call<User> register(
            @Field("email") String email,
            @Field("password") String password
    );


    @FormUrlEncoded
    @POST("users/change-password")
    Call<User> changePassword(
            @Field("email") String email,
            @Field("password") String oldPassword,
            @Field("new_password") String newPassword
    );

    @FormUrlEncoded
    @POST("users/forgot-password")
    Call<User> forgotPassword(
            @Field("email") String email
    );

    @FormUrlEncoded
    @POST("users/set-password")
    Call<User> setPasswordByAdmin(
            @Field("email") String targetUserEmail,
            @Field("password") String newPasswordForUser,
            @Field("admin_email") String adminEmail,
            @Field("admin_password") String adminPassword
    );

}
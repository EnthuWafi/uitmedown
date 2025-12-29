package com.enth.uitmedown.remote;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import com.enth.uitmedown.model.User;

public interface UserService {

    @FormUrlEncoded
    @POST("users/login")
    Call<User> login(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("users/login")
    Call<User> loginEmail(@Field("email") String email, @Field("password") String password);

}
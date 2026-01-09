package com.enth.uitmedown.remote;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import com.enth.uitmedown.model.DeleteResponse;
import com.enth.uitmedown.model.Transaction;

import java.util.List;

public interface TransactionService {

    @GET("transactions")
    Call<List<Transaction>> getAllTransactions(@Header("api_key") String apiKey);

    @GET("transactions/{id}")
    Call<Transaction> getTransactionById(@Header("api_key") String apiKey, @Path("id") int id);

    @POST("transactions")
    Call<Transaction> createTransaction(@Header("api_key") String apiKey, @Body Transaction transaction);

    @PUT("transactions/{id}")
    Call<Transaction> updateTransaction(@Header("api_key") String apiKey, @Path("id") int id, @Body Transaction transaction);

    @DELETE("transactions/{id}")
    Call<DeleteResponse> deleteTransaction(@Header("api_key") String apiKey, @Path("id") int id);



}
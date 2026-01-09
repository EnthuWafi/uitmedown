package com.enth.uitmedown.remote;


import com.enth.uitmedown.model.Notification;

public class ApiUtils {


    // return UserService instance
    public static UserService getUserService() {
        return RetrofitClient.getClient().create(UserService.class);
    }

    public static ItemService getItemService() {
        return RetrofitClient.getClient().create(ItemService.class);
    }

    public static TransactionService getTransactionService() {
        return RetrofitClient.getClient().create(TransactionService.class);
    }
    public static NotificationService getNotificationService() {
        return RetrofitClient.getClient().create(NotificationService.class);
    }



}
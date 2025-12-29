package com.enth.uitmedown.remote;


public class ApiUtils {


    // return UserService instance
    public static UserService getUserService() {
        return RetrofitClient.getClient().create(UserService.class);
    }

    public static ItemService getItemService() {
        return RetrofitClient.getClient().create(ItemService.class);
    }

}
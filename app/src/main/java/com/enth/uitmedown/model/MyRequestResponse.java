package com.enth.uitmedown.model;

import com.google.gson.annotations.SerializedName;

public class MyRequestResponse {

    @SerializedName("transaction_id")
    private int transactionId;

    @SerializedName("item_id")
    private int itemId;

    @SerializedName("buyer_id")
    private int buyerId;

    @SerializedName("amount")
    private double amount;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("meetup_location")
    private String meetupLocation;

    @SerializedName("seller_note")
    private String sellerNote;

    @SerializedName("title")
    private String itemTitle;

    @SerializedName("price")
    private double itemPrice;

    @SerializedName("buyer_name")
    private String buyerName;

    // Getters
    public int getTransactionId() { return transactionId; }
    public int getItemId() { return itemId; }
    public int getBuyerId() { return buyerId; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getMeetupLocation() { return meetupLocation; }
    public String getSellerNote() { return sellerNote; }
    public String getItemTitle() { return itemTitle; }
    public double getItemPrice() { return itemPrice; }
    public String getBuyerName() { return buyerName; }
}
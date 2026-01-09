package com.enth.uitmedown.model;

import com.google.gson.annotations.SerializedName;
public class Transaction {
    @SerializedName("transaction_id")
    private Integer transactionId;

    @SerializedName("buyer_id")
    private Integer buyerId;

    @SerializedName("seller_id")
    private Integer sellerId;

    @SerializedName("item_id")
    private Integer itemId;

    @SerializedName("price")
    private Double price;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Integer buyerId) {
        this.buyerId = buyerId;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

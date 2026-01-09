package com.enth.uitmedown.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Item implements Serializable {

    // Use Integer/Double objects (wrappers) instead of int/double primitives
    // to prevent crashes if the server sends null.
    @SerializedName("item_id")
    private Integer itemId;

    @SerializedName("seller_id")
    private Integer sellerId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private Double price;

    @SerializedName("category")
    private String category;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("file")
    private File file;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    // Empty Constructor (Required by some libraries)
    public Item() {}

    // Getters and Setters (Use Alt+Insert in Android Studio to generate these)

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
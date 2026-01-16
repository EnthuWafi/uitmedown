package com.enth.uitmedown.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserDetail implements Serializable {
    @SerializedName("user_detail_id")
    private Integer userDetailId;

    @SerializedName("name")
    private String name;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("picture_file_id")
    private int pictureFileId;

    @SerializedName("picture_file")
    private FileModel pictureFile;

    public Integer getUserDetailId() {
        return userDetailId;
    }

    public void setUserDetailId(Integer userDetailId) {
        this.userDetailId = userDetailId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getPictureFileId() {
        return pictureFileId;
    }

    public void setPictureFileId(int pictureFileId) {
        this.pictureFileId = pictureFileId;
    }

    public FileModel getPictureFile() {
        return pictureFile;
    }

    public void setPictureFile(FileModel pictureFile) {
        this.pictureFile = pictureFile;
    }
}

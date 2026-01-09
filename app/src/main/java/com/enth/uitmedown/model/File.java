package com.enth.uitmedown.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class File implements Serializable {
    @SerializedName("id")
    private Integer id;

    @SerializedName("file")
    private String file;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}

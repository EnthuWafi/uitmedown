package com.enth.uitmedown.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable  {

    // represent a record in users tables
    @SerializedName("id")
    private int id;
    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;
    @SerializedName("token")
    private String token;
    @SerializedName("lease")
    private String lease;

    @SerializedName("role")
    private String role;
    @SerializedName("is_active")
    private int isActive;
    @SerializedName("secret")
    private String secret;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLease() {
        return lease;
    }

    public void setLease(String lease) {
        this.lease = lease;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }


}

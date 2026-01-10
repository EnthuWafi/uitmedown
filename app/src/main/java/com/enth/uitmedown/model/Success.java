package com.enth.uitmedown.model;

import java.io.Serializable;

class Success implements Serializable {
    public int code;
    public String status;

    public Success() {
    }

    public Success(int code, String status) {
        this.code = code;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "Success{" +
                "code=" + code +
                ", status='" + status + '\'' +
                '}';
    }
}

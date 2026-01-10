package com.enth.uitmedown.model;

import java.io.Serializable;

public class DeleteResponse implements Serializable {
    private int status;
    private Success success;

    public DeleteResponse() {
        status = -1;
        success = null;
    }

    public DeleteResponse(int status, Success success) {
        this.status = status;
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Success getSuccess() {
        return success;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "DeleteResponse{" +
                "status=" + status +
                ", success=" + success +
                '}';
    }
}
package com.enth.uitmedown.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class MyNotificationResponse implements Serializable {

    @SerializedName("notification_id")
    private Integer notificationId;

    @SerializedName("title")
    private String title;

    @SerializedName("event_id")
    private String eventId;

    @SerializedName("template_text")
    private String templateText;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("item_title")
    private String itemTitle;

    @SerializedName("transaction_id")
    private Integer transactionId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("is_read")
    private Integer isRead;

    // --- Getters ---
    public Integer getNotificationId() { return notificationId; }
    public String getTitle() { return (title != null) ? title : "Notification"; }
    public Integer getTransactionId() { return transactionId; }
    public String getEventId() { return eventId; }
    public String getCreatedAt() { return createdAt; }

    public String getSenderName() { return (senderName != null) ? senderName : "Unknown"; }
    public String getItemTitle() { return (itemTitle != null) ? itemTitle : "Item"; }
    public String getTemplateText() { return (templateText != null) ? templateText : ""; }

    public boolean isRead() { return isRead != null && isRead == 1; }
}
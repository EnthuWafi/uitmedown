package com.enth.uitmedown.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Event implements Serializable {
    @SerializedName("event_id")
    private String eventId;

    @SerializedName("template_text")
    private String templateText;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTemplateText() {
        return templateText;
    }

    public void setTemplateText(String templateText) {
        this.templateText = templateText;
    }
}

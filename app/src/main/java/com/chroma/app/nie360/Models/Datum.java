package com.chroma.app.nie360.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Datum {

    @SerializedName("event_name")
    @Expose
    private String eventName;
    @SerializedName("id")
    @Expose
    private Integer id;

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("eventName", eventName).append("id", id).toString();
    }

}
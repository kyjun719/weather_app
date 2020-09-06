package com.jun.weather.repository.web.api.entity;

import com.google.gson.annotations.SerializedName;

public class Response<T> {
    @SerializedName("header")
    public ResHeader header;

    @SerializedName("body")
    public ResBody<T> body;
}

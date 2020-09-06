package com.jun.weather.repository.web.api.entity;

import com.google.gson.annotations.SerializedName;

public class ResBody<T> {
    @SerializedName("dataType")
    public String dataType;

    @SerializedName("items")
    public ResItems<T> items;

    @SerializedName("pageNo")
    public int pageNo;

    @SerializedName("numOfRows")
    public int numOfRows;

    @SerializedName("totalCount")
    public int totalCount;
}

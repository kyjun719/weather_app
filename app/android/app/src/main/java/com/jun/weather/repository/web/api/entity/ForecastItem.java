package com.jun.weather.repository.web.api.entity;

import com.google.gson.annotations.SerializedName;

public class ForecastItem extends ResponseItem{
    @SerializedName("fcstDate")
    public String fcstDate;

    @SerializedName("fcstTime")
    public String fcstTime;

    @SerializedName("fcstValue")
    public String fcstValue;
}

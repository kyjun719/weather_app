package com.jun.weather.repository.web.api.entity;

import com.google.gson.annotations.SerializedName;

public class ResHeader {
    @SerializedName("resultCode")
    public String resultCode;

    @SerializedName("resultMsg")
    public String resultMsg;
}

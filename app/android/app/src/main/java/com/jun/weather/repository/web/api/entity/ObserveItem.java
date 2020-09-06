package com.jun.weather.repository.web.api.entity;

import com.google.gson.annotations.SerializedName;

public class ObserveItem extends ResponseItem {
    @SerializedName("obsrValue")
    public String obsrValue;
}

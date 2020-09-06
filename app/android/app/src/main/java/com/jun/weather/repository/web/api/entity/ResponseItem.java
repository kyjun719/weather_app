package com.jun.weather.repository.web.api.entity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class ResponseItem {
    @SerializedName("baseDate")
    public String baseDate;

    @SerializedName("baseTime")
    public String baseTime;

    @SerializedName("category")
    public String category;

    @SerializedName("nx")
    public int nx;

    @SerializedName("ny")
    public int ny;

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}

package com.jun.weather.repository.web.api.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResItems<T> {
    @SerializedName("item")
    public List<T> item;
}

package com.jun.weather.repository.web.api.entity;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("response")
    public Response<T> response;
}

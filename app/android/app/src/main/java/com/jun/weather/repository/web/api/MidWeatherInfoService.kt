package com.jun.weather.repository.web.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface MidWeatherInfoService {
    @GET("getMidLandFcst")
    Call<ApiResponse<MidLandItem>> getMidLandFcst(@QueryMap Map<String, Object> query);

    @GET("getMidTa")
    Call<ApiResponse<MidTempItem>> getMidTa(@QueryMap Map<String, Object> query);
}
